import { defineStore } from 'pinia'

import {
  banUser,
  cancelAdminOrder,
  getAdminComplaintDetail,
  getAdminComplaints,
  getAdminOrderDetail,
  getAdminOrders,
  getAdminUserDetail,
  getAdminUsers,
  getCapitalRecords,
  getDashboardOverview,
  getOperationLogs,
  handleAdminComplaint,
  unbanUser,
} from '../api/admin'
import { defaultPageData, loadNormalizedPage } from '../utils/page'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    complaintDetail: null,
    complaintDetailLoading: false,
    complaintsFilters: {
      page: 1,
      pageSize: 10,
    },
    complaintsLoading: false,
    complaintsPage: defaultPageData(),
    dashboardLoading: false,
    dashboardOverview: {
      recentComplaints: [],
      recentLogs: [],
      recentOrders: [],
    },
    logsFilters: {
      action: '',
      bizType: '',
      keyword: '',
      operatorType: '',
      page: 1,
      pageSize: 10,
    },
    logsLoading: false,
    logsPage: defaultPageData(),
    metrics: {
      complaints: 0,
      orders: 0,
      users: 0,
    },
    orderDetail: null,
    orderDetailLoading: false,
    ordersFilters: {
      page: 1,
      pageSize: 10,
      status: '',
    },
    ordersLoading: false,
    ordersPage: defaultPageData(),
    recordsFilters: {
      keyword: '',
      page: 1,
      pageSize: 10,
      status: '',
      type: '',
    },
    recordsLoading: false,
    recordsPage: defaultPageData(),
    submitting: false,
    userDetail: null,
    userDetailLoading: false,
    usersFilters: {
      keyword: '',
      page: 1,
      pageSize: 10,
      status: '',
    },
    usersLoading: false,
    usersPage: defaultPageData(),
  }),
  actions: {
    async loadUsers(params = this.usersFilters) {
      this.usersLoading = true

      try {
        this.usersFilters = {
          ...this.usersFilters,
          ...params,
        }
        const { filters: resolvedFilters, pageData } = await loadNormalizedPage(getAdminUsers, this.usersFilters)
        this.usersFilters = resolvedFilters
        this.usersPage = pageData
        return this.usersPage
      } finally {
        this.usersLoading = false
      }
    },
    async loadUserDetail(userId) {
      this.userDetailLoading = true
      this.userDetail = null

      try {
        this.userDetail = await getAdminUserDetail(userId)
        return this.userDetail
      } finally {
        this.userDetailLoading = false
      }
    },
    async toggleUserStatus(row, payload = { reason: '管理员封禁用户' }) {
      if (this.submitting) {
        return this.usersPage
      }

      this.submitting = true

      try {
        if (row.status === 'NORMAL') {
          await banUser(row.userId, payload)
        } else {
          await unbanUser(row.userId)
        }

        const refreshTasks = [
          this.loadUsers(this.usersFilters),
          this.loadDashboardOverview(),
        ]

        if (this.userDetail?.userId === Number(row.userId)) {
          refreshTasks.push(this.loadUserDetail(row.userId))
        }

        await Promise.allSettled(refreshTasks)
        return this.usersPage
      } finally {
        this.submitting = false
      }
    },
    async loadOrders(params = this.ordersFilters) {
      this.ordersLoading = true

      try {
        this.ordersFilters = {
          ...this.ordersFilters,
          ...params,
        }
        const { filters: resolvedFilters, pageData } = await loadNormalizedPage(getAdminOrders, this.ordersFilters)
        this.ordersFilters = resolvedFilters
        this.ordersPage = pageData
        return this.ordersPage
      } finally {
        this.ordersLoading = false
      }
    },
    async loadOrderDetail(orderId) {
      this.orderDetailLoading = true
      this.orderDetail = null

      try {
        this.orderDetail = await getAdminOrderDetail(orderId)
        return this.orderDetail
      } finally {
        this.orderDetailLoading = false
      }
    },
    async cancelOrder(orderId, payload = { reason: '管理员取消订单' }) {
      if (this.submitting) {
        return this.ordersPage
      }

      this.submitting = true

      try {
        await cancelAdminOrder(orderId, payload)

        const refreshTasks = [
          this.loadOrders(this.ordersFilters),
          this.loadDashboardOverview(),
        ]

        if (this.orderDetail?.basicInfo?.orderId === Number(orderId)) {
          refreshTasks.push(this.loadOrderDetail(orderId))
        }

        await Promise.allSettled(refreshTasks)
        return this.ordersPage
      } finally {
        this.submitting = false
      }
    },
    async loadComplaints(params = this.complaintsFilters) {
      this.complaintsLoading = true

      try {
        this.complaintsFilters = {
          ...this.complaintsFilters,
          ...params,
        }
        const { filters: resolvedFilters, pageData } = await loadNormalizedPage(
          getAdminComplaints,
          this.complaintsFilters,
        )
        this.complaintsFilters = resolvedFilters
        this.complaintsPage = pageData
        return this.complaintsPage
      } finally {
        this.complaintsLoading = false
      }
    },
    async loadComplaintDetail(complaintId) {
      this.complaintDetailLoading = true
      this.complaintDetail = null

      try {
        this.complaintDetail = await getAdminComplaintDetail(complaintId)
        return this.complaintDetail
      } finally {
        this.complaintDetailLoading = false
      }
    },
    async processComplaint(complaintId, payload = { handleResult: '管理员已介入处理' }) {
      if (this.submitting) {
        return this.complaintsPage
      }

      this.submitting = true

      try {
        await handleAdminComplaint(complaintId, payload)

        const refreshTasks = [
          this.loadComplaints(this.complaintsFilters),
          this.loadOrders(this.ordersFilters),
          this.loadDashboardOverview(),
        ]

        if (this.complaintDetail?.complaintId === Number(complaintId)) {
          refreshTasks.push(this.loadComplaintDetail(complaintId))
        }

        await Promise.allSettled(refreshTasks)
        return this.complaintsPage
      } finally {
        this.submitting = false
      }
    },
    async loadCapitalRecords(params = this.recordsFilters) {
      this.recordsLoading = true

      try {
        this.recordsFilters = {
          ...this.recordsFilters,
          ...params,
        }
        const { filters: resolvedFilters, pageData } = await loadNormalizedPage(getCapitalRecords, this.recordsFilters)
        this.recordsFilters = resolvedFilters
        this.recordsPage = pageData
        return this.recordsPage
      } finally {
        this.recordsLoading = false
      }
    },
    async loadOperationLogs(params = this.logsFilters) {
      this.logsLoading = true

      try {
        this.logsFilters = {
          ...this.logsFilters,
          ...params,
        }
        const { filters: resolvedFilters, pageData } = await loadNormalizedPage(getOperationLogs, this.logsFilters)
        this.logsFilters = resolvedFilters
        this.logsPage = pageData
        return this.logsPage
      } finally {
        this.logsLoading = false
      }
    },
    async loadDashboardOverview() {
      this.dashboardLoading = true

      try {
        const overview = await getDashboardOverview()

        this.metrics = overview.metrics || this.metrics
        this.dashboardOverview = {
          recentComplaints: overview.recentComplaints || [],
          recentLogs: overview.recentLogs || [],
          recentOrders: overview.recentOrders || [],
        }

        return {
          metrics: this.metrics,
          overview: this.dashboardOverview,
        }
      } finally {
        this.dashboardLoading = false
      }
    },
  },
})
