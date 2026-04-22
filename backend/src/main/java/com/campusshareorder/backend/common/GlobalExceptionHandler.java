package com.campusshareorder.backend.common;

import com.campusshareorder.backend.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(exception.class)
    public ApiResponse<?> handleBusinessException(exception e) {
        log.error("业务异常: {}", e.getMessage());
        return ApiResponse.error(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<?> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: ", e);
        return ApiResponse.error(e.getMessage() != null ? e.getMessage() : "服务器内部错误");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception e) {
        log.error("系统异常: ", e);
        return ApiResponse.error("系统繁忙，请稍后再试");
    }
}
