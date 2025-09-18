<template>
  <div class="activate-container">
    <el-button type="text" class="back-btn" @click="goHome" style="position: absolute; left: 24px; top: 24px; z-index: 10; font-size: 22px; height: 48px;">
      <el-icon style="vertical-align: middle; margin-right: 6px; font-size: 26px;"><ArrowLeft /></el-icon>返回
    </el-button>
    <el-card class="activate-card">
      <h2 class="activate-title">设置私人邮箱</h2>
      <el-form :model="form" label-width="100px" @submit.prevent="onSubmit">
        <el-form-item label="私人邮箱">
          <el-input v-model="form.email" required />
          <el-button type="primary" @click="onSendCode" :disabled="countdown>0" style="margin-left:10px;">{{ countdown>0 ? `${countdown}s后重发` : '获取验证码' }}</el-button>
        </el-form-item>
        <el-form-item label="验证码">
          <el-input v-model="form.code" required />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSubmit">激活账号</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { setActivateEmail, verifyActivateCode, finalizeActivate } from '../../api/activate'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const form = ref({ email: '', code: '' })
const countdown = ref(0)
let timer = null

function goHome() { router.push('/') }

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

async function onSendCode() {
  if (!form.value.email) {
    ElMessage.error('请输入邮箱')
    return
  }
  const token = getActivateToken()
  if (!token) return
  const res = await setActivateEmail({ token, email: form.value.email })
  if (res.code === 0) {
    ElMessage.success('验证码已发送，请查收邮箱')
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } else {
    ElMessage.error(res.msg || '发送失败')
  }
}

async function onSubmit() {
  if (!form.value.code) {
    ElMessage.error('请输入验证码')
    return
  }
  const token = getActivateToken()
  if (!token) return
  const res = await verifyActivateCode({ email: token, code: form.value.code })
  if (res.code === 0) {
    const finishRes = await finalizeActivate({ token, new_email: form.value.email })
    if (finishRes.code === 0) {
      ElMessage.success('激活成功')
      router.push({ path: '/alumni/activate/success', query: {
        alumniId: finishRes.data?.alumniId || finishRes.alumniId || '',
        password: finishRes.data?.password || finishRes.password || ''
      } })
    } else {
      ElMessage.error(finishRes.msg || '激活失败')
    }
  } else {
    ElMessage.error(res.msg || '激活失败')
  }
}
</script>
<style scoped>
.activate-container { min-height: 80vh; display: flex; align-items: center; justify-content: center; }
.activate-card { width: 420px; padding: 32px; }
.activate-title { text-align: center; margin-bottom: 24px; }
</style> 