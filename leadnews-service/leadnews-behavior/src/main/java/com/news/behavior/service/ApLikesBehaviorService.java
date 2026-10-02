package com.news.behavior.service;

import com.news.model.behavior.dtos.LikesBehaviorDto;
import com.news.model.common.dtos.ResponseResult;

public interface ApLikesBehaviorService {
    ResponseResult like(LikesBehaviorDto dto);
}
