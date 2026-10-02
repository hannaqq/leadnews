package com.news.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import com.news.apis.article.IArticleClient;
import com.news.model.article.pojos.ApArticle;
import com.news.model.behavior.dtos.FollowBehaviorDto;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.user.dtos.UserRelationDto;
import com.news.model.user.pojos.ApUser;
import com.news.model.user.pojos.ApUserFan;
import com.news.model.user.pojos.ApUserFollow;
import com.news.user.repository.ApUserFollowRepository;
import com.news.user.service.ApUserFollowService;
import com.news.utils.thread.AppThreadLocalUtil;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
@Service
@RequiredArgsConstructor
public class ApUserFollowServiceImpl implements ApUserFollowService {

    private final IArticleClient iArticleClient;

    private final ApUserFollowRepository followRepository;

    private final UserFollowPersistenceService persistenceService;

    private final KafkaTemplate kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public ResponseResult followOrUnfollow(UserRelationDto dto) {
        ApUser user = AppThreadLocalUtil.getUser();
        if(user == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        if(dto.getOperation() == 0){
            if(followRepository.existsByUserIdAndFollowId(user.getId(), dto.getAuthorId())){
                return ResponseResult.errorResult(AppHttpCodeEnum.HAVE_FOLLOWED);
            }
            ApUserFollow apUserFollow = new ApUserFollow();
            apUserFollow.setUserId(user.getId());
            apUserFollow.setFollowId(dto.getAuthorId());
            ApArticle article = iArticleClient.getOne(dto.getArticleId());
            String authorName = article.getAuthorName();
            apUserFollow.setFollowName(authorName);
            apUserFollow.setIsNotice((short) 1);
            apUserFollow.setLevel((short)1);
            apUserFollow.setCreatedTime(new Date());
            ApUserFan apUserFan = new ApUserFan();
            apUserFan.setUserId(dto.getAuthorId());
            apUserFan.setFansId(user.getId());
            apUserFan.setFansName(user.getName());
            apUserFan.setLevel((short)1);
            apUserFan.setIsDisplay((short)0);
            apUserFan.setIsShieldLetter((short)0);
            apUserFan.setIsShieldComment((short)0);
            apUserFan.setCreatedTime(new Date());
            if (!persistenceService.createIfAbsent(apUserFollow, apUserFan)) {
                return ResponseResult.errorResult(AppHttpCodeEnum.HAVE_FOLLOWED);
            }

            FollowBehaviorDto followBehaviorDto = new FollowBehaviorDto();
            followBehaviorDto.setFollowId(dto.getAuthorId());
            followBehaviorDto.setArticleId(dto.getArticleId());
            followBehaviorDto.setUserId(user.getId());
            kafkaTemplate.send("follow.behavior.topic", objectMapper.writeValueAsString(followBehaviorDto));

        } else {
            persistenceService.delete(user.getId(), dto.getAuthorId());
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode());
    }
}
