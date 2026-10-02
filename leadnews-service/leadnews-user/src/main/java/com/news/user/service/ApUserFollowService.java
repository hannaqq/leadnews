package com.news.user.service;

import com.news.model.common.dtos.ResponseResult;
import com.news.model.user.dtos.UserRelationDto;

public interface ApUserFollowService {
    ResponseResult followOrUnfollow(UserRelationDto dto);
}
