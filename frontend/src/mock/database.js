const STORAGE_KEY = 'campus-share-order-mock-db'

const now = () => new Date().toISOString().slice(0, 19).replace('T', ' ')

const createPageResult = (list, page = 1, pageSize = 10) => {
  const start = (page - 1) * pageSize
  const chunk = list.slice(start, start + pageSize)

  return {
    list: chunk,
    page,
    pageSize,
    total: list.length,
    pages: list.length ? Math.ceil(list.length / pageSize) : 0,
  }
}

const createDefaultDatabase = () => ({
  admins: [
    {
      adminId: 1,
      username: 'admin',
      password: 'admin123',
      role: 'ADMIN',
      status: 'NORMAL',
    },
  ],
  complaints: [
    {
      accusedNickname: '李四',
      accusedUserId: 2,
      complaintId: 1,
      complaintNo: 'CP20260417001',
      content: '发起人超过预计时间仍未上传购买凭证。',
      createdAt: '2026-04-17 11:20:00',
      handleResult: '',
      handledAt: '',
      openedBySystem: true,
      orderId: 2,
      orderNo: 'GO20260417002',
      productName: '奶茶拼单',
      status: 'PENDING',
      type: 'NOT_PURCHASED',
      complainantUserId: 3,
    },
  ],
  logs: [
    {
      action: 'ORDER_CREATED',
      createdAt: '2026-04-17 08:30:00',
      logId: 1,
      operatorName: '张三',
      targetNo: 'GO20260417001',
    },
    {
      action: 'COMPLAINT_CREATED',
      createdAt: '2026-04-17 11:20:00',
      logId: 2,
      operatorName: '王五',
      targetNo: 'CP20260417001',
    },
  ],
  orders: [
    {
      actualTotalAmount: null,
      complaintOpened: false,
      creatorUserId: 1,
      currentMemberCount: 2,
      deadlineAt: '2026-04-17 21:00:00',
      deliveredAt: null,
      estimatedTotalAmount: 48,
      expectedDeliveryEndAt: '2026-04-17 22:00:00',
      initiatorNickname: '张三',
      totalMemberCount: 3,
      members: [
        {
          joinedAt: '2026-04-17 08:30:00',
          joinStatus: 'ACTIVE',
          memberId: 1,
          nickname: '张三',
          payAmount: 16,
          paidAt: '2026-04-17 08:35:00',
          payStatus: 'PAID',
          receivedAt: null,
          receiveStatus: 'NOT_READY',
          refundAmountTotal: 0,
          role: 'INITIATOR',
          userId: 1,
        },
        {
          exitedAt: '2026-04-17 08:58:00',
          joinedAt: '2026-04-17 08:34:00',
          joinStatus: 'EXITED',
          memberId: 6,
          nickname: '王五',
          payAmount: 16,
          paidAt: null,
          payStatus: 'UNPAID',
          receivedAt: null,
          receiveStatus: 'NOT_READY',
          refundAmountTotal: 0,
          role: 'MEMBER',
          userId: 3,
        },
        {
          joinedAt: '2026-04-17 08:41:00',
          joinStatus: 'ACTIVE',
          memberId: 2,
          nickname: '李四',
          payAmount: 16,
          paidAt: null,
          payStatus: 'UNPAID',
          receivedAt: null,
          receiveStatus: 'NOT_READY',
          refundAmountTotal: 0,
          role: 'MEMBER',
          userId: 2,
        },
      ],
      orderId: 1,
      orderNo: 'GO20260417001',
      pickupPoint: '东区宿舍门口',
      productDesc: '三个人一起点晚饭，均摊配送费。',
      productName: '晚饭拼单',
      receiptUploadedAt: null,
      receiptUploadDeadlineAt: null,
      status: 'OPEN',
    },
    {
      actualTotalAmount: 54,
      complaintOpened: true,
      creatorUserId: 2,
      currentMemberCount: 3,
      deadlineAt: '2026-04-17 10:00:00',
      deliveredAt: null,
      estimatedTotalAmount: 60,
      expectedDeliveryEndAt: '2026-04-17 11:00:00',
      initiatorNickname: '李四',
      totalMemberCount: 3,
      members: [
        {
          joinedAt: '2026-04-17 09:00:00',
          joinStatus: 'ACTIVE',
          memberId: 3,
          nickname: '李四',
          payAmount: 20,
          paidAt: '2026-04-17 09:01:00',
          payStatus: 'PAID',
          receivedAt: null,
          receiveStatus: 'NOT_READY',
          refundAmountTotal: 2,
          role: 'INITIATOR',
          userId: 2,
        },
        {
          joinedAt: '2026-04-17 09:03:00',
          joinStatus: 'ACTIVE',
          memberId: 4,
          nickname: '张三',
          payAmount: 20,
          paidAt: '2026-04-17 09:05:00',
          payStatus: 'PAID',
          receivedAt: null,
          receiveStatus: 'NOT_READY',
          refundAmountTotal: 2,
          role: 'MEMBER',
          userId: 1,
        },
        {
          joinedAt: '2026-04-17 09:08:00',
          joinStatus: 'ACTIVE',
          memberId: 5,
          nickname: '王五',
          payAmount: 20,
          paidAt: '2026-04-17 09:10:00',
          payStatus: 'PAID',
          receivedAt: null,
          receiveStatus: 'NOT_READY',
          refundAmountTotal: 2,
          role: 'MEMBER',
          userId: 3,
        },
      ],
      orderId: 2,
      orderNo: 'GO20260417002',
      pickupPoint: '图书馆南门',
      productDesc: '奶茶加小料拼单。',
      productName: '奶茶拼单',
      receiptUploadedAt: '2026-04-17 10:06:00',
      receiptUploadDeadlineAt: '2026-04-17 10:30:00',
      status: 'WAIT_DELIVERY',
    },
  ],
  records: [
    {
      amount: 16,
      bizNo: 'PAY20260417001',
      createdAt: '2026-04-17 08:35:00',
      recordId: 1,
      type: 'PAY',
      userNickname: '张三',
    },
    {
      amount: 2,
      bizNo: 'REFUND20260417002',
      createdAt: '2026-04-17 10:06:00',
      recordId: 2,
      type: 'REFUND_DIFF',
      userNickname: '张三',
    },
  ],
  tokens: {},
  users: [
    {
      contactInfo: '微信 zhangsan_01',
      createdAt: '2026-04-16 09:00:00',
      creditRecords: [
        {
          changeReason: '初始信用分',
          createdAt: '2026-04-16 09:00:00',
          delta: 100,
        },
      ],
      creditScore: 100,
      isVerified: true,
      nickname: '张三',
      password: '123456',
      phone: '13800000001',
      role: 'USER',
      status: 'NORMAL',
      studentNo: '20240001',
      userId: 1,
    },
    {
      contactInfo: '微信 lisi_02',
      createdAt: '2026-04-16 09:10:00',
      creditRecords: [
        {
          changeReason: '初始信用分',
          createdAt: '2026-04-16 09:10:00',
          delta: 96,
        },
      ],
      creditScore: 96,
      isVerified: true,
      nickname: '李四',
      password: '123456',
      phone: '13800000002',
      role: 'USER',
      status: 'NORMAL',
      studentNo: '20240002',
      userId: 2,
    },
    {
      contactInfo: '微信 wangwu_03',
      createdAt: '2026-04-16 09:20:00',
      creditRecords: [
        {
          changeReason: '初始信用分',
          createdAt: '2026-04-16 09:20:00',
          delta: 88,
        },
      ],
      creditScore: 88,
      isVerified: false,
      nickname: '王五',
      password: '123456',
      phone: '13800000003',
      role: 'USER',
      status: 'NORMAL',
      studentNo: '',
      userId: 3,
    },
  ],
})

const readStorage = () => {
  const raw = localStorage.getItem(STORAGE_KEY)

  if (!raw) {
    return createDefaultDatabase()
  }

  try {
    return JSON.parse(raw)
  } catch {
    return createDefaultDatabase()
  }
}

let database = readStorage()

const persist = () => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(database))
}

export const getDatabase = () => database

export const mutateDatabase = (updater) => {
  updater(database)
  persist()
}

export const resetDatabase = () => {
  database = createDefaultDatabase()
  persist()
}

export const makeSuccess = (data) => ({ code: 0, message: 'success', data })

export const makeFailure = (code, message) => {
  const error = new Error(message)
  error.code = code
  throw error
}

export const findUserByToken = (token) => {
  const mapping = database.tokens[token]

  if (!mapping) {
    return null
  }

  if (mapping.role === 'ADMIN') {
    return database.admins.find((item) => item.adminId === mapping.id) || null
  }

  return database.users.find((item) => item.userId === mapping.id) || null
}

export const issueToken = (role, id) => {
  const token = `${role.toLowerCase()}-${id}-${Date.now()}`
  database.tokens[token] = { id, role }
  persist()
  return token
}

export const revokeToken = (token) => {
  if (!token) {
    return
  }

  mutateDatabase((draft) => {
    delete draft.tokens[token]
  })
}

export const nextId = (collectionName, idField) => {
  const items = database[collectionName]
  return Math.max(0, ...items.map((item) => item[idField])) + 1
}

export const pageResult = createPageResult
export const timestamp = now
