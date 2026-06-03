<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import AppPagination from '../../components/common/AppPagination.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import { useUserStore } from '../../stores/user'
import { creditReasonTypeTextMap } from '../../utils/enum'
import { formatDateTime, formatSignedNumber } from '../../utils/format'

const router = useRouter()
const userStore = useUserStore()

const stats = computed(() => [
  {
    label: '当前信用分',
    value: userStore.credit.creditScore,
    hint: '用于后续投诉处理与风控场景',
  },
  {
    label: '信用等级',
    value: userStore.creditLevel,
    hint: '只做展示，不参与页面权限判断',
  },
  {
    label: '变更记录',
    value: userStore.credit.total || userStore.credit.records.length,
    hint: '展示与你相关的信用分变化明细',
  },
])

const summaryText = computed(() => {
  if (!userStore.credit.records.length) {
    return '当前还没有信用变更记录。'
  }

  return '信用分记录页重点展示变化原因和分值波动，便于快速回看信誉历史。'
})

const formatReasonType = (value) => creditReasonTypeTextMap[value] || value || '--'

const formatRelatedBiz = (row) => {
  if (row.relatedComplaintId) {
    return `投诉 #${row.relatedComplaintId}`
  }

  if (row.relatedOrderId) {
    return `订单 #${row.relatedOrderId}`
  }

  return '--'
}

const loadCredit = async (params) => {
  try {
    await Promise.all([userStore.loadCredit(params), userStore.ensureProfileLoaded()])
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handlePageChange = async ({ page, pageSize }) => {
  await loadCredit({ page, pageSize })
}

onMounted(loadCredit)
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
      title="信用分记录"
      description="查看信用分变化、关联订单和投诉处理记录。"
    >
      <p class="muted-text">
        {{ summaryText }}
      </p>

      <div class="table-toolbar">
        <span class="table-caption">
          当前信用分 <strong>{{ userStore.credit.creditScore }}</strong>，共 {{ userStore.credit.total || userStore.credit.records.length }} 条变更记录。
        </span>
        <div class="page-actions">
          <el-button @click="router.push('/profile')">
            返回个人资料
          </el-button>
        </div>
      </div>

      <div
        v-if="userStore.credit.records.length"
        class="table-stack"
      >
        <div class="desktop-table">
          <el-table
            v-loading="userStore.creditLoading"
            :data="userStore.credit.records"
            stripe
          >
            <el-table-column label="时间">
              <template #default="{ row }">
                {{ formatDateTime(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column
              prop="changeReason"
              label="变更原因"
              min-width="180"
            />
            <el-table-column label="类型">
              <template #default="{ row }">
                <el-tag size="small">
                  {{ formatReasonType(row.reasonType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="变更值">
              <template #default="{ row }">
                {{ formatSignedNumber(row.delta) }}
              </template>
            </el-table-column>
            <el-table-column label="当前分">
              <template #default="{ row }">
                {{ row.currentScore ?? '--' }}
              </template>
            </el-table-column>
            <el-table-column label="关联业务">
              <template #default="{ row }">
                {{ formatRelatedBiz(row) }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="mobile-record-list">
          <article
            v-for="(row, index) in userStore.credit.records"
            :key="`${row.createdAt}-${index}`"
            class="surface-card mobile-record-card"
          >
            <div class="mobile-record-title">
              <span>{{ formatDateTime(row.createdAt) }}</span>
              <strong>{{ row.changeReason || '信用分变更' }}</strong>
            </div>
            <ul class="mobile-record-fields">
              <li><span>类型</span><strong>{{ formatReasonType(row.reasonType) }}</strong></li>
              <li><span>变更值</span><strong>{{ formatSignedNumber(row.delta) }}</strong></li>
              <li><span>当前分</span><strong>{{ row.currentScore ?? '--' }}</strong></li>
              <li><span>关联业务</span><strong>{{ formatRelatedBiz(row) }}</strong></li>
            </ul>
          </article>
        </div>
        <AppPagination
          :page="userStore.credit.page"
          :page-size="userStore.credit.pageSize"
          :pages="userStore.credit.pages"
          :total="userStore.credit.total"
          @change="handlePageChange"
        />
      </div>
      <EmptyState
        v-else-if="!userStore.creditLoading"
        title="暂无信用分记录"
        description="信用分记录会在投诉处理等场景后自动同步。"
      />
    </PageSection>
  </div>
</template>
