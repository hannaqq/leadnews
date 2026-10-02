package com.news.article.service;

import com.news.model.behavior.dtos.CollectionBehaviorDto;
import com.news.model.common.dtos.ResponseResult;

public interface ApCollectionService {
    public ResponseResult collect(CollectionBehaviorDto dto);
}
