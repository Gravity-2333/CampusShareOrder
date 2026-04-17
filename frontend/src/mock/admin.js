import { getDatabase, mutateDatabase, pageResult, timestamp } from './database'
import { makeFailure } from './database'
import { buildOrderDetail, requireAdmin, sleep } from './shared'

export const getUsers = async (params = {}) => {
  await requireAdmin()
  await sleep()

  return pageResult(getDatabase().users, Number(params.page || 1), Number(params.pageSize || 10))
}

export const banUser = async (userId) => {
  await requireAdmin()

  mutateDatabase((draft) => {
    const user = draft.users.find((item) => item.userId === Number(userId))

    if (!user) {
      makeFailure(40401, '用户不存在')
    }

    user.status = 'BANNED'
  })

  return {
    success: true,
    userId: Number(userId),
  }
}

export const unbanUser = async (userId) => {
  await requireAdmin()

  mutateDatabase((draft) => {
    const user = draft.users.find((item) => item.userId === Number(userId))

    if (!user) {
      makeFailure(40401, '用户不存在')
    }

    user.status = 'NORMAL'
  })

  return {
    success: true,
    userId: Number(userId),
  }
}

export const getOrders = async (params = {}) => {
  await requireAdmin()
  await sleep()

  const list = getDatabase().orders.map((order) => ({
    creatorNickname: order.initiatorNickname,
    currentMemberCount: order.currentMemberCount,
    orderId: order.orderId,
    orderNo: order.orderNo,
    productName: order.productName,
    status: order.status,
    totalMemberCount: order.totalMemberCount,
  }))

  return pageResult(list, Number(params.page || 1), Number(params.pageSize || 10))
}

export const getOrderDetail = async (orderId) => {
  await requireAdmin()
  return buildOrderDetail(orderId, null, true)
}

export const cancelOrder = async (orderId) => {
  await requireAdmin()

  mutateDatabase((draft) => {
    const order = draft.orders.find((item) => item.orderId === Number(orderId))

    if (!order) {
      makeFailure(40401, '订单不存在')
    }

    order.status = 'CANCELED'
  })

  return {
    canceled: true,
    orderId: Number(orderId),
  }
}

export const getComplaints = async (params = {}) => {
  await requireAdmin()
  await sleep()

  return pageResult(getDatabase().complaints, Number(params.page || 1), Number(params.pageSize || 10))
}

export const getComplaintDetail = async (complaintId) => {
  await requireAdmin()
  await sleep()

  const complaint = getDatabase().complaints.find((item) => item.complaintId === Number(complaintId))

  if (!complaint) {
    makeFailure(40402, '投诉不存在')
  }

  return complaint
}

export const handleComplaint = async (complaintId, payload) => {
  await requireAdmin()

  mutateDatabase((draft) => {
    const complaint = draft.complaints.find((item) => item.complaintId === Number(complaintId))

    if (!complaint) {
      makeFailure(40402, '投诉不存在')
    }

    const order = draft.orders.find((item) => item.orderId === complaint.orderId)

    complaint.handleResult = payload.handleResult
    complaint.handledAt = timestamp()
    complaint.status = 'PROCESSED'

    if (order && order.status !== 'CANCELED' && order.status !== 'COMPLETED') {
      order.status = 'CANCELED'
      order.complaintOpened = true
    }

    const nextLogId = Math.max(0, ...draft.logs.map((item) => item.logId)) + 1
    draft.logs.unshift({
      action: 'COMPLAINT_HANDLED',
      createdAt: complaint.handledAt,
      logId: nextLogId,
      operatorName: '管理员',
      targetNo: complaint.complaintNo,
    })
  })

  return {
    complaintId: Number(complaintId),
    handled: true,
  }
}

export const getCapitalRecords = async (params = {}) => {
  await requireAdmin()
  await sleep()

  return pageResult(getDatabase().records, Number(params.page || 1), Number(params.pageSize || 10))
}

export const getOperationLogs = async (params = {}) => {
  await requireAdmin()
  await sleep()

  return pageResult(getDatabase().logs, Number(params.page || 1), Number(params.pageSize || 10))
}
