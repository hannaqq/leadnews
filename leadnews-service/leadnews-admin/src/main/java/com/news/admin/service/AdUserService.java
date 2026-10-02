package com.news.admin.service;

import com.news.model.admin.dtos.AdUserDto;
import com.news.model.common.dtos.ResponseResult;

public interface AdUserService {
    ResponseResult login(AdUserDto dto);

}
