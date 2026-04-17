import { defineStore } from 'pinia'

import { adminLogin, getCurrentLoginInfo, login, logout, register } from '../api/auth'
import { getUserCredit, getUserProfile, updateUserProfile, verifyStudent } from '../api/user'
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

const defaultCredit = () => ({
  creditScore: 0,
  records: [],
})

export const useUserStore = defineStore('user', {
  state: () => ({
    credit: defaultCredit(),
    creditLoading: false,
    initialized: false,
    initializing: false,
    profile: null,
    profileLoading: false,
    savingProfile: false,
    session: defaultSession(),
    verifyingStudent: false,
  }),
  getters: {
    creditLevel(state) {
      const score = Number(state.credit.creditScore || state.profile?.creditScore || 0)

      if (score >= 95) {
        return '优秀'
      }

      if (score >= 80) {
        return '良好'
      }

      if (score >= 60) {
        return '观察'
      }

      return '预警'
    },
    displayName(state) {
      return state.session.nickname || state.session.username || '未登录'
    },
    hasProfile(state) {
      return Boolean(state.profile?.userId)
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
    profileCompletion(state) {
      if (!state.profile) {
        return 0
      }

      const fields = [state.profile.nickname, state.profile.contactInfo, state.profile.studentNo]
      const completed = fields.filter((value) => String(value ?? '').trim()).length
      return Math.round((completed / fields.length) * 100)
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
      this.credit = defaultCredit()
      this.creditLoading = false
      this.profile = null
      this.profileLoading = false
      this.savingProfile = false
      this.session = defaultSession()
      this.verifyingStudent = false
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
      this.profileLoading = true

      try {
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
      } finally {
        this.profileLoading = false
      }
    },
    async ensureProfileLoaded() {
      if (this.profile?.userId) {
        return this.profile
      }

      return this.loadProfile()
    },
    async saveProfile(payload) {
      this.savingProfile = true

      try {
        const result = await updateUserProfile(payload)
        await this.loadProfile()
        return result
      } finally {
        this.savingProfile = false
      }
    },
    async loadCredit() {
      this.creditLoading = true

      try {
        const credit = await getUserCredit()
        this.credit = credit
        return credit
      } finally {
        this.creditLoading = false
      }
    },
    async submitStudentVerification(payload) {
      this.verifyingStudent = true

      try {
        const result = await verifyStudent(payload)
        this.applySession({
          ...this.session,
          isVerified: result.isVerified,
        })
        await this.loadProfile()
        return result
      } finally {
        this.verifyingStudent = false
      }
    },
    async bootstrapUserCenter() {
      await Promise.allSettled([this.ensureProfileLoaded(), this.loadCredit()])
    },
  },
})
