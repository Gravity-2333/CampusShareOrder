import { defineStore } from 'pinia'

import { adminLogin, getCurrentLoginInfo, login, logout, register } from '../api/auth'
import { getUserProfile, updateUserProfile, verifyStudent } from '../api/user'
import { clearSessionStorage, getStoredSession, saveSessionStorage } from '../utils/auth'

const defaultSession = () => ({
  adminId: null,
  isVerified: false,
  nickname: '',
  role: '',
  status: '',
  token: '',
  userId: null,
  username: '',
})

export const useUserStore = defineStore('user', {
  state: () => ({
    initialized: false,
    initializing: false,
    profile: null,
    session: defaultSession(),
  }),
  getters: {
    displayName(state) {
      return state.session.nickname || state.session.username || '未登录'
    },
    isAdmin(state) {
      return state.session.role === 'ADMIN'
    },
    isLoggedIn(state) {
      return Boolean(state.session.token)
    },
    isUser(state) {
      return state.session.role === 'USER'
    },
  },
  actions: {
    applySession(payload) {
      this.session = {
        ...defaultSession(),
        ...payload,
      }

      if (this.session.token) {
        saveSessionStorage(this.session)
      }
    },
    clearSession() {
      this.profile = null
      this.session = defaultSession()
      clearSessionStorage()
    },
    async initializeSession() {
      if (this.initialized || this.initializing) {
        return
      }

      this.initializing = true

      const cached = getStoredSession()

      if (!cached?.token) {
        this.initialized = true
        this.initializing = false
        return
      }

      this.session = {
        ...defaultSession(),
        ...cached,
      }

      try {
        await this.refreshCurrentLogin()
      } catch {
        this.clearSession()
      } finally {
        this.initialized = true
        this.initializing = false
      }
    },
    async refreshCurrentLogin() {
      const current = await getCurrentLoginInfo()
      this.applySession({
        ...current,
        token: this.session.token,
      })
      return current
    },
    async loginUser(payload) {
      const result = await login(payload)
      this.applySession({
        ...result.userInfo,
        token: result.token,
      })
      return result
    },
    async loginAdmin(payload) {
      const result = await adminLogin(payload)
      this.applySession({
        ...result.adminInfo,
        isVerified: true,
        nickname: '',
        token: result.token,
        userId: null,
      })
      return result
    },
    async registerUser(payload) {
      return register(payload)
    },
    async logoutCurrent() {
      try {
        await logout()
      } finally {
        this.clearSession()
      }
    },
    async loadProfile() {
      const profile = await getUserProfile()
      this.profile = profile
      this.applySession({
        ...this.session,
        isVerified: profile.isVerified,
        nickname: profile.nickname,
        status: profile.status,
        userId: profile.userId,
      })
      return profile
    },
    async saveProfile(payload) {
      const result = await updateUserProfile(payload)
      await this.loadProfile()
      return result
    },
    async submitStudentVerification(payload) {
      const result = await verifyStudent(payload)
      this.applySession({
        ...this.session,
        isVerified: result.isVerified,
      })
      await this.loadProfile()
      return result
    },
  },
})
