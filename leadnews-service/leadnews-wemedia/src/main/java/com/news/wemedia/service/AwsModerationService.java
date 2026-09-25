package com.news.wemedia.service;

public interface AwsModerationService {
    /**
     * 调用 AWS Rekognition 服务检测图片是否包含违规内容或敏感文本
     * @param imageBytes 图片的字节数组
     * @return 如果安全返回 true，如果包含敏感内容返回 false
     */
    boolean scanImageWithAwsRekognition(byte[] imageBytes);
}
