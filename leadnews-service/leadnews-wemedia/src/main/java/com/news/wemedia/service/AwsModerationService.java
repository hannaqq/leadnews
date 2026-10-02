package com.news.wemedia.service;

public interface AwsModerationService {
    ModerationResult scanImageWithAwsRekognition(byte[] imageBytes);
}
