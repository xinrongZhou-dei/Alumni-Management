<template>
  <div class="visit-record-list">
    <el-button type="text" @click="goBack" class="back-btn">
      <el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回
    </el-button>

    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>我的预约记录</span>
        </div>
      </template>

      <el-table :data="recordList" v-loading="loading" @row-click="handleRowClick">
        <el-table-column label="预约校区" prop="visitSchool" />
        <el-table-column label="预约日期" prop="visitDay" />
        <el-table-column label="预约时段" prop="visitTime">
          <template #default="scope">
            {{ getTimeText(scope.row.visitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="预约状态" prop="status">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" />
      </el-table>
    </el-card>

    <!-- 预约详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="预约详情"
      width="50%"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="recordDetail"
        :rules="rules"
        label-width="120px"
        :disabled="recordDetail.status !== 'PENDING'"
      >
        <el-form-item label="校友姓名">
          <el-input v-model="recordDetail.chineseName" :disabled="true" />
        </el-form-item>

        <el-form-item label="学号">
          <el-input v-model="recordDetail.alumniId" :disabled="true" />
        </el-form-item>

        <el-form-item label="当前所在地">
          <el-input v-model="recordDetail.currentLocation" :disabled="true" />
        </el-form-item>

        <el-form-item label="联系方式">
          <el-input v-model="recordDetail.contactNumber" :disabled="true" />
        </el-form-item>

        <el-form-item label="预约校区" prop="visitSchool">
          <el-select v-model="recordDetail.visitSchool" placeholder="请选择校区" :disabled="recordDetail.status !== 'PENDING'">
            <el-option
              v-for="item in campusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="预约日期" prop="visitDay">
          <el-date-picker
            v-model="recordDetail.visitDay"
            type="date"
            placeholder="请选择日期"
            :disabled-date="disabledDate"
            value-format="YYYY-MM-DD"
            :disabled="recordDetail.status !== 'PENDING'"
          />
        </el-form-item>

        <el-form-item label="预约时段" prop="visitTime">
          <el-select v-model="recordDetail.visitTime" placeholder="请选择时段" :disabled="recordDetail.status !== 'PENDING'">
            <el-option
              v-for="item in timeSlotOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="备注信息" prop="remark">
          <el-input
            v-model="recordDetail.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入备注信息"
            :disabled="recordDetail.status !== 'PENDING'"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button
            v-if="recordDetail.status === 'PENDING'"
            type="primary"
            @click="handleSave"
          >
            保存修改
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getAlumniVisitRecordList, getVisitRecordDetail, updateVisitStatus } from '../api/visitRecord'

const router = useRouter()
const loading = ref(false)
const dialogVisible = ref(false)
const recordList = ref([])
const recordDetail = ref({})
const ws = ref(null)

const rules = {
  visitSchool: [{ required: true, message: '请选择校区', trigger: 'change' }],
  visitDay: [{ required: true, message: '请选择日期', trigger: 'change' }],
  visitTime: [{ required: true, message: '请选择时间段', trigger: 'change' }],
  remark: [{ required: true, message: '请输入备注信息', trigger: 'blur' }]
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

const getStatusType = (status) => {
  const statusMap = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return statusMap[status]
}

const getStatusText = (status) => {
  const statusMap = {
    PENDING: '待审核',
    APPROVED: '已批准',
    REJECTED: '已拒绝'
  }
  return statusMap[status]
}

const getTimeText = (time) => {
  const timeMap = {
    MORNING: '上午(9:00-11:30)',
    AFTERNOON: '下午(13:30-16:00)'
  }
  return timeMap[time]
}

const getList = async () => {
  loading.value = true
  try {
    // 从token中获取alumni_id，确保与后端一致
    const token = sessionStorage.getItem('alumni_token')
    let alumniId = ''
    
    if (token) {
      try {
        // 解析JWT token获取alumni_id
        const tokenPayload = JSON.parse(atob(token.split('.')[1]))
        if (tokenPayload.alumni_id) {
          alumniId = tokenPayload.alumni_id
        }
      } catch (e) {
        console.warn('解析token失败:', e)
      }
    }
    
    if (!alumniId) {
      // 如果无法从token获取，使用userInfo作为备选
      const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')
      alumniId = userInfo.alumniId
    }
    
    const res = await getAlumniVisitRecordList(alumniId)
    if (res.code === 200) {
      recordList.value = res.data
    } else {
      ElMessage.error(res.message || '获取预约记录失败')
    }
  } catch (error) {
    console.error('获取预约记录失败:', error)
    ElMessage.error('获取预约记录失败')
  } finally {
    loading.value = false
  }
}

const handleRowClick = async (row) => {
  try {
    const res = await getVisitRecordDetail(row.visitUUID)
    if (res.code === 0 || res.code === 200) {
      // 将预约记录信息存储到 sessionStorage
      sessionStorage.setItem('visit_record', JSON.stringify(res.data))
      // 跳转到预约表单页面
      router.push('/visit-record/form')
    } else {
      ElMessage.error(res.message || '获取预约详情失败')
    }
  } catch (error) {
    console.error('获取预约详情失败:', error)
    ElMessage.error('获取预约详情失败')
  }
}

const handleSave = async () => {
  try {
    const res = await updateVisitStatus({
      visitUUID: recordDetail.value.visitUUID,
      status: recordDetail.value.status,
      remark: recordDetail.value.remark
    })
    if (res.code === 0) {
      ElMessage.success('修改成功')
      dialogVisible.value = false
      getList()
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch (error) {
    console.error('修改失败:', error)
    ElMessage.error('修改失败')
  }
}

const goBack = () => {
  router.push('/')
}

function handleWebSocketMessage(event) {
  // 只要有数据库变更就刷新
  getList()
}

function initWebSocket() {
  // WebSocket 连接初始化
  const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const wsHost = window.location.host;
  const wsPath = '/ws/database-updates';
  ws.value = new WebSocket(wsProtocol + '//' + wsHost + wsPath);
  ws.value.onmessage = handleWebSocketMessage;
  ws.value.onclose = () => {
    setTimeout(initWebSocket, 3000);
  };
}

onMounted(() => {
  getList()
  initWebSocket()
})

onUnmounted(() => {
  if (ws.value) ws.value.close()
})
</script>

<style scoped>
.visit-record-list {
  padding: 24px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.back-btn {
  position: absolute;
  left: 24px;
  top: 24px;
  font-size: 18px;
}

.list-card {
  max-width: 1200px;
  margin: 60px auto 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .visit-record-list {
    padding: 8px;
  }
  .back-btn {
    position: static;
    margin-bottom: 8px;
    font-size: 16px;
  }
  .list-card {
    max-width: 100%;
    margin: 24px 0 0;
    box-shadow: none;
    border-radius: 0;
  }
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  .el-table {
    font-size: 13px;
    overflow-x: auto;
  }
  .el-table__header th, .el-table__body td {
    padding: 6px 4px !important;
    font-size: 13px !important;
  }
  .el-dialog {
    width: 95% !important;
    margin: 5vh auto !important;
  }
  .el-dialog__body {
    padding: 12px !important;
  }
  .el-dialog__header {
    padding: 12px !important;
  }
  .el-dialog__footer {
    padding: 12px !important;
  }
  .dialog-footer {
    flex-direction: column;
    gap: 8px;
  }
  .el-form-item {
    margin-bottom: 12px;
  }
  :deep(.el-form-item__label) {
    font-size: 14px;
    min-width: 8em;
    width: 8em;
    display: inline-block;
    text-align: right;
  }
  :deep(.el-form-item__content) {
    margin-left: 8em !important;
  }
}
</style> 