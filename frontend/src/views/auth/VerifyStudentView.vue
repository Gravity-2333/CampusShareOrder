<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const form = reactive({
  studentNo: '',
})
const loading = ref(false)

onMounted(async () => {
  if (!userStore.profile) {
    await userStore.loadProfile()
  }

  form.studentNo = userStore.profile?.studentNo || ''
})

const handleSubmit = async () => {
  loading.value = true

  try {
    await userStore.submitStudentVerification(form)
    ElMessage.success('实名认证成功')
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
      <StatCard label="当前身份" :value="userStore.displayName" hint="实名认证后可发起拼单" />
      <StatCard
        label="认证状态"
        :value="userStore.session.isVerified ? '已认证' : '未认证'"
        hint="仅用户端需要实名认证"
      />
      <StatCard
        label="当前学号"
        :value="userStore.profile?.studentNo || '--'"
        hint="VerifyStudentRequest 仅提交 studentNo"
      />
    </div>

    <PageSection title="实名认证" description="该页面对应契约中的 POST /api/users/verify-student">
      <el-form label-position="top" :model="form" class="form-grid">
        <el-form-item label="学号">
          <el-input v-model="form.studentNo" placeholder="请输入学号" />
        </el-form-item>
      </el-form>
      <div class="page-actions">
        <el-button type="primary" :loading="loading" @click="handleSubmit">提交认证</el-button>
      </div>
    </PageSection>

    <PageSection title="当前资料" description="方便核对认证前后 UserProfileVO 的变化。">
      <ul class="detail-list">
        <li><span>用户 ID</span><strong>{{ userStore.profile?.userId || '--' }}</strong></li>
        <li><span>昵称</span><strong>{{ userStore.profile?.nickname || '--' }}</strong></li>
        <li><span>手机号</span><strong>{{ userStore.profile?.phone || '--' }}</strong></li>
        <li><span>学号</span><strong>{{ userStore.profile?.studentNo || '--' }}</strong></li>
      </ul>
    </PageSection>
  </div>
</template>
