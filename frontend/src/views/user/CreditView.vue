<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import AppPagination from '../../components/common/AppPagination.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import PageSection from '../../components/common/PageSection.vue'
import { useUserStore } from '../../stores/user'
import { creditReasonTypeTextMap } from '../../utils/enum'
import { formatDateTime, formatSignedNumber } from '../../utils/format'

const router = useRouter()
const userStore = useUserStore()

const creditLevels = [
  { key: 'E', label: 'E', max: 20, min: 0, range: '0-20' },
  { key: 'D', label: 'D', max: 40, min: 21, range: '21-40' },
  { key: 'C', label: 'C', max: 60, min: 41, range: '41-60' },
  { key: 'B', label: 'B', max: 80, min: 61, range: '61-80' },
  { key: 'A', label: 'A', max: 100, min: 81, range: '81-100' },
]

const creditScore = computed(() => Number(userStore.credit.creditScore || 0))
const creditPercent = computed(() => `${Math.min(Math.max(creditScore.value, 0), 100)}%`)
const currentCreditLevel = computed(
  () => creditLevels.find((level) => creditScore.value >= level.min && creditScore.value <= level.max) || creditLevels[0],
)

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
    <PageSection
      title="信用分"
      description="查看当前信用等级、信用分区间和历史变更记录。"
    >
      <section class="credit-display-panel">
        <div class="credit-display">
          <div class="credit-score">
            {{ userStore.credit.creditScore }}
          </div>
          <div class="credit-level">
            {{ currentCreditLevel.label }}
          </div>
          <div class="credit-range">
            当前信用分范围：{{ currentCreditLevel.range }}
          </div>
          <div
            class="credit-bar"
            role="presentation"
          >
            <div
              class="credit-bar-fill"
              :style="{ width: creditPercent }"
            />
          </div>
          <div class="credit-level-grid">
            <article
              v-for="level in creditLevels"
              :key="level.key"
              class="credit-level-item"
              :class="{ active: currentCreditLevel.key === level.key }"
            >
              <strong>{{ level.label }}</strong>
              <span>{{ level.range }}</span>
            </article>
          </div>
        </div>
      </section>

      <section class="credit-record-section">
        <div class="table-toolbar">
          <div>
            <h3>信用记录</h3>
            <p class="muted-text">
              {{ summaryText }}
            </p>
          </div>
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
              <el-table-column label="类型">
                <template #default="{ row }">
                  <el-tag
                    size="small"
                    :type="Number(row.delta) >= 0 ? 'success' : 'danger'"
                  >
                    {{ Number(row.delta) >= 0 ? '加分' : '扣分' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="变更">
                <template #default="{ row }">
                  <strong :class="Number(row.delta) >= 0 ? 'score-plus' : 'score-minus'">
                    {{ formatSignedNumber(row.delta) }}
                  </strong>
                </template>
              </el-table-column>
              <el-table-column label="变更后分数">
                <template #default="{ row }">
                  {{ row.currentScore ?? '--' }}
                </template>
              </el-table-column>
              <el-table-column
                prop="changeReason"
                label="描述"
                min-width="180"
              />
              <el-table-column label="原因">
                <template #default="{ row }">
                  {{ formatReasonType(row.reasonType) }}
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
                <li><span>类型</span><strong>{{ Number(row.delta) >= 0 ? '加分' : '扣分' }}</strong></li>
                <li><span>变更</span><strong>{{ formatSignedNumber(row.delta) }}</strong></li>
                <li><span>变更后分数</span><strong>{{ row.currentScore ?? '--' }}</strong></li>
                <li><span>原因</span><strong>{{ formatReasonType(row.reasonType) }}</strong></li>
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
      </section>
    </PageSection>
  </div>
</template>
