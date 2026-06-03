import { defineStore } from 'pinia'

import { createComplaint, getComplaintDetail, getMyComplaints } from '../api/complaint'
import { defaultPageData, loadNormalizedPage } from '../utils/page'

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
    async loadMyComplaints(params = this.myComplaintsFilters) {
      this.myComplaintsLoading = true

      try {
        this.myComplaintsFilters = {
          ...this.myComplaintsFilters,
          ...params,
        }
        const { filters: resolvedFilters, pageData } = await loadNormalizedPage(
          getMyComplaints,
          this.myComplaintsFilters,
        )
        this.myComplaintsFilters = resolvedFilters
        this.myComplaintsPage = pageData
        return this.myComplaintsPage
      } finally {
        this.myComplaintsLoading = false
      }
    },
    async loadComplaintDetail(complaintId) {
      this.complaintDetailLoading = true
      this.complaintDetail = null

      try {
        this.complaintDetail = await getComplaintDetail(complaintId)
        return this.complaintDetail
      } finally {
        this.complaintDetailLoading = false
      }
    },
    async submitComplaint(payload) {
      if (this.submitting) {
        return null
      }

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
