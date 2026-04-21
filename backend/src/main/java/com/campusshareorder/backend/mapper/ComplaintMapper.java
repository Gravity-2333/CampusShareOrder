package com.campusshareorder.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusshareorder.backend.entity.Complaint;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ComplaintMapper extends BaseMapper<Complaint> {
}