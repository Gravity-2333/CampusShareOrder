import request from '../../request'

export const createOrder = (payload) => request.post('/api/orders', payload)

export const getOrderList = (params) => request.get('/api/orders', { params })

export const getOrderDetail = (orderId) => request.get(`/api/orders/${orderId}`)

export const joinOrder = (orderId, payload) => request.post(`/api/orders/${orderId}/join`, payload)

export const payOrder = (orderId) => request.post(`/api/orders/${orderId}/pay`)

export const exitOrder = (orderId) => request.post(`/api/orders/${orderId}/exit`)

export const uploadReceipt = (orderId, payload) => request.post(`/api/orders/${orderId}/receipt`, payload)

export const markDelivered = (orderId) => request.post(`/api/orders/${orderId}/deliver`)

export const confirmReceived = (orderId) => request.post(`/api/orders/${orderId}/receive`)
