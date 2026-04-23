package com.campusshareorder.backend.vo.order;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TimelineItemVO {
    private String action;
    private String description;
    private LocalDateTime time;
    private String operator;
}