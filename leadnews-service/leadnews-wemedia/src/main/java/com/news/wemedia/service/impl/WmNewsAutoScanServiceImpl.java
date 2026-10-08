package com.news.wemedia.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;

import com.news.file.service.FileStorageService;
import com.news.model.wemedia.pojos.WmNews;
import com.news.model.wemedia.pojos.WmSensitive;
import com.news.utils.common.SensitiveWordUtil;
import com.news.wemedia.repository.WmNewsRepository;
import com.news.wemedia.repository.WmSensitiveRepository;
import com.news.wemedia.service.WmNewsAutoScanService;
import com.news.wemedia.service.WmNewsPublishService;
import com.news.wemedia.service.AwsModerationService;
import com.news.wemedia.service.ModerationResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
@Service
@Slf4j
@RequiredArgsConstructor
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {

    private final WmNewsRepository newsRepository;

    @Override
    public void autoScanWmNews(Integer id){
        WmNews wmNews = newsRepository.findById(id).orElse(null);
        if (wmNews == null || wmNews.getStatus() != WmNews.Status.SUBMIT.getCode()) {
            return;
        }
        wmNews.setStatus(WmNews.Status.PROCESSING.getCode());
        wmNews.setReason("processing");
        newsRepository.save(wmNews);
        try {
            Map<String, Object> textAndImages = extractTextAndImages(wmNews);
            if(!handleSensitiveScan((String) textAndImages.get("content"),wmNews)) return;
            if(!handleImageScan((List<String>) textAndImages.get("images"),wmNews)) return;
            publishService.reviewApproved(wmNews);
        } catch (Exception exception) {
            log.error("Automatic review failed for news {}", id, exception);
            updateStatus(wmNews, WmNews.Status.ADMIN_AUTH.getCode(),
                    "automatic review unavailable; manual review required");
        }
    }

    private final WmSensitiveRepository sensitiveRepository;
    private boolean handleSensitiveScan(String content,WmNews wmNews) {
        List<WmSensitive> wmSensitives = sensitiveRepository.findAll();
        List<String> sensitiveList = wmSensitives.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());
        SensitiveWordUtil.initMap(sensitiveList);

        Map<String, Integer> map = SensitiveWordUtil.matchWords(content);
        if(map.size()>0){
            updateStatus(wmNews, WmNews.Status.FAIL.getCode(),
                    "the content contains restrict words");
            return false;
        }

        return true;
    }

    private final FileStorageService fileStorageService;

    private final AwsModerationService awsModerationService;

    private final ObjectMapper objectMapper;
    private final WmNewsPublishService publishService;

    private boolean handleImageScan(List<String> images, WmNews wmNews) {
        if (images == null || images.isEmpty()) {
            return true;
        }
        for (String image : images.stream().distinct().collect(Collectors.toList())) {
            ModerationResult result = awsModerationService.scanImageWithAwsRekognition(
                    fileStorageService.downLoadFile(image));
            if (result == ModerationResult.REJECTED) {
                updateStatus(wmNews, WmNews.Status.FAIL.getCode(),
                        "image contains explicit/sensitive content");
                return false;
            }
            if (result == ModerationResult.MANUAL_REVIEW) {
                throw new IllegalStateException("image moderation unavailable");
            }
        }
        return true;
    }

    private Map<String, Object> extractTextAndImages(WmNews wmNews) throws JsonProcessingException {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> images = new ArrayList<>();
        String content = wmNews.getContent();
        if(StringUtils.isNotBlank(content)){
            List<Map<String, Object>> maps = objectMapper.readValue(content, new TypeReference<List<Map<String, Object>>>() {});
            for (Map map : maps) {
                if(map.get("type").equals("text")){
                    stringBuilder.append(map.get("value"));
                }else if(map.get("type").equals("image")){
                    images.add((String) map.get("value"));
                }
            }
        }
        if(StringUtils.isNotBlank(wmNews.getImages())){
            String[] split = wmNews.getImages().split(",");
            images.addAll(Arrays.asList(split));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("content", stringBuilder.toString());
        result.put("images",images);
        return result;

    }

    private void updateStatus(WmNews wmNews, short status, String reason){
        wmNews.setStatus(status);
        wmNews.setReason(reason);
        newsRepository.save(wmNews);
    }

}
