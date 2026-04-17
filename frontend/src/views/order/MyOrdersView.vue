<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import AppPagination from '../../components/common/AppPagination.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import PageSection from '../../components/common/PageSection.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useOrderStore } from '../../stores/order'
import { formatOrderStatus, formatPayStatus, formatReceiveStatus, formatRole } from '../../utils/format'

const router = useRouter()
const orderStore = useOrderStore()

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
  <PageSection title="我的拼单" description="对应 GET /api/users/my-orders。">
    <div v-if="orderStore.myOrdersPage.list.length" class="table-stack">
      <el-table
        v-loading="orderStore.myOrdersLoading"
        :data="orderStore.myOrdersPage.list"
        stripe
      >
        <el-table-column prop="orderNo" label="订单号" />
        <el-table-column prop="productName" label="商品" />
        <el-table-column label="我的角色">
          <template #default="{ row }">{{ formatRole(row.myRole) }}</template>
        </el-table-column>
        <el-table-column prop="myJoinStatus" label="参与状态" />
        <el-table-column label="支付状态">
          <template #default="{ row }">{{ formatPayStatus(row.myPayStatus) }}</template>
        </el-table-column>
        <el-table-column label="收货状态">
          <template #default="{ row }">{{ formatReceiveStatus(row.myReceiveStatus) }}</template>
        </el-table-column>
        <el-table-column label="订单状态">
          <template #default="{ row }">
            <StatusTag :value="row.status" :text="formatOrderStatus(row.status)" />
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/orders/${row.orderId}`)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
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
    />
  </PageSection>
</template>
