package com.news.wemedia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import com.news.apis.article.IArticleClient;

import com.news.file.service.FileStorageService;
import com.news.model.article.dtos.ArticleDto;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.wemedia.pojos.WmChannel;
import com.news.model.wemedia.pojos.WmNews;
import com.news.model.wemedia.pojos.WmSensitive;
import com.news.model.wemedia.pojos.WmUser;
import com.news.utils.common.SensitiveWordUtil;
import com.news.wemedia.repository.WmChannelRepository;
import com.news.wemedia.repository.WmNewsRepository;
import com.news.wemedia.repository.WmSensitiveRepository;
import com.news.wemedia.repository.WmUserRepository;
import com.news.wemedia.service.WmNewsAutoScanService;
import com.news.wemedia.service.AwsModerationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
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
    private final WemediaPersistenceService persistenceService;

    @Override
    @Async
    public void autoScanWmNews(Integer id){
        if (!persistenceService.claimNewsForProcessing(id)) {
            return;
        }
        WmNews wmNews = newsRepository.findById(id).orElse(null);
        if(wmNews == null){
            throw new RuntimeException("article doesn't exist");
        }
        try {
            Map<String, Object> textAndImages = handleTextAndImages(wmNews);
            if(!handleSensitiveScan((String) textAndImages.get("content"),wmNews)) return;
            if(!handleTextScan((String) textAndImages.get("content"))) return;
            if(!handleImageScan((List<String>) textAndImages.get("images"),wmNews)) return;
            ResponseResult responseResult = saveAppArticle(wmNews);
            if(!responseResult.getCode().equals(200)){
                throw new RuntimeException("save failed");
            }
            if (!persistenceService.completePublishing(wmNews.getId(), (Long) responseResult.getData())) {
                throw new IllegalStateException("News state changed while publishing");
            }
        } catch (Exception exception) {
            log.error("Automatic review failed for news {}", id, exception);
            persistenceService.transitionProcessingStatus(id, WmNews.Status.ADMIN_AUTH.getCode(),
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
            transitionFromProcessing(wmNews, WmNews.Status.FAIL.getCode(),
                    "the content contains restrict words");
            return false;
        }

        return true;
    }

    private final IArticleClient iArticleClient;

    private final WmChannelRepository channelRepository;

    private final WmUserRepository userRepository;

    private final FileStorageService fileStorageService;

    private final AwsModerationService awsModerationService;

    private final ObjectMapper objectMapper;

    private ResponseResult saveAppArticle(WmNews wmNews) {
        WmUser wmUser = userRepository.findById(wmNews.getUserId()).orElse(null);
        WmChannel wmChannel = channelRepository.findById(wmNews.getChannelId()).orElse(null);

        ArticleDto dto = new ArticleDto();
        BeanUtils.copyProperties(wmNews,dto);
        dto.setLayout(wmNews.getType());
        dto.setAuthorId(wmNews.getUserId().longValue());
        if(wmUser != null){
            dto.setAuthorName(wmUser.getName());
        }
        if(wmChannel != null){
            dto.setChannelName(wmChannel.getName());
        }

        if(wmNews.getArticleId() != null){
            dto.setId(wmNews.getArticleId());
        }

        dto.setCreatedTime(new Date());

        ResponseResult responseResult = iArticleClient.saveArticle(dto);
        return responseResult;

    }

    private boolean handleTextScan(String content){
        Boolean flag = true;
        return flag;
    }

    private boolean handleImageScan(List<String> images, WmNews wmNews) {
        boolean flag = true;
        if(images == null || images.size() == 0){
            return flag;
        }
        images = images.stream().distinct().collect(Collectors.toList());


        try {
            for (String image : images) {
                byte[] bytes = fileStorageService.downLoadFile(image);

                // 使用 AWS Rekognition 进行图片安全审核和 OCR 识别
                boolean isSafe = awsModerationService.scanImageWithAwsRekognition(bytes);
                if(!isSafe){
                    transitionFromProcessing(wmNews, WmNews.Status.FAIL.getCode(),
                            "AWS Rekognition: image contains explicit/sensitive content");
                    flag = false;
                    break;
                }
            }
        }catch (Exception e) {
            log.error("Image moderation failed for news {}", wmNews.getId(), e);
            transitionFromProcessing(wmNews, WmNews.Status.ADMIN_AUTH.getCode(),
                    "image moderation unavailable; manual review required");
            return false;
        }

        return flag;
    }

    @SneakyThrows
    private Map<String, Object> handleTextAndImages(WmNews wmNews){
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

    private void transitionFromProcessing(WmNews wmNews, short status, String reason){
        if (persistenceService.transitionProcessingStatus(wmNews.getId(), status, reason)) {
            wmNews.setStatus(status);
            wmNews.setReason(reason);
        }
    }

}
