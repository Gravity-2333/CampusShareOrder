import { getDatabase, mutateDatabase, pageResult, timestamp } from './database'
import { makeFailure } from './database'
import { buildOrderDetail, requireAdmin, sleep } from './shared'

export const getDashboardOverview = async () => {
  await requireAdmin()
  await sleep()

  const database = getDatabase()
  const recentOrders = database.orders.slice(0, 5).map((order) => ({
    creatorNickname: order.initiatorNickname,
    currentMemberCount: order.currentMemberCount,
    deadlineAt: order.deadlineAt,
    estimatedTotalAmount: order.estimatedTotalAmount,
    orderId: order.orderId,
    orderNo: order.orderNo,
    pickupPoint: order.pickupPoint,
    productName: order.productName,
    status: order.status,
    totalMemberCount: order.totalMemberCount,
  }))

  return {
    metrics: {
      complaints: database.complaints.length,
      orders: database.orders.length,
      users: database.users.length,
    },
    recentComplaints: database.complaints.slice(0, 5),
    recentLogs: database.logs.slice(0, 5),
    recentOrders,
  }
}

export const getUsers = async (params = {}) => {
  await requireAdmin()
  await sleep()

  const keyword = String(params.keyword || '').trim()
  const status = String(params.status || '').trim()
  const list = getDatabase().users.filter((user) => {
    const matchKeyword =
      !keyword || user.nickname.includes(keyword) || user.phone.includes(keyword)
    const matchStatus = !status || user.status === status
    return matchKeyword && matchStatus
  })

  return pageResult(list, Number(params.page || 1), Number(params.pageSize || 10))
}

export const getUserDetail = async (userId) => {
  await requireAdmin()
  await sleep()

  const user = getDatabase().users.find((item) => item.userId === Number(userId))

  if (!user) {
    makeFailure(40401, '用户不存在')
  }

  return {
    ...user,
    creditRecords: user.creditRecords || [],
  }
}

export const banUser = async (userId, payload = { reason: '管理员封禁用户' }) => {
  await requireAdmin()

  mutateDatabase((draft) => {
    const user = draft.users.find((item) => item.userId === Number(userId))

    if (!user) {
      makeFailure(40401, '用户不存在')
    }

    user.status = 'BANNED'

    const nextLogId = Math.max(0, ...draft.logs.map((item) => item.logId)) + 1
    draft.logs.unshift({
      action: 'USER_BANNED',
      createdAt: timestamp(),
      logId: nextLogId,
      operatorName: '管理员',
      targetNo: `USER-${user.userId}`,
    })

    if (payload.reason) {
      user.contactInfo = `${user.contactInfo || ''} [封禁原因] ${payload.reason}`.trim()
    }
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

    const nextLogId = Math.max(0, ...draft.logs.map((item) => item.logId)) + 1
    draft.logs.unshift({
      action: 'USER_UNBANNED',
      createdAt: timestamp(),
      logId: nextLogId,
      operatorName: '管理员',
      targetNo: `USER-${user.userId}`,
    })
  })

  return {
    success: true,
    userId: Number(userId),
  }
}

export const getOrders = async (params = {}) => {
  await requireAdmin()
  await sleep()

  const keyword = String(params.keyword || '').trim()
  const status = String(params.status || '').trim()
  const list = getDatabase()
    .orders.filter((order) => {
      const matchKeyword =
        !keyword ||
        order.orderNo.includes(keyword) ||
        order.productName.includes(keyword) ||
        order.pickupPoint.includes(keyword) ||
        order.initiatorNickname.includes(keyword)
      const matchStatus = !status || order.status === status
      return matchKeyword && matchStatus
    })
    .map((order) => ({
      creatorNickname: order.initiatorNickname,
      currentMemberCount: order.currentMemberCount,
      deadlineAt: order.deadlineAt,
      estimatedTotalAmount: order.estimatedTotalAmount,
      orderId: order.orderId,
      orderNo: order.orderNo,
      pickupPoint: order.pickupPoint,
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

export const cancelOrder = async (orderId, payload = { reason: '管理员取消订单' }) => {
  await requireAdmin()

  mutateDatabase((draft) => {
    const order = draft.orders.find((item) => item.orderId === Number(orderId))

    if (!order) {
      makeFailure(40401, '订单不存在')
    }

    order.status = 'CANCELED'

    const nextLogId = Math.max(0, ...draft.logs.map((item) => item.logId)) + 1
    draft.logs.unshift({
      action: 'ORDER_CANCELED_BY_ADMIN',
      createdAt: timestamp(),
      logId: nextLogId,
      operatorName: '管理员',
      targetNo: order.orderNo,
    })

    if (payload.reason) {
      order.productDesc = `${order.productDesc || ''}\n[管理员取消原因] ${payload.reason}`.trim()
    }
  })

  return {
    canceled: true,
    orderId: Number(orderId),
  }
}

export const getComplaints = async (params = {}) => {
  await requireAdmin()
  await sleep()

  const status = String(params.status || '').trim()
  const list = getDatabase().complaints.filter((item) => !status || item.status === status)

  return pageResult(list, Number(params.page || 1), Number(params.pageSize || 10))
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

  const type = String(params.type || '').trim()
  const list = getDatabase().records.filter((item) => !type || item.type === type)

  return pageResult(list, Number(params.page || 1), Number(params.pageSize || 10))
}

export const getOperationLogs = async (params = {}) => {
  await requireAdmin()
  await sleep()

  const action = String(params.action || '').trim()
  const list = getDatabase().logs.filter((item) => !action || item.action.includes(action))

  return pageResult(list, Number(params.page || 1), Number(params.pageSize || 10))
}
