import request from '../../request'

export const verifyStudent = (payload) => request.post('/api/users/verify-student', payload)

export const getUserProfile = () => request.get('/api/users/profile')

export const updateUserProfile = (payload) => request.put('/api/users/profile', payload)

export const getUserCredit = (params) => request.get('/api/users/credit', { params })

export const getMyOrders = (params) => request.get('/api/users/my-orders', { params })
