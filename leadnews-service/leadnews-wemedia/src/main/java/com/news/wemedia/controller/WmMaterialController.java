package com.news.wemedia.controller;

import com.news.model.common.dtos.ResponseResult;
import lombok.RequiredArgsConstructor;
import com.news.model.wemedia.dtos.WmMaterialDto;
import com.news.wemedia.service.WmMaterialService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/material")
@RequiredArgsConstructor
public class WmMaterialController {

    private final WmMaterialService wmMaterialService;

    @PostMapping("/upload_picture")
    public ResponseResult uploadPicture(MultipartFile multipartFile){
        return wmMaterialService.uploadPicture(multipartFile);

    }

    @PostMapping("/list")
    public ResponseResult list(@RequestBody WmMaterialDto wmMaterialDto){
        return wmMaterialService.list(wmMaterialDto);
    }

    @GetMapping("/del_picture/{id}")
    public ResponseResult delPicture(@PathVariable Integer id){
        return wmMaterialService.delPicture(id);
    }
}
