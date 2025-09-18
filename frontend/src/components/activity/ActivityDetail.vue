<template>
  <el-card class="activity-detail-root" shadow="hover">
    <el-button type="text" @click="goBack" class="back-btn"><el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回</el-button>
    <div class="activity-detail-header">
      <div class="header-left">
        <h2 class="activity-detail-title">活动详情</h2>
      </div>
      <div class="header-buttons" v-if="isAdminView && hasEditPermission">
        <el-button type="primary" @click="goEdit" class="action-btn">编辑活动</el-button>
        <el-button type="danger" @click="handleDelete(activity.uuid)" class="action-btn">删除活动</el-button>
      </div>
    </div>
    <el-divider />
    <el-row :gutter="32" class="activity-info-row">
      <el-col :xs="24" :sm="24" :md="10" :lg="10" :xl="10">
        <el-image
          v-if="activity.coverUrl"
          :src="activity.coverUrl"
          fit="cover"
          class="activity-detail-cover"
          :preview-src-list="[activity.coverUrl]"
        />
      </el-col>
      <el-col :xs="24" :sm="24" :md="14" :lg="14" :xl="14">
        <div class="activity-info-list">
          <div class="info-item"><span class="info-label">活动名称</span><span class="info-value">{{ activity.name }}</span></div>
          <div class="info-item"><span class="info-label">活动日期</span><span class="info-value">{{ formatDate(activity.activityDate) }}</span></div>
          <div class="info-item"><span class="info-label">报名时间</span><span class="info-value">{{ formatDate(activity.signupStart) }} ~ {{ formatDate(activity.signupEnd) }}</span></div>
          <div class="info-item"><span class="info-label">报名人数</span><span class="info-value">{{ activity.signupActual }} / {{ activity.signupTotal }}</span></div>
          <div class="info-item"><span class="info-label">报名状态</span><el-tag :type="statusTagType(activity.signupStatus)" effect="plain">{{ activity.signupStatus }}</el-tag></div>
          <div class="info-item"><span class="info-label">活动链接</span><el-link :href="activity.link" target="_blank">{{ activity.link }}</el-link></div>
          <div class="info-item"><span class="info-label">活动二维码</span><el-image v-if="activity.qrcodeUrl" :src="activity.qrcodeUrl" fit="contain" style="width:100px;height:100px;" />
            <span v-else style="color:#aaa;">暂无二维码</span></div>
          <div class="info-item"><span class="info-label">活动地点</span><span class="info-value">{{ activity.venue }}</span></div>
        </div>
      </el-col>
    </el-row>
    <el-divider />
    <div class="activity-detail-section">
      <div class="content-label">活动详情</div>
      <div class="activity-detail-content-text">{{ activity.detail }}</div>
    </div>
    <el-divider />
    <!-- 报名表 -->
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">{{ isRegistered ? '报名信息' : '报名表' }}</span>
          <el-tag v-if="isSignupEnded" type="danger" effect="dark" style="margin-left: 10px;">报名已结束</el-tag>
        </div>
      </template>
      
      <el-form :model="signupForm" :rules="rules" ref="signupFormRef" 
               label-position="top" 
               :disabled="isRegistered || isSignupEnded" 
               size="large" 
               class="signup-form">
        <div class="form-section">
          <el-row>
            <el-col :span="24" :sm="8">
              <el-form-item label="称谓" prop="salutation">
                <el-select v-model="signupForm.salutation" placeholder="请选择称谓" class="full-width">
                  <el-option label="Prof" value="Prof" />
                  <el-option label="Dr" value="Dr" />
                  <el-option label="Mr" value="Mr" />
                  <el-option label="Mrs" value="Mrs" />
                  <el-option label="Ms" value="Ms" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="24" :sm="8">
              <el-form-item label="英文名" prop="firstName">
                <el-input v-model="signupForm.firstName" placeholder="请输入英文名" />
              </el-form-item>
            </el-col>
            <el-col :span="24" :sm="8">
              <el-form-item label="英文姓" prop="lastName">
                <el-input v-model="signupForm.lastName" placeholder="请输入英文姓" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24" :sm="8">
              <el-form-item label="中文名" prop="chineseName">
                <el-input v-model="signupForm.chineseName" placeholder="请输入中文名" />
              </el-form-item>
            </el-col>
            <el-col :span="24" :sm="8">
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="signupForm.email" placeholder="请输入邮箱" />
              </el-form-item>
            </el-col>
            <el-col :span="24" :sm="8">
              <el-form-item label="联系电话" prop="contactNumber">
                <el-input v-model="signupForm.contactNumber" placeholder="请输入联系电话" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div class="form-section" v-if="activity.requiredFields?.wechat_id_required || activity.requiredFields?.address_required || activity.requiredFields?.current_location_required || activity.requiredFields?.companions_required">
          <el-row>
            <el-col :span="24" :sm="8" v-if="activity.requiredFields?.wechat_id_required">
              <el-form-item label="微信ID" prop="wechatId">
                <el-input v-model="signupForm.wechatId" placeholder="请输入微信ID" />
              </el-form-item>
            </el-col>
            <el-col :span="24" :sm="8" v-if="activity.requiredFields?.address_required">
              <el-form-item label="通讯地址" prop="correspondenceAddress">
                <el-input v-model="signupForm.correspondenceAddress" placeholder="请输入通讯地址" />
              </el-form-item>
            </el-col>
            <el-col :span="24" :sm="8" v-if="activity.requiredFields?.current_location_required">
              <el-form-item label="当前位置" prop="currentLocation">
                <el-input v-model="signupForm.currentLocation" placeholder="请输入当前位置" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24" :sm="8" v-if="activity.requiredFields?.companions_required">
              <el-form-item label="携带人数" prop="companions">
                <el-input-number v-model="signupForm.companions" :min="0" :max="10" class="full-width" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div class="form-section" v-if="activity.requiredFields?.payment_proof_required">
          <el-row>
            <el-col :span="24">
              <el-form-item label="上传凭证" prop="paymentProof">
                <el-upload
                  class="upload-demo"
                  action="#"
                  :auto-upload="false"
                  :show-file-list="false"
                  :on-change="onPaymentProofChange"
                  :disabled="isRegistered || isSignupEnded"
                >
                  <el-button type="primary" :disabled="isRegistered || isSignupEnded">
                    <el-icon style="vertical-align: middle; margin-right: 4px;"><Upload /></el-icon>
                    选择文件
                  </el-button>
                  <template #tip>
                    <div class="el-upload__tip">请上传支付凭证图片</div>
                  </template>
                </el-upload>
                <el-image
                  v-if="signupForm.paymentProof"
                  :src="signupForm.paymentProof"
                  fit="contain"
                  class="payment-proof-preview"
                  :preview-src-list="[signupForm.paymentProof]"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div class="form-actions">
          <template v-if="!isRegistered && !isSignupEnded">
            <el-button type="primary" @click="submitSignup">提交报名</el-button>
          </template>
          <template v-else-if="isRegistered">
            <div class="registration-status">
              <el-tag type="success" size="large">已报名</el-tag>
              <div class="signup-time">报名时间：{{ formatDate(registrationData?.createdAt) }}</div>
            </div>
          </template>
          <template v-else>
            <el-tag type="danger" size="large">报名已结束</el-tag>
          </template>
        </div>
      </el-form>
    </el-card>
    <el-divider v-if="isAdminView" />
    <div v-if="isAdminView" class="activity-detail-section">
      <div class="content-label">报名人员列表</div>
      <div class="search-wrapper">
        <el-form :inline="true" class="search-bar">
          <el-form-item>
            <el-input
              v-model="searchName"
              placeholder="请输入姓名搜索"
              clearable
            >
            </el-input>
          </el-form-item>
        </el-form>
      </div>
      <div class="table-wrapper">
        <el-table
          v-if="pagedSignups.length"
          :data="pagedSignups"
          style="width: 100%; margin-bottom: 16px;"
          @row-click="handleRowClick"
        >
          <el-table-column prop="chineseName" label="中文姓名" />
          <el-table-column prop="firstName" label="英文名" />
          <el-table-column prop="lastName" label="英文姓" />
          <el-table-column prop="email" label="邮箱" />
          <el-table-column prop="contactNumber" label="联系电话" />
          <el-table-column prop="createdAt" label="报名时间" :formatter="(row) => formatDate(row.createdAt)" />
        </el-table>
        <div v-else style="color:#aaa; text-align:center; margin: 16px 0;">暂无报名数据</div>
      </div>
    </div>
    <el-dialog v-model="dialogVisible" title="报名信息详情" width="500px">
      <el-descriptions :column="1" border v-if="selectedSignup">
        <el-descriptions-item label="校友ID">{{ selectedSignup.alumniId }}</el-descriptions-item>
        <el-descriptions-item label="称谓">{{ selectedSignup.salutation }}</el-descriptions-item>
        <el-descriptions-item label="英文名">{{ selectedSignup.firstName }}</el-descriptions-item>
        <el-descriptions-item label="英文姓">{{ selectedSignup.lastName }}</el-descriptions-item>
        <el-descriptions-item label="中文名">{{ selectedSignup.chineseName }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ selectedSignup.email }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ selectedSignup.contactNumber }}</el-descriptions-item>
        <el-descriptions-item label="微信ID">{{ selectedSignup.wechatId }}</el-descriptions-item>
        <el-descriptions-item label="通讯地址">{{ selectedSignup.correspondenceAddress }}</el-descriptions-item>
        <el-descriptions-item label="当前位置">{{ selectedSignup.currentLocation }}</el-descriptions-item>
        <el-descriptions-item label="携带人数">{{ selectedSignup.companions }}</el-descriptions-item>
        <el-descriptions-item label="支付凭证">
          <template #default>
            <el-image v-if="selectedSignup && selectedSignup.paymentProof && selectedSignup.paymentProof.startsWith('http')"
              :src="selectedSignup.paymentProof"
              style="max-width:200px;max-height:200px;"
              fit="contain"
              :preview-src-list="[selectedSignup.paymentProof]"
            />
            <span v-else>{{ selectedSignup?.paymentProof || '无' }}</span>
          </template>
        </el-descriptions-item>
        <el-descriptions-item label="报名时间">{{ formatDate(selectedSignup.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDate(selectedSignup.updatedAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted, watch, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import service from '@/utils/request'
import { ElMessageBox, ElMessage } from 'element-plus'
import { ArrowLeft, Upload } from '@element-plus/icons-vue'
import axios from 'axios' // Added axios import
import { createWS } from '@/utils/ws'

const route = useRoute()
const router = useRouter()
const activity = ref({})
const signups = ref([])
const searchName = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const signupFormRef = ref(null)
const isRegistered = ref(false)
const registrationData = ref(null)
const dialogVisible = ref(false)
const selectedSignup = ref(null)

// 从 localStorage 获取用户信息
const userInfo = ref(JSON.parse(sessionStorage.getItem('user_info') || '{}'))

// 权限信息
const permissions = ref({})
const permissionLevel = ref(0)

// 计算属性：是否有编辑权限
const hasEditPermission = computed(() => permissionLevel.value === 2)

// 计算属性：是否为管理员视图
const isAdminView = computed(() => {
  return userInfo.value.role === 'admin'
})

// 获取权限信息
const fetchPermissions = async () => {
  try {
    const permissionRes = await service.get('/admin/permissions')
    if (permissionRes.code === 0) {
      permissions.value = permissionRes.data
      // 设置权限级别 - 使用活动管理权限
      if (permissions.value.activity_management_permission !== undefined) {
        permissionLevel.value = permissions.value.activity_management_permission
      } else {
        // 如果找不到具体权限，使用默认值0（无权限）
        permissionLevel.value = 0
      }
    } else {
      console.error('获取权限信息失败:', permissionRes.msg)
      // 权限获取失败时，设置为无权限
      permissionLevel.value = 0
    }
  } catch (error) {
    console.error('获取权限信息出错:', error)
    // 权限获取失败时，设置为无权限
    permissionLevel.value = 0
  }
}

// 报名表单数据
const signupForm = ref({
  salutation: '',
  firstName: '',
  lastName: '',
  chineseName: '',
  email: '',
  contactNumber: '',
  wechatId: '',
  correspondenceAddress: '',
  currentLocation: '',
  companions: 0,
  paymentProof: ''
})

// 在signupForm中添加paymentProofFile和paymentProofUrl
const paymentProofFile = ref(null)
const paymentProofUrl = ref('')

const onPaymentProofChange = async (file) => {
  const realFile = file.raw || file
  // 直接上传原图到OSS
  const formData = new FormData()
  formData.append('file', realFile)
  try {
    const res = await axios.post('/api/upload/oss', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    if (res.data && res.data.code === 0) {
      signupForm.value.paymentProof = res.data.url
      paymentProofUrl.value = res.data.url
      ElMessage.success('支付凭证上传成功')
    } else {
      ElMessage.error(res.data.msg || '支付凭证上传失败')
    }
  } catch (err) {
    ElMessage.error('支付凭证上传失败')
  }
}

// 表单验证规则
const rules = {
  salutation: [{ required: true, message: '请选择称谓', trigger: 'change' }],
  firstName: [{ required: true, message: '请输入英文名', trigger: 'blur' }],
  lastName: [{ required: true, message: '请输入英文姓', trigger: 'blur' }],
  chineseName: [{ required: true, message: '请输入中文名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  contactNumber: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  wechatId: [],
  correspondenceAddress: [],
  currentLocation: [],
  companions: [],
  paymentProof: []
}

// 动态设置表单校验规则
watch(
  () => activity.value.requiredFields,
  (fields) => {
    if (!fields) return
    rules.wechatId = fields.wechat_id_required
      ? [{ required: true, message: '请输入微信ID', trigger: 'blur' }]
      : []
    rules.correspondenceAddress = fields.address_required
      ? [{ required: true, message: '请输入通讯地址', trigger: 'blur' }]
      : []
    rules.currentLocation = fields.current_location_required
      ? [{ required: true, message: '请输入当前位置', trigger: 'blur' }]
      : []
    rules.companions = fields.companions_required
      ? [{ required: true, message: '请输入携带人数', trigger: 'change' }]
      : []
    rules.paymentProof = fields.payment_proof_required
      ? [{ required: true, message: '请上传支付凭证', trigger: 'change' }]
      : []
  },
  { immediate: true, deep: true }
)

// 检查是否已报名
const checkRegistration = async () => {
  try {
    const res = await service.get('/activity/registration', {
      params: {
        alumniId: userInfo.value.alumniId,
        activityUuid: activity.value.uuid
      }
    })
    if (res.code === 0 && res.data) {
      isRegistered.value = true
      registrationData.value = res.data
      // 填充表单数据
      signupForm.value = {
        salutation: res.data.salutation,
        firstName: res.data.firstName,
        lastName: res.data.lastName,
        chineseName: res.data.chineseName,
        email: res.data.email,
        contactNumber: res.data.contactNumber,
        wechatId: res.data.wechatId,
        correspondenceAddress: res.data.correspondenceAddress,
        currentLocation: res.data.currentLocation,
        companions: res.data.companions,
        paymentProof: res.data.paymentProof
      }
    }
  } catch (error) {
    // 保留错误提示
    console.error('检查报名状态失败:', error)
  }
}

// 提交报名
const submitSignup = async () => {
  if (isSignupEnded.value) {
    ElMessage.warning('报名已结束')
    return
  }
  try {
    const formData = {
      alumniId: userInfo.value.alumniId,
      activityUuid: activity.value.uuid,
      salutation: signupForm.value.salutation,
      firstName: signupForm.value.firstName,
      lastName: signupForm.value.lastName,
      chineseName: signupForm.value.chineseName,
      email: signupForm.value.email,
      contactNumber: signupForm.value.contactNumber,
      wechatId: signupForm.value.wechatId,
      correspondenceAddress: signupForm.value.correspondenceAddress,
      currentLocation: signupForm.value.currentLocation,
      companions: signupForm.value.companions,
      paymentProof: signupForm.value.paymentProof
    }
    await service.post('/activity/signup', formData)
    ElMessage.success('报名成功')
    await fetchActivity()
  } catch (error) {
    console.error('报名失败:', error)
    ElMessage.error('报名失败，请稍后重试')
  }
}

const fetchActivity = async () => {
  const res = await service.get(`/activity/detail/${route.params.id}`)
  if (res.code === 0) {
    activity.value = res.data
    activity.value.requiredFields = {
      wechat_id_required: res.data.wechat_id_required,
      address_required: res.data.address_required,
      current_location_required: res.data.current_location_required,
      companions_required: res.data.companions_required,
      payment_proof_required: res.data.payment_proof_required
    }
    // 检查报名状态
    await checkRegistration()
  } else {
    console.error('获取活动详情失败', res)
  }
}

const goEdit = () => {
  router.push(`/admin/activity-edit/${activity.value.uuid}`)
}
const handleDelete = (uuid) => {
  ElMessageBox.confirm('确定要删除该活动吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const res = await service.delete(`/activity/delete/${uuid}`)
    if (res.data && res.data.code === 0) {
      ElMessage.success('删除成功')
      router.push('/admin/activity-manage')
    } else {
      ElMessage.error(res.data.msg || '删除失败')
    }
  }).catch(() => {})
}
const formatDate = (val) => {
  if (!val) return ''
  const d = new Date(val)
  if (isNaN(d.getTime())) return ''
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}
const statusTagType = (status) => {
  if (status === '进行中') return 'success'
  if (status === '未开始') return 'info'
  if (status === '即将结束') return 'warning'
  return 'danger'
}
const filteredSignups = computed(() => {
  if (!searchName.value) return signups.value
  return signups.value.filter(signup => 
    signup.chineseName?.includes(searchName.value) || 
    signup.firstName?.includes(searchName.value) ||
    signup.lastName?.includes(searchName.value)
  )
})
const pagedSignups = computed(() => {
  const filtered = filteredSignups.value || []
  const start = (currentPage.value - 1) * pageSize.value
  return filtered.slice(start, start + pageSize.value)
})
const handlePageChange = (page) => {
  currentPage.value = page
}
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}
const goBack = () => {
  const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')
  const isAdmin = userInfo.role === 'admin'
  const from = sessionStorage.getItem('activity_manage_from')
  if (isAdmin) {
    router.push('/admin/activity-manage')
  } else {
    router.push('/alumni/activity-manage')
  }
}
const isSignupEnded = computed(() => {
  if (!activity.value.signupEnd) return false
  const now = new Date()
  const endDate = new Date(activity.value.signupEnd)
  return now > endDate
})

let ws = null
function initWebSocket() {
  ws = createWS({
    onMessage: (event) => {
      const msg = event.data;
      if (["activity changed", "activity registration changed"].includes(msg)) {
        fetchActivity();
        if (isAdminView.value) fetchSignups();
      }
    },
    onClose: () => {
      // 可选：可加提示或其他处理
    }
  })
}

const fetchSignups = async () => {
  if (!activity.value.uuid) {
    console.error('activityUuid 为空，无法获取报名列表')
    return
  }
  try {
    const res = await service.get('/activity/registrations', { params: { activityUuid: activity.value.uuid } })
    if (res.code === 0) {
      signups.value = res.data || []
    } else {
      signups.value = []
    }
  } catch (error) {
    signups.value = []
    console.error('获取报名列表失败:', error)
  }
}

const handleRowClick = (row) => {
  selectedSignup.value = row
  dialogVisible.value = true
}

onMounted(async () => {
  await fetchPermissions()
  await fetchActivity()
  if (isAdminView.value) {
    await fetchSignups()
  }
  try {
    const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')
    const alumniId = userInfo.alumniId
    if (alumniId) {
      const res = await service.get(`/user/info?alumniId=${alumniId}`)
      if (res.code === 0 && res.data) {
        Object.assign(signupForm.value, res.data)
      }
    }
  } catch (e) {}
  initWebSocket()
})

onBeforeUnmount(() => {
  if (ws && ws._manualClose) ws._manualClose();
})
</script>

<style scoped>
.activity-detail-root {
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
  z-index: 10;
}

.activity-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-top: 40px;
}

.header-left {
  flex: 1;
  padding-right: 20px;
}

.activity-detail-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  padding: 0;
}

.header-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
  white-space: nowrap;
  flex: 0 0 auto;
}

.action-btn {
  min-width: 120px;
  height: 40px;
}

.activity-detail-cover {
  width: 100%;
  height: 300px;
  border-radius: 8px;
  object-fit: cover;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.activity-info-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.info-label {
  min-width: 100px;
  color: #606266;
  font-weight: 500;
}

.info-value {
  color: #303133;
  flex: 1;
}

.activity-detail-section {
  margin-bottom: 24px;
}

.content-label {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 4px solid #409eff;
}

.activity-detail-content-text {
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
}

.form-card {
  margin-top: 24px;
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

.card-header span {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.signup-form :deep(.el-form-item__label) {
  font-weight: 500;
  padding-bottom: 4px;
}

.signup-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.signup-form :deep(.el-input),
.signup-form :deep(.el-select .el-input__wrapper) {
  background-color: #f8fafc;
  border-radius: 6px;
}

.search-wrapper {
  margin-bottom: 16px;
}

.table-wrapper {
  overflow-x: auto;
  width: 100%;
}

.search-bar {
  display: flex;
  align-items: center;
}

.pagination-bar {
  margin: 16px 0;
  text-align: right;
}

.form-actions {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 24px;
}

.registration-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.signup-time {
  color: #606266;
  font-size: 14px;
}

@media (max-width: 768px) {
  .activity-detail-root {
    max-width: 100%;
    margin: 0;
    border-radius: 0;
    box-shadow: none;
    padding: 8px;
  }
  
  .back-btn {
    position: static;
    margin-bottom: 8px;
    font-size: 16px;
  }
  
  .activity-detail-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    margin-bottom: 16px;
    padding-top: 0;
  }
  
  .header-buttons {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }
  
  .activity-detail-title {
    font-size: 1.2rem;
  }
  
  .action-btn {
    width: 100% !important;
    min-width: unset;
    height: 36px;
    margin-left: 0 !important;
    margin-right: 0 !important;
  }
  
  .activity-detail-cover {
    max-width: 100%;
    max-height: 180px;
    margin: 0 auto 12px auto;
    border-radius: 6px;
  }
  
  .activity-info-list {
    gap: 8px;
    margin-top: 4px;
  }
  
  .info-item {
    font-size: 1rem;
    margin-bottom: 4px;
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .info-label {
    min-width: auto;
    margin-right: 0;
    margin-bottom: 4px;
    font-size: 14px;
    font-weight: 600;
  }
  
  .info-value {
    font-size: 14px;
    width: 100%;
  }
  
  .content-label {
    font-size: 1rem;
    margin-bottom: 8px;
    padding-left: 8px;
  }
  
  .activity-detail-content-text {
    font-size: 1rem;
    padding: 12px;
    margin-top: 2px;
  }
  
  .form-card {
    margin-top: 16px;
    border-radius: 0;
    box-shadow: none;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    padding: 12px 16px;
  }
  
  .header-title {
    font-size: 16px;
  }
  
  .form-section {
    padding: 12px 8px;
    margin-bottom: 16px;
  }
  
  .signup-form :deep(.el-form-item) {
    margin-bottom: 12px;
  }
  
  .signup-form :deep(.el-form-item__label) {
    font-size: 14px;
    padding-bottom: 4px;
  }

  .signup-form :deep(.el-input__wrapper),
  .signup-form :deep(.el-select .el-input__wrapper) {
    background-color: #f8fafc;
    border-radius: 6px;
  }
  
  .search-wrapper {
    margin-bottom: 8px;
  }
  
  .search-bar {
    flex-direction: column;
    align-items: stretch;
    width: 100%;
  }
  
  .search-bar :deep(.el-form-item) {
    margin-right: 0;
    width: 100%;
  }
  
  .search-bar :deep(.el-input) {
    width: 100%;
  }
  
  .table-wrapper {
    width: 100vw;
    margin-left: -8px;
    padding-bottom: 8px;
    overflow-x: auto;
  }
  
  :deep(.el-table) {
    font-size: 13px;
  }
  
  :deep(.el-table th) {
    padding: 8px 0;
  }
  
  :deep(.el-table td) {
    padding: 6px 0;
  }
  
  .pagination-bar {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  :deep(.el-form) {
    padding: 12px 8px;
  }
  
  :deep(.el-row) {
    margin-left: 0 !important;
    margin-right: 0 !important;
  }
  
  :deep(.el-col) {
    padding-left: 0 !important;
    padding-right: 0 !important;
  }
  
  .form-actions {
    margin-top: 16px;
  }
  
  .registration-status {
    gap: 12px;
  }
  
  .signup-time {
    font-size: 13px;
  }
}

:deep(.el-form) {
  padding: 24px;
}

:deep(.el-upload) {
  width: 100%;
}

:deep(.el-upload-dragger) {
  width: 100%;
  height: 120px;
}

:deep(.el-table) {
  margin-top: 16px;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  font-weight: 600;
}
</style> 