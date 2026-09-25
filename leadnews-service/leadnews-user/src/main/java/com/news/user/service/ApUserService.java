package com.news.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.user.dtos.LoginDto;
import com.news.model.user.pojos.ApUser;

public interface ApUserService extends IService<ApUser> {
    public ResponseResult login(LoginDto dto);
}
