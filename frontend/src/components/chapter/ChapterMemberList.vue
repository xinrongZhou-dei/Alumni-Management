<template>
  <div class="page-root">
    <el-button type="text" @click="goBack" class="back-btn">
      <el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回
    </el-button>
    <div class="alumni-search-page">
      <el-card class="search-card" shadow="hover">
        <div class="chapter-info">
          <h3>{{ chapterName }} - 分会成员</h3>
          <el-button type="danger" @click="handleLeaveChapter" class="leave-btn">退出分会</el-button>
        </div>
        <alumni-search-form :model-value="searchForm" @search="onSearch" @reset="onReset" />
      </el-card>
      <el-card class="table-card" shadow="hover">
        <div class="table-title">分会成员名录</div>
        <alumni-search-table
          :data="tableData"
          :total="total"
          :page-num="pageNum"
          :page-size="pageSize"
          :user-email="userEmail"
          @page-change="onPageChange"
          @page-size-change="onPageSizeChange"
        />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import AlumniSearchForm from '@/components/alumnibranchlist/AlumniSearchForm.vue';
import AlumniSearchTable from '@/components/alumnibranchlist/AlumniSearchTable.vue';
import { searchChapterMembers, getCurrentUserInfo } from '@/api/alumniSearch';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import axios from 'axios'
import { createWS } from '@/utils/ws'

const router = useRouter()
const route = useRoute()

// 根据来源实现不同的返回逻辑
const goBack = () => {
  const from = sessionStorage.getItem('chapter_members_from')
  
  switch (from) {
    case 'home':
      // 从首页卡片进入，返回首页
      router.push('/')
      break
    case 'my-chapters':
      // 从我的分会进入，返回分会列表并保持我的分会视图
      sessionStorage.setItem('branch_list_view', 'my-chapters')
      router.push('/alumni/branch-list')
      break
    case 'chapter-list':
    default:
      // 从分会列表进入或默认情况，返回分会列表
      router.push('/alumni/branch-list')
      break
  }
  
  // 清除来源标识
  sessionStorage.removeItem('chapter_members_from')
}

const chapterId = ref(route.params.chapterId)
const chapterName = ref('分会')
const searchForm = ref({ name: '', ycywSchoolsAttended: '' });
const tableData = ref([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);
const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}');
const userEmail = ref('');

// WebSocket连接
const ws = ref(null)

// 获取分会名称
const getChapterName = async () => {
  try {
    const res = await axios.get('/api/chapters')
    if (res.data.success && res.data.data) {
      const chapter = res.data.data.find(c => c.tagId == chapterId.value)
      if (chapter) {
        chapterName.value = chapter.branchName
      }
    }
  } catch (error) {
    console.error('获取分会名称失败:', error)
  }
}

async function fetchData(params = {}) {
  try {
    const res = await searchChapterMembers({ 
      ...searchForm.value, 
      chapterId: chapterId.value,
      pageNum: pageNum.value, 
      pageSize: pageSize.value, 
      ...params 
    });
    if (res.data) {
      tableData.value = res.data.data || [];
      total.value = res.data.total || 0;
    }
  } catch (error) {
    console.error('查询分会成员失败:', error);
    ElMessage.error('查询分会成员失败，请稍后重试');
  }
}

async function fetchUserInfo() {
  if (userInfo.alumniId) {
    try {
      const res = await getCurrentUserInfo(userInfo.alumniId);
      if (res.data && res.data.code === 0) {
        userEmail.value = res.data.data.email || '';
      }
    } catch (error) {
      console.error('获取用户信息失败:', error);
    }
  }
}

function onSearch(form) {
  pageNum.value = 1;
  Object.assign(searchForm.value, form);
  fetchData();
}

function onReset() {
  searchForm.value = { name: '', ycywSchoolsAttended: '' };
  pageNum.value = 1;
  fetchData();
}

function onPageChange(val) {
  pageNum.value = val;
  fetchData();
}

function onPageSizeChange(val) {
  pageSize.value = val;
  pageNum.value = 1;
  fetchData();
}

// 退出分会
const handleLeaveChapter = async () => {
  try {
    await ElMessageBox.confirm('确定要退出该分会吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    
    const res = await axios.post('/api/chapters/leave', {
      alumniId: userInfo.alumniId,
      tagId: chapterId.value
    })
    
    if (res.data.success) {
      ElMessage.success('退出成功')
      goBack()
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

// WebSocket初始化
function initWebSocket() {
  ws.value = createWS({
    onMessage: (event) => {
      const msg = event.data;
      if (["alumni changed", "tertiary changed", "career changed", "chapter changed"].includes(msg)) {
        fetchData();
      }
    },
    onClose: () => {
      // 可选：可加提示或其他处理
    }
  })
}

onMounted(async () => {
  await getChapterName();
  await fetchData();
  await fetchUserInfo();
  initWebSocket();
});

onBeforeUnmount(() => {
  if (ws.value && ws.value._manualClose) ws.value._manualClose();
})
</script>

<style scoped>
.page-root {
  position: relative;
  min-height: 100vh;
  background: #f5f7fa;
}
.back-btn {
  position: absolute;
  left: 24px;
  top: 24px;
  font-size: 18px;
  z-index: 10;
}
.alumni-search-page {
  padding: 80px 24px 24px;
  max-width: 1200px;
  margin: 0 auto;
}
.search-card {
  margin-bottom: 24px;
}
.chapter-info {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.chapter-info h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}
.table-card {
  margin-bottom: 24px;
}
.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #409eff;
}
.leave-btn {
  margin-left: 16px;
}
</style> 