package com.news.wemedia.service;

import com.news.apis.article.IArticleClient;
import com.news.apis.schedule.IScheduleClient;
import com.news.model.article.dtos.ArticleDto;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.schedule.dtos.Task;
import com.news.model.wemedia.pojos.WmNews;
import com.news.wemedia.repository.WmChannelRepository;
import com.news.wemedia.repository.WmNewsRepository;
import com.news.wemedia.repository.WmUserRepository;
import com.news.wemedia.service.impl.WmNewsPublishServiceImpl;
import com.news.wemedia.service.transaction.WmNewsScheduleTransactionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WmNewsPublishServiceImplTest {

    private final IScheduleClient scheduleClient = mock(IScheduleClient.class);
    private final IArticleClient articleClient = mock(IArticleClient.class);
    private final WmNewsRepository newsRepository = mock(WmNewsRepository.class);
    private final WmUserRepository userRepository = mock(WmUserRepository.class);
    private final WmChannelRepository channelRepository = mock(WmChannelRepository.class);
    private final WmNewsScheduleTransactionService scheduleTransactionService =
            mock(WmNewsScheduleTransactionService.class);
    private final WmNewsPublishServiceImpl service = new WmNewsPublishServiceImpl(
            scheduleClient,
            articleClient,
            newsRepository,
            userRepository,
            channelRepository,
            scheduleTransactionService);

    @Test
    void approvedFutureNewsIsScheduledBeforeBecomingPublishable() {
        Date publishTime = new Date(System.currentTimeMillis() + 60_000);
        WmNews news = news(publishTime);
        WmNewsScheduleTransactionService.ScheduleChange change =
                new WmNewsScheduleTransactionService.ScheduleChange(10, null, 1L, publishTime);
        when(scheduleTransactionService.prepareApprovedSchedule(10, publishTime)).thenReturn(change);
        when(scheduleClient.addTask(any())).thenReturn(ResponseResult.okResult(1L));
        when(scheduleTransactionService.attachTask(10, 1L, 1L)).thenReturn(true);

        service.reviewApproved(news);

        InOrder order = inOrder(scheduleTransactionService, scheduleClient);
        order.verify(scheduleTransactionService).prepareApprovedSchedule(10, publishTime);
        order.verify(scheduleClient).addTask(any());
        order.verify(scheduleTransactionService).attachTask(10, 1L, 1L);
        verify(articleClient, never()).saveArticle(any());
    }

    @Test
    void missingPublishTimePublishesImmediately() {
        WmNews news = news(null);
        when(articleClient.saveArticle(any())).thenReturn(
                new ResponseResult<>(AppHttpCodeEnum.SUCCESS.getCode(), 100L));

        service.reviewApproved(news);

        assertNotNull(news.getPublishTime());
        assertEquals(WmNews.Status.PUBLISHED.getCode(), news.getStatus());
        verify(scheduleClient, never()).addTask(any());
        verify(articleClient).saveArticle(any());
    }

    @Test
    void reschedulingCancelsOldTaskAndAttachesReplacement() {
        Date requestedPublishTime = new Date(System.currentTimeMillis() + 60_123);
        Date persistedPublishTime = new Date(requestedPublishTime.getTime() - 123);
        WmNewsScheduleTransactionService.ScheduleChange change =
                new WmNewsScheduleTransactionService.ScheduleChange(
                        10, 7L, 4L, persistedPublishTime);
        when(scheduleTransactionService.reschedule(10, 20, requestedPublishTime)).thenReturn(change);
        when(scheduleClient.cancelTask(7L)).thenReturn(ResponseResult.okResult(true));
        when(scheduleClient.addTask(any())).thenReturn(ResponseResult.okResult(8L));
        when(scheduleTransactionService.attachTask(10, 4L, 8L)).thenReturn(true);

        service.reschedule(10, 20, requestedPublishTime);

        ArgumentCaptor<Task> task = ArgumentCaptor.forClass(Task.class);
        verify(scheduleClient).cancelTask(7L);
        verify(scheduleClient).addTask(task.capture());
        assertEquals(persistedPublishTime.getTime(), task.getValue().getExecuteTime());
        verify(scheduleTransactionService).attachTask(10, 4L, 8L);
    }

    @Test
    void stalePublishTaskDoesNothing() {
        Date expected = new Date(10_000);
        when(scheduleTransactionService.claimPublication(10, expected, 1L))
                .thenReturn(Optional.empty());

        service.publishScheduled(10, expected, 1L);

        verify(articleClient, never()).saveArticle(any());
    }

    @Test
    void validPublishTaskWritesAnIdempotentArticle() {
        Date expected = new Date(10_000);
        WmNews news = approved(expected);
        when(scheduleTransactionService.claimPublication(10, expected, 1L))
                .thenReturn(Optional.of(news));
        when(articleClient.saveArticle(any())).thenReturn(
                new ResponseResult<>(AppHttpCodeEnum.SUCCESS.getCode(), 100L));
        when(scheduleTransactionService.completePublication(10, 1L, 100L)).thenReturn(true);

        service.publishScheduled(10, expected, 1L);

        ArgumentCaptor<ArticleDto> article = ArgumentCaptor.forClass(ArticleDto.class);
        verify(articleClient).saveArticle(article.capture());
        assertEquals(10, article.getValue().getSourceNewsId());
        verify(scheduleTransactionService).completePublication(10, 1L, 100L);
    }

    @Test
    void failedPublishReturnsToWaitingAndCreatesRetryTask() {
        Date expected = new Date(10_000);
        WmNews news = approved(expected);
        when(scheduleTransactionService.claimPublication(10, expected, 1L))
                .thenReturn(Optional.of(news));
        when(articleClient.saveArticle(any())).thenThrow(new IllegalStateException("unavailable"));
        when(scheduleTransactionService.restoreWaitingForRetry(10, 1L)).thenReturn(true);
        when(scheduleClient.addTask(any())).thenReturn(ResponseResult.okResult(2L));
        when(scheduleTransactionService.attachTask(10, 1L, 2L)).thenReturn(true);

        service.publishScheduled(10, expected, 1L);

        verify(scheduleTransactionService).restoreWaitingForRetry(10, 1L);
        verify(scheduleClient).addTask(any(Task.class));
    }

    private static WmNews approved(Date publishTime) {
        WmNews news = news(publishTime);
        news.setStatus(WmNews.Status.SUCCESS.getCode());
        return news;
    }

    private static WmNews news(Date publishTime) {
        WmNews news = new WmNews();
        news.setId(10);
        news.setUserId(20);
        news.setChannelId(30);
        news.setType((short) 0);
        news.setPublishTime(publishTime);
        news.setContent("[]");
        return news;
    }
}
