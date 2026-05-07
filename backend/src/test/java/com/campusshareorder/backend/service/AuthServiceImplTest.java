package com.campusshareorder.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campusshareorder.backend.common.exception.BusinessException;
import com.campusshareorder.backend.dto.auth.UserLoginRequest;
import com.campusshareorder.backend.mapper.AdminAccountMapper;
import com.campusshareorder.backend.mapper.UserAccountMapper;
import com.campusshareorder.backend.service.impl.AuthServiceImpl;
import com.campusshareorder.backend.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AdminAccountMapper adminAccountMapper;
    @Mock
    private UserAccountMapper userAccountMapper;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void loginWithUnknownPhoneReturnsUserNotFoundMessage() {
        UserLoginRequest request = new UserLoginRequest();
        request.setPhone("13131313131");
        request.setPassword("Aa123456");
        when(userAccountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getMessage()).isEqualTo("用户不存在"));
    }
}
