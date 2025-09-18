<template>
  <div class="page-root">
    <el-button type="text" @click="goBack" class="back-btn">
      <el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回
    </el-button>
    <el-card class="activity-edit-root" shadow="hover">
      <div class="activity-edit-header">
        <div class="activity-edit-title">添加新活动</div>
      </div>
      <el-divider />
      
      <el-form :model="form" :rules="rules" ref="formRef" 
               :label-position="isMobile ? 'top' : 'right'"
               label-width="120px"
               class="activity-form">
        <!-- 基本信息 -->
        <div class="form-section">
          <div class="section-title">基本信息</div>
          <el-row :gutter="20">
            <el-col :span="24" :sm="24" :md="12">
              <el-form-item label="活动名称" prop="name">
                <el-input v-model="form.name" placeholder="请输入活动名称" />
              </el-form-item>
            </el-col>
            <el-col :span="24" :sm="24" :md="12">
              <el-form-item label="活动日期" prop="activityDate">
                <el-date-picker
                  v-model="form.activityDate"
                  type="date"
                  placeholder="选择活动日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  class="full-width"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="报名时间" required>
            <el-row :gutter="20">
              <el-col :span="24" :sm="11">
                <el-form-item prop="signupStart" class="no-margin">
                  <el-date-picker
                    v-model="form.signupStart"
                    type="date"
                    placeholder="开始日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    class="full-width"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="24" :sm="2" class="text-center date-separator">
                <span class="text-gray-500">至</span>
              </el-col>
              <el-col :span="24" :sm="11">
                <el-form-item prop="signupEnd" class="no-margin">
                  <el-date-picker
                    v-model="form.signupEnd"
                    type="date"
                    placeholder="结束日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    class="full-width"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form-item>
          
          <el-row :gutter="20">
            <el-col :span="24" :sm="24" :md="12">
              <el-form-item label="报名人数上限" prop="signupTotal">
                <el-input-number v-model="form.signupTotal" :min="1" :max="1000" class="full-width" />
              </el-form-item>
            </el-col>
            <el-col :span="24" :sm="24" :md="12">
              <el-form-item label="活动地点" prop="venue">
                <el-input v-model="form.venue" placeholder="请输入活动地点" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        
        <el-divider />
        
        <!-- 活动详情 -->
        <div class="form-section">
          <div class="section-title">活动详情</div>
          <el-form-item label="活动详情" prop="detail">
            <el-input
              v-model="form.detail"
              type="textarea"
              :rows="6"
              placeholder="请输入活动详情"
            />
          </el-form-item>
        </div>
        
        <el-divider />
        
        <!-- 报名表配置 -->
        <div class="form-section">
          <div class="section-title">报名表配置</div>
          <el-form-item label="必填字段">
            <div class="checkbox-container">
              <el-checkbox-group v-model="form.requiredFields">
                <el-checkbox label="wechat_id_required">微信号</el-checkbox>
                <el-checkbox label="address_required">通讯地址</el-checkbox>
                <el-checkbox label="current_location_required">当前位置</el-checkbox>
                <el-checkbox label="companions_required">携带人数</el-checkbox>
                <el-checkbox label="payment_proof_required">支付凭证</el-checkbox>
              </el-checkbox-group>
            </div>
          </el-form-item>
        </div>
        
        <el-divider />
        
        <!-- 活动封面 -->
        <div class="form-section">
          <div class="section-title">活动封面</div>
          <el-form-item label="封面图片" prop="coverUrl">
            <div class="cover-upload-container">
              <input type="file" @change="onCoverChange" accept="image/*" class="file-input" />
              <el-button type="primary" class="upload-button">
                <el-icon style="vertical-align: middle; margin-right: 4px;"><Upload /></el-icon>
                选择文件
              </el-button>
              <div class="cover-preview-container" v-if="coverUrl">
                <img :src="coverUrl" class="cover-preview" />
              </div>
            </div>
          </el-form-item>
        </div>
        
        <el-divider />
        
        <div class="form-actions">
          <el-button type="primary" @click="submitForm" class="action-btn">保存</el-button>
          <el-button @click="goBack" class="action-btn">取消</el-button>
        </div>
      </el-form>

      <!-- 活动链接和二维码展示 -->
      <div v-if="activityLink" class="qrcode-section">
        <div class="qrcode-title">活动链接：</div>
        <el-input v-model="activityLink" readonly class="qrcode-link" />
        <div class="qrcode-title">活动二维码：</div>
        <img v-if="qrCodeUrl" :src="qrCodeUrl" alt="活动二维码" class="qrcode-image" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus, Upload } from '@element-plus/icons-vue'
import axios from 'axios'
import { v4 as uuidv4 } from 'uuid'
import QRCode from 'qrcode'
import imageCompression from 'browser-image-compression'

// 判断是否为移动设备
const isMobile = ref(window.innerWidth <= 768)

// 监听窗口大小变化
window.addEventListener('resize', () => {
  isMobile.value = window.innerWidth <= 768
})

const router = useRouter()
const formRef = ref(null)

// 表单数据
const form = ref({
  name: '',
  activityDate: '',
  signupStart: '',
  signupEnd: '',
  signupTotal: 100,
  venue: '',
  detail: '',
  requiredFields: [],
  coverUrl: '',
  link: '', // 自动生成
  qrcodeUrl: '' // 自动生成
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  activityDate: [{ required: true, message: '请选择活动日期', trigger: 'change' }],
  signupStart: [{ required: true, message: '请选择报名开始日期', trigger: 'change' }],
  signupEnd: [{ required: true, message: '请选择报名结束日期', trigger: 'change' }],
  signupTotal: [{ required: true, message: '请输入报名人数上限', trigger: 'change' }],
  venue: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入活动详情', trigger: 'blur' }],
  coverUrl: [{ required: true, message: '请上传活动封面', trigger: 'change' }]
}

// 返回上一页
const goBack = () => {
  router.back()
}

const coverUrl = ref('')

// 处理封面图片选择
const onCoverChange = async (e) => {
  const file = e.target.files[0]
  if (file) {
    // 直接上传原图到OSS
    const formData = new FormData()
    formData.append('file', file)
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
}

// 活动链接和二维码
const activityLink = ref('')
const qrCodeUrl = ref('')

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    // 处理必填字段数组转布尔对象
    const requiredFieldsObj = {
      wechatIdRequired: form.value.requiredFields.includes('wechat_id_required'),
      addressRequired: form.value.requiredFields.includes('address_required'),
      currentLocationRequired: form.value.requiredFields.includes('current_location_required'),
      companionsRequired: form.value.requiredFields.includes('companions_required'),
      paymentProofRequired: form.value.requiredFields.includes('payment_proof_required')
    }
    // 提交表单到后端，不传signupActual
    const res = await axios.post('/api/activity/add', {
      ...form.value,
      ...requiredFieldsObj,
      coverUrl: form.value.coverUrl // 只传递文件名
    })
    if (res.data && res.data.code === 0) {
      ElMessage.success('添加成功')
      const uuid = res.data.data.uuid
      router.push(`/admin/activity-detail/${uuid}`)
    } else {
      ElMessage.error(res.data.msg || '添加失败')
    }
  } catch (error) {
    ElMessage.error('表单验证失败')
  }
}


</script>

<style scoped>
.page-root {
  position: relative;
  min-height: 100vh;
  background: #f5f7fa;
  padding-top: 0;
}

.activity-edit-root {
  max-width: 1200px;
  margin: 40px auto 0 auto;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: relative;
}

.back-btn {
  position: absolute;
  left: 40px;
  top: 40px;
  font-size: 18px;
  z-index: 1000;
  color: #409eff;
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 0 8px;
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

.text-center {
  text-align: center;
  line-height: 32px;
}

.full-width {
  width: 100%;
}

.no-margin {
  margin-bottom: 0 !important;
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

.cover-upload-container {
  position: relative;
}

.file-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
  z-index: 1;
}

.upload-button {
  position: relative;
  z-index: 0;
}

.cover-preview-container {
  margin-top: 16px;
}

.cover-preview {
  max-width: 300px;
  max-height: 200px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.checkbox-container {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.qrcode-section {
  margin-top: 32px;
  padding: 16px;
  border-top: 1px solid #ebeef5;
}

.qrcode-title {
  margin-bottom: 8px;
  font-weight: bold;
}

.qrcode-link {
  max-width: 400px;
  margin-bottom: 16px;
}

.qrcode-image {
  width: 160px;
  height: 160px;
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

@media (max-width: 768px) {
  .page-root {
    padding: 0;
  }
  
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
  
  .date-separator {
    display: flex;
    justify-content: center;
    align-items: center;
    margin: 8px 0;
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
  
  .checkbox-container {
    flex-direction: column;
    gap: 8px;
  }
  
  .qrcode-section {
    padding: 12px 0;
  }
  
  .qrcode-link {
    max-width: 100%;
  }
}
</style> 