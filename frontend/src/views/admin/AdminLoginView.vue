<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { useAppStore } from '../../stores/app'
import { useUserStore } from '../../stores/user'
import { firstValidationError, validatePassword, requireValue } from '../../utils/validate'

const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const form = reactive({
  password: 'admin123',
  username: 'admin',
})
const loading = ref(false)

const fillAdminAccount = () => {
  form.username = 'admin'
  form.password = 'admin123'
}

const handleSubmit = async () => {
  const errorMessage = firstValidationError([
    requireValue(form.username, '请输入管理员账号'),
    validatePassword(form.password, '管理员密码'),
  ])

  if (errorMessage) {
    ElMessage.warning(errorMessage)
    return
  }

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

      <el-alert
        class="auth-alert"
        :title="
          appStore.apiMode === 'mock'
            ? 'Mock 模式下可直接使用 admin / admin123 登录。'
            : '当前为 live 模式，请使用真实管理员账号。'
        "
        type="info"
        :closable="false"
      />

      <el-form label-position="top" :model="form" @submit.prevent="handleSubmit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" show-password />
        </el-form-item>
        <div class="page-actions">
          <el-button type="primary" :loading="loading" @click="handleSubmit">登录管理端</el-button>
          <el-button v-if="appStore.apiMode === 'mock'" @click="fillAdminAccount">填入演示账号</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>
