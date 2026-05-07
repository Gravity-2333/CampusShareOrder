<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import AppPagination from '../../components/common/AppPagination.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useOrderStore } from '../../stores/order'
import { useUserStore } from '../../stores/user'
import { formatCurrency, formatDateTime, formatOrderStatus } from '../../utils/format'

const router = useRouter()
const orderStore = useOrderStore()
const userStore = useUserStore()
const filters = reactive({
  keyword: '',
  page: 1,
  pageSize: 10,
  status: '',
})

const visibleOrders = computed(() => orderStore.hallPage.list)
const canCreateOrder = computed(() => userStore.session.isVerified)

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

const hallTips = computed(() => {
  if (!visibleOrders.value.length) {
    return '当前筛选条件下没有可展示的拼单。'
  }

  if (!canCreateOrder.value) {
    return '当前账号尚未实名认证，可以先浏览大厅，认证后再发起拼单。'
  }

  return '浏览正在进行的拼单，选择合适的订单加入，或发起自己的拼单。'
})

const loadOrders = async () => {
  try {
    await orderStore.loadHallOrders(filters)
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

const handleJoin = async (order) => {
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

const handleJoinAndView = async (order) => {
  if (!canJoinOrder(order)) {
    ElMessage.warning('当前订单已不可直接加入')
    return
  }

  try {
    await orderStore.joinExistingOrder(order.orderId)
    ElMessage.success('加入成功，已同步刷新大厅与我的拼单')
    router.push(`/orders/${order.orderId}`)
  } catch (error) {
    ElMessage.error(error.message)
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
      title="拼单大厅"
      description="浏览校园内正在招募的拼单，按商品或状态快速筛选。"
    >
      <p class="muted-text">
        {{ hallTips }}
      </p>

      <div class="toolbar-row">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索商品名或订单号"
          clearable
          @keyup.enter="submitFilters"
        />
        <el-select
          v-model="filters.status"
          placeholder="全部状态"
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
          :loading="orderStore.hallLoading"
          @click="submitFilters"
        >
          查询
        </el-button>
      </div>

      <div class="page-actions wrap-actions">
        <el-button @click="resetFilters">
          重置筛选
        </el-button>
        <el-button
          type="primary"
          plain
          @click="goCreateOrder"
        >
          {{ canCreateOrder ? '发起拼单' : '先去认证再发起' }}
        </el-button>
        <el-button
          plain
          @click="router.push('/my-orders')"
        >
          查看我的拼单
        </el-button>
      </div>

      <template v-if="visibleOrders.length">
        <div class="order-grid">
          <article
            v-for="order in visibleOrders"
            :key="order.orderId"
            class="surface-card order-card"
            :class="{ 'is-canceled-order': order.status === 'CANCELED' }"
          >
            <div class="card-header-row">
              <div>
                <p class="section-kicker">
                  {{ order.orderNo }}
                </p>
                <h3>{{ order.productName }}</h3>
              </div>
              <StatusTag
                :value="order.status"
                :text="formatOrderStatus(order.status)"
              />
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
                <span>剩余名额</span>
                <strong>{{ order.remainingCount }}</strong>
              </li>
              <li>
                <span>预计金额</span>
                <strong>{{ formatCurrency(order.estimatedTotalAmount) }}</strong>
              </li>
            </ul>
            <div class="page-actions">
              <el-button @click="router.push(`/orders/${order.orderId}`)">
                查看详情
              </el-button>
              <el-button
                type="primary"
                plain
                :disabled="!canJoinOrder(order)"
                :loading="orderStore.submitting"
                @click="handleJoin(order)"
              >
                {{ getJoinButtonText(order) }}
              </el-button>
              <el-button
                v-if="canJoinOrder(order)"
                type="primary"
                :loading="orderStore.submitting"
                @click="handleJoinAndView(order)"
              >
                加入并查看详情
              </el-button>
            </div>
          </article>
        </div>
        <AppPagination
          :page="orderStore.hallPage.page"
          :page-size="orderStore.hallPage.pageSize"
          :pages="orderStore.hallPage.pages"
          :total="orderStore.hallPage.total"
          @change="handlePageChange"
        />
      </template>
      <EmptyState
        v-else
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
    </PageSection>
  </div>
</template>
