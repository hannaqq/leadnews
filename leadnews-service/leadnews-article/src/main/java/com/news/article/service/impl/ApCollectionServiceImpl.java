package com.news.article.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.news.article.mapper.ApCollectionMapper;
import com.news.article.service.ApCollectionService;
import com.news.model.behavior.dtos.CollectionBehaviorDto;
import com.news.model.article.pojos.ApCollection;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class ApCollectionServiceImpl extends ServiceImpl<ApCollectionMapper, ApCollection> implements ApCollectionService {
    @Override
    public ResponseResult collect(CollectionBehaviorDto dto) {
        if (dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        if (dto.getOperation() == 0){
            ApCollection apCollection = new ApCollection();
            BeanUtils.copyProperties(dto, apCollection);
            apCollection.setArticleId(dto.getEntryId());
            save(apCollection);
        } else {
            remove(Wrappers.<ApCollection>lambdaQuery().eq(ApCollection::getEntryId,dto.getEntryId()));
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
