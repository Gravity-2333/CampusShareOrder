import { clearSessionStorage, getAccessToken } from '../utils/auth'
import { getDatabase, issueToken, makeFailure, mutateDatabase, revokeToken, timestamp } from './database'
import { requireLogin, sleep } from './shared'

export const register = async (payload) => {
  await sleep()

  const database = getDatabase()

  if (database.users.some((item) => item.phone === payload.phone)) {
    makeFailure(42201, '手机号已注册')
  }

  let createdUser = null

  mutateDatabase((draft) => {
    const userId = Math.max(0, ...draft.users.map((item) => item.userId)) + 1
    createdUser = {
      contactInfo: '',
      createdAt: timestamp(),
      creditRecords: [
        {
          changeReason: '初始信用分',
          createdAt: timestamp(),
          delta: 100,
        },
      ],
      creditScore: 100,
      isVerified: false,
      nickname: payload.nickname,
      password: payload.password,
      phone: payload.phone,
      role: 'USER',
      status: 'NORMAL',
      studentNo: '',
      userId,
    }
    draft.users.push(createdUser)
  })

  return {
    nickname: createdUser.nickname,
    phone: createdUser.phone,
    userId: createdUser.userId,
  }
}

export const login = async (payload) => {
  await sleep()

  const database = getDatabase()
  const user = database.users.find(
    (item) => item.phone === payload.phone && item.password === payload.password,
  )

  if (!user) {
    makeFailure(40102, '手机号或密码错误')
  }

  if (user.status === 'BANNED') {
    makeFailure(40302, '当前账号已被封禁')
  }

  const token = issueToken('USER', user.userId)

  return {
    token,
    userInfo: {
      isVerified: user.isVerified,
      nickname: user.nickname,
      role: 'USER',
      status: user.status,
      userId: user.userId,
    },
  }
}

export const adminLogin = async (payload) => {
  await sleep()

  const database = getDatabase()
  const admin = database.admins.find(
    (item) => item.username === payload.username && item.password === payload.password,
  )

  if (!admin) {
    makeFailure(40102, '管理员账号或密码错误')
  }

  const token = issueToken('ADMIN', admin.adminId)

  return {
    adminInfo: {
      adminId: admin.adminId,
      role: 'ADMIN',
      status: admin.status,
      username: admin.username,
    },
    token,
  }
}

export const getCurrentLoginInfo = async () => {
  const account = await requireLogin()

  if ('adminId' in account) {
    return {
      adminId: account.adminId,
      isVerified: true,
      nickname: '',
      role: 'ADMIN',
      status: account.status,
      userId: null,
      username: account.username,
    }
  }

  return {
    adminId: null,
    isVerified: account.isVerified,
    nickname: account.nickname,
    role: 'USER',
    status: account.status,
    userId: account.userId,
    username: '',
  }
}

export const logout = async () => {
  await sleep()

  const token = getAccessToken()
  revokeToken(token)
  clearSessionStorage()

  return {
    success: true,
  }
}
