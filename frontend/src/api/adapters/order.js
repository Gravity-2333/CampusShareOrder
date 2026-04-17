const ensureArray = (value) => (Array.isArray(value) ? value : [])

const normalizeTimelineItem = (item = {}) => ({
  action: item.action || '',
  at: item.at || '',
  description: item.description || '',
})

const normalizeMember = (member = {}) => ({
  exitedAt: member.exitedAt || null,
  joinStatus: member.joinStatus || 'CANCELED',
  joinedAt: member.joinedAt || null,
  memberId: Number(member.memberId || 0),
  nickname: member.nickname || '',
  paidAt: member.paidAt || null,
  payAmount: Number(member.payAmount || 0),
  payStatus: member.payStatus || 'UNPAID',
  receivedAt: member.receivedAt || null,
  receiveStatus: member.receiveStatus || 'NOT_READY',
  refundAmountTotal: Number(member.refundAmountTotal || 0),
  role: member.role || 'MEMBER',
  userId: Number(member.userId || 0),
})

const normalizeCurrentUserMember = (member = {}) => ({
  ...normalizeMember(member),
  myRole: member.myRole || member.role || 'MEMBER',
})

const normalizeReceiveStatusItem = (item = {}) => ({
  nickname: item.nickname || '',
  receivedAt: item.receivedAt || null,
  receiveStatus: item.receiveStatus || 'NOT_READY',
})

export const normalizeOrderDetail = (detail = {}) => ({
  actionFlags: {
    canConfirmReceived: Boolean(detail.actionFlags?.canConfirmReceived),
    canCreateComplaint: Boolean(detail.actionFlags?.canCreateComplaint),
    canExit: Boolean(detail.actionFlags?.canExit),
    canJoin: Boolean(detail.actionFlags?.canJoin),
    canMarkDelivered: Boolean(detail.actionFlags?.canMarkDelivered),
    canPay: Boolean(detail.actionFlags?.canPay),
    canUploadReceipt: Boolean(detail.actionFlags?.canUploadReceipt),
    canViewReceipt: Boolean(detail.actionFlags?.canViewReceipt),
  },
  basicInfo: {
    complaintOpened: Boolean(detail.basicInfo?.complaintOpened),
    currentMemberCount: Number(detail.basicInfo?.currentMemberCount || 0),
    deadlineAt: detail.basicInfo?.deadlineAt || '',
    estimatedTotalAmount: Number(detail.basicInfo?.estimatedTotalAmount || 0),
    expectedDeliveryEndAt: detail.basicInfo?.expectedDeliveryEndAt || '',
    orderId: Number(detail.basicInfo?.orderId || 0),
    orderNo: detail.basicInfo?.orderNo || '',
    pickupPoint: detail.basicInfo?.pickupPoint || '',
    productDesc: detail.basicInfo?.productDesc || '',
    productName: detail.basicInfo?.productName || '',
    receiptUploadDeadlineAt: detail.basicInfo?.receiptUploadDeadlineAt || '',
    status: detail.basicInfo?.status || 'OPEN',
    totalMemberCount: Number(detail.basicInfo?.totalMemberCount || 0),
  },
  complaintInfo: {
    complaintCount: Number(detail.complaintInfo?.complaintCount || 0),
    complaintOpened: Boolean(detail.complaintInfo?.complaintOpened),
    myComplaintId:
      detail.complaintInfo?.myComplaintId === null || detail.complaintInfo?.myComplaintId === undefined
        ? null
        : Number(detail.complaintInfo.myComplaintId),
    myComplaintStatus: detail.complaintInfo?.myComplaintStatus || null,
  },
  currentUserMember: detail.currentUserMember
    ? normalizeCurrentUserMember(detail.currentUserMember)
    : null,
  initiatorInfo: {
    nickname: detail.initiatorInfo?.nickname || '',
    phoneMasked: detail.initiatorInfo?.phoneMasked || '',
    studentNoMasked: detail.initiatorInfo?.studentNoMasked || '',
    userId: Number(detail.initiatorInfo?.userId || 0),
  },
  memberList: ensureArray(detail.memberList).map(normalizeMember),
  paymentSummary: {
    actualTotalAmount:
      detail.paymentSummary?.actualTotalAmount === null ||
      detail.paymentSummary?.actualTotalAmount === undefined
        ? null
        : Number(detail.paymentSummary.actualTotalAmount),
    estimatedTotalAmount: Number(detail.paymentSummary?.estimatedTotalAmount || 0),
    paidMemberCount: Number(detail.paymentSummary?.paidMemberCount || 0),
    refundAmountTotal: Number(detail.paymentSummary?.refundAmountTotal || 0),
    totalPaidAmount: Number(detail.paymentSummary?.totalPaidAmount || 0),
  },
  receiptInfo: detail.receiptInfo
    ? {
        actualTotalAmount:
          detail.receiptInfo.actualTotalAmount === null || detail.receiptInfo.actualTotalAmount === undefined
            ? null
            : Number(detail.receiptInfo.actualTotalAmount),
        receiptImageUrl: detail.receiptInfo.receiptImageUrl || '',
        uploadedAt: detail.receiptInfo.uploadedAt || '',
      }
    : null,
  receiveInfo: {
    autoConfirmDeadlineAt: detail.receiveInfo?.autoConfirmDeadlineAt || '',
    deliveredAt: detail.receiveInfo?.deliveredAt || '',
    receiveStatusSummary: ensureArray(detail.receiveInfo?.receiveStatusSummary).map(
      normalizeReceiveStatusItem,
    ),
  },
  timeline: ensureArray(detail.timeline).map(normalizeTimelineItem),
  viewerRoleInOrder: detail.viewerRoleInOrder || 'VISITOR',
})
