<script setup>
import { onMounted } from 'vue'
import { ElMessage } from 'element-plus'

import PageSection from '../../components/common/PageSection.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useAdminStore } from '../../stores/admin'
import { formatUserStatus } from '../../utils/format'

const adminStore = useAdminStore()

const loadUsers = async () => {
  try {
    await adminStore.loadUsers({ page: 1, pageSize: 10 })
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const toggleStatus = async (row) => {
  try {
    await adminStore.toggleUserStatus(row)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

onMounted(loadUsers)
</script>

<template>
  <PageSection title="用户管理" description="管理端页面也通过统一 provider 获取数据。">
    <el-table v-loading="adminStore.usersLoading" :data="adminStore.usersPage.list" stripe>
      <el-table-column prop="userId" label="用户 ID" width="88" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column label="状态">
        <template #default="{ row }">
          <StatusTag :value="row.status" :text="formatUserStatus(row.status)" />
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button link type="primary" @click="toggleStatus(row)">
            {{ row.status === 'NORMAL' ? '封禁' : '解封' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </PageSection>
</template>
