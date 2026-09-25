package com.news.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.user.dtos.UserRelationDto;
import com.news.model.user.pojos.ApUserFollow;

public interface ApUserFollowService extends IService<ApUserFollow> {
    public ResponseResult followOrUnfollow(UserRelationDto dto);
}
