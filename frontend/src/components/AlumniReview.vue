<template>
  <div>
    <el-button type="text" @click="goHome" class="back-btn"><el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回</el-button>
    <div class="alumni-review-container">
      <div class="alumni-review-header">
        <div class="alumni-review-search">
          <el-input v-model="search.alumniID" placeholder="按学号搜索" @input="debouncedSearch" clearable />
          <el-input v-model="search.chinese_name" placeholder="按中文姓名搜索" @input="debouncedSearch" clearable />
          <el-input v-model="search.email" placeholder="按邮箱搜索" @input="debouncedSearch" clearable />
          <el-input v-model="search.contact_number" placeholder="按电话搜索" @input="debouncedSearch" clearable />
          <el-select v-model="search.ycyw_schools_attended" @change="debouncedSearch" clearable placeholder="就读学校">
            <el-option value="">全部学校</el-option>
            <el-option v-for="school in schoolOptions" :key="school" :label="school" :value="school" />
          </el-select>
          <el-button @click="handleClear">清空</el-button>
        </div>
      </div>
      <div class="table-wrapper">
        <el-table
          :data="filteredAlumniList"
          border
          style="width: 100%; margin-top: 18px;"
          v-loading="loading"
          :header-cell-style="{background:'#f9fafb',fontWeight:'bold'}"
        >
          <el-table-column prop="alumniId" label="学号" min-width="110" />
          <el-table-column prop="chineseName" label="中文姓名" min-width="110" />
          <el-table-column label="英文名/姓" min-width="120">
            <template #default="scope">
              {{ scope.row.firstName }}/{{ scope.row.lastName }}
            </template>
          </el-table-column>
          <el-table-column prop="ycywSchoolsAttended" label="YCYW就读学校" min-width="140" />
          <el-table-column prop="updateTime" label="信息最后更新时间" min-width="160">
            <template #default="scope">
              {{ scope.row.updateTime || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="状态" min-width="100">
            <template #default="scope">
              <el-tag :type="getStateType(scope.row.state)" effect="plain" style="font-weight:bold;font-size:1em;">
                {{ getStateText(scope.row.state) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="90">
            <template #default="scope">
              <el-button type="primary" size="small" round @click="handleView(scope.row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-bar">
        <div class="pagination-info">
          显示第{{ total === 0 ? 0 : (page - 1) * pageSize + 1 }}到{{ Math.min(page * pageSize, total) }}条，共{{ total }}条记录
        </div>
        <el-pagination
          background
          :layout="isSmallScreen ? 'sizes, prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
          :total="total"
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handlePageSizeChange"
          @current-change="fetchAlumniList"
          class="pagination-el"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onBeforeUnmount } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const goHome = () => router.push('/')
const isSmallScreen = ref(false)

const schoolOptions = ref([
  'YCIS Hong Kong', 'YCIS Shanghai', 'YCIS Beijing', 'YCIS Chongqing', 'YCIS Qingdao',
  'YWIES Beijing', 'YWIES Guangzhou', 'YWIES Shanghai Gubei', 'YWIES Shanghai Lingang',
  'YWIES Tongxiang', 'YWIES Yantai'
])

const search = ref({
  alumniID: '',
  chinese_name: '',
  email: '',
  contact_number: '',
  ycyw_schools_attended: ''
})
const alumniList = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const fetchAlumniList = async () => {
  loading.value = true
  try {
    const params = {
      alumniId: search.value.alumniID || undefined,
      chineseName: search.value.chinese_name || undefined,
      email: search.value.email || undefined,
      contactNumber: search.value.contact_number || undefined,
      ycywSchoolsAttended: search.value.ycyw_schools_attended || undefined,
      page: page.value,
      pageSize: pageSize.value,
      state: '2,3',
    }
    const res = await axios.get('/api/register/alumni-list', { params })
    if (res.data.code === 0) {
      alumniList.value = res.data.data.rows
      total.value = res.data.data.total
    } else {
      alumniList.value = []
      total.value = 0
      ElMessage.error(res.data.msg || '获取校友列表失败')
    }
  } catch (error) {
    alumniList.value = []
    total.value = 0
    ElMessage.error('网络错误，获取校友列表失败')
  }
  loading.value = false
}

const filteredAlumniList = computed(() => {
  // 只展示state为2和3的校友
  return alumniList.value.filter(row => row.state === 2 || row.state === 3)
})

const debouncedSearch = (() => {
  let timer = null
  return () => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      page.value = 1
      fetchAlumniList()
    }, 500)
  }
})()

const handleClear = () => {
  search.value = { alumniID: '', chinese_name: '', email: '', contact_number: '', ycyw_schools_attended: '' }
  page.value = 1
  fetchAlumniList()
}

const handlePageSizeChange = () => {
  page.value = 1
  fetchAlumniList()
}
const handleView = (row) => {
  router.push({ path: `/alumni/detail/${row.alumniId}`, query: { from: '/alumni/review' } })
}
function getStateText(state) {
  if (state === 2) return '待审核'
  if (state === 3) return '已拒绝'
  return '未知'
}
function getStateType(state) {
  if (state === 2) return 'warning'
  if (state === 3) return 'danger'
  return 'info'
}

onMounted(() => {
  fetchAlumniList()
  
  // 检测屏幕尺寸
  const checkScreenSize = () => {
    isSmallScreen.value = window.innerWidth <= 900
  }
  
  // 初始检测
  checkScreenSize()
  
  // 监听窗口大小变化
  window.addEventListener('resize', checkScreenSize)
  
  // 组件卸载时移除监听器
  onBeforeUnmount(() => {
    window.removeEventListener('resize', checkScreenSize)
  })
})
</script>

<style scoped>
.back-btn {
  position: absolute;
  left: 24px;
  top: 24px;
  font-size: 18px;
}

.alumni-review-container {
  max-width: 1200px;
  margin: 40px auto 0;
  padding: 48px 40px 40px;
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 2px 16px 0 rgba(0,0,0,0.07);
}

.alumni-review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.alumni-review-search {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.alumni-review-search .el-input {
  width: 140px;
}

.alumni-review-search .el-input:nth-child(3) {
  width: 180px;
}

.alumni-review-search .el-select {
  width: 180px;
}

.table-wrapper {
  overflow-x: auto;
  width: 100%;
}

.pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
  gap: 8px;
}

.pagination-info {
  font-size: 15px;
  color: #333;
}

.pagination-el {
  min-width: 220px;
}

/* 响应式样式 */
@media (max-width: 900px) {
  .back-btn {
    position: static;
    margin-bottom: 8px;
    font-size: 16px;
    margin-left: 8px;
    margin-top: 8px;
  }

  .alumni-review-container {
    padding: 8px 2px;
    max-width: 100vw;
    margin: 0;
    border-radius: 0;
    box-shadow: none;
  }

  .alumni-review-header {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
    margin-bottom: 12px;
  }

  .alumni-review-search {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
    width: 100%;
    padding: 0 8px;
  }

  .alumni-review-search .el-input,
  .alumni-review-search .el-input:nth-child(3),
  .alumni-review-search .el-select {
    width: 100%;
  }
  
  .alumni-review-search .el-button {
    width: 100%;
    margin: 0;
    padding: 10px 0;
  }
  
  .table-wrapper {
    width: 100vw;
    margin-left: -8px;
    padding-bottom: 8px;
    overflow-x: auto;
  }

  .pagination-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
    margin-top: 16px;
    padding: 0 8px;
  }

  .pagination-info {
    font-size: 13px;
    text-align: center;
    margin-bottom: 4px;
  }

  .pagination-el {
    min-width: 0;
    display: flex;
    justify-content: center;
  }

  :deep(.el-pagination) {
    justify-content: center;
    flex-wrap: wrap;
    padding: 0;
    gap: 6px;
  }
  
  :deep(.el-pagination .el-pagination__total) {
    display: none; /* 隐藏total，因为我们已经有了独立的信息显示 */
  }
  
  :deep(.el-pagination .el-pagination__sizes) {
    margin: 0;
    margin-right: 0 !important;
  }

  :deep(.el-pagination .el-select) {
    width: 80px;
    margin: 0;
  }
  
  :deep(.el-pagination .el-pagination__jump) {
    margin: 0;
    margin-left: 0 !important;
  }

  :deep(.el-pagination button) {
    min-width: 32px;
    height: 32px;
    margin: 0 3px;
  }
  
  :deep(.el-pagination .el-pager li) {
    min-width: 32px;
    height: 32px;
    line-height: 32px;
    margin: 0 3px;
    font-size: 14px;
  }
  
  :deep(.el-pagination .btn-prev),
  :deep(.el-pagination .btn-next) {
    padding: 0;
    margin: 0 3px;
    min-width: 32px;
  }
}
</style> 