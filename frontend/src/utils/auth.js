const STORAGE_KEY = 'campus-share-order-session'

export const getStoredSession = () => {
  const raw = localStorage.getItem(STORAGE_KEY)

  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw)
  } catch {
    clearSessionStorage()
    return null
  }
}

export const saveSessionStorage = (session) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
}

export const clearSessionStorage = () => {
  localStorage.removeItem(STORAGE_KEY)
}

export const getAccessToken = () => getStoredSession()?.token || ''
