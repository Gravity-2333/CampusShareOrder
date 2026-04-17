<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElImage, ElMessage, ElMessageBox } from 'element-plus'

import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useOrderStore } from '../../stores/order'
import {
  formatCurrency,
  formatDateTime,
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
      hint: '金额字段统一按契约输出 number',
      label: '预计金额',
      value: formatCurrency(detail.value.basicInfo.estimatedTotalAmount),
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
  if (!orderId) {
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
              <li><span>加入状态</span><strong>{{ detail.currentUserMember.joinStatus }}</strong></li>
              <li><span>支付状态</span><strong>{{ formatPayStatus(detail.currentUserMember.payStatus) }}</strong></li>
              <li><span>收货状态</span><strong>{{ formatReceiveStatus(detail.currentUserMember.receiveStatus) }}</strong></li>
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
            <el-table-column prop="joinStatus" label="加入状态" />
            <el-table-column label="支付状态">
              <template #default="{ row }">{{ formatPayStatus(row.payStatus) }}</template>
            </el-table-column>
            <el-table-column label="收货状态">
              <template #default="{ row }">{{ formatReceiveStatus(row.receiveStatus) }}</template>
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
            </ul>
          </PageSection>

          <PageSection title="投诉与凭证" description="对应 complaintInfo 与 receiptInfo。">
            <ul class="detail-list">
              <li>
                <span>投诉通道</span>
                <strong>{{ detail.complaintInfo.complaintOpened ? '已开放' : '未开放' }}</strong>
              </li>
              <li><span>投诉数量</span><strong>{{ detail.complaintInfo.complaintCount }}</strong></li>
              <li><span>我的投诉状态</span><strong>{{ detail.complaintInfo.myComplaintStatus || '--' }}</strong></li>
              <li><span>我的投诉单</span><strong>{{ detail.complaintInfo.myComplaintId || '--' }}</strong></li>
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
            </ul>

            <el-table :data="detail.receiveInfo.receiveStatusSummary" stripe>
              <el-table-column prop="nickname" label="成员" />
              <el-table-column label="收货状态">
                <template #default="{ row }">{{ formatReceiveStatus(row.receiveStatus) }}</template>
              </el-table-column>
            </el-table>
          </PageSection>

          <PageSection title="时间线" description="对应 timeline 聚合展示。">
            <el-timeline>
              <el-timeline-item
                v-for="item in detail.timeline"
                :key="`${item.action}-${item.at}`"
                :timestamp="formatDateTime(item.at)"
              >
                {{ item.description }}
              </el-timeline-item>
            </el-timeline>
          </PageSection>
        </div>

        <div class="page-actions wrap-actions">
          <el-button @click="router.push('/orders')">返回大厅</el-button>
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
            @click="router.push(`/complaints/create?orderId=${detail.basicInfo.orderId}`)"
          >
            发起投诉
          </el-button>
          <el-button
            v-else-if="detail.complaintInfo.myComplaintId"
            type="danger"
            plain
            @click="router.push(`/complaints/${detail.complaintInfo.myComplaintId}`)"
          >
            查看投诉
          </el-button>
        </div>
      </template>
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
