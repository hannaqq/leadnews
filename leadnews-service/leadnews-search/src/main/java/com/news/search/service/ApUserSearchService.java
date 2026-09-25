package com.news.search.service;

import com.news.model.common.dtos.ResponseResult;
import com.news.model.search.dtos.HistorySearchDto;

public interface ApUserSearchService {
    public void insert(String keyword, Integer userId);

    public ResponseResult findUserSearch();

    public ResponseResult delete(HistorySearchDto dto);

}
