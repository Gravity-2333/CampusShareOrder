<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

import { useAdminStore } from '../../stores/admin'
import {
  formatCurrency,
  formatDateTime,
  formatOrderStatus,
  formatPayStatus,
  formatReceiveStatus,
  formatRole,
} from '../../utils/format'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

const detail = computed(() => adminStore.orderDetail)
const currentOrderId = computed(() => route.params.orderId)
const normalizedOrderId = computed(() => Number(currentOrderId.value || 0))
const isValidOrderId = computed(() => Number.isInteger(normalizedOrderId.value) && normalizedOrderId.value > 0)

const loadDetail = async (orderId = route.params.orderId) => {
  if (!isValidOrderId.value) {
    adminStore.orderDetail = null
    return
  }

  try {
    await adminStore.loadOrderDetail(orderId)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handleCancel = async () => {
  if (!detail.value || adminStore.submitting) return

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
    await adminStore.cancelOrder(detail.value.basicInfo.orderId, { reason: value.trim() })
    ElMessage.success('订单已取消')
  } catch (error) {
    if (error === 'cancel' || error?.message === 'cancel') return
    ElMessage.error(error.message || '取消失败，请稍后重试')
  }
}

watch(
  () => route.params.orderId,
  (orderId, previousOrderId) => {
    if (orderId && orderId !== previousOrderId) loadDetail(orderId)
  },
)

onMounted(() => loadDetail())
</script>

<template>
  <div class="stack-page">
    <div
      v-if="detail"
      class="section"
    >
      <div class="section-header">
        <div>
          <span
            class="tag"
            :class="`tag-${detail.basicInfo.status === 'OPEN' ? 'warning' : detail.basicInfo.status === 'GROUPED' || detail.basicInfo.status === 'WAIT_DELIVERY' ? 'info' : detail.basicInfo.status === 'COMPLETED' ? 'success' : 'info'}`"
          >
            {{ formatOrderStatus(detail.basicInfo.status) }}
          </span>
          <h3>订单号：{{ detail.basicInfo.orderNo }}</h3>
        </div>
        <button
          v-if="detail.basicInfo.status !== 'CANCELED' && detail.basicInfo.status !== 'COMPLETED'"
          class="btn btn-danger"
          :disabled="adminStore.submitting"
          @click="handleCancel"
        >
          取消订单
        </button>
      </div>

      <div class="detail-panel">
        <h4>订单信息</h4>
        <ul class="detail-list">
          <li><span>商品名称</span><strong>{{ detail.basicInfo.productName }}</strong></li>
          <li><span>取货点</span><strong>{{ detail.basicInfo.pickupPoint }}</strong></li>
          <li><span>截止时间</span><strong>{{ formatDateTime(detail.basicInfo.deadlineAt) }}</strong></li>
          <li><span>发起人</span><strong>{{ detail.initiatorInfo.nickname }}</strong></li>
          <li><span>预计金额</span><strong>{{ formatCurrency(detail.paymentSummary.estimatedTotalAmount) }}</strong></li>
          <li><span>招募人数</span><strong>{{ detail.basicInfo.currentMemberCount }}/{{ detail.basicInfo.totalMemberCount }}</strong></li>
        </ul>
        <div style="border-top: 1px solid #e0d8cf; padding-top: 1rem; margin-top: 1rem;">
          <span style="color: #6b7c70;">商品备注</span>
          <p style="color: #2c3e50; margin-top: 0.3rem;">
            {{ detail.basicInfo.productDesc || '暂无备注说明' }}
          </p>
        </div>
      </div>

      <div>
        <h4>成员列表</h4>
        <table
          v-if="detail.memberList.length"
          class="table"
        >
          <thead>
            <tr>
              <th>成员</th>
              <th>角色</th>
              <th>支付状态</th>
              <th>收货状态</th>
              <th>应付金额</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(row, index) in detail.memberList"
              :key="`${row.nickname}-${index}`"
            >
              <td>{{ row.nickname }}</td>
              <td>{{ formatRole(row.role) }}</td>
              <td>
                <span
                  class="tag"
                  :class="`tag-${row.payStatus === 'PAID' ? 'success' : 'warning'}`"
                >
                  {{ formatPayStatus(row.payStatus) }}
                </span>
              </td>
              <td>
                <span class="tag tag-info">
                  {{ formatReceiveStatus(row.receiveStatus) }}
                </span>
              </td>
              <td>{{ row.amount || '--' }}</td>
            </tr>
          </tbody>
        </table>
        <p
          v-else
          class="muted-text"
        >
          暂无成员记录。
        </p>
      </div>

      <div style="margin-top: 1.5rem; padding: 1rem; background: #f8f6f2; border-radius: 10px;">
        <button
          class="btn btn-secondary"
          @click="router.push('/admin/orders')"
        >
          返回列表
        </button>
      </div>
    </div>

    <div
      v-else
      class="section"
    >
      <p class="muted-text">
        当前未能加载到订单详情，请返回列表重新选择。
      </p>
      <div style="margin-top: 1rem;">
        <button
          class="btn btn-secondary"
          @click="router.push('/admin/orders')"
        >
          返回订单管理
        </button>
      </div>
    </div>
  </div>
</template>
