<template>
  <div class="visit-record-container">
    <el-button type="text" @click="goBack" :style="isMobile ? 'position: absolute; left: 12px; top: 12px; font-size: 16px;' : 'position: absolute; left: 24px; top: 24px; font-size: 18px;'">
      <el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回
    </el-button>

    <!-- 查询表单 -->
    <el-card class="search-card">
      <el-form :model="queryParams" ref="queryForm" :inline="!isMobile" v-show="showSearch" :label-position="isMobile ? 'top' : 'right'" class="search-form" :class="{ 'mobile-form': isMobile }">
        <el-form-item prop="visitSchool">
          <el-select v-model="queryParams.visitSchool" placeholder="请选择校区" clearable :style="isMobile ? 'width: 100%;' : 'width: 240px;'" @change="handleQuery">
            <el-option
              v-for="item in schoolOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable :style="isMobile ? 'width: 100%;' : 'width: 160px;'">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item prop="visitDay">
          <el-date-picker
            v-model="queryParams.visitDay"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            clearable
            :style="isMobile ? 'width: 100%;' : ''"
            @change="handleQuery"
          />
        </el-form-item>
        <el-form-item :class="{ 'mobile-form-btns': isMobile }">
          <el-button icon="Refresh" @click="resetQuery" :style="isMobile ? 'width: 100%;' : ''">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮区域 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header" :class="{ 'mobile-header': isMobile }">
          <div class="right" :class="{ 'mobile-header-btns': isMobile }">
            <el-button
              v-if="campusVisitRecordPermission === 2"
              type="primary"
              @click="handleSelectAllPending"
              :style="isMobile ? 'width: 100%; margin-bottom: 8px;' : ''"
            >
              全选待审核
            </el-button>
            <el-button
              v-if="campusVisitRecordPermission === 2"
              type="primary"
              :disabled="multipleSelection.length === 0"
              @click="handleBatchApprove"
              :style="isMobile ? 'width: 100%; margin-bottom: 8px;' : ''"
            >
              批量批准
            </el-button>
            <el-button
              v-if="campusVisitRecordPermission === 2"
              type="danger"
              :disabled="multipleSelection.length === 0"
              @click="handleBatchReject"
              :style="isMobile ? 'width: 100%;' : ''"
            >
              批量拒绝
            </el-button>
          </div>
        </div>
      </template>

      <!-- 数据表格 -->
      <div class="table-responsive">
        <el-table
          v-loading="loading"
          :data="recordList"
          @selection-change="handleSelectionChange"
          @row-click="handleRowClick"
          :size="isMobile ? 'small' : 'default'"
          :style="isMobile ? 'font-size: 13px;' : ''"
        >
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="校友姓名" prop="chineseName" />
          <el-table-column label="学号" prop="alumniId" />
          <el-table-column label="当前位置" prop="currentLocation" />
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
          <el-table-column label="操作" align="center" width="150" v-if="campusVisitRecordPermission === 2">
            <template #default="scope">
              <div :class="{ 'mobile-table-actions': isMobile }">
                <el-button
                  v-if="scope.row.status === 'PENDING'"
                  type="success"
                  link
                  @click.stop="handleApprove(scope.row)"
                  class="action-btn"
                  :style="isMobile ? 'width: 100%; margin-bottom: 6px;' : ''"
                >
                  批准
                </el-button>
                <el-button
                  v-if="scope.row.status === 'PENDING'"
                  type="danger"
                  link
                  @click.stop="handleReject(scope.row)"
                  class="action-btn"
                  :style="isMobile ? 'width: 100%;' : ''"
                >
                  拒绝
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-container" :class="{ 'mobile-pagination': isMobile }">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          :layout="isMobile ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :small="isMobile"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getVisitRecordList, updateVisitStatus, batchUpdateVisitStatus } from '../../api/visitRecord'
import { ArrowLeft } from '@element-plus/icons-vue'
import axios from 'axios'
import { createWS } from '@/utils/ws'

const router = useRouter()
const loading = ref(false)
const showSearch = ref(true)
const total = ref(0)
const recordList = ref([])
const multipleSelection = ref([])
const campusVisitRecordPermission = ref(0)
const isMobile = ref(false)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  visitSchool: undefined,
  status: undefined,
  visitDay: undefined
})

// 校区选项
const schoolOptions = ref([])

// 状态选项
const statusOptions = [
  { value: 'PENDING', label: '待审核' },
  { value: 'APPROVED', label: '已批准' },
  { value: 'REJECTED', label: '已拒绝' }
]

// 时段选项
const timeOptions = [
  { value: 'MORNING', label: '上午(9:00-11:30)' },
  { value: 'AFTERNOON', label: '下午(13:30-16:00)' }
]

// 所有学校选项（用于映射）
const allSchoolOptions = [
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

// WebSocket连接
const ws = ref(null)

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

// 获取权限
const fetchPermission = async () => {
  try {
    const res = await axios.get('/api/admin/permissions')
    if (res.data.code === 0) {
      const permissions = res.data.data
      campusVisitRecordPermission.value = permissions.campus_visit_record_permission || 0
    } else {
      campusVisitRecordPermission.value = 0
    }
  } catch (e) {
    campusVisitRecordPermission.value = 0
  }
}

// 获取可访问的学校列表
const fetchAccessibleSchools = async () => {
  try {
    const res = await axios.get('/api/visitrecord/admin/visit-records/accessible-schools')
    if (res.data.code === 200) {
      const accessibleSchools = res.data.data
      // 根据可访问的学校代码过滤学校选项
      schoolOptions.value = allSchoolOptions.filter(option => 
        accessibleSchools.includes(option.value)
      )
    } else {
      // 如果获取失败，使用所有学校选项
      schoolOptions.value = allSchoolOptions
    }
  } catch (e) {
    console.error('获取可访问学校列表失败:', e)
    // 如果获取失败，使用所有学校选项
    schoolOptions.value = allSchoolOptions
  }
}

// 查询列表
const getList = async () => {
  loading.value = true
  try {
    await fetchPermission()
    await fetchAccessibleSchools()
    const res = await getVisitRecordList(queryParams)
    recordList.value = res.data
    total.value = res.total
  } catch (error) {
    console.error('获取预约记录列表失败:', error)
    ElMessage.error('获取预约记录列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索按钮操作
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置按钮操作
const resetQuery = () => {
  queryParams.visitSchool = undefined
  queryParams.status = undefined
  queryParams.visitDay = undefined
  handleQuery()
}

// 多选框选中数据
const handleSelectionChange = (selection) => {
  multipleSelection.value = selection
}

// 智能全选：只选中待审核的数据
const handleSelectAllPending = () => {
  const pendingData = recordList.value.filter(item => item.status === 'PENDING')
  multipleSelection.value = pendingData
  ElMessage.success(`已选中 ${pendingData.length} 条待审核数据`)
}

// 行点击事件
const handleRowClick = (row) => {
  router.push(`/admin/visit-records/${row.visitUUID}`)
}

// 批准操作
const handleApprove = async (row) => {
  await fetchPermission()
  if (campusVisitRecordPermission.value !== 2) {
    ElMessage.warning('权限不足，无法进行操作')
    return
  }
  
  try {
    await ElMessageBox.confirm('确认批准该预约申请吗？', '提示', {
      type: 'warning'
    })
    await updateVisitStatus({
      visitUUID: row.visitUUID,
      status: 'APPROVED'
    })
    ElMessage.success('批准成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批准失败:', error)
      ElMessage.error('批准失败')
    }
  }
}

// 拒绝操作
const handleReject = async (row) => {
  await fetchPermission()
  if (campusVisitRecordPermission.value !== 2) {
    ElMessage.warning('权限不足，无法进行操作')
    return
  }
  
  try {
    await ElMessageBox.confirm('确认拒绝该预约申请吗？', '提示', {
      type: 'warning'
    })
    await updateVisitStatus({
      visitUUID: row.visitUUID,
      status: 'REJECTED'
    })
    ElMessage.success('拒绝成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝失败:', error)
      ElMessage.error('拒绝失败')
    }
  }
}

// 批量批准
const handleBatchApprove = async () => {
  await fetchPermission()
  if (campusVisitRecordPermission.value !== 2) {
    ElMessage.warning('权限不足，无法进行操作')
    return
  }
  
  try {
    await ElMessageBox.confirm('确认批量批准选中的预约申请吗？', '提示', {
      type: 'warning'
    })
    const visitUUIDs = multipleSelection.value.map(item => item.visitUUID)
    await batchUpdateVisitStatus({
      visitUUIDs,
      status: 'APPROVED'
    })
    ElMessage.success('批量批准成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量批准失败:', error)
      ElMessage.error('批量批准失败')
    }
  }
}

// 批量拒绝
const handleBatchReject = async () => {
  await fetchPermission()
  if (campusVisitRecordPermission.value !== 2) {
    ElMessage.warning('权限不足，无法进行操作')
    return
  }
  
  try {
    await ElMessageBox.confirm('确认批量拒绝选中的预约申请吗？', '提示', {
      type: 'warning'
    })
    const visitUUIDs = multipleSelection.value.map(item => item.visitUUID)
    await batchUpdateVisitStatus({
      visitUUIDs,
      status: 'REJECTED'
    })
    ElMessage.success('批量拒绝成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量拒绝失败:', error)
      ElMessage.error('批量拒绝失败')
    }
  }
}

// 分页大小改变
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  getList()
}

// 页码改变
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  getList()
}

// 返回按钮
const goBack = () => {
  router.push('/')
}

// 处理WebSocket消息
const handleWebSocketMessage = (event) => {
  const data = JSON.parse(event.data)
  if (data.type === 'VISIT_RECORD_UPDATED' || data.type === 'VISIT_RECORD_CREATED') {
    // 如果当前列表包含被更新的记录，则刷新列表
    getList()
  }
}

// WebSocket初始化
function initWebSocket() {
  ws.value = createWS({
    onMessage: handleWebSocketMessage,
    onClose: () => {
      // 可选：可加提示或其他处理
    }
  })
}

const checkScreenWidth = () => {
  isMobile.value = window.innerWidth < 768
}

onMounted(() => {
  checkScreenWidth()
  window.addEventListener('resize', checkScreenWidth)
  fetchPermission()
  fetchAccessibleSchools()
  getList()
  initWebSocket()
})

onUnmounted(() => {
  if (ws.value && ws.value._manualClose) ws.value._manualClose();
  window.removeEventListener('resize', checkScreenWidth)
})
</script>

<style scoped>
.visit-record-container {
  padding: 20px;
}

.back-button {
  margin-bottom: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form.mobile-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mobile-form-btns {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: stretch;
}

.table-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mobile-header {
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
}

.mobile-header-btns {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
  align-items: stretch;
}

.mobile-header-btns .el-button {
  display: block;
  width: 100% !important;
  margin: 0 !important;
  box-sizing: border-box;
}

.table-responsive {
  width: 100%;
  overflow-x: auto;
}

.mobile-table-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: stretch;
}

.action-btn {
  width: 100%;
  min-width: 60px;
  box-sizing: border-box;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.mobile-pagination {
  justify-content: center;
}

:deep(.el-card__header) {
  padding: 10px 20px;
}

:deep(.el-table) {
  margin-top: 10px;
}
</style> 