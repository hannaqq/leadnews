package com.news.article.service;

import com.news.model.article.pojos.ApAuthor;

public interface ApAuthorService {
    ApAuthor save(ApAuthor author);
    ApAuthor findByUserId(Integer userId);
}
