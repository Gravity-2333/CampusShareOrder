import { defineStore } from 'pinia'

import {
  confirmReceived,
  createOrder,
  exitOrder,
  getOrderDetail,
  getOrderList,
  joinOrder,
  markDelivered,
  payOrder,
  uploadReceipt,
} from '../api/order'
import { getMyOrders } from '../api/user'
import { defaultPageData, loadNormalizedPage } from '../utils/page'

export const useOrderStore = defineStore('order', {
  state: () => ({
    detail: null,
    detailError: '',
    detailLoading: false,
    hallFilters: {
      keyword: '',
      page: 1,
      pageSize: 10,
      status: '',
    },
    hallLoading: false,
    hallPage: defaultPageData(),
    myOrdersFilters: {
      page: 1,
      pageSize: 10,
    },
    myOrdersLoading: false,
    myOrdersPage: defaultPageData(),
    submitting: false,
  }),
  actions: {
    async loadHallOrders(filters = this.hallFilters) {
      this.hallLoading = true

      try {
        this.hallFilters = {
          ...this.hallFilters,
          ...filters,
        }
        const { filters: resolvedFilters, pageData } = await loadNormalizedPage(getOrderList, this.hallFilters)
        this.hallFilters = resolvedFilters
        this.hallPage = pageData
        return this.hallPage
      } finally {
        this.hallLoading = false
      }
    },
    async loadMyOrders(params = this.myOrdersFilters) {
      this.myOrdersLoading = true

      try {
        this.myOrdersFilters = {
          ...this.myOrdersFilters,
          ...params,
        }
        const { filters: resolvedFilters, pageData } = await loadNormalizedPage(getMyOrders, this.myOrdersFilters)
        this.myOrdersFilters = resolvedFilters
        this.myOrdersPage = pageData
        return this.myOrdersPage
      } finally {
        this.myOrdersLoading = false
      }
    },
    async refreshLinkedOrderPages() {
      const refreshTasks = [
        this.loadHallOrders(this.hallFilters),
        this.loadMyOrders(this.myOrdersFilters),
      ]

      await Promise.allSettled(refreshTasks)
    },
    async loadOrderDetail(orderId) {
      this.detailLoading = true
      this.detailError = ''
      this.detail = null

      try {
        this.detail = await getOrderDetail(orderId)
        return this.detail
      } catch (error) {
        this.detailError = error.message || '订单详情加载失败'
        throw error
      } finally {
        this.detailLoading = false
      }
    },
    async createNewOrder(payload) {
      if (this.submitting) {
        return null
      }

      this.submitting = true

      try {
        const result = await createOrder(payload)
        await this.refreshLinkedOrderPages()
        return result
      } finally {
        this.submitting = false
      }
    },
    async joinExistingOrder(orderId) {
      if (this.submitting) {
        return null
      }

      this.submitting = true

      try {
        const result = await joinOrder(orderId, {})
        await this.refreshLinkedOrderPages()
        return result
      } finally {
        this.submitting = false
      }
    },
    async joinOrderFromDetail(orderId) {
      if (this.submitting) {
        return this.detail
      }

      this.submitting = true

      try {
        await joinOrder(orderId, {})
        return await this.loadOrderDetail(orderId)
      } finally {
        this.submitting = false
      }
    },
    async exitExistingOrder(orderId) {
      if (this.submitting) {
        return this.detail
      }

      this.submitting = true

      try {
        await exitOrder(orderId)
        return await this.loadOrderDetail(orderId)
      } finally {
        this.submitting = false
      }
    },
    async runDetailAction(orderId, action, payload = {}) {
      if (this.submitting) {
        return this.detail
      }

      this.submitting = true

      try {
        if (action === 'join') {
          await joinOrder(orderId, {})
        }

        if (action === 'exit') {
          await exitOrder(orderId)
        }

        if (action === 'pay') {
          await payOrder(orderId)
        }

        if (action === 'upload') {
          await uploadReceipt(orderId, payload)
        }

        if (action === 'delivered') {
          await markDelivered(orderId)
        }

        if (action === 'received') {
          await confirmReceived(orderId)
        }

        await this.refreshLinkedOrderPages()
        return await this.loadOrderDetail(orderId)
      } finally {
        this.submitting = false
      }
    },
  },
})
