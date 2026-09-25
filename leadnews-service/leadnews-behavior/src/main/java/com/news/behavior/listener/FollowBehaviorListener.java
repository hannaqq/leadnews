package com.news.behavior.listener;

import com.news.behavior.service.ApFollowBehaviorService;
import com.news.model.behavior.dtos.FollowBehaviorDto;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
@Component
@Slf4j
public class FollowBehaviorListener {

    @Autowired
    private ApFollowBehaviorService apFollowBehaviorService;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "follow.behavior.topic")
    public void onMessage(String message){
        log.info("receive message:{}",message);
        if(StringUtils.isNotBlank(message)){
            FollowBehaviorDto dto = objectMapper.readValue(message, FollowBehaviorDto.class);
            apFollowBehaviorService.saveFollowBehavior(dto);

        }
    }

}
