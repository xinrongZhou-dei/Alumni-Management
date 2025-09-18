<template>
    <el-card class="activity-detail-root" shadow="hover">
      <el-button type="text" @click="goBack" class="back-btn"><el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回</el-button>
      <div class="activity-detail-header">
        <div class="activity-detail-title">活动详情</div>
      </div>
      <el-divider />
      <el-row :gutter="32">
        <el-col :span="10">
          <el-image
            v-if="activity.coverUrl"
            :src="activity.coverUrl"
            fit="cover"
            class="activity-detail-cover"
            :preview-src-list="[activity.coverUrl]"
          />
        </el-col>
        <el-col :span="14">
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
            <el-tag v-if="isSignupFull" type="warning" effect="dark" style="margin-left: 10px;">报名人数已满</el-tag>
          </div>
        </template>
        <el-form :model="signupForm" :rules="rules" ref="signupFormRef" 
                 label-position="top" 
                 :disabled="isRegistered || isSignupEnded || isSignupFull" 
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
                    :disabled="isRegistered || isSignupEnded || isSignupFull"
                  >
                    <el-button type="primary" :disabled="isRegistered || isSignupEnded || isSignupFull">
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
            <template v-if="canSignup">
              <el-button type="primary" @click="submitSignup">提交报名</el-button>
            </template>
            <template v-else-if="isRegistered">
              <div class="registration-status">
                <el-tag type="success" size="large">已报名</el-tag>
                <div class="signup-time">报名时间：{{ formatDate(registrationData?.createdAt) }}</div>
              </div>
            </template>
            <template v-else-if="isSignupFull">
              <el-tag type="danger" size="large">报名人数已满</el-tag>
            </template>
            <template v-else>
              <el-tag type="danger" size="large">报名已结束</el-tag>
            </template>
          </div>
        </el-form>
      </el-card>
    </el-card>
  </template>
  
  <script setup>
  // ... 复用ActivityDetail.vue的逻辑，去除isAdminView、goEdit、handleDelete、报名人员列表、信息展示等
  import { ref, computed, onMounted, watch, onBeforeUnmount } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import axios from 'axios'
  import { ElMessage } from 'element-plus'
  import { ArrowLeft, Upload } from '@element-plus/icons-vue'
  import imageCompression from 'browser-image-compression'
  import { createWS } from '@/utils/ws'

  const route = useRoute()
  const router = useRouter()
  const activity = ref({})
  const signupFormRef = ref(null)
  const isRegistered = ref(false)
  const registrationData = ref(null)
  const userInfo = ref(JSON.parse(sessionStorage.getItem('user_info') || '{}'))
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
  
  // 添加人数满员状态判断
  const isSignupFull = computed(() => {
    return activity.value.signupActual >= activity.value.signupTotal
  })
  
  // 是否可以报名
  const canSignup = computed(() => {
    return !isRegistered.value && !isSignupEnded.value && !isSignupFull.value
  })
  
  const checkRegistration = async () => {
    try {
      const res = await axios.get('/api/activity/registration', {
        params: {
          alumniId: userInfo.value.alumniId,
          activityUuid: activity.value.uuid
        }
      })
      if (res.data && res.data.code === 0 && res.data.data) {
        isRegistered.value = true
        registrationData.value = res.data.data
        signupForm.value = {
          salutation: res.data.data.salutation,
          firstName: res.data.data.firstName,
          lastName: res.data.data.lastName,
          chineseName: res.data.data.chineseName,
          email: res.data.data.email,
          contactNumber: res.data.data.contactNumber,
          wechatId: res.data.data.wechatId,
          correspondenceAddress: res.data.data.correspondenceAddress,
          currentLocation: res.data.data.currentLocation,
          companions: res.data.data.companions,
          paymentProof: res.data.data.paymentProof
        }
      }
    } catch (error) {
      // 忽略
    }
  }
  const submitSignup = async () => {
    if (isSignupEnded.value) {
      ElMessage.warning('报名已结束')
      return
    }
    if (isSignupFull.value) {
      ElMessage.warning('报名人数已满')
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
      const res = await axios.post('/api/activity/signup', formData)
      if (res.data && res.data.code === 0) {
        ElMessage.success('报名成功')
        await fetchActivity()
      } else {
        ElMessage.error(res.data.msg || '报名失败，请稍后重试')
      }
    } catch (error) {
      if (error.response && error.response.data) {
        ElMessage.error(error.response.data.msg || '报名失败，请稍后重试')
      } else {
        ElMessage.error('报名失败，请稍后重试')
      }
    }
  }
  const fetchActivity = async () => {
    const res = await axios.get(`/api/activity/detail/${route.params.id}`)
    if (res.data && res.data.code === 0) {
      activity.value = res.data.data
      activity.value.requiredFields = {
        wechat_id_required: res.data.data.wechat_id_required,
        address_required: res.data.data.address_required,
        current_location_required: res.data.data.current_location_required,
        companions_required: res.data.data.companions_required,
        payment_proof_required: res.data.data.payment_proof_required
      }
      await checkRegistration()
    }
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
  const goBack = () => {
    const from = sessionStorage.getItem('activity_detail_from')
    if (from === 'list') {
      router.push('/alumni/activity-signup')
    } else {
      router.push('/')
    }
  }
  const isSignupEnded = computed(() => {
    if (!activity.value.signupEnd) return false
    const now = new Date()
    const endDate = new Date(activity.value.signupEnd)
    return now > endDate
  })
  onMounted(async () => {
    await fetchActivity()
    try {
      const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')
      const alumniId = userInfo.alumniId
      if (alumniId) {
        const res = await axios.get(`/api/user/info?alumniId=${alumniId}`)
        if (res.data && res.data.code === 0) {
          Object.assign(signupForm.value, res.data.data)
        }
      }
    } catch (e) {}
    initWebSocket()
  })
  let ws = null
  function initWebSocket() {
    ws = createWS({
      onMessage: (event) => {
        const msg = event.data;
        if (["activity changed", "activity registration changed"].includes(msg)) {
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
  </script>
  
  <style scoped>
  /* 复用ActivityDetail.vue的样式 */
  .activity-detail-root {
    max-width: 1000px;
    margin: 32px auto;
    border-radius: 16px;
    box-shadow: 0 4px 24px #e0e7ef22;
    padding: 36px 40px 40px 40px;
    background: #fff;
  }
  .activity-detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0;
  }
  .activity-detail-title {
    font-size: 2rem;
    font-weight: bold;
    color: #2563eb;
  }
  .activity-detail-cover {
    width: 100%;
    max-width: 360px;
    max-height: 220px;
    margin: 0 auto 18px auto;
    display: block;
    border-radius: 10px;
    box-shadow: 0 2px 8px #c7d2fe22;
  }
  .activity-info-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-top: 8px;
  }
  .info-item {
    display: flex;
    align-items: center;
    font-size: 1.08rem;
    margin-bottom: 2px;
  }
  .info-label {
    color: #666;
    font-weight: 400;
    min-width: 90px;
    margin-right: 12px;
    font-size: 16px;
    font-family: 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  }
  .info-value {
    color: #222;
    font-weight: 400;
    font-size: 16px;
    font-family: 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  }
  .content-label {
    color: #888;
    font-size: 1.08rem;
    font-weight: 500;
    margin-bottom: 6px;
  }
  .activity-detail-content-text {
    font-size: 1.25rem;
    color: #222;
    line-height: 1.8;
    font-family: 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
    background: #f8fafc;
    border-radius: 8px;
    padding: 18px 22px;
    margin-top: 4px;
    box-shadow: 0 1px 4px #e0e7ef22;
    font-weight: normal;
  }
  .back-btn {
    position: absolute;
    left: 24px;
    top: 24px;
    font-size: 18px;
    z-index: 10;
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
  .header-title {
    font-size: 18px;
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
    align-items: center;
    gap: 16px;
    margin-top: 32px;
    padding-top: 24px;
    border-top: 1px solid #ebeef5;
  }
  .signup-time {
    margin-left: 16px;
    color: #67c23a;
    font-size: 14px;
  }
  .payment-proof-preview {
    margin-top: 16px;
    max-width: 200px;
    border-radius: 4px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  .el-upload__tip {
    margin-top: 8px;
    color: #909399;
    font-size: 12px;
  }
  .signup-form :deep(.el-form-item__label) {
    font-weight: 500;
    padding-bottom: 4px;
  }
  .signup-form :deep(.el-form-item) {
    margin-bottom: 16px;
  }
  .registration-status {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
  }
  @media (max-width: 768px) {
    .activity-detail-root {
      max-width: 100%;
      margin: 0;
      border-radius: 0;
      box-shadow: none;
      padding: 8px;
    }
    .activity-detail-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }
    .activity-detail-title {
      font-size: 1.2rem;
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
    }
    .content-label {
      font-size: 1rem;
      margin-bottom: 4px;
    }
    .activity-detail-content-text {
      font-size: 1rem;
      padding: 12px 16px;
      margin-top: 2px;
    }
    .back-btn {
      position: static;
      margin-bottom: 8px;
      font-size: 16px;
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
    }
    .signup-time {
      margin-left: 0;
      margin-top: 8px;
      font-size: 13px;
    }
    .payment-proof-preview {
      max-width: 150px;
    }
    .signup-form :deep(.el-form-item__label) {
      font-size: 14px;
      padding-bottom: 4px;
      width: auto !important;
      min-width: auto !important;
      display: block;
      text-align: left;
    }
    .signup-form :deep(.el-input__wrapper),
    .signup-form :deep(.el-select .el-input__wrapper) {
      background-color: #f8fafc;
      border-radius: 6px;
    }
  }
  </style>
  