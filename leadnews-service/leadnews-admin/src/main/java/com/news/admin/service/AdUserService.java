package com.news.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.news.model.admin.dtos.AdUserDto;
import com.news.model.admin.pojos.AdUser;
import com.news.model.common.dtos.ResponseResult;

public interface AdUserService extends IService<AdUser> {
    public ResponseResult login(AdUserDto dto);

}
