<template>
  <div>
    <el-button type="text" @click="goHome" style="position: absolute; left: 24px; top: 24px; font-size: 18px;"><el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回</el-button>
    <div class="change-password-container">
      <div class="change-password-card">
        <h2>修改密码</h2>
        <form @submit.prevent="handleSubmit">
          <div class="form-item">
            <label>原密码</label>
            <input v-model="pastPassword" type="password" required autocomplete="current-password" />
          </div>
          <div class="form-item">
            <label>新密码</label>
            <input v-model="newPassword" type="password" required autocomplete="new-password" @input="validateNewPassword" />
            <div v-if="newPasswordError" class="form-error">{{ newPasswordError }}</div>
          </div>
          <div class="form-item">
            <label>确认新密码</label>
            <input v-model="confirmPassword" type="password" required autocomplete="new-password" @input="validateConfirmPassword" />
            <div v-if="confirmPasswordError" class="form-error">{{ confirmPasswordError }}</div>
          </div>
          <button class="submit-btn" type="submit" :disabled="!canSubmit">修改密码</button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import service from '../../utils/request' // 使用统一的request实例
import md5 from 'crypto-js/md5'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const pastPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const newPasswordError = ref('')
const confirmPasswordError = ref('')

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
  pastPassword.value && newPassword.value && confirmPassword.value && !newPasswordError.value && !confirmPasswordError.value
)
async function handleSubmit() {
  validateNewPassword()
  validateConfirmPassword()
  if (!canSubmit.value) return
  const token = sessionStorage.getItem('alumni_token')
  if (!token) {
    alert('请先登录')
    router.push('/login')
    return
  }
  try {
    const res = await service.put('/password', {
      pastPassword: md5(pastPassword.value).toString(),
      newPassword: md5(newPassword.value).toString()
    }, {
      headers: { 
        'Authorization': 'Bearer ' + token
      }
    })
    if (res.code === 0) {
      alert('修改成功，请重新登录')
      sessionStorage.removeItem('alumni_token')
      sessionStorage.removeItem('user_info')
      router.push('/login')
    } else {
      alert(res.msg || '修改失败')
    }
  } catch (e) {
    console.error('修改密码请求失败:', e)
    alert('请求失败: ' + (e.response?.data?.msg || e.message))
  }
}

function goHome() { router.push('/') }
</script>

<style scoped>
.change-password-container {
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}
.change-password-card {
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