package com.campusshareorder.backend.vo.auth;

import lombok.Data;

@Data
public class UserLoginVO {
    private String token;
    private LoginUserInfoVO userInfo;
}
