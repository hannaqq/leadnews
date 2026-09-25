package com.news.article.controller;

import com.news.article.service.ApCollectionService;
import lombok.RequiredArgsConstructor;
import com.news.model.behavior.dtos.CollectionBehaviorDto;
import com.news.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ArticleCollectionController {
    private final ApCollectionService apCollectionService;

    @PostMapping("/collection_behavior")
    public ResponseResult collect(@RequestBody CollectionBehaviorDto dto){
        return apCollectionService.collect(dto);
    }
}
