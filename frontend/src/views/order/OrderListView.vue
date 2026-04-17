<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import EmptyState from '../../components/common/EmptyState.vue'
import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useOrderStore } from '../../stores/order'
import { formatCurrency, formatDateTime, formatOrderStatus } from '../../utils/format'

const router = useRouter()
const orderStore = useOrderStore()
const filters = reactive({
  keyword: '',
  page: 1,
  pageSize: 10,
  status: '',
})

const stats = computed(() => [
  {
    label: '可见订单',
    value: orderStore.hallPage.total,
    hint: '契约分页结构 list/page/pageSize/total/pages',
  },
  {
    label: '当前页',
    value: `${orderStore.hallPage.page}/${orderStore.hallPage.pages || 1}`,
    hint: '支持后续切换到真实分页接口',
  },
])

const loadOrders = async () => {
  try {
    await orderStore.loadHallOrders(filters)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handleJoin = async (orderId) => {
  try {
    await orderStore.joinExistingOrder(orderId)
    ElMessage.success('加入成功')
  } catch (error) {
    ElMessage.error(error.message)
  }
}

onMounted(loadOrders)
</script>

<template>
  <div class="stack-page">
    <div class="stats-grid">
      <StatCard v-for="item in stats" :key="item.label" :label="item.label" :value="item.value" :hint="item.hint" />
    </div>

    <PageSection title="拼单大厅" description="页面只使用统一 API 服务，不直接拼接接口地址。">
      <div class="toolbar-row">
        <el-input v-model="filters.keyword" placeholder="搜索商品名或订单号" clearable />
        <el-select v-model="filters.status" placeholder="全部状态" clearable>
          <el-option label="招募中" value="OPEN" />
          <el-option label="已成团" value="GROUPED" />
          <el-option label="待送达" value="WAIT_DELIVERY" />
          <el-option label="待收货" value="WAIT_RECEIVE" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELED" />
        </el-select>
        <el-button type="primary" @click="loadOrders">查询</el-button>
      </div>

      <template v-if="orderStore.hallPage.list.length">
        <div class="order-grid">
          <article
            v-for="order in orderStore.hallPage.list"
            :key="order.orderId"
            class="surface-card order-card"
          >
            <div class="card-header-row">
              <div>
                <p class="section-kicker">{{ order.orderNo }}</p>
                <h3>{{ order.productName }}</h3>
              </div>
              <StatusTag :value="order.status" :text="formatOrderStatus(order.status)" />
            </div>
            <ul class="detail-list">
              <li><span>取货点</span><strong>{{ order.pickupPoint }}</strong></li>
              <li><span>发起人</span><strong>{{ order.creatorNickname }}</strong></li>
              <li><span>截止时间</span><strong>{{ formatDateTime(order.deadlineAt) }}</strong></li>
              <li>
                <span>金额</span>
                <strong>{{ formatCurrency(order.estimatedTotalAmount) }}</strong>
              </li>
            </ul>
            <div class="page-actions">
              <el-button @click="router.push(`/orders/${order.orderId}`)">查看详情</el-button>
              <el-button type="primary" plain @click="handleJoin(order.orderId)">快速加入</el-button>
            </div>
          </article>
        </div>
      </template>
      <EmptyState v-else title="暂无拼单" description="切换到 live 后只需替换 provider，无需修改本页展示逻辑。" />
    </PageSection>
  </div>
</template>
