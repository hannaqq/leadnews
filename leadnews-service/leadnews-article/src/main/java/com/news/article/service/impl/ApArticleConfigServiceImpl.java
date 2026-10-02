package com.news.article.service.impl;

import com.news.article.repository.ApArticleConfigRepository;
import com.news.article.service.ApArticleConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApArticleConfigServiceImpl implements ApArticleConfigService {

    private final ApArticleConfigRepository repository;

    @Override
    public void updateByMap(Map map) {
        Number enable = (Number) map.get("enable");
        Number articleId = (Number) map.get("articleId");
        boolean isDown = enable == null || enable.intValue() != 1;
        repository.updateDownByArticleId(articleId.longValue(), isDown);
    }
}
