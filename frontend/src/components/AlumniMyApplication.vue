<template>
  <div class="my-application-root">
    <el-button class="back-btn" type="text" @click="handleBack" style="position: absolute; left: 16px; top: 16px; z-index: 10;">
      <el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回
    </el-button>
    <div class="my-application-title" style="padding-left: 80px;">我的申请</div>
    
    <!-- 搜索区域 -->
    <div class="search-area" style="margin-bottom: 16px; padding: 16px; background: #f5f7fa; border-radius: 8px;">
      <el-row :gutter="16">
        <el-col :span="8">
          <el-input
            v-model="searchForm.branchName"
            placeholder="分会名称"
            clearable
            style="width: 100%;"
          />
        </el-col>
        <el-col :span="8">
          <el-select v-model="searchForm.status" placeholder="申请状态" clearable style="width: 100%;">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-button @click="handleReset">重置</el-button>
        </el-col>
      </el-row>
    </div>
    
    <el-table :data="pagedTableData" v-loading="loading" border style="width: 100%">
      <el-table-column prop="branch_name" label="分会名称" width="180" />
      <el-table-column prop="applicant_name" label="申请人" width="120" />
      <el-table-column prop="alumni_id" label="申请人学号" width="120" />
      <el-table-column prop="status" label="申请状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="apply_time" label="申请时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.apply_time) }}
        </template>
      </el-table-column>
      <el-table-column prop="review_time" label="审核时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.review_time) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <template v-if="scope.row.creator_id === userInfo.alumniId">
            <el-button type="danger" link @click="handleDissolveChapter(scope.row)">解散分会</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      :current-page="page"
      :page-size="20"
      :total="filteredTableData.length"
      layout="prev, pager, next"
      @current-change="handlePageChange"
      :page-sizes="[20]"
      :disabled="true"
      style="margin: 16px 0; text-align: right;"
    />
    <div style="text-align:right; color:#888; margin-bottom:16px;">
      当前第{{ startItem }}到{{ endItem }}条，共{{ filteredTableData.length }}条
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getMyApplications } from '@/api/alumniSearch'
import axios from 'axios'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')

// 搜索表单
const searchForm = ref({
  branchName: '',
  status: ''
})

// WebSocket连接
const ws = ref(null)

// 获取我的申请列表
const getApplications = async () => {
  loading.value = true
  try {
    const res = await getMyApplications(userInfo.alumniId)
    if (res.data.success) {
      tableData.value = res.data.data || []
    }
  } catch (error) {
    console.error('获取我的申请列表失败', error)
    ElMessage.error('获取我的申请列表失败')
  } finally {
    loading.value = false
  }
}

// 过滤和排序后的表格数据
const filteredTableData = computed(() => {
  let filtered = [...tableData.value]
  
  // 应用搜索条件
  if (searchForm.value.branchName) {
    const name = searchForm.value.branchName.toLowerCase()
    filtered = filtered.filter(item => 
      item.branch_name && item.branch_name.toLowerCase().includes(name)
    )
  }
  
  if (searchForm.value.status) {
    filtered = filtered.filter(item => item.status === searchForm.value.status)
  }
  
  // 排序：待审核 -> 已拒绝 -> 已通过，次级按审核时间
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

// 重置搜索
const handleReset = () => {
  searchForm.value = {
    branchName: '',
    status: ''
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0')
}

// 获取状态类型
const getStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return map[status] || status
}

const handleBack = () => {
  router.push('/alumni/branch-list')
}

// WebSocket初始化
function initWebSocket() {
  // WebSocket 连接初始化
  const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const wsHost = window.location.host;
  const wsPath = '/ws/database-updates';
  ws.value = new WebSocket(wsProtocol + '//' + wsHost + wsPath);
  ws.value.onmessage = (event) => {
    const msg = event.data;
    if (["chapter application created", "chapter application reviewed"].includes(msg)) {
      getApplications();
    }
  };
  ws.value.onclose = () => {
    setTimeout(initWebSocket, 3000);
  };
}

const handleDissolveChapter = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要解散分会"${row.branch_name}"吗？此操作不可恢复！`, '警告', { type: 'warning' })
    const res = await axios.post('/api/chapters/dissolve', {
      alumniId: userInfo.alumniId,
      tagId: row.tag_id
    })
    if (res.data.success) {
      ElMessage.success('分会已解散')
      getApplications()
    } else {
      ElMessage.error(res.data.msg || '解散失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('解散分会失败', error)
      ElMessage.error('解散分会失败')
    }
  }
}

const page = ref(1)
const pageSize = 20
const pagedTableData = computed(() => {
  const start = (page.value - 1) * pageSize
  const end = start + pageSize
  return filteredTableData.value.slice(start, end)
})
const startItem = computed(() => (filteredTableData.value.length === 0 ? 0 : (page.value - 1) * pageSize + 1))
const endItem = computed(() => Math.min(page.value * pageSize, filteredTableData.value.length))
function handlePageChange(val) {
  page.value = val
}

onMounted(() => {
  getApplications()
  initWebSocket()
})

onBeforeUnmount(() => {
  if (ws.value) {
    ws.value.close();
  }
})
</script>

<style scoped>
.my-application-root {
  padding: 20px;
  background: #f5f6fa;
  min-height: 100vh;
}

.my-application-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #2563eb;
}

.search-area {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.back-btn {
  color: #2563eb;
}
</style> 