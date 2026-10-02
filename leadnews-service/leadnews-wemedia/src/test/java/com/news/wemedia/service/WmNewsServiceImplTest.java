package com.news.wemedia.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.news.apis.article.IArticleClient;
import com.news.common.constants.WemediaConstants;
import com.news.model.wemedia.dtos.WmNewsDto;
import com.news.model.wemedia.pojos.WmNews;
import com.news.model.wemedia.pojos.WmUser;
import com.news.utils.thread.WmThreadLocalUtil;
import com.news.wemedia.repository.WmChannelRepository;
import com.news.wemedia.repository.WmNewsRepository;
import com.news.wemedia.repository.WmUserRepository;
import com.news.wemedia.service.impl.WemediaPersistenceService;
import com.news.wemedia.service.impl.WmNewsServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
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
    void schedulesReviewOnlyAfterDatabasePersistenceReturns() {
        WmNewsRepository newsRepository = mock(WmNewsRepository.class);
        WmUserRepository userRepository = mock(WmUserRepository.class);
        WmChannelRepository channelRepository = mock(WmChannelRepository.class);
        WemediaPersistenceService persistenceService = mock(WemediaPersistenceService.class);
        WmNewsTaskService taskService = mock(WmNewsTaskService.class);
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        IArticleClient articleClient = mock(IArticleClient.class);
        WmNewsServiceImpl service = new WmNewsServiceImpl(
                newsRepository, userRepository, channelRepository, persistenceService,
                taskService, kafkaTemplate, articleClient, new ObjectMapper());

        WmUser currentUser = new WmUser();
        currentUser.setId(7);
        WmThreadLocalUtil.setUser(currentUser);
        Date publishTime = new Date(5_000);
        WmNewsDto request = new WmNewsDto();
        request.setContent("[]");
        request.setStatus(WmNews.Status.SUBMIT.getCode());
        request.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
        request.setPublishTime(publishTime);
        WmNews saved = new WmNews();
        saved.setId(10);
        saved.setPublishTime(publishTime);
        when(persistenceService.saveNewsAndRelations(
                any(WmNews.class), eq(7), anyList(), anyList(),
                eq(WemediaConstants.WM_CONTENT_REFERENCE),
                eq(WemediaConstants.WM_COVER_REFERENCE))).thenReturn(saved);

        service.submit(request);

        InOrder order = inOrder(persistenceService, taskService);
        order.verify(persistenceService).saveNewsAndRelations(
                any(WmNews.class), eq(7), anyList(), anyList(),
                eq(WemediaConstants.WM_CONTENT_REFERENCE),
                eq(WemediaConstants.WM_COVER_REFERENCE));
        order.verify(taskService).addNewsToTask(10, publishTime);
    }

    @Test
    void scopesAuthorDetailAndDownOrUpOperationsToCurrentUser() {
        WmNewsRepository newsRepository = mock(WmNewsRepository.class);
        WmUserRepository userRepository = mock(WmUserRepository.class);
        WmChannelRepository channelRepository = mock(WmChannelRepository.class);
        WemediaPersistenceService persistenceService = mock(WemediaPersistenceService.class);
        WmNewsTaskService taskService = mock(WmNewsTaskService.class);
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        IArticleClient articleClient = mock(IArticleClient.class);
        WmNewsServiceImpl service = new WmNewsServiceImpl(
                newsRepository, userRepository, channelRepository, persistenceService,
                taskService, kafkaTemplate, articleClient, new ObjectMapper());
        WmUser currentUser = new WmUser();
        currentUser.setId(7);
        WmThreadLocalUtil.setUser(currentUser);
        when(newsRepository.findByIdAndUserId(99, 7)).thenReturn(Optional.empty());

        WmNewsDto request = new WmNewsDto();
        request.setId(99);
        request.setEnable((short) 1);
        service.downOrUp(request);
        service.getOne(99);

        verify(newsRepository, times(2)).findByIdAndUserId(99, 7);
        verify(newsRepository, never()).save(any(WmNews.class));
    }
}
