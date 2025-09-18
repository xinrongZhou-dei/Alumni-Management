<template>
  <div class="branch-list-root">
    <el-button class="back-btn" type="text" @click="handleBack" style="position: absolute; left: 16px; top: 16px; z-index: 10;">
      <el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回
    </el-button>
    <div class="branch-list-header" style="padding-left: 80px;">
      <div class="branch-list-title">分会管理</div>
      <div class="branch-list-actions">
        <el-button type="primary" @click="showCreateDialog">创建分会</el-button>
      </div>
    </div>
    
    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="branch-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="分会列表" name="branch-list">
        <div class="branch-list-content">
          <el-table :data="branches" style="width: 100%" v-loading="loading">
            <el-table-column prop="branch_name" label="分会名称" width="180" />
            <el-table-column prop="alumni_id" label="负责人" width="120" />
            <el-table-column prop="total" label="分会人数" width="100">
              <template #default="scope">
                {{ scope.row.total || 0 }}
              </template>
            </el-table-column>
            <el-table-column prop="create_time" label="创建时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.create_time) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <template v-if="scope.row.alumni_id === userInfo.alumniId">
                  <el-button type="danger" link @click="handleDissolveChapter(scope.row)">解散分会</el-button>
                </template>
                <template v-else-if="isInBranch(scope.row.tag_id)">
                  <el-button type="primary" link @click="handleViewMembers(scope.row)">查看成员</el-button>
                  <el-button type="danger" link @click="handleLeaveChapter(scope.row)">退出分会</el-button>
                </template>
                <template v-else-if="getApplicationStatus(scope.row.tag_id) === 'PENDING'">
                  <el-button type="warning" link disabled>审核中</el-button>
                </template>
                <template v-else-if="getApplicationStatus(scope.row.tag_id) === 'REJECTED'">
                  <el-button type="danger" link @click="handleApplyChapter(scope.row)">申请被拒绝</el-button>
                </template>
                <template v-else>
                  <el-button type="primary" link @click="handleApplyChapter(scope.row)">申请加入</el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="我的分会" name="my-branches">
        <div class="branch-list-content">
          <el-table :data="myBranches" style="width: 100%" v-loading="loading">
            <el-table-column prop="branch_name" label="分会名称" width="180" />
            <el-table-column prop="alumni_id" label="负责人" width="120" />
            <el-table-column prop="total" label="分会人数" width="100">
              <template #default="scope">
                {{ scope.row.total || 0 }}
              </template>
            </el-table-column>
            <el-table-column prop="create_time" label="创建时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.create_time) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="审核状态" width="100">
              <template #default="scope">
                {{ getStatusText(scope.row.status) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <template v-if="scope.row.alumni_id === userInfo.alumniId">
                  <el-button type="danger" link @click="handleDissolveChapter(scope.row)">解散分会</el-button>
                </template>
                <template v-else>
                  <el-button type="primary" link @click="handleViewMembers(scope.row)">查看成员</el-button>
                  <el-button type="danger" link @click="handleLeaveChapter(scope.row)">退出分会</el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      
      <el-tab-pane name="chapter-applications">
        <template #label>
          <span style="position: relative;">
            分会申请
            <span v-if="pendingApplicationCount > 0" class="badge">{{ pendingApplicationCount }}</span>
          </span>
        </template>
        
        <!-- 分会申请管理内容 -->
        <div class="chapter-application-content">
          <!-- 搜索区域 -->
          <div class="search-area" style="margin-bottom: 16px; padding: 16px; background: #f5f7fa; border-radius: 8px;">
            <el-row :gutter="16">
              <el-col :span="6">
                <el-select v-model="chapterSearchForm.chapterId" placeholder="选择分会" clearable style="width: 100%;">
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
                  v-model="chapterSearchForm.applicantName"
                  placeholder="申请人姓名"
                  clearable
                  style="width: 100%;"
                />
              </el-col>
              <el-col :span="6">
                <el-input
                  v-model="chapterSearchForm.alumniId"
                  placeholder="申请人学号"
                  clearable
                  style="width: 100%;"
                />
              </el-col>
              <el-col :span="6">
                <el-select v-model="chapterSearchForm.status" placeholder="申请状态" clearable style="width: 100%;">
                  <el-option label="待审核" value="PENDING" />
                  <el-option label="已通过" value="APPROVED" />
                  <el-option label="已拒绝" value="REJECTED" />
                </el-select>
              </el-col>
            </el-row>
            <el-row style="margin-top: 12px;">
              <el-col :span="24">
                <el-button @click="handleChapterSearchReset">重置</el-button>
              </el-col>
            </el-row>
          </div>
          
          <div style="margin-bottom: 16px;">
            <el-button type="primary" @click="handleSelectAllPending">全选待审核</el-button>
            <el-button type="success" :disabled="selectedApplications.length === 0" @click="handleBatchApprove">批量批准</el-button>
            <el-button type="danger" :disabled="selectedApplications.length === 0" @click="handleBatchReject" style="margin-left: 8px;">批量拒绝</el-button>
          </div>
          
          <el-table
            ref="chapterTableRef"
            :data="pagedChapterApplications"
            v-loading="chapterLoading"
            border
            style="width: 100%"
            @selection-change="handleChapterSelectionChange"
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
            :current-page="chapterPage"
            :page-size="20"
            :total="filteredChapterApplications.length"
            layout="prev, pager, next"
            @current-change="handleChapterPageChange"
            :page-sizes="[20]"
            :disabled="true"
            style="margin: 16px 0; text-align: right;"
          />
          <div style="text-align:right; color:#888; margin-bottom:16px;">
            当前第{{ chapterStartItem }}到{{ chapterEndItem }}条，共{{ filteredChapterApplications.length }}条
          </div>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="我的申请" name="my-applications">
        <!-- 我的申请内容 -->
        <div class="my-application-content">
          <!-- 搜索区域 -->
          <div class="search-area" style="margin-bottom: 16px; padding: 16px; background: #f5f7fa; border-radius: 8px;">
            <el-row :gutter="16">
              <el-col :span="8">
                <el-input
                  v-model="myApplicationSearchForm.branchName"
                  placeholder="分会名称"
                  clearable
                  style="width: 100%;"
                />
              </el-col>
              <el-col :span="8">
                <el-select v-model="myApplicationSearchForm.status" placeholder="申请状态" clearable style="width: 100%;">
                  <el-option label="待审核" value="PENDING" />
                  <el-option label="已通过" value="APPROVED" />
                  <el-option label="已拒绝" value="REJECTED" />
                </el-select>
              </el-col>
              <el-col :span="8">
                <el-button @click="handleMyApplicationSearchReset">重置</el-button>
              </el-col>
            </el-row>
          </div>
          
          <el-table :data="pagedMyApplications" v-loading="myApplicationLoading" border style="width: 100%">
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
            :current-page="myApplicationPage"
            :page-size="20"
            :total="filteredMyApplications.length"
            layout="prev, pager, next"
            @current-change="handleMyApplicationPageChange"
            :page-sizes="[20]"
            :disabled="true"
            style="margin: 16px 0; text-align: right;"
          />
          <div style="text-align:right; color:#888; margin-bottom:16px;">
            当前第{{ myApplicationStartItem }}到{{ myApplicationEndItem }}条，共{{ filteredMyApplications.length }}条
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 创建分会对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建分会"
      width="500px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="100px"
        class="create-form"
      >
        <el-form-item label="创建人学号" prop="alumniId">
          <el-input 
            v-model="createForm.alumniId" 
            disabled
          />
        </el-form-item>
        <el-form-item label="分会名称" prop="branchName">
          <el-input 
            v-model="createForm.branchName" 
            placeholder="请输入分会名称"
            :maxlength="64"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreateSubmit" :loading="submitting">
            确认创建
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import axios from 'axios'
import { createWS } from '@/utils/ws'

const router = useRouter()
const loading = ref(false)
const branches = ref([])
const myBranches = ref([])
const activeTab = ref('branch-list')
const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')
const joinedTagIds = ref([])
const applicationStatus = ref({})
const pendingApplicationCount = ref(0)

// 分会申请相关变量
const chapterLoading = ref(false)
const chapterApplications = ref([])
const myChapters = ref([])
const selectedApplications = ref([])
const chapterPage = ref(1)
const chapterPageSize = 20

// 我的申请相关变量
const myApplicationLoading = ref(false)
const myApplications = ref([])
const myApplicationPage = ref(1)
const myApplicationPageSize = 20

// 搜索表单
const chapterSearchForm = ref({
  chapterId: '',
  applicantName: '',
  alumniId: '',
  status: ''
})

const myApplicationSearchForm = ref({
  branchName: '',
  status: ''
})

// WebSocket连接
const ws = ref(null)

// 判断是否为管理员
const isAdmin = computed(() => {
  const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')
  return userInfo.role === 'admin'
})

// 分会申请相关计算属性
const filteredChapterApplications = computed(() => {
  let filtered = [...chapterApplications.value]
  
  // 应用搜索条件
  if (chapterSearchForm.value.chapterId) {
    filtered = filtered.filter(item => item.tag_id === chapterSearchForm.value.chapterId)
  }
  
  if (chapterSearchForm.value.applicantName) {
    const name = chapterSearchForm.value.applicantName.toLowerCase()
    filtered = filtered.filter(item => 
      item.applicant_name && item.applicant_name.toLowerCase().includes(name)
    )
  }
  
  if (chapterSearchForm.value.alumniId) {
    const id = chapterSearchForm.value.alumniId.toLowerCase()
    filtered = filtered.filter(item => 
      item.alumni_id && item.alumni_id.toLowerCase().includes(id)
    )
  }
  
  if (chapterSearchForm.value.status) {
    filtered = filtered.filter(item => item.status === chapterSearchForm.value.status)
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

const pagedChapterApplications = computed(() => {
  const start = (chapterPage.value - 1) * chapterPageSize
  const end = start + chapterPageSize
  return filteredChapterApplications.value.slice(start, end)
})

const chapterStartItem = computed(() => (filteredChapterApplications.value.length === 0 ? 0 : (chapterPage.value - 1) * chapterPageSize + 1))
const chapterEndItem = computed(() => Math.min(chapterPage.value * chapterPageSize, filteredChapterApplications.value.length))

// 我的申请相关计算属性
const filteredMyApplications = computed(() => {
  let filtered = [...myApplications.value]
  
  // 应用搜索条件
  if (myApplicationSearchForm.value.branchName) {
    const name = myApplicationSearchForm.value.branchName.toLowerCase()
    filtered = filtered.filter(item => 
      item.branch_name && item.branch_name.toLowerCase().includes(name)
    )
  }
  
  if (myApplicationSearchForm.value.status) {
    filtered = filtered.filter(item => item.status === myApplicationSearchForm.value.status)
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

const pagedMyApplications = computed(() => {
  const start = (myApplicationPage.value - 1) * myApplicationPageSize
  const end = start + myApplicationPageSize
  return filteredMyApplications.value.slice(start, end)
})

const myApplicationStartItem = computed(() => (filteredMyApplications.value.length === 0 ? 0 : (myApplicationPage.value - 1) * myApplicationPageSize + 1))
const myApplicationEndItem = computed(() => Math.min(myApplicationPage.value * myApplicationPageSize, filteredMyApplications.value.length))

// 表格引用
const chapterTableRef = ref(null)

// 获取分会列表
const getBranches = async () => {
  loading.value = true
  try {
    const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')
    const res = await axios.get('/api/chapters/list-with-joined', { params: { alumniId: userInfo.alumniId } })
    branches.value = res.data.data || []
    joinedTagIds.value = (res.data.joinedTagIds || []).map(String)
    applicationStatus.value = res.data.applicationStatus || {}
  } catch (error) {
    console.error('获取分会列表失败', error)
    ElMessage.error('获取分会列表失败')
  } finally {
    loading.value = false
  }
}

// 获取我的分会
const getMyBranches = async () => {
  loading.value = true
  try {
    const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')
    const res = await axios.get('/api/chapters/my', { params: { alumniId: userInfo.alumniId } })
    myBranches.value = (res.data.data || []).map(item => ({
      tag_id: item.tagId,
      branch_name: item.branchName,
      alumni_id: item.alumniId,
      status: item.status,
      total: item.total || 0,
      create_time: item.createTime,
      update_time: item.updateTime
    }))
  } catch (error) {
    console.error('获取我的分会失败', error)
    ElMessage.error('获取我的分会失败')
  } finally {
    loading.value = false
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
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    pending: '待审核',
    approved: '已批准',
    rejected: '已拒绝'
  }
  return map[status] || status
}

// 查看详情
const handleViewDetail = (row) => {
  router.push(`/alumni/branch-detail/${row.tag_id}`)
}

// 创建分会相关
const createDialogVisible = ref(false)
const createFormRef = ref(null)
const submitting = ref(false)
const createForm = ref({
  alumniId: '',
  branchName: ''
})

// 表单验证规则
const createRules = {
  branchName: [
    { required: true, message: '请输入分会名称', trigger: 'blur' },
    { min: 2, max: 64, message: '长度在 2 到 64 个字符', trigger: 'blur' }
  ]
}

// 显示创建对话框
const showCreateDialog = () => {
  // 获取当前用户信息
  const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')
  createForm.value.alumniId = userInfo.alumniId || ''
  createDialogVisible.value = true
}

// 提交创建
const handleCreateSubmit = async () => {
  if (!createFormRef.value) return
  
  try {
    await createFormRef.value.validate()
    submitting.value = true
    
    // 调用创建分会接口
    const res = await axios.post('/api/chapters', {
      alumniId: createForm.value.alumniId,
      branchName: createForm.value.branchName
    })
    
    if (res.data.success) {
      ElMessage.success('创建成功')
      createDialogVisible.value = false
      getBranches() // 刷新列表
    } else {
      ElMessage.error(res.data.message || '创建失败')
    }
  } catch (error) {
    console.error('创建分会失败', error)
    ElMessage.error(error.response?.data || '创建分会失败')
  } finally {
    submitting.value = false
  }
}



// 处理申请加入分会
const handleApplyChapter = async (row) => {
  try {
    const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')
    const res = await axios.post('/api/chapters/apply', {
      tagId: row.tag_id,
      alumniId: userInfo.alumniId
    })
    
    if (res.data.code === 0) {
      ElMessage.success(res.data.msg || '申请成功，请等待审核')
      // 更新申请状态
      applicationStatus.value[row.tag_id] = 'PENDING'
    } else {
      ElMessage.error(res.data.msg || '申请失败')
    }
  } catch (error) {
    console.error('申请加入分会失败', error)
    ElMessage.error('申请加入分会失败')
  }
}

// 查看分会成员
const handleViewMembers = (row) => {
  // 根据当前标签页设置来源标识
  const from = activeTab.value === 'my-branches' ? 'my-chapters' : 'chapter-list'
  sessionStorage.setItem('chapter_members_from', from)
  router.push(`/alumni/chapter-members/${row.tag_id}`)
}

// 退出分会
const handleLeaveChapter = async (row) => {
  try {
    await ElMessageBox.confirm('确定要退出该分会吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    
    const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')
    const res = await axios.post('/api/chapters/leave', {
      alumniId: userInfo.alumniId,
      tagId: row.tag_id || row.tagId
    })
    
    if (res.data.success) {
      ElMessage.success('退出成功')
      // 根据当前标签页刷新数据
      if (activeTab.value === 'my-branches') {
        getMyBranches()
      } else {
        getBranches()
      }
    } else {
      ElMessage.error(res.data.msg || '退出失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出分会失败', error)
      ElMessage.error('退出分会失败')
    }
  }
}

const handleBack = () => {
  router.push('/')
}

const isInBranch = (tagId) => joinedTagIds.value.includes(String(tagId))

// 分会申请相关方法
const handleChapterSearchReset = () => {
  chapterSearchForm.value = {
    chapterId: '',
    applicantName: '',
    alumniId: '',
    status: ''
  }
}

const handleChapterSelectionChange = (selection) => {
  selectedApplications.value = selection
}

const handleChapterPageChange = (val) => {
  chapterPage.value = val
}

// 智能全选：只选中待审核的数据
const handleSelectAllPending = async () => {
  // 先清空所有选中
  if (chapterTableRef.value) {
    chapterTableRef.value.clearSelection();
  }
  // 只选中待审核
  const pendingData = filteredChapterApplications.value.filter(item => item.status === 'PENDING');
  await nextTick(); // 等待DOM更新
  pendingData.forEach(row => {
    chapterTableRef.value.toggleRowSelection(row, true);
  });
  // 让selectedApplications.value和UI同步
  selectedApplications.value = pendingData;
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
    getChapterApplications();
    getPendingApplicationCount();
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
    getChapterApplications();
    getPendingApplicationCount();
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
    await ElMessageBox.confirm(`确定要批准选中的 ${selectedApplications.value.length} 个申请吗？`, '提示', {
      type: 'warning'
    })
    
    // 按分会分组申请
    const applicationsByChapter = {}
    selectedApplications.value.forEach(row => {
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
    getChapterApplications()
    getPendingApplicationCount()
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
    await ElMessageBox.confirm(`确定要拒绝选中的 ${selectedApplications.value.length} 个申请吗？`, '提示', {
      type: 'warning'
    })
    
    // 按分会分组申请
    const applicationsByChapter = {}
    selectedApplications.value.forEach(row => {
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
    getChapterApplications()
    getPendingApplicationCount()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量拒绝失败', error)
      ElMessage.error('批量拒绝失败')
    }
  }
}

// 我的申请相关方法
const handleMyApplicationSearchReset = () => {
  myApplicationSearchForm.value = {
    branchName: '',
    status: ''
  }
}

const handleMyApplicationPageChange = (val) => {
  myApplicationPage.value = val
}

// 标签页切换处理
const handleTabChange = (tabName) => {
  switch (tabName) {
    case 'branch-list':
      getBranches()
      break
    case 'my-branches':
      getMyBranches()
      break
    case 'chapter-applications':
      getChapterApplications()
      break
    case 'my-applications':
      getMyApplications()
      break
  }
}

// 获取待审核申请数量
const getPendingApplicationCount = async () => {
  try {
    const res = await axios.get('/api/chapters/my-pending-application-count')
    if (res.data.code === 0) {
      pendingApplicationCount.value = res.data.data || 0
    }
  } catch (error) {
    console.error('获取待审核申请数量失败', error)
  }
}

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
const getChapterApplications = async () => {
  chapterLoading.value = true
  try {
    // 获取当前用户创建的分会列表
    const chaptersRes = await axios.get('/api/chapters/my', { params: { alumniId: userInfo.alumniId } })
    const chapters = chaptersRes.data.data || []
    
    if (chapters.length === 0) {
      chapterApplications.value = []
      return
    }
    
    // 获取所有分会的申请列表
    const allApplications = []
    for (const chapter of chapters) {
      const res = await axios.get('/api/chapters/chapter-applications', { params: { tagId: chapter.tagId } })
      if (res.data.success && res.data.data) {
        allApplications.push(...res.data.data)
      }
    }
    
    chapterApplications.value = allApplications
  } catch (error) {
    console.error('获取申请列表失败', error)
    ElMessage.error('获取申请列表失败')
  } finally {
    chapterLoading.value = false
  }
}

// 获取我的申请列表
const getMyApplications = async () => {
  myApplicationLoading.value = true
  try {
    const res = await axios.get('/api/chapters/my-applications', { params: { alumniId: userInfo.alumniId } })
    if (res.data.success) {
      myApplications.value = res.data.data || []
    }
  } catch (error) {
    console.error('获取我的申请列表失败', error)
    ElMessage.error('获取我的申请列表失败')
  } finally {
    myApplicationLoading.value = false
  }
}

// WebSocket初始化
function initWebSocket() {
  ws.value = createWS({
    onMessage: (event) => {
      const msg = event.data;
      if (["chapter changed", "alumni changed", "chapter application created", "chapter application reviewed"].includes(msg)) {
        if (activeTab.value === 'my-branches') {
          getMyBranches();
        } else if (activeTab.value === 'branch-list') {
          getBranches();
        }
        if (activeTab.value === 'chapter-applications') {
          getChapterApplications();
        }
        if (activeTab.value === 'my-applications') {
          getMyApplications();
        }
        getPendingApplicationCount();
      }
    },
    onClose: () => {
      // 可选：可加提示或其他处理
    }
  })
}

onMounted(() => {
  // 检查是否需要保持特定视图状态
  const branchListView = sessionStorage.getItem('branch_list_view')
  if (branchListView === 'my-chapters') {
    activeTab.value = 'my-branches'
    getMyBranches()
    sessionStorage.removeItem('branch_list_view')
  } else {
    getBranches()
  }
  getMyChapters()
  getChapterApplications()
  getMyApplications()
  getPendingApplicationCount()
  initWebSocket()
})

onBeforeUnmount(() => {
  if (ws.value && ws.value._manualClose) ws.value._manualClose();
})

// 添加getApplicationStatus方法，依赖applicationStatus对象
const getApplicationStatus = (tagId) => {
  return applicationStatus.value[String(tagId)] || null
}

// 解散分会
const handleDissolveChapter = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要解散分会"${row.branch_name}"吗？此操作不可恢复！`, '警告', { type: 'warning' })
    const res = await axios.post('/api/chapters/dissolve', {
      alumniId: userInfo.alumniId,
      tagId: row.tag_id
    })
    if (res.data.success) {
      ElMessage.success('分会已解散')
      getBranches()
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
</script>

<style scoped>
.branch-list-root {
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.branch-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.branch-list-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.branch-list-content {
  margin-top: 20px;
}

.create-form {
  padding: 20px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background-color: #f56c6c;
  color: white;
  border-radius: 50%;
  min-width: 18px;
  height: 18px;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
  box-sizing: border-box;
  font-weight: bold;
}

.branch-tabs {
  margin-top: 20px;
}

.chapter-application-content,
.my-application-content {
  padding: 20px 0;
}

.search-area {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

@media (max-width: 900px) {
  .branch-list-root {
    padding: 8px;
    border-radius: 0;
    box-shadow: none;
  }
  .branch-list-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    margin-bottom: 12px;
    padding-left: 0 !important;
  }
  .branch-list-title {
    font-size: 16px;
  }
  .branch-list-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 4px;
  }
  .branch-list-content {
    margin-top: 8px;
  }
  .el-table {
    font-size: 13px;
    overflow-x: auto;
  }
  .el-table__header th, .el-table__body td {
    padding: 6px 4px !important;
    font-size: 13px !important;
  }
  .back-btn {
    position: static !important;
    margin-bottom: 8px;
    font-size: 16px;
    left: 0 !important;
    top: 0 !important;
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
}
@media (max-width: 600px) {
  .el-table {
    font-size: 12px;
  }
  .branch-list-header {
    gap: 4px;
    margin-bottom: 6px;
  }
  .branch-list-title {
    font-size: 14px;
  }
  .branch-list-actions {
    gap: 4px;
  }
}
</style> 