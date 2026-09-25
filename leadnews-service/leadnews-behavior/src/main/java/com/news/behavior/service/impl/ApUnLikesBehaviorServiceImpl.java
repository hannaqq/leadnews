package com.news.behavior.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.news.behavior.mapper.ApUnLikesBehaviorMapper;

import com.news.behavior.service.ApUnlikesBehaviorService;
import com.news.model.behavior.dtos.UnLikesBehaviorDto;
import com.news.model.behavior.pojos.ApUnlikesBehavior;
import com.news.model.common.dtos.ResponseResult;
import org.springframework.stereotype.Service;

@Service
public class ApUnLikesBehaviorServiceImpl extends ServiceImpl<ApUnLikesBehaviorMapper, ApUnlikesBehavior> implements ApUnlikesBehaviorService {
    @Override
    public ResponseResult unlike(UnLikesBehaviorDto dto) {
        return null;
    }
}
