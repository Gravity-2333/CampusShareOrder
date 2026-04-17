<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import AppPagination from '../../components/common/AppPagination.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useComplaintStore } from '../../stores/complaint'
import { formatComplaintStatus, formatComplaintType, formatDateTime } from '../../utils/format'

const router = useRouter()
const complaintStore = useComplaintStore()

const stats = computed(() => [
  {
    label: '投诉总数',
    value: complaintStore.myComplaintsPage.total,
    hint: '对应固定分页结构 total',
  },
  {
    label: '当前页',
    value: `${complaintStore.myComplaintsPage.page}/${complaintStore.myComplaintsPage.pages || 1}`,
    hint: '后续切 live 仍保持相同分页消费方式',
  },
])

const loadComplaints = async () => {
  try {
    await complaintStore.loadMyComplaints({ page: 1, pageSize: 10 })
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handlePageChange = async ({ page, pageSize }) => {
  try {
    await complaintStore.loadMyComplaints({ page, pageSize })
  } catch (error) {
    ElMessage.error(error.message)
  }
}

onMounted(loadComplaints)
</script>

<template>
  <div class="stack-page">
    <div class="stats-grid">
      <StatCard v-for="item in stats" :key="item.label" :label="item.label" :value="item.value" :hint="item.hint" />
    </div>

    <PageSection title="我的投诉" description="对应 GET /api/complaints/my。">
      <div class="page-actions">
        <el-button type="danger" plain @click="router.push('/complaints/create')">发起新投诉</el-button>
      </div>

      <div v-if="complaintStore.myComplaintsPage.list.length" class="table-stack">
        <el-table
          v-loading="complaintStore.myComplaintsLoading"
          :data="complaintStore.myComplaintsPage.list"
          stripe
        >
          <el-table-column prop="complaintNo" label="投诉单号" />
          <el-table-column label="投诉类型">
            <template #default="{ row }">{{ formatComplaintType(row.type) }}</template>
          </el-table-column>
          <el-table-column prop="productName" label="商品" />
          <el-table-column prop="accusedNickname" label="被投诉人" />
          <el-table-column label="状态">
            <template #default="{ row }">
              <StatusTag :value="row.status" :text="formatComplaintStatus(row.status)" />
            </template>
          </el-table-column>
          <el-table-column label="发起时间">
            <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button link type="primary" @click="router.push(`/complaints/${row.complaintId}`)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <AppPagination
          :page="complaintStore.myComplaintsPage.page"
          :page-size="complaintStore.myComplaintsPage.pageSize"
          :pages="complaintStore.myComplaintsPage.pages"
          :total="complaintStore.myComplaintsPage.total"
          @change="handlePageChange"
        />
      </div>
      <EmptyState
        v-else
        title="暂无投诉"
        description="投诉列表也遵循固定分页结构，方便后续无缝换到 live。"
      />
    </PageSection>
  </div>
</template>
