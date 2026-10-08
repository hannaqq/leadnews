package com.news.schedule.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import com.news.common.constants.ScheduleConstants;
import com.news.common.redis.CacheService;
import com.news.model.schedule.dtos.Task;
import com.news.model.schedule.pojos.Taskinfo;
import com.news.schedule.repository.TaskinfoRepository;
import com.news.schedule.service.TaskService;
import com.news.schedule.service.transaction.ScheduleTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
@Slf4j
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final CacheService cacheService;
    private final ScheduleTransactionService transactionService;
    private final TaskinfoRepository taskinfoRepository;
    private final ObjectMapper objectMapper;

    @Override
    public long addTask(Task task) {
        task.setTaskId(transactionService.addTaskToDB(task));
        addTaskToCache(task);
        return task.getTaskId();
    }

    @Override
    public boolean cancelTask(long taskId) {
        boolean flag = false;
        Task task = transactionService.cancelTaskInDB(taskId);

        if(task != null){
            removeTaskFromCache(task);
            flag = true;
        }
        return flag;
    }

    @Override
    public Task poll(int type, int priority) {
        String key = type +"_"+priority;
        String task_json = cacheService.lRightPop(ScheduleConstants.READY+key);

        Task task = null;
        if(StringUtils.isNotBlank(task_json)){
            Task polled = fromJson(task_json);
            // Only return the task if SCHEDULED -> EXECUTED succeeds; otherwise it was cancelled
            task = transactionService.markExecutedInDB(polled.getTaskId());
        }

        return task;
    }

    private void removeTaskFromCache(Task task) {
        String key = task.getTaskType() + "_" + task.getPriority();
        String json = toJson(task);
        cacheService.lRemove(ScheduleConstants.READY + key, 0, json);
        cacheService.zRemove(ScheduleConstants.FUTURE + key, json);
    }


    private void addTaskToCache(Task task) {
        String key = task.getTaskType()+"_"+task.getPriority();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE,5);
        long nextScheduleTime = calendar.getTimeInMillis();

        if(task.getExecuteTime()<=System.currentTimeMillis()){
            cacheService.lLeftPush(ScheduleConstants.READY+key, toJson(task));
        }else if(task.getExecuteTime() <= nextScheduleTime){
            cacheService.zAdd(ScheduleConstants.FUTURE+key,toJson(task),task.getExecuteTime());
        }

    }


    @Scheduled(cron = "0 */1 * * * ?")
    public void refresh(){

        String token = cacheService.tryLock("FUTURE_TASK_SYNC", 1000 * 30);
        if(StringUtils.isNotBlank(token)){
            Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
            for (String futureKey : futureKeys) {
                String readyKey = ScheduleConstants.READY+futureKey.split(ScheduleConstants.FUTURE)[1];
                Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());

                if(!tasks.isEmpty()){
                    org.springframework.util.StopWatch stopWatch = new org.springframework.util.StopWatch("Task-Migration");
                    stopWatch.start("Pipeline_Batch_Migration");
                    cacheService.refreshWithPipeline(futureKey,readyKey,tasks);

                    stopWatch.stop();
                    log.info("Task migration completed, performance report:\n{}", stopWatch.prettyPrint());
                }
            }
        }
    }

    @PostConstruct
    @Scheduled(cron = "0 */5 * * * ?")
    public void reloadData(){
        Set<String> readyKeys = cacheService.scan(ScheduleConstants.READY + "*");
        Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
        cacheService.delete(readyKeys);
        cacheService.delete(futureKeys);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE,5);
        List<Taskinfo> taskinfos = taskinfoRepository.findByExecuteTimeBefore(calendar.getTime());

        if(taskinfos != null && !taskinfos.isEmpty()){
            for (Taskinfo taskinfo : taskinfos) {
                Task task = new Task();
                BeanUtils.copyProperties(taskinfo,task);
                task.setExecuteTime(taskinfo.getExecuteTime().getTime());
                addTaskToCache(task);
            }
        }
    }

    private Task fromJson(String taskJson) {
        try {
            return objectMapper.readValue(taskJson, Task.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize delayed task", exception);
        }
    }

    private String toJson(Task task) {
        try {
            return objectMapper.writeValueAsString(task);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize delayed task " + task.getTaskId(), exception);
        }
    }
}
