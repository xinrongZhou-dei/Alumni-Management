<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import jwtDecode from 'jwt-decode'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const isLogin = ref(!!sessionStorage.getItem('alumni_token'))
const alumniName = ref('')
let tokenCheckInterval = null

// 检查token是否过期
function checkTokenExpiration() {
  const token = sessionStorage.getItem('alumni_token')
  if (!token) return

  try {
    const payload = jwtDecode(token)
    const now = Math.floor(Date.now() / 1000)
    if (payload.exp && now > payload.exp) {
      // token已过期，清除登录状态
      sessionStorage.removeItem('alumni_token')
      sessionStorage.removeItem('user_info')
      isLogin.value = false
      alumniName.value = ''
      ElMessage({ message: '登录已过期，请重新登录', type: 'warning', duration: 2000, showClose: false })
      router.push('/login')
    }
  } catch (e) {
    console.error('token解析失败', e)
  }
}

// 检查register_token是否过期
function checkRegisterTokenExpiration() {
  const registerToken = sessionStorage.getItem('register_token')
  if (!registerToken) return

  try {
    const payload = jwtDecode(registerToken)
    const now = Math.floor(Date.now() / 1000)
    if (payload.exp && now > payload.exp) {
      // register_token已过期，清除注册状态
      sessionStorage.removeItem('register_token')
      ElMessage({ message: '注册会话已过期，请重新开始注册', type: 'warning', duration: 2000, showClose: false })
      // 如果当前在注册流程页面，跳转到注册首页
      if (route.path.startsWith('/alumni/register')) {
        router.push('/alumni/register')
      }
    }
  } catch (e) {
    console.error('register_token解析失败', e)
    // 解析失败也清除无效token
    sessionStorage.removeItem('register_token')
  }
}

onMounted(() => {
  // 每60秒检查一次token是否过期
  tokenCheckInterval = setInterval(() => {
    checkTokenExpiration()
    checkRegisterTokenExpiration()
  }, 60000)
  // 立即检查一次
  checkTokenExpiration()
  checkRegisterTokenExpiration()
})

onUnmounted(() => {
  if (tokenCheckInterval) {
    clearInterval(tokenCheckInterval)
  }
})

// 保留UI相关变量和方法，不做任何token校验
</script>

<template>
  <main class="main-content">
    <router-view />
  </main>
</template>

<style>
body, #app, .app-root {
  margin: 0;
  padding: 0;
  width: 100vw;
  min-height: 100vh;
  background: #f5f6fa;
}
.app-root {
  width: 100vw;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.main-header {
  width: 100vw;
  background: #fff;
  color: #222;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2px 8px #e0e7ef33;
  border-bottom: 1px solid #e5e7eb;
}
.main-header-inner {
  width: 100%;
  max-width: 1200px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 32px;
  margin: 0 auto;
}
.logo-title {
  font-size: 1.3rem;
  font-weight: bold;
  letter-spacing: 1px;
  color: #2563eb;
  flex: 1;
  text-align: left;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 18px;
}
.main-content {
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  box-sizing: border-box;
  padding: 48px 0;
  background: #f5f6fa;
}
.main-content > * {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}
.alumni-name {
  margin-right: 16px;
  font-size: 1rem;
  color: #2563eb;
  font-weight: bold;
}
</style>
