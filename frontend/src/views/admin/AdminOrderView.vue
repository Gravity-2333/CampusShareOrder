<script setup>
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

import { useAdminStore } from '../../stores/admin'
import { formatOrderStatus } from '../../utils/format'
import { normalizePageFilters } from '../../utils/page'

const router = useRouter()
const adminStore = useAdminStore()

const filters = reactive({
  keyword: '',
  page: 1,
  pageSize: 10,
  status: '',
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

const normalizeFilters = () => {
  Object.assign(filters, normalizePageFilters(filters))
}

const submitFilters = async () => {
  normalizeFilters()
  filters.page = 1
  await loadOrders()
}

const handleCancel = async (row) => {
  if (adminStore.submitting) return

  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因', '取消订单', {
      cancelButtonText: '取消',
      confirmButtonText: '确认取消',
      inputPlaceholder: '例如：投诉成立，后台介入取消',
      inputValidator: (inputValue) => {
        if (!inputValue?.trim()) return '请输入取消原因'
        if (inputValue.trim().length > 255) return '取消原因长度不能超过 255 个字符'
        return true
      },
    })
    await adminStore.cancelOrder(row.orderId, { reason: value.trim() })
    ElMessage.success('订单已取消')
  } catch (error) {
    if (error === 'cancel' || error?.message === 'cancel') return
    ElMessage.error(error.message || '取消失败，请稍后重试')
  }
}

const handlePageChange = async (page) => {
  normalizeFilters()
  filters.page = page
  await loadOrders()
}

onMounted(loadOrders)
</script>

<template>
  <div class="stack-page">
    <div class="section">
      <div class="section-header">
        <h3>订单列表</h3>
      </div>

      <div class="toolbar">
        <input
          v-model="filters.keyword"
          type="text"
          placeholder="搜索订单号或商品名"
          @keyup.enter="submitFilters"
        >
        <select v-model="filters.status">
          <option value="">
            全部状态
          </option>
          <option value="OPEN">
            招募中
          </option>
          <option value="GROUPED">
            已成团
          </option>
          <option value="COMPLETED">
            已完成
          </option>
        </select>
        <button
          class="btn btn-primary"
          :disabled="adminStore.ordersLoading"
          @click="submitFilters"
        >
          查询
        </button>
      </div>

      <table
        v-if="adminStore.ordersPage.list.length"
        class="table"
      >
        <thead>
          <tr>
            <th>订单号</th>
            <th>商品</th>
            <th>发起人</th>
            <th>人数</th>
            <th>金额</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="row in adminStore.ordersPage.list"
            :key="row.orderId"
          >
            <td>{{ row.orderNo }}</td>
            <td>{{ row.productName }}</td>
            <td>{{ row.creatorNickname || '--' }}</td>
            <td>{{ row.currentMemberCount }}/{{ row.totalMemberCount }}</td>
            <td>{{ row.estimatedTotalAmount ?? '--' }}</td>
            <td>
              <span
                class="tag"
                :class="`tag-${row.status === 'OPEN' ? 'warning' : row.status === 'GROUPED' || row.status === 'WAIT_DELIVERY' ? 'info' : row.status === 'COMPLETED' ? 'success' : 'info'}`"
              >
                {{ formatOrderStatus(row.status) }}
              </span>
            </td>
            <td>
              <a
                href="#"
                class="table-link"
                @click.prevent="router.push(`/admin/orders/${row.orderId}`)"
              >详情</a>
              <a
                v-if="row.status !== 'CANCELED' && row.status !== 'COMPLETED'"
                href="#"
                class="table-link"
                style="color: #dc3545; margin-left: 0.5rem;"
                @click.prevent="handleCancel(row)"
              >取消</a>
            </td>
          </tr>
        </tbody>
      </table>
      <p
        v-else-if="!adminStore.ordersLoading"
        class="muted-text"
      >
        暂无订单数据。
      </p>

      <div
        v-if="adminStore.ordersPage.pages > 1"
        class="pagination"
      >
        <button
          :disabled="adminStore.ordersPage.page <= 1"
          @click="handlePageChange(adminStore.ordersPage.page - 1)"
        >
          上一页
        </button>
        <button
          v-for="p in adminStore.ordersPage.pages"
          :key="p"
          :class="{ active: p === adminStore.ordersPage.page }"
          @click="handlePageChange(p)"
        >
          {{ p }}
        </button>
        <button
          :disabled="adminStore.ordersPage.page >= adminStore.ordersPage.pages"
          @click="handlePageChange(adminStore.ordersPage.page + 1)"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>
