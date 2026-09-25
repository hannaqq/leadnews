package com.news.article.controller;

import com.news.article.service.ApArticleService;
import lombok.RequiredArgsConstructor;
import com.news.common.constants.ArticleConstants;
import com.news.model.article.dtos.ArticleHomeDto;
import com.news.model.article.dtos.ArticleInfoDto;
import com.news.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/article")
@RequiredArgsConstructor
public class ArticleHomeController {

    private final ApArticleService apArticleService;

    @PostMapping("/load")
    public ResponseResult load(@RequestBody ArticleHomeDto dto){
        return apArticleService.load(dto, ArticleConstants.LOADTYPE_LOAD_MORE);
    }

    @PostMapping("/loadmore")
    public ResponseResult loadMore(@RequestBody ArticleHomeDto dto){
        return apArticleService.load(dto,ArticleConstants.LOADTYPE_LOAD_MORE);

    }

    @PostMapping("/loadnew")
    public ResponseResult loadnew(@RequestBody ArticleHomeDto dto){
        return apArticleService.load(dto,ArticleConstants.LOADTYPE_LOAD_NEW);
    }

    @PostMapping("/load_article_behavior")
    public ResponseResult loadArticleBehavior(@RequestBody ArticleInfoDto dto){
        return apArticleService.loadArticleBehavior(dto);
    }

    @PostMapping("/load_article_info")
    public ResponseResult loadArticleInfo(){
        return null;
    }




}
