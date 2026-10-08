package com.news.wemedia.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.news.file.service.FileStorageService;
import com.news.model.wemedia.pojos.WmNews;
import com.news.wemedia.repository.WmNewsRepository;
import com.news.wemedia.repository.WmSensitiveRepository;
import com.news.wemedia.service.impl.WmNewsAutoScanServiceImpl;
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
    private final WmSensitiveRepository sensitiveRepository = mock(WmSensitiveRepository.class);
    private final FileStorageService storageService = mock(FileStorageService.class);
    private final AwsModerationService moderationService = mock(AwsModerationService.class);
    private final WmNewsPublishService publishService = mock(WmNewsPublishService.class);
    private final WmNewsAutoScanServiceImpl service = new WmNewsAutoScanServiceImpl(
            newsRepository, sensitiveRepository, storageService,
            moderationService, new ObjectMapper(), publishService);

    @Test
    void scansInlineImageBlocksUsingSingularImageType() {
        WmNews news = submittedNews();
        when(newsRepository.findById(10)).thenReturn(Optional.of(news));
        when(sensitiveRepository.findAll()).thenReturn(List.of());
        when(storageService.downLoadFile("inline-image")).thenReturn(new byte[]{1});
        when(moderationService.scanImageWithAwsRekognition(any(byte[].class)))
                .thenReturn(ModerationResult.REJECTED);

        service.autoScanWmNews(10);

        assertEquals(WmNews.Status.FAIL.getCode(), news.getStatus());
        verify(moderationService).scanImageWithAwsRekognition(any(byte[].class));
        verify(publishService, never()).reviewApproved(any());
    }

    @Test
    void sendsNewsToManualReviewWhenImageModerationFails() {
        WmNews news = submittedNews();
        when(newsRepository.findById(10)).thenReturn(Optional.of(news));
        when(sensitiveRepository.findAll()).thenReturn(List.of());
        when(storageService.downLoadFile("inline-image"))
                .thenThrow(new IllegalStateException("storage unavailable"));

        service.autoScanWmNews(10);

        assertEquals(WmNews.Status.ADMIN_AUTH.getCode(), news.getStatus());
        assertEquals("automatic review unavailable; manual review required", news.getReason());
        verify(publishService, never()).reviewApproved(any());
    }

    @Test
    void sendsNewsToManualReviewWhenRekognitionCannotDecide() {
        WmNews news = submittedNews();
        when(newsRepository.findById(10)).thenReturn(Optional.of(news));
        when(sensitiveRepository.findAll()).thenReturn(List.of());
        when(storageService.downLoadFile("inline-image")).thenReturn(new byte[]{1});
        when(moderationService.scanImageWithAwsRekognition(any(byte[].class)))
                .thenReturn(ModerationResult.MANUAL_REVIEW);

        service.autoScanWmNews(10);

        assertEquals(WmNews.Status.ADMIN_AUTH.getCode(), news.getStatus());
        verify(publishService, never()).reviewApproved(any());
    }

    @Test
    void schedulesPublicationWhenRekognitionApprovesImage() {
        WmNews news = submittedNews();
        news.setUserId(20);
        news.setChannelId(30);
        news.setType((short) 1);
        when(newsRepository.findById(10)).thenReturn(Optional.of(news));
        when(sensitiveRepository.findAll()).thenReturn(List.of());
        when(storageService.downLoadFile("inline-image")).thenReturn(new byte[]{1});
        when(moderationService.scanImageWithAwsRekognition(any(byte[].class)))
                .thenReturn(ModerationResult.APPROVED);

        service.autoScanWmNews(10);

        verify(publishService).reviewApproved(news);
    }

    @Test
    void skipsReviewWhenNewsIsNotSubmitted() {
        WmNews news = submittedNews();
        news.setStatus(WmNews.Status.PROCESSING.getCode());
        when(newsRepository.findById(10)).thenReturn(Optional.of(news));

        service.autoScanWmNews(10);

        verify(newsRepository, never()).save(any());
        verify(publishService, never()).reviewApproved(any());
    }

    private static WmNews submittedNews() {
        WmNews news = new WmNews();
        news.setId(10);
        news.setStatus(WmNews.Status.SUBMIT.getCode());
        news.setContent("[{\"type\":\"image\",\"value\":\"inline-image\"}]");
        return news;
    }
}
