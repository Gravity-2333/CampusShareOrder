import { defineStore } from 'pinia'

import { apiMode } from '../api/provider'

export const useAppStore = defineStore('app', {
  state: () => ({
    apiMode,
    mockResetting: false,
    sidebarCollapsed: false,
  }),
  actions: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },
    async resetMockData() {
      if (this.apiMode !== 'mock') {
        return false
      }

      this.mockResetting = true

      try {
        const { resetDatabase } = await import('../mock/database')
        resetDatabase()
        return true
      } finally {
        this.mockResetting = false
      }
    },
  },
})
