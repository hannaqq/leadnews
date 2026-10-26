package com.news.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.util.ObjectBuilder;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.search.dtos.UserSearchDto;
import com.news.model.search.vos.SearchArticleVo;
import com.news.search.service.impl.ArticleSearchServiceImpl;
import com.news.utils.thread.AppThreadLocalUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArticleSearchServiceImplTest {

    private final ElasticsearchClient elasticsearchClient = mock(ElasticsearchClient.class);
    private final ApUserSearchService userSearchService = mock(ApUserSearchService.class);
    private final ArticleSearchServiceImpl service =
            new ArticleSearchServiceImpl(elasticsearchClient, userSearchService);

    @AfterEach
    void clearUserContext() {
        AppThreadLocalUtil.clear();
    }

    @Test
    void returnsSearchResultsAndSubmitsHistoryRecording() throws Exception {
        SearchResponse<SearchArticleVo> searchResponse = mock(SearchResponse.class);
        HitsMetadata<SearchArticleVo> hits = mock(HitsMetadata.class);
        when(searchResponse.hits()).thenReturn(hits);
        when(hits.hits()).thenReturn(List.of());
        when(elasticsearchClient.search(
                org.mockito.ArgumentMatchers
                        .<Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>>>any(),
                eq(SearchArticleVo.class)))
                .thenReturn(searchResponse);

        AppThreadLocalUtil.setUserId(7);

        UserSearchDto dto = new UserSearchDto();
        dto.setSearchWords("kafka");
        dto.setPageNum(1);
        dto.setPageSize(10);
        dto.setMinBehotTime(new Date());

        ResponseResult<?> result = service.search(dto);

        assertEquals(200, result.getCode());
        assertInstanceOf(List.class, result.getData());
        assertEquals(0, ((List<?>) result.getData()).size());
        verify(userSearchService).recordSearchHistory("kafka", 7);
    }
}
