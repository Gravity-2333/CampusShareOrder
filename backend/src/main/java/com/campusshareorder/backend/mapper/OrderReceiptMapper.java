package com.campusshareorder.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusshareorder.backend.entity.OrderReceipt;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderReceiptMapper extends BaseMapper<OrderReceipt> {
}