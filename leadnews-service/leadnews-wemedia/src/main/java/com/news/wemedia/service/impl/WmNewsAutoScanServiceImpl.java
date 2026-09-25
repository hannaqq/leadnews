package com.news.wemedia.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.news.apis.article.IArticleClient;

import com.news.file.service.FileStorageService;
import com.news.model.article.dtos.ArticleDto;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.wemedia.pojos.WmChannel;
import com.news.model.wemedia.pojos.WmNews;
import com.news.model.wemedia.pojos.WmSensitive;
import com.news.model.wemedia.pojos.WmUser;
import com.news.utils.common.SensitiveWordUtil;
import com.news.wemedia.mapper.WmChannelMapper;
import com.news.wemedia.mapper.WmNewsMapper;
import com.news.wemedia.mapper.WmSensitiveMapper;
import com.news.wemedia.mapper.WmUserMapper;
import com.news.wemedia.service.WmNewsAutoScanService;
import com.news.wemedia.service.AwsModerationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.Resource;

import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
@Service
@Slf4j
@Transactional
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {

    @Autowired
    private WmNewsMapper wmNewsMapper;
    @Override
    @Async
    public void autoScanWmNews(Integer id){
        WmNews wmNews = wmNewsMapper.selectById(id);
        if(wmNews == null){
            throw new RuntimeException("article doesn't exist");
        }

        if(wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode())){
            Map<String, Object> textAndImages = handleTextAndImages(wmNews);
            if(!handleSensitiveScan((String) textAndImages.get("content"),wmNews)) return;
            if(!handleTextScan((String) textAndImages.get("content"))) return;
            if(!handleImageScan((List<String>) textAndImages.get("images"),wmNews)) return;
            ResponseResult responseResult = saveAppArticle(wmNews);
            if(!responseResult.getCode().equals(200)){
                throw new RuntimeException("save failed");
            }
            wmNews.setArticleId((Long) responseResult.getData());
            updateWmNews(wmNews,(short) 9,"processed");

        }

    }

    @Autowired
    private WmSensitiveMapper wmSensitiveMapper;
    private boolean handleSensitiveScan(String content,WmNews wmNews) {
        List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(Wrappers.<WmSensitive>lambdaQuery().select(WmSensitive::getSensitives));
        List<String> sensitiveList = wmSensitives.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());
        SensitiveWordUtil.initMap(sensitiveList);

        Map<String, Integer> map = SensitiveWordUtil.matchWords(content);
        if(map.size()>0){
            updateWmNews(wmNews, (short) 2,"the content contains restrict words");
            return false;
        }

        return true;
    }

    @Resource
    private IArticleClient iArticleClient;

    @Autowired
    private WmChannelMapper wmChannelMapper;

    @Autowired
    private WmUserMapper wmUserMapper;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private AwsModerationService awsModerationService;

    @Autowired
    private ObjectMapper objectMapper;

    private ResponseResult saveAppArticle(WmNews wmNews) {
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());

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
                    updateWmNews(wmNews, (short) 2, "AWS Rekognition: image contains explicit/sensitive content");
                    flag = false;
                    break;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return flag;
    }

    private Map<String, Object> handleTextAndImages(WmNews wmNews){
        StringBuilder stringBuilder = new StringBuilder();
        List<String> images = new ArrayList<>();
        String content = wmNews.getContent();
        if(StringUtils.isNotBlank(content)){
            List<Map> maps = objectMapper.readValue(content, new TypeReference<List<Map<String, Object>>>() {});
            for (Map map : maps) {
                if(map.get("type").equals("text")){
                    stringBuilder.append(map.get("value"));
                }else if(map.get("type").equals("images")){
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

    private void updateWmNews(WmNews wmNews, short status, String reason){
        wmNews.setStatus(status);
        wmNews.setReason(reason);
        wmNewsMapper.updateById(wmNews);
    }

}
