<script setup>
import { onMounted } from 'vue'
import { ElMessage } from 'element-plus'

import PageSection from '../../components/common/PageSection.vue'
import { useAdminStore } from '../../stores/admin'

const adminStore = useAdminStore()

const loadLogs = async () => {
  try {
    await adminStore.loadOperationLogs({ page: 1, pageSize: 10 })
  } catch (error) {
    ElMessage.error(error.message)
  }
}

onMounted(loadLogs)
</script>

<template>
  <PageSection title="操作日志" description="对应 GET /api/admin/records/logs。">
    <el-table v-loading="adminStore.logsLoading" :data="adminStore.logsPage.list" stripe>
      <el-table-column prop="action" label="动作" />
      <el-table-column prop="operatorName" label="操作人" />
      <el-table-column prop="targetNo" label="目标编号" />
      <el-table-column prop="createdAt" label="时间" />
    </el-table>
  </PageSection>
</template>
