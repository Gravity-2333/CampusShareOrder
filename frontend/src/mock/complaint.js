import { getDatabase, mutateDatabase, pageResult, timestamp } from './database'
import { requireUser, sleep } from './shared'
import { makeFailure } from './database'

export const createComplaint = async (payload) => {
  const user = await requireUser()

  const database = getDatabase()
  const duplicate = database.complaints.find(
    (item) => item.orderId === Number(payload.orderId) && item.complainantUserId === user.userId,
  )

  if (duplicate) {
    makeFailure(40913, '请勿重复投诉')
  }

  let createdComplaint = null

  mutateDatabase((draft) => {
    const order = draft.orders.find((item) => item.orderId === Number(payload.orderId))

    if (!order) {
      makeFailure(40401, '订单不存在')
    }

    const complaintId = Math.max(0, ...draft.complaints.map((item) => item.complaintId)) + 1
    const accusedMember = order.members.find((item) => item.role === 'INITIATOR')
    createdComplaint = {
      accusedNickname: accusedMember?.nickname || order.initiatorNickname,
      accusedUserId: order.creatorUserId,
      complaintId,
      complaintNo: `CP${Date.now()}`,
      complainantUserId: user.userId,
      content: payload.content,
      createdAt: timestamp(),
      handleResult: '',
      handledAt: '',
      openedBySystem: order.complaintOpened,
      orderId: Number(payload.orderId),
      orderNo: order.orderNo,
      productName: order.productName,
      status: 'PENDING',
      type: payload.type,
    }
    draft.complaints.unshift(createdComplaint)
  })

  return {
    complaintId: createdComplaint.complaintId,
    complaintNo: createdComplaint.complaintNo,
    status: createdComplaint.status,
  }
}

export const getMyComplaints = async (params = {}) => {
  const user = await requireUser()
  await sleep()

  const database = getDatabase()
  const list = database.complaints.filter((item) => item.complainantUserId === user.userId)

  return pageResult(list, Number(params.page || 1), Number(params.pageSize || 10))
}

export const getComplaintDetail = async (complaintId) => {
  const user = await requireUser()
  await sleep()

  const complaint = getDatabase().complaints.find((item) => item.complaintId === Number(complaintId))

  if (!complaint) {
    makeFailure(40402, '投诉不存在')
  }

  if (complaint.complainantUserId !== user.userId) {
    makeFailure(40301, '无权查看该投诉')
  }

  return complaint
}
