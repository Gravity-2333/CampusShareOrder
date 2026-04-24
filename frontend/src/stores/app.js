import { defineStore } from 'pinia'

let viewportListeners = 0
let detachViewportListener = null

export const useAppStore = defineStore('app', {
  state: () => ({
    isMobileViewport: false,
    mobileNavOpen: false,
    sidebarCollapsed: false,
  }),
  actions: {
    initializeViewportTracking() {
      if (typeof window === 'undefined') {
        return
      }

      viewportListeners += 1

      if (detachViewportListener) {
        return
      }

      const mediaQuery = window.matchMedia('(max-width: 960px)')
      const syncViewport = (matches) => {
        this.isMobileViewport = matches

        if (!matches) {
          this.mobileNavOpen = false
        }
      }

      const handleChange = (event) => {
        syncViewport(event.matches)
      }

      syncViewport(mediaQuery.matches)
      mediaQuery.addEventListener('change', handleChange)

      detachViewportListener = () => {
        mediaQuery.removeEventListener('change', handleChange)
        detachViewportListener = null
      }
    },
    releaseViewportTracking() {
      if (viewportListeners > 0) {
        viewportListeners -= 1
      }

      if (viewportListeners === 0 && detachViewportListener) {
        detachViewportListener()
      }
    },
    toggleSidebar() {
      if (this.isMobileViewport) {
        this.mobileNavOpen = !this.mobileNavOpen
        return
      }

      this.sidebarCollapsed = !this.sidebarCollapsed
    },
    closeMobileNav() {
      this.mobileNavOpen = false
    },
  },
})
