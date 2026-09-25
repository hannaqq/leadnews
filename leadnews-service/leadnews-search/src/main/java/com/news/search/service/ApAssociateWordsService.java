package com.news.search.service;

import com.news.model.common.dtos.ResponseResult;
import com.news.model.search.dtos.UserSearchDto;

public interface ApAssociateWordsService {
    public ResponseResult  search(UserSearchDto dto);
}
