package com.news.wemedia.service;

import com.news.wemedia.service.impl.AwsModerationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectModerationLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectModerationLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.ModerationLabel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AwsModerationServiceImplTest {

    private final RekognitionClient client = mock(RekognitionClient.class);
    private final AwsModerationServiceImpl service = new AwsModerationServiceImpl(client);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "minConfidence", 75F);
    }

    @Test
    void approvesImageWithoutModerationLabels() {
        when(client.detectModerationLabels(any(DetectModerationLabelsRequest.class)))
                .thenReturn(DetectModerationLabelsResponse.builder().moderationLabels(List.of()).build());

        assertEquals(ModerationResult.APPROVED,
                service.scanImageWithAwsRekognition(new byte[]{1}));
    }

    @Test
    void rejectsImageWithModerationLabels() {
        ModerationLabel label = ModerationLabel.builder()
                .name("Explicit Nudity")
                .confidence(99F)
                .build();
        when(client.detectModerationLabels(any(DetectModerationLabelsRequest.class)))
                .thenReturn(DetectModerationLabelsResponse.builder()
                        .moderationLabels(label)
                        .build());

        assertEquals(ModerationResult.REJECTED,
                service.scanImageWithAwsRekognition(new byte[]{1}));
    }

    @Test
    void sendsImageToManualReviewWhenAwsCallFails() {
        when(client.detectModerationLabels(any(DetectModerationLabelsRequest.class)))
                .thenThrow(new RuntimeException("AWS unavailable"));

        assertEquals(ModerationResult.MANUAL_REVIEW,
                service.scanImageWithAwsRekognition(new byte[]{1}));
    }

    @Test
    void sendsEmptyImageToManualReview() {
        assertEquals(ModerationResult.MANUAL_REVIEW,
                service.scanImageWithAwsRekognition(new byte[0]));
    }
}
