import request from '../../request'

const normalizeRole = (role) => (role === 'CREATOR' ? 'INITIATOR' : role)

export const verifyStudent = async (payload) => {
  await request.post('/api/users/verify-student', payload)

  return {
    isVerified: true,
    studentNo: payload.studentNo,
  }
}

export const getUserProfile = () => request.get('/api/users/profile')

export const updateUserProfile = (payload) => request.put('/api/users/profile', payload)

export const getUserCredit = async (params) => {
  const records = await request.get('/api/users/credit', { params })

  return {
    creditScore: Number(records?.[0]?.currentScore || 0),
    records: Array.isArray(records) ? records : [],
  }
}

export const getMyOrders = async (params) => {
  const page = await request.get('/api/users/my-orders', { params })

  return {
    ...page,
    list: Array.isArray(page?.list)
      ? page.list.map((item) => ({
          ...item,
          myRole: normalizeRole(item.myRole),
        }))
      : [],
  }
}
