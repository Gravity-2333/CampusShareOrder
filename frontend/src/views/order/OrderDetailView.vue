<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElImage, ElMessage, ElMessageBox } from 'element-plus'

import EmptyState from '../../components/common/EmptyState.vue'
import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useOrderStore } from '../../stores/order'
import {
  formatCurrency,
  formatDateTime,
  formatJoinStatus,
  formatOptionalCurrency,
  formatOrderStatus,
  formatPayStatus,
  formatReceiveStatus,
  formatRole,
} from '../../utils/format'
import { validateApiDateTime } from '../../utils/validate'

const route = useRoute()
const router = useRouter()
const orderStore = useOrderStore()

const receiptDialogVisible = ref(false)
const activeReceiptUrl = ref('')
const now = ref(Date.now())
let countdownTimer = null

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

const deadlineRemainingText = computed(() => {
  const deadlineAt = detail.value?.basicInfo?.deadlineAt
  if (!deadlineAt) {
    return '--'
  }

  if (detail.value?.basicInfo?.status !== 'OPEN') {
    return '已结束'
  }

  const remainingMs = new Date(String(deadlineAt).replace(' ', 'T')).getTime() - now.value
  if (Number.isNaN(remainingMs) || remainingMs <= 0) {
    return '已截止'
  }

  const totalSeconds = Math.floor(remainingMs / 1000)
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60

  if (hours > 0) {
    return `${hours}小时${String(minutes).padStart(2, '0')}分${String(seconds).padStart(2, '0')}秒`
  }

  return `${minutes}分${String(seconds).padStart(2, '0')}秒`
})

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

const stats = computed(() => {
  if (!detail.value) {
    return []
  }

  return [
    {
      hint: '当前拼单所处阶段',
      label: '订单状态',
      value: formatOrderStatus(detail.value.basicInfo.status),
    },
    {
      hint: '根据当前订单状态汇总',
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
    {
      hint: '招募截止倒计时',
      label: '剩余时间',
      value: deadlineRemainingText.value,
    },
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

const toApiDateTime = (value) => String(value || '').trim()
const parseApiDateTime = (value) => new Date(String(value || '').trim().replace(' ', 'T')).getTime()

const promptReceiptPayload = async () => {
  const { value: imageUrl } = await ElMessageBox.prompt('请输入凭证图片地址', '上传凭证', {
    cancelButtonText: '取消',
    confirmButtonText: '下一步',
    inputPlaceholder: '例如 https://example.com/receipt.jpg',
    inputValidator: (inputValue) => {
      if (!inputValue?.trim()) {
        return '请输入凭证图片地址'
      }

      if (inputValue.trim().length > 500) {
        return '凭证图片地址长度不能超过 500 个字符'
      }

      return true
    },
  })

  const { value: amount } = await ElMessageBox.prompt('请输入本次拼单的实际总金额', '上传凭证', {
    cancelButtonText: '取消',
    confirmButtonText: '下一步',
    inputPattern: /^(0|[1-9]\d*)(\.\d{1,2})?$/,
    inputPlaceholder: '例如 54.00',
    inputType: 'number',
    inputValidator: (inputValue) => {
      const normalizedValue = String(inputValue ?? '').trim()

      if (!normalizedValue) {
        return '请输入实际总金额'
      }

      const actualAmount = Number(normalizedValue)
      if (!Number.isFinite(actualAmount) || actualAmount <= 0) {
        return '金额必须大于 0'
      }

      return true
    },
  })

  const { value: startAt } = await ElMessageBox.prompt('请输入预计开始送达时间', '上传凭证', {
    cancelButtonText: '取消',
    confirmButtonText: '下一步',
    inputPlaceholder: 'yyyy-MM-dd HH:mm:ss',
    inputValidator: (inputValue) => {
      return validateApiDateTime(inputValue, '预计开始送达时间格式应为 yyyy-MM-dd HH:mm:ss') || true
    },
  })

  const { value: endAt } = await ElMessageBox.prompt('请输入预计最晚送达时间', '上传凭证', {
    cancelButtonText: '取消',
    confirmButtonText: '提交',
    inputPlaceholder: 'yyyy-MM-dd HH:mm:ss',
    inputValidator: (inputValue) => {
      const formatError = validateApiDateTime(inputValue, '预计最晚送达时间格式应为 yyyy-MM-dd HH:mm:ss')
      if (formatError) {
        return formatError
      }

      return parseApiDateTime(inputValue) > parseApiDateTime(startAt) || '预计最晚送达时间必须晚于开始送达时间'
    },
  })

  return {
    actualTotalAmount: Number(String(amount).trim()),
    expectedDeliveryEndAt: toApiDateTime(endAt),
    expectedDeliveryStartAt: toApiDateTime(startAt),
    imageUrl: imageUrl.trim(),
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
  if (orderStore.submitting && action !== 'viewReceipt') {
    return
  }

  if (!isValidOrderId.value) {
    ElMessage.warning('当前订单 ID 无效，请返回大厅重新进入详情页')
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
      payload = await promptReceiptPayload()
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
  countdownTimer = window.setInterval(() => {
    now.value = Date.now()
  }, 1000)
  loadDetail()
})

onBeforeUnmount(() => {
  if (countdownTimer) {
    window.clearInterval(countdownTimer)
  }
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
      description="查看订单状态、成员进度、支付凭证、收货确认和异常处理信息。"
    >
      <template v-if="detail">
        <div class="card-header-row">
          <div>
            <p class="section-kicker">
              {{ detail.basicInfo.orderNo }}
            </p>
            <h2>{{ detail.basicInfo.productName }}</h2>
          </div>
          <StatusTag
            :value="detail.basicInfo.status"
            :text="formatOrderStatus(detail.basicInfo.status)"
          />
        </div>

        <div class="surface-card detail-panel detail-hero-panel">
          <h3>订单概况</h3>
          <ul class="detail-list">
            <li><span>投诉状态</span><strong>{{ complaintActionText }}</strong></li>
            <li><span>凭证状态</span><strong>{{ receiptStatusText }}</strong></li>
          </ul>
          <div
            v-if="primaryAction"
            class="page-actions detail-primary-actions"
          >
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
            <ul
              v-if="detail.currentUserMember"
              class="detail-list"
            >
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
            <p
              v-else
              class="muted-text"
            >
              当前账号尚未加入该订单。
            </p>
          </div>
        </div>

        <PageSection
          title="成员列表"
          description="查看成员加入、支付、收货和最近事件，快速判断当前进展。"
        >
          <div class="desktop-table">
            <el-table
              :data="detail.memberList"
              stripe
            >
              <el-table-column
                prop="nickname"
                label="成员"
              />
              <el-table-column label="角色">
                <template #default="{ row }">
                  <div class="member-role-cell">
                    <span>{{ formatRole(row.role) }}</span>
                    <el-tag
                      v-if="row.isCreator"
                      size="small"
                      type="warning"
                      effect="light"
                    >
                      团长
                    </el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="加入状态">
                <template #default="{ row }">
                  <StatusTag
                    :value="row.joinStatus"
                    :text="formatJoinStatus(row.joinStatus)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="支付状态">
                <template #default="{ row }">
                  <StatusTag
                    :value="row.payStatus"
                    :text="formatPayStatus(row.payStatus)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="收货状态">
                <template #default="{ row }">
                  <StatusTag
                    :value="row.receiveStatus"
                    :text="formatReceiveStatus(row.receiveStatus)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="应付金额">
                <template #default="{ row }">
                  {{ formatCurrency(row.payAmount) }}
                </template>
              </el-table-column>
              <el-table-column label="退款合计">
                <template #default="{ row }">
                  {{ formatCurrency(row.refundAmountTotal) }}
                </template>
              </el-table-column>
              <el-table-column label="最近事件">
                <template #default="{ row }">
                  {{ getMemberLatestEvent(row) }}
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="mobile-record-list">
            <article
              v-for="(row, index) in detail.memberList"
              :key="`${row.nickname}-${index}`"
              class="surface-card mobile-record-card"
            >
              <div class="mobile-record-header">
                <div class="mobile-record-title">
                  <span>
                    {{ formatRole(row.role) }}
                    <el-tag
                      v-if="row.isCreator"
                      size="small"
                      type="warning"
                      effect="light"
                    >
                      团长
                    </el-tag>
                  </span>
                  <strong>{{ row.nickname || '--' }}</strong>
                </div>
                <StatusTag
                  :value="row.payStatus"
                  :text="formatPayStatus(row.payStatus)"
                />
              </div>
              <ul class="mobile-record-fields">
                <li><span>加入状态</span><strong>{{ formatJoinStatus(row.joinStatus) }}</strong></li>
                <li><span>收货状态</span><strong>{{ formatReceiveStatus(row.receiveStatus) }}</strong></li>
                <li><span>应付金额</span><strong>{{ formatCurrency(row.payAmount) }}</strong></li>
                <li><span>退款合计</span><strong>{{ formatCurrency(row.refundAmountTotal) }}</strong></li>
                <li><span>最近事件</span><strong>{{ getMemberLatestEvent(row) }}</strong></li>
              </ul>
            </article>
          </div>
        </PageSection>

        <PageSection
          title="补充信息"
          description="保留金额、凭证、收货和投诉的关键字段。"
        >
          <ul class="detail-list compact-detail-list">
            <li>
              <span>预计 / 实付</span>
              <strong>
                {{ formatCurrency(detail.paymentSummary.estimatedTotalAmount) }} /
                {{ formatOptionalCurrency(detail.paymentSummary.actualTotalAmount) }}
              </strong>
            </li>
            <li>
              <span>支付 / 收货</span>
              <strong>
                {{ detail.paymentSummary.paidMemberCount }}/{{ activeMemberList.length || 0 }} /
                {{ receivedMemberCount }}/{{ activeMemberList.length || 0 }}
              </strong>
            </li>
            <li>
              <span>退款 / 差额</span>
              <strong>
                {{ formatCurrency(detail.paymentSummary.refundAmountTotal) }} / {{ paymentDeltaText }}
              </strong>
            </li>
            <li>
              <span>凭证</span>
              <strong>
                {{ detail.receiptInfo ? formatOptionalCurrency(detail.receiptInfo.actualTotalAmount) : '--' }}
                · {{ detail.receiptInfo ? formatDateTime(detail.receiptInfo.uploadedAt) : formatDateTime(detail.basicInfo.receiptUploadDeadlineAt) }}
              </strong>
            </li>
            <li>
              <span>送达 / 自动确认</span>
              <strong>
                {{ formatDateTime(detail.receiveInfo.deliveredAt) }} /
                {{ formatDateTime(detail.receiveInfo.autoConfirmDeadlineAt) }}
              </strong>
            </li>
            <li>
              <span>投诉</span>
              <strong>
                {{ detail.complaintInfo.complaintOpened ? '已开放' : '未开放' }} ·
                {{ detail.complaintInfo.complaintCount }} 条 · {{ complaintActionText }}
              </strong>
            </li>
          </ul>
        </PageSection>

        <div class="detail-grid">
          <PageSection
            title="收货明细"
            description="保留每个成员的收货确认结果。"
          >
            <div class="desktop-table">
              <el-table
                :data="detail.receiveInfo.receiveStatusSummary"
                stripe
              >
                <el-table-column
                  prop="nickname"
                  label="成员"
                />
                <el-table-column label="收货状态">
                  <template #default="{ row }">
                    <StatusTag
                      :value="row.receiveStatus"
                      :text="formatReceiveStatus(row.receiveStatus)"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="收货时间">
                  <template #default="{ row }">
                    {{ formatDateTime(row.receivedAt) }}
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div class="mobile-record-list">
              <article
                v-for="(row, index) in detail.receiveInfo.receiveStatusSummary"
                :key="`${row.nickname}-${index}`"
                class="surface-card mobile-record-card"
              >
                <div class="mobile-record-header">
                  <div class="mobile-record-title">
                    <span>收货汇总</span>
                    <strong>{{ row.nickname || '--' }}</strong>
                  </div>
                  <StatusTag
                    :value="row.receiveStatus"
                    :text="formatReceiveStatus(row.receiveStatus)"
                  />
                </div>
                <ul class="mobile-record-fields">
                  <li><span>收货时间</span><strong>{{ formatDateTime(row.receivedAt) }}</strong></li>
                </ul>
              </article>
            </div>
          </PageSection>
        </div>

        <div class="page-actions wrap-actions detail-actions-bar">
          <el-button @click="router.push('/orders')">
            返回大厅
          </el-button>
          <el-button
            plain
            @click="router.push('/my-orders')"
          >
            我的拼单
          </el-button>
          <el-button
            plain
            :loading="orderStore.detailLoading"
            @click="loadDetail()"
          >
            刷新详情
          </el-button>
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
          <el-button @click="router.push('/orders')">
            返回大厅
          </el-button>
          <el-button
            type="primary"
            plain
            :disabled="!isValidOrderId"
            @click="loadDetail()"
          >
            重新加载
          </el-button>
        </div>
      </EmptyState>
    </PageSection>

    <el-dialog
      v-model="receiptDialogVisible"
      title="订单凭证"
      width="680px"
    >
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
