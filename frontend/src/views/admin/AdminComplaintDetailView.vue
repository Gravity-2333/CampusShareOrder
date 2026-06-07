<script setup>
import { computed, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { useAdminStore } from '../../stores/admin'
import { formatComplaintStatus, formatComplaintType, formatDateTime } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

const complaint = computed(() => adminStore.complaintDetail)
const currentComplaintId = computed(() => route.params.complaintId)
const normalizedComplaintId = computed(() => Number(currentComplaintId.value || 0))
const isValidComplaintId = computed(() => Number.isInteger(normalizedComplaintId.value) && normalizedComplaintId.value > 0)
const relatedOrderId = computed(() => Number(complaint.value?.orderId || 0))
const canViewRelatedOrder = computed(() => Number.isInteger(relatedOrderId.value) && relatedOrderId.value > 0)

const form = reactive({
  handleResult: '',
})

const loadDetail = async (complaintId = route.params.complaintId) => {
  if (!isValidComplaintId.value) {
    adminStore.complaintDetail = null
    return
  }

  try {
    await adminStore.loadComplaintDetail(complaintId)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handleProcess = async () => {
  if (!complaint.value || adminStore.submitting) return

  try {
    await adminStore.processComplaint(complaint.value.complaintId, {
      result: 'CONFIRMED',
      handleResult: form.handleResult.trim() || '经核实投诉成立，按平台规则处理。',
    })
    ElMessage.success('投诉已判定成立')
    router.push('/admin/complaints') // 跳转回管理员投诉列表
  } catch (error) {
    if (error === 'cancel' || error?.message === 'cancel') return
    ElMessage.error(error.message || '处理失败，请稍后重试')
  }
}

const handleReject = async () => {
  if (!complaint.value || adminStore.submitting) return

  try {
    await adminStore.processComplaint(complaint.value.complaintId, {
      result: 'REJECTED',
      handleResult: form.handleResult.trim() || '经核实投诉证据不足，本次驳回。',
    })
    ElMessage.success('投诉已驳回')
    router.push('/admin/complaints') // 跳转回管理员投诉列表
  } catch (error) {
    if (error === 'cancel' || error?.message === 'cancel') return
    ElMessage.error(error.message || '处理失败，请稍后重试')
  }
}

const goRelatedOrder = () => {
  if (!canViewRelatedOrder.value) {
    ElMessage.warning('当前投诉缺少有效的关联订单')
    return
  }
  router.push(`/admin/orders/${relatedOrderId.value}`)
}

watch(
  () => route.params.complaintId,
  (complaintId, previousComplaintId) => {
    if (complaintId && complaintId !== previousComplaintId) loadDetail(complaintId)
  },
)

onMounted(() => loadDetail())
</script>

<template>
  <div class="stack-page">
    <div
      v-if="complaint"
      class="section"
    >
      <div class="section-header">
        <div>
          <span
            class="tag"
            :class="`tag-${complaint.status === 'PENDING' ? 'warning' : 'success'}`"
          >
            {{ formatComplaintStatus(complaint.status) }}
          </span>
          <h3>投诉单号：{{ complaint.complaintNo }}</h3>
        </div>
      </div>

      <div class="detail-panel">
        <h4>投诉信息</h4>
        <ul class="detail-list">
          <li><span>关联订单</span><strong>{{ complaint.orderNo || '--' }} - {{ complaint.productName || '--' }}</strong></li>
          <li><span>投诉类型</span><strong>{{ formatComplaintType(complaint.type) }}</strong></li>
          <li><span>投诉人</span><strong>{{ complaint.complainantNickname || '--' }}</strong></li>
          <li><span>被投诉人</span><strong>{{ complaint.accusedNickname || '--' }}</strong></li>
          <li><span>创建时间</span><strong>{{ formatDateTime(complaint.createdAt) }}</strong></li>
        </ul>
        <div style="border-top: 1px solid #e0d8cf; padding-top: 1rem; margin-top: 1rem;">
          <span style="color: #6b7c70;">投诉描述</span>
          <p style="color: #2c3e50; margin-top: 0.3rem;">
            {{ complaint.content || '暂无投诉内容' }}
          </p>
        </div>
      </div>

      <div>
        <h4>处理进度</h4>
        <div class="timeline">
          <div class="timeline-item">
            <strong>投诉已提交</strong>
            <div>系统已记录投诉信息</div>
            <div style="color: #6b7c70; font-size: 0.85rem;">
              {{ formatDateTime(complaint.createdAt) }}
            </div>
          </div>
          <div
            v-if="complaint.status === 'PROCESSED'"
            class="timeline-item"
          >
            <strong>管理员已处理</strong>
            <div>{{ complaint.handleResult || '已处理' }}</div>
            <div style="color: #6b7c70; font-size: 0.85rem;">
              {{ formatDateTime(complaint.handledAt) }}
            </div>
          </div>
          <div
            v-else-if="complaint.handledAt"
            class="timeline-item"
          >
            <strong>管理员已受理</strong>
            <div>管理员正在处理中</div>
            <div style="color: #6b7c70; font-size: 0.85rem;">
              {{ formatDateTime(complaint.handledAt) }}
            </div>
          </div>
        </div>
      </div>

      <div
        v-if="complaint.status === 'PENDING'"
        class="detail-panel"
        style="margin-top: 1rem;"
      >
        <h4>处理投诉</h4>
        <div class="form-group">
          <label>处理结果</label>
          <textarea
            v-model="form.handleResult"
            placeholder="请输入处理结果..."
          />
        </div>
        <div style="display: flex; gap: 0.5rem;">
          <button
            class="btn btn-success"
            :disabled="adminStore.submitting"
            @click="handleProcess"
          >
            投诉成立
          </button>
          <button
            class="btn btn-danger"
            :disabled="adminStore.submitting"
            @click="handleReject"
          >
            驳回投诉
          </button>
          <button
            class="btn btn-secondary"
            @click="router.push('/admin/complaints')"
          >
            返回列表
          </button>
        </div>
      </div>

      <div
        v-else
        style="margin-top: 1.5rem; padding: 1rem; background: #f8f6f2; border-radius: 10px;"
      >
        <button
          class="btn btn-secondary"
          @click="router.push('/admin/complaints')"
        >
          返回列表
        </button>
        <button
          v-if="canViewRelatedOrder"
          class="btn btn-primary"
          style="margin-left: 0.5rem;"
          @click="goRelatedOrder"
        >
          查看关联订单
        </button>
      </div>
    </div>

    <div
      v-else
      class="section"
    >
      <p class="muted-text">
        当前未能加载到投诉详情，请返回列表重新选择。
      </p>
      <div style="margin-top: 1rem;">
        <button
          class="btn btn-secondary"
          @click="router.push('/admin/complaints')"
        >
          返回投诉管理
        </button>
      </div>
    </div>
  </div>
</template>
