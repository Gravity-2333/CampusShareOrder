export const apiMode = import.meta.env.VITE_API_MODE === 'live' ? 'live' : 'mock'

const providerLoaders = {
  live: {
    admin: () => import('../providers/live/admin'),
    auth: () => import('../providers/live/auth'),
    complaint: () => import('../providers/live/complaint'),
    order: () => import('../providers/live/order'),
    user: () => import('../providers/live/user'),
  },
  mock: {
    admin: () => import('../../mock/admin'),
    auth: () => import('../../mock/auth'),
    complaint: () => import('../../mock/complaint'),
    order: () => import('../../mock/order'),
    user: () => import('../../mock/user'),
  },
}

const providerModules = new Map()

const loadProviderModule = async (domain) => {
  const cacheKey = `${apiMode}:${domain}`

  if (!providerModules.has(cacheKey)) {
    const loader = providerLoaders[apiMode]?.[domain]

    if (!loader) {
      throw new Error(`未找到 ${apiMode} 模式下的 ${domain} provider`)
    }

    providerModules.set(cacheKey, loader())
  }

  return providerModules.get(cacheKey)
}

const createDomainProxy = (domain) =>
  new Proxy(
    {},
    {
      get(_target, methodName) {
        if (typeof methodName !== 'string') {
          return undefined
        }

        return async (...args) => {
          const domainModule = await loadProviderModule(domain)
          const targetMethod = domainModule[methodName]

          if (typeof targetMethod !== 'function') {
            throw new Error(`provider ${domain}.${methodName} 不存在`)
          }

          return targetMethod(...args)
        }
      },
    },
  )

const provider = {
  admin: createDomainProxy('admin'),
  auth: createDomainProxy('auth'),
  complaint: createDomainProxy('complaint'),
  order: createDomainProxy('order'),
  user: createDomainProxy('user'),
}

export default provider
