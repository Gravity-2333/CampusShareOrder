<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import { useComplaintStore } from '../../stores/complaint'
import { formatComplaintType } from '../../utils/format'

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
  if (!form.orderId) {
    ElMessage.warning('请填写订单 ID')
    return
  }

  if (!form.content.trim()) {
    ElMessage.warning('请填写投诉内容')
    return
  }

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
  <div class="stack-page">
    <div class="stats-grid">
      <StatCard label="投诉订单" :value="form.orderId || '--'" hint="CreateComplaintRequest 要求提交 orderId" />
      <StatCard
        label="投诉类型"
        :value="formatComplaintType(form.type)"
        hint="契约枚举只允许 NOT_PURCHASED / FAKE_RECEIPT"
      />
    </div>

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

    <PageSection title="提交预览" description="方便在前端阶段核对投诉请求字段与文档一致。">
      <ul class="detail-list">
        <li><span>orderId</span><strong>{{ form.orderId || '--' }}</strong></li>
        <li><span>type</span><strong>{{ form.type }}</strong></li>
        <li><span>content</span><strong>{{ form.content || '--' }}</strong></li>
      </ul>
    </PageSection>
  </div>
</template>
