package com.news.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.wemedia.dtos.WmMaterialDto;
import com.news.model.wemedia.pojos.WmMaterial;
import org.springframework.web.multipart.MultipartFile;

public interface WmMaterialService extends IService<WmMaterial> {
    public ResponseResult uploadPicture(MultipartFile multipartFile);

    public ResponseResult list(WmMaterialDto wmMaterialDto);

    public ResponseResult delPicture(Integer id);
}
