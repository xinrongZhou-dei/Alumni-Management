<template>
  <div class="page-root">
    <el-button type="text" @click="goBack" class="back-btn">
      <el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回
    </el-button>
    <div class="alumni-search-page">
      <el-card class="search-card" shadow="hover">
        <alumni-search-form :model-value="searchForm" @search="onSearch" @reset="onReset" />
      </el-card>
      <el-card class="table-card" shadow="hover">
        <div class="table-title">校友名录</div>
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
import { searchAlumni, getCurrentUserInfo } from '@/api/alumniSearch';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { createWS } from '@/utils/ws'

const router = useRouter()
const goBack = () => { router.push('/') }
const searchForm = ref({ name: '', ycywSchoolsAttended: '', university: '' });
const tableData = ref([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);
const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}');
const userEmail = ref('');

async function fetchData(params = {}) {
  try {
    const res = await searchAlumni({ 
      ...searchForm.value, 
      pageNum: pageNum.value, 
      pageSize: pageSize.value, 
      ...params 
    });
    if (res.data) {
      tableData.value = res.data.data || [];
      total.value = res.data.total || 0;
    }
  } catch (error) {
    console.error('搜索校友失败:', error);
    ElMessage.error('搜索校友失败，请稍后重试');
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
  searchForm.value = { name: '', ycywSchoolsAttended: '', university: '' };
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

let ws = null
function initWebSocket() {
  ws = createWS({
    onMessage: (event) => {
      const msg = event.data;
      if (["alumni changed", "tertiary changed", "career changed"].includes(msg)) {
        fetchData();
      }
    },
    onClose: () => {
      // 可选：可加提示或其他处理
    }
  })
}
onBeforeUnmount(() => {
  if (ws && ws._manualClose) ws._manualClose();
})

onMounted(async () => {
  await fetchData();
  await fetchUserInfo();
  initWebSocket();
});
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
  max-width: 1200px;
  margin: 0 auto;
  padding-top: 64px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}
.search-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px #e0e7ef22;
  padding: 32px 32px 16px 32px;
  background: #fff;
}
.table-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px #e0e7ef22;
  padding: 24px 24px 8px 24px;
  background: #fff;
}
.table-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}
@media (max-width: 768px) {
  .page-root {
    padding: 0;
  }
  .back-btn {
    position: static;
    margin-bottom: 8px;
    font-size: 16px;
  }
  .alumni-search-page {
    max-width: 100%;
    padding-top: 8px;
    gap: 16px;
  }
  .search-card,
  .table-card {
    border-radius: 0;
    box-shadow: none;
    padding: 8px 4px;
  }
  .table-title {
    font-size: 16px;
    margin-bottom: 8px;
  }
}
</style> 