<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import AppPagination from '../../components/common/AppPagination.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import { getUserCapitalRecords } from '../../api/user'
import { formatCapitalRecordType, formatCurrency, formatDateTime } from '../../utils/format'
import { defaultPageData, loadNormalizedPage } from '../../utils/page'

const loading = ref(false)
const recordsPage = ref(defaultPageData())
const filters = reactive({
  page: 1,
  pageSize: 10,
  type: '',
})

const stats = computed(() => {
  const records = recordsPage.value.list
  const paid = records
    .filter((item) => item.type === 'PAY')
    .reduce((sum, item) => sum + Number(item.amount || 0), 0)
  const refunded = records
    .filter((item) => String(item.type).startsWith('REFUND'))
    .reduce((sum, item) => sum + Number(item.amount || 0), 0)

  return [
    { label: '流水总数', value: recordsPage.value.total, hint: '当前筛选条件下的全部记录' },
    { label: '本页支付', value: formatCurrency(paid), hint: '本页支付流水金额合计' },
    { label: '本页退款', value: formatCurrency(refunded), hint: '本页退款流水金额合计' },
  ]
})

const loadRecords = async () => {
  loading.value = true
  try {
    const { filters: resolvedFilters, pageData } = await loadNormalizedPage(getUserCapitalRecords, filters)
    Object.assign(filters, resolvedFilters)
    recordsPage.value = pageData
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

const submitFilter = async () => {
  filters.page = 1
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
      title="支付与退款记录"
      description="查看自己参与拼单产生的支付、退款和发起人打款流水。"
    >
      <div class="toolbar-row">
        <el-select
          v-model="filters.type"
          placeholder="全部流水类型"
          clearable
          @change="submitFilter"
        >
          <el-option label="支付" value="PAY" />
          <el-option label="取消退款" value="REFUND_CANCEL" />
          <el-option label="退出退款" value="REFUND_EXIT" />
          <el-option label="差额退款" value="REFUND_DIFF" />
          <el-option label="发起人打款" value="SETTLE_TO_CREATOR" />
        </el-select>
      </div>

      <div
        v-if="recordsPage.list.length"
        class="table-stack"
      >
        <div class="desktop-table">
          <el-table
            v-loading="loading"
            :data="recordsPage.list"
            stripe
          >
            <el-table-column prop="bizNo" label="流水号" />
            <el-table-column label="类型">
              <template #default="{ row }">
                {{ formatCapitalRecordType(row.type) }}
              </template>
            </el-table-column>
            <el-table-column prop="orderNo" label="关联拼单" />
            <el-table-column prop="orderId" label="拼单 ID" />
            <el-table-column label="金额">
              <template #default="{ row }">
                {{ formatCurrency(row.amount) }}
              </template>
            </el-table-column>
            <el-table-column prop="operatorName" label="操作人" />
            <el-table-column label="收款人">
              <template #default="{ row }">
                {{ row.receiverName || '--' }}
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="原因/备注" />
            <el-table-column label="支付/退款时间">
              <template #default="{ row }">
                {{ formatDateTime(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="mobile-record-list">
          <article
            v-for="row in recordsPage.list"
            :key="row.bizNo"
            class="surface-card mobile-record-card"
          >
            <div class="mobile-record-title">
              <span>{{ row.orderNo || '关联拼单' }}</span>
              <strong>{{ formatCapitalRecordType(row.type) }}</strong>
            </div>
            <ul class="mobile-record-fields">
              <li><span>流水号</span><strong>{{ row.bizNo }}</strong></li>
              <li><span>拼单 ID</span><strong>{{ row.orderId || '--' }}</strong></li>
              <li><span>金额</span><strong>{{ formatCurrency(row.amount) }}</strong></li>
              <li><span>操作人</span><strong>{{ row.operatorName || '--' }}</strong></li>
              <li><span>收款人</span><strong>{{ row.receiverName || '--' }}</strong></li>
              <li><span>原因</span><strong>{{ row.remark || '--' }}</strong></li>
              <li><span>支付/退款时间</span><strong>{{ formatDateTime(row.createdAt) }}</strong></li>
            </ul>
          </article>
        </div>

        <AppPagination
          :page="recordsPage.page"
          :page-size="recordsPage.pageSize"
          :pages="recordsPage.pages"
          :total="recordsPage.total"
          @change="handlePageChange"
        />
      </div>

      <EmptyState
        v-else-if="!loading"
        title="暂无支付或退款记录"
        description="加入并支付拼单、退出退款或取消拼单后，流水会显示在这里。"
      />
    </PageSection>
  </div>
</template>
