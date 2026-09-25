package com.news.article.feign;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.news.apis.article.IArticleClient;
import com.news.article.mapper.ApAuthorMapper;
import com.news.article.service.ApArticleService;
import com.news.model.article.dtos.ArticleDto;
import com.news.model.article.pojos.ApArticle;
import com.news.model.article.pojos.ApAuthor;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/article")
public class ArticleClient implements IArticleClient {
    @Autowired
    private ApArticleService apArticleService;

    @Autowired
    private ApAuthorMapper apAuthorMapper;

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

    @PostMapping("/apAuthor/save")
    public ResponseResult saveApAuthor(ApAuthor apAuthor) {
        apAuthorMapper.insert(apAuthor);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @GetMapping("/apAuthor/getOne/{id}")
    public ApAuthor getByUserId(@PathVariable Integer id){
        return apAuthorMapper.selectOne(Wrappers.<ApAuthor>lambdaQuery().eq(ApAuthor::getUserId, id));

    }



}
