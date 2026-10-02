package com.news.user.controller;

import com.news.model.common.dtos.ResponseResult;
import lombok.RequiredArgsConstructor;
import com.news.model.user.dtos.UserRelationDto;
import com.news.user.service.ApUserFollowService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class ApUserFollowController {
    private final ApUserFollowService apUserFollowService;

    @PostMapping("/user_follow")
    public ResponseResult followOrUnfollow(@RequestBody UserRelationDto dto){
        return apUserFollowService.followOrUnfollow(dto);
    }
}
