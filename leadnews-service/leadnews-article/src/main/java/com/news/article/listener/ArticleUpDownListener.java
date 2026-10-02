package com.news.article.listener;

import com.news.article.service.ApArticleConfigService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
    @SneakyThrows
    public void onMessage(String message){
        log.info("receive message:{}",message);
        if(StringUtils.isNotBlank(message)){
            Map map = objectMapper.readValue(message, Map.class);
            apArticleConfigService.updateByMap(map);

        }
    }
}
