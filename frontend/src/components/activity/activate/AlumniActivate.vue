<template>
  <div class="activate-container">
    <el-button type="text" class="back-btn" @click="goHome" style="position: absolute; left: 24px; top: 24px; z-index: 10; font-size: 22px; height: 48px;">
      <el-icon style="vertical-align: middle; margin-right: 6px; font-size: 26px;"><ArrowLeft /></el-icon>返回
    </el-button>
    <el-card class="activate-card">
      <h2 class="activate-title">账号激活</h2>
      <el-form :model="form" label-width="100px" @submit.prevent="onSubmit">
        <el-form-item label="Alumni ID">
          <el-input v-model="form.alumniId" required />
        </el-form-item>
        <el-form-item label="中文姓名">
          <el-input v-model="form.chineseName" required />
        </el-form-item>
        <el-form-item label="英文姓">
          <el-input v-model="form.firstName" required />
        </el-form-item>
        <el-form-item label="英文名">
          <el-input v-model="form.lastName" required />
        </el-form-item>
        <el-form-item label="校园邮箱">
          <el-input v-model="form.campusEmail" required />
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker v-model="form.birthday" type="date" placeholder="请选择出生日期" value-format="YYYY-MM-DD" required />
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
import { useRouter } from 'vue-router'
import { activateAccount } from '../../api/activate'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
function goHome() { router.push('/') }
const form = ref({
  alumniId: '',
  chineseName: '',
  firstName: '',
  lastName: '',
  campusEmail: '',
  birthday: ''
})

async function onSubmit() {
  const data = {
    alumniId: form.value.alumniId,
    chineseName: form.value.chineseName,
    firstName: form.value.firstName,
    lastName: form.value.lastName,
    campusEmail: form.value.campusEmail,
    birthday: form.value.birthday
  }
  const res = await activateAccount(data)
  if (res.code === 0) {
    ElMessage.success('身份校验成功，请设置新密码')
    sessionStorage.setItem('activate_token', res.token)
    sessionStorage.setItem('activate_token_expire', Date.now() + 5 * 60 * 1000)
    router.push({ path: '/alumni/activate/set-password' })
  } else {
    ElMessage.error(res.msg)
  }
}
</script>
<style scoped>
.activate-container { min-height: 80vh; display: flex; align-items: center; justify-content: center; }
.activate-card { width: 420px; padding: 32px; }
.activate-title { text-align: center; margin-bottom: 24px; }
</style> 