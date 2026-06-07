import request from '../../request'

const normalizeRole = (role) => (role === 'CREATOR' ? 'INITIATOR' : role)

export const verifyStudent = (payload) => request.post('/api/users/verify-student', payload)

export const getUserProfile = () => request.get('/api/users/profile')

export const updateUserProfile = (payload) => request.put('/api/users/profile', payload)

export const getUserCredit = (params) => request.get('/api/users/credit', { params })

export const getUserCapitalRecords = (params) => request.get('/api/users/capital-records', { params })

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
