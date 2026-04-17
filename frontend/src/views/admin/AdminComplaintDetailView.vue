<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useAdminStore } from '../../stores/admin'
import { formatComplaintStatus, formatComplaintType, formatDateTime } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

const complaint = computed(() => adminStore.complaintDetail)

const stats = computed(() => {
  if (!complaint.value) {
    return []
  }

  return [
    {
      label: '投诉状态',
      value: formatComplaintStatus(complaint.value.status),
      hint: '状态变更后会同步刷新投诉列表',
    },
    {
      label: '关联订单',
      value: complaint.value.orderNo,
      hint: '处理投诉后，mock 中会同步取消关联订单',
    },
    {
      label: '投诉类型',
      value: formatComplaintType(complaint.value.type),
      hint: '管理员详情与用户详情消费同一批核心字段',
    },
  ]
})

const loadDetail = async (complaintId = route.params.complaintId) => {
  if (!complaintId) {
    return
  }

  try {
    await adminStore.loadComplaintDetail(complaintId)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handleProcess = async () => {
  if (!complaint.value) {
    return
  }

  try {
    const { value } = await ElMessageBox.prompt('请输入处理结果摘要', '处理投诉', {
      cancelButtonText: '取消',
      confirmButtonText: '确认处理',
      inputPlaceholder: '例如：投诉成立，订单已取消',
      inputValidator: (inputValue) => {
        if (!inputValue?.trim()) {
          return '请输入处理结果'
        }

        return true
      },
    })

    await adminStore.processComplaint(complaint.value.complaintId, { handleResult: value.trim() })
    ElMessage.success('投诉已处理')
  } catch (error) {
    if (error === 'cancel' || error?.message === 'cancel') {
      return
    }

    ElMessage.error(error.message || '处理失败，请稍后重试')
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
      v-loading="adminStore.complaintDetailLoading"
      title="投诉详情"
      description="对应 GET /api/admin/complaints/{complaintId}。"
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
            <h3>投诉信息</h3>
            <ul class="detail-list">
              <li><span>投诉类型</span><strong>{{ formatComplaintType(complaint.type) }}</strong></li>
              <li><span>关联订单</span><strong>{{ complaint.orderNo }}</strong></li>
              <li><span>订单商品</span><strong>{{ complaint.productName }}</strong></li>
              <li><span>投诉人 ID</span><strong>{{ complaint.complainantUserId }}</strong></li>
              <li><span>被投诉人</span><strong>{{ complaint.accusedNickname }}</strong></li>
              <li><span>系统开启通道</span><strong>{{ complaint.openedBySystem ? '是' : '否' }}</strong></li>
              <li><span>提交时间</span><strong>{{ formatDateTime(complaint.createdAt) }}</strong></li>
            </ul>
          </div>

          <div class="surface-card detail-panel">
            <h3>处理结果</h3>
            <ul class="detail-list">
              <li><span>当前状态</span><strong>{{ formatComplaintStatus(complaint.status) }}</strong></li>
              <li><span>处理时间</span><strong>{{ formatDateTime(complaint.handledAt) }}</strong></li>
            </ul>

            <div class="detail-note">
              <span>投诉内容</span>
              <p>{{ complaint.content || '暂无投诉内容' }}</p>
            </div>

            <div class="detail-note">
              <span>处理说明</span>
              <p>{{ complaint.handleResult || '尚未填写处理说明' }}</p>
            </div>
          </div>
        </div>

        <div class="page-actions wrap-actions">
          <el-button @click="router.push('/admin/complaints')">返回投诉管理</el-button>
          <el-button type="primary" plain @click="router.push('/admin/orders')">查看订单管理</el-button>
          <el-button
            v-if="complaint.status === 'PENDING'"
            type="danger"
            :loading="adminStore.submitting"
            @click="handleProcess"
          >
            处理投诉
          </el-button>
        </div>
      </template>
    </PageSection>
  </div>
</template>
