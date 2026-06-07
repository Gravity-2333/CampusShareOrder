<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { useComplaintStore } from '../../stores/complaint'
import { useOrderStore } from '../../stores/order'
import { normalizeId, sameId } from '../../utils/id'
import { firstValidationError, requireValue } from '../../utils/validate'

const route = useRoute()
const router = useRouter()
const complaintStore = useComplaintStore()
const orderStore = useOrderStore()
const loading = ref(false)
const loadingOrders = ref(false)
const loadingOrderDetail = ref(false)
const complaintableOrders = ref([])
const selectedOrderDetail = ref(null)

const form = reactive({
  accusedUserId: null,
  content: '',
  orderId: null,
  type: '',
})

// 投诉类型选项
const complaintTypes = [
  { label: '未购买', value: 'NOT_PURCHASED' },
  { label: '伪造凭证', value: 'FAKE_RECEIPT' },
  { label: '未发货', value: 'NO_SHIP' },
  { label: '未送达', value: 'NO_DELIVERY' },
  { label: '商品质量', value: 'QUALITY' },
  { label: '其他', value: 'OTHER' },
]

// 是否有可投诉订单
const hasComplaintableOrders = computed(() => complaintableOrders.value.length > 0)
const accusedOptions = computed(() => {
  const detail = selectedOrderDetail.value
  if (!detail) return []

  const options = []
  const currentUserId = normalizeId(detail.currentUserMember?.userId)
  const pushUnique = (member) => {
    const memberUserId = normalizeId(member?.userId)
    if (
      !memberUserId ||
      sameId(memberUserId, currentUserId) ||
      options.some((option) => sameId(option.userId, memberUserId))
    ) return
    options.push({
      userId: memberUserId,
      nickname: member.nickname || `用户 #${memberUserId}`,
      roleText: member.isCreator ? '发起人' : '参与成员',
    })
  }

  pushUnique({
    userId: detail.initiatorInfo?.userId,
    nickname: detail.initiatorInfo?.nickname,
    isCreator: true,
  })
  const activeMembers = (detail.memberList || []).filter((member) => member.joinStatus === 'ACTIVE')
  activeMembers.forEach(pushUnique)

  return options
})
const canSubmitComplaint = computed(
  () =>
    hasComplaintableOrders.value &&
    selectedOrderDetail.value?.actionFlags?.canCreateComplaint === true &&
    accusedOptions.value.length > 0,
)
const selectedOrderTip = computed(() => {
  if (!form.orderId) return '请选择订单后，系统会校验当前订单是否已开放投诉。'
  if (loadingOrderDetail.value) return '正在校验订单投诉权限...'
  if (!selectedOrderDetail.value) return '暂未加载到订单详情，请重新选择订单。'
  if (canSubmitComplaint.value) return '该订单当前已开放投诉，请选择被投诉人并填写描述。'
  return '该订单当前不满足投诉条件，可能是投诉通道尚未开放或你已提交过投诉。'
})

// 加载可投诉的订单列表
const loadComplaintableOrders = async () => {
  loadingOrders.value = true
  try {
    // 获取我的订单列表
    await orderStore.loadMyOrders({ page: 1, pageSize: 100 })

    // 筛选可投诉的订单（状态为已完成或特定状态的订单）
    complaintableOrders.value = orderStore.myOrdersPage.list
      .filter(order => {
        // 可以投诉的订单状态：已成团、待送达、待收货、已完成
        const complaintableStatuses = ['GROUPED', 'WAIT_DELIVERY', 'WAIT_RECEIVE', 'COMPLETED']
        return complaintableStatuses.includes(order.status)
      })
      .map(order => ({
        orderId: order.orderId,
        orderNo: order.orderNo,
        productName: order.productName,
        label: `${order.orderNo} - ${order.productName}`,
      }))

    const routeOrderId = normalizeId(route.query.orderId)
    if (routeOrderId) {
      form.orderId = routeOrderId
    } else if (!form.orderId && complaintableOrders.value.length === 1) {
      form.orderId = complaintableOrders.value[0].orderId
    }
  } catch (error) {
    ElMessage.error('加载订单列表失败：' + error.message)
  } finally {
    loadingOrders.value = false
  }
}

const loadSelectedOrderDetail = async (orderId) => {
  selectedOrderDetail.value = null
  form.accusedUserId = null
  if (!orderId) return

  loadingOrderDetail.value = true
  try {
    selectedOrderDetail.value = await orderStore.loadOrderDetail(orderId)
    const defaultAccused = accusedOptions.value.find((member) => member.roleText === '发起人') || accusedOptions.value[0]
    form.accusedUserId = defaultAccused?.userId || null
  } catch (error) {
    ElMessage.error('加载订单详情失败：' + error.message)
  } finally {
    loadingOrderDetail.value = false
  }
}

const handleSubmit = async () => {
  if (loading.value || complaintStore.submitting) {
    return
  }

  const errorMessage = firstValidationError([
    requireValue(form.orderId, '请选择要投诉的订单'),
    !canSubmitComplaint.value ? '当前订单暂不支持发起投诉' : '',
    requireValue(form.accusedUserId, '请选择被投诉人'),
    requireValue(form.type, '请选择投诉类型'),
    requireValue(form.content, '请填写投诉描述'),
    form.content.trim().length > 500 ? '投诉描述长度不能超过 500 个字符' : '',
  ])

  if (errorMessage) {
    ElMessage.warning(errorMessage)
    return
  }

  loading.value = true

  try {
    const result = await complaintStore.submitComplaint({
      accusedUserId: form.accusedUserId,
      orderId: form.orderId,
      type: form.type,
      content: form.content.trim(),
    })
    ElMessage.success('投诉已提交')

    if (result?.complaintId) {
      router.push(`/complaints/${result.complaintId}`)
      return
    }

    router.push('/complaints')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  router.push('/complaints')
}

onMounted(() => {
  loadComplaintableOrders()
})

watch(
  () => form.orderId,
  (orderId) => {
    loadSelectedOrderDetail(orderId)
  },
)
</script>

<template>
  <div class="stack-page">
    <div class="section">
      <div class="section-header">
        <h3>发起投诉</h3>
      </div>

      <!-- 无可投诉订单提示 -->
      <el-alert
        v-if="!loadingOrders && !hasComplaintableOrders"
        title="目前没有可以投诉的订单"
        description="只有已成团、待送达、待收货或已完成的订单才能发起投诉。"
        type="info"
        :closable="false"
        show-icon
      />

      <el-form
        v-else
        label-position="top"
        :model="form"
        class="form-grid"
      >
        <el-form-item label="选择订单">
          <el-select
            v-model="form.orderId"
            placeholder="请选择要投诉的订单"
            :loading="loadingOrders"
            :disabled="!hasComplaintableOrders"
            style="width: 100%"
          >
            <el-option
              v-for="order in complaintableOrders"
              :key="order.orderId"
              :label="order.label"
              :value="order.orderId"
            />
          </el-select>
        </el-form-item>

        <el-alert
          class="full-span"
          :title="selectedOrderTip"
          :type="canSubmitComplaint ? 'success' : 'warning'"
          :closable="false"
          show-icon
        />

        <el-form-item label="被投诉人">
          <el-select
            v-model="form.accusedUserId"
            placeholder="请选择被投诉人"
            :loading="loadingOrderDetail"
            :disabled="!canSubmitComplaint"
            style="width: 100%"
          >
            <el-option
              v-for="member in accusedOptions"
              :key="member.userId"
              :label="`${member.nickname}（${member.roleText}）`"
              :value="member.userId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="投诉类型">
          <el-select
            v-model="form.type"
            placeholder="请选择投诉类型"
            :disabled="!canSubmitComplaint"
            style="width: 100%"
          >
            <el-option
              v-for="type in complaintTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item
          label="投诉描述"
          class="full-span"
        >
          <el-input
            v-model="form.content"
            type="textarea"
            maxlength="500"
            show-word-limit
            :rows="6"
            placeholder="请详细描述投诉内容..."
            :disabled="!canSubmitComplaint"
          />
        </el-form-item>
      </el-form>

      <div class="page-actions">
        <el-button @click="handleCancel">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="loading"
          :disabled="!canSubmitComplaint"
          @click="handleSubmit"
        >
          提交投诉
        </el-button>
      </div>
    </div>
  </div>
</template>
