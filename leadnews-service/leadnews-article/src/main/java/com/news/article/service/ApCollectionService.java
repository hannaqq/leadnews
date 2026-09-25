package com.news.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.news.model.behavior.dtos.CollectionBehaviorDto;
import com.news.model.article.pojos.ApCollection;
import com.news.model.common.dtos.ResponseResult;

public interface ApCollectionService extends IService<ApCollection> {
    public ResponseResult collect(CollectionBehaviorDto dto);
}
