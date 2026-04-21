import { defineStore } from 'pinia'

import { createComplaint, getComplaintDetail, getMyComplaints } from '../api/complaint'

const defaultPageData = () => ({
  list: [],
  page: 1,
  pageSize: 10,
  pages: 0,
  total: 0,
})

export const useComplaintStore = defineStore('complaint', {
  state: () => ({
    complaintDetail: null,
    complaintDetailLoading: false,
    myComplaintsFilters: {
      page: 1,
      pageSize: 10,
    },
    myComplaintsLoading: false,
    myComplaintsPage: defaultPageData(),
    submitting: false,
  }),
  actions: {
    async loadMyComplaints(params = { page: 1, pageSize: 10 }) {
      this.myComplaintsLoading = true

      try {
        this.myComplaintsFilters = {
          ...this.myComplaintsFilters,
          ...params,
        }
        this.myComplaintsPage = await getMyComplaints(this.myComplaintsFilters)
        return this.myComplaintsPage
      } finally {
        this.myComplaintsLoading = false
      }
    },
    async loadComplaintDetail(complaintId) {
      this.complaintDetailLoading = true

      try {
        this.complaintDetail = await getComplaintDetail(complaintId)
        return this.complaintDetail
      } finally {
        this.complaintDetailLoading = false
      }
    },
    async submitComplaint(payload) {
      this.submitting = true

      try {
        const result = await createComplaint(payload)
        await this.loadMyComplaints(this.myComplaintsFilters)
        return result
      } finally {
        this.submitting = false
      }
    },
  },
})
