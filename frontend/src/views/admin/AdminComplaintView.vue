<script setup>
import { onMounted } from 'vue'
import { ElMessage } from 'element-plus'

import AppPagination from '../../components/common/AppPagination.vue'
import PageSection from '../../components/common/PageSection.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useAdminStore } from '../../stores/admin'
import { formatComplaintStatus, formatComplaintType } from '../../utils/format'

const adminStore = useAdminStore()

const loadComplaints = async () => {
  try {
    await adminStore.loadComplaints({ page: 1, pageSize: 10 })
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handleComplaint = async (row) => {
  try {
    await adminStore.processComplaint(row.complaintId)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handlePageChange = async ({ page, pageSize }) => {
  try {
    await adminStore.loadComplaints({ page, pageSize })
  } catch (error) {
    ElMessage.error(error.message)
  }
}

onMounted(loadComplaints)
</script>

<template>
  <PageSection title="投诉管理" description="管理端投诉处理也保持 provider 同签名结构。">
    <div class="table-stack">
      <el-table v-loading="adminStore.complaintsLoading" :data="adminStore.complaintsPage.list" stripe>
        <el-table-column prop="complaintNo" label="投诉单号" />
        <el-table-column label="类型">
          <template #default="{ row }">{{ formatComplaintType(row.type) }}</template>
        </el-table-column>
        <el-table-column label="状态">
          <template #default="{ row }">
            <StatusTag :value="row.status" :text="formatComplaintStatus(row.status)" />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" />
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleComplaint(row)">标记处理</el-button>
          </template>
        </el-table-column>
      </el-table>
      <AppPagination
        :page="adminStore.complaintsPage.page"
        :page-size="adminStore.complaintsPage.pageSize"
        :pages="adminStore.complaintsPage.pages"
        :total="adminStore.complaintsPage.total"
        @change="handlePageChange"
      />
    </div>
  </PageSection>
</template>
