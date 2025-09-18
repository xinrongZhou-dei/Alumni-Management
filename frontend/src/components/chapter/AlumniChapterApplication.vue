<template>
  <div class="chapter-application-root">
    <el-button class="back-btn" type="text" @click="handleBack" style="position: absolute; left: 16px; top: 16px; z-index: 10;">
      <el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回
    </el-button>
    <div class="chapter-application-title" style="padding-left: 80px;">分会申请管理</div>
    
    <!-- 搜索区域 -->
    <div class="search-area" style="margin-bottom: 16px; padding: 16px; background: #f5f7fa; border-radius: 8px;">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-select v-model="searchForm.chapterId" placeholder="选择分会" clearable style="width: 100%;">
            <el-option
              v-for="chapter in myChapters"
              :key="chapter.tagId"
              :label="chapter.branchName"
              :value="chapter.tagId"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-input
            v-model="searchForm.applicantName"
            placeholder="申请人姓名"
            clearable
            style="width: 100%;"
          />
        </el-col>
        <el-col :span="6">
          <el-input
            v-model="searchForm.alumniId"
            placeholder="申请人学号"
            clearable
            style="width: 100%;"
          />
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchForm.status" placeholder="申请状态" clearable style="width: 100%;">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-col>
      </el-row>
      <el-row style="margin-top: 12px;">
        <el-col :span="24">
          <el-button @click="handleReset">重置</el-button>
        </el-col>
      </el-row>
    </div>
    
    <div style="margin-bottom: 16px;">
      <el-button type="primary" @click="handleSelectAllPending">全选待审核</el-button>
      <el-button type="success" :disabled="selected.length === 0" @click="handleBatchApprove">批量批准</el-button>
      <el-button type="danger" :disabled="selected.length === 0" @click="handleBatchReject" style="margin-left: 8px;">批量拒绝</el-button>
    </div>
    <el-table
      ref="tableRef"
      :data="pagedTableData"
      v-loading="loading"
      border
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
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
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button v-if="scope.row.status === 'PENDING'" type="success" size="small" @click="handleApprove(scope.row)">允许</el-button>
          <el-button v-if="scope.row.status === 'PENDING'" type="danger" size="small" @click="handleReject(scope.row)">拒绝</el-button>
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
import { ref, onMounted, onBeforeUnmount, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getChapterApplications, reviewApplication } from '@/api/alumniSearch'
import axios from 'axios'
import { createWS } from '@/utils/ws'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const selected = ref([])
const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')
const myChapters = ref([])
const page = ref(1)
const pageSize = 20

// 搜索表单
const searchForm = ref({
  chapterId: '',
  applicantName: '',
  alumniId: '',
  status: ''
})

// WebSocket连接
const ws = ref(null)

// 获取当前用户管理的分会列表
const getMyChapters = async () => {
  try {
    const res = await axios.get('/api/chapters/my', { params: { alumniId: userInfo.alumniId } })
    myChapters.value = res.data.data || []
  } catch (error) {
    console.error('获取我的分会失败', error)
  }
}

// 获取分会申请列表
const getApplications = async () => {
  loading.value = true
  try {
    // 获取当前用户创建的分会列表
    const chaptersRes = await axios.get('/api/chapters/my', { params: { alumniId: userInfo.alumniId } })
    const chapters = chaptersRes.data.data || []
    
    if (chapters.length === 0) {
      tableData.value = []
      return
    }
    
    // 获取所有分会的申请列表
    const allApplications = []
    for (const chapter of chapters) {
      const res = await getChapterApplications(chapter.tagId)
      if (res.data.success && res.data.data) {
        allApplications.push(...res.data.data)
      }
    }
    
    tableData.value = allApplications
  } catch (error) {
    console.error('获取申请列表失败', error)
    ElMessage.error('获取申请列表失败')
  } finally {
    loading.value = false
  }
}

// 过滤和排序后的表格数据
const filteredTableData = computed(() => {
  let filtered = [...tableData.value]
  
  // 应用搜索条件
  if (searchForm.value.chapterId) {
    filtered = filtered.filter(item => item.tag_id === searchForm.value.chapterId)
  }
  
  if (searchForm.value.applicantName) {
    const name = searchForm.value.applicantName.toLowerCase()
    filtered = filtered.filter(item => 
      item.applicant_name && item.applicant_name.toLowerCase().includes(name)
    )
  }
  
  if (searchForm.value.alumniId) {
    const id = searchForm.value.alumniId.toLowerCase()
    filtered = filtered.filter(item => 
      item.alumni_id && item.alumni_id.toLowerCase().includes(id)
    )
  }
  
  if (searchForm.value.status) {
    filtered = filtered.filter(item => item.status === searchForm.value.status)
  }
  
  // 排序：待审核 -> 已拒绝 -> 已通过，次级按申请时间
  const statusOrder = { 'PENDING': 1, 'REJECTED': 2, 'APPROVED': 3 }
  filtered.sort((a, b) => {
    const statusDiff = statusOrder[a.status] - statusOrder[b.status]
    if (statusDiff !== 0) return statusDiff
    
    // 次级排序：申请时间
    const timeA = new Date(a.apply_time || 0)
    const timeB = new Date(b.apply_time || 0)
    return timeB - timeA // 最新的在前
  })
  
  return filtered
})

// 重置搜索
const handleReset = () => {
  searchForm.value = {
    chapterId: '',
    applicantName: '',
    alumniId: '',
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

// 选择变化
const handleSelectionChange = (selection) => {
  selected.value = selection
}

// 智能全选：只选中待审核的数据
const handleSelectAllPending = async () => {
  // 先清空所有选中
  if (tableRef.value) {
    tableRef.value.clearSelection();
  }
  // 只选中待审核
  const pendingData = filteredTableData.value.filter(item => item.status === 'PENDING');
  await nextTick(); // 等待DOM更新
  pendingData.forEach(row => {
    tableRef.value.toggleRowSelection(row, true);
  });
  // 让selected.value和UI同步
  selected.value = pendingData;
  ElMessage.success(`已选中 ${pendingData.length} 条待审核数据`);
}

// 批准申请
const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确定要批准该申请吗？', '提示', { type: 'warning' });
    await axios.post('/api/chapters/batch-review', {
      tagId: row.tag_id,
      applicationIds: [row.id],
      status: 'APPROVED'
    });
    ElMessage.success('批准成功');
    getApplications();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批准申请失败', error);
      ElMessage.error('批准申请失败');
    }
  }
}

// 拒绝申请
const handleReject = async (row) => {
  try {
    await ElMessageBox.confirm('确定要拒绝该申请吗？', '提示', { type: 'warning' });
    await axios.post('/api/chapters/batch-review', {
      tagId: row.tag_id,
      applicationIds: [row.id],
      status: 'REJECTED'
    });
    ElMessage.success('拒绝成功');
    getApplications();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝申请失败', error);
      ElMessage.error('拒绝申请失败');
    }
  }
}

// 批量批准
const handleBatchApprove = async () => {
  try {
    await ElMessageBox.confirm(`确定要批准选中的 ${selected.value.length} 个申请吗？`, '提示', {
      type: 'warning'
    })
    
    // 按分会分组申请
    const applicationsByChapter = {}
    selected.value.forEach(row => {
      if (!applicationsByChapter[row.tag_id]) {
        applicationsByChapter[row.tag_id] = []
      }
      applicationsByChapter[row.tag_id].push(row.id)
    })
    
    // 为每个分会发送一个批量请求
    const promises = Object.entries(applicationsByChapter).map(([tagId, applicationIds]) => 
      axios.post('/api/chapters/batch-review', {
        tagId: tagId,
        applicationIds: applicationIds,
        status: 'APPROVED'
      })
    )
    
    await Promise.all(promises)
    ElMessage.success('批量批准成功')
    getApplications()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量批准失败', error)
      ElMessage.error('批量批准失败')
    }
  }
}

// 批量拒绝
const handleBatchReject = async () => {
  try {
    await ElMessageBox.confirm(`确定要拒绝选中的 ${selected.value.length} 个申请吗？`, '提示', {
      type: 'warning'
    })
    
    // 按分会分组申请
    const applicationsByChapter = {}
    selected.value.forEach(row => {
      if (!applicationsByChapter[row.tag_id]) {
        applicationsByChapter[row.tag_id] = []
      }
      applicationsByChapter[row.tag_id].push(row.id)
    })
    
    // 为每个分会发送一个批量请求
    const promises = Object.entries(applicationsByChapter).map(([tagId, applicationIds]) => 
      axios.post('/api/chapters/batch-review', {
        tagId: tagId,
        applicationIds: applicationIds,
        status: 'REJECTED'
      })
    )
    
    await Promise.all(promises)
    ElMessage.success('批量拒绝成功')
    getApplications()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量拒绝失败', error)
      ElMessage.error('批量拒绝失败')
    }
  }
}

const handleBack = () => {
  router.push('/alumni/branch-list')
}

// WebSocket初始化
function initWebSocket() {
  ws.value = createWS({
    onMessage: (event) => {
      const msg = event.data;
      if (["chapter application created", "chapter application reviewed"].includes(msg)) {
        getApplications();
      }
    },
    onClose: () => {
      // 可选：可加提示或其他处理
    }
  })
}

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

const tableRef = ref(null)

onMounted(() => {
  getMyChapters()
  getApplications()
  initWebSocket()
})

onBeforeUnmount(() => {
  if (ws.value && ws.value._manualClose) ws.value._manualClose();
})
</script>

<style scoped>
.chapter-application-root {
  padding: 20px;
  background: #f5f6fa;
  min-height: 100vh;
}

.chapter-application-title {
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