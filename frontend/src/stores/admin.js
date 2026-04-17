import { defineStore } from 'pinia'

import {
  banUser,
  cancelAdminOrder,
  getAdminComplaints,
  getAdminOrders,
  getAdminUsers,
  getCapitalRecords,
  getOperationLogs,
  handleAdminComplaint,
  unbanUser,
} from '../api/admin'

const defaultPageData = () => ({
  list: [],
  page: 1,
  pageSize: 10,
  pages: 0,
  total: 0,
})

export const useAdminStore = defineStore('admin', {
  state: () => ({
    complaintsLoading: false,
    complaintsPage: defaultPageData(),
    logsLoading: false,
    logsPage: defaultPageData(),
    metrics: {
      complaints: 0,
      orders: 0,
      users: 0,
    },
    ordersLoading: false,
    ordersPage: defaultPageData(),
    recordsLoading: false,
    recordsPage: defaultPageData(),
    submitting: false,
    usersLoading: false,
    usersPage: defaultPageData(),
  }),
  actions: {
    async loadUsers(params = { page: 1, pageSize: 10 }) {
      this.usersLoading = true

      try {
        this.usersPage = await getAdminUsers(params)
        return this.usersPage
      } finally {
        this.usersLoading = false
      }
    },
    async toggleUserStatus(row) {
      this.submitting = true

      try {
        if (row.status === 'NORMAL') {
          await banUser(row.userId, { reason: 'mock ban' })
        } else {
          await unbanUser(row.userId)
        }

        return await this.loadUsers({
          page: this.usersPage.page || 1,
          pageSize: this.usersPage.pageSize || 10,
        })
      } finally {
        this.submitting = false
      }
    },
    async loadOrders(params = { page: 1, pageSize: 10 }) {
      this.ordersLoading = true

      try {
        this.ordersPage = await getAdminOrders(params)
        return this.ordersPage
      } finally {
        this.ordersLoading = false
      }
    },
    async cancelOrder(orderId) {
      this.submitting = true

      try {
        await cancelAdminOrder(orderId, { reason: 'mock cancel' })
        return await this.loadOrders({
          page: this.ordersPage.page || 1,
          pageSize: this.ordersPage.pageSize || 10,
        })
      } finally {
        this.submitting = false
      }
    },
    async loadComplaints(params = { page: 1, pageSize: 10 }) {
      this.complaintsLoading = true

      try {
        this.complaintsPage = await getAdminComplaints(params)
        return this.complaintsPage
      } finally {
        this.complaintsLoading = false
      }
    },
    async processComplaint(complaintId) {
      this.submitting = true

      try {
        await handleAdminComplaint(complaintId, { handleResult: '管理员已介入处理' })
        return await this.loadComplaints({
          page: this.complaintsPage.page || 1,
          pageSize: this.complaintsPage.pageSize || 10,
        })
      } finally {
        this.submitting = false
      }
    },
    async loadCapitalRecords(params = { page: 1, pageSize: 10 }) {
      this.recordsLoading = true

      try {
        this.recordsPage = await getCapitalRecords(params)
        return this.recordsPage
      } finally {
        this.recordsLoading = false
      }
    },
    async loadOperationLogs(params = { page: 1, pageSize: 10 }) {
      this.logsLoading = true

      try {
        this.logsPage = await getOperationLogs(params)
        return this.logsPage
      } finally {
        this.logsLoading = false
      }
    },
    async loadDashboardMetrics() {
      const [users, orders, complaints] = await Promise.all([
        getAdminUsers({ page: 1, pageSize: 100 }),
        getAdminOrders({ page: 1, pageSize: 100 }),
        getAdminComplaints({ page: 1, pageSize: 100 }),
      ])

      this.metrics = {
        complaints: complaints.total,
        orders: orders.total,
        users: users.total,
      }

      return this.metrics
    },
  },
})
