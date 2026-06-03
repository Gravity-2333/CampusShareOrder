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
    hint: '请填写本人真实学号',
  },
])

const profileRows = computed(() => [
  { label: '用户 ID', value: userStore.profile?.userId || '--' },
  { label: '昵称', value: userStore.profile?.nickname || '--' },
  { label: '手机号', value: userStore.profile?.phone || '--' },
  { label: '学号', value: userStore.profile?.studentNo || '--' },
])
const isAlreadyVerified = computed(() => Boolean(userStore.session.isVerified || userStore.profile?.isVerified))

const loadProfile = async () => {
  try {
    const profile = await userStore.ensureProfileLoaded()
    form.studentNo = profile?.studentNo || ''
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handleSubmit = async () => {
  if (userStore.verifyingStudent) {
    return
  }

  if (isAlreadyVerified.value) {
    ElMessage.info('当前账号已完成实名认证')
    router.push('/profile')
    return
  }

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
      <StatCard
        v-for="item in stats"
        :key="item.label"
        :label="item.label"
        :value="item.value"
        :hint="item.hint"
      />
    </div>

    <PageSection
      title="实名认证"
      description="完成学号认证后，即可发起拼单并参与完整订单流程。"
    >
      <el-alert
        v-if="isAlreadyVerified"
        title="当前账号已完成实名认证，无需重复提交。"
        type="success"
        :closable="false"
      />

      <el-form
        label-position="top"
        :model="form"
        class="form-grid"
      >
        <el-form-item label="学号">
          <el-input
            v-model="form.studentNo"
            maxlength="10"
            show-word-limit
            placeholder="请输入学号"
          />
        </el-form-item>
      </el-form>

      <div class="page-actions">
        <el-button @click="router.push('/profile')">
          返回资料页
        </el-button>
        <el-button
          type="primary"
          :loading="userStore.verifyingStudent"
          :disabled="isAlreadyVerified"
          @click="handleSubmit"
        >
          {{ isAlreadyVerified ? '已完成认证' : '提交认证' }}
        </el-button>
      </div>
    </PageSection>

    <PageSection
      title="当前资料"
      description="认证完成后，这里的学号和认证状态会同步更新。"
    >
      <ul class="detail-list">
        <li
          v-for="row in profileRows"
          :key="row.label"
        >
          <span>{{ row.label }}</span>
          <strong>{{ row.value }}</strong>
        </li>
      </ul>
    </PageSection>
  </div>
</template>
