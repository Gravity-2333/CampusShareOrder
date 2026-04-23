import request from '../../request'

export const register = (payload) => request.post('/api/auth/register', payload)

export const login = (payload) => request.post('/api/auth/login', payload)

export const adminLogin = async (payload) => {
  try {
    return await request.post('/api/auth/admin/login', payload)
  } catch (error) {
    if (error.code === 404 || error.response?.status === 404) {
      throw new Error('当前后端还未提供管理员登录接口', { cause: error })
    }

    throw error
  }
}

export const getCurrentLoginInfo = () => request.get('/api/auth/me')

export const logout = () => request.post('/api/auth/logout')
