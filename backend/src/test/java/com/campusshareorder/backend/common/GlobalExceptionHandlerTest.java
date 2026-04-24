package com.campusshareorder.backend.common;

import com.campusshareorder.backend.common.enums.ErrorCode;
import com.campusshareorder.backend.common.exception.BusinessException;
import com.campusshareorder.backend.common.response.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void businessExceptionKeepsBusinessMessage() {
        ApiResponse<?> response = handler.handleBusinessException(
                new BusinessException(ErrorCode.ORDER_STATUS_INVALID, "当前状态不能操作")
        );

        assertThat(response.getCode()).isEqualTo(ErrorCode.ORDER_STATUS_INVALID.getCode());
        assertThat(response.getMessage()).isEqualTo("当前状态不能操作");
    }

    @Test
    void runtimeExceptionDoesNotLeakInternalMessage() {
        ApiResponse<?> response = handler.handleRuntimeException(
                new RuntimeException("SQL syntax error near secret_table")
        );

        assertThat(response.getCode()).isEqualTo(ErrorCode.SYSTEM_ERROR.getCode());
        assertThat(response.getMessage()).isEqualTo(ErrorCode.SYSTEM_ERROR.getMessage());
    }
}
