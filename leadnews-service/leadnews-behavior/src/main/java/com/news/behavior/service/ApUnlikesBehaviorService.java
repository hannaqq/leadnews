package com.news.behavior.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.news.model.behavior.dtos.UnLikesBehaviorDto;
import com.news.model.behavior.pojos.ApUnlikesBehavior;
import com.news.model.common.dtos.ResponseResult;

public interface ApUnlikesBehaviorService extends IService<ApUnlikesBehavior> {
    public ResponseResult unlike(UnLikesBehaviorDto dto);
}
