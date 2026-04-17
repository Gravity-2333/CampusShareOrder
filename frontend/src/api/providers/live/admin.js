import request from '../../request'

export const getUsers = (params) => request.get('/api/admin/users', { params })

export const banUser = (userId, payload) => request.post(`/api/admin/users/${userId}/ban`, payload)

export const unbanUser = (userId) => request.post(`/api/admin/users/${userId}/unban`)

export const getOrders = (params) => request.get('/api/admin/orders', { params })

export const getOrderDetail = (orderId) => request.get(`/api/admin/orders/${orderId}`)

export const cancelOrder = (orderId, payload) =>
  request.post(`/api/admin/orders/${orderId}/cancel`, payload)

export const getComplaints = (params) => request.get('/api/admin/complaints', { params })

export const getComplaintDetail = (complaintId) =>
  request.get(`/api/admin/complaints/${complaintId}`)

export const handleComplaint = (complaintId, payload) =>
  request.post(`/api/admin/complaints/${complaintId}/handle`, payload)

export const getCapitalRecords = (params) => request.get('/api/admin/records/capital', { params })

export const getOperationLogs = (params) => request.get('/api/admin/records/logs', { params })
