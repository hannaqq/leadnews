package com.news.user.service;

import com.news.model.common.dtos.ResponseResult;
import com.news.model.user.dtos.LoginDto;

public interface ApUserService {
    ResponseResult login(LoginDto dto);
}
