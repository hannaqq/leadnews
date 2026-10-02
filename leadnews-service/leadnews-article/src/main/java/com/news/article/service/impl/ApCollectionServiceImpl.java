package com.news.article.service.impl;

import com.news.article.repository.ApCollectionRepository;
import com.news.article.service.ApCollectionService;
import com.news.model.article.pojos.ApCollection;
import com.news.model.behavior.dtos.CollectionBehaviorDto;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApCollectionServiceImpl implements ApCollectionService {

    private final ApCollectionRepository repository;

    @Override
    @Transactional
    public ResponseResult collect(CollectionBehaviorDto dto) {
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        if (dto.getOperation() == 0) {
            ApCollection collection = new ApCollection();
            BeanUtils.copyProperties(dto, collection);
            collection.setArticleId(dto.getEntryId());
            repository.save(collection);
        } else {
            repository.deleteByEntryId(dto.getEntryId());
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
