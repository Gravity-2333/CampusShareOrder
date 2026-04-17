<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  password: 'admin123',
  username: 'admin',
})
const loading = ref(false)

const handleSubmit = async () => {
  loading.value = true

  try {
    await userStore.loginAdmin(form)
    ElMessage.success('管理员登录成功')
    router.push('/admin/dashboard')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page compact-page">
    <el-card class="auth-card" shadow="hover">
      <template #header>
        <div class="card-header-row">
          <div>
            <p class="section-kicker">Admin Auth</p>
            <h2>管理员登录</h2>
          </div>
          <el-button link @click="router.push('/login')">用户入口</el-button>
        </div>
      </template>

      <el-form label-position="top" :model="form" @submit.prevent="handleSubmit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" show-password />
        </el-form-item>
        <div class="page-actions">
          <el-button type="primary" :loading="loading" @click="handleSubmit">登录管理端</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>
