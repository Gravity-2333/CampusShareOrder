const providerLoaders = {
  admin: () => import('../providers/live/admin'),
  auth: () => import('../providers/live/auth'),
  complaint: () => import('../providers/live/complaint'),
  order: () => import('../providers/live/order'),
  user: () => import('../providers/live/user'),
}

const providerModules = new Map()

const loadProviderModule = async (domain) => {
  if (!providerModules.has(domain)) {
    const loader = providerLoaders[domain]

    if (!loader) {
      throw new Error(`未找到 ${domain} 服务`)
    }

    providerModules.set(domain, loader())
  }

  return providerModules.get(domain)
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
            throw new Error(`${domain}.${methodName} 服务不存在`)
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
