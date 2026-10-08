package com.news.behavior.service;

import com.news.model.common.dtos.ResponseResult;

public interface ApUserFollowService {

    ResponseResult follow(Integer creatorId);

    ResponseResult unfollow(Integer creatorId);
}
