package com.news.wemedia.service.impl;

import com.news.file.service.FileStorageService;
import com.news.model.common.dtos.PageResponseResult;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.wemedia.dtos.WmMaterialDto;
import com.news.model.wemedia.pojos.WmMaterial;
import com.news.utils.thread.WmThreadLocalUtil;
import com.news.wemedia.repository.WmMaterialRepository;
import com.news.wemedia.repository.WmNewsMaterialRepository;
import com.news.wemedia.service.WmMaterialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class WmMaterialServiceImpl implements WmMaterialService {
    private final FileStorageService fileStorageService;
    private final WmMaterialRepository materialRepository;
    private final WmNewsMaterialRepository relationRepository;

    @Override
    public ResponseResult uploadPicture(MultipartFile file) {
        if (file == null || file.getSize() == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        String objectName = UUID.randomUUID().toString().replace("-", "")
                + originalName.substring(originalName.lastIndexOf('.'));
        final String fileId;
        try {
            fileId = fileStorageService.uploadImgFile("", objectName, file.getInputStream());
        } catch (IOException exception) {
            log.error("Failed to upload material", exception);
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "upload failed");
        }

        WmMaterial material = new WmMaterial();
        material.setUserId(WmThreadLocalUtil.getUser().getId());
        material.setUrl(fileId);
        material.setIsCollection((short) 0);
        material.setType((short) 0);
        material.setCreatedTime(new Date());
        return ResponseResult.okResult(materialRepository.save(material));
    }

    @Override
    public ResponseResult list(WmMaterialDto dto) {
        dto.checkParam();
        PageRequest request = PageRequest.of(dto.getPage() - 1, dto.getSize(),
                Sort.by(Sort.Direction.DESC, "createdTime"));
        Integer userId = WmThreadLocalUtil.getUser().getId();
        Page<WmMaterial> page = dto.getIsCollection() != null && dto.getIsCollection() == 1
                ? materialRepository.findByUserIdAndIsCollection(userId, dto.getIsCollection(), request)
                : materialRepository.findByUserId(userId, request);
        ResponseResult result = new PageResponseResult(dto.getPage(), dto.getSize(),
                Math.toIntExact(page.getTotalElements()));
        result.setData(page.getContent());
        return result;
    }

    @Override
    public ResponseResult delPicture(Integer id) {
        WmMaterial material = materialRepository.findByIdAndUserId(
                id, WmThreadLocalUtil.getUser().getId()).orElse(null);
        if (material == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        if (relationRepository.existsByMaterialId(id)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.MATERIAL_REFERENCED);
        }
        materialRepository.delete(material);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode());
    }
}
