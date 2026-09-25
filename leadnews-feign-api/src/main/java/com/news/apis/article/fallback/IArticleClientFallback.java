package com.news.apis.article.fallback;

import com.news.apis.article.IArticleClient;
import com.news.model.article.dtos.ArticleDto;
import com.news.model.article.pojos.ApArticle;
import com.news.model.article.pojos.ApAuthor;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

@Component
public class IArticleClientFallback implements IArticleClient {
    @Override
    public ResponseResult saveArticle(ArticleDto dto) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"can't get data");
    }

    @Override
    public ResponseResult delArticle(Long id) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"can't get data");
    }

    @Override
    public ApArticle getOne(Long id) {
        return null;
    }

    @Override
    public ResponseResult saveApAuthor(ApAuthor apAuthor) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"can't get data");
    }

    public ApAuthor getByUserId(@PathVariable Integer id){
        return null;
    }


}
