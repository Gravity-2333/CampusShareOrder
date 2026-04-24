<script setup>
import { computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'

import AppPagination from '../../components/common/AppPagination.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import { useAdminStore } from '../../stores/admin'
import { formatCapitalRecordType, formatCurrency, formatDateTime } from '../../utils/format'

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

const summaryText = computed(() => {
  if (!adminStore.recordsPage.list.length) {
    return '当前筛选条件下没有资金记录。'
  }

  if (filters.type) {
    return '当前列表已经按流水类型筛选，适合集中查看同一类资金流转。'
  }

  return '资金记录页优先帮助后台快速核对订单支付、退款和发起人结算流向。'
})

const loadRecords = async () => {
  try {
    await adminStore.loadCapitalRecords(filters)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const submitFilters = async () => {
  filters.page = 1
  await loadRecords()
}

const resetFilters = async () => {
  filters.page = 1
  filters.pageSize = 10
  filters.type = ''
  await loadRecords()
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
      <StatCard
        v-for="item in stats"
        :key="item.label"
        :label="item.label"
        :value="item.value"
        :hint="item.hint"
      />
    </div>

    <PageSection
      title="资金记录"
      description="对应 GET /api/admin/records/capital。"
    >
      <p class="muted-text">
        {{ summaryText }}
      </p>

      <div class="toolbar-row">
        <el-select
          v-model="filters.type"
          placeholder="按流水类型筛选"
          clearable
        >
          <el-option
            label="支付"
            value="PAY"
          />
          <el-option
            label="取消退款"
            value="REFUND_CANCEL"
          />
          <el-option
            label="差额退款"
            value="REFUND_DIFF"
          />
          <el-option
            label="发起人结算"
            value="SETTLE_TO_CREATOR"
          />
        </el-select>
        <div />
        <el-button
          type="primary"
          @click="submitFilters"
        >
          查询
        </el-button>
      </div>

      <div class="table-toolbar">
        <span class="table-caption">
          共 {{ adminStore.recordsPage.total }} 条资金记录{{ filters.type ? `，当前筛选：${formatCapitalRecordType(filters.type)}` : '' }}。
        </span>
        <div class="page-actions">
          <el-button @click="resetFilters">
            恢复默认筛选
          </el-button>
        </div>
      </div>

      <div
        v-if="adminStore.recordsPage.list.length"
        class="table-stack"
      >
        <div class="desktop-table">
          <el-table
            v-loading="adminStore.recordsLoading"
            :data="adminStore.recordsPage.list"
            stripe
          >
            <el-table-column
              prop="bizNo"
              label="业务单号"
            />
            <el-table-column label="类型">
              <template #default="{ row }">
                {{ formatCapitalRecordType(row.type) }}
              </template>
            </el-table-column>
            <el-table-column
              prop="userNickname"
              label="用户"
            />
            <el-table-column
              prop="orderNo"
              label="关联订单"
            />
            <el-table-column label="金额">
              <template #default="{ row }">
                {{ formatCurrency(row.amount) }}
              </template>
            </el-table-column>
            <el-table-column
              prop="remark"
              label="备注"
            />
            <el-table-column label="时间">
              <template #default="{ row }">
                {{ formatDateTime(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="mobile-record-list">
          <article
            v-for="(row, index) in adminStore.recordsPage.list"
            :key="`${row.bizNo}-${index}`"
            class="surface-card mobile-record-card"
          >
            <div class="mobile-record-title">
              <span>{{ row.bizNo || '业务记录' }}</span>
              <strong>{{ formatCapitalRecordType(row.type) }}</strong>
            </div>
            <ul class="mobile-record-fields">
              <li><span>用户</span><strong>{{ row.userNickname || '--' }}</strong></li>
              <li><span>关联订单</span><strong>{{ row.orderNo || '--' }}</strong></li>
              <li><span>金额</span><strong>{{ formatCurrency(row.amount) }}</strong></li>
              <li><span>备注</span><strong>{{ row.remark || '--' }}</strong></li>
              <li><span>时间</span><strong>{{ formatDateTime(row.createdAt) }}</strong></li>
            </ul>
          </article>
        </div>

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
        description="支付、退款和结算动作会在这里形成资金流水。"
      />
    </PageSection>
  </div>
</template>
