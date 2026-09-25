package com.news.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.wemedia.dtos.SensitiveDto;
import com.news.model.wemedia.pojos.WmSensitive;

public interface WmSensitiveService extends IService<WmSensitive> {

    public ResponseResult getList(SensitiveDto dto);

    public ResponseResult delete(Integer id);

    public ResponseResult saveOrUpdateSensitive(WmSensitive wmSensitive);

}
