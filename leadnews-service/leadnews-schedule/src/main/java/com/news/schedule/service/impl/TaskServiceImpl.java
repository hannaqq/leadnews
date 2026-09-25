package com.news.schedule.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import com.news.common.constants.ScheduleConstants;
import com.news.common.redis.CacheService;
import com.news.model.schedule.dtos.Task;
import com.news.model.schedule.pojos.Taskinfo;
import com.news.model.schedule.pojos.TaskinfoLogs;
import com.news.schedule.mapper.TaskinfoLogsMapper;
import com.news.schedule.mapper.TaskinfoMapper;
import com.news.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
@Service
@Slf4j
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    
    /**
     * Add a delayed task to the system
     * Persists task to database first, then adds to Redis cache based on execution time
     *
     * @param task Task to be scheduled
     * @return Task ID assigned by database
     */
    @Override
    public long addTask(Task task) {

        boolean success = addTaskToDb(task);
        if(success){
            addTaskToCache(task);
        }

        return task.getTaskId();
    }

    /**
     * Cancel a scheduled task
     * Updates task status in database and removes from Redis cache
     *
     * @param taskId Task ID to cancel
     * @return true if task was successfully cancelled, false otherwise
     */
    @Override
    public boolean cancelTask(long taskId) {
        boolean flag = false;
        Task task = updateDb(taskId,ScheduleConstants.CANCELLED);

        if(task != null){
            removeTaskFromCache(task);
            flag = true;
        }
        return flag;
    }

    /**
     * Poll and consume a ready task from Redis List
     * Tasks are consumed in FIFO order and marked as executed in database
     *
     * @param type Task type identifier
     * @param priority Task priority level
     * @return Task if available, null if no task ready
     */
    @Override
    @SneakyThrows
    public Task poll(int type, int priority) {
        String key = type +"_"+priority;
        String task_json = cacheService.lRightPop(ScheduleConstants.TOPIC+key);

        Task task = null;
        if(StringUtils.isNotBlank(task_json)){
            Task polled = objectMapper.readValue(task_json, Task.class);
            // Only return the task if SCHEDULED -> EXECUTED succeeds; otherwise it was cancelled
            task = updateDb(polled.getTaskId(), ScheduleConstants.EXECUTED);
        }

        return task;
    }

    /**
     * Remove task from Redis cache
     * Removes from List if task is ready, or from ZSet if still scheduled
     *
     * @param task Task to remove
     */
    @SneakyThrows
    private void removeTaskFromCache(Task task) {
        String key = task.getTaskType() + "_" + task.getPriority();
        if(task.getExecuteTime()<=System.currentTimeMillis()){
            cacheService.lRemove(ScheduleConstants.TOPIC+key,0,objectMapper.writeValueAsString(task));
        } else {
            cacheService.zRemove(ScheduleConstants.FUTURE+key,0,objectMapper.writeValueAsString(task));
        }
    }

    /**
     * Finalize a task with state-machine conditional update.
     * Only allows SCHEDULED -> EXECUTED / CANCELLED to avoid execute/cancel races.
     *
     * @return task when transition succeeds; null if already finalized
     */
    private Task updateDb(long taskId, int status) {
        TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(taskId);
        if (taskinfoLogs == null) {
            return null;
        }

        int rows = taskinfoLogsMapper.update(null, Wrappers.<TaskinfoLogs>lambdaUpdate()
                .set(TaskinfoLogs::getStatus, status)
                .eq(TaskinfoLogs::getTaskId, taskId)
                .eq(TaskinfoLogs::getStatus, ScheduleConstants.SCHEDULED));

        if (rows == 0) {
            log.warn("task {} already finalized, skip transition to status={}", taskId, status);
            return null;
        }

        taskinfoMapper.deleteById(taskId);

        taskinfoLogs.setStatus(status);
        Task task = new Task();
        BeanUtils.copyProperties(taskinfoLogs, task);
        task.setExecuteTime(taskinfoLogs.getExecuteTime().getTime());
        return task;
    }

    private final CacheService cacheService;

    /**
     * Add task to Redis cache based on execution time window
     * Tasks within 5 minutes are cached in Redis:
     * - Immediate tasks (executeTime <= now) → List (topic_*)
     * - Future tasks (executeTime <= now + 5min) → ZSet (future_*)
     * Tasks beyond 5 minutes are only stored in database
     *
     * @param task Task to cache
     */
    @SneakyThrows
    private void addTaskToCache(Task task) {
        String key = task.getTaskType()+"_"+task.getPriority();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE,5);
        long nextScheduleTime = calendar.getTimeInMillis();

        if(task.getExecuteTime()<=System.currentTimeMillis()){
            // Add to List for immediate execution
            cacheService.lLeftPush(ScheduleConstants.TOPIC+key, objectMapper.writeValueAsString(task));
        }else if(task.getExecuteTime() <= nextScheduleTime){
            // Add to ZSet for future execution (within 5 minutes)
            cacheService.zAdd(ScheduleConstants.FUTURE+key,objectMapper.writeValueAsString(task),task.getExecuteTime());
        }

    }

    private final TaskinfoMapper taskinfoMapper;

    private final TaskinfoLogsMapper taskinfoLogsMapper;

    private final ObjectMapper objectMapper;

    private boolean addTaskToDb(Task task){
        boolean flag = false;
        Taskinfo taskinfo = new Taskinfo();
        BeanUtils.copyProperties(task,taskinfo);

        taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
        taskinfoMapper.insert(taskinfo);

        task.setTaskId(taskinfo.getTaskId());

        TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
        BeanUtils.copyProperties(taskinfo,taskinfoLogs);
        taskinfoLogs.setVersion(1);
        taskinfoLogs.setStatus(ScheduleConstants.SCHEDULED);
        taskinfoLogsMapper.insert(taskinfoLogs);
        flag = true;
        return flag;
    }

    /**
     * Scheduled refresh mechanism - runs every minute
     * Migrates expired tasks from ZSet (future_*) to List (topic_*) for execution
     * Uses distributed lock to prevent concurrent execution in multi-instance deployments
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void refresh(){

        String token = cacheService.tryLock("FUTURE_TASK_SYNC", 1000 * 30);
        if(StringUtils.isNotBlank(token)){
            Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
            for (String futureKey : futureKeys) {
                String topicKey = ScheduleConstants.TOPIC+futureKey.split(ScheduleConstants.FUTURE)[1];
                // Query tasks with score <= current time (expired tasks)
                Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());

                if(!tasks.isEmpty()){
                    // Batch migrate using Pipeline for better performance
                    cacheService.refreshWithPipeline(futureKey,topicKey,tasks);
                }
            }
        }
    }

    /**
     * Data recovery mechanism - runs on startup and every 5 minutes
     * Clears Redis cache and reloads tasks from database to ensure consistency
     * Only loads tasks scheduled within the next 5 minutes to Redis
     * This prevents data loss if Redis cache is cleared or service restarts
     */
    @PostConstruct
    @Scheduled(cron = "0 */5 * * * ?")
    public void reloadData(){
        // Clear all cached tasks
        Set<String> topicKeys = cacheService.scan(ScheduleConstants.TOPIC + "*");
        Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
        cacheService.delete(topicKeys);
        cacheService.delete(futureKeys);

        // Reload tasks scheduled within next 5 minutes from database
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE,5);
        List<Taskinfo> taskinfos = taskinfoMapper.selectList(Wrappers.<Taskinfo>lambdaQuery().lt(Taskinfo::getExecuteTime, calendar.getTime()));

        if(taskinfos != null && taskinfos.size() != 0){
            for (Taskinfo taskinfo : taskinfos) {
                Task task = new Task();
                BeanUtils.copyProperties(taskinfo,task);
                task.setExecuteTime(taskinfo.getExecuteTime().getTime());
                addTaskToCache(task);
            }
        }
    }
}
