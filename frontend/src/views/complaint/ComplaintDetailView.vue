<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import EmptyState from '../../components/common/EmptyState.vue'
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
  isValidComplaintId.value
    ? '当前未能加载到投诉详情，请返回列表重新选择。'
    : '当前路由中的投诉 ID 无效，请返回我的投诉重新进入详情页。',
)

// 处理进度时间轴
const timeline = computed(() => {
  if (!complaint.value) return []

  const events = []

  // 投诉已提交
  if (complaint.value.createdAt) {
    events.push({
      title: '投诉已提交',
      description: '系统已记录投诉信息',
      time: complaint.value.createdAt,
    })
  }

  // 管理员已受理
  if (complaint.value.status === 'PROCESSED' && complaint.value.handledAt) {
    events.push({
      title: '管理员已受理',
      description: complaint.value.handleResult || '管理员正在处理中',
      time: complaint.value.handledAt,
    })
  }

  return events
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
    <div
      v-loading="complaintStore.complaintDetailLoading"
      class="section"
    >
      <template v-if="complaint">
        <div class="section-header">
          <div>
            <StatusTag
              :value="complaint.status"
              :text="formatComplaintStatus(complaint.status)"
            />
            <h3>投诉单号：{{ complaint.complaintNo }}</h3>
          </div>
        </div>

        <div class="detail-panel">
          <h4>投诉信息</h4>
          <ul class="detail-list">
            <li>
              <span>关联订单</span>
              <strong>{{ complaint.orderNo }} - {{ complaint.productName || '--' }}</strong>
            </li>
            <li>
              <span>投诉类型</span>
              <strong>{{ formatComplaintType(complaint.type) }}</strong>
            </li>
            <li>
              <span>投诉人</span>
              <strong>{{ complaint.complainantNickname || '--' }}</strong>
            </li>
            <li>
              <span>被投诉人</span>
              <strong>{{ complaint.accusedNickname || '--' }}</strong>
            </li>
            <li>
              <span>创建时间</span>
              <strong>{{ formatDateTime(complaint.createdAt) }}</strong>
            </li>
          </ul>
          <div class="detail-note">
            <span>投诉描述</span>
            <p>{{ complaint.content || '暂无投诉描述' }}</p>
          </div>

          <div style="margin-top: 1.5rem">
            <h4>处理进度</h4>
            <div
              v-if="timeline.length"
              class="timeline"
            >
              <div
                v-for="(event, index) in timeline"
                :key="index"
                class="timeline-item"
              >
                <strong>{{ event.title }}</strong>
                <div>{{ event.description }}</div>
                <div style="color: #6b7c70; font-size: 0.85rem">
                  {{ formatDateTime(event.time) }}
                </div>
              </div>
            </div>
            <div
              v-else
              style="color: #6b7c70; padding: 1rem 0"
            >
              暂无处理记录
            </div>
          </div>

          <div class="page-actions detail-actions-bar">
            <el-button @click="router.push('/complaints')">
              返回列表
            </el-button>
          </div>
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
    </div>
  </div>
</template>
