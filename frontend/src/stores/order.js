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

const defaultPageData = () => ({
  list: [],
  page: 1,
  pageSize: 10,
  pages: 0,
  total: 0,
})

export const useOrderStore = defineStore('order', {
  state: () => ({
    detail: null,
    detailLoading: false,
    hallFilters: {
      keyword: '',
      page: 1,
      pageSize: 10,
      status: '',
    },
    hallLoading: false,
    hallPage: defaultPageData(),
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
        this.hallPage = await getOrderList(this.hallFilters)
        return this.hallPage
      } finally {
        this.hallLoading = false
      }
    },
    async loadMyOrders(params = { page: 1, pageSize: 10 }) {
      this.myOrdersLoading = true

      try {
        this.myOrdersPage = await getMyOrders(params)
        return this.myOrdersPage
      } finally {
        this.myOrdersLoading = false
      }
    },
    async loadOrderDetail(orderId) {
      this.detailLoading = true

      try {
        this.detail = await getOrderDetail(orderId)
        return this.detail
      } finally {
        this.detailLoading = false
      }
    },
    async createNewOrder(payload) {
      this.submitting = true

      try {
        return await createOrder(payload)
      } finally {
        this.submitting = false
      }
    },
    async joinExistingOrder(orderId) {
      this.submitting = true

      try {
        const result = await joinOrder(orderId, {})
        await this.loadHallOrders()
        return result
      } finally {
        this.submitting = false
      }
    },
    async joinOrderFromDetail(orderId) {
      this.submitting = true

      try {
        await joinOrder(orderId, {})
        return await this.loadOrderDetail(orderId)
      } finally {
        this.submitting = false
      }
    },
    async exitExistingOrder(orderId) {
      this.submitting = true

      try {
        await exitOrder(orderId)
        return await this.loadOrderDetail(orderId)
      } finally {
        this.submitting = false
      }
    },
    async runDetailAction(orderId, action, payload = {}) {
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

        return await this.loadOrderDetail(orderId)
      } finally {
        this.submitting = false
      }
    },
  },
})
