<script setup>
import { onMounted } from 'vue'
import { ElMessage } from 'element-plus'

import AppPagination from '../../components/common/AppPagination.vue'
import PageSection from '../../components/common/PageSection.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useAdminStore } from '../../stores/admin'
import { formatOrderStatus } from '../../utils/format'

const adminStore = useAdminStore()

const loadOrders = async () => {
  try {
    await adminStore.loadOrders({ page: 1, pageSize: 10 })
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handleCancel = async (row) => {
  try {
    await adminStore.cancelOrder(row.orderId)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handlePageChange = async ({ page, pageSize }) => {
  try {
    await adminStore.loadOrders({ page, pageSize })
  } catch (error) {
    ElMessage.error(error.message)
  }
}

onMounted(loadOrders)
</script>

<template>
  <PageSection title="订单管理" description="这里预留了和真实管理端对接时的字段位置。">
    <div class="table-stack">
      <el-table v-loading="adminStore.ordersLoading" :data="adminStore.ordersPage.list" stripe>
        <el-table-column prop="orderNo" label="订单号" />
        <el-table-column prop="productName" label="商品" />
        <el-table-column prop="creatorNickname" label="发起人" />
        <el-table-column label="状态">
          <template #default="{ row }">
            <StatusTag :value="row.status" :text="formatOrderStatus(row.status)" />
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button link type="danger" @click="handleCancel(row)">取消订单</el-button>
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
  </PageSection>
</template>
