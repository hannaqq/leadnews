package com.news.wemedia.controller;

import com.news.model.common.dtos.ResponseResult;
import lombok.RequiredArgsConstructor;
import com.news.model.wemedia.dtos.WmLoginDto;
import com.news.wemedia.service.WmUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {

    private final WmUserService wmUserService;

    @PostMapping("/in")
    public ResponseResult login(@RequestBody WmLoginDto dto){
        return wmUserService.login(dto);
    }


}
