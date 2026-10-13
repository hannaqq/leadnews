package com.news.wemedia.service.impl;

import com.news.apis.schedule.IScheduleClient;
import lombok.RequiredArgsConstructor;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.TaskTypeEnum;
import com.news.model.schedule.dtos.Task;
import com.news.model.wemedia.dtos.NewsPublishTaskPayload;
import com.news.model.wemedia.pojos.WmNews;
import com.news.utils.common.ProtostuffUtil;
import com.news.wemedia.service.WmNewsTaskService;
import com.news.wemedia.service.WmNewsPublishService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class WmNewsTaskServiceImpl implements WmNewsTaskService {

    private final IScheduleClient iScheduleClient;

    private final WmNewsPublishService publishService;

    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = 1000)
    @Override
    public void scanPublishTasks() {
        ResponseResult poll = iScheduleClient.poll(
                TaskTypeEnum.NEWS_PUBLISH.getTaskType(),
                TaskTypeEnum.NEWS_PUBLISH.getPriority());
        if (poll.getCode().equals(200) && poll.getData() != null) {
            Task task = objectMapper.convertValue(poll.getData(), Task.class);
            NewsPublishTaskPayload payload = deserializePayload(task.getParameters());
            publishService.publishScheduled(
                    payload.getNewsId(),
                    payload.getExpectedPublishTime(),
                    payload.getScheduleVersion());
        }
    }

    private NewsPublishTaskPayload deserializePayload(byte[] parameters) {
        try {
            NewsPublishTaskPayload payload =
                    ProtostuffUtil.deserialize(parameters, NewsPublishTaskPayload.class);
            if (payload.getNewsId() != null
                    && payload.getExpectedPublishTime() != null
                    && payload.getScheduleVersion() != null) {
                return payload;
            }
        } catch (RuntimeException ignored) {
            // Fall through to the pre-schedule-version payload used by already queued tasks.
        }

        WmNews legacy = ProtostuffUtil.deserialize(parameters, WmNews.class);
        return new NewsPublishTaskPayload(legacy.getId(), legacy.getPublishTime(), 0L);
    }
}
