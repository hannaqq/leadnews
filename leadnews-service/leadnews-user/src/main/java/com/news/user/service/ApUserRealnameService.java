package com.news.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.news.model.user.dtos.AuthDto;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.user.pojos.ApUserRealname;

public interface ApUserRealnameService extends IService<ApUserRealname> {
    public ResponseResult getList(AuthDto dto);
    public ResponseResult pass(AuthDto dto);

    public ResponseResult fail(AuthDto dto);

}
