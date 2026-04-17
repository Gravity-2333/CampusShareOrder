<script setup>
import { computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'

import AppPagination from '../../components/common/AppPagination.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import { useAdminStore } from '../../stores/admin'
import { formatCurrency, formatDateTime } from '../../utils/format'

const adminStore = useAdminStore()

const filters = reactive({
  page: 1,
  pageSize: 10,
  type: '',
})

const stats = computed(() => {
  const list = adminStore.recordsPage.list
  const totalAmount = list.reduce((sum, item) => sum + Number(item.amount || 0), 0)
  const refundCount = list.filter((item) => String(item.type).includes('REFUND')).length

  return [
    {
      label: '记录总量',
      value: adminStore.recordsPage.total,
      hint: '对应固定分页结构 total',
    },
    {
      label: '当前页金额',
      value: formatCurrency(totalAmount),
      hint: '仅用于后台快速查看当前页汇总',
    },
    {
      label: '退款记录',
      value: refundCount,
      hint: '便于快速定位退款类流水',
    },
  ]
})

const loadRecords = async () => {
  try {
    await adminStore.loadCapitalRecords(filters)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handlePageChange = async ({ page, pageSize }) => {
  filters.page = page
  filters.pageSize = pageSize
  await loadRecords()
}

onMounted(loadRecords)
</script>

<template>
  <div class="stack-page">
    <div class="stats-grid">
      <StatCard v-for="item in stats" :key="item.label" :label="item.label" :value="item.value" :hint="item.hint" />
    </div>

    <PageSection title="资金记录" description="对应 GET /api/admin/records/capital。">
      <div class="toolbar-row">
        <el-select v-model="filters.type" placeholder="按流水类型筛选" clearable>
          <el-option label="支付" value="PAY" />
          <el-option label="取消退款" value="REFUND_CANCEL" />
          <el-option label="差额退款" value="REFUND_DIFF" />
          <el-option label="发起人结算" value="SETTLE_TO_CREATOR" />
        </el-select>
        <div />
        <el-button type="primary" @click="loadRecords">查询</el-button>
      </div>

      <div v-if="adminStore.recordsPage.list.length" class="table-stack">
        <el-table v-loading="adminStore.recordsLoading" :data="adminStore.recordsPage.list" stripe>
          <el-table-column prop="bizNo" label="业务单号" />
          <el-table-column prop="type" label="类型" />
          <el-table-column prop="userNickname" label="用户" />
          <el-table-column label="金额">
            <template #default="{ row }">{{ formatCurrency(row.amount) }}</template>
          </el-table-column>
          <el-table-column label="时间">
            <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
          </el-table-column>
        </el-table>
        <AppPagination
          :page="adminStore.recordsPage.page"
          :page-size="adminStore.recordsPage.pageSize"
          :pages="adminStore.recordsPage.pages"
          :total="adminStore.recordsPage.total"
          @change="handlePageChange"
        />
      </div>
      <EmptyState
        v-else
        title="暂无资金记录"
        description="后续切到 live 时，页面仍然只通过 store -> api 层消费。"
      />
    </PageSection>
  </div>
</template>
