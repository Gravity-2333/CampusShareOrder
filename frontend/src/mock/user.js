import { getDatabase, mutateDatabase, pageResult } from './database'
import { makeFailure } from './database'
import { requireUser, sleep } from './shared'

export const verifyStudent = async (payload) => {
  const user = await requireUser()
  const studentNo = String(payload.studentNo || '').trim()

  if (!studentNo) {
    makeFailure(42201, '学号不能为空')
  }

  if (!/^[A-Za-z0-9]+$/.test(studentNo) || studentNo.length > 10) {
    makeFailure(42201, '学号只能包含字母和数字，且不能超过10位')
  }

  if (user.isVerified) {
    makeFailure(42201, '已完成实名认证，不能重复认证')
  }

  const database = getDatabase()
  const duplicated = database.users.some(
    (item) => item.userId !== user.userId && item.studentNo === studentNo,
  )

  if (duplicated) {
    makeFailure(42201, '该学号已被认证')
  }

  mutateDatabase((draft) => {
    const target = draft.users.find((item) => item.userId === user.userId)
    target.isVerified = true
    target.studentNo = studentNo
  })

  return {
    isVerified: true,
    studentNo,
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

export const getUserCredit = async (params = {}) => {
  const user = await requireUser()
  const records = user.creditRecords.map((record, index) => ({
    currentScore: record.currentScore ?? user.creditScore,
    delta: record.delta,
    reasonType: record.reasonType || 'INITIAL',
    recordId: record.recordId || index + 1,
    relatedComplaintId: record.relatedComplaintId || null,
    relatedOrderId: record.relatedOrderId || null,
    remark: record.remark || record.changeReason,
    changeReason: record.changeReason || record.remark,
    createdAt: record.createdAt,
  }))
  const page = pageResult(records, Number(params.page || 1), Number(params.pageSize || 10))

  return {
    creditScore: user.creditScore,
    page: page.page,
    pageSize: page.pageSize,
    pages: page.pages,
    records: page.list,
    total: page.total,
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
