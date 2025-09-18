<template>
  <div class="alumni-table-wrapper">
    <el-table :data="data" style="width: 100%" border stripe class="alumni-table">
      <el-table-column prop="chineseName" label="中文姓名" />
      <el-table-column prop="firstName" label="英文名" />
      <el-table-column prop="lastName" label="英文姓" />
      <el-table-column prop="ycywSchoolsAttended" label="YCYW Schools Attended" />
      <el-table-column prop="university" label="大学" />
      <el-table-column prop="major" label="专业" />
      <el-table-column prop="company" label="公司" />
      <el-table-column prop="jobTitle" label="职位" />
      <el-table-column prop="industry" label="行业" />
      <el-table-column prop="countryRegion" label="国家/地区" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openMailDialog(row)">发送邮件</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-bar">
      <div class="pagination-info">
        显示第{{ total === 0 ? 0 : (pageNum - 1) * pageSize + 1 }}到{{ Math.min(pageNum * pageSize, total) }}条，共{{ total }}条记录
      </div>
      <el-pagination
        background
        layout="prev, pager, next, jumper"
        :current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        @current-change="onPageChange"
        class="pagination-el"
      />
    </div>

    <!-- 邮件对话框 -->
    <el-dialog
      v-model="mailDialogVisible"
      title="发送邮件"
      width="500px"
    >
      <el-form :model="mailForm" label-width="80px">
        <el-form-item label="发件人">
          <el-input v-model="mailForm.from" disabled />
        </el-form-item>
        <el-form-item label="主题">
          <el-input v-model="mailForm.subject" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="mailForm.content"
            type="textarea"
            :rows="4"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="mailDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="sendMail">发送</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { ElTable, ElTableColumn, ElPagination, ElDialog, ElForm, ElFormItem, ElInput, ElButton } from 'element-plus';
import axios from 'axios';

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  total: {
    type: Number,
    default: 0
  },
  pageNum: {
    type: Number,
    default: 1
  },
  pageSize: {
    type: Number,
    default: 10
  },
  userEmail: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['page-change', 'page-size-change']);

const mailDialogVisible = ref(false);
const mailForm = ref({
  to: '',
  subject: '',
  content: '',
  from: ''
});

async function openMailDialog(row) {
  if (!props.userEmail) {
    ElMessage.warning('请先登录后再发送邮件');
    return;
  }
  
  try {
    // 获取当前登录用户信息
    const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}');
    if (!userInfo.alumniId) {
      ElMessage.warning('请先登录后再发送邮件');
      return;
    }
    
    const res = await axios.get(`/api/user/info?alumniId=${userInfo.alumniId}`);
    if (res.data.code === 0 && res.data.data) {
      mailForm.value = {
        to: row.email || '', // 保存收件人邮箱但不显示
        subject: '',
        content: '',
        from: res.data.data.email
      };
      mailDialogVisible.value = true;
    } else {
      ElMessage.error('获取用户信息失败');
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
    ElMessage.error('获取用户信息失败，请稍后重试');
  }
}

async function sendMail() {
  if (!mailForm.value.subject) {
    ElMessage.warning('请输入邮件主题');
    return;
  }
  if (!mailForm.value.content) {
    ElMessage.warning('请输入邮件内容');
    return;
  }
  try {
    const res = await axios.post('/api/mail/send', {
      to: mailForm.value.to,
      subject: mailForm.value.subject,
      content: mailForm.value.content,
      from: mailForm.value.from
    });
    if (res.data.code === 0) {
      ElMessage.success('邮件发送成功');
      mailDialogVisible.value = false;
    } else {
      ElMessage.error(res.data.msg || '邮件发送失败');
    }
  } catch (error) {
    console.error('发送邮件失败:', error);
    ElMessage.error('发送邮件失败，请稍后重试');
  }
}

function onPageChange(val) {
  emit('page-change', val);
}

function onPageSizeChange(val) {
  emit('page-size-change', val);
}
</script>

<style scoped>
.alumni-table-wrapper {
  border: 2px solid #bfcbd9;
  border-radius: 12px;
  box-shadow: 0 2px 8px #e0e7ef22;
  background: #fff;
  overflow: hidden;
  margin-bottom: 16px;
}
.alumni-table {
  border-radius: 12px;
  overflow: hidden;
}
.mt-2 {
  margin-top: 16px;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
.alumni-table >>> .el-table__body tr td,
.alumni-table >>> .el-table__header tr th {
  border-color: #bfcbd9 !important;
  border-width: 2px !important;
}
.alumni-table >>> .el-table__header tr th {
  background: #f4f8fb !important;
  font-weight: 700;
  font-size: 16px;
  color: #222;
  height: 52px;
}
.alumni-table >>> .el-table__body tr td {
  border-bottom: 2px solid #e4e7ed !important;
  font-size: 15px;
  height: 48px;
}
.alumni-table >>> .el-table__row:hover td {
  background: #e6f7ff !important;
  font-weight: 600;
}
.pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  gap: 8px;
}
.pagination-info {
  font-size: 15px;
  color: #333;
}
.pagination-el {
  min-width: 220px;
}
@media (max-width: 768px) {
  .alumni-table-wrapper {
    border-radius: 0;
    box-shadow: none;
    margin-bottom: 8px;
    border-width: 1px;
    padding: 0;
  }
  .alumni-table {
    border-radius: 0;
    font-size: 13px;
    overflow-x: auto;
  }
  .alumni-table >>> .el-table__header tr th {
    font-size: 13px;
    height: 36px;
  }
  .alumni-table >>> .el-table__body tr td {
    font-size: 13px;
    height: 32px;
  }
  .mt-2 {
    margin-top: 8px;
  }
  .pagination-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 6px;
    margin-top: 10px;
  }
  .pagination-info {
    font-size: 13px;
    text-align: left;
  }
  .pagination-el {
    font-size: 13px;
    min-width: 0;
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
</style> 