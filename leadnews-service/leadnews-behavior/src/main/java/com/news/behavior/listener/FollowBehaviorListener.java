package com.news.behavior.listener;

import com.news.behavior.service.ApFollowBehaviorService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import com.news.model.behavior.dtos.FollowBehaviorDto;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
@Component
@Slf4j
@RequiredArgsConstructor
public class FollowBehaviorListener {

    private final ApFollowBehaviorService apFollowBehaviorService;

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "follow.behavior.topic")
    @SneakyThrows
    public void onMessage(String message){
        log.info("receive message:{}",message);
        if(StringUtils.isNotBlank(message)){
            FollowBehaviorDto dto = objectMapper.readValue(message, FollowBehaviorDto.class);
            apFollowBehaviorService.saveFollowBehavior(dto);

        }
    }

}
