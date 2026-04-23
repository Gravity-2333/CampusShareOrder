import { getDatabase, mutateDatabase, pageResult } from './database'
import { makeFailure } from './database'
import { requireUser, sleep } from './shared'

export const verifyStudent = async (payload) => {
  const user = await requireUser()

  if (!payload.studentNo) {
    makeFailure(42201, '学号不能为空')
  }

  mutateDatabase((draft) => {
    const target = draft.users.find((item) => item.userId === user.userId)
    target.isVerified = true
    target.studentNo = payload.studentNo
  })

  return {
    isVerified: true,
    studentNo: payload.studentNo,
  }
}

export const getUserProfile = async () => {
  const user = await requireUser()

  return {
    contactInfo: user.contactInfo,
    createdAt: user.createdAt,
    creditScore: user.creditScore,
    isVerified: user.isVerified,
    nickname: user.nickname,
    phone: user.phone,
    status: user.status,
    studentNo: user.studentNo,
    userId: user.userId,
  }
}

export const updateUserProfile = async (payload) => {
  const user = await requireUser()

  mutateDatabase((draft) => {
    const target = draft.users.find((item) => item.userId === user.userId)
    target.contactInfo = payload.contactInfo
    target.nickname = payload.nickname
  })

  return {
    contactInfo: payload.contactInfo,
    nickname: payload.nickname,
    updated: true,
  }
}

export const getUserCredit = async () => {
  const user = await requireUser()

  return {
    creditScore: user.creditScore,
    records: user.creditRecords,
  }
}

export const getMyOrders = async (params = {}) => {
  const user = await requireUser()
  await sleep()

  const database = getDatabase()
  const list = database.orders
    .filter((order) => order.members.some((member) => member.userId === user.userId))
    .map((order) => {
      const current = order.members.find((member) => member.userId === user.userId)

      return {
        myJoinStatus: current?.joinStatus || null,
        myPayStatus: current?.payStatus || null,
        myReceiveStatus: current?.receiveStatus || null,
        myRole: current?.role || 'VISITOR',
        orderId: order.orderId,
        orderNo: order.orderNo,
        pickupPoint: order.pickupPoint,
        productName: order.productName,
        refundAmountTotal: current?.refundAmountTotal || 0,
        status: order.status,
      }
    })

  return pageResult(list, Number(params.page || 1), Number(params.pageSize || 10))
}
