<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

import AppPagination from '../../components/common/AppPagination.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useAdminStore } from '../../stores/admin'
import { formatDateTime, formatOrderStatus } from '../../utils/format'

const router = useRouter()
const adminStore = useAdminStore()

const filters = reactive({
  keyword: '',
  page: 1,
  pageSize: 10,
  status: '',
})

const stats = computed(() => [
  {
    label: '订单总量',
    value: adminStore.ordersPage.total,
    hint: '管理端订单列表遵循固定分页结构',
  },
  {
    label: '开放订单',
    value: adminStore.ordersPage.list.filter((item) => item.status === 'OPEN').length,
    hint: '优先关注仍可继续流转的订单',
  },
  {
    label: '异常关注',
    value: adminStore.ordersPage.list.filter(
      (item) => item.status === 'WAIT_DELIVERY' || item.status === 'WAIT_RECEIVE',
    ).length,
    hint: '待送达与待收货订单更需要后台关注',
  },
])

const summaryText = computed(() => {
  if (!adminStore.ordersPage.list.length) {
    return '当前筛选条件下没有订单数据。'
  }

  if (filters.status) {
    return '当前列表已经按订单状态筛选，适合集中处理同一阶段的治理动作。'
  }

  return '后台列表优先用于定位异常状态、查看详情和执行取消，不在列表页重复推导复杂详情信息。'
})

const loadOrders = async () => {
  try {
    const page = await adminStore.loadOrders(filters)
    filters.page = page.page
    filters.pageSize = page.pageSize
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const submitFilters = async () => {
  filters.page = 1
  await loadOrders()
}

const resetFilters = async () => {
  filters.keyword = ''
  filters.page = 1
  filters.pageSize = 10
  filters.status = ''
  await loadOrders()
}

const handleCancel = async (row) => {
  if (adminStore.submitting) {
    return
  }

  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因', '取消订单', {
      cancelButtonText: '取消',
      confirmButtonText: '确认取消',
      inputPlaceholder: '例如：投诉成立，后台介入取消',
      inputValidator: (inputValue) => {
        if (!inputValue?.trim()) {
          return '请输入取消原因'
        }

        if (inputValue.trim().length > 255) {
          return '取消原因长度不能超过 255 个字符'
        }

        return true
      },
    })

    await adminStore.cancelOrder(row.orderId, { reason: value.trim() })
    ElMessage.success('订单已取消')
  } catch (error) {
    if (error === 'cancel' || error?.message === 'cancel') {
      return
    }

    ElMessage.error(error.message || '取消失败，请稍后重试')
  }
}

const handlePageChange = async ({ page, pageSize }) => {
  filters.page = page
  filters.pageSize = pageSize
  await loadOrders()
}

onMounted(loadOrders)
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
      title="订单管理"
      description="先收口订单筛选、详情查看和后台取消这一条治理链路。"
    >
      <p class="muted-text">
        {{ summaryText }}
      </p>
      <div class="toolbar-row">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索订单号、商品或取货点"
          clearable
          @keyup.enter="submitFilters"
        />
        <el-select
          v-model="filters.status"
          placeholder="按状态筛选"
          clearable
        >
          <el-option
            label="招募中"
            value="OPEN"
          />
          <el-option
            label="已成团"
            value="GROUPED"
          />
          <el-option
            label="待送达"
            value="WAIT_DELIVERY"
          />
          <el-option
            label="待收货"
            value="WAIT_RECEIVE"
          />
          <el-option
            label="已完成"
            value="COMPLETED"
          />
          <el-option
            label="已取消"
            value="CANCELED"
          />
        </el-select>
        <el-button
          type="primary"
          :loading="adminStore.ordersLoading"
          @click="submitFilters"
        >
          查询
        </el-button>
      </div>

      <div class="table-toolbar">
        <span class="table-caption">共 {{ adminStore.ordersPage.total }} 条订单，优先处理待送达和待收货状态。</span>
        <div class="page-actions">
          <el-button
            :disabled="adminStore.ordersLoading"
            @click="resetFilters"
          >
            恢复默认筛选
          </el-button>
        </div>
      </div>

      <div
        v-if="adminStore.ordersPage.list.length"
        class="table-stack"
      >
        <div class="desktop-table">
          <el-table
            v-loading="adminStore.ordersLoading"
            :data="adminStore.ordersPage.list"
            stripe
          >
            <el-table-column
              prop="orderNo"
              label="订单号"
            />
            <el-table-column
              prop="productName"
              label="商品"
            />
            <el-table-column
              prop="creatorNickname"
              label="发起人"
            />
            <el-table-column label="成员进度">
              <template #default="{ row }">
                {{ row.currentMemberCount }}/{{ row.totalMemberCount }}
              </template>
            </el-table-column>
            <el-table-column label="状态">
              <template #default="{ row }">
                <StatusTag
                  :value="row.status"
                  :text="formatOrderStatus(row.status)"
                />
              </template>
            </el-table-column>
            <el-table-column label="截止时间">
              <template #default="{ row }">
                {{ formatDateTime(row.deadlineAt) }}
              </template>
            </el-table-column>
            <el-table-column
              label="操作"
              width="220"
            >
              <template #default="{ row }">
                <div class="row-action-group">
                  <el-button
                    link
                    type="primary"
                    @click="router.push(`/admin/orders/${row.orderId}`)"
                  >
                    查看详情
                  </el-button>
                  <el-button
                    v-if="row.status !== 'CANCELED' && row.status !== 'COMPLETED'"
                    link
                    type="danger"
                    :disabled="adminStore.submitting"
                    @click="handleCancel(row)"
                  >
                    取消订单
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="mobile-record-list">
          <article
            v-for="row in adminStore.ordersPage.list"
            :key="row.orderId"
            class="surface-card mobile-record-card"
          >
            <div class="mobile-record-header">
              <div class="mobile-record-title">
                <span>{{ row.orderNo }}</span>
                <strong>{{ row.productName }}</strong>
              </div>
              <StatusTag
                :value="row.status"
                :text="formatOrderStatus(row.status)"
              />
            </div>
            <ul class="mobile-record-fields">
              <li><span>发起人</span><strong>{{ row.creatorNickname || '--' }}</strong></li>
              <li><span>成员进度</span><strong>{{ row.currentMemberCount }}/{{ row.totalMemberCount }}</strong></li>
              <li><span>截止时间</span><strong>{{ formatDateTime(row.deadlineAt) }}</strong></li>
            </ul>
            <div class="page-actions">
              <el-button
                type="primary"
                plain
                @click="router.push(`/admin/orders/${row.orderId}`)"
              >
                查看详情
              </el-button>
              <el-button
                v-if="row.status !== 'CANCELED' && row.status !== 'COMPLETED'"
                type="danger"
                plain
                :loading="adminStore.submitting"
                @click="handleCancel(row)"
              >
                取消订单
              </el-button>
            </div>
          </article>
        </div>

        <AppPagination
          :page="adminStore.ordersPage.page"
          :page-size="adminStore.ordersPage.pageSize"
          :pages="adminStore.ordersPage.pages"
          :total="adminStore.ordersPage.total"
          @change="handlePageChange"
        />
      </div>
      <EmptyState
        v-else-if="!adminStore.ordersLoading"
        title="暂无订单"
        description="订单列表统一通过 store -> api 层消费，便于持续维护。"
      />
    </PageSection>
  </div>
</template>
