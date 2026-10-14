package com.news.common.exception;

import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResponseResult<?>> handleCustomException(CustomException exception) {
        log.warn("Business request failed: {}", exception.getAppHttpCodeEnum());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseResult.errorResult(exception.getAppHttpCodeEnum()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseResult<?>> handleUnexpectedException(Exception exception) {
        log.error("Unexpected server error", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR));
    }
}
