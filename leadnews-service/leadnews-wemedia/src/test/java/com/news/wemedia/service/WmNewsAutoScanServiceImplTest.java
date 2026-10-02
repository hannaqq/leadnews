package com.news.wemedia.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.news.apis.article.IArticleClient;
import com.news.file.service.FileStorageService;
import com.news.model.wemedia.pojos.WmNews;
import com.news.wemedia.repository.WmChannelRepository;
import com.news.wemedia.repository.WmNewsRepository;
import com.news.wemedia.repository.WmSensitiveRepository;
import com.news.wemedia.repository.WmUserRepository;
import com.news.wemedia.service.impl.WmNewsAutoScanServiceImpl;
import com.news.wemedia.service.impl.WemediaPersistenceService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

class WmNewsAutoScanServiceImplTest {
    private final WmNewsRepository newsRepository = mock(WmNewsRepository.class);
    private final WemediaPersistenceService persistenceService = mock(WemediaPersistenceService.class);
    private final WmSensitiveRepository sensitiveRepository = mock(WmSensitiveRepository.class);
    private final IArticleClient articleClient = mock(IArticleClient.class);
    private final WmChannelRepository channelRepository = mock(WmChannelRepository.class);
    private final WmUserRepository userRepository = mock(WmUserRepository.class);
    private final FileStorageService storageService = mock(FileStorageService.class);
    private final AwsModerationService moderationService = mock(AwsModerationService.class);
    private final WmNewsAutoScanServiceImpl service = new WmNewsAutoScanServiceImpl(
            newsRepository, persistenceService, sensitiveRepository, articleClient, channelRepository,
            userRepository, storageService, moderationService, new ObjectMapper());

    @Test
    void scansInlineImageBlocksUsingSingularImageType() {
        WmNews news = submittedNews();
        when(persistenceService.claimNewsForProcessing(10)).thenReturn(true);
        when(persistenceService.transitionProcessingStatus(
                10, WmNews.Status.FAIL.getCode(),
                "AWS Rekognition: image contains explicit/sensitive content")).thenReturn(true);
        when(newsRepository.findById(10)).thenReturn(Optional.of(news));
        when(sensitiveRepository.findAll()).thenReturn(List.of());
        when(storageService.downLoadFile("inline-image")).thenReturn(new byte[]{1});
        when(moderationService.scanImageWithAwsRekognition(any(byte[].class))).thenReturn(false);

        service.autoScanWmNews(10);

        assertEquals(WmNews.Status.FAIL.getCode(), news.getStatus());
        verify(moderationService).scanImageWithAwsRekognition(any(byte[].class));
        verify(articleClient, never()).saveArticle(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void sendsNewsToManualReviewWhenImageModerationFails() {
        WmNews news = submittedNews();
        when(persistenceService.claimNewsForProcessing(10)).thenReturn(true);
        when(persistenceService.transitionProcessingStatus(
                10, WmNews.Status.ADMIN_AUTH.getCode(),
                "image moderation unavailable; manual review required")).thenReturn(true);
        when(newsRepository.findById(10)).thenReturn(Optional.of(news));
        when(sensitiveRepository.findAll()).thenReturn(List.of());
        when(storageService.downLoadFile("inline-image"))
                .thenThrow(new IllegalStateException("storage unavailable"));

        service.autoScanWmNews(10);

        assertEquals(WmNews.Status.ADMIN_AUTH.getCode(), news.getStatus());
        assertEquals("image moderation unavailable; manual review required", news.getReason());
        verify(articleClient, never()).saveArticle(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void skipsReviewWhenAnotherWorkerAlreadyClaimedTheNews() {
        when(persistenceService.claimNewsForProcessing(10)).thenReturn(false);

        service.autoScanWmNews(10);

        verify(newsRepository, never()).findById(10);
        verify(articleClient, never()).saveArticle(org.mockito.ArgumentMatchers.any());
    }

    private static WmNews submittedNews() {
        WmNews news = new WmNews();
        news.setId(10);
        news.setStatus(WmNews.Status.SUBMIT.getCode());
        news.setContent("[{\"type\":\"image\",\"value\":\"inline-image\"}]");
        return news;
    }
}
