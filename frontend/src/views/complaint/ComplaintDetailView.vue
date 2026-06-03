<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import EmptyState from '../../components/common/EmptyState.vue'
import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useComplaintStore } from '../../stores/complaint'
import { formatComplaintStatus, formatComplaintType, formatDateTime } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const complaintStore = useComplaintStore()

const complaint = computed(() => complaintStore.complaintDetail)
const currentComplaintId = computed(() => route.params.complaintId)
const normalizedComplaintId = computed(() => Number(currentComplaintId.value || 0))
const isValidComplaintId = computed(
  () => Number.isInteger(normalizedComplaintId.value) && normalizedComplaintId.value > 0,
)
const detailErrorText = computed(() =>
  isValidComplaintId.value ? '当前未能加载到投诉详情，请返回列表重新选择。' : '当前路由中的投诉 ID 无效，请返回我的投诉重新进入详情页。',
)
const relatedOrderId = computed(() => Number(complaint.value?.orderId || 0))
const canViewRelatedOrder = computed(() => Number.isInteger(relatedOrderId.value) && relatedOrderId.value > 0)

const stats = computed(() => {
  if (!complaint.value) {
    return []
  }

  return [
    {
      label: '投诉状态',
      value: formatComplaintStatus(complaint.value.status),
      hint: '展示当前处理进度',
    },
    {
      label: '投诉类型',
      value: formatComplaintType(complaint.value.type),
      hint: '帮助管理员快速判断问题类型',
    },
    {
      label: '关联订单',
      value: complaint.value.orderNo || '--',
      hint: '详情页继续沿用 complaint -> order 的固定字段关系',
    },
  ]
})

const loadDetail = async (complaintId = route.params.complaintId) => {
  if (!isValidComplaintId.value) {
    complaintStore.complaintDetail = null
    return
  }

  try {
    await complaintStore.loadComplaintDetail(complaintId)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const goRelatedOrder = () => {
  if (!canViewRelatedOrder.value) {
    ElMessage.warning('当前投诉缺少有效的关联订单')
    return
  }

  router.push(`/orders/${relatedOrderId.value}`)
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
      <StatCard
        v-for="item in stats"
        :key="item.label"
        :label="item.label"
        :value="item.value"
        :hint="item.hint"
      />
    </div>

    <PageSection
      v-loading="complaintStore.complaintDetailLoading"
      title="投诉详情"
      description="查看投诉内容、处理状态、关联订单和最终处理结果。"
    >
      <template v-if="complaint">
        <div class="card-header-row">
          <div>
            <p class="section-kicker">
              {{ complaint.complaintNo }}
            </p>
            <h2>{{ complaint.productName || '关联订单商品' }}</h2>
          </div>
          <StatusTag
            :value="complaint.status"
            :text="formatComplaintStatus(complaint.status)"
          />
        </div>

        <div class="detail-grid">
          <div class="surface-card detail-panel">
            <h3>基础信息</h3>
            <ul class="detail-list">
              <li><span>投诉类型</span><strong>{{ formatComplaintType(complaint.type) }}</strong></li>
              <li><span>关联订单</span><strong>{{ complaint.orderNo || '--' }}</strong></li>
              <li><span>订单商品</span><strong>{{ complaint.productName || '--' }}</strong></li>
              <li><span>被投诉人</span><strong>{{ complaint.accusedNickname || '--' }}</strong></li>
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

        <div class="table-toolbar">
          <span class="table-caption">
            当前投诉状态为 <strong>{{ formatComplaintStatus(complaint.status) }}</strong>，处理结果会在这里同步更新。
          </span>
        </div>

        <div class="page-actions wrap-actions">
          <el-button @click="router.push('/complaints')">
            返回我的投诉
          </el-button>
          <el-button
            type="primary"
            plain
            :disabled="!canViewRelatedOrder"
            @click="goRelatedOrder"
          >
            查看关联订单
          </el-button>
        </div>
      </template>
      <EmptyState
        v-else-if="!complaintStore.complaintDetailLoading"
        title="投诉详情不可用"
        :description="detailErrorText"
      >
        <div class="page-actions">
          <el-button @click="router.push('/complaints')">
            返回我的投诉
          </el-button>
          <el-button
            type="primary"
            plain
            :disabled="!isValidComplaintId"
            @click="loadDetail()"
          >
            重新加载
          </el-button>
        </div>
      </EmptyState>
    </PageSection>
  </div>
</template>
