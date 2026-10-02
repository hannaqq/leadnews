package com.news.wemedia.service.impl;

import com.news.wemedia.service.AwsModerationService;
import com.news.wemedia.service.ModerationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectModerationLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.Image;

@Service
@Slf4j
@RequiredArgsConstructor
public class AwsModerationServiceImpl implements AwsModerationService {

    private static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;

    private final RekognitionClient rekognitionClient;

    @Value("${aws.rekognition.min-confidence:75}")
    private float minConfidence;

    @Override
    public ModerationResult scanImageWithAwsRekognition(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0 || imageBytes.length > MAX_IMAGE_BYTES) {
            return ModerationResult.MANUAL_REVIEW;
        }

        try {
            Image image = Image.builder()
                    .bytes(SdkBytes.fromByteArray(imageBytes))
                    .build();
            DetectModerationLabelsRequest request = DetectModerationLabelsRequest.builder()
                    .image(image)
                    .minConfidence(minConfidence)
                    .build();

            boolean rejected = !rekognitionClient.detectModerationLabels(request)
                    .moderationLabels()
                    .isEmpty();
            return rejected ? ModerationResult.REJECTED : ModerationResult.APPROVED;
        } catch (RuntimeException exception) {
            log.error("AWS Rekognition moderation failed", exception);
            return ModerationResult.MANUAL_REVIEW;
        }
    }
}
