package com.news.article.listener;

import com.news.article.service.ApArticleConfigService;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
@Component
@Slf4j
public class ArticleUpDownListener {

    @Autowired
    private ApArticleConfigService apArticleConfigService;

    @Autowired
    private ObjectMapper objectMapper;


    @KafkaListener(topics = "wm.news.topic.down.or.up")
    public void onMessage(String message){
        log.info("receive message:{}",message);
        if(StringUtils.isNotBlank(message)){
            Map map = objectMapper.readValue(message, Map.class);
            apArticleConfigService.updateByMap(map);

        }
    }
}
