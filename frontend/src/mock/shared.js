import { getAccessToken } from '../utils/auth'
import { findUserByToken, getDatabase, makeFailure, timestamp } from './database'

export const sleep = (ms = 180) => new Promise((resolve) => setTimeout(resolve, ms))

export const requireLogin = async () => {
  await sleep()

  const token = getAccessToken()

  if (!token) {
    makeFailure(40101, '请先登录')
  }

  const account = findUserByToken(token)

  if (!account) {
    makeFailure(40102, '登录状态已失效')
  }

  return account
}

export const requireUser = async () => {
  const account = await requireLogin()

  if (!('userId' in account)) {
    makeFailure(40301, '无权访问用户接口')
  }

  if (account.status === 'BANNED') {
    makeFailure(40302, '当前账号已被封禁')
  }

  return account
}

export const requireAdmin = async () => {
  const account = await requireLogin()

  if (!('adminId' in account)) {
    makeFailure(40301, '无权访问管理端接口')
  }

  return account
}

export const maskPhone = (value) =>
  value ? `${value.slice(0, 3)}****${value.slice(-4)}` : ''

export const maskStudentNo = (value) =>
  value ? `${value.slice(0, 2)}****${value.slice(-2)}` : ''

export const buildOrderDetail = (orderId, viewerId, isAdmin = false) => {
  const database = getDatabase()
  const order = database.orders.find((item) => item.orderId === Number(orderId))

  if (!order) {
    makeFailure(40401, '订单不存在')
  }

  const initiator = database.users.find((item) => item.userId === order.creatorUserId)
  const currentUserMember = order.members.find((item) => item.userId === viewerId) || null
  const myComplaint =
    database.complaints.find(
      (item) => item.orderId === order.orderId && item.complainantUserId === viewerId,
    ) || null
  const hasActiveSlot = order.currentMemberCount < order.totalMemberCount
  const canJoin =
    !isAdmin &&
    order.status === 'OPEN' &&
    !currentUserMember &&
    hasActiveSlot &&
    Boolean(viewerId)
  const canPay =
    !isAdmin &&
    order.status === 'OPEN' &&
    currentUserMember?.joinStatus === 'ACTIVE' &&
    currentUserMember?.payStatus === 'UNPAID'
  const canExit =
    !isAdmin &&
    order.status === 'OPEN' &&
    currentUserMember?.role === 'MEMBER' &&
    currentUserMember?.joinStatus === 'ACTIVE'
  const canUploadReceipt = !isAdmin && order.status === 'GROUPED' && order.creatorUserId === viewerId
  const canMarkDelivered =
    !isAdmin && order.status === 'WAIT_DELIVERY' && order.creatorUserId === viewerId
  const canConfirmReceived =
    !isAdmin &&
    order.status === 'WAIT_RECEIVE' &&
    currentUserMember?.receiveStatus === 'WAIT_CONFIRM'
  const canCreateComplaint =
    !isAdmin &&
    order.complaintOpened &&
    order.creatorUserId !== viewerId &&
    !database.complaints.find(
      (item) => item.orderId === order.orderId && item.complainantUserId === viewerId,
    )

  return {
    actionFlags: {
      canConfirmReceived,
      canCreateComplaint,
      canExit,
      canJoin,
      canMarkDelivered,
      canPay,
      canUploadReceipt,
      canViewReceipt: Boolean(order.actualTotalAmount),
    },
    basicInfo: {
      complaintOpened: order.complaintOpened,
      currentMemberCount: order.currentMemberCount,
      deadlineAt: order.deadlineAt,
      estimatedTotalAmount: order.estimatedTotalAmount,
      expectedDeliveryEndAt: order.expectedDeliveryEndAt,
      orderId: order.orderId,
      orderNo: order.orderNo,
      pickupPoint: order.pickupPoint,
      productDesc: order.productDesc,
      productName: order.productName,
      receiptUploadDeadlineAt: order.receiptUploadDeadlineAt,
      status: order.status,
      totalMemberCount: order.totalMemberCount,
    },
    complaintInfo: {
      complaintCount: database.complaints.filter((item) => item.orderId === order.orderId).length,
      complaintOpened: order.complaintOpened,
      myComplaintId: myComplaint?.complaintId || null,
      myComplaintStatus: myComplaint?.status || null,
    },
    currentUserMember: currentUserMember
      ? {
          joinStatus: currentUserMember.joinStatus,
          memberId: currentUserMember.memberId,
          myRole: currentUserMember.role,
          payStatus: currentUserMember.payStatus,
          receiveStatus: currentUserMember.receiveStatus,
          refundAmountTotal: currentUserMember.refundAmountTotal,
        }
      : null,
    initiatorInfo: {
      nickname: initiator?.nickname || order.initiatorNickname,
      phoneMasked: maskPhone(initiator?.phone),
      studentNoMasked: maskStudentNo(initiator?.studentNo),
      userId: order.creatorUserId,
    },
    memberList: order.members.map((member) => ({
      joinStatus: member.joinStatus,
      nickname: member.nickname,
      payAmount: member.payAmount,
      payStatus: member.payStatus,
      receiveStatus: member.receiveStatus,
      refundAmountTotal: member.refundAmountTotal,
      role: member.role,
      userId: member.userId,
    })),
    paymentSummary: {
      actualTotalAmount: order.actualTotalAmount,
      estimatedTotalAmount: order.estimatedTotalAmount,
      paidMemberCount: order.members.filter((item) => item.payStatus === 'PAID').length,
      refundAmountTotal: order.members.reduce((sum, item) => sum + item.refundAmountTotal, 0),
      totalPaidAmount: order.members.reduce(
        (sum, item) => sum + (item.payStatus === 'PAID' ? item.payAmount : 0),
        0,
      ),
    },
    receiptInfo: order.actualTotalAmount
      ? {
          actualTotalAmount: order.actualTotalAmount,
          receiptImageUrl: 'https://dummyimage.com/640x480/f3f1ea/2f3c32&text=Order+Receipt',
          uploadedAt: timestamp(),
        }
      : null,
    receiveInfo: {
      autoConfirmDeadlineAt: order.deliveredAt,
      deliveredAt: order.deliveredAt,
      receiveStatusSummary: order.members.map((item) => ({
        nickname: item.nickname,
        receiveStatus: item.receiveStatus,
      })),
    },
    timeline: [
      {
        action: 'ORDER_CREATED',
        at: order.members[0]?.joinedAt || order.deadlineAt,
        description: `拼单 ${order.orderNo} 已创建`,
      },
      {
        action: 'ORDER_STATUS',
        at: timestamp(),
        description: `当前订单状态为 ${order.status}`,
      },
    ],
    viewerRoleInOrder: isAdmin ? 'ADMIN' : currentUserMember?.role || 'VISITOR',
  }
}
