<template>
  <div>
    <el-button type="text" @click="goHome" style="position: absolute; left: 24px; top: 24px; font-size: 18px;"><el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回</el-button>
    <div class="centered-register">
      <div class="register-card">
        <h1 class="register-title">欢迎来到校友平台</h1>
        <div class="register-desc">欢迎您来到校友平台，请按照提示进行信息注册。<br>首先，请输入您的私人邮箱，我们将向该邮箱发送验证码。</div>
      </div>
      <div class="register-card">
        <h2 class="register-subtitle">邮箱验证</h2>
        <div class="register-form-row">
          <label class="register-label">私人邮箱</label>
          <div class="register-input-group">
            <input v-model="email" @input="clearError" :class="{'input-error': emailError}" placeholder="请输入您的私人邮箱" class="register-input" />
            <button @click="sendCode" class="register-btn">发送验证码</button>
          </div>
          <div v-if="emailError" class="register-error">{{ emailError }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ArrowLeft } from '@element-plus/icons-vue'

const email = ref('')
const emailError = ref('')
const router = useRouter()

function validateEmail(val) {
  // 简单邮箱正则
  return /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(val)
}

function clearError() {
  emailError.value = ''
}

async function sendCode() {
  if (!validateEmail(email.value)) {
    emailError.value = '请输入正确的邮箱'
    return
  }
  try {
    const res = await axios.post('/api/register/verify-email', { email: email.value })
    if (res.data.code === 1) {
      emailError.value = res.data.msg || '该邮箱已被注册'
      return
    }
    // 保存token
    sessionStorage.setItem('register_token', res.data.data.token)
    // 跳转到验证码页面
    router.push({ path: '/alumni/register/verify', query: { email: email.value } })
  } catch (e) {
    emailError.value = '网络错误'
  }
}

function goHome() { router.push('/') }
</script>

<style scoped>
.centered-register {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
  gap: 32px;
}
.register-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 8px #eee;
  padding: 32px;
  width: 520px;
  margin-bottom: 0;
}
.register-title {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 12px;
  text-align: center;
}
.register-desc {
  color: #64748b;
  font-size: 1.1rem;
  text-align: center;
}
.register-subtitle {
  font-size: 1.3rem;
  font-weight: bold;
  margin-bottom: 16px;
  text-align: left;
}
.register-form-row {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.register-label {
  font-size: 1rem;
  color: #222;
  margin-bottom: 4px;
}
.register-input-group {
  display: flex;
}
.register-input {
  flex: 1;
  padding: 10px;
  border-radius: 6px 0 0 6px;
  border: 1px solid #d1d5db;
  outline: none;
  font-size: 1rem;
}
.register-btn {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 0 6px 6px 0;
  padding: 0 18px;
  font-size: 1rem;
  cursor: pointer;
}
.register-error {
  color: #e11d48;
  margin-top: 6px;
  font-size: 0.95rem;
}
.input-error {
  border-color: #e11d48 !important;
}
@media (max-width: 600px) {
  .centered-register {
    gap: 16px;
    min-height: 60vh;
  }
  .register-card {
    width: 100%;
    min-width: 0;
    padding: 14px 8px;
    border-radius: 0;
    box-shadow: none;
  }
  .register-title {
    font-size: 1.3rem;
    margin-bottom: 8px;
  }
  .register-desc {
    font-size: 0.95rem;
  }
  .register-subtitle {
    font-size: 1.05rem;
    margin-bottom: 10px;
  }
  .register-form-row {
    gap: 6px;
    margin-bottom: 10px;
  }
  .register-label {
    font-size: 0.98rem;
    margin-bottom: 2px;
  }
  .register-input-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  .register-input {
    border-radius: 6px;
    font-size: 0.98rem;
    padding: 8px;
  }
  .register-btn {
    border-radius: 6px;
    font-size: 0.98rem;
    padding: 8px 0;
    width: 100%;
  }
}
</style> 