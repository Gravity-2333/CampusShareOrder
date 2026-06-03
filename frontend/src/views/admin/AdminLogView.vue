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
  bizType: '',
  keyword: '',
  operatorType: '',
  page: 1,
  pageSize: 10,
})

const stats = computed(() => {
  const list = adminStore.logsPage.list
  const adminActions = list.filter((item) => item.operatorType === 'ADMIN').length
  const complaintActions = list.filter((item) => String(item.action).includes('COMPLAINT')).length

  return [
    {
      label: '日志总量',
      value: adminStore.logsPage.total,
      hint: '统计当前筛选条件下的记录总量',
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

const summaryText = computed(() => {
  if (!adminStore.logsPage.list.length) {
    return '当前筛选条件下没有日志记录。'
  }

  if (filters.action || filters.bizType || filters.keyword || filters.operatorType) {
    return '当前列表已经按动作关键字过滤，适合快速排查一类后台操作链路。'
  }

  return '操作日志页优先帮助你回看后台治理动作和投诉、封禁、取消等关键链路。'
})

const loadLogs = async () => {
  try {
    const page = await adminStore.loadOperationLogs(filters)
    filters.page = page.page
    filters.pageSize = page.pageSize
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const submitFilters = async () => {
  filters.page = 1
  await loadLogs()
}

const resetFilters = async () => {
  filters.action = ''
  filters.bizType = ''
  filters.keyword = ''
  filters.operatorType = ''
  filters.page = 1
  filters.pageSize = 10
  await loadLogs()
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
      <StatCard
        v-for="item in stats"
        :key="item.label"
        :label="item.label"
        :value="item.value"
        :hint="item.hint"
      />
    </div>

    <PageSection
      title="操作日志"
      description="追踪关键后台操作、处理对象和操作人信息。"
    >
      <p class="muted-text">
        {{ summaryText }}
      </p>

      <div class="toolbar-row">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索动作、对象或详情"
          clearable
          @keyup.enter="submitFilters"
        />
        <el-input
          v-model="filters.action"
          placeholder="按动作筛选，如 COMPLAINT"
          clearable
          @keyup.enter="submitFilters"
        />
        <el-select
          v-model="filters.operatorType"
          placeholder="操作人"
          clearable
        >
          <el-option
            label="ADMIN"
            value="ADMIN"
          />
          <el-option
            label="USER"
            value="USER"
          />
        </el-select>
        <el-select
          v-model="filters.bizType"
          placeholder="业务类型"
          clearable
        >
          <el-option
            label="USER"
            value="USER"
          />
          <el-option
            label="ORDER"
            value="ORDER"
          />
          <el-option
            label="COMPLAINT"
            value="COMPLAINT"
          />
        </el-select>
        <el-button
          type="primary"
          :loading="adminStore.logsLoading"
          @click="submitFilters"
        >
          查询
        </el-button>
      </div>

      <div class="table-toolbar">
        <span class="table-caption">
          共 {{ adminStore.logsPage.total }} 条日志{{ filters.action ? `，当前关键字：${filters.action}` : '' }}。
        </span>
        <div class="page-actions">
          <el-button
            :disabled="adminStore.logsLoading"
            @click="resetFilters"
          >
            恢复默认筛选
          </el-button>
        </div>
      </div>

      <div
        v-if="adminStore.logsPage.list.length"
        class="table-stack"
      >
        <div class="desktop-table">
          <el-table
            v-loading="adminStore.logsLoading"
            :data="adminStore.logsPage.list"
            stripe
          >
            <el-table-column
              prop="action"
              label="动作"
            />
            <el-table-column
              prop="operatorName"
              label="操作人"
            />
            <el-table-column
              prop="operatorType"
              label="操作者类型"
            />
            <el-table-column
              prop="bizType"
              label="业务类型"
            />
            <el-table-column
              prop="targetNo"
              label="目标编号"
            />
            <el-table-column
              prop="detail"
              label="说明"
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
            v-for="(row, index) in adminStore.logsPage.list"
            :key="`${row.action}-${row.createdAt}-${index}`"
            class="surface-card mobile-record-card"
          >
            <div class="mobile-record-title">
              <span>{{ formatDateTime(row.createdAt) }}</span>
              <strong>{{ row.action || '--' }}</strong>
            </div>
            <ul class="mobile-record-fields">
              <li><span>操作人</span><strong>{{ row.operatorName || '--' }}</strong></li>
              <li><span>操作者类型</span><strong>{{ row.operatorType || '--' }}</strong></li>
              <li><span>业务类型</span><strong>{{ row.bizType || '--' }}</strong></li>
              <li><span>目标编号</span><strong>{{ row.targetNo || '--' }}</strong></li>
              <li><span>说明</span><strong>{{ row.detail || '--' }}</strong></li>
            </ul>
          </article>
        </div>

        <AppPagination
          :page="adminStore.logsPage.page"
          :page-size="adminStore.logsPage.pageSize"
          :pages="adminStore.logsPage.pages"
          :total="adminStore.logsPage.total"
          @change="handlePageChange"
        />
      </div>
      <EmptyState
        v-else-if="!adminStore.logsLoading"
        title="暂无操作日志"
        description="订单、投诉和后台处理动作会在这里持续沉淀。"
      />
    </PageSection>
  </div>
</template>
