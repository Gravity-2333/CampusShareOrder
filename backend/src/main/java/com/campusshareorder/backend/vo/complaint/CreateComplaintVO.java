package com.campusshareorder.backend.vo.complaint;

import lombok.Data;

@Data
public class CreateComplaintVO {
    private Long complaintId;
    private String complaintNo;
    private String status;
}
