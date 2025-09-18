<template>
  <el-card class="activity-edit-root" shadow="hover">
    <el-button type="text" @click="goBack" class="back-btn">
      <el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回
    </el-button>
    
    <div class="activity-edit-header">
      <div class="activity-edit-title">{{ isEdit ? '编辑活动' : '创建活动' }}</div>
    </div>

    <el-form :model="form" :rules="rules" ref="formRef" 
             :label-position="isMobile ? 'top' : 'right'"
             label-width="100px" 
             size="large"
             class="activity-form">
      <div class="form-section">
        <div class="section-title">基本信息</div>
        <el-row :gutter="20">
          <el-col :span="24" :sm="12">
            <el-form-item label="活动名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入活动名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24" :sm="12">
            <el-form-item label="活动日期" prop="activityDate">
              <el-date-picker
                v-model="form.activityDate"
                type="date"
                placeholder="请选择活动日期"
                class="full-width"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24" :sm="12">
            <el-form-item label="报名开始" prop="signupStart">
              <el-date-picker
                v-model="form.signupStart"
                type="datetime"
                placeholder="请选择报名开始时间"
                class="full-width"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24" :sm="12">
            <el-form-item label="报名结束" prop="signupEnd">
              <el-date-picker
                v-model="form.signupEnd"
                type="datetime"
                placeholder="请选择报名结束时间"
                class="full-width"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24" :sm="12">
            <el-form-item label="报名人数" prop="signupTotal">
              <el-input-number v-model="form.signupTotal" :min="1" class="full-width" />
            </el-form-item>
          </el-col>
          <el-col :span="24" :sm="12">
            <el-form-item label="活动地点" prop="venue">
              <el-input v-model="form.venue" placeholder="请输入活动地点" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="活动链接" prop="link">
              <el-input v-model="form.link" placeholder="请输入活动链接" />
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <div class="form-section">
        <div class="section-title">活动封面</div>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="上传封面" prop="coverUrl">
              <el-upload
                class="upload-demo"
                action="#"
                :auto-upload="false"
                :show-file-list="false"
                :on-change="onCoverChange"
              >
                <el-button type="primary">
                  <el-icon style="vertical-align: middle; margin-right: 4px;"><Upload /></el-icon>
                  选择文件
                </el-button>
                <template #tip>
                  <div class="el-upload__tip">请上传活动封面图片</div>
                </template>
              </el-upload>
              <el-image
                v-if="coverUrl"
                :src="coverUrl"
                fit="contain"
                class="cover-preview"
                :preview-src-list="[coverUrl]"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <div class="form-section">
        <div class="section-title">活动详情</div>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="活动详情" prop="detail">
              <el-input
                v-model="form.detail"
                type="textarea"
                :rows="6"
                placeholder="请输入活动详情"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <div class="form-section">
        <div class="section-title">报名设置</div>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="必填字段">
              <div class="required-fields-container">
                <el-switch
                  v-model="form.requiredFields.wechat_id_required"
                  active-text="微信ID"
                />
                <el-switch
                  v-model="form.requiredFields.address_required"
                  active-text="通讯地址"
                />
                <el-switch
                  v-model="form.requiredFields.current_location_required"
                  active-text="当前位置"
                />
                <el-switch
                  v-model="form.requiredFields.companions_required"
                  active-text="携带人数"
                />
                <el-switch
                  v-model="form.requiredFields.payment_proof_required"
                  active-text="支付凭证"
                />
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <div class="form-actions">
        <el-button @click="goBack" class="action-btn">取消</el-button>
        <el-button type="primary" @click="onSave" class="action-btn">保存</el-button>
      </div>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, onMounted, watch, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Upload } from '@element-plus/icons-vue'
import { createWS } from '@/utils/ws'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const form = ref({
  uuid: '',
  name: '',
  activityDate: '',
  signupStart: '',
  signupEnd: '',
  detail: '',
  coverUrl: '',
  link: '',
  qrcodeUrl: '',
  venue: '',
  signupStatus: '',
  signupTotal: 0,
  requiredFields: {
    wechat_id_required: false,
    address_required: false,
    current_location_required: false,
    companions_required: false,
    payment_proof_required: false
  }
})
const signupRange = ref([])
const coverUrl = ref('')

// 判断是否为移动设备
const isMobile = ref(window.innerWidth <= 768)

// 监听窗口大小变化
window.addEventListener('resize', () => {
  isMobile.value = window.innerWidth <= 768
})

const fetchActivity = async () => {
  try {
    const res = await axios.get(`/api/activity/detail/${route.params.id}`)
    if (res.data && res.data.code === 0) {
      const activityData = res.data.data
      // 初始化表单数据
      form.value.uuid = activityData.uuid || ''
      form.value.name = activityData.name || ''
      form.value.detail = activityData.detail || ''
      form.value.activityDate = activityData.activityDate || ''
      form.value.signupStart = activityData.signupStart || ''
      form.value.signupEnd = activityData.signupEnd || ''
      form.value.venue = activityData.venue || ''
      form.value.signupTotal = activityData.signupTotal || 0
      form.value.signupStatus = activityData.signupStatus || ''
      form.value.coverUrl = activityData.coverUrl || ''
      coverUrl.value = activityData.coverUrl || '' // 修复：同步更新预览
      form.value.link = activityData.link || ''
      form.value.qrcodeUrl = activityData.qrcodeUrl || ''
      // 初始化必填字段配置
      form.value.requiredFields = {
        wechat_id_required: activityData.wechat_id_required || false,
        address_required: activityData.address_required || false,
        current_location_required: activityData.current_location_required || false,
        companions_required: activityData.companions_required || false,
        payment_proof_required: activityData.payment_proof_required || false
      }
    }
  } catch (error) {
    console.error('获取活动详情失败:', error)
    ElMessage.error('获取活动详情失败')
  }
}

const beforeUpload = (file) => {
  // 这里只做本地预览，实际应上传到服务器
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  return true
}
const handleCoverChange = (file) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    form.value.coverUrl = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const statusTagType = (status) => {
  if (status === '进行中') return 'success'
  if (status === '未开始') return 'info'
  if (status === '即将结束') return 'warning'
  return 'danger'
}

const onSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // 日期格式化函数，确保传递给后端的是'YYYY-MM-DD'字符串
    const formatDate = (val) => {
      if (!val) return null
      if (typeof val === 'string') return val.slice(0, 10)
      if (val instanceof Date) return val.toISOString().slice(0, 10)
      return null
    }
    // 构建保存的数据
    const saveData = {
      uuid: form.value.uuid,
      name: form.value.name,
      activityDate: formatDate(form.value.activityDate),
      signupStart: formatDate(form.value.signupStart),
      signupEnd: formatDate(form.value.signupEnd),
      detail: form.value.detail,
      coverUrl: form.value.coverUrl,
      link: form.value.link,
      qrcodeUrl: form.value.qrcodeUrl,
      venue: form.value.venue,
      signupTotal: form.value.signupTotal,
      signupStatus: form.value.signupStatus,
      wechatIdRequired: form.value.requiredFields.wechat_id_required,
      addressRequired: form.value.requiredFields.address_required,
      currentLocationRequired: form.value.requiredFields.current_location_required,
      companionsRequired: form.value.requiredFields.companions_required,
      paymentProofRequired: form.value.requiredFields.payment_proof_required
    }
    const res = await axios.put(`/api/activity/edit/${form.value.uuid}`, saveData)
    if (res.data && res.data.code === 0) {
      ElMessage.success('保存成功')
      router.push('/admin/activity-manage')
    } else {
      ElMessage.error(res.data.msg || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}
const onCancel = () => {
  router.push('/admin/activity-manage')
}

const goBack = () => {
  router.push('/admin/activity-manage')
}

// 添加时间格式化函数
const formatDateTime = (date) => {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  const s = String(d.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}:${s}`
}

// 封面图片上传到OSS
const onCoverChange = async (file) => {
  const realFile = file.raw || file
  const formData = new FormData()
  formData.append('file', realFile)
  try {
    const res = await axios.post('/api/upload/oss', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    if (res.data && res.data.code === 0) {
      form.value.coverUrl = res.data.url
      coverUrl.value = res.data.url
      ElMessage.success('封面上传成功')
    } else {
      ElMessage.error(res.data.msg || '封面上传失败')
    }
  } catch (err) {
    ElMessage.error('封面上传失败')
  }
}

let ws = null
function initWebSocket() {
  ws = createWS({
    onMessage: (event) => {
      const msg = event.data;
      if (msg === "activity changed") {
        fetchActivity();
      }
    },
    onClose: () => {
      // 可选：可加提示或其他处理
    }
  })
}
onBeforeUnmount(() => {
  if (ws && ws._manualClose) ws._manualClose();
})

onMounted(() => {
  coverUrl.value = form.value.coverUrl
  fetchActivity()
  initWebSocket()
})
</script>

<style scoped>
.activity-edit-root {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.back-btn {
  position: absolute;
  left: 24px;
  top: 24px;
  font-size: 18px;
}

.activity-edit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-top: 40px;
}

.activity-edit-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.form-section {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
  padding-left: 12px;
  border-left: 4px solid #409eff;
}

.full-width {
  width: 100%;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

.action-btn {
  min-width: 120px;
  height: 40px;
}

.activity-form :deep(.el-form-item__label) {
  font-weight: 500;
}

.activity-form :deep(.el-input),
.activity-form :deep(.el-select),
.activity-form :deep(.el-date-editor),
.activity-form :deep(.el-input-number) {
  width: 100%;
}

.activity-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.activity-form :deep(.el-upload) {
  width: 100%;
}

.activity-form :deep(.el-upload-dragger) {
  width: 100%;
  height: 120px;
}

.activity-form :deep(.el-switch) {
  margin-right: 16px;
  margin-bottom: 12px;
}

.activity-form :deep(.el-switch__label) {
  color: #606266;
}

.activity-form :deep(.el-switch__label.is-active) {
  color: #409eff;
}

.required-fields-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.cover-preview {
  margin-top: 16px;
  max-width: 300px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

@media (max-width: 768px) {
  .activity-edit-root {
    max-width: 100%;
    margin: 0;
    padding: 12px;
    border-radius: 0;
    box-shadow: none;
  }
  
  .back-btn {
    position: static;
    margin-bottom: 8px;
    font-size: 16px;
  }
  
  .activity-edit-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    margin-bottom: 16px;
    padding-top: 0;
  }
  
  .activity-edit-title {
    font-size: 1.2rem;
  }
  
  .form-section {
    padding: 16px 8px;
    margin-bottom: 16px;
    box-shadow: none;
    border-radius: 0;
  }
  
  .section-title {
    font-size: 15px;
    margin-bottom: 12px;
    padding-left: 8px;
    border-left-width: 3px;
  }
  
  .el-row {
    margin-left: 0 !important;
    margin-right: 0 !important;
  }
  
  .el-col {
    padding-left: 0 !important;
    padding-right: 0 !important;
  }
  
  .activity-form :deep(.el-form-item) {
    margin-bottom: 16px;
  }
  
  .activity-form :deep(.el-form-item__label) {
    padding-bottom: 4px;
    font-size: 14px;
  }
  
  .activity-form :deep(.el-input__wrapper),
  .activity-form :deep(.el-select .el-input__wrapper),
  .activity-form :deep(.el-date-editor.el-input__wrapper) {
    background-color: #f8fafc;
    border-radius: 6px;
  }
  
  .form-actions {
    flex-direction: column;
    gap: 8px;
    margin-top: 16px;
    padding-top: 12px;
  }
  
  .form-actions .el-button {
    width: 100%;
    margin-left: 0 !important;
    margin-right: 0 !important;
  }
  
  .action-btn {
    height: 36px;
  }
  
  .required-fields-container {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .cover-preview {
    max-width: 100%;
  }
}
</style> 