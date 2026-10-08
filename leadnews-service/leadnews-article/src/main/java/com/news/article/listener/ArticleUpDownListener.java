package com.news.article.listener;

import com.news.article.service.ApArticleConfigService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
@Component
@Slf4j
@RequiredArgsConstructor
public class ArticleUpDownListener {

    private final ApArticleConfigService apArticleConfigService;

    private final ObjectMapper objectMapper;


    @KafkaListener(topics = "wm.news.topic.down.or.up")
    public void onMessage(String message) throws JsonProcessingException {
        log.info("receive message:{}",message);
        if(StringUtils.isNotBlank(message)){
            Map map = objectMapper.readValue(message, Map.class);
            apArticleConfigService.updateByMap(map);

        }
    }
}
