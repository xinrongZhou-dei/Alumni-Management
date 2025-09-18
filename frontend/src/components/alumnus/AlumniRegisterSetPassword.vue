<template>
  <div>
    <el-button type="text" @click="goHome" style="position: absolute; left: 24px; top: 24px; font-size: 18px;"><el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回</el-button>
    <div class="centered-setpwd">
      <div class="setpwd-card">
        <h1 class="setpwd-title">新增校友信息</h1>
        <div class="setpwd-desc">欢迎您来到校友平台，请按照提示进行信息注册。<br>现在，请按照要求设置您的密码</div>
      </div>
      <div class="setpwd-card">
        <h2 class="setpwd-subtitle">设置密码</h2>
        <div class="setpwd-form-row">
          <label class="setpwd-label">密码</label>
          <input v-model="password" type="password" @input="clearError" :class="{'input-error': passwordError}" placeholder="请输入密码" class="setpwd-input" />
          <div class="setpwd-tip">密码长度至少8位，必须包含字母和数字</div>
          <label class="setpwd-label">确认密码</label>
          <input v-model="confirmPassword" type="password" @input="clearError" :class="{'input-error': passwordError}" placeholder="请再次输入密码" class="setpwd-input" />
          <div v-if="passwordError" class="setpwd-error">{{ passwordError }}</div>
        </div>
        <button @click="setPassword" :disabled="!canSubmit"
          :style="{
            background: '#2563eb', color: '#fff', border: 'none', borderRadius: '6px', padding: '10px 32px', fontSize: '1.1rem', cursor: 'pointer', float: 'right', opacity: canSubmit ? 1 : 0.6
          }"
        >设置密码</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { ArrowLeft } from '@element-plus/icons-vue'
import md5 from 'md5'

const password = ref('')
const confirmPassword = ref('')
const passwordError = ref('')

const router = useRouter()
const route = useRoute()
const email = route.query.email

function clearError() {
  passwordError.value = ''
}

function validatePassword(pwd) {
  // 至少8位，包含字母和数字
  return /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$/.test(pwd)
}

const canSubmit = computed(() => {
  return password.value && confirmPassword.value && !passwordError.value
})

function ensureTokenCookie() {
  const token = sessionStorage.getItem('register_token');
  if (token) {
    document.cookie = `alumni-token=${token}; path=/`;
  }
}

onMounted(() => {
  ensureTokenCookie();
});

async function setPassword() {
  if (!validatePassword(password.value)) {
    passwordError.value = '密码长度至少8位，必须包含字母和数字'
    return
  }
  if (password.value !== confirmPassword.value) {
    passwordError.value = '两次输入的密码不一致'
    return
  }
  const token = sessionStorage.getItem('register_token')
  try {
    const res = await axios.post('/api/register/set-password', { password: password.value }, {
      headers: { Authorization: 'Bearer ' + token }
    })
    if (res.data.code !== 0) {
      passwordError.value = res.data.msg || '设置密码失败'
      return
    }
    sessionStorage.setItem('register_password', md5(password.value))
    ensureTokenCookie();
    router.push({ path: '/alumni/register/submit-info', query: { email } })
  } catch (e) {
    console.error('设置密码错误:', e)
    console.error('错误详情:', e.response ? e.response.data : '无响应数据')
    if (e.response) {
      passwordError.value = e.response.data.msg || '设置密码失败'
    } else if (e.request) {
      passwordError.value = '无法连接到服务器，请检查网络连接'
    } else {
      passwordError.value = '发生错误：' + e.message
    }
  }
}

function goHome() { router.push('/') }
</script>

<style scoped>
.centered-setpwd {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
  gap: 32px;
}
.setpwd-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 8px #eee;
  padding: 32px;
  width: 520px;
  margin-bottom: 0;
}
.setpwd-title {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 12px;
  text-align: center;
}
.setpwd-desc {
  color: #64748b;
  font-size: 1.1rem;
  text-align: center;
}
.setpwd-subtitle {
  font-size: 1.3rem;
  font-weight: bold;
  margin-bottom: 16px;
  text-align: left;
}
.setpwd-form-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}
.setpwd-label {
  font-size: 1rem;
  color: #222;
  margin-bottom: 4px;
}
.setpwd-input {
  width: 100%;
  padding: 10px;
  border-radius: 6px;
  border: 1px solid #d1d5db;
  outline: none;
  font-size: 1rem;
  margin-bottom: 6px;
}
.setpwd-tip {
  color: #64748b;
  font-size: 0.95rem;
  margin-bottom: 8px;
}
.setpwd-error {
  color: #e11d48;
  margin-top: 6px;
  font-size: 0.95rem;
}
.input-error {
  border-color: #e11d48 !important;
}
@media (max-width: 600px) {
  .centered-setpwd {
    gap: 16px;
    min-height: 60vh;
  }
  .setpwd-card {
    width: 100%;
    min-width: 0;
    padding: 14px 8px;
    border-radius: 0;
    box-shadow: none;
  }
  .setpwd-title {
    font-size: 1.3rem;
    margin-bottom: 8px;
  }
  .setpwd-desc {
    font-size: 0.95rem;
  }
  .setpwd-subtitle {
    font-size: 1.05rem;
    margin-bottom: 10px;
  }
  .setpwd-form-row {
    gap: 6px;
    margin-bottom: 10px;
  }
  .setpwd-label {
    font-size: 0.98rem;
    margin-bottom: 2px;
  }
  .setpwd-input {
    font-size: 0.98rem;
    padding: 8px;
    margin-bottom: 4px;
  }
  .setpwd-tip {
    font-size: 0.92rem;
    margin-bottom: 6px;
  }
  .setpwd-error {
    font-size: 0.92rem;
    margin-top: 4px;
  }
  .setpwd-card button {
    width: 100%;
    padding: 10px 0;
    font-size: 1rem;
    float: none !important;
    margin-top: 8px;
  }
}
</style> 