export const roleTextMap = {
  ADMIN: '管理员',
  INITIATOR: '发起人',
  MEMBER: '成员',
  USER: '用户',
  VISITOR: '访客',
}

export const userStatusTextMap = {
  BANNED: '已封禁',
  NORMAL: '正常',
}

export const orderStatusTextMap = {
  CANCELED: '已取消',
  COMPLETED: '已完成',
  GROUPED: '已成团',
  OPEN: '招募中',
  WAIT_DELIVERY: '待送达',
  WAIT_RECEIVE: '待收货',
}

export const payStatusTextMap = {
  PAID: '已支付',
  REFUNDED: '已退款',
  UNPAID: '未支付',
}

export const joinStatusTextMap = {
  ACTIVE: '已参与',
  CANCELED: '已取消',
  EXITED: '已退出',
  REFUNDED: '已退款',
}

export const receiveStatusTextMap = {
  AUTO_RECEIVED: '系统代收',
  NOT_READY: '未开始收货',
  RECEIVED: '已收货',
  WAIT_CONFIRM: '待确认',
}

export const complaintStatusTextMap = {
  PENDING: '待处理',
  PROCESSED: '已处理',
}

export const complaintTypeTextMap = {
  FAKE_RECEIPT: '伪造凭证',
  NOT_PURCHASED: '未购买',
}

export const capitalRecordTypeTextMap = {
  PAY: '支付',
  REFUND_CANCEL: '取消退款',
  REFUND_DIFF: '差额退款',
  REFUND_EXIT: '退出退款',
  SETTLE_TO_CREATOR: '发起人结算',
}

export const creditReasonTypeTextMap = {
  COMPLAINT_PENALTY: '投诉扣分',
  INITIAL: '初始信用分',
}

export const statusToneMap = {
  ACTIVE: 'success',
  AUTO_RECEIVED: 'success',
  BANNED: 'danger',
  CANCELED: 'info',
  COMPLETED: 'success',
  EXITED: 'info',
  GROUPED: 'success',
  NOT_READY: 'info',
  NORMAL: 'success',
  OPEN: 'warning',
  PAID: 'success',
  PENDING: 'warning',
  PROCESSED: 'info',
  RECEIVED: 'success',
  REFUNDED: 'info',
  UNPAID: 'warning',
  WAIT_CONFIRM: 'warning',
  WAIT_DELIVERY: 'primary',
  WAIT_RECEIVE: 'warning',
}
