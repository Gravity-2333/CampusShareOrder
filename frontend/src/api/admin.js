import provider from './provider'

export const getAdminUsers = (params) => provider.admin.getUsers(params)

export const banUser = (userId, payload) => provider.admin.banUser(userId, payload)

export const unbanUser = (userId) => provider.admin.unbanUser(userId)

export const getAdminOrders = (params) => provider.admin.getOrders(params)

export const getAdminOrderDetail = (orderId) => provider.admin.getOrderDetail(orderId)

export const cancelAdminOrder = (orderId, payload) => provider.admin.cancelOrder(orderId, payload)

export const getAdminComplaints = (params) => provider.admin.getComplaints(params)

export const getAdminComplaintDetail = (complaintId) => provider.admin.getComplaintDetail(complaintId)

export const handleAdminComplaint = (complaintId, payload) =>
  provider.admin.handleComplaint(complaintId, payload)

export const getCapitalRecords = (params) => provider.admin.getCapitalRecords(params)

export const getOperationLogs = (params) => provider.admin.getOperationLogs(params)
