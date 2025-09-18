<template>
  <div class="chapter-review-root">
    <el-button class="back-btn" type="text" @click="handleBack" :style="isMobile ? 'position: static; margin-bottom: 16px;' : 'position: absolute; left: 16px; top: 16px; z-index: 10;'">
      <el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回
    </el-button>
    <div class="chapter-review-title" :style="isMobile ? '' : 'padding-left: 80px;'">分会审核</div>
    <div class="action-buttons">
      <el-button type="primary" @click="handleSelectAllPending" class="action-btn">全选待审核</el-button>
      <el-button type="success" :disabled="selected.length === 0" @click="handleBatchApprove" class="action-btn">批量批准</el-button>
      <el-button type="danger" :disabled="selected.length === 0" @click="handleBatchReject" class="action-btn">批量拒绝</el-button>
    </div>
    <el-table
      ref="tableRef"
      :data="pagedTableData"
      v-loading="loading"
      border
      style="width: 100%"
      @selection-change="handleSelectionChange"
      :size="isMobile ? 'small' : 'default'"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="branch_name" label="分会名称" :width="isMobile ? '' : '180'" />
      <el-table-column prop="creator_name" label="创建人姓名" :width="isMobile ? '' : '120'" />
      <el-table-column prop="alumni_id" label="创建人学号" :width="isMobile ? '' : '120'" />
      <el-table-column prop="create_time" label="创建时间" :width="isMobile ? '' : '150'">
        <template #default="scope">
          {{ formatDate(scope.row.create_time) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="审核状态" :width="isMobile ? '' : '100'">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" :width="isMobile ? '' : '180'" v-if="chapterReviewPermission === 2">
        <template #default="scope">
          <div class="operation-btns">
            <el-button v-if="scope.row.status === 'pending'" type="success" size="small" @click="handleApprove(scope.row)">批准</el-button>
            <el-button v-if="scope.row.status === 'pending'" type="danger" size="small" @click="handleReject(scope.row)">拒绝</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      :current-page="page"
      :page-size="20"
      :total="tableData.length"
      :layout="isMobile ? 'prev, pager, next' : 'prev, pager, next'"
      @current-change="handlePageChange"
      :page-sizes="[20]"
      :disabled="true"
      class="pagination"
    />
    <div class="pagination-info">
      当前第{{ startItem }}到{{ endItem }}条，共{{ tableData.length }}条
    </div>

    <!-- 申请加入分会表格 -->
    <div class="application-section">
      <div class="section-title">申请加入分会</div>
      
      <!-- 搜索区域 -->
      <div class="search-area">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="24" :md="8" :lg="6" class="search-item">
            <el-select v-model="applicationSearchForm.chapterId" placeholder="选择分会" clearable style="width: 100%;">
              <el-option
                v-for="chapter in allChapters"
                :key="chapter.tagId"
                :label="chapter.branchName"
                :value="chapter.tagId"
              />
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="24" :md="8" :lg="6" class="search-item">
            <el-select v-model="applicationSearchForm.status" placeholder="申请状态" clearable style="width: 100%;">
              <el-option label="待审核" value="PENDING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已拒绝" value="REJECTED" />
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="24" :md="8" :lg="6" class="search-item">
            <el-input
              v-model="applicationSearchForm.alumniId"
              placeholder="申请人学号"
              clearable
              style="width: 100%;"
            />
          </el-col>
          <el-col :xs="24" :sm="24" :md="8" :lg="6" class="search-item">
            <el-button @click="handleApplicationReset">重置</el-button>
          </el-col>
        </el-row>
      </div>
      
      <el-table 
        :data="pagedApplicationData" 
        v-loading="applicationLoading" 
        border 
        style="width: 100%"
        :size="isMobile ? 'small' : 'default'"
      >
        <el-table-column prop="branch_name" label="分会名称" :width="isMobile ? '' : '180'" />
        <el-table-column prop="applicant_name" label="申请人" :width="isMobile ? '' : '120'" />
        <el-table-column prop="alumni_id" label="申请人学号" :width="isMobile ? '' : '120'" />
        <el-table-column prop="status" label="申请状态" :width="isMobile ? '' : '100'">
          <template #default="scope">
            <el-tag :type="getApplicationStatusType(scope.row.status)">{{ getApplicationStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="apply_time" label="申请时间" :width="isMobile ? '' : '180'">
          <template #default="scope">
            {{ formatDate(scope.row.apply_time) }}
          </template>
        </el-table-column>
        <el-table-column prop="review_time" label="审核时间" :width="isMobile ? '' : '180'">
          <template #default="scope">
            {{ formatDate(scope.row.review_time) }}
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        :current-page="applicationPage"
        :page-size="20"
        :total="filteredApplicationData.length"
        :layout="isMobile ? 'prev, pager, next' : 'prev, pager, next'"
        @current-change="handleApplicationPageChange"
        :page-sizes="[20]"
        :disabled="true"
        class="pagination"
      />
      <div class="pagination-info">
        当前第{{ applicationStartItem }}到{{ applicationEndItem }}条，共{{ filteredApplicationData.length }}条
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import axios from 'axios'
import { createWS } from '@/utils/ws'

const router = useRouter()
const tableData = ref([])
const loading = ref(false)
const selected = ref([])
const applicationData = ref([])
const applicationLoading = ref(false)
const allChapters = ref([])
const page = ref(1)
const pageSize = 20
const chapterReviewPermission = ref(0)

// 判断是否为移动设备
const isMobile = ref(window.innerWidth <= 768)

// 监听窗口大小变化
window.addEventListener('resize', () => {
  isMobile.value = window.innerWidth <= 768
})

// 申请搜索表单
const applicationSearchForm = ref({
  chapterId: '',
  status: '',
  alumniId: ''
})

// WebSocket连接
const ws = ref(null)

const getStatusText = (status) => {
  const map = {
    pending: '待审核',
    approved: '已批准',
    rejected: '已拒绝'
  }
  return map[status] || status
}
const getStatusType = (status) => {
  const map = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return map[status] || 'info'
}
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
}
const fetchPermission = async () => {
  try {
    const res = await axios.get('/api/admin/permissions')
    if (res.data.code === 0) {
      const permissions = res.data.data
      chapterReviewPermission.value = permissions.chapter_review_permission || 0
    } else {
      chapterReviewPermission.value = 0
    }
  } catch (e) {
    chapterReviewPermission.value = 0
  }
}
const fetchData = async () => {
  loading.value = true
  try {
    await fetchPermission()
    const res = await axios.get('/api/chapters/review-list')
    tableData.value = res.data.data || []
  } catch (e) {
    ElMessage.error('获取分会审核列表失败')
  } finally {
    loading.value = false
  }
}
const handleSelectionChange = (val) => {
  selected.value = val
}

// 智能全选：只选中待审核的数据
const tableRef = ref(null)

const handleSelectAllPending = async () => {
  // 先清空所有选中
  if (tableRef.value) {
    tableRef.value.clearSelection();
  }
  // 只选中待审核
  const pendingData = tableData.value.filter(item => item.status === 'pending');
  await nextTick(); // 等待DOM更新
  pendingData.forEach(row => {
    tableRef.value.toggleRowSelection(row, true);
  });
  selected.value = pendingData;
  ElMessage.success(`已选中 ${pendingData.length} 条待审核数据`);
}

const handleApprove = async (row) => {
  await fetchPermission()
  if (chapterReviewPermission.value !== 2) {
    ElMessage.warning('权限不足，无法进行操作')
    return
  }
  await updateStatus([row.tag_id], 'approved')
}
const handleReject = async (row) => {
  await fetchPermission()
  if (chapterReviewPermission.value !== 2) {
    ElMessage.warning('权限不足，无法进行操作')
    return
  }
  await updateStatus([row.tag_id], 'rejected')
}
const handleBatchApprove = async () => {
  await fetchPermission()
  if (chapterReviewPermission.value !== 2) {
    ElMessage.warning('权限不足，无法进行操作')
    return
  }
  await updateStatus(selected.value.map(r => r.tag_id), 'approved')
}
const handleBatchReject = async () => {
  await fetchPermission()
  if (chapterReviewPermission.value !== 2) {
    ElMessage.warning('权限不足，无法进行操作')
    return
  }
  await updateStatus(selected.value.map(r => r.tag_id), 'rejected')
}
const updateStatus = async (tagIds, status) => {
  try {
    await fetchPermission()
    if (chapterReviewPermission.value !== 2) {
      ElMessage.warning('权限不足，无法进行操作')
      return
    }
    await axios.post('/api/chapters/review-status', { tagIds, status })
    ElMessage.success('操作成功')
    fetchData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}
const handleBack = () => {
  router.push('../')
}

// WebSocket初始化
function initWebSocket() {
  ws.value = createWS({
    onMessage: (event) => {
      const msg = event.data;
      if (["chapter changed", "alumni changed"].includes(msg)) {
        getChapters();
      }
    },
    onClose: () => {
      // 可选：可加提示或其他处理
    }
  })
}

// 获取申请列表
const fetchApplicationData = async () => {
  applicationLoading.value = true
  try {
    const res = await axios.get('/api/chapters/applications')
    applicationData.value = res.data.data || []
  } catch (e) {
    ElMessage.error('获取申请列表失败')
  } finally {
    applicationLoading.value = false
  }
}

// 获取所有分会列表
const getAllChapters = async () => {
  try {
    const res = await axios.get('/api/chapters')
    if (res.data.success) {
      allChapters.value = res.data.data || []
    } else {
      ElMessage.error(res.data.msg || '获取分会列表失败')
    }
  } catch (error) {
    console.error('获取所有分会失败', error)
    ElMessage.error('获取分会列表失败')
  }
}

// 过滤和排序后的申请数据
const filteredApplicationData = computed(() => {
  let filtered = [...applicationData.value]
  
  // 应用搜索条件
  if (applicationSearchForm.value.chapterId) {
    filtered = filtered.filter(item => item.tag_id === applicationSearchForm.value.chapterId)
  }
  
  if (applicationSearchForm.value.status) {
    filtered = filtered.filter(item => item.status === applicationSearchForm.value.status)
  }
  
  if (applicationSearchForm.value.alumniId) {
    const id = applicationSearchForm.value.alumniId.toLowerCase()
    filtered = filtered.filter(item => 
      item.alumni_id && item.alumni_id.toLowerCase().includes(id)
    )
  }
  
  // 排序：待审核 -> 已拒绝 -> 已通过，次级按审核时间（申请时间）
  const statusOrder = { 'PENDING': 1, 'REJECTED': 2, 'APPROVED': 3 }
  filtered.sort((a, b) => {
    const statusDiff = statusOrder[a.status] - statusOrder[b.status]
    if (statusDiff !== 0) return statusDiff
    
    // 次级排序：审核时间（如果没有审核时间则用申请时间）
    const timeA = new Date(a.review_time || a.apply_time || 0)
    const timeB = new Date(b.review_time || b.apply_time || 0)
    return timeB - timeA // 最新的在前
  })
  
  return filtered
})

// 获取申请状态类型
const getApplicationStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return map[status] || 'info'
}

// 获取申请状态文本
const getApplicationStatusText = (status) => {
  const map = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return map[status] || status
}

// 申请重置搜索
const handleApplicationReset = () => {
  applicationSearchForm.value = {
    chapterId: '',
    status: '',
    alumniId: ''
  }
}

const pagedTableData = computed(() => {
  const start = (page.value - 1) * pageSize
  const end = start + pageSize
  return tableData.value.slice(start, end)
})
const startItem = computed(() => (tableData.value.length === 0 ? 0 : (page.value - 1) * pageSize + 1))
const endItem = computed(() => Math.min(page.value * pageSize, tableData.value.length))
function handlePageChange(val) {
  page.value = val
}

const applicationPage = ref(1)
const applicationPageSize = 20
const pagedApplicationData = computed(() => {
  const start = (applicationPage.value - 1) * applicationPageSize
  const end = start + applicationPageSize
  return filteredApplicationData.value.slice(start, end)
})
const applicationStartItem = computed(() => (filteredApplicationData.value.length === 0 ? 0 : (applicationPage.value - 1) * applicationPageSize + 1))
const applicationEndItem = computed(() => Math.min(applicationPage.value * applicationPageSize, filteredApplicationData.value.length))
function handleApplicationPageChange(val) {
  applicationPage.value = val
}

onMounted(() => {
  fetchPermission()
  fetchData()
  fetchApplicationData()
  getAllChapters()
  initWebSocket()
})

onBeforeUnmount(() => {
  if (ws.value && ws.value._manualClose) ws.value._manualClose();
})
</script>

<style scoped>
.chapter-review-root {
  padding: 32px;
  background: #fff;
  border-radius: 12px;
  min-height: 600px;
}

.chapter-review-title {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 24px;
}

.search-area {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.action-buttons {
  margin-bottom: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.action-btn {
  min-width: 120px;
}

.operation-btns {
  display: flex;
  gap: 8px;
}

.pagination {
  margin: 16px 0;
  text-align: right;
}

.pagination-info {
  text-align: right;
  color: #888;
  margin-bottom: 16px;
}

.application-section {
  margin-top: 40px;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 16px;
  color: #2563eb;
}

.search-item {
  margin-bottom: 8px;
}

@media (max-width: 768px) {
  .chapter-review-root {
    padding: 16px;
    border-radius: 8px;
  }

  .chapter-review-title {
    font-size: 18px;
    margin-bottom: 16px;
    text-align: center;
  }

  .search-area {
    padding: 12px;
  }

  .action-buttons {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }

  .action-btn {
    width: 100%;
    margin-left: 0 !important;
    margin-right: 0 !important;
  }

  .section-title {
    font-size: 16px;
    text-align: center;
  }

  .pagination {
    display: flex;
    justify-content: center;
  }

  .pagination-info {
    text-align: center;
    font-size: 12px;
  }

  .operation-btns {
    display: flex;
    flex-direction: row;
    justify-content: center;
    gap: 8px;
  }
}
</style> 