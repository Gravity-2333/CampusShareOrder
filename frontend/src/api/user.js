import provider from './provider'

export const verifyStudent = (payload) => provider.user.verifyStudent(payload)

export const getUserProfile = () => provider.user.getUserProfile()

export const updateUserProfile = (payload) => provider.user.updateUserProfile(payload)

export const getUserCredit = (params) => provider.user.getUserCredit(params)

export const getUserCapitalRecords = (params) => provider.user.getUserCapitalRecords(params)

export const getMyOrders = (params) => provider.user.getMyOrders(params)
