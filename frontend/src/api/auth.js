import provider from './provider'

export const register = (payload) => provider.auth.register(payload)

export const login = (payload) => provider.auth.login(payload)

export const adminLogin = (payload) => provider.auth.adminLogin(payload)

export const getCurrentLoginInfo = () => provider.auth.getCurrentLoginInfo()

export const logout = () => provider.auth.logout()
