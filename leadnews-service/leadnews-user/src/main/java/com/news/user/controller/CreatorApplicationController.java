package com.news.user.controller;

import com.news.model.common.dtos.ResponseResult;
import com.news.model.user.dtos.CreatorApplicationQueryDto;
import com.news.model.user.dtos.CreatorApplicationRejectDto;
import com.news.model.user.dtos.CreatorApplicationSubmitDto;
import com.news.user.service.CreatorApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/creator-applications")
@RequiredArgsConstructor
public class CreatorApplicationController {

    private final CreatorApplicationService creatorApplicationService;

    @PostMapping
    public ResponseResult submit(@Valid @RequestBody CreatorApplicationSubmitDto dto) {
        return creatorApplicationService.submit(dto);
    }

    @GetMapping("/me")
    public ResponseResult getMine() {
        return creatorApplicationService.getMine();
    }

    @PostMapping("/admin/list")
    public ResponseResult getList(@RequestBody CreatorApplicationQueryDto dto) {
        return creatorApplicationService.getList(dto);
    }

    @PostMapping("/admin/{id}/approve")
    public ResponseResult approve(@PathVariable Integer id) {
        return creatorApplicationService.approve(id);
    }

    @PostMapping("/admin/{id}/reject")
    public ResponseResult reject(
            @PathVariable Integer id,
            @Valid @RequestBody CreatorApplicationRejectDto dto) {
        return creatorApplicationService.reject(id, dto);
    }
}
