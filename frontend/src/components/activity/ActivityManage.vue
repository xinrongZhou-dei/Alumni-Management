<template>
  <el-card class="activity-manage-root" shadow="hover">
    <el-button type="text" @click="goAdminHome" class="back-btn"><el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回</el-button>
    <div class="activity-manage-header-flex">
      <h2 class="activity-manage-title">{{ isAlumniView ? '活动列表' : '活动管理' }}</h2>
      <el-button v-if="!isAlumniView && hasEditPermission" class="create-activity-btn" type="primary" @click="goToAdd">
        <el-icon style="vertical-align: middle; margin-right: 6px;"><Plus /></el-icon>
        创建新活动
      </el-button>
    </div>
    <el-divider />
    <el-table :data="pagedList" border stripe class="activity-table">
      <el-table-column prop="name" label="活动名称" min-width="140" />
      <el-table-column prop="activityDate" label="活动时间" min-width="120">
        <template #default="scope">
          {{ formatDate(scope.row.activityDate) }}
        </template>
      </el-table-column>
      <el-table-column label="报名起止时间" min-width="180">
        <template #default="scope">
          {{ formatDate(scope.row.signupStart) }} ~ {{ formatDate(scope.row.signupEnd) }}
        </template>
      </el-table-column>
      <el-table-column prop="signupTotal" label="报名人数" min-width="100" />
      <el-table-column prop="signupActual" label="已报名人数" min-width="100" />
      <el-table-column prop="signupStatus" label="报名状态" min-width="110">
        <template #default="scope">
          <el-tag
            :type="scope.row.signupStatus === '进行中'
              ? 'success'
              : scope.row.signupStatus === '未开始'
                ? 'info'
                : scope.row.signupStatus === '即将结束'
                  ? 'warning'
                  : 'danger'"
            effect="light"
            round
          >
            {{ scope.row.signupStatus }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="操作" min-width="200">
        <template #default="scope">
          <el-space>
            <el-button type="primary" size="small" @click="goDetail(scope.row)">查看</el-button>
            <el-button v-if="!isAlumniView && hasEditPermission" type="warning" size="small" @click="goEdit(scope.row)">编辑</el-button>
            <el-button v-if="!isAlumniView && hasEditPermission" type="danger" size="small" @click="handleDelete(scope.row.uuid)">删除</el-button>
          </el-space>
        </template>
      </el-table-column>
    </el-table>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 16px;">
      <div>
        显示第{{ startItem }}到{{ endItem }}条，共{{ total }}条记录
      </div>
      <el-pagination
        background
        layout="prev, pager, next, jumper"
        :current-page="page"
        :page-size="pageSize"
        :total="total"
        @current-change="handlePageChange"
        style="margin-right: 8px;"
      />
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, computed, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessageBox, ElMessage } from 'element-plus'
import { createWS } from '@/utils/ws'

const router = useRouter()
const isAlumniView = ref(false)

// 权限信息
const permissions = ref({})
const permissionLevel = ref(0)
const hasEditPermission = computed(() => permissionLevel.value === 2)

const goBack = () => {
  router.push('/')
}

const goAdminHome = () => {
  router.push('../')
}

// 活动数据存储
const activityList = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const pagedList = ref([])
const startItem = computed(() => (total.value === 0 ? 0 : (page.value - 1) * pageSize.value + 1))
const endItem = computed(() => Math.min(page.value * pageSize.value, total.value))

// 获取权限信息
const fetchPermissions = async () => {
  try {
    const permissionRes = await axios.get('/api/admin/permissions')
    if (permissionRes.data.code === 0) {
      permissions.value = permissionRes.data.data
      
      // 设置权限级别 - 使用活动管理权限
      if (permissions.value.activity_management_permission !== undefined) {
        permissionLevel.value = permissions.value.activity_management_permission
      } else {
        // 如果找不到具体权限，使用默认值0（无权限）
        permissionLevel.value = 0
      }
    } else {
      console.error('获取权限信息失败:', permissionRes.data.msg)
      // 权限获取失败时，设置为无权限
      permissionLevel.value = 0
    }
  } catch (error) {
    console.error('获取权限信息出错:', error)
    // 权限获取失败时，设置为无权限
    permissionLevel.value = 0
  }
}

// 获取活动列表
const fetchActivityList = async () => {
  try {
    const res = await axios.get('/api/activity/list', { 
      params: { 
        page: page.value, 
        size: pageSize.value
      } 
    })
    
    if (res.data && res.data.code === 0) {
      const statusOrder = { '即将结束': 0, '进行中': 1, '未开始': 2, '已结束': 3 }
      const list = (res.data.data || []).slice().sort((a, b) => {
        const aStatus = statusOrder[a.signupStatus] ?? 99
        const bStatus = statusOrder[b.signupStatus] ?? 99
        if (aStatus !== bStatus) return aStatus - bStatus
        const aDate = new Date(a.activityDate).getTime()
        const bDate = new Date(b.activityDate).getTime()
        return aDate - bDate
      })
      activityList.value = list
      total.value = res.data.total || list.length
      // 只取当前页数据
      pagedList.value = list
    } else {
      activityList.value = []
      pagedList.value = []
      total.value = 0
    }
  } catch (e) {
    console.error('获取活动列表出错:', e)
    activityList.value = []
    pagedList.value = []
    total.value = 0
  }
}

function formatDate(val) {
  if (!val) return ''
  const d = new Date(val)
  if (isNaN(d.getTime())) return ''
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const handleDelete = (uuid) => {
  ElMessageBox.confirm('确定要删除该活动吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const res = await axios.delete(`/api/activity/delete/${uuid}`)
    if (res.data && res.data.code === 0) {
      ElMessage.success('删除成功')
      fetchActivityList() // 重新加载活动列表
    } else {
      ElMessage.error(res.data.msg || '删除失败')
    }
  }).catch(() => {})
}

const goDetail = (row) => {
  sessionStorage.setItem('activity_detail_from', 'admin')
  router.push(`/admin/activity-detail/${row.uuid}`)
}
const goEdit = (row) => {
  router.push(`/admin/activity-edit/${row.uuid}`)
}

const goToAdd = () => {
  router.push('/admin/activity-add')
}

const handlePageChange = (val) => {
  page.value = val
  fetchActivityList()
}

const ws = ref(null)

function initWebSocket() {
  ws.value = createWS({
    onMessage: (event) => {
      const msg = event.data
      if (["activity changed", "activity registration changed"].includes(msg)) {
        fetchActivityList()
      }
    },
    onClose: () => {
      // 可选：可加提示或其他处理
    }
  })
}

onMounted(async () => {
  const from = sessionStorage.getItem('activity_manage_from')
  isAlumniView.value = from === 'alumni'
  
  // 先获取权限信息
  await fetchPermissions()
  
  // 再获取活动列表
  await fetchActivityList()
  
  initWebSocket()
})

onBeforeUnmount(() => {
  if (ws.value) {
    ws.value._manualClose && ws.value._manualClose();
  }
})
</script>

<style scoped>
.activity-manage-root {
  max-width: 1200px;
  margin: 32px auto;
  border-radius: 16px;
  box-shadow: 0 4px 24px #e0e7ef22;
  padding: 32px 32px 24px 32px;
  background: #fff;
}
.activity-manage-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0;
}
.activity-manage-title {
  font-size: 2rem;
  font-weight: bold;
  color: #2563eb;
  margin-bottom: 0;
}
.activity-table {
  margin-top: 18px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px #e0e7ef22;
}
.back-btn {
  position: absolute;
  left: 24px;
  top: 24px;
  font-size: 18px;
  z-index: 10;
}
.create-activity-btn {
  background: linear-gradient(90deg, #2563eb 0%, #4f8cff 100%);
  color: #fff;
  border: none;
  border-radius: 24px;
  font-weight: bold;
  font-size: 16px;
  padding: 0 28px;
  height: 44px;
  box-shadow: 0 4px 16px #2563eb33;
  transition: all 0.2s;
  display: flex;
  align-items: center;
}
.create-activity-btn:hover {
  background: linear-gradient(90deg, #4f8cff 0%, #2563eb 100%);
  box-shadow: 0 6px 24px #2563eb44;
  color: #fff;
  opacity: 0.95;
}
@media (max-width: 768px) {
  .activity-manage-root {
    max-width: 100%;
    margin: 0;
    border-radius: 0;
    box-shadow: none;
    padding: 8px 0 8px 0;
  }
  .activity-table {
    margin-top: 8px;
    border-radius: 0;
    box-shadow: none;
    font-size: 13px;
    overflow-x: auto;
  }
  .activity-manage-header-flex {
    flex-direction: column;
    align-items: flex-start;
    margin-bottom: 8px;
  }
  .activity-manage-title {
    font-size: 1.2rem;
    margin-bottom: 4px;
  }
  .back-btn {
    position: static;
    margin-bottom: 8px;
    font-size: 16px;
  }
  .create-activity-btn {
    font-size: 14px;
    height: 36px;
    padding: 0 16px;
  }
  .el-table__header th, .el-table__body td {
    padding: 6px 4px !important;
    font-size: 13px !important;
  }
  .el-pagination {
    flex-wrap: wrap;
    justify-content: flex-end;
    font-size: 13px;
  }
  /* 分页和提示信息分两行 */
  div[style*='display: flex; justify-content: space-between'] {
    flex-direction: column;
    align-items: stretch !important;
    gap: 8px;
  }
  div[style*='display: flex; justify-content: space-between'] > div:first-child {
    font-size: 13px;
    text-align: center;
    color: #888;
    margin-bottom: 0;
  }
}
</style> 