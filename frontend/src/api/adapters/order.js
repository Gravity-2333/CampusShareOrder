import { normalizeId } from '../../utils/id'

const ensureArray = (value) => (Array.isArray(value) ? value : [])

const normalizeRole = (role, isCreator = false) => {
  if (role === 'CREATOR') {
    return 'INITIATOR'
  }

  if (role) {
    return role
  }

  return isCreator ? 'INITIATOR' : 'MEMBER'
}

const normalizeTimelineItem = (item = {}) => ({
  action: item.action || '',
  at: item.at || item.time || '',
  description: item.description || '',
})

const normalizeMember = (member = {}) => ({
  exitedAt: member.exitedAt || null,
  joinStatus: member.joinStatus || 'CANCELED',
  joinedAt: member.joinedAt || null,
  memberId: normalizeId(member.memberId || member.userId),
  nickname: member.nickname || '',
  paidAt: member.paidAt || null,
  payAmount: Number(member.payAmount || 0),
  payStatus: member.payStatus || 'UNPAID',
  receivedAt: member.receivedAt || null,
  receiveStatus: member.receiveStatus || 'NOT_READY',
  refundAmountTotal: Number(member.refundAmountTotal || 0),
  role: normalizeRole(member.role, member.isCreator),
  userId: normalizeId(member.userId),
})

const normalizeCurrentUserMember = (member = {}) => ({
  ...normalizeMember(member),
  myRole: normalizeRole(member.myRole || member.role, member.isCreator),
})

const normalizeReceiveStatusItem = (item = {}) => ({
  nickname: item.nickname || '',
  receivedAt: item.receivedAt || null,
  receiveStatus: item.receiveStatus || 'NOT_READY',
})

export const normalizeOrderDetail = (detail = {}) => {
  const memberList = ensureArray(detail.memberList).map(normalizeMember)
  const receiveStatusSummary =
    ensureArray(detail.receiveInfo?.receiveStatusSummary).length > 0
      ? ensureArray(detail.receiveInfo?.receiveStatusSummary).map(normalizeReceiveStatusItem)
      : memberList.map((member) => normalizeReceiveStatusItem(member))

  return {
    actionFlags: {
      canCancel: Boolean(detail.actionFlags?.canCancel),
      canConfirmReceived: Boolean(detail.actionFlags?.canConfirmReceived),
      canCreateComplaint: Boolean(detail.actionFlags?.canCreateComplaint),
      canExit: Boolean(detail.actionFlags?.canExit),
      canJoin: Boolean(detail.actionFlags?.canJoin),
      canMarkDelivered: Boolean(detail.actionFlags?.canMarkDelivered),
      canPay: Boolean(detail.actionFlags?.canPay),
      canUploadReceipt: Boolean(detail.actionFlags?.canUploadReceipt),
      canViewReceipt: Boolean(detail.actionFlags?.canViewReceipt || detail.receiptInfo?.receiptImageUrl),
    },
    basicInfo: {
      complaintOpened: Boolean(detail.basicInfo?.complaintOpened),
      currentMemberCount: Number(detail.basicInfo?.currentMemberCount || 0),
      deadlineAt: detail.basicInfo?.deadlineAt || '',
      estimatedTotalAmount:
        Number(detail.basicInfo?.estimatedTotalAmount || detail.paymentSummary?.estimatedTotalAmount || 0),
      expectedDeliveryEndAt: detail.basicInfo?.expectedDeliveryEndAt || '',
      orderId: normalizeId(detail.basicInfo?.orderId),
      orderNo: detail.basicInfo?.orderNo || '',
      pickupPoint: detail.basicInfo?.pickupPoint || '',
      productDesc: detail.basicInfo?.productDesc || '',
      productName: detail.basicInfo?.productName || '',
      receiptUploadDeadlineAt: detail.basicInfo?.receiptUploadDeadlineAt || '',
      status: detail.basicInfo?.status || 'OPEN',
      totalMemberCount: Number(detail.basicInfo?.totalMemberCount || 0),
    },
    complaintInfo: {
      complaintCount: Number(detail.complaintInfo?.complaintCount || (detail.complaintInfo?.complaintId ? 1 : 0)),
      complaintOpened: Boolean(detail.complaintInfo?.complaintOpened || detail.complaintInfo?.complaintId),
      myComplaintId:
        detail.complaintInfo?.myComplaintId === null || detail.complaintInfo?.myComplaintId === undefined
          ? detail.complaintInfo?.complaintId === null || detail.complaintInfo?.complaintId === undefined
            ? null
            : normalizeId(detail.complaintInfo.complaintId)
          : normalizeId(detail.complaintInfo.myComplaintId),
      myComplaintStatus: detail.complaintInfo?.myComplaintStatus || detail.complaintInfo?.status || null,
    },
    currentUserMember: detail.currentUserMember
      ? normalizeCurrentUserMember(detail.currentUserMember)
      : null,
    initiatorInfo: {
      nickname: detail.initiatorInfo?.nickname || '',
      phoneMasked: detail.initiatorInfo?.phoneMasked || detail.initiatorInfo?.phone || '',
      studentNoMasked: detail.initiatorInfo?.studentNoMasked || '',
      userId: normalizeId(detail.initiatorInfo?.userId),
    },
    memberList,
    paymentSummary: {
      actualTotalAmount:
        detail.paymentSummary?.actualTotalAmount === null ||
        detail.paymentSummary?.actualTotalAmount === undefined
          ? detail.basicInfo?.actualTotalAmount === null || detail.basicInfo?.actualTotalAmount === undefined
            ? null
            : Number(detail.basicInfo.actualTotalAmount)
          : Number(detail.paymentSummary.actualTotalAmount),
      estimatedTotalAmount: Number(
        detail.paymentSummary?.estimatedTotalAmount || detail.basicInfo?.estimatedTotalAmount || 0,
      ),
      paidMemberCount: Number(detail.paymentSummary?.paidMemberCount || detail.paymentSummary?.paidMembers || 0),
      refundAmountTotal: Number(
        detail.paymentSummary?.refundAmountTotal || detail.paymentSummary?.refundTotalAmount || 0,
      ),
      totalPaidAmount: Number(detail.paymentSummary?.totalPaidAmount || 0),
    },
    receiptInfo: detail.receiptInfo
      ? {
          actualTotalAmount:
            detail.receiptInfo.actualTotalAmount === null || detail.receiptInfo.actualTotalAmount === undefined
              ? null
              : Number(detail.receiptInfo.actualTotalAmount),
          expectedDeliveryEndAt: detail.receiptInfo.expectedDeliveryEndAt || '',
          expectedDeliveryStartAt: detail.receiptInfo.expectedDeliveryStartAt || '',
          receiptImageUrl: detail.receiptInfo.receiptImageUrl || detail.receiptInfo.imageUrl || '',
          uploadedAt: detail.receiptInfo.uploadedAt || '',
        }
      : null,
    receiveInfo: {
      autoConfirmDeadlineAt: detail.receiveInfo?.autoConfirmDeadlineAt || '',
      deliveredAt: detail.receiveInfo?.deliveredAt || detail.basicInfo?.deliveredAt || '',
      receiveStatusSummary,
    },
    timeline: ensureArray(detail.timeline).map(normalizeTimelineItem),
    viewerRoleInOrder: normalizeRole(detail.viewerRoleInOrder),
  }
}
