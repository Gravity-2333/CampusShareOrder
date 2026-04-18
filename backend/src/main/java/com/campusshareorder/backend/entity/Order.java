package com.campusshareorder.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("order")
public class Order {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private Integer targetCount;

    private Integer currentCount;

    private LocalDateTime deadline;

    private BigDecimal totalAmount;

    private BigDecimal amountPerPerson;

    private Integer status;

    private Long creatorId;

    private String deliveryAddress;

    private LocalDateTime deliveryTime;

    private String proofImage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
