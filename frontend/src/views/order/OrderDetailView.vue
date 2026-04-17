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
  () => detail.value?.paymentSummary?.actualTotalAmount !== null && detail.value?.paymentSummary?.actualTotalAmount !== undefined,
)
const activeMemberList = computed(() =>
  detail.value?.memberList?.filter((member) => member.joinStatus === 'ACTIVE') || [],
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
const complaintActionText = computed(() => {
  if (!detail.value) {
    return '--'
  }

  if (detail.value.actionFlags.canCreateComplaint) {
    return '当前可直接发起投诉'
  }

  if (detail.value.complaintInfo.myComplaintId) {
    return '当前账号已有投诉记录'
  }

  if (detail.value.complaintInfo.complaintOpened) {
    return '投诉通道已开，但当前账号不可再创建'
  }

  return '投诉通道尚未开放'
})
const receiptStatusText = computed(() => {
  if (!detail.value) {
    return '--'
  }

  if (detail.value.receiptInfo) {
    return '已上传凭证'
  }

  if (detail.value.actionFlags.canUploadReceipt) {
    return '待上传凭证'
  }

  return '暂无凭证'
})
const paymentDeltaText = computed(() => {
  if (!detail.value || !hasActualAmount.value) {
    return '--'
  }

  const estimated = Number(detail.value.paymentSummary.estimatedTotalAmount || 0)
  const actual = Number(detail.value.paymentSummary.actualTotalAmount || 0)
  const delta = estimated - actual

  return formatCurrency(delta)
})
const detailErrorText = computed(() => {
  if (!isValidOrderId.value) {
    return '当前路由里的订单 ID 无效，请返回大厅重新进入详情页。'
  }

  return orderStore.detailError || '当前未加载到订单详情数据，可以返回大厅重新进入，或手动刷新当前页面。'
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
    return '你已加入但尚未支付，完成支付后订单才可能继续推进成团。'
  }

  if (flags.canUploadReceipt) {
    return '当前已成团，下一步应由发起人上传购买凭证，进入待送达阶段。'
  }

  if (flags.canMarkDelivered) {
    return '凭证已上传，当前由发起人确认送达，随后成员可确认收货。'
  }

  if (flags.canConfirmReceived) {
    return '订单已送达，当前账号可以确认收货，推动订单走向完成。'
  }

  if (flags.canCreateComplaint) {
    return '如订单异常，可直接从当前详情页发起投诉。'
  }

  if (detail.value.complaintInfo.myComplaintId) {
    return '当前账号已经创建投诉，可直接查看投诉详情跟进处理结果。'
  }

  return '当前页面主要用于查看订单聚合信息，按钮是否可操作完全以 actionFlags 为准。'
})

const timelineToneMap = {
  COMPLAINT_CREATED: 'danger',
  INITIATOR_JOINED: 'primary',
  MEMBER_EXITED: 'info',
  MEMBER_JOINED: 'primary',
  ORDER_CREATED: 'primary',
  ORDER_DELIVERED: 'success',
  ORDER_STATUS: 'warning',
  RECEIPT_UPLOADED: 'success',
}

const getTimelineType = (action) => timelineToneMap[action] || 'info'

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

const stats = computed(() => {
  if (!detail.value) {
    return []
  }

  return [
    {
      hint: '基础状态来自 basicInfo.status',
      label: '订单状态',
      value: formatOrderStatus(detail.value.basicInfo.status),
    },
    {
      hint: '展示层优先使用 actionFlags',
      label: '可执行动作',
      value: Object.values(detail.value.actionFlags).filter(Boolean).length,
    },
    {
      hint: '仅统计 ACTIVE 成员，用于判断当前成团推进程度',
      label: '支付进度',
      value: `${paidMemberCount.value}/${activeMemberList.value.length || 0}`,
    },
    {
      hint: '成员参与情况来自 basicInfo.currentMemberCount / totalMemberCount',
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
    { key: 'upload', label: '可传凭证', enabled: flags.canUploadReceipt },
    { key: 'delivered', label: '可确认送达', enabled: flags.canMarkDelivered },
    { key: 'received', label: '可确认收货', enabled: flags.canConfirmReceived },
    { key: 'complaint', label: '可投诉', enabled: flags.canCreateComplaint },
    { key: 'receipt', label: '可查看凭证', enabled: flags.canViewReceipt },
  ]
})

const loadDetail = async (orderId = currentOrderId.value) => {
  if (!isValidOrderId.value) {
    orderStore.detail = null
    orderStore.detailError = '当前路由里的订单 ID 无效，请返回大厅重新进入详情页。'
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
    exit: '退出拼单后你将失去当前成员资格，是否继续？',
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
  const { value } = await ElMessageBox.prompt('请输入本次拼单的实付总金额', '上传凭证', {
    cancelButtonText: '取消',
    confirmButtonText: '提交',
    inputPattern: /^(0|[1-9]\d*)(\.\d{1,2})?$/,
    inputPlaceholder: '例如 54.00',
    inputType: 'number',
    inputValidator: (inputValue) => {
      if (!inputValue) {
        return '请输入实付总金额'
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
    if (error === 'cancel') {
      return
    }

    if (error?.message === 'cancel') {
      return
    }

    ElMessage.error(error.message || '操作失败，请稍后重试')
  }
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
      <StatCard v-for="item in stats" :key="item.label" :label="item.label" :value="item.value" :hint="item.hint" />
    </div>

    <PageSection
      v-loading="orderStore.detailLoading"
      title="拼单详情"
      description="按钮显示仅依赖 actionFlags，不在前端重复推导复杂状态。"
    >
      <template v-if="detail">
        <div class="card-header-row">
          <div>
            <p class="section-kicker">{{ detail.basicInfo.orderNo }}</p>
            <h2>{{ detail.basicInfo.productName }}</h2>
          </div>
          <StatusTag :value="detail.basicInfo.status" :text="formatOrderStatus(detail.basicInfo.status)" />
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
                <StatusTag :value="detail.currentUserMember.joinStatus" :text="formatJoinStatus(detail.currentUserMember.joinStatus)" />
              </li>
              <li>
                <span>支付状态</span>
                <StatusTag :value="detail.currentUserMember.payStatus" :text="formatPayStatus(detail.currentUserMember.payStatus)" />
              </li>
              <li>
                <span>收货状态</span>
                <StatusTag :value="detail.currentUserMember.receiveStatus" :text="formatReceiveStatus(detail.currentUserMember.receiveStatus)" />
              </li>
              <li>
                <span>退款合计</span>
                <strong>{{ formatCurrency(detail.currentUserMember.refundAmountTotal) }}</strong>
              </li>
            </ul>
            <p v-else class="muted-text">当前账号尚未加入该订单。</p>
          </div>
        </div>

        <PageSection title="动作概览" description="详情页只消费契约中的 actionFlags，便于后续联调替换。">
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

        <PageSection title="成员列表" description="字段来源对齐 group_order_member 与 user_account 联合展示。">
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
                <strong>{{ detail.complaintInfo.myComplaintStatus ? formatComplaintStatus(detail.complaintInfo.myComplaintStatus) : '--' }}</strong>
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
          <PageSection title="收货信息" description="对应 receiveInfo 聚合展示。">
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
            </el-table>
          </PageSection>

          <PageSection title="时间线" description="对应 timeline 聚合展示。">
            <el-timeline>
              <el-timeline-item
                v-for="item in detail.timeline"
                :key="`${item.action}-${item.at}`"
                :timestamp="formatDateTime(item.at)"
                :type="getTimelineType(item.action)"
              >
                {{ item.description }}
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
          <el-button type="primary" plain :disabled="!isValidOrderId" @click="loadDetail()">重新加载</el-button>
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
