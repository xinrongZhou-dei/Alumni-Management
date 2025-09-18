<template>
  <div class="email-template-manage">
    <el-button type="text" @click="goAdminHome" class="back-btn" :style="isMobile ? 'position: static; margin-bottom: 16px;' : ''">
      <el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回
    </el-button>
    <h2 :class="{'mobile-title': isMobile}">邮件模版管理</h2>
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="校友邮件模版" name="alumni"></el-tab-pane>
      <el-tab-pane v-if="emailTemplatePermission === 2" label="管理员邮件模版" name="admin"></el-tab-pane>
    </el-tabs>
    <div class="action-container">
      <el-row :gutter="isMobile ? 0 : 16">
        <el-col :xs="24" :sm="12" :md="6" :lg="4" class="action-col">
          <el-button type="primary" @click="goAdd" class="action-btn">添加模版</el-button>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6" :lg="4" class="action-col">
          <el-button type="success" @click="goSendEmail" class="action-btn">发送邮件</el-button>
        </el-col>
      </el-row>
    </div>
    <div class="template-card-list">
      <el-card v-for="item in templateList" :key="item.id" class="template-card">
        <div class="template-title">{{ item.templateName }}</div>
        <el-form :label-width="isMobile ? '70px' : '80px'" class="template-form" :label-position="isMobile ? 'top' : 'left'">
          <el-form-item label="邮件主题">
            <el-input v-model="item.theme" />
          </el-form-item>
          <el-form-item label="邮件内容">
            <el-input type="textarea" v-model="item.content" :rows="isMobile ? 8 : 14" />
          </el-form-item>
        </el-form>
        <div class="template-card-footer">
          <el-button type="primary" size="small" @click="saveTemplate(item)" class="footer-btn">保存模版</el-button>
          <el-button type="danger" size="small" @click="deleteTemplate(item)" class="footer-btn">删除模版</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getEmailTemplates, updateEmailTemplate, deleteEmailTemplate } from '../../api/emailTemplate'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const activeTab = ref('alumni')
const templateList = ref([])
const emailTemplatePermission = ref(0)

// 判断是否为移动设备
const isMobile = ref(window.innerWidth <= 768)

// 监听窗口大小变化
window.addEventListener('resize', () => {
  isMobile.value = window.innerWidth <= 768
})

const fetchTemplates = async () => {
  const res = await getEmailTemplates(activeTab.value)
  templateList.value = res.data
}

const goAdd = () => {
  router.push({ name: 'EmailTemplateAdd', query: { target_type: activeTab.value } })
}

const saveTemplate = async (item) => {
  try {
    await updateEmailTemplate(item.id, item)
    ElMessage({
      message: '保存成功',
      type: 'success',
      duration: 2000
    })
    fetchTemplates()
  } catch (error) {
    ElMessage.error('保存失败，请重试')
  }
}

const deleteTemplate = async (item) => {
  if (confirm('确定要删除该模版吗？')) {
    try {
      await deleteEmailTemplate(item.id)
      ElMessage({
        message: '删除成功',
        type: 'success',
        duration: 2000
      })
      fetchTemplates()
    } catch (error) {
      ElMessage.error('删除失败，请重试')
    }
  }
}

const onTabChange = () => {
  fetchTemplates()
}

const goAdminHome = () => {
  router.push('/')
}

const goSendEmail = () => {
  router.push({ name: 'EmailSend' })
}

// 监听activeTab变化，首次加载和切换都刷新
watch(activeTab, () => {
  fetchTemplates()
})

onMounted(() => {
  const permissions = JSON.parse(sessionStorage.getItem('admin_permissions') || '{}')
  emailTemplatePermission.value = permissions.email_template_permission || 0
  fetchTemplates()
})
</script>

<style scoped>
.email-template-manage {
  background: #f7f8fa;
  padding: 24px;
  min-height: 100vh;
}

.mobile-title {
  text-align: center;
  font-size: 20px;
  margin-top: 0;
}

.action-container {
  margin: 16px 0;
}

.action-col {
  margin-bottom: 8px;
}

.action-btn {
  width: 100%;
  min-width: 100px;
}

.template-card-list {
  display: flex;
  flex-direction: column;
  gap: 40px;
  margin-top: 32px;
}

.template-card {
  max-width: 1800px;
  min-width: auto;
  margin: 0 auto;
  border-radius: 16px;
  box-shadow: 0 4px 24px #00000014;
  padding: 72px 96px 56px 96px;
  background: #fff;
  width: 100%;
}

.template-title {
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 24px;
  color: #222;
}

.template-form {
  margin-bottom: 0;
}

.template-card-footer {
  display: flex;
  justify-content: flex-end;
  gap: 24px;
  margin-top: 24px;
}

.footer-btn {
  min-width: 90px;
}

.el-form-item {
  margin-bottom: 28px;
}

.el-input,
.el-textarea {
  font-size: 16px;
}

.back-btn {
  position: absolute;
  left: 24px;
  top: 24px;
  font-size: 18px;
  z-index: 10;
}

@media (max-width: 768px) {
  .email-template-manage {
    padding: 16px;
  }
  
  .template-card {
    padding: 24px 16px;
    border-radius: 8px;
  }
  
  .template-title {
    font-size: 20px;
    text-align: center;
    margin-bottom: 16px;
  }
  
  .template-card-footer {
    flex-direction: row;
    justify-content: center;
    gap: 16px;
  }
  
  .template-card-footer .el-button {
    width: auto;
    flex: 1;
  }
  
  .action-container {
    flex-direction: column;
    gap: 8px;
    width: 100%;
  }
  
  .action-col {
    margin-bottom: 10px;
  }
  
  .action-btn {
    width: 100%;
  }
  
  .el-form-item {
    margin-bottom: 16px;
  }
  
  .el-input,
  .el-textarea {
    font-size: 14px;
  }
}
</style> 