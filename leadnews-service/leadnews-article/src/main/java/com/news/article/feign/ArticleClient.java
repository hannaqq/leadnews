package com.news.article.feign;

import lombok.RequiredArgsConstructor;
import com.news.apis.article.IArticleClient;
import com.news.article.service.ApArticleService;
import com.news.model.article.dtos.ArticleDto;
import com.news.model.article.pojos.ApArticle;
import com.news.model.common.dtos.ResponseResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/article")
@RequiredArgsConstructor
public class ArticleClient implements IArticleClient {
    private final ApArticleService apArticleService;

    @PostMapping("/save")
    @Override
    public ResponseResult saveArticle(ArticleDto dto) {
        return apArticleService.saveArticle(dto);
    }

    @Override
    @GetMapping("/del/{id}")
    public ResponseResult delArticle(Long id) {
        return apArticleService.delArticle(id);
    }

    @GetMapping("/getOne/{id}")
    public ApArticle getOne(Long id){

        return apArticleService.getById(id);
    }

}
