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
  <PageSection title="个人资料" description="对应 GET/PUT /api/users/profile。">
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
</template>
