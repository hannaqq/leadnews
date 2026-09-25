package com.news.wemedia.service.impl;

import com.news.apis.schedule.IScheduleClient;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.TaskTypeEnum;
import com.news.model.schedule.dtos.Task;
import com.news.model.wemedia.pojos.WmNews;
import com.news.utils.common.ProtostuffUtil;
import com.news.wemedia.service.WmNewsAutoScanService;
import com.news.wemedia.service.WmNewsTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
@Service
@Slf4j
public class WmNewsTaskServiceImpl implements WmNewsTaskService {

    @Autowired
    private IScheduleClient iScheduleClient;

    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Async
    public void addNewsToTask(Integer id, Date publishTime) {
        log.info("begin to add news to task........");
        WmNews wmNews = new WmNews();
        wmNews.setId(id);
        Task task = new Task();
        task.setExecuteTime(publishTime.getTime());
        task.setTaskType(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType());
        task.setPriority(TaskTypeEnum.NEWS_SCAN_TIME.getPriority());

        task.setParameters(ProtostuffUtil.serialize(wmNews));

        iScheduleClient.addTask(task);

        log.info("end adding news to task................");

    }

    @Scheduled(fixedRate = 1000)
    @Override
    public void scanNewsByTask() {
        log.info("scanning task....");
        ResponseResult poll = iScheduleClient.poll(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType(), TaskTypeEnum.NEWS_SCAN_TIME.getPriority());
        if(poll.getCode().equals(200) && poll.getData() != null){
            Task task = objectMapper.convertValue(poll.getData(), Task.class);
            WmNews wmNews = ProtostuffUtil.deserialize(task.getParameters(), WmNews.class);
            wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
        }
    }

}
