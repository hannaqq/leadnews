package com.news.behavior.service;

import com.news.model.behavior.dtos.UnLikesBehaviorDto;
import com.news.model.common.dtos.ResponseResult;

public interface ApUnlikesBehaviorService {
    ResponseResult unlike(UnLikesBehaviorDto dto);
}
