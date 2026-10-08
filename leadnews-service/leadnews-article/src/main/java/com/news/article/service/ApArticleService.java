package com.news.article.service;

import com.news.model.article.dtos.ArticleDto;
import com.news.model.article.dtos.ArticleHomeDto;
import com.news.model.article.dtos.ArticleInfoDto;
import com.news.model.article.pojos.ApArticle;
import com.news.model.common.dtos.ResponseResult;

public interface ApArticleService {

    public ResponseResult load(ArticleHomeDto dto,Short type);

    public ResponseResult saveArticle(ArticleDto dto);

    public ResponseResult delArticle(Long id);

    public ResponseResult loadArticleBehavior(ArticleInfoDto dto);

    ResponseResult loadArticleInfo(Long articleId);

    ApArticle getById(Long id);
}
