package com.news.search.controller;

import com.news.model.common.dtos.ResponseResult;
import lombok.RequiredArgsConstructor;
import com.news.model.search.dtos.UserSearchDto;
import com.news.search.service.ApAssociateWordsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/associate")
@RequiredArgsConstructor
public class ApAssociateWordsController {

    final ApAssociateWordsService apAssociateWordsService;

    @PostMapping("/search")
    public ResponseResult search(@RequestBody UserSearchDto dto){
        return apAssociateWordsService.search(dto);
    }
}
