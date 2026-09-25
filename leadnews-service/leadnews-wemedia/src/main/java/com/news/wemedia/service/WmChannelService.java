package com.news.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.news.model.wemedia.dtos.ChannelDto;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.wemedia.pojos.WmChannel;

public interface WmChannelService extends IService<WmChannel> {

    public ResponseResult findAll();

    public ResponseResult getList(ChannelDto dto);

    public ResponseResult saveOrUpdateChannel(WmChannel wmChannel);

    public ResponseResult delete(Integer id);
}
