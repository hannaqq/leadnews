package com.news.search.controller;

import com.news.model.common.dtos.ResponseResult;
import lombok.RequiredArgsConstructor;
import com.news.model.search.dtos.HistorySearchDto;
import com.news.search.service.ApUserSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
public class ApUserSearchController {
    private final ApUserSearchService apUserSearchService;

    @PostMapping("/load")
    public ResponseResult findUserSearch(){
        return apUserSearchService.findUserSearch();
    }

    @PostMapping("/del")
    public ResponseResult delete(@RequestBody HistorySearchDto dto){
        return apUserSearchService.delete(dto);
    }
}
