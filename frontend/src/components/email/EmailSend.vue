<template>
  <div class="email-send">
    <el-button type="text" @click="goBack" class="back-btn" :style="isMobile ? 'position: static; margin-bottom: 16px;' : ''">
      <el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回
    </el-button>
    
    <el-steps :active="currentStep" finish-status="success" :direction="isMobile ? 'vertical' : 'horizontal'" class="email-steps">
      <el-step title="选择校友" />
      <el-step title="选择模板" />
      <el-step title="预览内容" />
      <el-step title="发送邮件" />
    </el-steps>

    <!-- 步骤1：选择校友 -->
    <div v-if="currentStep === 0" class="step-content">
      <h3>选择收件人</h3>
      <div class="search-section">
        <el-row :gutter="isMobile ? 0 : 10">
          <el-col :xs="24" :sm="12" :md="8" :lg="8" class="search-item">
            <el-input
              v-model="searchForm.name"
              placeholder="搜索校友姓名"
              class="search-input"
              @input="debounceSearch"
              clearable
            />
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" class="search-item">
            <el-input
              v-model="searchForm.email"
              placeholder="搜索邮箱"
              class="search-input"
              @input="debounceSearch"
              clearable
            />
          </el-col>
          <el-col :xs="24" :sm="24" :md="8" :lg="8" class="search-item clear-btn-col">
            <el-button @click="clearSearch" class="clear-btn">清空</el-button>
          </el-col>
        </el-row>
      </div>
      
      <div class="alumni-list">
        <el-table
          :data="alumniList"
          @selection-change="handleSelectionChange"
          style="width: 100%"
          :size="isMobile ? 'small' : 'default'"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="chineseName" label="中文姓名" />
          <el-table-column prop="email" label="邮箱" />
          <el-table-column prop="ycywSchoolsAttended" label="就读学校" :show-overflow-tooltip="true" />
          <el-table-column prop="yearLeft" label="毕业年份" :width="isMobile ? '' : '100'" />
        </el-table>
        
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            :layout="isMobile ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :small="isMobile"
          />
        </div>
      </div>
      
      <div class="selected-info">
        <p>已选择 <strong>{{ selectedAlumniIds.length }}</strong> 位校友</p>
      </div>
    </div>

    <!-- 步骤2：选择模板 -->
    <div v-if="currentStep === 1" class="step-content">
      <h3>选择邮件模板</h3>
      <el-form :model="templateForm" :label-width="isMobile ? '70px' : '100px'" :label-position="isMobile ? 'top' : 'right'">
        <el-form-item label="目标对象">
          <el-select v-model="templateForm.targetType" @change="loadTemplates" :disabled="emailTemplatePermission === 1" style="width: 100%;">
            <el-option label="校友" value="alumni" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮件模板">
          <el-select v-model="templateForm.templateId" @change="handleSelectTemplate" style="width: 100%;">
            <el-option
              v-for="template in templateList"
              :key="template.id"
              :label="template.templateName"
              :value="template.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="needActivity" label="选择活动">
          <el-select v-model="selectedActivityUuid" placeholder="请选择活动" style="width: 100%;">
            <el-option 
              v-for="act in activityList" 
              :key="act.uuid" 
              :label="(act.name || '无名称') + '（' + (act.activityDate || '无日期') + '）'" 
              :value="act.uuid" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="主题">
          <el-input v-model="editableTheme" placeholder="请输入邮件主题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input type="textarea" v-model="editableContent" :rows="isMobile ? 6 : 10" placeholder="请输入邮件内容，可插入变量" />
        </el-form-item>
        
      </el-form>
      
      
    </div>

    <!-- 步骤3：预览内容 -->
    <div v-if="currentStep === 2" class="step-content">
      <h3>预览邮件内容</h3>
      <div v-if="loading" class="loading">
        <el-loading :fullscreen="false" />
        <p>正在生成预览内容...</p>
      </div>
      
      <div v-else class="preview-content">
        <div class="preview-summary">
          <p>共选择 <strong>{{ selectedAlumniIds.length }}</strong> 位校友，以下是每位的个性化邮件内容：</p>
        </div>
        
        <div v-for="(preview, index) in emailPreviews" :key="index" class="preview-item">
          <div class="preview-header">
            <h4>第 {{ index + 1 }} 位校友</h4>
            <div class="alumni-info" :class="{'mobile-alumni-info': isMobile}">
              <p><strong>学号：</strong>{{ preview.alumniId }}</p>
              <p><strong>姓名：</strong>{{ preview.chineseName || preview.firstName + ' ' + preview.lastName }}</p>
              <p><strong>邮箱：</strong>{{ preview.email }}</p>
              <p v-if="preview.ycywSchoolsAttended"><strong>就读学校：</strong>{{ preview.ycywSchoolsAttended }}</p>
              <p v-if="preview.yearLeft"><strong>毕业年份：</strong>{{ preview.yearLeft }}</p>
            </div>
          </div>
          
          <div class="email-preview">
            <div class="email-header">
              <p><strong>收件人：</strong>{{ preview.email }}</p>
              <p><strong>主题：</strong>{{ preview.subject }}</p>
            </div>
            <div class="email-body">
              <div class="email-content">{{ preview.content }}</div>
            </div>
          </div>
        </div>
        
        <div class="preview-footer">
          <p><strong>注意：</strong>以上是变量替换后的个性化内容，每位校友将收到针对其个人信息的定制邮件。</p>
        </div>
      </div>
    </div>

    <!-- 步骤4：发送邮件 -->
    <div v-if="currentStep === 3" class="step-content">
      <h3>确认发送</h3>
      <div class="send-confirmation">
        <p>即将向 <strong>{{ selectedAlumniIds.length }}</strong> 位校友发送邮件</p>
        <p>邮件主题：{{ selectedTemplate?.theme }}</p>
        <el-button type="primary" @click="sendEmails" :loading="sending" class="send-btn">
          {{ sending ? '发送中...' : '确认发送' }}
        </el-button>
      </div>
    </div>

    <!-- 底部按钮 -->
    <div class="step-actions">
      <div class="action-row">
        <el-button 
          v-if="currentStep > 0" 
          @click="prevStep" 
          class="action-btn"
        >
          上一步
        </el-button>
        <el-button 
          v-if="currentStep < 3" 
          type="primary" 
          @click="nextStep"
          :disabled="!canProceed"
          class="action-btn"
        >
          下一步
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getEmailTemplates, getEmailVariablesDynamic, getActivityList, updateEmailTemplate } from '../../api/emailTemplate'
import { searchAlumni } from '../../api/alumniSearch'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

// 判断是否为移动设备
const isMobile = ref(window.innerWidth <= 768)

// 监听窗口大小变化
window.addEventListener('resize', () => {
  isMobile.value = window.innerWidth <= 768
})

// 步骤控制
const currentStep = ref(0)
const adminPermissionLevel = ref(0) // 管理员权限等级
const emailTemplatePermission = ref(0) // 邮件模版权限等级

// 校友搜索和选择
const searchForm = ref({
  name: '',
  email: '',
  schools: []
})
const alumniList = ref([])
const selectedAlumniIds = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 搜索防抖定时器
let searchTimer = null

// 防抖搜索方法
const debounceSearch = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    searchAlumniData()
  }, 500) // 500ms后执行搜索
}

// 监听搜索表单变化
watch([() => searchForm.value.name, () => searchForm.value.email], () => {
  debounceSearch()
})

// 模板选择
const templateForm = ref({
  targetType: 'alumni',
  templateId: ''
})
const templateList = ref([])
const selectedTemplate = ref(null)

// 邮件预览
const loading = ref(false)
const emailPreviews = ref([])

// 发送状态
const sending = ref(false)

// 活动选择
const activityList = ref([])
const selectedActivityUuid = ref('')

// 编辑模板
const editableTheme = ref('')
const editableContent = ref('')

// 计算属性
const canProceed = computed(() => {
  switch (currentStep.value) {
    case 0:
      return selectedAlumniIds.value.length > 0
    case 1:
      return selectedTemplate.value !== null
    case 2:
      return emailPreviews.value.length > 0
    default:
      return true
  }
})

const needActivity = computed(() => {
  // 只要模板内容包含activity.变量就需要活动选择
  return /{{\s*activity\./.test(selectedTemplate.value?.content || '')
})

// 方法
const searchAlumniData = async () => {
  try {
    const permissions = JSON.parse(localStorage.getItem('admin_permissions') || '{}')
    const params = {
      name: searchForm.value.name,
      email: searchForm.value.email,
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      permissionSchool: permissions.accessible_schools || '',
      alumniInfoManagementPermission: permissions.alumni_info_management_permission || 0
    }
    const res = await searchAlumni(params)
    if (res.data.code === 0) {
      alumniList.value = res.data.data || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.data.msg || '搜索校友失败')
      alumniList.value = []
      total.value = 0
    }
  } catch (error) {
    ElMessage.error('搜索校友失败')
    alumniList.value = []
    total.value = 0
  }
}

const clearSearch = () => {
  searchForm.value = { name: '', email: '', schools: [] }
  currentPage.value = 1
  searchAlumniData()
}

const handleSelectionChange = (selection) => {
  selectedAlumniIds.value = selection.map(item => item.alumniId)
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  searchAlumniData()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  searchAlumniData()
}

const loadTemplates = async () => {
  try {
    const res = await getEmailTemplates(templateForm.value.targetType)
    templateList.value = res.data
    templateForm.value.templateId = ''
    selectedTemplate.value = null
  } catch (error) {
    ElMessage.error('加载模板失败')
  }
}

const selectTemplate = () => {
  selectedTemplate.value = templateList.value.find(t => t.id === templateForm.value.templateId)
}

const loadActivityList = async () => {
  try {
    const res = await getActivityList()
    activityList.value = (res.data && res.data.data) ? res.data.data : []
  } catch (e) {
    activityList.value = []
  }
}

const handleSelectTemplate = () => {
  selectTemplate()
  if (selectedTemplate.value) {
    editableTheme.value = selectedTemplate.value.theme
    editableContent.value = selectedTemplate.value.content
  }
  if (needActivity.value) {
    loadActivityList()
  }
}

// 提取模板变量名
function extractTemplateFields(templateContent) {
  const matches = [...templateContent.matchAll(/{{(.*?)}}/g)];
  return [...new Set(matches.map(m => m[1].trim()))];
}

const generatePreviews = async () => {
  if (!selectedTemplate.value || selectedAlumniIds.value.length === 0) {
    return
  }
  if (needActivity.value && !selectedActivityUuid.value) {
    ElMessage.warning('请选择活动！')
    return
  }
  loading.value = true
  try {
    const fields = extractTemplateFields(selectedTemplate.value.content);
    const req = {
      alumniIds: selectedAlumniIds.value,
      fields,
      templateContent: selectedTemplate.value.content
    }
    if (needActivity.value) {
      req.activityUuid = selectedActivityUuid.value
    }
    const res = await getEmailVariablesDynamic(req)
    emailPreviews.value = res.data.map(item => {
      const alumniInfo = alumniList.value.find(a => a.alumniId === item.alumniId) || {}
      let content = selectedTemplate.value.content
      Object.entries(item.variables).forEach(([key, value]) => {
        content = content.replace(new RegExp(`{{${key}}}`, 'g'), value || '')
      })
      return {
        alumniId: item.alumniId,
        chineseName: alumniInfo.chineseName,
        firstName: alumniInfo.firstName,
        lastName: alumniInfo.lastName,
        email: alumniInfo.email,
        ycywSchoolsAttended: alumniInfo.ycywSchoolsAttended,
        yearLeft: alumniInfo.yearLeft,
        subject: selectedTemplate.value.theme,
        content: content
      }
    })
  } catch (error) {
    ElMessage.error('生成预览失败')
  } finally {
    loading.value = false
  }
}

const sendEmails = async () => {
  sending.value = true
  try {
    // 这里需要调用发送邮件的API
    await new Promise(resolve => setTimeout(resolve, 2000)) // 模拟发送
    ElMessage.success('邮件发送成功')
    if (route.query.from === 'review') {
      router.push('/alumni/review')
    } else {
      router.back()
    }
  } catch (error) {
    ElMessage.error('邮件发送失败')
  } finally {
    sending.value = false
  }
}

const nextStep = async () => {
  if (currentStep.value === 1) {
    // 步骤2"下一步"时自动保存模板
    if (selectedTemplate.value) {
      await updateEmailTemplate(selectedTemplate.value.id, {
        ...selectedTemplate.value,
        theme: editableTheme.value,
        content: editableContent.value
      })
      // 同步本地模板内容
      selectedTemplate.value.theme = editableTheme.value
      selectedTemplate.value.content = editableContent.value
    }
    await generatePreviews()
  }
  currentStep.value++
}

const prevStep = () => {
  currentStep.value--
}

const goBack = () => {
  router.back()
}

onMounted(async () => {
  const permissions = JSON.parse(localStorage.getItem('admin_permissions') || '{}')
  adminPermissionLevel.value = permissions.alumni_info_management_permission || 0
  emailTemplatePermission.value = permissions.email_template_permission || 0

  // 当email_template_permission为1时，强制设置为校友并禁用选择
  if (emailTemplatePermission.value === 1) {
    templateForm.value.targetType = 'alumni'
  }

  await searchAlumniData()
  await loadTemplates()

  // 自动选中alumniId并跳到选择模板
  const autoAlumniId = route.query.alumniId
  if (autoAlumniId) {
    const found = alumniList.value.find(a => a.alumniId === autoAlumniId)
    if (found) {
      selectedAlumniIds.value = [autoAlumniId]
      currentStep.value = 1 // 跳到选择模板
    }
  }
})
</script>

<style scoped>
.email-send {
  background: #f7f8fa;
  padding: 24px;
  min-height: 100vh;
}

.mobile-title {
  text-align: center;
  font-size: 20px;
  margin-top: 0;
}

.email-steps {
  margin-bottom: 30px;
}

.back-btn {
  position: absolute;
  left: 24px;
  top: 24px;
  font-size: 18px;
  z-index: 10;
}

.step-content {
  background: white;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-section {
  margin-bottom: 20px;
}

.search-item {
  margin-bottom: 10px;
}

.search-input {
  width: 100%;
}

.button-row {
  display: flex;
  gap: 10px;
  width: 100%;
}

.action-search-btn {
  flex: 1;
}

.alumni-list {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

.selected-info {
  background: #f0f9ff;
  padding: 12px;
  border-radius: 4px;
  border-left: 4px solid #1890ff;
}

.template-preview {
  margin-top: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 4px;
}

.template-content {
  white-space: pre-wrap;
  background: white;
  padding: 12px;
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  margin-top: 8px;
}

.loading {
  text-align: center;
  padding: 40px;
}

.preview-content {
  max-height: 600px;
  overflow-y: auto;
}

.preview-summary {
  background: #f0f9ff;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #1890ff;
}

.preview-item {
  margin-bottom: 24px;
  padding: 20px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.preview-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f3f4f6;
}

.preview-header h4 {
  margin: 0 0 12px 0;
  color: #1f2937;
  font-size: 18px;
}

.alumni-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 8px;
  font-size: 14px;
}

.mobile-alumni-info {
  grid-template-columns: 1fr;
}

.alumni-info p {
  margin: 0;
  color: #6b7280;
}

.alumni-info strong {
  color: #374151;
}

.email-preview {
  background: #f9fafb;
  border-radius: 6px;
  padding: 16px;
}

.email-header {
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e5e7eb;
}

.email-header p {
  margin: 4px 0;
  font-size: 14px;
  color: #6b7280;
}

.email-header strong {
  color: #374151;
}

.email-body {
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  padding: 16px;
}

.email-content {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #1f2937;
  font-size: 14px;
}

.preview-footer {
  background: #fef3c7;
  padding: 12px 16px;
  border-radius: 6px;
  margin-top: 20px;
  border-left: 4px solid #f59e0b;
}

.preview-footer p {
  margin: 0;
  color: #92400e;
  font-size: 14px;
}

.send-confirmation {
  text-align: center;
  padding: 40px;
}

.send-btn {
  min-width: 120px;
}

.step-actions {
  margin-top: 20px;
  text-align: center;
}

.action-row {
  display: inline-flex;
  gap: 16px;
  justify-content: center;
}

.action-btn {
  min-width: 120px;
}

.clear-btn-col {
  display: flex;
  justify-content: flex-start;
}

.clear-btn {
  width: auto;
  min-width: 120px;
}

@media (max-width: 768px) {
  .email-send {
    padding: 16px;
  }
  
  .step-content {
    padding: 16px;
    border-radius: 6px;
  }
  
  .button-row {
    flex-direction: column;
    gap: 8px;
  }
  
  .action-search-btn {
    width: 100%;
  }
  
  .preview-item {
    padding: 12px;
  }
  
  .email-preview {
    padding: 12px;
  }
  
  .email-body {
    padding: 12px;
  }
  
  .preview-header h4 {
    font-size: 16px;
  }
  
  .action-row {
    display: flex;
    gap: 10px;
    width: 100%;
    max-width: 400px;
    margin: 0 auto;
    justify-content: center;
  }
  
  .action-btn {
    min-width: 100px;
    flex: 1;
  }
  
  .clear-btn-col {
    justify-content: center;
  }
  
  .clear-btn {
    width: auto;
    max-width: 200px;
  }
}

@media (max-width: 480px) {
  .email-send {
    padding: 12px;
  }
  
  .step-content {
    padding: 12px;
  }
  
  .preview-item {
    padding: 10px;
  }
  
  .email-preview {
    padding: 10px;
  }
  
  .email-body {
    padding: 10px;
  }
  
  .email-content {
    font-size: 13px;
  }
  
  .action-row {
    max-width: 100%;
  }
  
  .action-btn {
    min-width: 80px;
    padding-left: 10px;
    padding-right: 10px;
  }
}
</style> 