package com.news.article.controller;

import com.news.article.service.ApCollectionService;
import com.news.model.behavior.dtos.CollectionBehaviorDto;
import com.news.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ArticleCollectionController {
    @Autowired
    private ApCollectionService apCollectionService;

    @PostMapping("/collection_behavior")
    public ResponseResult collect(@RequestBody CollectionBehaviorDto dto){
        return apCollectionService.collect(dto);
    }
}
