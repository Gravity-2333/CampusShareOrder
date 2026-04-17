<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import PageSection from '../../components/common/PageSection.vue'
import { useOrderStore } from '../../stores/order'

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

const handleSubmit = async () => {
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
  <PageSection title="发起拼单" description="CreateOrderRequest 字段严格对齐冻结契约。">
    <el-form label-position="top" :model="form" class="form-grid">
      <el-form-item label="商品名称">
        <el-input v-model="form.productName" />
      </el-form-item>
      <el-form-item label="商品描述">
        <el-input v-model="form.productDesc" type="textarea" />
      </el-form-item>
      <el-form-item label="拼单人数">
        <el-input-number v-model="form.totalMemberCount" :min="2" :max="6" />
      </el-form-item>
      <el-form-item label="预计金额">
        <el-input-number v-model="form.estimatedTotalAmount" :min="1" :precision="2" />
      </el-form-item>
      <el-form-item label="取货点">
        <el-input v-model="form.pickupPoint" />
      </el-form-item>
      <el-form-item label="截止时间">
        <el-input v-model="form.deadlineAt" />
      </el-form-item>
    </el-form>

    <div class="page-actions">
      <el-button type="primary" :loading="loading" @click="handleSubmit">创建拼单</el-button>
    </div>
  </PageSection>
</template>
