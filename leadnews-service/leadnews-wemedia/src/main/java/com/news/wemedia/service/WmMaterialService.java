package com.news.wemedia.service;

import com.news.model.common.dtos.ResponseResult;
import com.news.model.wemedia.dtos.WmMaterialDto;
import org.springframework.web.multipart.MultipartFile;

public interface WmMaterialService {
    public ResponseResult uploadPicture(MultipartFile multipartFile);

    public ResponseResult list(WmMaterialDto wmMaterialDto);

    public ResponseResult delPicture(Integer id);
}
