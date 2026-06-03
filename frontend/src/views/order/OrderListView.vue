<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import EmptyState from '../../components/common/EmptyState.vue'
import StatCard from '../../components/common/StatCard.vue'
import { useOrderStore } from '../../stores/order'
import { useUserStore } from '../../stores/user'
import { formatCurrency, formatDateTime, formatOrderStatus } from '../../utils/format'
import { normalizePageFilters } from '../../utils/page'

const router = useRouter()
const orderStore = useOrderStore()
const userStore = useUserStore()
const filters = reactive({
  keyword: '',
  page: 1,
  pageSize: 3,
  status: '',
})

const visibleOrders = computed(() => orderStore.hallPage.list)
const canCreateOrder = computed(() => userStore.session.isVerified)
const pageNumbers = computed(() => Array.from(
  { length: orderStore.hallPage.pages || 1 },
  (_, index) => index + 1,
))

const stats = computed(() => {
  const openCount = visibleOrders.value.filter((order) => order.status === 'OPEN').length
  const remainingSeats = visibleOrders.value.reduce(
    (total, order) => total + Number(order.remainingCount || 0),
    0,
  )

  return [
    {
      label: '可见订单',
      value: orderStore.hallPage.total,
      hint: '当前筛选条件下的拼单数量',
    },
    {
      label: '招募中',
      value: openCount,
      hint: '当前筛选结果里仍可继续加入的拼单数',
    },
    {
      label: '剩余名额',
      value: remainingSeats,
      hint: '基于 remainingCount 聚合，便于快速浏览大厅供给',
    },
    {
      label: '当前页',
      value: `${orderStore.hallPage.page}/${orderStore.hallPage.pages || 1}`,
      hint: '继续翻页查看更多拼单',
    },
  ]
})

const loadOrders = async () => {
  try {
    const page = await orderStore.loadHallOrders(filters)
    filters.page = page.page
    filters.pageSize = page.pageSize
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const normalizeFilters = () => {
  Object.assign(filters, normalizePageFilters(filters))
}

const submitFilters = async () => {
  normalizeFilters()
  filters.page = 1
  await loadOrders()
}

const resetFilters = async () => {
  filters.keyword = ''
  filters.page = 1
  filters.pageSize = 3
  filters.status = ''
  await loadOrders()
}

const goCreateOrder = () => {
  if (canCreateOrder.value) {
    router.push('/orders/create')
    return
  }

  ElMessage.warning('请先完成实名认证，再发起拼单')
  router.push('/verify-student')
}

const canJoinOrder = (order) => order.status === 'OPEN' && Number(order.remainingCount || 0) > 0

const getJoinButtonText = (order) => {
  if (order.status !== 'OPEN') {
    return '当前不可加入'
  }

  if (Number(order.remainingCount || 0) <= 0) {
    return '名额已满'
  }

  return '快速加入'
}

const getStatusTagClass = (status) => {
  if (status === 'OPEN') {
    return 'tag-warning'
  }

  if (status === 'GROUPED' || status === 'COMPLETED') {
    return 'tag-success'
  }

  if (status === 'CANCELED') {
    return 'tag-danger'
  }

  return 'tag-info'
}

const handleJoin = async (order) => {
  if (orderStore.submitting) {
    return
  }

  if (!canJoinOrder(order)) {
    if (order.status !== 'OPEN') {
      ElMessage.warning('当前订单已不可直接加入')
      return
    }

    ElMessage.warning('当前订单名额已满')
    return
  }

  try {
    await orderStore.joinExistingOrder(order.orderId)
    ElMessage.success('加入成功，已同步刷新大厅与我的拼单')
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handlePageChange = async (page) => {
  normalizeFilters()
  filters.page = page
  filters.pageSize = 3
  await loadOrders()
}

onMounted(loadOrders)
</script>

<template>
  <div class="stack-page order-hall-page">
    <div class="stats-grid">
      <StatCard
        v-for="item in stats"
        :key="item.label"
        :label="item.label"
        :value="item.value"
        :hint="item.hint"
      />
    </div>

    <div class="section">
      <div class="section-header">
        <h3>拼单大厅</h3>
        <div class="page-actions">
          <button
            class="btn btn-secondary"
            :disabled="orderStore.hallLoading"
            @click="resetFilters"
          >
            重置筛选
          </button>
          <button
            class="btn btn-outline"
            @click="goCreateOrder"
          >
            {{ canCreateOrder ? '发起拼单' : '先去认证再发起' }}
          </button>
          <button
            class="btn btn-secondary"
            @click="router.push('/my-orders')"
          >
            查看我的拼单
          </button>
        </div>
      </div>

      <div class="toolbar">
        <input
          v-model="filters.keyword"
          type="text"
          placeholder="搜索商品名或订单号"
          @keyup.enter="submitFilters"
        >
        <select
          v-model="filters.status"
        >
          <option value="">
            全部状态
          </option>
          <option value="OPEN">
            招募中
          </option>
          <option value="GROUPED">
            已成团
          </option>
          <option value="WAIT_DELIVERY">
            待送达
          </option>
          <option value="WAIT_RECEIVE">
            待收货
          </option>
          <option value="COMPLETED">
            已完成
          </option>
        </select>
        <button
          class="btn btn-primary"
          :disabled="orderStore.hallLoading"
          @click="submitFilters"
        >
          查询
        </button>
      </div>

      <template v-if="visibleOrders.length">
        <div class="order-grid">
          <article
            v-for="order in visibleOrders"
            :key="order.orderId"
            class="order-card"
            :class="{ 'is-canceled-order': order.status === 'CANCELED' }"
          >
            <div class="header">
              <div>
                <div class="kicker">
                  {{ order.orderNo }}
                </div>
                <h4>{{ order.productName }}</h4>
              </div>
              <span
                class="tag"
                :class="getStatusTagClass(order.status)"
              >
                {{ formatOrderStatus(order.status) }}
              </span>
            </div>
            <ul class="detail-list">
              <li><span>取货点</span><strong>{{ order.pickupPoint }}</strong></li>
              <li><span>发起人</span><strong>{{ order.creatorNickname }}</strong></li>
              <li><span>截止时间</span><strong>{{ formatDateTime(order.deadlineAt) }}</strong></li>
              <li>
                <span>招募进度</span>
                <strong>{{ order.currentMemberCount }}/{{ order.totalMemberCount }}</strong>
              </li>
              <li>
                <span>预计金额</span>
                <strong>{{ formatCurrency(order.estimatedTotalAmount) }}</strong>
              </li>
            </ul>
            <div class="actions">
              <button
                class="btn btn-secondary btn-sm"
                @click="router.push(`/orders/${order.orderId}`)"
              >
                查看详情
              </button>
              <button
                class="btn btn-primary btn-sm"
                :disabled="!canJoinOrder(order)"
                @click="handleJoin(order)"
              >
                {{ getJoinButtonText(order) }}
              </button>
            </div>
          </article>
        </div>
        <div
          v-if="orderStore.hallPage.total > 0"
          class="pagination"
        >
          <button
            :disabled="orderStore.hallPage.page <= 1"
            @click="handlePageChange(orderStore.hallPage.page - 1)"
          >
            上一页
          </button>
          <button
            v-for="page in pageNumbers"
            :key="page"
            :class="{ active: page === orderStore.hallPage.page }"
            @click="handlePageChange(page)"
          >
            {{ page }}
          </button>
          <button
            :disabled="orderStore.hallPage.page >= (orderStore.hallPage.pages || 1)"
            @click="handlePageChange(orderStore.hallPage.page + 1)"
          >
            下一页
          </button>
        </div>
      </template>
      <EmptyState
        v-else-if="!orderStore.hallLoading"
        title="暂无匹配的拼单"
        description="可以调整筛选条件，或在完成实名认证后自己发起一个新的拼单。"
      >
        <div class="page-actions">
          <el-button @click="resetFilters">
            恢复默认筛选
          </el-button>
          <el-button
            type="primary"
            plain
            @click="goCreateOrder"
          >
            {{ canCreateOrder ? '去发起拼单' : '去完成认证' }}
          </el-button>
        </div>
      </EmptyState>
    </div>
  </div>
</template>
