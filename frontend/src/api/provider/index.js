import * as liveAdmin from '../providers/live/admin'
import * as liveAuth from '../providers/live/auth'
import * as liveComplaint from '../providers/live/complaint'
import * as liveOrder from '../providers/live/order'
import * as liveUser from '../providers/live/user'
import * as mockAdmin from '../../mock/admin'
import * as mockAuth from '../../mock/auth'
import * as mockComplaint from '../../mock/complaint'
import * as mockOrder from '../../mock/order'
import * as mockUser from '../../mock/user'

export const apiMode = import.meta.env.VITE_API_MODE === 'live' ? 'live' : 'mock'

const liveProvider = {
  admin: liveAdmin,
  auth: liveAuth,
  complaint: liveComplaint,
  order: liveOrder,
  user: liveUser,
}

const mockProvider = {
  admin: mockAdmin,
  auth: mockAuth,
  complaint: mockComplaint,
  order: mockOrder,
  user: mockUser,
}

const provider = apiMode === 'live' ? liveProvider : mockProvider

export default provider
