package com.news.user.controller;

import com.news.model.common.dtos.ResponseResult;
import lombok.RequiredArgsConstructor;
import com.news.model.user.dtos.LoginDto;
import com.news.user.service.ApUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/login")
@Tag(name = "AppUserLoginApi",description = "app user login")
@RequiredArgsConstructor
public class ApUserLoginController {

    private final ApUserService apUserService;

    @PostMapping("/login_auth")
    @Operation(summary = "user login")
    public ResponseResult login(@RequestBody LoginDto dto){
        return apUserService.login(dto);
    }
}
