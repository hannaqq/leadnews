package com.news.wemedia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;

@Configuration
public class RekognitionConfig {

    @Bean
    public RekognitionClient rekognitionClient(
            @Value("${aws.rekognition.region:us-west-2}") String region) {
        return RekognitionClient.builder()
                .region(Region.of(region))
                .build();
    }
}
