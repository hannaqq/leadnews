package com.news.wemedia.service;

import com.news.model.common.dtos.ResponseResult;
import com.news.model.wemedia.dtos.WmLoginDto;
import com.news.model.wemedia.pojos.WmUser;

public interface WmUserService {
    public ResponseResult login(WmLoginDto dto);

    WmUser findByApUserId(Integer apUserId);

    WmUser save(WmUser user);

}
