package com.news.schedule.service.impl;

import com.news.common.constants.ScheduleConstants;
import com.news.model.schedule.dtos.Task;
import com.news.model.schedule.pojos.Taskinfo;
import com.news.model.schedule.pojos.TaskinfoLogs;
import com.news.schedule.repository.TaskinfoLogsRepository;
import com.news.schedule.repository.TaskinfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchedulePersistenceService {
    private final TaskinfoRepository taskinfoRepository;
    private final TaskinfoLogsRepository taskinfoLogsRepository;

    @Transactional
    public long addTask(Task task) {
        Taskinfo taskinfo = new Taskinfo();
        BeanUtils.copyProperties(task, taskinfo);
        taskinfo.setTaskId(null);
        taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
        taskinfo = taskinfoRepository.saveAndFlush(taskinfo);

        TaskinfoLogs log = new TaskinfoLogs();
        BeanUtils.copyProperties(taskinfo, log);
        log.setVersion(null);
        log.setStatus(ScheduleConstants.SCHEDULED);
        taskinfoLogsRepository.save(log);
        return taskinfo.getTaskId();
    }

    @Transactional
    public Task finalizeTask(long taskId, int nextStatus) {
        TaskinfoLogs log = taskinfoLogsRepository.findById(taskId).orElse(null);
        if (log == null) {
            return null;
        }
        int rows = taskinfoLogsRepository.finalizeScheduledTask(
                taskId, ScheduleConstants.SCHEDULED, nextStatus);
        if (rows == 0) {
            return null;
        }
        taskinfoRepository.deleteById(taskId);

        Task task = new Task();
        BeanUtils.copyProperties(log, task);
        task.setExecuteTime(log.getExecuteTime().getTime());
        return task;
    }

    @Transactional(readOnly = true)
    public List<Taskinfo> findTasksBefore(Date executeTime) {
        return taskinfoRepository.findByExecuteTimeBefore(executeTime);
    }
}
