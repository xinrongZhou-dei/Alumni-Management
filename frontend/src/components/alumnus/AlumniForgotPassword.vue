<template>
  <div>
    <el-button type="text" @click="goHome" style="position: absolute; left: 24px; top: 24px; font-size: 18px;"><el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回</el-button>
    <div class="forgot-container">
      <div class="forgot-card">
        <h2>忘记密码</h2>
        <form v-if="step === 1" @submit.prevent="handleEmail">
          <div class="form-item">
            <label>注册邮箱</label>
            <input v-model="email" type="email" required placeholder="请输入注册邮箱" />
          </div>
          <button class="submit-btn" type="submit">获取验证码</button>
        </form>
        <form v-else-if="step === 2" @submit.prevent="handleVerify">
          <div class="form-item">
            <label>验证码</label>
            <input v-model="verifyCode" required placeholder="请输入验证码" />
          </div>
          <button class="submit-btn" type="submit">验证</button>
        </form>
        <form v-else-if="step === 3" @submit.prevent="handleReset">
          <div class="form-item">
            <label>新密码</label>
            <input v-model="newPassword" type="password" required @input="validateNewPassword" placeholder="请输入新密码" />
            <div v-if="newPasswordError" class="form-error">{{ newPasswordError }}</div>
          </div>
          <div class="form-item">
            <label>确认新密码</label>
            <input v-model="confirmPassword" type="password" required @input="validateConfirmPassword" placeholder="请再次输入新密码" />
            <div v-if="confirmPasswordError" class="form-error">{{ confirmPasswordError }}</div>
          </div>
          <button class="submit-btn" type="submit" :disabled="!canSubmit">重置密码</button>
        </form>
        <div v-if="msg" class="form-msg">{{ msg }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import axios from 'axios'
import md5 from 'crypto-js/md5'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const step = ref(1)
const email = ref('')
const verifyCode = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const newPasswordError = ref('')
const confirmPasswordError = ref('')
const msg = ref('')
const token = ref('')
const router = useRouter()

async function handleEmail() {
  msg.value = ''
  if (!email.value) {
    msg.value = '请输入邮箱'
    return
  }
  try {
    const res = await axios.post('/api/forgot-password', { email: email.value })
    if (res.data.code === 0) {
      msg.value = '验证码已发送到邮箱（测试用：123456）'
      token.value = res.data.data.token
      step.value = 2
    } else {
      msg.value = res.data.msg || '邮箱不存在'
    }
  } catch (e) {
    msg.value = '请求失败'
  }
}

async function handleVerify() {
  msg.value = ''
  if (!verifyCode.value) {
    msg.value = '请输入验证码'
    return
  }
  try {
    const res = await axios.put('/api/forgot-password-verify', {
      verifyCode: verifyCode.value
    }, {
      headers: { 
        'Authorization': 'Bearer ' + token.value
      }
    })
    if (res.data.code === 0) {
      ElMessage.success('验证成功')
      step.value = 3
    } else {
      ElMessage.error(res.data.msg || '验证失败')
    }
  } catch (e) {
    ElMessage.error('请求失败: ' + e.message)
  }
}

function validateNewPassword() {
  const val = newPassword.value
  if (!/^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$/.test(val)) {
    newPasswordError.value = '密码至少8位，包含字母和数字'
  } else {
    newPasswordError.value = ''
  }
  validateConfirmPassword()
}

function validateConfirmPassword() {
  if (confirmPassword.value !== newPassword.value) {
    confirmPasswordError.value = '两次输入的新密码不一致'
  } else {
    confirmPasswordError.value = ''
  }
}

const canSubmit = computed(() =>
  newPassword.value && confirmPassword.value && !newPasswordError.value && !confirmPasswordError.value
)

async function handleReset() {
  validateNewPassword()
  validateConfirmPassword()
  if (!canSubmit.value) return
  try {
    const res = await axios.put('/api/forgot-password-put', {
      email: email.value,
      newPassword: md5(newPassword.value).toString()
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    })
    if (res.data.code === 0) {
      msg.value = '重置成功，请重新登录'
      sessionStorage.removeItem('alumni_token')
      sessionStorage.removeItem('user_info')
      setTimeout(() => {
        router.push('/login')
      }, 1200)
    } else {
      msg.value = res.data.msg || '重置失败'
    }
  } catch (e) {
    msg.value = '请求失败: ' + (e.response?.data?.msg || e.message)
  }
}

function goHome() { router.push('/') }
</script>

<style scoped>
.forgot-container {
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}
.forgot-card {
  background: #fff;
  padding: 32px 40px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  width: 400px;
}
.form-item {
  margin-bottom: 18px;
  display: flex;
  flex-direction: column;
}
.form-item label {
  font-size: 1rem;
  margin-bottom: 6px;
  color: #222;
}
.form-item input {
  padding: 8px 10px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 1rem;
}
.form-error {
  color: #e53e3e;
  font-size: 0.95rem;
  margin-top: 4px;
}
.form-msg {
  color: #2563eb;
  font-size: 1rem;
  margin-top: 16px;
  text-align: center;
}
.submit-btn {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 10px 0;
  font-size: 1rem;
  cursor: pointer;
  width: 100%;
  margin-top: 8px;
  transition: background 0.2s;
}
.submit-btn:disabled {
  background: #a5b4fc;
  cursor: not-allowed;
}
.submit-btn:hover:enabled {
  background: #1746a2;
}
</style> 