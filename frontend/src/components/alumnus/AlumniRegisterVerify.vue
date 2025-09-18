<template>
  <div>
    <el-button type="text" @click="goHome" style="position: absolute; left: 24px; top: 24px; font-size: 18px;"><el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回</el-button>
    <div class="centered-verify">
      <div class="verify-card">
        <h1 class="verify-title">新增校友信息</h1>
        <div class="verify-desc">请输入您收到的邮箱验证码，完成校友注册。</div>
      </div>
      <div class="verify-card">
        <h2 class="verify-subtitle">验证码验证</h2>
        <div class="verify-form-row">
          <input v-model="code" placeholder="请输入验证码" class="verify-input" />
          <button @click="verifyCode" class="verify-btn">验证</button>
        </div>
        <div v-if="codeError" class="verify-error">{{ codeError }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ArrowLeft } from '@element-plus/icons-vue'

const code = ref('')
const codeError = ref('')
const route = useRoute()
const router = useRouter()
const email = route.query.email

async function verifyCode() {
  if (!code.value) {
    codeError.value = '请输入验证码'
    return
  }
  const token = sessionStorage.getItem('register_token')
  try {
    const res = await axios.post('/api/register/verify', { verify_code: code.value }, {
      headers: { Authorization: 'Bearer ' + token }
    })
    if (res.data.code !== 0) {
      codeError.value = res.data.msg || '验证码错误'
      return
    }
    // 跳转到设置密码页面
    router.push({ path: '/alumni/register/set-password', query: { email } })
  } catch (e) {
    codeError.value = '网络错误'
  }
}

function goHome() { router.push('/') }
</script>

<style scoped>
.centered-verify {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
  gap: 32px;
}
.verify-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 8px #eee;
  padding: 32px;
  width: 520px;
  margin-bottom: 0;
}
.verify-title {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 12px;
  text-align: center;
}
.verify-desc {
  color: #64748b;
  font-size: 1.1rem;
  text-align: center;
}
.verify-subtitle {
  font-size: 1.3rem;
  font-weight: bold;
  margin-bottom: 16px;
  text-align: left;
}
.verify-form-row {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.verify-input {
  flex: 1;
  padding: 10px;
  border-radius: 6px 0 0 6px;
  border: 1px solid #d1d5db;
  outline: none;
  font-size: 1rem;
}
.verify-btn {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 0 6px 6px 0;
  padding: 0 18px;
  font-size: 1rem;
  cursor: pointer;
}
.verify-error {
  color: #e11d48;
  margin-top: 6px;
  font-size: 0.95rem;
}
@media (max-width: 600px) {
  .centered-verify {
    gap: 16px;
    min-height: 60vh;
  }
  .verify-card {
    width: 100%;
    min-width: 0;
    padding: 14px 8px;
    border-radius: 0;
    box-shadow: none;
  }
  .verify-title {
    font-size: 1.3rem;
    margin-bottom: 8px;
  }
  .verify-desc {
    font-size: 0.95rem;
  }
  .verify-subtitle {
    font-size: 1.05rem;
    margin-bottom: 10px;
  }
  .verify-form-row {
    flex-direction: column;
    gap: 8px;
    margin-bottom: 10px;
  }
  .verify-input {
    border-radius: 6px;
    font-size: 0.98rem;
    padding: 8px;
  }
  .verify-btn {
    border-radius: 6px;
    font-size: 0.98rem;
    padding: 8px 0;
    width: 100%;
  }
}
</style> 