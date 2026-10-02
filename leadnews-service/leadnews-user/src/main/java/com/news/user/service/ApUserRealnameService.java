package com.news.user.service;

import com.news.model.user.dtos.AuthDto;
import com.news.model.common.dtos.ResponseResult;

public interface ApUserRealnameService {
    ResponseResult getList(AuthDto dto);
    ResponseResult pass(AuthDto dto);

    ResponseResult fail(AuthDto dto);

}
