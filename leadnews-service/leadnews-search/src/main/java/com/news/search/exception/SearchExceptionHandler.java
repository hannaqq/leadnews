package com.news.search.exception;

import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.news.search")
public class SearchExceptionHandler {

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ResponseResult<?>> handleSearchUnavailable(IOException exception) {
        log.error("Search backend is unavailable", exception);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ResponseResult.errorResult(
                        AppHttpCodeEnum.SERVER_ERROR,
                        "Search is temporarily unavailable"));
    }
}
