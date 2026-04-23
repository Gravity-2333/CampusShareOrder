package com.campusshareorder.backend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("credit_change_record")
public class CreditChangeRecord {
    @TableId
    private Long id;
    private Long userId;
    private Integer changeValue;
    private String reasonType;
    private Long relatedOrderId;
    private Long relatedComplaintId;
    private String remark;
    private LocalDateTime createdAt;
}