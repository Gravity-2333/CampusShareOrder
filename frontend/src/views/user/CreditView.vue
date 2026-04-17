<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { getUserCredit } from '../../api/user'
import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'

const loading = ref(false)
const credit = ref({
  creditScore: 0,
  records: [],
})

const loadCredit = async () => {
  loading.value = true

  try {
    credit.value = await getUserCredit()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

onMounted(loadCredit)
</script>

<template>
  <div class="stack-page">
    <div class="stats-grid">
      <StatCard label="当前信用分" :value="credit.creditScore" hint="用于后续投诉处理与风控场景" />
    </div>

    <PageSection title="信用分记录" description="对应 GET /api/users/credit。">
      <el-table v-loading="loading" :data="credit.records" stripe>
        <el-table-column prop="createdAt" label="时间" />
        <el-table-column prop="changeReason" label="变更原因" />
        <el-table-column prop="delta" label="变更值" />
      </el-table>
    </PageSection>
  </div>
</template>
