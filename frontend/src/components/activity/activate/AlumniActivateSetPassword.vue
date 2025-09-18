<template>
  <div class="activate-container">
    <el-button type="text" class="back-btn" @click="goHome" style="position: absolute; left: 24px; top: 24px; z-index: 10; font-size: 22px; height: 48px;">
      <el-icon style="vertical-align: middle; margin-right: 6px; font-size: 26px;"><ArrowLeft /></el-icon>返回
    </el-button>
    <el-card class="activate-card">
      <h2 class="activate-title">设置新密码</h2>
      <el-form :model="form" label-width="100px" @submit.prevent="onSubmit" autocomplete="off">
        <el-form-item label="新密码">
          <el-input v-model="form.password" type="password" required autocomplete="new-password" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="form.confirmPassword" type="password" required autocomplete="new-password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSubmit">下一步</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { setActivatePassword } from '../../api/activate'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const form = ref({ password: '', confirmPassword: '' })

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

async function onSubmit() {
  if (!form.value.password || form.value.password.length < 8) {
    ElMessage.error('密码长度至少8位')
    return
  }
  if (form.value.password !== form.value.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  const token = getActivateToken()
  if (!token) return
  const res = await setActivatePassword({
    email: token,
    password: form.value.password
  })
  if (res.code === 0) {
    ElMessage.success('密码设置成功，请填写私人邮箱')
    router.push({ path: '/alumni/activate/set-email' })
  } else {
    ElMessage.error(res.msg || '设置失败')
  }
}
</script>
<style scoped>
.activate-container { min-height: 80vh; display: flex; align-items: center; justify-content: center; }
.activate-card { width: 420px; padding: 32px; }
.activate-title { text-align: center; margin-bottom: 24px; }
</style> 