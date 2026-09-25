package com.news.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.news.apis.article.IArticleClient;
import com.news.model.article.pojos.ApArticle;
import com.news.model.behavior.dtos.FollowBehaviorDto;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.user.dtos.UserRelationDto;
import com.news.model.user.pojos.ApUser;
import com.news.model.user.pojos.ApUserFan;
import com.news.model.user.pojos.ApUserFollow;
import com.news.user.mapper.ApUserFanMapper;
import com.news.user.mapper.ApUserFollowMapper;
import com.news.user.service.ApUserFollowService;
import com.news.utils.thread.AppThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
@Service
public class ApUserFollowServiceImpl extends ServiceImpl<ApUserFollowMapper,ApUserFollow> implements ApUserFollowService {

    @Autowired
    private IArticleClient iArticleClient;

    @Autowired
    private ApUserFanMapper apUserFanMapper;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ResponseResult followOrUnfollow(UserRelationDto dto) {
        ApUser user = AppThreadLocalUtil.getUser();
        if(user == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        if(dto.getOperation() == 0){
            ApUserFollow one = getOne(Wrappers.<ApUserFollow>lambdaQuery()
                    .eq(ApUserFollow::getUserId, user.getId())
                    .eq(ApUserFollow::getFollowId, dto.getAuthorId()));
            if(one != null){
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
            save(apUserFollow);

            ApUserFan apUserFan = new ApUserFan();
            apUserFan.setUserId(dto.getAuthorId());
            apUserFan.setFansId(user.getId());
            apUserFan.setFansName(user.getName());
            apUserFan.setLevel((short)1);
            apUserFan.setIsDisplay((short)0);
            apUserFan.setIsShieldLetter((short)0);
            apUserFan.setIsShieldComment((short)0);
            apUserFan.setCreatedTime(new Date());
            apUserFanMapper.insert(apUserFan);

            FollowBehaviorDto followBehaviorDto = new FollowBehaviorDto();
            followBehaviorDto.setFollowId(dto.getAuthorId());
            followBehaviorDto.setArticleId(dto.getArticleId());
            followBehaviorDto.setUserId(user.getId());
            kafkaTemplate.send("follow.behavior.topic", objectMapper.writeValueAsString(dto));

        } else {
            remove(Wrappers.<ApUserFollow>lambdaQuery().eq(ApUserFollow::getUserId, user.getId())
                    .eq(ApUserFollow::getFollowId,dto.getAuthorId()));

            apUserFanMapper.delete(Wrappers.<ApUserFan>lambdaQuery().eq(ApUserFan::getFansId,user.getId())
                    .eq(ApUserFan::getUserId,dto.getAuthorId()));
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode());
    }
}
