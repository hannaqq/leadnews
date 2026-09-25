package com.news.search.controller;

import com.news.model.common.dtos.ResponseResult;
import lombok.RequiredArgsConstructor;
import com.news.model.search.dtos.UserSearchDto;
import com.news.search.service.ArticleSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/article/search")
@RequiredArgsConstructor
public class ArticleSearchController {
    private final ArticleSearchService articleSearchService;

    @PostMapping("/search")
    public ResponseResult search(@RequestBody UserSearchDto userSearchDto) throws IOException {
        return articleSearchService.search(userSearchDto);
    }
}
