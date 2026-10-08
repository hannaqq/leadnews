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
    private final WmNewsPublishServiceImpl service = new WmNewsPublishServiceImpl(
            scheduleClient,
            articleClient,
            newsRepository,
            userRepository,
            channelRepository);

    @Test
    void approvedFutureNewsIsScheduledBeforeBecomingPublishable() {
        WmNews news = news(new Date(System.currentTimeMillis() + 60_000));
        when(scheduleClient.addTask(any())).thenReturn(ResponseResult.okResult(1L));

        service.reviewApproved(news);

        InOrder order = inOrder(scheduleClient, newsRepository);
        order.verify(scheduleClient).addTask(any());
        order.verify(newsRepository).save(news);
        assertEquals(WmNews.Status.SUCCESS.getCode(), news.getStatus());
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
    void stalePublishTaskDoesNothing() {
        Date expected = new Date(10_000);
        WmNews news = approved(new Date(20_000));
        when(newsRepository.findById(10)).thenReturn(Optional.of(news));

        service.publishScheduled(10, expected);

        verify(articleClient, never()).saveArticle(any());
    }

    @Test
    void validPublishTaskWritesAnIdempotentArticle() {
        Date expected = new Date(10_000);
        WmNews news = approved(expected);
        when(newsRepository.findById(10)).thenReturn(Optional.of(news));
        when(articleClient.saveArticle(any())).thenReturn(
                new ResponseResult<>(AppHttpCodeEnum.SUCCESS.getCode(), 100L));

        service.publishScheduled(10, expected);

        ArgumentCaptor<ArticleDto> article = ArgumentCaptor.forClass(ArticleDto.class);
        verify(articleClient).saveArticle(article.capture());
        assertEquals(10, article.getValue().getSourceNewsId());
        assertEquals(WmNews.Status.PUBLISHED.getCode(), news.getStatus());
        assertEquals(100L, news.getArticleId());
        verify(newsRepository).save(news);
    }

    @Test
    void failedPublishReturnsToWaitingAndCreatesRetryTask() {
        Date expected = new Date(10_000);
        WmNews news = approved(expected);
        when(newsRepository.findById(10)).thenReturn(Optional.of(news));
        when(articleClient.saveArticle(any())).thenThrow(new IllegalStateException("unavailable"));
        when(scheduleClient.addTask(any())).thenReturn(ResponseResult.okResult(2L));

        service.publishScheduled(10, expected);

        assertEquals(WmNews.Status.SUCCESS.getCode(), news.getStatus());
        verify(newsRepository).save(news);
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
