package com.campusshareorder.backend.filter;

import com.campusshareorder.backend.common.enums.ErrorCode;
import com.campusshareorder.backend.common.response.ApiResponse;
import com.campusshareorder.backend.entity.AdminAccount;
import com.campusshareorder.backend.entity.UserAccount;
import com.campusshareorder.backend.mapper.AdminAccountMapper;
import com.campusshareorder.backend.mapper.UserAccountMapper;
import com.campusshareorder.backend.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AdminAccountMapper adminAccountMapper;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final UserAccountMapper userAccountMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            Long currentId = jwtUtil.extractUserId(token);
            String loginName = jwtUtil.extractPhone(token);
            String role = jwtUtil.extractRole(token);

            ApiResponse<?> accountError = validateAccount(currentId, role);
            if (accountError != null) {
                writeError(response, accountError);
                return;
            }

            if (currentId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        currentId, null, new ArrayList<>());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                request.setAttribute("userId", currentId);
                request.setAttribute("role", role);
                request.setAttribute("loginName", loginName);
            }
        } catch (Exception ignored) {
            // token 无效时保持匿名状态
        }

        filterChain.doFilter(request, response);
    }

    private ApiResponse<?> validateAccount(Long currentId, String role) {
        if (currentId == null) {
            return ApiResponse.error(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
        }
        if ("USER".equals(role)) {
            UserAccount user = userAccountMapper.selectById(currentId);
            if (user == null) {
                return ApiResponse.error(ErrorCode.UNAUTHORIZED.getCode(), "用户不存在");
            }
            if ("BANNED".equals(user.getStatus())) {
                return ApiResponse.error(ErrorCode.USER_BANNED.getCode(), ErrorCode.USER_BANNED.getMessage());
            }
            return null;
        }
        if ("ADMIN".equals(role)) {
            AdminAccount admin = adminAccountMapper.selectById(currentId);
            if (admin == null) {
                return ApiResponse.error(ErrorCode.UNAUTHORIZED.getCode(), "管理员不存在");
            }
            if ("BANNED".equals(admin.getStatus())) {
                return ApiResponse.error(ErrorCode.FORBIDDEN.getCode(), "管理员账号已被禁用");
            }
            return null;
        }
        return ApiResponse.error(ErrorCode.TOKEN_INVALID.getCode(), ErrorCode.TOKEN_INVALID.getMessage());
    }

    private void writeError(HttpServletResponse response, ApiResponse<?> body) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
