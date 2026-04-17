<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

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
} from '../../utils/format'

const route = useRoute()
const router = useRouter()
const orderStore = useOrderStore()
const detail = computed(() => orderStore.detail)

const stats = computed(() => {
  if (!orderStore.detail) {
    return []
  }

  return [
    {
      hint: '基础状态来自 basicInfo.status',
      label: '订单状态',
      value: formatOrderStatus(orderStore.detail.basicInfo.status),
    },
    {
      hint: '展示层优先使用 actionFlags',
      label: '可执行动作',
      value: Object.values(orderStore.detail.actionFlags).filter(Boolean).length,
    },
    {
      hint: '金额字段统一按契约输出 number',
      label: '预计金额',
      value: formatCurrency(orderStore.detail.basicInfo.estimatedTotalAmount),
    },
  ]
})

const loadDetail = async () => {
  try {
    await orderStore.loadOrderDetail(route.params.orderId)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const runAction = async (action) => {
  try {
    await orderStore.runDetailAction(route.params.orderId, action, { actualTotalAmount: 54 })
    ElMessage.success('操作成功')
  } catch (error) {
    ElMessage.error(error.message)
  }
}

onMounted(loadDetail)
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
              <li><span>成员进度</span><strong>{{ detail.basicInfo.currentMemberCount }}/{{ detail.basicInfo.totalMemberCount }}</strong></li>
            </ul>
          </div>

          <div class="surface-card detail-panel">
            <h3>当前成员信息</h3>
            <ul v-if="detail.currentUserMember" class="detail-list">
              <li><span>我的角色</span><strong>{{ detail.currentUserMember.myRole }}</strong></li>
              <li><span>支付状态</span><strong>{{ formatPayStatus(detail.currentUserMember.payStatus) }}</strong></li>
              <li><span>收货状态</span><strong>{{ formatReceiveStatus(detail.currentUserMember.receiveStatus) }}</strong></li>
            </ul>
            <p v-else class="muted-text">当前账号尚未加入该订单。</p>
          </div>
        </div>

        <PageSection title="成员列表" description="字段来源对齐 group_order_member + user_account 聚合。">
          <el-table :data="detail.memberList" stripe>
            <el-table-column prop="nickname" label="成员" />
            <el-table-column prop="role" label="角色" />
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
                <strong>{{ formatCurrency(detail.paymentSummary.totalPaidAmount) }}</strong>
              </li>
              <li>
                <span>已支付人数</span>
                <strong>{{ detail.paymentSummary.paidMemberCount }}</strong>
              </li>
              <li>
                <span>退款总额</span>
                <strong>{{ formatCurrency(detail.paymentSummary.refundAmountTotal) }}</strong>
              </li>
            </ul>
          </PageSection>

          <PageSection title="投诉与凭证" description="对应 complaintInfo 与 receiptInfo。">
            <ul class="detail-list">
              <li><span>投诉通道</span><strong>{{ detail.complaintInfo.complaintOpened ? '已开放' : '未开放' }}</strong></li>
              <li><span>投诉数量</span><strong>{{ detail.complaintInfo.complaintCount }}</strong></li>
              <li><span>我的投诉状态</span><strong>{{ detail.complaintInfo.myComplaintStatus || '--' }}</strong></li>
              <li><span>凭证金额</span><strong>{{ detail.receiptInfo ? formatCurrency(detail.receiptInfo.actualTotalAmount) : '--' }}</strong></li>
            </ul>
          </PageSection>
        </div>

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

        <div class="page-actions wrap-actions">
          <el-button @click="router.push('/orders')">返回大厅</el-button>
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
        </div>
      </template>
    </PageSection>
  </div>
</template>
