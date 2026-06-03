<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import StatCard from '../../components/common/StatCard.vue'
import { useAdminStore } from '../../stores/admin'
import { formatComplaintStatus, formatComplaintType, formatDateTime, formatOrderStatus } from '../../utils/format'

const router = useRouter()
const adminStore = useAdminStore()

const cards = computed(() => [
  { label: '总订单数', value: adminStore.metrics.orders },
  { label: '待处理投诉', value: adminStore.metrics.complaints },
  { label: '注册用户', value: adminStore.metrics.users },
  { label: '今日新增', value: adminStore.metrics.todayNew || '--' },
])

// 过滤只显示待处理的投诉
const pendingComplaints = computed(() => {
  return adminStore.dashboardOverview.recentComplaints.filter(
    complaint => complaint.status === 'PENDING'
  )
})

const loadDashboard = async () => {
  try {
    await adminStore.loadDashboardOverview()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

onMounted(loadDashboard)
</script>

<template>
  <div class="stack-page">
    <div class="stats-grid">
      <StatCard
        v-for="item in cards"
        :key="item.label"
        :label="item.label"
        :value="item.value"
      />
    </div>

    <div class="section">
      <div class="section-header">
        <h3>待处理投诉</h3>
        <button
          class="btn btn-primary"
          @click="router.push('/admin/complaints')"
        >
          查看全部
        </button>
      </div>

      <table
        v-if="pendingComplaints.length"
        class="table"
      >
        <thead>
          <tr>
            <th>投诉单号</th>
            <th>关联订单</th>
            <th>投诉人</th>
            <th>类型</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="row in pendingComplaints"
            :key="row.complaintId || row.complaintNo"
          >
            <td>{{ row.complaintNo }}</td>
            <td>{{ row.orderNo || '--' }}</td>
            <td>{{ row.complainantNickname || '--' }}</td>
            <td>{{ formatComplaintType(row.type) }}</td>
            <td>
              <span
                class="tag"
                :class="`tag-${row.status === 'PENDING' ? 'warning' : 'success'}`"
              >
                {{ formatComplaintStatus(row.status) }}
              </span>
            </td>
            <td>
              <a
                :href="`/admin/complaints/${row.complaintId}`"
                @click.prevent="router.push(`/admin/complaints/${row.complaintId}`)"
              >处理</a>
            </td>
          </tr>
        </tbody>
      </table>
      <p
        v-else
        class="muted-text"
      >
        暂无待处理投诉。
      </p>
    </div>

    <div class="section">
      <div class="section-header">
        <h3>最近订单</h3>
        <button
          class="btn btn-secondary"
          @click="router.push('/admin/orders')"
        >
          查看全部
        </button>
      </div>

      <table
        v-if="adminStore.dashboardOverview.recentOrders.length"
        class="table"
      >
        <thead>
          <tr>
            <th>订单号</th>
            <th>商品</th>
            <th>发起人</th>
            <th>状态</th>
            <th>时间</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="row in adminStore.dashboardOverview.recentOrders"
            :key="row.orderId || row.orderNo"
          >
            <td>{{ row.orderNo }}</td>
            <td>{{ row.productName }}</td>
            <td>{{ row.creatorNickname || '--' }}</td>
            <td>
              <span
                class="tag"
                :class="`tag-${row.status === 'OPEN' ? 'warning' : row.status === 'COMPLETED' ? 'success' : row.status === 'WAIT_DELIVERY' || row.status === 'WAIT_RECEIVE' ? 'info' : 'success'}`"
              >
                {{ formatOrderStatus(row.status) }}
              </span>
            </td>
            <td>{{ formatDateTime(row.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
      <p
        v-else
        class="muted-text"
      >
        暂无订单数据。
      </p>
    </div>
  </div>
</template>
