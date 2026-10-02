package com.news.wemedia.service;

import com.news.model.wemedia.dtos.ChannelDto;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.wemedia.pojos.WmChannel;

public interface WmChannelService {

    public ResponseResult findAll();

    public ResponseResult getList(ChannelDto dto);

    public ResponseResult saveOrUpdateChannel(WmChannel wmChannel);

    public ResponseResult delete(Integer id);
}
