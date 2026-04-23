import request from '../../request'

export const createComplaint = async (payload) => {
  await request.post('/api/complaints', payload)

  const latestComplaints = await request.get('/api/complaints/my', {
    params: {
      page: 1,
      pageSize: 1,
    },
  })

  return latestComplaints?.list?.[0]
    ? {
        complaintId: Number(latestComplaints.list[0].complaintId),
        complaintNo: latestComplaints.list[0].complaintNo,
        status: latestComplaints.list[0].status,
      }
    : null
}

export const getMyComplaints = (params) => request.get('/api/complaints/my', { params })

export const getComplaintDetail = (complaintId) => request.get(`/api/complaints/${complaintId}`)
