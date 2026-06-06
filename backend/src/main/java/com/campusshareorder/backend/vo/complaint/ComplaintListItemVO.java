package com.campusshareorder.backend.vo.complaint;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ComplaintListItemVO {
    private Long complaintId;
    private String complaintNo;
    private Long orderId;
    private String orderNo;
    private String productName;
    private Long complainantUserId;
    private String complainantNickname;
    private Long accusedUserId;
    private String accusedNickname;
    private String viewerRoleInComplaint;
    private String type;
    private String content;
    private String status;
    private Boolean openedBySystem;
    private String handleResult;
    private LocalDateTime createdAt;
    private LocalDateTime handledAt;
}
