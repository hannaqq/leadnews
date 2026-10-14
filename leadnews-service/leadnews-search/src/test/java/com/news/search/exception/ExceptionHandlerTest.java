package com.news.search.exception;

import com.news.common.exception.GlobalExceptionHandler;
import com.news.model.common.enums.AppHttpCodeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExceptionHandlerTest {

    @Test
    void returnsServiceUnavailableWhenSearchBackendFails() {
        var response = new SearchExceptionHandler()
                .handleSearchUnavailable(new IOException("connection refused"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(AppHttpCodeEnum.SERVER_ERROR.getCode(), response.getBody().getCode());
        assertEquals("Search is temporarily unavailable", response.getBody().getErrorMessage());
    }

    @Test
    void returnsInternalServerErrorForUnexpectedExceptions() {
        var response = new GlobalExceptionHandler()
                .handleUnexpectedException(new IllegalStateException("unexpected"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(AppHttpCodeEnum.SERVER_ERROR.getCode(), response.getBody().getCode());
    }
}
