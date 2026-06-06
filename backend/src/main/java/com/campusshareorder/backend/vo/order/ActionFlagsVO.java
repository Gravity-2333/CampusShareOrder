package com.campusshareorder.backend.vo.order;

import lombok.Data;

@Data
public class ActionFlagsVO {
    private boolean canJoin;
    private boolean canPay;
    private boolean canExit;
    private boolean canCancel;
    private boolean canUploadReceipt;
    private boolean canMarkDelivered;
    private boolean canConfirmReceived;
    private boolean canCreateComplaint;
    private boolean canViewReceipt;
}
