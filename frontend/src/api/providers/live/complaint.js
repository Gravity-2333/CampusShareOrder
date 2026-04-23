import request from '../../request'

export const createComplaint = (payload) => request.post('/api/complaints', payload)

export const getMyComplaints = (params) => request.get('/api/complaints/my', { params })

export const getComplaintDetail = (complaintId) => request.get(`/api/complaints/${complaintId}`)
