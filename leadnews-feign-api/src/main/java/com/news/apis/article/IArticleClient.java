package com.news.apis.article;

import com.news.model.article.dtos.ArticleDto;
import com.news.model.article.pojos.ApArticle;
import com.news.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "leadnews-article", path = "/api/v1/article")
public interface IArticleClient {
    @PostMapping("/save")
    public ResponseResult saveArticle(@RequestBody ArticleDto dto);

    @GetMapping("/del/{id}")
    public ResponseResult delArticle(@PathVariable Long id);

    @GetMapping("/getOne/{id}")
    public ApArticle getOne(@PathVariable Long id);

}
