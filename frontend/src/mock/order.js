import { getDatabase, mutateDatabase, pageResult, timestamp } from './database'
import { addMinutes, buildOrderDetail, requireUser, sleep } from './shared'
import { makeFailure } from './database'

export const createOrder = async (payload) => {
  const user = await requireUser()

  let createdOrder = null

  mutateDatabase((draft) => {
    const orderId = Math.max(0, ...draft.orders.map((item) => item.orderId)) + 1
    const memberId = Math.max(0, ...draft.orders.flatMap((item) => item.members.map((m) => m.memberId))) + 1

    createdOrder = {
      actualTotalAmount: null,
      complaintOpened: false,
      creatorUserId: user.userId,
      currentMemberCount: 1,
      deadlineAt: payload.deadlineAt,
      deliveredAt: null,
      estimatedTotalAmount: Number(payload.estimatedTotalAmount),
      expectedDeliveryEndAt: '',
      initiatorNickname: user.nickname,
      totalMemberCount: Number(payload.totalMemberCount),
      members: [
        {
          exitedAt: null,
          joinStatus: 'ACTIVE',
          joinedAt: timestamp(),
          memberId,
          nickname: user.nickname,
          payAmount: Number(payload.estimatedTotalAmount) / Number(payload.totalMemberCount),
          paidAt: null,
          payStatus: 'UNPAID',
          receivedAt: null,
          receiveStatus: 'NOT_READY',
          refundAmountTotal: 0,
          role: 'INITIATOR',
          userId: user.userId,
        },
      ],
      orderId,
      orderNo: `GO${Date.now()}`,
      pickupPoint: payload.pickupPoint,
      productDesc: payload.productDesc || '',
      productName: payload.productName,
      receiptUploadedAt: null,
      receiptUploadDeadlineAt: '',
      status: 'OPEN',
    }

    draft.orders.unshift(createdOrder)
  })

  return {
    orderId: createdOrder.orderId,
    orderNo: createdOrder.orderNo,
    status: createdOrder.status,
  }
}

export const getOrderList = async (params = {}) => {
  await sleep()

  const database = getDatabase()
  const keyword = params.keyword?.trim()
  const status = params.status?.trim()

  const list = database.orders
    .filter((order) => !keyword || order.productName.includes(keyword) || order.orderNo.includes(keyword))
    .filter((order) => !status || order.status === status)
    .map((order) => ({
      creatorNickname: order.initiatorNickname,
      currentMemberCount: order.currentMemberCount,
      deadlineAt: order.deadlineAt,
      estimatedTotalAmount: order.estimatedTotalAmount,
      orderId: order.orderId,
      orderNo: order.orderNo,
      pickupPoint: order.pickupPoint,
      productName: order.productName,
      remainingCount: Math.max(order.totalMemberCount - order.currentMemberCount, 0),
      status: order.status,
      totalMemberCount: order.totalMemberCount,
    }))

  return pageResult(list, Number(params.page || 1), Number(params.pageSize || 10))
}

export const getOrderDetail = async (orderId) => {
  const user = await requireUser()
  return buildOrderDetail(orderId, user.userId)
}

export const joinOrder = async (orderId) => {
  const user = await requireUser()
  const database = getDatabase()
  const order = database.orders.find((item) => item.orderId === Number(orderId))

  if (!order) {
    makeFailure(40401, '订单不存在')
  }

  if (order.members.some((member) => member.userId === user.userId)) {
    makeFailure(40902, '已加入订单')
  }

  mutateDatabase((draft) => {
    const target = draft.orders.find((item) => item.orderId === Number(orderId))
    const memberId =
      Math.max(0, ...draft.orders.flatMap((item) => item.members.map((member) => member.memberId))) + 1

    target.members.push({
      exitedAt: null,
      joinStatus: 'ACTIVE',
      joinedAt: timestamp(),
      memberId,
      nickname: user.nickname,
      payAmount: Number(target.estimatedTotalAmount) / Number(target.totalMemberCount),
      paidAt: null,
      payStatus: 'UNPAID',
      receivedAt: null,
      receiveStatus: 'NOT_READY',
      refundAmountTotal: 0,
      role: 'MEMBER',
      userId: user.userId,
    })
    target.currentMemberCount += 1
  })

  return {
    orderId: Number(orderId),
    joined: true,
  }
}

const refreshOrderStatusAfterPayment = (order) => {
  if (
    order.status === 'OPEN' &&
    order.currentMemberCount === order.totalMemberCount &&
    order.members.every((member) => member.joinStatus === 'ACTIVE' && member.payStatus === 'PAID')
  ) {
    const groupedAt = timestamp()
    order.status = 'GROUPED'
    order.receiptUploadDeadlineAt = addMinutes(groupedAt, 30)
    order.expectedDeliveryEndAt = addMinutes(groupedAt, 90)
  }
}

export const payOrder = async (orderId) => {
  const user = await requireUser()

  mutateDatabase((draft) => {
    const order = draft.orders.find((item) => item.orderId === Number(orderId))
    const member = order?.members.find((item) => item.userId === user.userId)

    if (!order) {
      makeFailure(40401, '订单不存在')
    }

    if (!member) {
      makeFailure(40904, '你尚未加入该订单')
    }

    member.payStatus = 'PAID'
    member.paidAt = timestamp()
    refreshOrderStatusAfterPayment(order)
  })

  return {
    orderId: Number(orderId),
    paid: true,
  }
}

export const exitOrder = async (orderId) => {
  const user = await requireUser()

  mutateDatabase((draft) => {
    const order = draft.orders.find((item) => item.orderId === Number(orderId))
    const member = order?.members.find((item) => item.userId === user.userId)

    if (!order) {
      makeFailure(40401, '订单不存在')
    }

    if (!member) {
      makeFailure(40904, '你尚未加入该订单')
    }

    if (member.role === 'INITIATOR') {
      makeFailure(40908, '发起人不能退出订单')
    }

    member.joinStatus = 'EXITED'
    member.exitedAt = timestamp()
    order.currentMemberCount = Math.max(order.currentMemberCount - 1, 0)
  })

  return {
    exited: true,
    orderId: Number(orderId),
  }
}

export const uploadReceipt = async (orderId, payload) => {
  const user = await requireUser()

  mutateDatabase((draft) => {
    const order = draft.orders.find((item) => item.orderId === Number(orderId))

    if (!order) {
      makeFailure(40401, '订单不存在')
    }

    if (order.creatorUserId !== user.userId) {
      makeFailure(40301, '只有发起人可以上传凭证')
    }

    order.actualTotalAmount = Number(payload.actualTotalAmount)
    order.expectedDeliveryStartAt = payload.expectedDeliveryStartAt || order.expectedDeliveryStartAt
    order.expectedDeliveryEndAt = payload.expectedDeliveryEndAt || order.expectedDeliveryEndAt
    order.receiptImageUrl = payload.imageUrl || order.receiptImageUrl
    order.receiptUploadedAt = timestamp()
    order.status = 'WAIT_DELIVERY'
  })

  return {
    orderId: Number(orderId),
    uploaded: true,
  }
}

export const markDelivered = async (orderId) => {
  const user = await requireUser()

  mutateDatabase((draft) => {
    const order = draft.orders.find((item) => item.orderId === Number(orderId))

    if (!order) {
      makeFailure(40401, '订单不存在')
    }

    if (order.creatorUserId !== user.userId) {
      makeFailure(40301, '只有发起人可以确认送达')
    }

    const deliveredAt = timestamp()
    order.status = 'WAIT_RECEIVE'
    order.deliveredAt = deliveredAt
    order.members.forEach((member) => {
      member.receiveStatus = member.role === 'INITIATOR' ? 'RECEIVED' : 'WAIT_CONFIRM'
      member.receivedAt = member.role === 'INITIATOR' ? deliveredAt : null
    })
  })

  return {
    delivered: true,
    orderId: Number(orderId),
  }
}

export const confirmReceived = async (orderId) => {
  const user = await requireUser()

  mutateDatabase((draft) => {
    const order = draft.orders.find((item) => item.orderId === Number(orderId))
    const member = order?.members.find((item) => item.userId === user.userId)

    if (!order) {
      makeFailure(40401, '订单不存在')
    }

    if (!member) {
      makeFailure(40904, '你尚未加入该订单')
    }

    member.receiveStatus = 'RECEIVED'
    member.receivedAt = timestamp()

    if (
      order.members
        .filter((item) => item.joinStatus === 'ACTIVE')
        .every((item) => item.receiveStatus === 'RECEIVED' || item.receiveStatus === 'AUTO_RECEIVED')
    ) {
      order.status = 'COMPLETED'
    }
  })

  return {
    confirmed: true,
    orderId: Number(orderId),
  }
}
