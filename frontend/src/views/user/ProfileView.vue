<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import PageSection from '../../components/common/PageSection.vue'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const loading = ref(false)
const form = reactive({
  contactInfo: '',
  nickname: '',
})

const loadProfile = async () => {
  loading.value = true

  try {
    const profile = await userStore.loadProfile()
    form.contactInfo = profile.contactInfo
    form.nickname = profile.nickname
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  loading.value = true

  try {
    await userStore.saveProfile(form)
    ElMessage.success('资料已更新')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

onMounted(loadProfile)
</script>

<template>
  <div class="stack-page">
    <PageSection
      title="个人资料概览"
      description="优先展示 UserProfileVO 里的固定字段，方便后续联调直接对照。"
    >
      <div class="detail-grid">
        <div class="surface-card detail-panel">
          <h3>基础身份</h3>
          <ul class="detail-list">
            <li><span>用户 ID</span><strong>{{ userStore.profile?.userId || '--' }}</strong></li>
            <li><span>手机号</span><strong>{{ userStore.profile?.phone || '--' }}</strong></li>
            <li><span>认证状态</span><strong>{{ userStore.profile?.isVerified ? '已认证' : '未认证' }}</strong></li>
            <li><span>账号状态</span><strong>{{ userStore.profile?.status || '--' }}</strong></li>
          </ul>
        </div>

        <div class="surface-card detail-panel">
          <h3>业务资料</h3>
          <ul class="detail-list">
            <li><span>昵称</span><strong>{{ userStore.profile?.nickname || '--' }}</strong></li>
            <li><span>学号</span><strong>{{ userStore.profile?.studentNo || '--' }}</strong></li>
            <li><span>信用分</span><strong>{{ userStore.profile?.creditScore ?? '--' }}</strong></li>
            <li><span>注册时间</span><strong>{{ userStore.profile?.createdAt || '--' }}</strong></li>
          </ul>
        </div>
      </div>
    </PageSection>

    <PageSection title="修改资料" description="对应 GET/PUT /api/users/profile。">
      <el-form label-position="top" :model="form" class="form-grid">
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="联系信息">
          <el-input v-model="form.contactInfo" />
        </el-form-item>
      </el-form>
      <div class="page-actions">
        <el-button type="primary" :loading="loading" @click="handleSubmit">保存修改</el-button>
      </div>
    </PageSection>
  </div>
</template>
