package com.news.admin.controller;

import com.news.admin.service.AdUserService;
import lombok.RequiredArgsConstructor;
import com.news.model.admin.dtos.AdUserDto;
import com.news.model.common.dtos.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class AdUserLoginController {

    private final AdUserService adUserService;

    @PostMapping("/in")
    public ResponseResult login(@RequestBody AdUserDto dto){
        return adUserService.login(dto);
    }
}
