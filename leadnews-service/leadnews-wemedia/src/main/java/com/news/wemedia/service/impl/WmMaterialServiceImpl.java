package com.news.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.news.file.service.FileStorageService;
import com.news.model.common.dtos.PageResponseResult;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.wemedia.dtos.WmMaterialDto;
import com.news.model.wemedia.pojos.WmMaterial;
import com.news.model.wemedia.pojos.WmNewsMaterial;
import com.news.utils.thread.WmThreadLocalUtil;
import com.news.wemedia.mapper.WmMaterialMapper;
import com.news.wemedia.mapper.WmNewsMaterialMapper;
import com.news.wemedia.service.WmMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {
    @Autowired
    private FileStorageService fileStorageService;

    @Override
    public ResponseResult uploadPicture(MultipartFile multipartFile) {
        if(multipartFile == null || multipartFile.getSize() == 0){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        String fileName = UUID.randomUUID().toString().replace("-", "");

        String originalFilename = multipartFile.getOriginalFilename();
        String postfix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileId = null;
        try {
            fileId = fileStorageService.uploadImgFile("", fileName + postfix, multipartFile.getInputStream());
            log.info("upload picture to minio,fileId:{}",fileId);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("upload failed");
        }

        WmMaterial wmMaterial = new WmMaterial();
        wmMaterial.setUserId(WmThreadLocalUtil.getUser().getId());
        wmMaterial.setUrl(fileId);
        wmMaterial.setIsCollection((short)0);
        wmMaterial.setType((short)0);
        wmMaterial.setCreatedTime(new Date());
        save(wmMaterial);

        return ResponseResult.okResult(wmMaterial);
    }

    private final static short MAX_PAGE_SIZE = 50;

    @Override
    public ResponseResult list(WmMaterialDto wmMaterialDto) {

        wmMaterialDto.checkParam();
        IPage page = new Page(wmMaterialDto.getPage(),wmMaterialDto.getSize());
        LambdaQueryWrapper<WmMaterial> lqw = new LambdaQueryWrapper<>();
        if(wmMaterialDto.getIsCollection() != null && wmMaterialDto.getIsCollection() == 1){
            lqw.eq(WmMaterial::getIsCollection,wmMaterialDto.getIsCollection());
        }
        lqw.eq(WmMaterial::getUserId,WmThreadLocalUtil.getUser().getId())
                .orderByDesc(WmMaterial::getCreatedTime);

        page = page(page, lqw);
        ResponseResult responseResult = new PageResponseResult(wmMaterialDto.getPage(), wmMaterialDto.getSize(), (int) page.getTotal());

        responseResult.setData(page.getRecords());
        return responseResult;
    }

    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;
    @Override
    public ResponseResult delPicture(Integer id) {
        WmMaterial wmMaterial = getById(id);
        if(wmMaterial == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        List<WmNewsMaterial> wmNewsMaterials = wmNewsMaterialMapper.selectList(Wrappers.<WmNewsMaterial>lambdaQuery().eq(WmNewsMaterial::getMaterialId, id));
        if(wmNewsMaterials.isEmpty()){
            return ResponseResult.errorResult(AppHttpCodeEnum.MATERIAL_REFERENCED);
        }

        removeById(wmMaterial);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode());
    }
}
