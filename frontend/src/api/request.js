import axios from 'axios'

import { clearSessionStorage, getAccessToken } from '../utils/auth'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000,
})

request.interceptors.request.use((config) => {
  const token = getAccessToken()

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

request.interceptors.response.use(
  (response) => {
    const payload = response.data

    if (payload && typeof payload === 'object' && 'code' in payload) {
      if (payload.code !== 0) {
        const error = new Error(payload.message || '请求失败')
        error.code = payload.code

        if (payload.code === 40101 || payload.code === 40102) {
          clearSessionStorage()
        }

        throw error
      }

      return payload.data
    }

    return payload
  },
  (error) => {
    const responsePayload = error.response?.data

    if (responsePayload?.code) {
      const wrappedError = new Error(responsePayload.message || '请求失败')
      wrappedError.code = responsePayload.code

      if (wrappedError.code === 40101 || wrappedError.code === 40102) {
        clearSessionStorage()
      }

      throw wrappedError
    }

    const networkError = new Error(error.message || '网络异常')
    networkError.code = error.code || 50000
    throw networkError
  },
)

export default request
