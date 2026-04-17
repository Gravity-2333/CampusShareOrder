<script setup>
import { computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'

import AppPagination from '../../components/common/AppPagination.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import { useAdminStore } from '../../stores/admin'
import { formatDateTime } from '../../utils/format'

const adminStore = useAdminStore()

const filters = reactive({
  action: '',
  page: 1,
  pageSize: 10,
})

const stats = computed(() => {
  const list = adminStore.logsPage.list
  const adminActions = list.filter((item) => String(item.operatorName || '').includes('管理员')).length
  const complaintActions = list.filter((item) => String(item.action).includes('COMPLAINT')).length

  return [
    {
      label: '日志总量',
      value: adminStore.logsPage.total,
      hint: '对应固定分页结构 total',
    },
    {
      label: '管理员操作',
      value: adminActions,
      hint: '统计当前页由管理员触发的动作',
    },
    {
      label: '投诉相关',
      value: complaintActions,
      hint: '便于快速定位投诉处理链路日志',
    },
  ]
})

const loadLogs = async () => {
  try {
    await adminStore.loadOperationLogs(filters)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handlePageChange = async ({ page, pageSize }) => {
  filters.page = page
  filters.pageSize = pageSize
  await loadLogs()
}

onMounted(loadLogs)
</script>

<template>
  <div class="stack-page">
    <div class="stats-grid">
      <StatCard v-for="item in stats" :key="item.label" :label="item.label" :value="item.value" :hint="item.hint" />
    </div>

    <PageSection title="操作日志" description="对应 GET /api/admin/records/logs。">
      <div class="toolbar-row">
        <el-input v-model="filters.action" placeholder="按动作关键字筛选，例如 COMPLAINT" clearable />
        <div />
        <el-button type="primary" @click="loadLogs">查询</el-button>
      </div>

      <div v-if="adminStore.logsPage.list.length" class="table-stack">
        <el-table v-loading="adminStore.logsLoading" :data="adminStore.logsPage.list" stripe>
          <el-table-column prop="action" label="动作" />
          <el-table-column prop="operatorName" label="操作人" />
          <el-table-column prop="targetNo" label="目标编号" />
          <el-table-column label="时间">
            <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
          </el-table-column>
        </el-table>
        <AppPagination
          :page="adminStore.logsPage.page"
          :page-size="adminStore.logsPage.pageSize"
          :pages="adminStore.logsPage.pages"
          :total="adminStore.logsPage.total"
          @change="handlePageChange"
        />
      </div>
      <EmptyState
        v-else
        title="暂无操作日志"
        description="日志列表同样保持 provider 同签名结构，方便后续联调切换。"
      />
    </PageSection>
  </div>
</template>
