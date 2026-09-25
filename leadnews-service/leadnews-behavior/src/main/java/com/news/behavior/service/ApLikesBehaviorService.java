package com.news.behavior.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.news.model.behavior.dtos.LikesBehaviorDto;
import com.news.model.behavior.pojos.ApLikesBehavior;
import com.news.model.common.dtos.ResponseResult;

public interface ApLikesBehaviorService extends IService<ApLikesBehavior> {
    public ResponseResult like(LikesBehaviorDto dto);
}
