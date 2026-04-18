<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import { useOrderStore } from '../../stores/order'
import { formatCurrency } from '../../utils/format'
import {
  firstValidationError,
  requireValue,
  validatePositiveNumber,
} from '../../utils/validate'

const router = useRouter()
const orderStore = useOrderStore()
const loading = ref(false)
const form = reactive({
  deadlineAt: '2026-04-17 21:00:00',
  estimatedTotalAmount: 48,
  pickupPoint: '东区宿舍门口',
  productDesc: '',
  productName: '新建拼单',
  totalMemberCount: 3,
})

const previewRows = [
  { label: '请求对象', value: 'CreateOrderRequest' },
  { label: '接口路径', value: 'POST /api/orders' },
]

const handleSubmit = async () => {
  const errorMessage = firstValidationError([
    requireValue(form.productName, '请填写商品名称'),
    validatePositiveNumber(form.totalMemberCount, '拼单人数必须大于 0'),
    validatePositiveNumber(form.estimatedTotalAmount, '预计金额必须大于 0'),
    requireValue(form.pickupPoint, '请填写取货点'),
    requireValue(form.deadlineAt, '请填写截止时间'),
  ])

  if (errorMessage) {
    ElMessage.warning(errorMessage)
    return
  }

  loading.value = true

  try {
    const result = await orderStore.createNewOrder(form)
    ElMessage.success('创建成功')
    router.push(`/orders/${result.orderId}`)
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="stack-page">
    <div class="stats-grid">
      <StatCard
        label="预计人均"
        :value="formatCurrency(form.estimatedTotalAmount / form.totalMemberCount)"
        hint="仅作为前端预览，不替代后端结算逻辑"
      />
      <StatCard label="目标人数" :value="form.totalMemberCount" hint="创建后发起人也算成员之一" />
    </div>

    <PageSection title="发起拼单" description="CreateOrderRequest 字段严格对齐冻结契约。">
      <div class="form-intro surface-card">
        <strong>创建前提示</strong>
        <p>发起人创建成功后会自动成为首位成员，后续招募、支付、上传凭证和确认送达都从详情页继续推进。</p>
      </div>

      <el-form label-position="top" :model="form" class="form-grid">
        <el-form-item label="商品名称">
          <el-input v-model="form.productName" placeholder="例如：晚饭拼单、奶茶拼单" />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="form.productDesc" type="textarea" :rows="4" placeholder="可补充口味、备注、配送说明等" />
        </el-form-item>
        <el-form-item label="拼单人数">
          <el-input-number v-model="form.totalMemberCount" :min="2" :max="6" />
        </el-form-item>
        <el-form-item label="预计金额">
          <el-input-number v-model="form.estimatedTotalAmount" :min="1" :precision="2" />
        </el-form-item>
        <el-form-item label="取货点">
          <el-input v-model="form.pickupPoint" placeholder="例如：东区宿舍门口、图书馆南门" />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-input v-model="form.deadlineAt" placeholder="yyyy-MM-dd HH:mm:ss" />
        </el-form-item>
      </el-form>

      <div class="page-actions">
        <el-button type="primary" :loading="loading" @click="handleSubmit">创建拼单</el-button>
      </div>
    </PageSection>

    <PageSection title="提交预览" description="这里用于在前端阶段先核对请求字段是否符合冻结契约。">
      <ul class="detail-list">
        <li v-for="row in previewRows" :key="row.label">
          <span>{{ row.label }}</span>
          <strong>{{ row.value }}</strong>
        </li>
        <li><span>productName</span><strong>{{ form.productName || '--' }}</strong></li>
        <li><span>productDesc</span><strong>{{ form.productDesc || '--' }}</strong></li>
        <li><span>totalMemberCount</span><strong>{{ form.totalMemberCount }}</strong></li>
        <li><span>estimatedTotalAmount</span><strong>{{ formatCurrency(form.estimatedTotalAmount) }}</strong></li>
        <li><span>pickupPoint</span><strong>{{ form.pickupPoint || '--' }}</strong></li>
        <li><span>deadlineAt</span><strong>{{ form.deadlineAt || '--' }}</strong></li>
      </ul>
    </PageSection>
  </div>
</template>
