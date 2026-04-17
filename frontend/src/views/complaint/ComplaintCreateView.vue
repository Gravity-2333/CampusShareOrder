<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import PageSection from '../../components/common/PageSection.vue'
import { useComplaintStore } from '../../stores/complaint'

const route = useRoute()
const router = useRouter()
const complaintStore = useComplaintStore()
const loading = ref(false)
const form = reactive({
  content: '',
  orderId: Number(route.query.orderId || 1),
  type: 'NOT_PURCHASED',
})

const handleSubmit = async () => {
  loading.value = true

  try {
    await complaintStore.submitComplaint(form)
    ElMessage.success('投诉已提交')
    router.push('/complaints')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <PageSection title="发起投诉" description="对应 POST /api/complaints。">
    <el-form label-position="top" :model="form" class="form-grid">
      <el-form-item label="订单 ID">
        <el-input-number v-model="form.orderId" :min="1" />
      </el-form-item>
      <el-form-item label="投诉类型">
        <el-select v-model="form.type">
          <el-option label="未购买" value="NOT_PURCHASED" />
          <el-option label="伪造凭证" value="FAKE_RECEIPT" />
        </el-select>
      </el-form-item>
      <el-form-item class="full-span" label="投诉内容">
        <el-input v-model="form.content" type="textarea" :rows="4" />
      </el-form-item>
    </el-form>
    <div class="page-actions">
      <el-button type="danger" :loading="loading" @click="handleSubmit">提交投诉</el-button>
    </div>
  </PageSection>
</template>
