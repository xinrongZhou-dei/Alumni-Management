<template>
  <el-button type="text" class="back-btn" @click="goHome" style="position: fixed; left: 16px; top: 16px; z-index: 2000; font-size: 22px; height: 48px; padding: 0 18px; width: auto; min-width: unset; max-width: unset;">
    <el-icon style="vertical-align: middle; margin-right: 6px; font-size: 26px;"><ArrowLeft /></el-icon>返回
  </el-button>
  <div class="login-pc-container">
    <div class="login-pc-card">
      <div class="login-pc-logo-row">
        <img class="login-pc-logo" src="/logo.png" alt="校友平台Logo" />
        <span class="login-pc-title">校友平台登录</span>
      </div>
      <form @submit.prevent="onLogin" class="login-pc-form">
        <div class="login-pc-field">
          <label>校友ID</label>
          <input v-model="alumniId" placeholder="请输入校友ID" />
        </div>
        <div class="login-pc-field">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="请输入密码" />
        </div>
        <div class="login-pc-field captcha-field">
          <label>验证码</label>
          <div style="display: flex; align-items: center; gap: 10px;">
            <input v-model="captchaCode" placeholder="请输入验证码" maxlength="6" style="flex: 1;" />
            <img :src="captchaImgUrl" @click="refreshCaptcha" :title="'点击刷新验证码'" style="height: 40px; cursor: pointer; border-radius: 4px; border: 1px solid #eee; background: #f8fafc;" />
          </div>
        </div>
        <button class="login-pc-btn" type="submit">登录</button>
        <div v-if="errorMsg" class="login-pc-error">{{ errorMsg }}</div>
        <div class="login-pc-forgot-row">
          <button type="button" class="login-pc-register-btn" @click="goRegister">注册</button>
          <button type="button" class="login-pc-forgot-btn" @click="goForgotPassword">忘记密码？</button>
        </div>
      </form>
    </div>
  </div>
  
  <!-- 微信关注弹窗 -->
  <WeChatFollowModal 
    v-if="showWeChatModal"
    :alumni-id="currentAlumniId"
    @follow-success="onWeChatFollowSuccess"
    @close="showWeChatModal = false"
  />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import service from '../../utils/request' // 导入配置好的axios实例
import md5 from 'crypto-js/md5'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import WeChatFollowModal from '../WeChatFollowModal.vue'

const alumniId = ref('')
const password = ref('')
const errorMsg = ref('')
const router = useRouter()
const showWeChatModal = ref(false)
const currentAlumniId = ref('')
const captchaCode = ref('')
const captchaUuid = ref('')
const captchaImgUrl = ref('')

function refreshCaptcha() {
  captchaUuid.value = ''
  captchaImgUrl.value = ''
  // 请求验证码图片
  fetch('/api/captcha', { method: 'GET' })
    .then(async res => {
      const uuid = res.headers.get('Captcha-UUID')
      captchaUuid.value = uuid
      const blob = await res.blob()
      captchaImgUrl.value = URL.createObjectURL(blob)
    })
}

onMounted(() => {
  refreshCaptcha()
})

async function onLogin() {
  errorMsg.value = ''
  if (!alumniId.value || !password.value) {
    errorMsg.value = '请输入校友ID和密码'
    return
  }
  if (!captchaCode.value) {
    errorMsg.value = '请输入验证码'
    return
  }
  try {
    const encryptedPwd = md5(password.value).toString()
    const res = await service.post('/login', {
      alumni_id: alumniId.value,
      password: encryptedPwd,
      captchaCode: captchaCode.value,
      captchaUuid: captchaUuid.value
    })
    if (res.code === 0) {
      const userInfo = {
        chineseName: res.data.chineseName,
        affiliation: res.data.affiliation,
        role: res.data.role || 'alumni',
        alumniId: res.data.alumni_id
      }
      sessionStorage.setItem('user_info', JSON.stringify(userInfo))
      sessionStorage.setItem('alumni_token', res.data.token)
      sessionStorage.setItem('login_success_flag', '1')
      const redirectPath = sessionStorage.getItem('redirect_url')
      if (redirectPath) {
        sessionStorage.removeItem('redirect_url')
        router.push(redirectPath)
      } else {
        router.push('/') // 或其他默认页
      }
      // 不再强制reload页面
    } else if (res.code === 2) {
      ElMessage.error('账号未激活，请在首页点击"获取校友AlumniID"或等待管理员审核')
      // 不写入token和user_info
    } else if (res.code === 3) {
      // 需要关注微信服务号
      currentAlumniId.value = alumniId.value
      showWeChatModal.value = true
    } else if (res.code === 1001 || res.msg?.includes('验证码')) {
      errorMsg.value = res.msg || '验证码错误或已失效'
      refreshCaptcha()
      captchaCode.value = ''
    } else {
      errorMsg.value = res.msg || '登录失败'
    }
  } catch (e) {
    errorMsg.value = '网络错误'
  }
}

function goForgotPassword() {
  router.push('/alumni/forgot-password')
}

function goRegister() {
  router.push('/alumni/register')
}

function goHome() { router.push('/') }

// 处理微信关注成功后的回调
function onWeChatFollowSuccess() {
  showWeChatModal.value = false
  // 重新尝试登录
  onLogin()
}
</script>

<style scoped>
.login-pc-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(120deg, #e0e7ff 0%, #f1f5f9 100%);
}
.login-pc-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 16px #c7d2fe44;
  width: 420px;
  padding: 40px 36px 32px 36px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.login-pc-logo-row {
  display: flex;
  align-items: center;
  margin-bottom: 32px;
}
.login-pc-logo {
  width: 48px;
  height: 48px;
  margin-right: 16px;
}
.login-pc-title {
  font-size: 1.6rem;
  font-weight: bold;
  color: #2563eb;
}
.login-pc-form {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: stretch;
}
.login-pc-field {
  margin-bottom: 22px;
  display: flex;
  flex-direction: column;
}
.login-pc-field label {
  font-size: 1rem;
  color: #222;
  margin-bottom: 6px;
}
.login-pc-field input {
  padding: 10px;
  border-radius: 6px;
  border: 1px solid #d1d5db;
  font-size: 1rem;
}
.login-pc-btn {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 12px 0;
  font-size: 1.1rem;
  cursor: pointer;
  margin-top: 8px;
  width: 100%;
}
.login-pc-error {
  color: #e11d48;
  margin-top: 18px;
  text-align: center;
}
.login-pc-forgot-row {
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.login-pc-register-btn {
  background: none;
  border: none;
  color: #2563eb;
  font-size: 1rem;
  cursor: pointer;
  padding: 0;
  text-decoration: underline;
}
.login-pc-register-btn:hover {
  color: #1746a2;
}
.login-pc-forgot-btn {
  background: none;
  border: none;
  color: #2563eb;
  font-size: 1rem;
  cursor: pointer;
  padding: 0;
  text-decoration: underline;
}
.login-pc-forgot-btn:hover {
  color: #1746a2;
}
.captcha-field input {
  letter-spacing: 2px;
  font-size: 1.1rem;
  text-transform: uppercase;
}
</style> 