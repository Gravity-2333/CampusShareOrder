<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import PageSection from '../../components/common/PageSection.vue'
import StatCard from '../../components/common/StatCard.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import { useUserStore } from '../../stores/user'
import { formatDateTime, formatUserStatus } from '../../utils/format'
import { firstValidationError, requireValue, validateNickname } from '../../utils/validate'

const router = useRouter()
const userStore = useUserStore()
const form = reactive({
  contactInfo: '',
  nickname: '',
})

const stats = computed(() => [
  {
    label: '资料完整度',
    value: `${userStore.profileCompletion}%`,
    hint: '根据昵称、联系方式、学号三个核心字段估算',
  },
  {
    label: '认证状态',
    value: userStore.profile?.isVerified ? '已认证' : '未认证',
    hint: '未认证用户会被限制发起拼单',
  },
  {
    label: '信用等级',
    value: userStore.creditLevel,
    hint: '基于当前信用分做展示，不参与前端业务判断',
  },
])

const profileRows = computed(() => [
  { label: '用户 ID', value: userStore.profile?.userId || '--' },
  { label: '手机号', value: userStore.profile?.phone || '--' },
  { label: '账号状态', value: formatUserStatus(userStore.profile?.status) },
  { label: '注册时间', value: formatDateTime(userStore.profile?.createdAt) },
])

const businessRows = computed(() => [
  { label: '昵称', value: userStore.profile?.nickname || '--' },
  { label: '联系方式', value: userStore.profile?.contactInfo || '--' },
  { label: '学号', value: userStore.profile?.studentNo || '--' },
  { label: '信用分', value: userStore.profile?.creditScore ?? '--' },
])

const loadProfile = async () => {
  try {
    const [profile] = await Promise.all([userStore.loadProfile(), userStore.loadCredit()])
    form.contactInfo = profile.contactInfo || ''
    form.nickname = profile.nickname || ''
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handleSubmit = async () => {
  const errorMessage = firstValidationError([
    validateNickname(form.nickname),
    requireValue(form.contactInfo, '请填写联系方式'),
  ])

  if (errorMessage) {
    ElMessage.warning(errorMessage)
    return
  }

  try {
    await userStore.saveProfile({
      contactInfo: form.contactInfo.trim(),
      nickname: form.nickname.trim(),
    })
    ElMessage.success('资料已更新')
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
      v-loading="userStore.profileLoading"
      title="个人资料概览"
      description="优先展示 UserProfileVO 里的固定字段，方便后续联调直接对照。"
    >
      <div class="detail-grid">
        <div class="surface-card detail-panel">
          <div class="card-header-row">
            <h3>基础身份</h3>
            <StatusTag
              :value="userStore.profile?.status"
              :text="formatUserStatus(userStore.profile?.status)"
            />
          </div>
          <ul class="detail-list">
            <li
              v-for="row in profileRows"
              :key="row.label"
            >
              <span>{{ row.label }}</span>
              <strong>{{ row.value }}</strong>
            </li>
          </ul>
        </div>

        <div class="surface-card detail-panel">
          <div class="card-header-row">
            <h3>业务资料</h3>
            <el-button
              v-if="!userStore.profile?.isVerified"
              type="primary"
              plain
              @click="router.push('/verify-student')"
            >
              去认证
            </el-button>
          </div>
          <ul class="detail-list">
            <li
              v-for="row in businessRows"
              :key="row.label"
            >
              <span>{{ row.label }}</span>
              <strong>{{ row.value }}</strong>
            </li>
          </ul>
        </div>
      </div>
    </PageSection>

    <PageSection
      title="修改资料"
      description="对应 GET / PUT /api/users/profile。"
    >
      <div class="form-intro surface-card">
        <strong>修改后会同步更新当前登录上下文和资料页展示。</strong>
        <p>这里仅维护昵称和联系方式，不改动契约外字段，页面始终通过统一资料接口提交。</p>
      </div>

      <el-form
        label-position="top"
        :model="form"
        class="form-grid"
      >
        <el-form-item label="昵称">
          <el-input
            v-model="form.nickname"
            maxlength="20"
            show-word-limit
            placeholder="例如：宿舍拼单小队长"
          />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input
            v-model="form.contactInfo"
            placeholder="例如：微信 zhangsan_01"
          />
        </el-form-item>
      </el-form>

      <div class="page-actions">
        <el-button
          v-if="!userStore.profile?.isVerified"
          plain
          @click="router.push('/verify-student')"
        >
          去完成认证
        </el-button>
        <el-button @click="router.push('/credit')">
          查看信用分
        </el-button>
        <el-button
          type="primary"
          :loading="userStore.savingProfile"
          @click="handleSubmit"
        >
          保存修改
        </el-button>
      </div>
    </PageSection>
  </div>
</template>
