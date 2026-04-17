<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useComplaintStore } from '../../stores/complaint'
import { formatComplaintStatus, formatComplaintType, formatDateTime } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const complaintStore = useComplaintStore()

const complaint = computed(() => complaintStore.complaintDetail)

const stats = computed(() => {
  if (!complaint.value) {
    return []
  }

  return [
    {
      label: '投诉状态',
      value: formatComplaintStatus(complaint.value.status),
      hint: '字段来自 ComplaintDetailVO.status',
    },
    {
      label: '投诉类型',
      value: formatComplaintType(complaint.value.type),
      hint: '契约枚举 NOT_PURCHASED / FAKE_RECEIPT',
    },
    {
      label: '关联订单',
      value: complaint.value.orderNo,
      hint: '详情页继续沿用 complaint -> order 的固定字段关系',
    },
  ]
})

const loadDetail = async (complaintId = route.params.complaintId) => {
  if (!complaintId) {
    return
  }

  try {
    await complaintStore.loadComplaintDetail(complaintId)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

watch(
  () => route.params.complaintId,
  (complaintId, previousComplaintId) => {
    if (complaintId && complaintId !== previousComplaintId) {
      loadDetail(complaintId)
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
      v-loading="complaintStore.complaintDetailLoading"
      title="投诉详情"
      description="对应 GET /api/complaints/{complaintId}。"
    >
      <template v-if="complaint">
        <div class="card-header-row">
          <div>
            <p class="section-kicker">{{ complaint.complaintNo }}</p>
            <h2>{{ complaint.productName }}</h2>
          </div>
          <StatusTag :value="complaint.status" :text="formatComplaintStatus(complaint.status)" />
        </div>

        <div class="detail-grid">
          <div class="surface-card detail-panel">
            <h3>基础信息</h3>
            <ul class="detail-list">
              <li><span>投诉类型</span><strong>{{ formatComplaintType(complaint.type) }}</strong></li>
              <li><span>关联订单</span><strong>{{ complaint.orderNo }}</strong></li>
              <li><span>订单商品</span><strong>{{ complaint.productName }}</strong></li>
              <li><span>被投诉人</span><strong>{{ complaint.accusedNickname }}</strong></li>
              <li><span>系统开启通道</span><strong>{{ complaint.openedBySystem ? '是' : '否' }}</strong></li>
              <li><span>创建时间</span><strong>{{ formatDateTime(complaint.createdAt) }}</strong></li>
            </ul>
          </div>

          <div class="surface-card detail-panel">
            <h3>处理信息</h3>
            <ul class="detail-list">
              <li><span>处理状态</span><strong>{{ formatComplaintStatus(complaint.status) }}</strong></li>
              <li><span>处理时间</span><strong>{{ formatDateTime(complaint.handledAt) }}</strong></li>
            </ul>

            <div class="detail-note">
              <span>投诉内容</span>
              <p>{{ complaint.content || '暂无投诉内容' }}</p>
            </div>

            <div class="detail-note">
              <span>处理结果</span>
              <p>{{ complaint.handleResult || '管理员尚未处理' }}</p>
            </div>
          </div>
        </div>

        <div class="page-actions wrap-actions">
          <el-button @click="router.push('/complaints')">返回我的投诉</el-button>
          <el-button type="primary" plain @click="router.push(`/orders/${complaint.orderId}`)">查看关联订单</el-button>
        </div>
      </template>
    </PageSection>
  </div>
</template>
