<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import { useComplaintStore } from '../../stores/complaint'
import { formatComplaintType } from '../../utils/format'
import { firstValidationError, validatePositiveNumber, requireValue } from '../../utils/validate'

const route = useRoute()
const router = useRouter()
const complaintStore = useComplaintStore()
const loading = ref(false)
const form = reactive({
  content: '',
  orderId: Number(route.query.orderId || 0),
  type: 'NOT_PURCHASED',
})

const previewRows = computed(() => [
  { label: '请求对象', value: 'CreateComplaintRequest' },
  { label: '接口路径', value: 'POST /api/complaints' },
  { label: 'orderId', value: form.orderId || '--' },
  { label: 'type', value: form.type },
  { label: 'content', value: form.content || '--' },
])

const handleSubmit = async () => {
  const errorMessage = firstValidationError([
    validatePositiveNumber(form.orderId, '请填写正确的订单 ID'),
    requireValue(form.content, '请填写投诉内容'),
  ])

  if (errorMessage) {
    ElMessage.warning(errorMessage)
    return
  }

  loading.value = true

  try {
    const result = await complaintStore.submitComplaint(form)
    ElMessage.success('投诉已提交')
    router.push(`/complaints/${result.complaintId}`)
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
        label="投诉订单"
        :value="form.orderId || '--'"
        hint="CreateComplaintRequest 要求提交 orderId"
      />
      <StatCard
        label="投诉类型"
        :value="formatComplaintType(form.type)"
        hint="契约枚举只允许 NOT_PURCHASED / FAKE_RECEIPT"
      />
    </div>

    <PageSection
      title="发起投诉"
      description="对应 POST /api/complaints。"
    >
      <el-alert
        title="投诉提交后将进入待处理状态，后续可在我的投诉中查看处理结果。"
        type="warning"
        :closable="false"
      />

      <div class="form-intro surface-card">
        <strong>投诉说明</strong>
        <p>当前阶段投诉类型只允许“未购买”或“伪造凭证”，是否能发起投诉仍以订单详情里的 actionFlags 为准。</p>
      </div>

      <el-form
        label-position="top"
        :model="form"
        class="form-grid"
      >
        <el-form-item label="订单 ID">
          <el-input-number
            v-model="form.orderId"
            :min="1"
          />
        </el-form-item>
        <el-form-item label="投诉类型">
          <el-select v-model="form.type">
            <el-option
              label="未购买"
              value="NOT_PURCHASED"
            />
            <el-option
              label="伪造凭证"
              value="FAKE_RECEIPT"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          class="full-span"
          label="投诉内容"
        >
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="5"
            placeholder="请简要描述订单异常、发生时间和你希望平台介入处理的原因"
          />
        </el-form-item>
      </el-form>

      <div class="page-actions">
        <el-button @click="router.push('/complaints')">
          返回我的投诉
        </el-button>
        <el-button
          type="danger"
          :loading="loading"
          @click="handleSubmit"
        >
          提交投诉
        </el-button>
      </div>
    </PageSection>

    <PageSection
      title="提交预览"
      description="方便在前端阶段核对投诉请求字段与契约一致。"
    >
      <ul class="detail-list">
        <li
          v-for="row in previewRows"
          :key="row.label"
        >
          <span>{{ row.label }}</span>
          <strong>{{ row.value }}</strong>
        </li>
      </ul>
    </PageSection>
  </div>
</template>
