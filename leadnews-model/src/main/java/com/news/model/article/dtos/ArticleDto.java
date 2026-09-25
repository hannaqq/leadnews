package com.news.model.article.dtos;

import com.news.model.article.pojos.ApArticle;
import lombok.Data;

@Data
public class ArticleDto extends ApArticle {
    private String content;
}
