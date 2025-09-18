<template>
  <div class="email-template-add">
    <el-button type="text" @click="goBackManage" class="back-btn" :style="isMobile ? 'position: static; margin-bottom: 16px;' : ''">
      <el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回
    </el-button>
    <h2 :class="{'mobile-title': isMobile}">添加邮件模版</h2>
    <el-form 
      :model="form" 
      :label-width="isMobile ? '80px' : '100px'" 
      :label-position="isMobile ? 'top' : 'right'"
      class="template-form"
    >
      <el-form-item label="模版名称">
        <el-input v-model="form.templateName" maxlength="100" />
      </el-form-item>
      <el-form-item label="主题">
        <el-input v-model="form.theme" maxlength="100" />
      </el-form-item>
      <el-form-item label="目标对象">
        <el-select v-model="form.targetType" placeholder="请选择对象" :disabled="emailTemplatePermission === 1" style="width: 100%;">
          <el-option label="校友" value="alumni" />
          <el-option label="管理员" value="admin" />
        </el-select>
      </el-form-item>
      <el-form-item label="内容">
        <el-input
          type="textarea"
          v-model="form.content"
          :rows="isMobile ? 6 : 8"
          placeholder="可插入变量，如{{alumni_id}}"
        />
      </el-form-item>
      <el-form-item label="可用变量" class="variable-container">
        <div class="variable-list">
          <el-tag
            v-for="v in variableList"
            :key="v.name"
            @click="insertVariable(v.name)"
            class="variable-tag"
            v-html="`{{${v.name}}}：${v.desc}`"
          />
        </div>
      </el-form-item>
      <el-form-item class="form-buttons">
        <div class="button-container">
          <el-button type="primary" @click="submit" class="form-btn">保存</el-button>
          <el-button @click="goBack" class="form-btn">取消</el-button>
        </div>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { addEmailTemplate, updateEmailTemplate, deleteEmailTemplate } from '../../api/emailTemplate'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const emailTemplatePermission = ref(0) // 邮件模版权限等级

// 判断是否为移动设备
const isMobile = ref(window.innerWidth <= 768)

// 监听窗口大小变化
window.addEventListener('resize', () => {
  isMobile.value = window.innerWidth <= 768
})

const form = ref({
  templateName: '',
  theme: '',
  targetType: route.query.target_type || '',
  content: ''
})

const id = route.query.id
if (id) {
  // getEmailTemplateDetail(id).then(res => {
  //   Object.assign(form.value, res.data)
  // })
}

const variableList = [
  { name: 'alumnus.alumni_id', desc: '校友ID' },
  { name: 'activity.name', desc: '活动名称' },
  { name: 'activity.detail', desc: '活动详情' },
  { name: 'alumnus.chinese_name', desc: '中文姓名' },
  { name: 'alumnus.email', desc: '邮箱' },
  { name: 'alumnus.contact_number', desc: '联系电话' },
  { name: 'alumnus.ycyw_schools_attended', desc: '就读学校' },
  { name: 'alumnus.first_name', desc: '英文名' },
  { name: 'alumnus.last_name', desc: '英文姓' }
]

const insertVariable = (name) => {
  const textarea = document.querySelector('textarea')
  if (textarea) {
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    const value = form.value.content
    form.value.content = value.slice(0, start) + `{{${name}}}` + value.slice(end)
    // 光标移动到插入后
    setTimeout(() => {
      textarea.selectionStart = textarea.selectionEnd = start + `{{${name}}}`.length
      textarea.focus()
    }, 0)
  } else {
    form.value.content += `{{${name}}}`
  }
}

const submit = async () => {
  if (id) {
    await updateEmailTemplate(id, form.value)
  } else {
    await addEmailTemplate(form.value)
  }
  router.back()
}

const goBack = () => {
  router.back()
}

const goBackManage = () => {
  router.push({ name: 'EmailTemplateManage' })
}

onMounted(() => {
  const permissions = JSON.parse(sessionStorage.getItem('admin_permissions') || '{}')
  emailTemplatePermission.value = permissions.email_template_permission || 0
  
  // 当email_template_permission为1时，强制设置为校友
  if (emailTemplatePermission.value === 1) {
    form.value.targetType = 'alumni'
  }
})
</script>

<style scoped>
.email-template-add {
  background: #f7f8fa;
  padding: 24px;
  min-height: 100vh;
}

.mobile-title {
  text-align: center;
  font-size: 20px;
  margin-top: 0;
}

.template-form {
  max-width: 800px;
  margin: 0 auto;
}

.variable-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.variable-tag {
  cursor: pointer;
  margin-bottom: 8px;
}

.form-buttons {
  margin-top: 20px;
}

.button-container {
  display: flex;
  gap: 16px;
}

.form-btn {
  min-width: 100px;
}

.back-btn {
  position: absolute;
  left: 24px;
  top: 24px;
  font-size: 18px;
  z-index: 10;
}

@media (max-width: 768px) {
  .email-template-add {
    padding: 16px;
  }
  
  .back-btn {
    position: static;
    margin-bottom: 16px;
  }
  
  .template-form {
    padding: 0;
  }
  
  .variable-list {
    gap: 6px;
  }
  
  .variable-tag {
    font-size: 12px;
    padding: 0 8px;
    height: 24px;
    line-height: 22px;
  }
  
  .button-container {
    flex-direction: column;
    width: 100%;
    gap: 10px;
  }
  
  .form-btn {
    width: 100%;
    margin-left: 0 !important;
    margin-right: 0 !important;
  }
  
  .el-form-item {
    margin-bottom: 16px;
  }
}

@media (max-width: 480px) {
  .variable-tag {
    font-size: 11px;
    padding: 0 6px;
    height: 22px;
    line-height: 20px;
  }
}
</style> 