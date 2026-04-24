package com.campusshareorder.backend.common;

import com.campusshareorder.backend.common.enums.ErrorCode;
import com.campusshareorder.backend.common.exception.BusinessException;
import com.campusshareorder.backend.common.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<?> handleBusinessException(BusinessException e) {
        log.warn("Business exception: code={}, message={}", e.getCode(), e.getMessage());
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class
    })
    public ApiResponse<?> handleValidationException(Exception e) {
        String message = resolveValidationMessage(e);
        log.warn("Validation exception: {}", message);
        return ApiResponse.error(ErrorCode.VALIDATION_ERROR.getCode(), message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("Request body parse exception: {}", e.getMessage());
        return ApiResponse.error(ErrorCode.VALIDATION_ERROR.getCode(), "请求体格式不正确");
    }

    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<?> handleRuntimeException(RuntimeException e) {
        log.error("Runtime exception", e);
        return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception e) {
        log.error("System exception", e);
        return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage());
    }

    private String resolveValidationMessage(Exception e) {
        if (e instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            FieldError fieldError = methodArgumentNotValidException.getBindingResult().getFieldError();
            return fieldError == null ? ErrorCode.VALIDATION_ERROR.getMessage() : fieldError.getDefaultMessage();
        }
        if (e instanceof BindException bindException) {
            FieldError fieldError = bindException.getBindingResult().getFieldError();
            return fieldError == null ? ErrorCode.VALIDATION_ERROR.getMessage() : fieldError.getDefaultMessage();
        }
        if (e instanceof ConstraintViolationException constraintViolationException) {
            return constraintViolationException.getConstraintViolations().stream()
                    .findFirst()
                    .map(item -> item.getMessage() == null ? ErrorCode.VALIDATION_ERROR.getMessage() : item.getMessage())
                    .orElse(ErrorCode.VALIDATION_ERROR.getMessage());
        }
        return ErrorCode.VALIDATION_ERROR.getMessage();
    }
}
