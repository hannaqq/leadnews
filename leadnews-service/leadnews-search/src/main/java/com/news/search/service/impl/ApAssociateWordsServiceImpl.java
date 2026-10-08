package com.news.search.service.impl;

import com.news.model.common.dtos.ResponseResult;
import lombok.RequiredArgsConstructor;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.search.dtos.UserSearchDto;
import com.news.search.pojos.ApAssociateWords;
import com.news.search.service.ApAssociateWordsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApAssociateWordsServiceImpl implements ApAssociateWordsService {

    private final MongoTemplate mongoTemplate;

    @Override
    public ResponseResult search(UserSearchDto dto) {

        if(StringUtils.isBlank(dto.getSearchWords())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        if(dto.getPageSize() >20){
            dto.setPageSize(20);
        }

        List<ApAssociateWords> wordsList = mongoTemplate.find(Query
                .query(Criteria.where("associateWords").regex(".*?\\" + dto.getSearchWords() + ".*"))
                .limit(dto.getPageSize()), ApAssociateWords.class);
        return ResponseResult.okResult(wordsList);
    }
}
