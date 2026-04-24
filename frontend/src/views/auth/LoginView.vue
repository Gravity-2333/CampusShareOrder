<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { useAppStore } from '../../stores/app'
import { useUserStore } from '../../stores/user'
import { firstValidationError, validatePassword, validatePhone } from '../../utils/validate'

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
const heroMetrics = [
  { label: '统一返回', value: '{ code, message, data }' },
  { label: '鉴权方式', value: 'Bearer Token' },
  { label: '数据模式', value: 'mock / live' },
]

const applyDemoAccount = (phone) => {
  form.phone = phone
  form.password = '123456'
}

const handleSubmit = async () => {
  const errorMessage = firstValidationError([
    validatePhone(form.phone),
    validatePassword(form.password),
  ])

  if (errorMessage) {
    ElMessage.warning(errorMessage)
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
      <p class="section-kicker">
        契约优先前端
      </p>
      <h1>先把前端主干搭稳，再安心对接真实接口。</h1>
      <p>
        当前运行模式为 <strong>{{ appStore.apiMode }}</strong>，页面只通过统一 API 方法取数，业务代码不直接依赖临时数据。
      </p>

      <div class="auth-metric-grid">
        <article
          v-for="item in heroMetrics"
          :key="item.label"
          class="auth-metric-card"
        >
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </article>
      </div>

      <div class="auth-side-note">
        <strong>当前重点</strong>
        <p>先把用户主流程、订单详情闭环和后续联调结构稳定，再逐步切到真实接口。</p>
      </div>

      <div
        v-if="appStore.apiMode === 'mock'"
        class="auth-demo-grid"
      >
        <button
          v-for="account in demoAccounts"
          :key="account.phone"
          type="button"
          class="auth-demo-card"
          @click="applyDemoAccount(account.phone)"
        >
          <span>演示账号</span>
          <strong>{{ account.label }}</strong>
          <small>{{ account.phone }}</small>
        </button>
      </div>
    </section>

    <el-card
      class="auth-card"
      shadow="hover"
    >
      <template #header>
        <div class="card-header-row">
          <div>
            <p class="section-kicker">
              用户入口
            </p>
            <h2>用户登录</h2>
          </div>
          <el-button
            link
            @click="router.push('/admin/login')"
          >
            管理员入口
          </el-button>
        </div>
      </template>

      <div class="auth-card-summary">
        <strong>欢迎回来</strong>
        <p>登录后会先进入拼单大厅，后续创建拼单、参与拼单和投诉流程都从统一路由继续。</p>
      </div>

      <el-form
        label-position="top"
        :model="form"
        @submit.prevent="handleSubmit"
      >
        <el-form-item label="手机号">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
          />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            show-password
            placeholder="请输入密码"
          />
        </el-form-item>
        <div class="page-actions">
          <el-button
            type="primary"
            :loading="loading"
            @click="handleSubmit"
          >
            登录
          </el-button>
          <el-button @click="router.push('/register')">
            去注册
          </el-button>
        </div>
      </el-form>

      <div class="auth-card-footer">
        <span>普通用户与管理员账号完全分离</span>
        <button
          type="button"
          class="auth-inline-link"
          @click="router.push('/admin/login')"
        >
          切换到管理员登录
        </button>
      </div>
    </el-card>
  </div>
</template>
