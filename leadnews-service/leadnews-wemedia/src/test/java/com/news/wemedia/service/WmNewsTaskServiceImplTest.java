package com.news.wemedia.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.news.apis.schedule.IScheduleClient;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.schedule.dtos.Task;
import com.news.model.wemedia.dtos.NewsPublishTaskPayload;
import com.news.model.wemedia.pojos.WmNews;
import com.news.utils.common.ProtostuffUtil;
import com.news.wemedia.service.impl.WmNewsTaskServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WmNewsTaskServiceImplTest {

    private final IScheduleClient scheduleClient = mock(IScheduleClient.class);
    private final WmNewsPublishService publishService = mock(WmNewsPublishService.class);
    private final WmNewsTaskServiceImpl service = new WmNewsTaskServiceImpl(
            scheduleClient, publishService, new ObjectMapper());

    @Test
    void publishesUsingExpectedTimeFromTaskPayload() {
        Date expected = new Date(10_000);
        when(scheduleClient.poll(1003, 1)).thenReturn(taskResult(10, expected, 3L));

        service.scanPublishTasks();

        verify(publishService).publishScheduled(10, expected, 3L);
    }

    @Test
    void acceptsLegacyTaskPayloadDuringRollingUpgrade() {
        Date expected = new Date(20_000);
        WmNews payload = new WmNews();
        payload.setId(11);
        payload.setPublishTime(expected);
        Task task = new Task();
        task.setParameters(ProtostuffUtil.serialize(payload));
        when(scheduleClient.poll(1003, 1)).thenReturn(
                new ResponseResult<>(AppHttpCodeEnum.SUCCESS.getCode(), task));

        service.scanPublishTasks();

        verify(publishService).publishScheduled(11, expected, 0L);
    }

    private static ResponseResult taskResult(Integer newsId, Date publishTime, Long scheduleVersion) {
        NewsPublishTaskPayload payload =
                new NewsPublishTaskPayload(newsId, publishTime, scheduleVersion);
        Task task = new Task();
        task.setParameters(ProtostuffUtil.serialize(payload));
        return new ResponseResult<>(AppHttpCodeEnum.SUCCESS.getCode(), task);
    }
}
