import provider from './provider'

export const createComplaint = (payload) => provider.complaint.createComplaint(payload)

export const getMyComplaints = (params) => provider.complaint.getMyComplaints(params)

export const getComplaintDetail = (complaintId) => provider.complaint.getComplaintDetail(complaintId)
