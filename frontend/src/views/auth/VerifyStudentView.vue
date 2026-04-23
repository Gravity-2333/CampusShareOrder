<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import { useUserStore } from '../../stores/user'
import { validateStudentNo } from '../../utils/validate'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  studentNo: '',
})

const stats = computed(() => [
  {
    label: '当前身份',
    value: userStore.displayName,
    hint: '实名认证完成后可发起拼单',
  },
  {
    label: '认证状态',
    value: userStore.session.isVerified ? '已认证' : '未认证',
    hint: '仅用户端需要实名认证',
  },
  {
    label: '当前学号',
    value: userStore.profile?.studentNo || '--',
    hint: 'VerifyStudentRequest 仅提交 studentNo',
  },
])

const profileRows = computed(() => [
  { label: '用户 ID', value: userStore.profile?.userId || '--' },
  { label: '昵称', value: userStore.profile?.nickname || '--' },
  { label: '手机号', value: userStore.profile?.phone || '--' },
  { label: '学号', value: userStore.profile?.studentNo || '--' },
])

const loadProfile = async () => {
  try {
    const profile = await userStore.ensureProfileLoaded()
    form.studentNo = profile?.studentNo || ''
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handleSubmit = async () => {
  const errorMessage = validateStudentNo(form.studentNo)

  if (errorMessage) {
    ElMessage.warning(errorMessage)
    return
  }

  try {
    await userStore.submitStudentVerification({
      studentNo: form.studentNo.trim(),
    })
    ElMessage.success('实名认证成功')
    router.push('/profile')
  } catch (error) {
    ElMessage.error(error.message)
  }
}

onMounted(loadProfile)
</script>

<template>
  <div class="stack-page">
    <div class="stats-grid">
      <StatCard v-for="item in stats" :key="item.label" :label="item.label" :value="item.value" :hint="item.hint" />
    </div>

    <PageSection title="实名认证" description="该页面对应契约中的 POST /api/users/verify-student。">
      <el-alert
        title="认证后，路由守卫会允许你进入发起拼单页面。"
        type="info"
        :closable="false"
      />

      <div class="form-intro surface-card">
        <strong>认证说明</strong>
        <p>当前阶段仅提交学号字段，认证完成后会同步刷新登录态和个人资料中的认证状态。</p>
      </div>

      <el-form label-position="top" :model="form" class="form-grid">
        <el-form-item label="学号">
          <el-input v-model="form.studentNo" placeholder="请输入学号" />
        </el-form-item>
      </el-form>

      <div class="page-actions">
        <el-button @click="router.push('/profile')">返回资料页</el-button>
        <el-button type="primary" :loading="userStore.verifyingStudent" @click="handleSubmit">提交认证</el-button>
      </div>
    </PageSection>

    <PageSection title="当前资料" description="方便核对认证前后 UserProfileVO 的变化。">
      <ul class="detail-list">
        <li v-for="row in profileRows" :key="row.label">
          <span>{{ row.label }}</span>
          <strong>{{ row.value }}</strong>
        </li>
      </ul>
    </PageSection>
  </div>
</template>
