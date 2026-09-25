package com.news.article.service;

import com.news.model.article.pojos.ApArticle;

public interface ArticleFreemarkerService {
    public void buildArticleToMinIO(ApArticle apArticle, String content);
}
