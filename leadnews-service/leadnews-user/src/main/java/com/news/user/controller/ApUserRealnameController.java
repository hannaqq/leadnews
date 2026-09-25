package com.news.user.controller;

import com.news.model.user.dtos.AuthDto;
import com.news.model.common.dtos.ResponseResult;
import com.news.user.service.ApUserRealnameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class ApUserRealnameController {

    @Autowired
    private ApUserRealnameService apUserRealnameService;

    @PostMapping(value = "/list")
    public ResponseResult getList(@RequestBody AuthDto dto){
        return apUserRealnameService.getList(dto);
    }

    @PostMapping(value = "/authPass")
    public ResponseResult pass(@RequestBody AuthDto dto){
        return apUserRealnameService.pass(dto);
    }

    @PostMapping(value = "/authFail")
    public ResponseResult fail(@RequestBody AuthDto dto){
        return apUserRealnameService.fail(dto);
    }



}
