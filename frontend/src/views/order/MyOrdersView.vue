<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import AppPagination from '../../components/common/AppPagination.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useOrderStore } from '../../stores/order'
import { formatJoinStatus, formatOrderStatus, formatPayStatus, formatReceiveStatus, formatRole } from '../../utils/format'

const router = useRouter()
const orderStore = useOrderStore()

const stats = computed(() => [
  {
    label: '参与订单',
    value: orderStore.myOrdersPage.total,
    hint: '对应固定分页结构 total',
  },
  {
    label: '待收货',
    value: orderStore.myOrdersPage.list.filter((item) => item.myReceiveStatus === 'WAIT_CONFIRM').length,
    hint: '优先关注已经进入收货确认阶段的订单',
  },
  {
    label: '待支付',
    value: orderStore.myOrdersPage.list.filter((item) => item.myPayStatus === 'UNPAID').length,
    hint: '便于快速定位还未支付的参与订单',
  },
])

const summaryText = computed(() => {
  if (!orderStore.myOrdersPage.list.length) {
    return '当前还没有参与中的拼单。'
  }

  return '这里按“我的角色、支付状态、收货状态、订单状态”统一查看参与记录，详细动作仍在详情页完成。'
})

const loadOrders = async () => {
  try {
    await orderStore.loadMyOrders({ page: 1, pageSize: 10 })
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handlePageChange = async ({ page, pageSize }) => {
  try {
    await orderStore.loadMyOrders({ page, pageSize })
  } catch (error) {
    ElMessage.error(error.message)
  }
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
      title="我的拼单"
      description="对应 GET /api/users/my-orders。"
    >
      <p class="muted-text">
        {{ summaryText }}
      </p>

      <div class="table-toolbar">
        <span class="table-caption">共 {{ orderStore.myOrdersPage.total }} 条参与记录，可从这里继续进入订单详情。</span>
        <div class="page-actions">
          <el-button @click="router.push('/orders')">
            返回拼单大厅
          </el-button>
          <el-button
            type="primary"
            plain
            @click="router.push('/orders/create')"
          >
            发起新拼单
          </el-button>
        </div>
      </div>

      <div
        v-if="orderStore.myOrdersPage.list.length"
        class="table-stack"
      >
        <div class="desktop-table">
          <el-table
            v-loading="orderStore.myOrdersLoading"
            :data="orderStore.myOrdersPage.list"
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
            <el-table-column label="我的角色">
              <template #default="{ row }">
                {{ formatRole(row.myRole) }}
              </template>
            </el-table-column>
            <el-table-column label="参与状态">
              <template #default="{ row }">
                {{ formatJoinStatus(row.myJoinStatus) }}
              </template>
            </el-table-column>
            <el-table-column label="支付状态">
              <template #default="{ row }">
                {{ formatPayStatus(row.myPayStatus) }}
              </template>
            </el-table-column>
            <el-table-column label="收货状态">
              <template #default="{ row }">
                {{ formatReceiveStatus(row.myReceiveStatus) }}
              </template>
            </el-table-column>
            <el-table-column label="订单状态">
              <template #default="{ row }">
                <StatusTag
                  :value="row.status"
                  :text="formatOrderStatus(row.status)"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template #default="{ row }">
                <el-button
                  link
                  type="primary"
                  @click="router.push(`/orders/${row.orderId}`)"
                >
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="mobile-record-list">
          <article
            v-for="row in orderStore.myOrdersPage.list"
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
              <li><span>我的角色</span><strong>{{ formatRole(row.myRole) }}</strong></li>
              <li><span>参与状态</span><strong>{{ formatJoinStatus(row.myJoinStatus) }}</strong></li>
              <li><span>支付状态</span><strong>{{ formatPayStatus(row.myPayStatus) }}</strong></li>
              <li><span>收货状态</span><strong>{{ formatReceiveStatus(row.myReceiveStatus) }}</strong></li>
            </ul>
            <div class="page-actions">
              <el-button
                type="primary"
                plain
                @click="router.push(`/orders/${row.orderId}`)"
              >
                查看详情
              </el-button>
            </div>
          </article>
        </div>

        <AppPagination
          :page="orderStore.myOrdersPage.page"
          :page-size="orderStore.myOrdersPage.pageSize"
          :pages="orderStore.myOrdersPage.pages"
          :total="orderStore.myOrdersPage.total"
          @change="handlePageChange"
        />
      </div>
      <EmptyState
        v-else
        title="还没有参与的拼单"
        description="加入或创建拼单后，这里会按分页结构展示。"
      >
        <div class="page-actions">
          <el-button @click="router.push('/orders')">
            先去浏览大厅
          </el-button>
          <el-button
            type="primary"
            plain
            @click="router.push('/orders/create')"
          >
            直接发起拼单
          </el-button>
        </div>
      </EmptyState>
    </PageSection>
  </div>
</template>
