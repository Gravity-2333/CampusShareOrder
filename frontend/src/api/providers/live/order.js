import request from '../../request'

export const createOrder = (payload) => request.post('/api/orders', payload)

export const getOrderList = (params) => request.get('/api/orders', { params })

export const getOrderDetail = (orderId) => request.get(`/api/orders/${orderId}`)

export const joinOrder = (orderId, payload) => request.post(`/api/orders/${orderId}/join`, payload)

export const payOrder = (orderId) => request.post(`/api/orders/${orderId}/pay`)

export const exitOrder = (orderId) => request.post(`/api/orders/${orderId}/exit`)

export const cancelOrder = (orderId) => request.post(`/api/orders/${orderId}/cancel`)

export const uploadReceipt = (orderId, payload) => {
  const formData = new FormData()
  formData.append('image', payload.image)
  formData.append('actualTotalAmount', payload.actualTotalAmount)
  formData.append('expectedDeliveryStartAt', payload.expectedDeliveryStartAt)
  formData.append('expectedDeliveryEndAt', payload.expectedDeliveryEndAt)
  return request.post(`/api/orders/${orderId}/upload-receipt`, formData)
}

export const markDelivered = (orderId) => request.post(`/api/orders/${orderId}/mark-delivered`)

export const confirmReceived = (orderId) =>
  request.post(`/api/orders/${orderId}/confirm-received`)
