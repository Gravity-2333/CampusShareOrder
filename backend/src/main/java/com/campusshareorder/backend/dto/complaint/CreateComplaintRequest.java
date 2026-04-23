package com.campusshareorder.backend.dto.complaint;

import lombok.Data;

@Data
public class CreateComplaintRequest {
    private Long orderId;
    private Long accusedUserId;
    private String type;
    private String content;
}