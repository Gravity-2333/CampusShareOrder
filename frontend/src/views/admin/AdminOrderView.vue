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
    await adminStore.loadOrders(filters)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const resetFilters = async () => {
  filters.page = 1
  filters.pageSize = 10
  filters.status = ''
  await loadOrders()
}

const handleCancel = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因', '取消订单', {
      cancelButtonText: '取消',
      confirmButtonText: '确认取消',
      inputPlaceholder: '例如：投诉成立，后台介入取消',
      inputValidator: (inputValue) => {
        if (!inputValue?.trim()) {
          return '请输入取消原因'
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
      <StatCard v-for="item in stats" :key="item.label" :label="item.label" :value="item.value" :hint="item.hint" />
    </div>

    <PageSection title="订单管理" description="先收口订单筛选、详情查看和后台取消这一条治理链路。">
      <p class="muted-text">{{ summaryText }}</p>
      <div class="toolbar-row">
        <el-select v-model="filters.status" placeholder="按状态筛选" clearable>
          <el-option label="招募中" value="OPEN" />
          <el-option label="已成团" value="GROUPED" />
          <el-option label="待送达" value="WAIT_DELIVERY" />
          <el-option label="待收货" value="WAIT_RECEIVE" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELED" />
        </el-select>
        <el-button type="primary" @click="loadOrders">查询</el-button>
      </div>

      <div class="table-toolbar">
        <span class="table-caption">共 {{ adminStore.ordersPage.total }} 条订单，优先处理待送达和待收货状态。</span>
        <div class="page-actions">
          <el-button @click="resetFilters">恢复默认筛选</el-button>
        </div>
      </div>

      <div v-if="adminStore.ordersPage.list.length" class="table-stack">
        <el-table v-loading="adminStore.ordersLoading" :data="adminStore.ordersPage.list" stripe>
          <el-table-column prop="orderNo" label="订单号" />
          <el-table-column prop="productName" label="商品" />
          <el-table-column prop="creatorNickname" label="发起人" />
          <el-table-column label="成员进度">
            <template #default="{ row }">{{ row.currentMemberCount }}/{{ row.totalMemberCount }}</template>
          </el-table-column>
          <el-table-column label="状态">
            <template #default="{ row }">
              <StatusTag :value="row.status" :text="formatOrderStatus(row.status)" />
            </template>
          </el-table-column>
          <el-table-column label="截止时间">
            <template #default="{ row }">{{ formatDateTime(row.deadlineAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="220">
            <template #default="{ row }">
              <div class="row-action-group">
                <el-button link type="primary" @click="router.push(`/admin/orders/${row.orderId}`)">
                  查看详情
                </el-button>
                <el-button
                  v-if="row.status !== 'CANCELED' && row.status !== 'COMPLETED'"
                  link
                  type="danger"
                  @click="handleCancel(row)"
                >
                  取消订单
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <AppPagination
          :page="adminStore.ordersPage.page"
          :page-size="adminStore.ordersPage.pageSize"
          :pages="adminStore.ordersPage.pages"
          :total="adminStore.ordersPage.total"
          @change="handlePageChange"
        />
      </div>
      <EmptyState
        v-else
        title="暂无订单"
        description="后续切换 live 时，列表仍只通过 store -> api 层消费。"
      />
    </PageSection>
  </div>
</template>
