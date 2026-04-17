<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { useUserStore } from '../../stores/user'
import {
  firstValidationError,
  validateNickname,
  validatePassword,
  validatePhone,
} from '../../utils/validate'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  nickname: '',
  password: '123456',
  phone: '',
})
const loading = ref(false)

const handleSubmit = async () => {
  const errorMessage = firstValidationError([
    validateNickname(form.nickname),
    validatePhone(form.phone),
    validatePassword(form.password),
  ])

  if (errorMessage) {
    ElMessage.warning(errorMessage)
    return
  }

  loading.value = true

  try {
    await userStore.registerUser(form)
    ElMessage.success('注册成功，请返回登录')
    router.push('/login')
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
            <p class="section-kicker">创建账号</p>
            <h2>创建用户账号</h2>
          </div>
          <el-button link @click="router.push('/login')">返回登录</el-button>
        </div>
      </template>

      <el-alert
        v-if="userStore.session.role === ''"
        class="auth-alert"
        title="注册后将进入用户端登录流程，管理员账号与普通用户账号分离。"
        type="info"
        :closable="false"
      />

      <el-form label-position="top" :model="form" @submit.prevent="handleSubmit">
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" show-password placeholder="请输入密码" />
        </el-form-item>
        <div class="page-actions">
          <el-button type="primary" :loading="loading" @click="handleSubmit">立即注册</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>
