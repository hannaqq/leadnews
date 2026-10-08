package com.news.search.service.impl;

import com.news.model.common.dtos.ResponseResult;
import lombok.RequiredArgsConstructor;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.search.dtos.HistorySearchDto;
import com.news.search.pojos.ApUserSearch;
import com.news.search.service.ApUserSearchService;
import com.news.utils.thread.AppThreadLocalUtil;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApUserSearchServiceImpl implements ApUserSearchService {
    private final MongoTemplate mongoTemplate;

    @Override
    @Async
    public void insert(String keyword,Integer userId) {
        Query query = Query.query(Criteria.where("userId").is(userId).and("keyword").is(keyword));
        ApUserSearch apUserSearch = mongoTemplate.findOne(query, ApUserSearch.class);

        if(apUserSearch != null){
            apUserSearch.setCreatedTime(new Date());
            mongoTemplate.save(apUserSearch);
            return;
        }

        apUserSearch = new ApUserSearch();
        apUserSearch.setUserId(userId);
        apUserSearch.setKeyword(keyword);
        apUserSearch.setCreatedTime(new Date());

        Query query1 = Query.query(Criteria.where("userId").is(userId))
                .with(Sort.by(Sort.Direction.DESC, "createdTime"));
        List<ApUserSearch> list = mongoTemplate.find(query1, ApUserSearch.class);
        if(list == null || list.size()<10){
            mongoTemplate.save(apUserSearch);
        } else {
            mongoTemplate.findAndReplace(Query.query(Criteria.where("id").is(list.get(list.size()-1).getId())), apUserSearch);

        }


    }

    @Override
    public ResponseResult findUserSearch() {
        Integer userId = AppThreadLocalUtil.getUserId();
        if(userId == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        List<ApUserSearch> list = mongoTemplate.find(Query.query(Criteria
                .where("userId").is(userId))
                .with(Sort.by(Sort.Direction.DESC, "createdTime")), ApUserSearch.class);

        return ResponseResult.okResult(list);
    }

    @Override
    public ResponseResult delete(HistorySearchDto dto) {
        Integer userId = AppThreadLocalUtil.getUserId();
        if(userId == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        if(dto ==null || dto.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        mongoTemplate.remove(Query.query(Criteria
                .where("userId").is(userId
                )
                .and("id").is(dto.getId())),ApUserSearch.class);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
