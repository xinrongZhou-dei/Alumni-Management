<template>
  <div class="visit-record-form">
    <el-button type="text" @click="handleBack" style="position: absolute; left: 24px; top: 24px; font-size: 18px;">
      <el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回
    </el-button>

    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span>预约参观校园</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="visit-form"
        size="large"
        :disabled="isEditMode && currentRecord && currentRecord.status !== 'PENDING'"
      >
        <!-- 校友信息展示 -->
        <div class="form-section">
          <div class="section-title">校友信息</div>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="校友姓名">
                <el-input v-model="alumniInfo.name" disabled class="disabled-input" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="学号">
                <el-input v-model="alumniInfo.studentId" disabled class="disabled-input" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="当前所在地">
                <el-input v-model="alumniInfo.currentLocation" disabled class="disabled-input" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系方式">
                <el-input v-model="alumniInfo.phone" disabled class="disabled-input" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div class="form-section">
          <div class="section-title">预约信息</div>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="预约校区" prop="campus">
                <el-select v-model="form.campus" placeholder="请选择校区" class="full-width" :disabled="isEditMode && currentRecord && currentRecord.status !== 'PENDING'">
                  <el-option
                    v-for="item in campusOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="预约日期" prop="date">
                <el-date-picker
                  v-model="form.date"
                  type="date"
                  placeholder="请选择日期"
                  :disabled-date="disabledDate"
                  value-format="YYYY-MM-DD"
                  class="full-width"
                  :disabled="isEditMode && currentRecord && currentRecord.status !== 'PENDING'"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="预约时段" prop="timeSlot">
                <el-select v-model="form.timeSlot" placeholder="请选择时段" class="full-width" :disabled="isEditMode && currentRecord && currentRecord.status !== 'PENDING'">
                  <el-option
                    v-for="item in timeSlotOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="备注信息" prop="remarks">
            <el-input
              v-model="form.remarks"
              type="textarea"
              :rows="4"
              placeholder="请输入备注信息"
              class="full-width"
              :disabled="isEditMode && currentRecord && currentRecord.status !== 'PENDING'"
            />
          </el-form-item>
        </div>

        <div class="form-actions">
          <el-button type="primary" @click="submitForm" size="large" v-if="!isEditMode || (currentRecord && currentRecord.status === 'PENDING')">
            <el-icon style="vertical-align: middle; margin-right: 4px;"><Check /></el-icon>
            提交预约
          </el-button>
          <el-button @click="resetForm" size="large" v-if="!isEditMode || (currentRecord && currentRecord.status === 'PENDING')">
            <el-icon style="vertical-align: middle; margin-right: 4px;"><Refresh /></el-icon>
            重置
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { ArrowLeft, Check, Refresh } from '@element-plus/icons-vue'
import { createWS } from '@/utils/ws'

const route = useRoute()
const router = useRouter()
const userInfo = ref(JSON.parse(sessionStorage.getItem('user_info') || '{}'))
const alumniInfo = ref({
  name: '',
  studentId: '',
  currentLocation: '',
  phone: ''
})

const form = ref({
  campus: '',
  date: '',
  timeSlot: '',
  remarks: ''
})

// 新增：是否是编辑模式
const isEditMode = ref(false)
// 新增：当前预约记录
const currentRecord = ref(null)
// 新增：是否从预约记录列表进入
const isFromRecordList = ref(false)

const rules = {
  campus: [{ required: true, message: '请选择校区', trigger: 'change' }],
  date: [{ required: true, message: '请选择日期', trigger: 'change' }],
  timeSlot: [{ required: true, message: '请选择时间段', trigger: 'change' }],
  remarks: [{ required: true, message: '请输入备注信息', trigger: 'blur' }]
}

const campusOptions = [
  { value: 'YCIS_HK', label: 'YCIS Hong Kong - 耀中国际学校（香港）' },
  { value: 'YCIS_BJ', label: 'YCIS Beijing - 耀中国际学校（北京）' },
  { value: 'YCIS_CQ', label: 'YCIS Chongqing - 耀中国际学校（重庆）' },
  { value: 'YCIS_QD', label: 'YCIS Qingdao - 耀中国际学校（青岛）' },
  { value: 'YCIS_SH', label: 'YCIS Shanghai - 耀中国际学校（上海）' },
  { value: 'YWIES_BJ', label: 'YWIES Beijing - 耀华国际教育学校（北京）' },
  { value: 'YWIES_GZ', label: 'YWIES Guangzhou - 耀华国际教育学校（广州）' },
  { value: 'YWIES_SH_GB', label: 'YWIES Shanghai Gubei - 耀华国际教育学校（上海古北）' },
  { value: 'YWIES_SH_LG', label: 'YWIES Shanghai Lingang - 耀华国际教育学校（上海临港）' },
  { value: 'YWIES_TX', label: 'YWIES Tongxiang - 耀华国际教育学校（桐乡）' },
  { value: 'YWIES_YT', label: 'YWIES Yantai - 耀华国际教育学校（烟台）' }
]

const timeSlotOptions = [
  { value: 'MORNING', label: '上午 (9:00-11:30)' },
  { value: 'AFTERNOON', label: '下午 (13:30-16:00)' }
]

const disabledDate = (time) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const tomorrow = new Date(today)
  tomorrow.setDate(tomorrow.getDate() + 1)
  const sevenDaysLater = new Date(today)
  sevenDaysLater.setDate(sevenDaysLater.getDate() + 7)
  return time.getTime() < tomorrow.getTime() || time.getTime() > sevenDaysLater.getTime()
}

const handleBack = () => {
  if (isFromRecordList.value) {
    router.push('/alumni/visit-records')
  } else {
    router.push('/')
  }
}

const getAlumniInfo = async () => {
  try {
    if (!userInfo.value.alumniId) {
      ElMessage.error('未获取到用户信息，请重新登录')
      router.push('/login')
      return
    }
    const res = await axios.get('/api/user/info', {
      params: {
        alumniId: userInfo.value.alumniId
      }
    })
    if (res.data.code === 0) {
      alumniInfo.value = {
        name: res.data.data.chineseName,
        studentId: userInfo.value.alumniId,
        currentLocation: res.data.data.currentLocation,
        phone: res.data.data.contactNumber
      }
    } else {
      ElMessage.error(res.data.msg || '获取校友信息失败')
    }
  } catch (error) {
    console.error('获取校友信息失败:', error)
    ElMessage.error('获取校友信息失败')
  }
}

const initFormData = () => {
  // 从 sessionStorage 获取预约记录
  const recordStr = sessionStorage.getItem('visit_record')
  if (recordStr) {
    currentRecord.value = JSON.parse(recordStr)
    isEditMode.value = true
    isFromRecordList.value = true
    
    // 填充表单数据
    form.value = {
      campus: currentRecord.value.visitSchool,
      date: currentRecord.value.visitDay,
      timeSlot: currentRecord.value.visitTime,
      remarks: currentRecord.value.remark
    }
    
    // 清除 sessionStorage
    sessionStorage.removeItem('visit_record')
  }
}

const submitForm = async () => {
  try {
    // 从token中获取alumni_id，确保与后端一致
    const token = sessionStorage.getItem('alumni_token')
    let tokenAlumniId = userInfo.value.alumniId // 默认值
    
    if (token) {
      try {
        // 解析JWT token获取alumni_id
        const tokenPayload = JSON.parse(atob(token.split('.')[1]))
        if (tokenPayload.alumni_id) {
          tokenAlumniId = tokenPayload.alumni_id
        }
      } catch (e) {
        console.warn('解析token失败，使用默认alumniId:', e)
      }
    }
    
    const data = {
      alumniId: tokenAlumniId,
      chineseName: alumniInfo.value.name,
      currentLocation: alumniInfo.value.currentLocation,
      contactNumber: alumniInfo.value.phone,
      visitSchool: form.value.campus,
      visitDay: form.value.date,
      visitTime: form.value.timeSlot,
      remark: form.value.remarks
    }
    
    console.log('提交的数据:', data)

    let res
    if (isEditMode.value) {
      // 更新预约记录
      res = await axios.put(`/api/visitrecord/alumni/visit-records/${currentRecord.value.visitUUID}`, data)
    } else {
      // 创建新预约记录
      res = await axios.post('/api/visitrecord/alumni/visit-records', data)
    }

    if (res.data.code === 0) {
      ElMessage.success(isEditMode.value ? '更新成功' : '预约成功')
      router.push('/alumni/visit-records')
    } else {
      ElMessage.error(res.data.msg || (isEditMode.value ? '更新失败' : '预约失败'))
    }
  } catch (error) {
    console.error(isEditMode.value ? '更新失败:' : '预约失败:', error)
    ElMessage.error(isEditMode.value ? '更新失败' : '预约失败')
  }
}

const resetForm = () => {
  if (isEditMode.value && currentRecord.value) {
    // 如果是编辑模式，重置为原始数据
    form.value = {
      campus: currentRecord.value.visitSchool,
      date: currentRecord.value.visitDay,
      timeSlot: currentRecord.value.visitTime,
      remarks: currentRecord.value.remark
    }
  } else {
    // 如果是新建模式，清空表单
    form.value = {
      campus: '',
      date: '',
      timeSlot: '',
      remarks: ''
    }
  }
}

// WebSocket连接
const ws = ref(null)

// 处理WebSocket消息
const handleWebSocketMessage = (event) => {
  const data = JSON.parse(event.data)
  if (data.type === 'VISIT_RECORD_UPDATED' && isEditMode.value && data.payload.visitUUID === currentRecord.value?.visitUUID) {
    // 如果当前正在编辑的记录被更新，则刷新表单数据
    currentRecord.value = data.payload
    form.value = {
      campus: data.payload.visitSchool,
      date: data.payload.visitDay,
      timeSlot: data.payload.visitTime,
      remarks: data.payload.remark
    }
  }
}

function initWebSocket() {
  ws.value = createWS({
    onMessage: handleWebSocketMessage,
    onClose: () => {
      // 可选：可加提示或其他处理
    }
  })
}

onMounted(() => {
  getAlumniInfo()
  initFormData()
  initWebSocket()
})

onUnmounted(() => {
  if (ws.value && ws.value._manualClose) ws.value._manualClose();
})
</script>

<style scoped>
.visit-record-form {
  padding: 24px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.form-card {
  max-width: 900px;
  margin: 60px auto 0;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.visit-form {
  padding: 20px;
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

.disabled-input :deep(.el-input__wrapper) {
  background-color: #f5f7fa;
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

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

:deep(.el-input__wrapper:hover),
:deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}

@media (max-width: 768px) {
  .visit-record-form {
    padding: 8px;
  }
  .form-card {
    margin: 24px 0 0;
    max-width: 100%;
    box-shadow: none;
    border-radius: 0;
  }
  .visit-form {
    padding: 8px;
  }
  .form-section {
    padding: 8px;
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
    flex-direction: column !important;
    margin-left: 0 !important;
    margin-right: 0 !important;
  }
  .el-col {
    width: 100% !important;
    max-width: 100% !important;
    padding-left: 0 !important;
    padding-right: 0 !important;
    margin-bottom: 8px;
  }
  .form-actions {
    flex-direction: column;
    gap: 8px;
    margin-top: 16px;
    padding-top: 12px;
    border-top: none;
    align-items: stretch;
  }
  .form-actions .el-button {
    width: 100% !important;
    margin: 0 !important;
    box-sizing: border-box;
  }
  .el-button[style*='position: absolute'] {
    position: static !important;
    margin-bottom: 8px;
    font-size: 16px;
  }
  :deep(.el-form-item__label) {
    font-size: 14px;
    min-width: 0;
    width: auto;
    display: block;
    text-align: left !important;
    padding-left: 2px;
  }
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }
}
</style> 