import request from '../../request'

export const register = (payload) => request.post('/api/auth/register', payload)

export const login = (payload) => request.post('/api/auth/login', payload)

export const adminLogin = (payload) => request.post('/api/auth/admin/login', payload)

export const getCurrentLoginInfo = () => request.get('/api/auth/me')

export const logout = () => request.post('/api/auth/logout')
