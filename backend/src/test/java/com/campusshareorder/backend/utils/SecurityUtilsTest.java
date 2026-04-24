package com.campusshareorder.backend.utils;

import com.campusshareorder.backend.common.enums.ErrorCode;
import com.campusshareorder.backend.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getRequiredUserIdAllowsUserRole() {
        bindRequestWithRole("USER");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(100L, null, List.of())
        );

        assertThat(SecurityUtils.getRequiredUserId()).isEqualTo(100L);
    }

    @Test
    void getRequiredUserIdRejectsAdminRole() {
        bindRequestWithRole("ADMIN");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(900L, null, List.of())
        );

        assertThatThrownBy(SecurityUtils::getRequiredUserId)
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.FORBIDDEN.getCode()));
    }

    @Test
    void getRequiredCurrentUserIdRejectsAnonymousRequest() {
        bindRequestWithRole("USER");

        assertThatThrownBy(SecurityUtils::getRequiredCurrentUserId)
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ErrorCode.UNAUTHORIZED.getCode()));
    }

    private void bindRequestWithRole(String role) {
        HttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("role", role);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
