import {
  complaintStatusTextMap,
  complaintTypeTextMap,
  joinStatusTextMap,
  orderStatusTextMap,
  payStatusTextMap,
  receiveStatusTextMap,
  roleTextMap,
  statusToneMap,
  userStatusTextMap,
} from './enum'

export const formatCurrency = (value) => {
  const amount = Number(value || 0)
  return `¥ ${amount.toFixed(2)}`
}

export const formatDateTime = (value) => value || '--'

export const formatSignedNumber = (value) => {
  const amount = Number(value || 0)

  if (amount > 0) {
    return `+${amount}`
  }

  return `${amount}`
}

export const maskPhone = (value) =>
  value ? `${value.slice(0, 3)}****${value.slice(-4)}` : '--'

export const formatRole = (value) => roleTextMap[value] || value || '--'

export const formatUserStatus = (value) => userStatusTextMap[value] || value || '--'

export const formatOrderStatus = (value) => orderStatusTextMap[value] || value || '--'

export const formatPayStatus = (value) => payStatusTextMap[value] || value || '--'

export const formatJoinStatus = (value) => joinStatusTextMap[value] || value || '--'

export const formatReceiveStatus = (value) => receiveStatusTextMap[value] || value || '--'

export const formatComplaintStatus = (value) => complaintStatusTextMap[value] || value || '--'

export const formatComplaintType = (value) => complaintTypeTextMap[value] || value || '--'

export const getStatusTone = (value) => statusToneMap[value] || 'info'
