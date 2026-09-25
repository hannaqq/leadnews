package com.news.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.wemedia.dtos.WmLoginDto;
import com.news.model.wemedia.pojos.WmUser;

public interface WmUserService extends IService<WmUser> {
    public ResponseResult login(WmLoginDto dto);

}
