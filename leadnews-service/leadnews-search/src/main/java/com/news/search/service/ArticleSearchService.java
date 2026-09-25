package com.news.search.service;

import com.news.model.common.dtos.ResponseResult;
import com.news.model.search.dtos.UserSearchDto;

import java.io.IOException;

public interface ArticleSearchService {
    public ResponseResult search(UserSearchDto userSearchDto) throws IOException;
}
