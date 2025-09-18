<template>
  <div class="visit-record-detail">
    <el-button type="text" @click="handleBack" :style="isMobile ? 'position: absolute; left: 12px; top: 12px; font-size: 16px;' : 'position: absolute; left: 24px; top: 24px; font-size: 18px;'">
      <el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回
    </el-button>

    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <span>预约详情</span>
        </div>
      </template>

      <!-- 移动端自定义字段渲染 -->
      <div v-if="isMobile" class="mobile-detail-list">
        <div class="mobile-detail-item"><div class="mobile-label">校友姓名</div><div class="mobile-value">{{ recordDetail.chineseName }}</div></div>
        <div class="mobile-detail-item"><div class="mobile-label">学号</div><div class="mobile-value">{{ recordDetail.alumniId }}</div></div>
        <div class="mobile-detail-item"><div class="mobile-label">当前位置</div><div class="mobile-value">{{ recordDetail.currentLocation }}</div></div>
        <div class="mobile-detail-item"><div class="mobile-label">联系方式</div><div class="mobile-value">{{ recordDetail.contactNumber }}</div></div>
        <div class="mobile-detail-item"><div class="mobile-label">预约校区</div><div class="mobile-value">{{ recordDetail.visitSchool }}</div></div>
        <div class="mobile-detail-item"><div class="mobile-label">预约日期</div><div class="mobile-value">{{ recordDetail.visitDay }}</div></div>
        <div class="mobile-detail-item"><div class="mobile-label">预约时段</div><div class="mobile-value">{{ getTimeText(recordDetail.visitTime) }}</div></div>
        <div class="mobile-detail-item"><div class="mobile-label">预约状态</div><div class="mobile-value"><el-tag :type="getStatusType(recordDetail.status)">{{ getStatusText(recordDetail.status) }}</el-tag></div></div>
        <div class="mobile-detail-item"><div class="mobile-label">创建时间</div><div class="mobile-value">{{ recordDetail.createTime }}</div></div>
        <div class="mobile-detail-item"><div class="mobile-label">更新时间</div><div class="mobile-value">{{ recordDetail.updateTime }}</div></div>
        <div class="mobile-detail-item"><div class="mobile-label">备注</div><div class="mobile-value">{{ recordDetail.remark || '无' }}</div></div>
      </div>

      <!-- PC端原有布局 -->
      <el-descriptions v-else :column="2" border>
        <el-descriptions-item label="校友姓名">{{ recordDetail.chineseName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ recordDetail.alumniId }}</el-descriptions-item>
        <el-descriptions-item label="当前位置">{{ recordDetail.currentLocation }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ recordDetail.contactNumber }}</el-descriptions-item>
        <el-descriptions-item label="预约校区">{{ recordDetail.visitSchool }}</el-descriptions-item>
        <el-descriptions-item label="预约日期">{{ recordDetail.visitDay }}</el-descriptions-item>
        <el-descriptions-item label="预约时段">{{ getTimeText(recordDetail.visitTime) }}</el-descriptions-item>
        <el-descriptions-item label="预约状态"><el-tag :type="getStatusType(recordDetail.status)">{{ getStatusText(recordDetail.status) }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ recordDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ recordDetail.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ recordDetail.remark || '无' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 审核操作区域 -->
      <div v-if="recordDetail.status === 'PENDING'" class="action-area">
        <el-form :model="reviewForm" :label-width="isMobile ? '100px' : '80px'" :label-position="isMobile ? 'top' : 'right'">
          <el-form-item label="审核结果">
            <el-radio-group v-model="reviewForm.status">
              <el-radio
                v-for="option in statusOptions"
                :key="option.value"
                :label="option.value"
              >
                {{ option.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="备注">
            <el-input
              v-model="reviewForm.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入审核备注"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleReview" :style="isMobile ? 'width: 100%;' : ''">提交审核</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getVisitRecordDetail, updateVisitStatus } from '../../api/visitRecord'
import { ArrowLeft } from '@element-plus/icons-vue'
import { createWS } from '@/utils/ws'

const route = useRoute()
const router = useRouter()
const recordDetail = ref({})

// 获取用户信息
const userInfo = ref(JSON.parse(sessionStorage.getItem('user_info') || '{}'))

// 审核表单
const reviewForm = reactive({
  status: 'APPROVED',
  remark: ''
})

// 状态选项
const statusOptions = [
  { value: 'APPROVED', label: '批准' },
  { value: 'REJECTED', label: '拒绝' }
]

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return statusMap[status]
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    PENDING: '待审核',
    APPROVED: '已批准',
    REJECTED: '已拒绝'
  }
  return statusMap[status]
}

// 获取时段文本
const getTimeText = (time) => {
  const timeMap = {
    MORNING: '上午(9:00-11:30)',
    AFTERNOON: '下午(13:30-16:00)'
  }
  return timeMap[time]
}

// WebSocket连接
const ws = ref(null)

// 获取详情
const getDetail = async () => {
  try {
    const res = await getVisitRecordDetail(route.params.id)
    recordDetail.value = res.data
  } catch (error) {
    console.error('获取预约详情失败:', error)
    ElMessage.error('获取预约详情失败')
  }
}

// 提交审核
const handleReview = async () => {
  try {
    await ElMessageBox.confirm('确认提交审核结果吗？', '提示', {
      type: 'warning'
    })
    await updateVisitStatus({
      visitUUID: recordDetail.value.visitUUID,
      status: reviewForm.status,
      remark: reviewForm.remark
    })
    ElMessage.success('审核提交成功')
    getDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核提交失败:', error)
      ElMessage.error('审核提交失败')
    }
  }
}

// 处理返回
const handleBack = () => {
    router.push('/admin/visit-records')
}

// 处理WebSocket消息
const handleWebSocketMessage = (event) => {
  const data = JSON.parse(event.data)
  if (data.type === 'VISIT_RECORD_UPDATED' && data.payload.visitUUID === route.params.id) {
    // 如果当前正在查看的记录被更新，则刷新详情
    getDetail()
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

const isMobile = ref(false)
const checkScreenWidth = () => {
  isMobile.value = window.innerWidth < 768
}

onMounted(() => {
  checkScreenWidth()
  window.addEventListener('resize', checkScreenWidth)
  getDetail()
  initWebSocket()
})

onUnmounted(() => {
  if (ws.value && ws.value._manualClose) ws.value._manualClose();
})
</script>

<style scoped>
.visit-record-detail {
  padding: 20px;
}

.detail-card {
  margin-top: 60px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-area {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

:deep(.el-descriptions) {
  margin-top: 20px;
}

:deep(.el-descriptions__label) {
  width: 120px;
}

.mobile-detail-list {
  margin-top: 20px;
}
.mobile-detail-item {
  padding: 12px 0 8px 0;
  border-bottom: 1px solid #f0f0f0;
}
.mobile-label {
  font-size: 14px;
  color: #888;
  font-weight: bold;
  margin-bottom: 2px;
}
.mobile-value {
  font-size: 16px;
  color: #222;
  word-break: break-all;
  margin-top: 2px;
}
</style> 