import axios from 'axios'

import { clearSessionStorage, getAccessToken } from '../utils/auth'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000,
})

const latin1Pattern = /[\u00C0-\u00FF]/
const cjkPattern = /[\u3400-\u9FFF]/

const decodePotentialMojibake = (value) => {
  if (typeof value !== 'string' || !value || cjkPattern.test(value) || !latin1Pattern.test(value)) {
    return value
  }

  try {
    const bytes = Uint8Array.from([...value].map((char) => char.charCodeAt(0)))
    const decoded = new TextDecoder('utf-8', { fatal: true }).decode(bytes)
    return decoded || value
  } catch {
    return value
  }
}

const normalizeResponseStrings = (payload) => {
  if (typeof payload === 'string') {
    return decodePotentialMojibake(payload)
  }

  if (Array.isArray(payload)) {
    return payload.map(normalizeResponseStrings)
  }

  if (payload && typeof payload === 'object') {
    return Object.fromEntries(
      Object.entries(payload).map(([key, value]) => [key, normalizeResponseStrings(value)]),
    )
  }

  return payload
}

const shouldClearSession = (code) => [401, 40101, 40102, 40302].includes(Number(code))

request.interceptors.request.use((config) => {
  const token = getAccessToken()

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

request.interceptors.response.use(
  (response) => {
    const payload = normalizeResponseStrings(response.data)

    if (payload && typeof payload === 'object' && 'code' in payload) {
      if (payload.code !== 0) {
        const error = new Error(payload.message || '请求失败')
        error.code = payload.code

        if (shouldClearSession(payload.code)) {
          clearSessionStorage()
        }

        throw error
      }

      return payload.data
    }

    return payload
  },
  (error) => {
    const responsePayload = normalizeResponseStrings(error.response?.data)

    if (responsePayload?.code) {
      const wrappedError = new Error(responsePayload.message || '请求失败')
      wrappedError.code = responsePayload.code

      if (shouldClearSession(wrappedError.code)) {
        clearSessionStorage()
      }

      throw wrappedError
    }

    const status = error.response?.status

    if (status === 401) {
      clearSessionStorage()
    }

    const fallbackMessageMap = {
      401: '登录状态已失效，请重新登录',
      403: '当前账号无权执行该操作',
      404: '当前接口暂未提供或请求地址不存在',
      500: '服务器开小差了，请稍后重试',
    }

    const networkError = new Error(
      fallbackMessageMap[status] || error.message || '网络异常，请检查服务是否已启动',
    )
    networkError.code = status || error.code || 50000
    throw networkError
  },
)

export default request
