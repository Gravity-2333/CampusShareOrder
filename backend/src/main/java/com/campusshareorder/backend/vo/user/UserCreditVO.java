package com.campusshareorder.backend.vo.user;

import lombok.Data;

import java.util.List;

@Data
public class UserCreditVO {
    private Integer creditScore;
    private List<UserCreditItemVO> records;
}
