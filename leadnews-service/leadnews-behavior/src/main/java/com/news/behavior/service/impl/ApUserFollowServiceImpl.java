package com.news.behavior.service.impl;

import com.news.apis.wemedia.IWemediaClient;
import com.news.behavior.repository.ApUserFollowRepository;
import com.news.behavior.service.ApUserFollowService;
import com.news.model.behavior.pojos.ApUserFollow;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.wemedia.vos.CreatorAccountVo;
import com.news.utils.thread.AppThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class ApUserFollowServiceImpl implements ApUserFollowService {

    private final ApUserFollowRepository followRepository;
    private final IWemediaClient wemediaClient;

    @Override
    @Transactional
    public ResponseResult follow(Integer creatorId) {
        Integer userId = AppThreadLocalUtil.getUserId();
        if (userId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        if (creatorId == null || creatorId <= 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "creatorId is invalid");
        }
        CreatorAccountVo targetCreator = wemediaClient.getById(creatorId);
        if (targetCreator == null) {
            return ResponseResult.errorResult(
                    AppHttpCodeEnum.DATA_NOT_EXIST, "creator does not exist or is unavailable");
        }
        if (userId.equals(targetCreator.getApUserId())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "cannot follow yourself");
        }
        if (followRepository.existsByUserIdAndCreatorId(userId, creatorId)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.HAVE_FOLLOWED);
        }

        ApUserFollow relation = new ApUserFollow();
        relation.setUserId(userId);
        relation.setCreatorId(creatorId);
        relation.setCreatedTime(new Date());
        try {
            followRepository.saveAndFlush(relation);
        } catch (DataIntegrityViolationException exception) {
            return ResponseResult.errorResult(AppHttpCodeEnum.HAVE_FOLLOWED);
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode());
    }

    @Override
    @Transactional
    public ResponseResult unfollow(Integer creatorId) {
        Integer userId = AppThreadLocalUtil.getUserId();
        if (userId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        if (creatorId == null || creatorId <= 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "creatorId is invalid");
        }
        followRepository.deleteByUserIdAndCreatorId(userId, creatorId);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode());
    }
}
