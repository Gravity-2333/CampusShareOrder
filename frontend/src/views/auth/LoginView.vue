<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { useAppStore } from '../../stores/app'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const form = reactive({
  password: '123456',
  phone: '13800000001',
})
const loading = ref(false)
const demoAccounts = [
  { label: '张三', phone: '13800000001' },
  { label: '李四', phone: '13800000002' },
  { label: '王五', phone: '13800000003' },
]

const applyDemoAccount = (phone) => {
  form.phone = phone
  form.password = '123456'
}

const handleSubmit = async () => {
  if (!form.phone.trim()) {
    ElMessage.warning('请输入手机号')
    return
  }

  if (!form.password.trim()) {
    ElMessage.warning('请输入密码')
    return
  }

  loading.value = true

  try {
    await userStore.loginUser(form)
    ElMessage.success('登录成功')
    router.push('/orders')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <section class="auth-hero">
      <p class="section-kicker">Mock Friendly Frontend</p>
      <h1>先把前端主干搭稳，再安心对接真实接口。</h1>
      <p>
        当前运行模式为 <strong>{{ appStore.apiMode }}</strong
        >，页面只通过统一 API 方法取数，后续切换到 live 模式时不用逐页清理假数据。
      </p>
      <div v-if="appStore.apiMode === 'mock'" class="page-actions">
        <el-button
          v-for="account in demoAccounts"
          :key="account.phone"
          plain
          @click="applyDemoAccount(account.phone)"
        >
          使用{{ account.label }}账号
        </el-button>
      </div>
    </section>

    <el-card class="auth-card" shadow="hover">
      <template #header>
        <div class="card-header-row">
          <div>
            <p class="section-kicker">User Auth</p>
            <h2>用户登录</h2>
          </div>
          <el-button link @click="router.push('/admin/login')">管理员入口</el-button>
        </div>
      </template>

      <el-form label-position="top" :model="form" @submit.prevent="handleSubmit">
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" show-password placeholder="请输入密码" />
        </el-form-item>
        <div class="page-actions">
          <el-button type="primary" :loading="loading" @click="handleSubmit">登录</el-button>
          <el-button @click="router.push('/register')">去注册</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>
