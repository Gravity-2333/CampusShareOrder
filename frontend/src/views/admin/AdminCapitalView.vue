<script setup>
import { onMounted } from 'vue'
import { ElMessage } from 'element-plus'

import AppPagination from '../../components/common/AppPagination.vue'
import PageSection from '../../components/common/PageSection.vue'
import { useAdminStore } from '../../stores/admin'

const adminStore = useAdminStore()

const loadRecords = async () => {
  try {
    await adminStore.loadCapitalRecords({ page: 1, pageSize: 10 })
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handlePageChange = async ({ page, pageSize }) => {
  try {
    await adminStore.loadCapitalRecords({ page, pageSize })
  } catch (error) {
    ElMessage.error(error.message)
  }
}

onMounted(loadRecords)
</script>

<template>
  <PageSection title="资金记录" description="对应 GET /api/admin/records/capital。">
    <div class="table-stack">
      <el-table v-loading="adminStore.recordsLoading" :data="adminStore.recordsPage.list" stripe>
        <el-table-column prop="bizNo" label="业务单号" />
        <el-table-column prop="type" label="类型" />
        <el-table-column prop="userNickname" label="用户" />
        <el-table-column prop="amount" label="金额" />
        <el-table-column prop="createdAt" label="时间" />
      </el-table>
      <AppPagination
        :page="adminStore.recordsPage.page"
        :page-size="adminStore.recordsPage.pageSize"
        :pages="adminStore.recordsPage.pages"
        :total="adminStore.recordsPage.total"
        @change="handlePageChange"
      />
    </div>
  </PageSection>
</template>
