package com.news.wemedia.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.news.apis.article.IArticleClient;
import com.news.common.constants.WemediaConstants;
import com.news.model.wemedia.dtos.NewsAuthDto;
import com.news.model.wemedia.dtos.WmNewsDto;
import com.news.model.wemedia.pojos.WmNews;
import com.news.utils.thread.WmThreadLocalUtil;
import com.news.wemedia.repository.WmNewsRepository;
import com.news.wemedia.repository.WmUserRepository;
import com.news.wemedia.service.transaction.WmNewsTransactionService;
import com.news.wemedia.service.impl.WmNewsServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WmNewsServiceImplTest {

    @AfterEach
    void clearThreadLocal() {
        WmThreadLocalUtil.clear();
    }

    @Test
    void reviewsSubmittedNewsDirectlyAfterDatabasePersistenceReturns() {
        WmNewsRepository newsRepository = mock(WmNewsRepository.class);
        WmUserRepository userRepository = mock(WmUserRepository.class);
        WmNewsTransactionService transactionService = mock(WmNewsTransactionService.class);
        WmNewsPublishService publishService = mock(WmNewsPublishService.class);
        WmNewsAutoScanService autoScanService = mock(WmNewsAutoScanService.class);
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        IArticleClient articleClient = mock(IArticleClient.class);
        WmNewsServiceImpl service = new WmNewsServiceImpl(
                newsRepository, userRepository, transactionService,
                publishService, autoScanService, kafkaTemplate, articleClient, new ObjectMapper());

        WmThreadLocalUtil.setUserId(7);
        Date publishTime = new Date(5_000);
        WmNewsDto request = new WmNewsDto();
        request.setContent("[]");
        request.setStatus(WmNews.Status.SUBMIT.getCode());
        request.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
        request.setPublishTime(publishTime);
        WmNews saved = new WmNews();
        saved.setId(10);
        saved.setPublishTime(publishTime);
        when(transactionService.saveNewsAndRelations(
                any(WmNews.class), eq(7), anyList(), anyList(),
                eq(WemediaConstants.WM_CONTENT_REFERENCE),
                eq(WemediaConstants.WM_COVER_REFERENCE))).thenReturn(saved);

        service.submit(request);

        InOrder order = inOrder(transactionService, autoScanService);
        order.verify(transactionService).saveNewsAndRelations(
                any(WmNews.class), eq(7), anyList(), anyList(),
                eq(WemediaConstants.WM_CONTENT_REFERENCE),
                eq(WemediaConstants.WM_COVER_REFERENCE));
        order.verify(autoScanService).autoScanWmNews(10);
    }

    @Test
    void scopesAuthorDetailAndDownOrUpOperationsToCurrentUser() {
        WmNewsRepository newsRepository = mock(WmNewsRepository.class);
        WmUserRepository userRepository = mock(WmUserRepository.class);
        WmNewsTransactionService transactionService = mock(WmNewsTransactionService.class);
        WmNewsPublishService publishService = mock(WmNewsPublishService.class);
        WmNewsAutoScanService autoScanService = mock(WmNewsAutoScanService.class);
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        IArticleClient articleClient = mock(IArticleClient.class);
        WmNewsServiceImpl service = new WmNewsServiceImpl(
                newsRepository, userRepository, transactionService,
                publishService, autoScanService, kafkaTemplate, articleClient, new ObjectMapper());
        WmThreadLocalUtil.setUserId(7);
        when(newsRepository.findByIdAndUserId(99, 7)).thenReturn(Optional.empty());

        WmNewsDto request = new WmNewsDto();
        request.setId(99);
        request.setEnable((short) 1);
        service.downOrUp(request);
        service.getOne(99);

        verify(newsRepository, times(2)).findByIdAndUserId(99, 7);
        verify(newsRepository, never()).save(any(WmNews.class));
    }

    @Test
    void editingApprovedUnpublishedNewsForcesAnotherReview() {
        WmNewsRepository newsRepository = mock(WmNewsRepository.class);
        WmUserRepository userRepository = mock(WmUserRepository.class);
        WmNewsTransactionService transactionService = mock(WmNewsTransactionService.class);
        WmNewsPublishService publishService = mock(WmNewsPublishService.class);
        WmNewsAutoScanService autoScanService = mock(WmNewsAutoScanService.class);
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        IArticleClient articleClient = mock(IArticleClient.class);
        WmNewsServiceImpl service = new WmNewsServiceImpl(
                newsRepository, userRepository, transactionService,
                publishService, autoScanService, kafkaTemplate, articleClient, new ObjectMapper());
        WmThreadLocalUtil.setUserId(7);

        WmNewsDto request = new WmNewsDto();
        request.setId(10);
        request.setContent("[]");
        request.setStatus(WmNews.Status.SUCCESS.getCode());
        request.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
        request.setPublishTime(new Date(10_000));
        WmNews saved = new WmNews();
        saved.setId(10);
        when(transactionService.saveNewsAndRelations(
                any(WmNews.class), eq(7), anyList(), anyList(),
                eq(WemediaConstants.WM_CONTENT_REFERENCE),
                eq(WemediaConstants.WM_COVER_REFERENCE))).thenReturn(saved);

        service.submit(request);

        ArgumentCaptor<WmNews> incoming = ArgumentCaptor.forClass(WmNews.class);
        verify(transactionService).saveNewsAndRelations(
                incoming.capture(), eq(7), anyList(), anyList(),
                eq(WemediaConstants.WM_CONTENT_REFERENCE),
                eq(WemediaConstants.WM_COVER_REFERENCE));
        assertEquals(WmNews.Status.SUBMIT.getCode(), incoming.getValue().getStatus());
        verify(autoScanService).autoScanWmNews(10);
    }

    @Test
    void failedManualApprovalReturnsNewsToManualReview() {
        WmNewsRepository newsRepository = mock(WmNewsRepository.class);
        WmUserRepository userRepository = mock(WmUserRepository.class);
        WmNewsTransactionService transactionService = mock(WmNewsTransactionService.class);
        WmNewsPublishService publishService = mock(WmNewsPublishService.class);
        WmNewsAutoScanService autoScanService = mock(WmNewsAutoScanService.class);
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        IArticleClient articleClient = mock(IArticleClient.class);
        WmNewsServiceImpl service = new WmNewsServiceImpl(
                newsRepository, userRepository, transactionService,
                publishService, autoScanService, kafkaTemplate, articleClient, new ObjectMapper());
        WmNews news = new WmNews();
        news.setId(10);
        news.setStatus(WmNews.Status.ADMIN_AUTH.getCode());
        when(newsRepository.findById(10)).thenReturn(Optional.of(news));
        doThrow(new IllegalStateException("unavailable"))
                .when(publishService).reviewApproved(news);
        NewsAuthDto request = new NewsAuthDto();
        request.setId(10);

        service.authPass(request);

        assertEquals(WmNews.Status.ADMIN_AUTH.getCode(), news.getStatus());
        verify(newsRepository, times(2)).save(news);
    }
}
