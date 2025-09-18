<template>
  <div class="activate-container">
    <el-button type="text" class="back-btn" @click="goHome" style="position: absolute; left: 24px; top: 24px; z-index: 10; font-size: 22px; height: 48px;">
      <el-icon style="vertical-align: middle; margin-right: 6px; font-size: 26px;"><ArrowLeft /></el-icon>返回
    </el-button>
    <el-card class="activate-card">
      <h2 class="activate-title">激活成功</h2>
      <div class="success-info">
        <p>您的账号已激活！</p>
        <p>学号：<b>{{ alumniId }}</b></p>
        <p>初始密码：<b>{{ password }}</b></p>
        <el-alert title="登录后请前往信息更新界面进行信息更新" type="info" show-icon style="margin: 20px 0;" />
        <el-button type="primary" @click="onClose">关闭</el-button>
      </div>
    </el-card>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const alumniId = ref('')
const password = ref('')

const getActivateToken = () => {
  const token = sessionStorage.getItem('activate_token')
  const expire = sessionStorage.getItem('activate_token_expire')
  if (!token || !expire || Date.now() > Number(expire)) {
    sessionStorage.removeItem('activate_token')
    sessionStorage.removeItem('activate_token_expire')
    ElMessage.error('激活流程已过期，请重新激活')
    router.push('/alumni/activate')
    return null
  }
  return token
}

onMounted(() => {
  alumniId.value = route.query.alumniId || ''
  password.value = route.query.password || ''
  // 激活完成后清除token
  sessionStorage.removeItem('activate_token')
  sessionStorage.removeItem('activate_token_expire')
})

function onClose() {
  router.push('/login')
}

function goHome() { router.push('/') }
</script>
<style scoped>
.activate-container { min-height: 80vh; display: flex; align-items: center; justify-content: center; }
.activate-card { width: 420px; padding: 32px; }
.activate-title { text-align: center; margin-bottom: 24px; }
.success-info { text-align: center; }
</style> 