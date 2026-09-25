package com.news.wemedia.service.impl;

import com.news.wemedia.service.AwsModerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
public class AwsModerationServiceImpl implements AwsModerationService {

    @Override
    public boolean scanImageWithAwsRekognition(byte[] imageBytes) {
        log.info("🚀 [Mock] Initiating AWS Rekognition API call...");
        
        try {
            // 模拟网络延迟 (50-200ms)，让体验更像是在调用真实的云端 API
            Thread.sleep(50 + new Random().nextInt(150));
            
            log.info("☁️ [Mock] AWS Rekognition: Analyzing image ({} bytes) for Explicit/Suggestive labels and OCR text...", imageBytes.length);
            
            // -------------------------------------------------------------------------
            // 📝 AWS SDK 真实生产环境代码示例（由于当前是本地 Mock 模式，故注释掉）：
            // -------------------------------------------------------------------------
            /*
            AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();
            
            Image awsImage = new Image().withBytes(ByteBuffer.wrap(imageBytes));
            
            // 1. 调用内容审核 API (DetectModerationLabels)
            DetectModerationLabelsRequest moderationRequest = new DetectModerationLabelsRequest()
                    .withImage(awsImage).withMinConfidence(75F);
            DetectModerationLabelsResult moderationResult = rekognitionClient.detectModerationLabels(moderationRequest);
            if (!moderationResult.getModerationLabels().isEmpty()) {
                log.warn("AWS detected inappropriate content: {}", moderationResult.getModerationLabels());
                return false;
            }
            
            // 2. 调用文本提取 API (DetectText)
            DetectTextRequest textRequest = new DetectTextRequest().withImage(awsImage);
            DetectTextResult textResult = rekognitionClient.detectText(textRequest);
            StringBuilder extractedText = new StringBuilder();
            for (TextDetection text : textResult.getTextDetections()) {
                if (text.getType().equals("LINE")) {
                    extractedText.append(text.getDetectedText()).append(" ");
                }
            }
            // 将提取的文字去匹配本地的敏感词字典
            if(SensitiveWordUtil.matchWords(extractedText.toString()).size() > 0) {
                return false;
            }
            */
            // -------------------------------------------------------------------------

            log.info("✅ [Mock] AWS Rekognition: Image passed all safety and OCR checks.");
            
        } catch (InterruptedException e) {
            log.error("AWS Rekognition mock call interrupted", e);
            Thread.currentThread().interrupt();
        }
        
        // 默认放行所有测试图片
        return true; 
    }
}
