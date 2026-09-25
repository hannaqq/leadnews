package com.news.user.controller;

import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.user.dtos.UserRelationDto;
import com.news.user.service.ApUserFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class ApUserFollowController {
    @Autowired
    private ApUserFollowService apUserFollowService;

    @PostMapping("/user_follow")
    public ResponseResult followOrUnfollow(@RequestBody UserRelationDto dto){
        apUserFollowService.followOrUnfollow(dto);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode());
    }
}
