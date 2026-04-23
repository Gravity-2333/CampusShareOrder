import provider from './provider'
import { normalizeOrderDetail } from './adapters/order'

export const createOrder = (payload) => provider.order.createOrder(payload)

export const getOrderList = (params) => provider.order.getOrderList(params)

export const getOrderDetail = async (orderId) => normalizeOrderDetail(await provider.order.getOrderDetail(orderId))

export const joinOrder = (orderId, payload) => provider.order.joinOrder(orderId, payload)

export const payOrder = (orderId) => provider.order.payOrder(orderId)

export const exitOrder = (orderId) => provider.order.exitOrder(orderId)

export const uploadReceipt = (orderId, payload) => provider.order.uploadReceipt(orderId, payload)

export const markDelivered = (orderId) => provider.order.markDelivered(orderId)

export const confirmReceived = (orderId) => provider.order.confirmReceived(orderId)
