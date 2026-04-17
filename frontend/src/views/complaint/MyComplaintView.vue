<script setup>
import { onMounted } from 'vue'
import { ElMessage } from 'element-plus'

import EmptyState from '../../components/common/EmptyState.vue'
import PageSection from '../../components/common/PageSection.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useComplaintStore } from '../../stores/complaint'
import { formatComplaintStatus, formatComplaintType } from '../../utils/format'

const complaintStore = useComplaintStore()

const loadComplaints = async () => {
  try {
    await complaintStore.loadMyComplaints({ page: 1, pageSize: 10 })
  } catch (error) {
    ElMessage.error(error.message)
  }
}

onMounted(loadComplaints)
</script>

<template>
  <PageSection title="我的投诉" description="对应 GET /api/complaints/my。">
    <el-table
      v-if="complaintStore.myComplaintsPage.list.length"
      v-loading="complaintStore.myComplaintsLoading"
      :data="complaintStore.myComplaintsPage.list"
      stripe
    >
      <el-table-column prop="complaintNo" label="投诉单号" />
      <el-table-column label="投诉类型">
        <template #default="{ row }">{{ formatComplaintType(row.type) }}</template>
      </el-table-column>
      <el-table-column prop="productName" label="商品" />
      <el-table-column label="状态">
        <template #default="{ row }">
          <StatusTag :value="row.status" :text="formatComplaintStatus(row.status)" />
        </template>
      </el-table-column>
      <el-table-column prop="content" label="投诉内容" />
    </el-table>
    <EmptyState
      v-else
      title="暂无投诉"
      description="投诉列表也遵循固定分页结构，方便后续无缝换到 live。"
    />
  </PageSection>
</template>
