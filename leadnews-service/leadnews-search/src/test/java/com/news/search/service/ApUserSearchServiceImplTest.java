package com.news.search.service;

import com.news.model.common.dtos.ResponseResult;
import com.news.search.pojos.ApUserSearch;
import com.news.search.service.impl.ApUserSearchServiceImpl;
import com.news.utils.thread.AppThreadLocalUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApUserSearchServiceImplTest {

    private final MongoTemplate mongoTemplate = mock(MongoTemplate.class);
    private final ApUserSearchServiceImpl service = new ApUserSearchServiceImpl(mongoTemplate);

    @AfterEach
    void clearUserContext() {
        AppThreadLocalUtil.clear();
    }

    @Test
    void returnsSearchHistoryWhenMongoDbIsAvailable() {
        ApUserSearch history = new ApUserSearch();
        history.setKeyword("redis");
        when(mongoTemplate.find(any(Query.class), eq(ApUserSearch.class)))
                .thenReturn(List.of(history));
        AppThreadLocalUtil.setUserId(7);

        ResponseResult<?> result = service.findUserSearch();

        assertEquals(200, result.getCode());
        List<?> data = (List<?>) result.getData();
        assertEquals(1, data.size());
        assertSame(history, data.get(0));
    }

    @Test
    void returnsEmptyHistoryWhenMongoDbIsUnavailable() {
        when(mongoTemplate.find(any(Query.class), eq(ApUserSearch.class)))
                .thenThrow(new DataAccessResourceFailureException("MongoDB unavailable"));
        AppThreadLocalUtil.setUserId(7);

        ResponseResult<?> result = service.findUserSearch();

        assertEquals(200, result.getCode());
        List<?> data = (List<?>) result.getData();
        assertEquals(0, data.size());
    }

    @Test
    void ignoresMongoDbFailureWhenRecordingSearchHistory() {
        when(mongoTemplate.findOne(any(Query.class), eq(ApUserSearch.class)))
                .thenThrow(new DataAccessResourceFailureException("MongoDB unavailable"));

        assertDoesNotThrow(() -> service.recordSearchHistory("redis", 7));
    }
}
