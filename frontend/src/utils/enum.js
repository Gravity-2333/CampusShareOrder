export const roleTextMap = {
  ADMIN: '管理员',
  USER: '用户',
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
  UNPAID: '未支付',
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

export const statusToneMap = {
  BANNED: 'danger',
  CANCELED: 'info',
  COMPLETED: 'success',
  GROUPED: 'success',
  NORMAL: 'success',
  OPEN: 'warning',
  PAID: 'success',
  PENDING: 'warning',
  PROCESSED: 'info',
  RECEIVED: 'success',
  UNPAID: 'warning',
  WAIT_CONFIRM: 'warning',
  WAIT_DELIVERY: 'primary',
  WAIT_RECEIVE: 'warning',
}
