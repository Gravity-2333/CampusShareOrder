import { defineStore } from 'pinia'

import { createComplaint, getMyComplaints } from '../api/complaint'

const defaultPageData = () => ({
  list: [],
  page: 1,
  pageSize: 10,
  pages: 0,
  total: 0,
})

export const useComplaintStore = defineStore('complaint', {
  state: () => ({
    myComplaintsLoading: false,
    myComplaintsPage: defaultPageData(),
    submitting: false,
  }),
  actions: {
    async loadMyComplaints(params = { page: 1, pageSize: 10 }) {
      this.myComplaintsLoading = true

      try {
        this.myComplaintsPage = await getMyComplaints(params)
        return this.myComplaintsPage
      } finally {
        this.myComplaintsLoading = false
      }
    },
    async submitComplaint(payload) {
      this.submitting = true

      try {
        return await createComplaint(payload)
      } finally {
        this.submitting = false
      }
    },
  },
})
