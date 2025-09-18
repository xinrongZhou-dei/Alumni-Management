<template>
  <el-card class="activity-manage-root" shadow="hover">
    <el-button type="text" @click="goBack" class="back-btn"><el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回</el-button>
    <div class="activity-manage-header-flex">
      <h2 class="activity-manage-title">活动报名</h2>
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
            :type="getStatusTagType(scope.row)"
            effect="light"
            round
          >
            {{ getStatusText(scope.row) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="200">
        <template #default="scope">
          <el-space>
            <el-button type="primary" size="small" @click="goDetail(scope.row)">查看</el-button>
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
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()

const goBack = () => {
  router.push('/')
}

// 活动数据存储
const activityList = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const pagedList = ref([])
const startItem = computed(() => (total.value === 0 ? 0 : (page.value - 1) * pageSize.value + 1))
const endItem = computed(() => Math.min(page.value * pageSize.value, total.value))

// 获取活动列表
const fetchActivityList = async () => {
  try {
    const res = await axios.get('/api/activity/list', { params: { page: page.value, size: pageSize.value } })
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

const goDetail = (row) => {
  sessionStorage.setItem('activity_detail_from', 'list')
  router.push(`/alumni/activity-detail/${row.uuid}`)
}

const handlePageChange = (val) => {
  page.value = val
  fetchActivityList()
}

// 获取状态标签类型
const getStatusTagType = (row) => {
  if (row.signupStatus === '报名人数已满') {
    return 'danger'
  }
  if (row.signupStatus === '进行中') return 'success'
  if (row.signupStatus === '未开始') return 'info'
  if (row.signupStatus === '即将结束') return 'warning'
  return 'danger'
}

// 获取状态文本
const getStatusText = (row) => {
  if (row.signupStatus === '报名人数已满') {
    return '报名人数已满'
  }
  return row.signupStatus
}

onMounted(() => {
  fetchActivityList()
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