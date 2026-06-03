<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { useUserStore } from '../../stores/user'
import { firstValidationError, validatePassword, validatePhone } from '../../utils/validate'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  password: '',
  phone: '',
})
const loading = ref(false)

const handleSubmit = async () => {
  if (loading.value) {
    return
  }

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
    await userStore.loginUser({
      password: form.password,
      phone: form.phone.trim(),
    })
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
        Campus Share Order
      </p>
      <h1>让校园拼单更省心、更透明。</h1>
      <p>
        和同学一起凑单、分摊配送费、跟踪凭证与收货状态，把每一次拼单都放在清楚的流程里。
      </p>
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

      <el-form
        label-position="top"
        :model="form"
        @submit.prevent="handleSubmit"
      >
        <el-form-item label="手机号">
          <el-input
            v-model="form.phone"
            maxlength="11"
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
        <span>还没有账号？注册后即可完善资料</span>
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
