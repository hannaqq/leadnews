package com.news.article.service.impl;

import com.news.article.repository.ApArticleRepository;
import com.news.article.service.transaction.ArticleTransactionService;
import com.news.article.service.ApArticleService;
import com.news.article.service.ArticleFreemarkerService;
import com.news.common.constants.ArticleConstants;
import com.news.model.article.dtos.ArticleDto;
import com.news.model.article.dtos.ArticleHomeDto;
import com.news.model.article.dtos.ArticleInfoDto;
import com.news.model.article.pojos.ApArticle;
import com.news.model.article.vos.ArticleDetailVo;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApArticleServiceImpl implements ApArticleService {

    private static final int DEFAULT_PAGE_SIZE = 7;
    private static final int MAX_PAGE_SIZE = 50;

    private final ApArticleRepository articleRepository;
    private final ArticleTransactionService transactionService;
    private final ArticleFreemarkerService articleFreemarkerService;

    @Override
    public ResponseResult load(ArticleHomeDto dto, Short type) {
        int size = dto.getSize() == null || dto.getSize() <= 0
                ? DEFAULT_PAGE_SIZE
                : Math.min(dto.getSize(), MAX_PAGE_SIZE);
        dto.setSize(size);

        if (!ArticleConstants.LOADTYPE_LOAD_MORE.equals(type)
                && !ArticleConstants.LOADTYPE_LOAD_NEW.equals(type)) {
            type = ArticleConstants.LOADTYPE_LOAD_MORE;
        }
        if (StringUtils.isBlank(dto.getTag())) {
            dto.setTag(ArticleConstants.DEFAULT_TAG);
        }
        if (dto.getMaxBehotTime() == null) {
            dto.setMaxBehotTime(new Date());
        }
        if (dto.getMinBehotTime() == null) {
            dto.setMinBehotTime(new Date());
        }

        Date beforeTime = ArticleConstants.LOADTYPE_LOAD_MORE.equals(type)
                ? dto.getMinBehotTime() : null;
        Date afterTime = ArticleConstants.LOADTYPE_LOAD_NEW.equals(type)
                ? dto.getMaxBehotTime() : null;
        Integer channelId = ArticleConstants.DEFAULT_TAG.equals(dto.getTag())
                ? null : Integer.valueOf(dto.getTag());
        List<ApArticle> articles = articleRepository.findFeed(
                beforeTime, afterTime, channelId, PageRequest.of(0, size));
        return ResponseResult.okResult(articles);
    }

    @Override
    public ResponseResult saveArticle(ArticleDto dto) {
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApArticle article = transactionService.save(dto);
        articleFreemarkerService.buildArticleToMinIO(article, dto.getContent());
        return ResponseResult.okResult(article.getId());
    }

    @Override
    public ResponseResult delArticle(Long id) {
        transactionService.delete(id);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode());
    }

    @Override
    public ResponseResult loadArticleBehavior(ArticleInfoDto dto) {
        return null;
    }

    @Override
    public ResponseResult loadArticleInfo(Long articleId) {
        if (articleId == null || articleId <= 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApArticle article = articleRepository.findById(articleId).orElse(null);
        if (article == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        Integer creatorId;
        try {
            creatorId = Math.toIntExact(article.getAuthorId());
        } catch (ArithmeticException | NullPointerException exception) {
            return ResponseResult.errorResult(
                    AppHttpCodeEnum.SERVER_ERROR, "article has an invalid creator");
        }

        ArticleDetailVo detail = new ArticleDetailVo();
        detail.setArticleId(article.getId());
        detail.setCreatorId(creatorId);
        detail.setAuthorName(article.getAuthorName());
        detail.setTitle(article.getTitle());
        detail.setStaticUrl(article.getStaticUrl());
        detail.setPublishTime(article.getPublishTime());
        return ResponseResult.okResult(detail);
    }

    @Override
    public ApArticle getById(Long id) {
        return articleRepository.findById(id).orElse(null);
    }
}
