<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElImage, ElMessage, ElMessageBox } from 'element-plus'

import EmptyState from '../../components/common/EmptyState.vue'
import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useOrderStore } from '../../stores/order'
import {
  formatComplaintStatus,
  formatCurrency,
  formatDateTime,
  formatJoinStatus,
  formatOrderStatus,
  formatPayStatus,
  formatReceiveStatus,
  formatRole,
} from '../../utils/format'

const route = useRoute()
const router = useRouter()
const orderStore = useOrderStore()

const receiptDialogVisible = ref(false)
const activeReceiptUrl = ref('')

const detail = computed(() => orderStore.detail)
const currentOrderId = computed(() => route.params.orderId)
const normalizedOrderId = computed(() => Number(currentOrderId.value || 0))
const isValidOrderId = computed(() => Number.isInteger(normalizedOrderId.value) && normalizedOrderId.value > 0)

const hasActualAmount = computed(
  () =>
    detail.value?.paymentSummary?.actualTotalAmount !== null &&
    detail.value?.paymentSummary?.actualTotalAmount !== undefined,
)

const activeMemberList = computed(
  () => detail.value?.memberList?.filter((member) => member.joinStatus === 'ACTIVE') || [],
)

const paidMemberCount = computed(
  () => activeMemberList.value.filter((member) => member.payStatus === 'PAID').length,
)

const receivedMemberCount = computed(
  () =>
    activeMemberList.value.filter((member) =>
      ['RECEIVED', 'AUTO_RECEIVED'].includes(member.receiveStatus),
    ).length,
)

const activeMembersNeedPay = computed(
  () => activeMemberList.value.filter((member) => member.payStatus === 'UNPAID').length,
)

const activeMembersWaitingReceive = computed(
  () => activeMemberList.value.filter((member) => member.receiveStatus === 'WAIT_CONFIRM').length,
)

const paymentDeltaText = computed(() => {
  if (!detail.value || !hasActualAmount.value) {
    return '--'
  }

  const estimated = Number(detail.value.paymentSummary.estimatedTotalAmount || 0)
  const actual = Number(detail.value.paymentSummary.actualTotalAmount || 0)
  return formatCurrency(estimated - actual)
})

const complaintActionText = computed(() => {
  if (!detail.value) {
    return '--'
  }

  if (detail.value.actionFlags.canCreateComplaint) {
    return '当前可直接发起投诉。'
  }

  if (detail.value.complaintInfo.myComplaintId) {
    return '当前账号已经发起过投诉，可直接查看详情。'
  }

  if (detail.value.complaintInfo.complaintOpened) {
    return '投诉通道已开放，但当前账号此时不可再次创建投诉。'
  }

  return '投诉通道尚未开放。'
})

const receiptStatusText = computed(() => {
  if (!detail.value) {
    return '--'
  }

  if (detail.value.receiptInfo) {
    return '已上传凭证。'
  }

  if (detail.value.actionFlags.canUploadReceipt) {
    return '待发起人上传凭证。'
  }

  return '当前暂无凭证。'
})

const detailErrorText = computed(() => {
  if (!isValidOrderId.value) {
    return '当前路由中的订单 ID 无效，请返回大厅重新进入详情页。'
  }

  return orderStore.detailError || '当前未能加载到订单详情，请稍后重试。'
})

const primaryAction = computed(() => {
  if (!detail.value) {
    return null
  }

  const flags = detail.value.actionFlags

  if (flags.canJoin) {
    return { key: 'join', label: '立即加入拼单', type: 'primary' }
  }

  if (flags.canPay) {
    return { key: 'pay', label: '立即支付', type: 'primary' }
  }

  if (flags.canUploadReceipt) {
    return { key: 'upload', label: '上传凭证', type: 'primary' }
  }

  if (flags.canMarkDelivered) {
    return { key: 'delivered', label: '确认送达', type: 'success' }
  }

  if (flags.canConfirmReceived) {
    return { key: 'received', label: '确认收货', type: 'success' }
  }

  if (flags.canCreateComplaint) {
    return { key: 'complaint', label: '发起投诉', type: 'danger' }
  }

  if (flags.canViewReceipt) {
    return { key: 'viewReceipt', label: '查看凭证', type: 'default' }
  }

  return null
})

const nextStepHint = computed(() => {
  if (!detail.value) {
    return ''
  }

  const flags = detail.value.actionFlags

  if (flags.canJoin) {
    return '当前订单仍可加入，进入详情后可直接参与本次拼单。'
  }

  if (flags.canPay) {
    return '你已经加入订单，但尚未支付；支付后才能推动订单进入成团流程。'
  }

  if (flags.canUploadReceipt) {
    return '订单已成团，下一步应由发起人上传购买凭证。'
  }

  if (flags.canMarkDelivered) {
    return '凭证已经上传，下一步应由发起人确认送达。'
  }

  if (flags.canConfirmReceived) {
    return '订单已经送达，当前账号可以确认收货。'
  }

  if (flags.canCreateComplaint) {
    return '如果订单存在异常，可直接从当前页面发起投诉。'
  }

  if (detail.value.complaintInfo.myComplaintId) {
    return '当前账号已经提交投诉，可继续查看处理进度。'
  }

  return '当前页面主要用于查看订单聚合信息，是否可操作以 actionFlags 为准。'
})

const timelineToneMap = {
  COMPLAINT_CREATED: 'danger',
  INITIATOR_JOINED: 'primary',
  INITIATOR_PAID: 'success',
  INITIATOR_RECEIVED: 'success',
  MEMBER_EXITED: 'info',
  MEMBER_JOINED: 'primary',
  MEMBER_PAID: 'success',
  MEMBER_RECEIVED: 'success',
  ORDER_CREATED: 'primary',
  ORDER_DELIVERED: 'success',
  ORDER_STATUS: 'warning',
  RECEIPT_UPLOADED: 'success',
}

const timelineActionTextMap = {
  COMPLAINT_CREATED: '投诉创建',
  INITIATOR_JOINED: '发起拼单',
  INITIATOR_PAID: '发起人支付',
  INITIATOR_RECEIVED: '发起人收货',
  MEMBER_EXITED: '成员退出',
  MEMBER_JOINED: '成员加入',
  MEMBER_PAID: '成员支付',
  MEMBER_RECEIVED: '成员收货',
  ORDER_CREATED: '订单创建',
  ORDER_DELIVERED: '确认送达',
  ORDER_STATUS: '状态更新',
  RECEIPT_UPLOADED: '上传凭证',
}

const getTimelineType = (action) => timelineToneMap[action] || 'info'
const formatTimelineAction = (action) => timelineActionTextMap[action] || action

const getMemberLatestEvent = (member) => {
  if (!member) {
    return '--'
  }

  if (member.receivedAt) {
    return `最近收货：${formatDateTime(member.receivedAt)}`
  }

  if (member.exitedAt) {
    return `最近退出：${formatDateTime(member.exitedAt)}`
  }

  if (member.paidAt) {
    return `最近支付：${formatDateTime(member.paidAt)}`
  }

  if (member.joinedAt) {
    return `最近加入：${formatDateTime(member.joinedAt)}`
  }

  return '--'
}

const riskAlerts = computed(() => {
  if (!detail.value) {
    return []
  }

  const alerts = []

  if (detail.value.actionFlags.canPay) {
    alerts.push({
      key: 'self-unpaid',
      tone: 'warning',
      text: '当前账号仍未支付，订单无法继续推进成团。',
    })
  }

  if (activeMembersNeedPay.value > 0) {
    alerts.push({
      key: 'members-unpaid',
      tone: 'warning',
      text: `仍有 ${activeMembersNeedPay.value} 名有效成员未支付。`,
    })
  }

  if (detail.value.actionFlags.canUploadReceipt) {
    alerts.push({
      key: 'missing-receipt',
      tone: 'primary',
      text: '订单已成团，但发起人尚未上传购买凭证。',
    })
  }

  if (detail.value.actionFlags.canMarkDelivered) {
    alerts.push({
      key: 'waiting-delivery-confirm',
      tone: 'primary',
      text: '凭证已上传，当前仍待发起人确认送达。',
    })
  }

  if (activeMembersWaitingReceive.value > 0) {
    alerts.push({
      key: 'waiting-receive',
      tone: 'warning',
      text: `仍有 ${activeMembersWaitingReceive.value} 名成员待确认收货。`,
    })
  }

  if (detail.value.complaintInfo.complaintOpened) {
    alerts.push({
      key: 'complaint-opened',
      tone: detail.value.complaintInfo.complaintCount > 0 ? 'danger' : 'warning',
      text:
        detail.value.complaintInfo.complaintCount > 0
          ? `投诉通道已开启，当前已有 ${detail.value.complaintInfo.complaintCount} 条投诉记录。`
          : '投诉通道已开启，后续可能进入异常处理流程。',
    })
  }

  if (!alerts.length) {
    alerts.push({
      key: 'no-risk',
      tone: 'success',
      text: '当前未发现明显阻塞项，详情状态可继续按既有流程推进。',
    })
  }

  return alerts
})

const riskTagTypeMap = {
  danger: 'danger',
  primary: 'primary',
  success: 'success',
  warning: 'warning',
}

const formatRiskToneText = (tone) => {
  if (tone === 'danger') {
    return '高风险'
  }

  if (tone === 'warning') {
    return '提醒'
  }

  if (tone === 'primary') {
    return '处理中'
  }

  return '正常'
}

const toDate = (value) => {
  if (!value) {
    return null
  }

  const normalized = String(value).replace(' ', 'T')
  const date = new Date(normalized)

  return Number.isNaN(date.getTime()) ? null : date
}

const milestoneTagTypeMap = {
  danger: 'danger',
  info: 'info',
  success: 'success',
  warning: 'warning',
}

const makeMilestone = ({ key, label, time, doneText, pendingText, skipped = false, done = false }) => {
  if (skipped || !time) {
    return {
      key,
      label,
      statusText: '当前不适用',
      tag: 'info',
      timeText: '--',
    }
  }

  if (done) {
    return {
      key,
      label,
      statusText: doneText,
      tag: 'success',
      timeText: formatDateTime(time),
    }
  }

  const targetDate = toDate(time)
  const now = new Date()
  const expired = targetDate ? now.getTime() > targetDate.getTime() : false

  return {
    key,
    label,
    statusText: expired ? '已超时' : pendingText,
    tag: expired ? 'danger' : 'warning',
    timeText: formatDateTime(time),
  }
}

const milestoneItems = computed(() => {
  if (!detail.value) {
    return []
  }

  return [
    makeMilestone({
      key: 'deadline',
      label: '招募截止',
      time: detail.value.basicInfo.deadlineAt,
      done: detail.value.basicInfo.status !== 'OPEN',
      doneText: '招募阶段已结束',
      pendingText: '仍在招募中',
    }),
    makeMilestone({
      key: 'receipt',
      label: '凭证上传截止',
      time: detail.value.basicInfo.receiptUploadDeadlineAt,
      done: Boolean(detail.value.receiptInfo?.uploadedAt),
      doneText: '凭证已上传',
      pendingText: '待上传凭证',
      skipped:
        !detail.value.basicInfo.receiptUploadDeadlineAt &&
        !detail.value.receiptInfo?.uploadedAt,
    }),
    makeMilestone({
      key: 'delivery',
      label: '预计送达截止',
      time: detail.value.basicInfo.expectedDeliveryEndAt,
      done: Boolean(detail.value.receiveInfo.deliveredAt),
      doneText: '已确认送达',
      pendingText: '待送达',
      skipped:
        !detail.value.basicInfo.expectedDeliveryEndAt &&
        !detail.value.receiveInfo.deliveredAt,
    }),
    makeMilestone({
      key: 'auto-confirm',
      label: '自动确认收货',
      time: detail.value.receiveInfo.autoConfirmDeadlineAt,
      done: detail.value.basicInfo.status === 'COMPLETED',
      doneText: '收货阶段已完成',
      pendingText: '待成员确认收货',
      skipped:
        !detail.value.receiveInfo.autoConfirmDeadlineAt &&
        detail.value.basicInfo.status !== 'COMPLETED',
    }),
  ]
})

const phaseItems = computed(() => {
  if (!detail.value) {
    return []
  }

  const status = detail.value.basicInfo.status
  const items = [
    {
      key: 'open',
      label: '招募阶段',
      hint: '成员加入并完成支付',
      done: ['GROUPED', 'WAIT_DELIVERY', 'WAIT_RECEIVE', 'COMPLETED'].includes(status),
      current: status === 'OPEN',
    },
    {
      key: 'grouped',
      label: '成团待凭证',
      hint: '发起人上传购买凭证',
      done: ['WAIT_DELIVERY', 'WAIT_RECEIVE', 'COMPLETED'].includes(status),
      current: status === 'GROUPED',
    },
    {
      key: 'delivery',
      label: '待确认送达',
      hint: '凭证已上传，等待发起人确认送达',
      done: ['WAIT_RECEIVE', 'COMPLETED'].includes(status),
      current: status === 'WAIT_DELIVERY',
    },
    {
      key: 'receive',
      label: '待确认收货',
      hint: '成员确认收货或系统自动确认',
      done: status === 'COMPLETED',
      current: status === 'WAIT_RECEIVE',
    },
    {
      key: 'completed',
      label: '订单完成',
      hint: '流程全部结束',
      done: status === 'COMPLETED',
      current: status === 'COMPLETED',
    },
  ]

  if (status === 'CANCELED') {
    return items.map((item) => ({
      ...item,
      current: false,
    }))
  }

  return items
})

const phaseSummaryText = computed(() => {
  if (!detail.value) {
    return '--'
  }

  if (detail.value.basicInfo.status === 'CANCELED') {
    return '订单已取消，当前不再继续推进后续阶段。'
  }

  if (detail.value.basicInfo.status === 'COMPLETED') {
    return '订单已经完成，招募、凭证、送达、收货流程均已结束。'
  }

  const currentPhase = phaseItems.value.find((item) => item.current)
  return currentPhase ? `当前处于“${currentPhase.label}”阶段。` : '--'
})

const phaseOwnerCards = computed(() => {
  if (!detail.value) {
    return []
  }

  const flags = detail.value.actionFlags
  const status = detail.value.basicInfo.status

  if (status === 'CANCELED') {
    return [
      { key: 'owner', label: '当前负责人', value: '--' },
      { key: 'blocker', label: '当前卡点', value: '订单已取消' },
      { key: 'next', label: '下一步', value: '无需继续推进' },
    ]
  }

  if (status === 'COMPLETED') {
    return [
      { key: 'owner', label: '当前负责人', value: '--' },
      { key: 'blocker', label: '当前卡点', value: '无阻塞' },
      { key: 'next', label: '下一步', value: '订单流程已完成' },
    ]
  }

  if (flags.canPay || activeMembersNeedPay.value > 0) {
    return [
      { key: 'owner', label: '当前负责人', value: flags.canPay ? '当前账号' : '待支付成员' },
      { key: 'blocker', label: '当前卡点', value: '仍有成员未支付，无法稳定推进成团' },
      { key: 'next', label: '下一步', value: '完成支付并等待订单进入成团状态' },
    ]
  }

  if (flags.canUploadReceipt) {
    return [
      { key: 'owner', label: '当前负责人', value: '发起人' },
      { key: 'blocker', label: '当前卡点', value: '尚未上传购买凭证' },
      { key: 'next', label: '下一步', value: '上传凭证后进入待确认送达阶段' },
    ]
  }

  if (flags.canMarkDelivered) {
    return [
      { key: 'owner', label: '当前负责人', value: '发起人' },
      { key: 'blocker', label: '当前卡点', value: '尚未确认送达' },
      { key: 'next', label: '下一步', value: '确认送达后成员即可确认收货' },
    ]
  }

  if (flags.canConfirmReceived || activeMembersWaitingReceive.value > 0) {
    return [
      {
        key: 'owner',
        label: '当前负责人',
        value: flags.canConfirmReceived ? '当前账号' : '待收货成员',
      },
      { key: 'blocker', label: '当前卡点', value: '仍有成员待确认收货' },
      { key: 'next', label: '下一步', value: '完成收货确认并等待订单结束' },
    ]
  }

  if (detail.value.complaintInfo.complaintOpened) {
    return [
      { key: 'owner', label: '当前负责人', value: '投诉相关方' },
      { key: 'blocker', label: '当前卡点', value: '投诉通道已开启，需关注异常处理' },
      { key: 'next', label: '下一步', value: '根据投诉处理结果继续跟进订单状态' },
    ]
  }

  return [
    { key: 'owner', label: '当前负责人', value: '系统流程' },
    { key: 'blocker', label: '当前卡点', value: '暂无明显阻塞' },
    { key: 'next', label: '下一步', value: '按当前阶段继续推进' },
  ]
})

const getBlockedReason = (actionKey) => {
  if (!detail.value) {
    return '--'
  }

  const flags = detail.value.actionFlags
  const status = detail.value.basicInfo.status
  const currentMember = detail.value.currentUserMember
  const viewerRole = detail.value.viewerRoleInOrder
  const memberCount = detail.value.basicInfo.currentMemberCount
  const totalMemberCount = detail.value.basicInfo.totalMemberCount

  if (flags[actionKey]) {
    return '当前可直接执行'
  }

  if (viewerRole === 'ADMIN') {
    return '管理员视角仅查看详情，不参与用户侧动作'
  }

  if (actionKey === 'canJoin') {
    if (currentMember?.joinStatus === 'ACTIVE') {
      return '当前账号已经加入该订单'
    }

    if (currentMember?.joinStatus === 'EXITED') {
      return '当前账号已退出该订单，不再处于可加入状态'
    }

    if (status !== 'OPEN') {
      return '只有招募中的订单才允许加入'
    }

    if (memberCount >= totalMemberCount) {
      return '当前订单名额已满'
    }

    return '当前账号暂不满足加入条件'
  }

  if (actionKey === 'canPay') {
    if (!currentMember) {
      return '需要先加入订单后才能支付'
    }

    if (currentMember.joinStatus !== 'ACTIVE') {
      return '只有有效成员才能执行支付'
    }

    if (currentMember.payStatus === 'PAID') {
      return '当前账号已经完成支付'
    }

    return status === 'OPEN' ? '当前账号暂不满足支付条件' : '支付动作只在招募阶段开放'
  }

  if (actionKey === 'canExit') {
    if (!currentMember) {
      return '当前账号尚未加入订单'
    }

    if (currentMember.myRole === 'INITIATOR') {
      return '发起人不能直接退出自己的拼单'
    }

    if (currentMember.joinStatus === 'EXITED') {
      return '当前账号已经退出过该订单'
    }

    return status === 'OPEN' ? '当前账号暂不满足退出条件' : '退出动作只在招募阶段开放'
  }

  if (actionKey === 'canUploadReceipt') {
    if (detail.value.viewerRoleInOrder !== 'INITIATOR') {
      return '只有发起人才能上传购买凭证'
    }

    return status === 'GROUPED' ? '当前账号暂不满足上传条件' : '上传凭证只在已成团阶段开放'
  }

  if (actionKey === 'canMarkDelivered') {
    if (detail.value.viewerRoleInOrder !== 'INITIATOR') {
      return '只有发起人才能确认送达'
    }

    return status === 'WAIT_DELIVERY' ? '当前账号暂不满足确认条件' : '确认送达只在待送达阶段开放'
  }

  if (actionKey === 'canConfirmReceived') {
    if (!currentMember) {
      return '需要先加入订单后才可能确认收货'
    }

    if (currentMember.receiveStatus === 'RECEIVED' || currentMember.receiveStatus === 'AUTO_RECEIVED') {
      return '当前账号已经完成收货'
    }

    return status === 'WAIT_RECEIVE' ? '当前账号暂不满足收货确认条件' : '确认收货只在待收货阶段开放'
  }

  if (actionKey === 'canCreateComplaint') {
    if (detail.value.complaintInfo.myComplaintId) {
      return '当前账号已经提交过投诉'
    }

    if (detail.value.viewerRoleInOrder === 'INITIATOR') {
      return '发起人不能以当前身份对本单发起投诉'
    }

    return detail.value.complaintInfo.complaintOpened
      ? '投诉通道虽已开放，但当前账号此时不满足创建条件'
      : '投诉通道尚未开放'
  }

  return '当前不满足执行条件'
}

const viewerPerspectiveCards = computed(() => {
  if (!detail.value) {
    return []
  }

  const enabledActions = actionSummary.value
    .filter((item) => item.enabled)
    .map((item) => item.label)
  const roleText = formatRole(detail.value.viewerRoleInOrder)
  const currentMember = detail.value.currentUserMember

  let permissionSummary = enabledActions.length
    ? `当前可执行：${enabledActions.join('、')}`
    : '当前没有可直接执行的用户侧动作'

  if (detail.value.viewerRoleInOrder === 'ADMIN') {
    permissionSummary = '当前为管理员视角，仅查看聚合详情，不参与用户侧流程'
  }

  let blockerSummary = '暂无明显限制'

  if (detail.value.basicInfo.status === 'CANCELED') {
    blockerSummary = '订单已取消，当前账号不再推进后续流程'
  } else if (detail.value.basicInfo.status === 'COMPLETED') {
    blockerSummary = '订单已完成，当前账号无须继续操作'
  } else if (!enabledActions.length) {
    blockerSummary = getBlockedReason('canJoin')
  } else if (detail.value.actionFlags.canPay) {
    blockerSummary = '当前账号未支付，支付完成后才能继续推进订单'
  } else if (detail.value.actionFlags.canUploadReceipt) {
    blockerSummary = '当前轮到发起人上传购买凭证'
  } else if (detail.value.actionFlags.canMarkDelivered) {
    blockerSummary = '当前轮到发起人确认送达'
  } else if (detail.value.actionFlags.canConfirmReceived) {
    blockerSummary = '当前轮到当前账号确认收货'
  } else if (detail.value.actionFlags.canCreateComplaint) {
    blockerSummary = '当前可以针对异常订单发起投诉'
  } else if (currentMember?.joinStatus === 'EXITED') {
    blockerSummary = '当前账号已经退出该订单'
  }

  return [
    { key: 'identity', label: '当前身份', value: roleText },
    {
      key: 'member',
      label: '成员归属',
      value: currentMember ? `${formatRole(currentMember.myRole)} / ${formatJoinStatus(currentMember.joinStatus)}` : '尚未加入订单',
    },
    { key: 'permission', label: '当前权限', value: permissionSummary },
    { key: 'blocker', label: '当前限制', value: blockerSummary },
  ]
})

const unavailableActionCards = computed(() => {
  if (!detail.value) {
    return []
  }

  return [
    { key: 'join', label: '为什么现在不能加入', enabled: detail.value.actionFlags.canJoin, reason: getBlockedReason('canJoin') },
    { key: 'pay', label: '为什么现在不能支付', enabled: detail.value.actionFlags.canPay, reason: getBlockedReason('canPay') },
    { key: 'upload', label: '为什么现在不能上传凭证', enabled: detail.value.actionFlags.canUploadReceipt, reason: getBlockedReason('canUploadReceipt') },
    { key: 'receive', label: '为什么现在不能确认收货', enabled: detail.value.actionFlags.canConfirmReceived, reason: getBlockedReason('canConfirmReceived') },
    { key: 'complaint', label: '为什么现在不能投诉', enabled: detail.value.actionFlags.canCreateComplaint, reason: getBlockedReason('canCreateComplaint') },
  ].filter((item) => !item.enabled)
})

const stats = computed(() => {
  if (!detail.value) {
    return []
  }

  return [
    {
      hint: '来自 basicInfo.status',
      label: '订单状态',
      value: formatOrderStatus(detail.value.basicInfo.status),
    },
    {
      hint: '基于 actionFlags 汇总',
      label: '可执行动作',
      value: Object.values(detail.value.actionFlags).filter(Boolean).length,
    },
    {
      hint: '仅统计 ACTIVE 成员',
      label: '支付进度',
      value: `${paidMemberCount.value}/${activeMemberList.value.length || 0}`,
    },
    {
      hint: '当前成员数 / 目标成员数',
      label: '成员进度',
      value: `${detail.value.basicInfo.currentMemberCount}/${detail.value.basicInfo.totalMemberCount}`,
    },
  ]
})

const actionSummary = computed(() => {
  if (!detail.value) {
    return []
  }

  const flags = detail.value.actionFlags

  return [
    { key: 'join', label: '可加入', enabled: flags.canJoin },
    { key: 'exit', label: '可退出', enabled: flags.canExit },
    { key: 'pay', label: '可支付', enabled: flags.canPay },
    { key: 'upload', label: '可上传凭证', enabled: flags.canUploadReceipt },
    { key: 'delivered', label: '可确认送达', enabled: flags.canMarkDelivered },
    { key: 'received', label: '可确认收货', enabled: flags.canConfirmReceived },
    { key: 'complaint', label: '可投诉', enabled: flags.canCreateComplaint },
    { key: 'receipt', label: '可查看凭证', enabled: flags.canViewReceipt },
  ]
})

const loadDetail = async (orderId = currentOrderId.value) => {
  if (!isValidOrderId.value) {
    orderStore.detail = null
    orderStore.detailError = '当前路由中的订单 ID 无效，请返回大厅重新进入详情页。'
    return
  }

  try {
    await orderStore.loadOrderDetail(orderId)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const confirmAction = async (action) => {
  const confirmMap = {
    delivered: '确认已送达后，订单会进入待收货阶段，是否继续？',
    exit: '退出拼单后将失去当前成员资格，是否继续？',
    join: '确认加入该拼单吗？',
    pay: '确认将当前账号标记为已支付吗？',
    received: '确认已经收货吗？',
  }

  if (!confirmMap[action]) {
    return true
  }

  try {
    await ElMessageBox.confirm(confirmMap[action], '操作确认', {
      cancelButtonText: '取消',
      confirmButtonText: '确认',
      type: 'warning',
    })
    return true
  } catch {
    return false
  }
}

const promptReceiptAmount = async () => {
  const { value } = await ElMessageBox.prompt('请输入本次拼单的实际总金额', '上传凭证', {
    cancelButtonText: '取消',
    confirmButtonText: '提交',
    inputPattern: /^(0|[1-9]\d*)(\.\d{1,2})?$/,
    inputPlaceholder: '例如 54.00',
    inputType: 'number',
    inputValidator: (inputValue) => {
      if (!inputValue) {
        return '请输入实际总金额'
      }

      if (Number(inputValue) <= 0) {
        return '金额必须大于 0'
      }

      return true
    },
  })

  return {
    actualTotalAmount: Number(value),
  }
}

const openReceiptDialog = () => {
  if (!detail.value?.receiptInfo?.receiptImageUrl) {
    ElMessage.warning('当前订单暂无凭证图片')
    return
  }

  activeReceiptUrl.value = detail.value.receiptInfo.receiptImageUrl
  receiptDialogVisible.value = true
}

const navigateToComplaint = () => {
  if (!detail.value) {
    return
  }

  if (detail.value.actionFlags.canCreateComplaint) {
    router.push(`/complaints/create?orderId=${detail.value.basicInfo.orderId}`)
    return
  }

  if (detail.value.complaintInfo.myComplaintId) {
    router.push(`/complaints/${detail.value.complaintInfo.myComplaintId}`)
  }
}

const runAction = async (action) => {
  if (!currentOrderId.value) {
    return
  }

  try {
    let payload = {}

    if (action === 'viewReceipt') {
      openReceiptDialog()
      return
    }

    if (!(await confirmAction(action))) {
      return
    }

    if (action === 'upload') {
      payload = await promptReceiptAmount()
    }

    await orderStore.runDetailAction(currentOrderId.value, action, payload)
    ElMessage.success('操作成功')
  } catch (error) {
    if (error === 'cancel' || error?.message === 'cancel') {
      return
    }

    ElMessage.error(error.message || '操作失败，请稍后重试')
  }
}

const runPrimaryAction = async () => {
  if (!primaryAction.value) {
    return
  }

  if (primaryAction.value.key === 'complaint') {
    navigateToComplaint()
    return
  }

  await runAction(primaryAction.value.key)
}

watch(
  () => route.params.orderId,
  (orderId, previousOrderId) => {
    if (orderId && orderId !== previousOrderId) {
      loadDetail(orderId)
    }
  },
)

onMounted(() => {
  loadDetail()
})
</script>

<template>
  <div class="stack-page">
    <div class="stats-grid">
      <StatCard
        v-for="item in stats"
        :key="item.label"
        :label="item.label"
        :value="item.value"
        :hint="item.hint"
      />
    </div>

    <PageSection
      v-loading="orderStore.detailLoading"
      title="拼单详情"
      description="当前页面按聚合详情结构展示，按钮可用性完全以 actionFlags 为准。"
    >
      <template v-if="detail">
        <div class="card-header-row">
          <div>
            <p class="section-kicker">{{ detail.basicInfo.orderNo }}</p>
            <h2>{{ detail.basicInfo.productName }}</h2>
          </div>
          <StatusTag
            :value="detail.basicInfo.status"
            :text="formatOrderStatus(detail.basicInfo.status)"
          />
        </div>

        <div class="surface-card detail-panel">
          <h3>当前引导</h3>
          <ul class="detail-list">
            <li><span>下一步建议</span><strong>{{ nextStepHint }}</strong></li>
            <li><span>投诉状态</span><strong>{{ complaintActionText }}</strong></li>
            <li><span>凭证状态</span><strong>{{ receiptStatusText }}</strong></li>
          </ul>
          <div v-if="primaryAction" class="page-actions">
            <el-button
              :type="primaryAction.type"
              :plain="primaryAction.type !== 'primary'"
              :loading="orderStore.submitting"
              @click="runPrimaryAction"
            >
              {{ primaryAction.label }}
            </el-button>
          </div>
        </div>

        <div class="detail-grid">
          <div class="surface-card detail-panel">
            <h3>基础信息</h3>
            <ul class="detail-list">
              <li><span>取货点</span><strong>{{ detail.basicInfo.pickupPoint }}</strong></li>
              <li><span>截止时间</span><strong>{{ formatDateTime(detail.basicInfo.deadlineAt) }}</strong></li>
              <li><span>发起人</span><strong>{{ detail.initiatorInfo.nickname }}</strong></li>
              <li><span>联系方式</span><strong>{{ detail.initiatorInfo.phoneMasked || '--' }}</strong></li>
              <li><span>学号</span><strong>{{ detail.initiatorInfo.studentNoMasked || '--' }}</strong></li>
              <li><span>查看身份</span><strong>{{ formatRole(detail.viewerRoleInOrder) }}</strong></li>
              <li>
                <span>成员进度</span>
                <strong>{{ detail.basicInfo.currentMemberCount }}/{{ detail.basicInfo.totalMemberCount }}</strong>
              </li>
            </ul>

            <div class="detail-note">
              <span>商品备注</span>
              <p>{{ detail.basicInfo.productDesc || '暂无备注说明' }}</p>
            </div>
          </div>

          <div class="surface-card detail-panel">
            <h3>当前成员信息</h3>
            <ul v-if="detail.currentUserMember" class="detail-list">
              <li><span>我的角色</span><strong>{{ formatRole(detail.currentUserMember.myRole) }}</strong></li>
              <li>
                <span>加入状态</span>
                <StatusTag
                  :value="detail.currentUserMember.joinStatus"
                  :text="formatJoinStatus(detail.currentUserMember.joinStatus)"
                />
              </li>
              <li>
                <span>支付状态</span>
                <StatusTag
                  :value="detail.currentUserMember.payStatus"
                  :text="formatPayStatus(detail.currentUserMember.payStatus)"
                />
              </li>
              <li><span>应付金额</span><strong>{{ formatCurrency(detail.currentUserMember.payAmount) }}</strong></li>
              <li>
                <span>收货状态</span>
                <StatusTag
                  :value="detail.currentUserMember.receiveStatus"
                  :text="formatReceiveStatus(detail.currentUserMember.receiveStatus)"
                />
              </li>
              <li><span>加入时间</span><strong>{{ formatDateTime(detail.currentUserMember.joinedAt) }}</strong></li>
              <li><span>支付时间</span><strong>{{ formatDateTime(detail.currentUserMember.paidAt) }}</strong></li>
              <li><span>收货时间</span><strong>{{ formatDateTime(detail.currentUserMember.receivedAt) }}</strong></li>
              <li><span>退出时间</span><strong>{{ formatDateTime(detail.currentUserMember.exitedAt) }}</strong></li>
              <li>
                <span>退款合计</span>
                <strong>{{ formatCurrency(detail.currentUserMember.refundAmountTotal) }}</strong>
              </li>
            </ul>
            <p v-else class="muted-text">当前账号尚未加入该订单。</p>
          </div>
        </div>

        <PageSection
          title="当前账号视角"
          description="把当前身份、当前权限和当前限制说清楚，减少联调时反复猜测。"
        >
          <div class="action-summary-grid">
            <div
              v-for="item in viewerPerspectiveCards"
              :key="item.key"
              class="surface-card action-summary-card is-enabled"
            >
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </div>
          </div>
        </PageSection>

        <PageSection
          title="阶段进度"
          description="按业务阶段展示订单当前所处位置，便于快速判断还差哪一步。"
        >
          <div class="action-summary-grid">
            <div
              v-for="item in phaseItems"
              :key="item.key"
              class="surface-card action-summary-card"
              :class="{ 'is-enabled': item.done || item.current }"
            >
              <span>{{ item.label }}</span>
              <el-tag
                :type="item.done ? 'success' : item.current ? 'warning' : 'info'"
                effect="light"
                round
              >
                {{ item.done ? '已完成' : item.current ? '当前阶段' : '未开始' }}
              </el-tag>
              <strong>{{ item.hint }}</strong>
            </div>
          </div>
          <p class="muted-text">{{ phaseSummaryText }}</p>
        </PageSection>

        <PageSection
          title="阶段摘要"
          description="把当前负责人、当前卡点和下一步动作集中展示，减少来回判断。"
        >
          <div class="action-summary-grid">
            <div
              v-for="item in phaseOwnerCards"
              :key="item.key"
              class="surface-card action-summary-card is-enabled"
            >
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </div>
          </div>
        </PageSection>

        <PageSection
          title="动作概览"
          description="详情页只消费契约中的 actionFlags，便于后续从 mock 切换到真实接口。"
        >
          <div class="action-summary-grid">
            <div
              v-for="item in actionSummary"
              :key="item.key"
              class="surface-card action-summary-card"
              :class="{ 'is-enabled': item.enabled }"
            >
              <span>{{ item.label }}</span>
              <strong>{{ item.enabled ? '是' : '否' }}</strong>
            </div>
          </div>
        </PageSection>

        <PageSection
          v-if="unavailableActionCards.length"
          title="动作限制说明"
          description="说明当前为什么做不了某些动作，避免把禁用态误解成前端错误。"
        >
          <div class="action-summary-grid">
            <div
              v-for="item in unavailableActionCards"
              :key="item.key"
              class="surface-card action-summary-card"
            >
              <span>{{ item.label }}</span>
              <strong>{{ item.reason }}</strong>
            </div>
          </div>
        </PageSection>

        <PageSection
          title="当前风险"
          description="把支付、凭证、送达、投诉这些阻塞点集中展示，方便联调和演示。"
        >
          <div class="action-summary-grid">
            <div
              v-for="item in riskAlerts"
              :key="item.key"
              class="surface-card action-summary-card is-enabled"
            >
              <el-tag :type="riskTagTypeMap[item.tone] || 'info'" effect="light" round>
                {{ formatRiskToneText(item.tone) }}
              </el-tag>
              <strong>{{ item.text }}</strong>
            </div>
          </div>
        </PageSection>

        <PageSection
          title="关键时限"
          description="把当前阶段的截止时间集中展示，便于判断是否超时或已完成。"
        >
          <div class="action-summary-grid">
            <div
              v-for="item in milestoneItems"
              :key="item.key"
              class="surface-card action-summary-card is-enabled"
            >
              <span>{{ item.label }}</span>
              <el-tag :type="milestoneTagTypeMap[item.tag] || 'info'" effect="light" round>
                {{ item.statusText }}
              </el-tag>
              <strong>{{ item.timeText }}</strong>
            </div>
          </div>
        </PageSection>

        <PageSection
          title="成员列表"
          description="成员信息展示加入、支付、收货及最近事件，方便联调时核对状态流转。"
        >
          <el-table :data="detail.memberList" stripe>
            <el-table-column prop="nickname" label="成员" />
            <el-table-column label="角色">
              <template #default="{ row }">{{ formatRole(row.role) }}</template>
            </el-table-column>
            <el-table-column label="加入状态">
              <template #default="{ row }">
                <StatusTag :value="row.joinStatus" :text="formatJoinStatus(row.joinStatus)" />
              </template>
            </el-table-column>
            <el-table-column label="支付状态">
              <template #default="{ row }">
                <StatusTag :value="row.payStatus" :text="formatPayStatus(row.payStatus)" />
              </template>
            </el-table-column>
            <el-table-column label="收货状态">
              <template #default="{ row }">
                <StatusTag :value="row.receiveStatus" :text="formatReceiveStatus(row.receiveStatus)" />
              </template>
            </el-table-column>
            <el-table-column label="应付金额">
              <template #default="{ row }">{{ formatCurrency(row.payAmount) }}</template>
            </el-table-column>
            <el-table-column label="退款合计">
              <template #default="{ row }">{{ formatCurrency(row.refundAmountTotal) }}</template>
            </el-table-column>
            <el-table-column label="最近事件">
              <template #default="{ row }">{{ getMemberLatestEvent(row) }}</template>
            </el-table-column>
          </el-table>
        </PageSection>

        <div class="detail-grid">
          <PageSection title="支付汇总" description="对应 paymentSummary 聚合字段。">
            <ul class="detail-list">
              <li>
                <span>预计总额</span>
                <strong>{{ formatCurrency(detail.paymentSummary.estimatedTotalAmount) }}</strong>
              </li>
              <li>
                <span>实付总额</span>
                <strong>{{ formatCurrency(detail.paymentSummary.actualTotalAmount) }}</strong>
              </li>
              <li>
                <span>已支付人数</span>
                <strong>{{ detail.paymentSummary.paidMemberCount }}</strong>
              </li>
              <li>
                <span>累计支付</span>
                <strong>{{ formatCurrency(detail.paymentSummary.totalPaidAmount) }}</strong>
              </li>
              <li>
                <span>退款总额</span>
                <strong>{{ formatCurrency(detail.paymentSummary.refundAmountTotal) }}</strong>
              </li>
              <li>
                <span>与预估差额</span>
                <strong>{{ paymentDeltaText }}</strong>
              </li>
            </ul>
          </PageSection>

          <PageSection title="投诉与凭证" description="对应 complaintInfo 与 receiptInfo。">
            <ul class="detail-list">
              <li>
                <span>投诉通道</span>
                <strong>{{ detail.complaintInfo.complaintOpened ? '已开放' : '未开放' }}</strong>
              </li>
              <li><span>投诉数量</span><strong>{{ detail.complaintInfo.complaintCount }}</strong></li>
              <li>
                <span>我的投诉状态</span>
                <strong>
                  {{ detail.complaintInfo.myComplaintStatus ? formatComplaintStatus(detail.complaintInfo.myComplaintStatus) : '--' }}
                </strong>
              </li>
              <li><span>我的投诉单</span><strong>{{ detail.complaintInfo.myComplaintId || '--' }}</strong></li>
              <li><span>投诉引导</span><strong>{{ complaintActionText }}</strong></li>
              <li>
                <span>凭证金额</span>
                <strong>{{ detail.receiptInfo ? formatCurrency(detail.receiptInfo.actualTotalAmount) : '--' }}</strong>
              </li>
              <li>
                <span>上传截止</span>
                <strong>{{ formatDateTime(detail.basicInfo.receiptUploadDeadlineAt) }}</strong>
              </li>
              <li>
                <span>上传时间</span>
                <strong>{{ detail.receiptInfo ? formatDateTime(detail.receiptInfo.uploadedAt) : '--' }}</strong>
              </li>
            </ul>
          </PageSection>
        </div>

        <div class="detail-grid">
          <PageSection title="收货信息" description="对应 receiveInfo 聚合字段。">
            <ul class="detail-list">
              <li>
                <span>送达时间</span>
                <strong>{{ formatDateTime(detail.receiveInfo.deliveredAt) }}</strong>
              </li>
              <li>
                <span>自动确认时间</span>
                <strong>{{ formatDateTime(detail.receiveInfo.autoConfirmDeadlineAt) }}</strong>
              </li>
              <li>
                <span>预计送达截止</span>
                <strong>{{ formatDateTime(detail.basicInfo.expectedDeliveryEndAt) }}</strong>
              </li>
              <li>
                <span>当前收货进度</span>
                <strong>{{ receivedMemberCount }}/{{ activeMemberList.length || 0 }}</strong>
              </li>
            </ul>

            <el-table :data="detail.receiveInfo.receiveStatusSummary" stripe>
              <el-table-column prop="nickname" label="成员" />
              <el-table-column label="收货状态">
                <template #default="{ row }">
                  <StatusTag :value="row.receiveStatus" :text="formatReceiveStatus(row.receiveStatus)" />
                </template>
              </el-table-column>
              <el-table-column label="收货时间">
                <template #default="{ row }">{{ formatDateTime(row.receivedAt) }}</template>
              </el-table-column>
            </el-table>
          </PageSection>

          <PageSection title="时间线" description="展示订单内关键事件的时间顺序。">
            <el-timeline>
              <el-timeline-item
                v-for="item in detail.timeline"
                :key="`${item.action}-${item.at}`"
                :timestamp="formatDateTime(item.at)"
                :type="getTimelineType(item.action)"
              >
                <strong>{{ formatTimelineAction(item.action) }}</strong>
                <div>{{ item.description }}</div>
              </el-timeline-item>
            </el-timeline>
          </PageSection>
        </div>

        <div class="page-actions wrap-actions">
          <el-button @click="router.push('/orders')">返回大厅</el-button>
          <el-button plain @click="router.push('/my-orders')">我的拼单</el-button>
          <el-button plain :loading="orderStore.detailLoading" @click="loadDetail()">刷新详情</el-button>
          <el-button
            v-if="detail.actionFlags.canJoin"
            type="primary"
            :loading="orderStore.submitting"
            @click="runAction('join')"
          >
            加入拼单
          </el-button>
          <el-button
            v-if="detail.actionFlags.canExit"
            type="warning"
            plain
            :loading="orderStore.submitting"
            @click="runAction('exit')"
          >
            退出拼单
          </el-button>
          <el-button
            v-if="detail.actionFlags.canPay"
            type="primary"
            :loading="orderStore.submitting"
            @click="runAction('pay')"
          >
            立即支付
          </el-button>
          <el-button
            v-if="detail.actionFlags.canUploadReceipt"
            type="primary"
            plain
            :loading="orderStore.submitting"
            @click="runAction('upload')"
          >
            上传凭证
          </el-button>
          <el-button
            v-if="detail.actionFlags.canViewReceipt"
            plain
            :disabled="!detail.receiptInfo"
            @click="runAction('viewReceipt')"
          >
            查看凭证
          </el-button>
          <el-button
            v-if="detail.actionFlags.canMarkDelivered"
            type="success"
            plain
            :loading="orderStore.submitting"
            @click="runAction('delivered')"
          >
            确认送达
          </el-button>
          <el-button
            v-if="detail.actionFlags.canConfirmReceived"
            type="success"
            :loading="orderStore.submitting"
            @click="runAction('received')"
          >
            确认收货
          </el-button>
          <el-button
            v-if="detail.actionFlags.canCreateComplaint"
            type="danger"
            plain
            @click="navigateToComplaint"
          >
            发起投诉
          </el-button>
          <el-button
            v-else-if="detail.complaintInfo.myComplaintId"
            type="danger"
            plain
            @click="navigateToComplaint"
          >
            查看投诉
          </el-button>
        </div>
      </template>

      <EmptyState
        v-else-if="!orderStore.detailLoading"
        title="订单详情不可用"
        :description="detailErrorText"
      >
        <div class="page-actions">
          <el-button @click="router.push('/orders')">返回大厅</el-button>
          <el-button type="primary" plain :disabled="!isValidOrderId" @click="loadDetail()">
            重新加载
          </el-button>
        </div>
      </EmptyState>
    </PageSection>

    <el-dialog v-model="receiptDialogVisible" title="订单凭证" width="680px">
      <div class="receipt-preview">
        <ElImage
          v-if="activeReceiptUrl"
          :preview-src-list="[activeReceiptUrl]"
          :src="activeReceiptUrl"
          fit="contain"
        />
      </div>
    </el-dialog>
  </div>
</template>
