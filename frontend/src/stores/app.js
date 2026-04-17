import { defineStore } from 'pinia'

import { apiMode } from '../api/provider'

export const useAppStore = defineStore('app', {
  state: () => ({
    apiMode,
    sidebarCollapsed: false,
  }),
  actions: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },
  },
})
