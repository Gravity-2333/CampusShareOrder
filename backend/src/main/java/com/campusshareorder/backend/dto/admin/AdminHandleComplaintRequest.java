package com.campusshareorder.backend.dto.admin;

import lombok.Data;

@Data
public class AdminHandleComplaintRequest {
    private Boolean isApproved;
    private String result;
}
