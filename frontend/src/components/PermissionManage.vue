<template>
  <div>
    <el-button type="text" @click="goHome" :style="isMobile ? 'position: absolute; left: 12px; top: 12px; font-size: 16px;' : 'position: absolute; left: 24px; top: 24px; font-size: 18px;'"><el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回</el-button>
    <div class="permission-manage-container">
      <div class="page-header">
        <h1 class="page-title">权限管理</h1>
      </div>

      <!-- 搜索和操作区域 -->
      <div class="actions-row" :class="{ 'mobile-actions': isMobile }">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索学号、姓名..."
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
          class="search-input"
          :class="{ 'mobile-search': isMobile }"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
        <el-button v-if="permissionManagementPermission === 2" type="primary" @click="openAddAdminDialog">添加管理员</el-button>
      </div>

      <!-- 表格区域 -->
      <el-table :data="tableData" v-loading="loading" stripe class="admin-table" :size="isMobile ? 'small' : 'default'">
        <el-table-column prop="adminInfo" label="管理员信息" :width="isMobile ? 'auto' : '250'">
          <template #default="{ row }">
            <div class="user-info-cell">
              <span class="user-name">{{ row.chineseName || (row.firstName + ' ' + row.lastName) }}</span>
              <span class="user-id">ID: {{ row.id }}</span>
              <span class="user-email">{{ row.email }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="accessible_schools" label="可访问学校" min-width="200">
          <template #default="{ row }">
            <div v-if="row.accessible_schools" class="schools-container">
              <el-tag v-for="school in getSchools(row.accessible_schools).slice(0, isMobile ? 1 : 2)" :key="school" type="info" size="small" class="school-tag">
                {{ school }}
              </el-tag>
              <el-popover placement="bottom-start" :width="250" trigger="hover" v-if="getSchools(row.accessible_schools).length > (isMobile ? 1 : 2)">
                <template #reference>
                  <el-tag type="primary" size="small" style="cursor: pointer;">
                    +{{ getSchools(row.accessible_schools).length - (isMobile ? 1 : 2) }}
                  </el-tag>
                </template>
                <div class="permission-popover">
                  <el-tag v-for="school in getSchools(row.accessible_schools).slice(isMobile ? 1 : 2)" :key="school" type="info" size="small" class="school-tag">
                    {{ school }}
                  </el-tag>
                </div>
              </el-popover>
            </div>
            <span v-else>无</span>
          </template>
        </el-table-column>

        <el-table-column prop="permissions" label="页面权限" min-width="300">
          <template #default="{ row }">
            <div v-if="getPermissionEntries(row).length" class="permissions-container">
              <el-tag
                v-for="perm in getPermissionEntries(row).slice(0, isMobile ? 2 : 3)"
                :key="perm.key"
                :type="perm.type"
                effect="light"
                size="small"
                class="permission-tag"
              >
                {{ perm.name }}: {{ perm.level }}
              </el-tag>
              <el-popover placement="bottom-start" :width="250" trigger="hover" v-if="getPermissionEntries(row).length > (isMobile ? 2 : 3)">
                <template #reference>
                  <el-tag type="primary" size="small" style="cursor: pointer;">
                    +{{ getPermissionEntries(row).length - (isMobile ? 2 : 3) }}
                  </el-tag>
                </template>
                <div class="permission-popover">
                  <el-tag
                    v-for="perm in getPermissionEntries(row).slice(isMobile ? 2 : 3)"
                    :key="perm.key"
                    :type="perm.type"
                    effect="light"
                    size="small"
                    class="permission-tag"
                  >
                    {{ perm.name }}: {{ perm.level }}
                  </el-tag>
                </div>
              </el-popover>
            </div>
            <span v-else>无任何权限</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" :width="isMobile ? 'auto' : '180'" min-width="90" v-if="permissionManagementPermission === 2">
          <template #default="{ row }">
            <div :class="{'mobile-actions-buttons': isMobile}">
              <el-button type="primary" link @click="handleEdit(row)" class="action-btn">编辑</el-button>
              <el-popconfirm
                title="确定要删除该管理员吗？"
                confirm-button-text="确定"
                cancel-button-text="取消"
                @confirm="handleDelete(row)"
                width="220"
              >
                <template #reference>
                  <el-button type="danger" link class="action-btn">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
      <div class="pagination-container" :class="{ 'mobile-pagination': isMobile }">
        <el-pagination
          background
          :layout="isMobile ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
          :total="total"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :small="isMobile"
        />
      </div>

      <!-- 添加/编辑对话框 -->
      <el-dialog v-model="dialogVisible" :title="dialogTitle" :width="isMobile ? '90%' : '60%'" @close="resetForm">
        <el-form :model="form" ref="formRef" :label-width="isMobile ? '100px' : '140px'" :rules="rules" :label-position="isMobile ? 'top' : 'right'">
          <el-form-item label="管理员ID (学号)" prop="id">
            <el-input v-model="form.id" placeholder="请输入新校友的学号" :disabled="isEditMode" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email" v-if="!isEditMode">
            <el-input v-model="form.email" placeholder="请输入新校友的邮箱" />
          </el-form-item>
          <el-form-item label="可访问学校" prop="accessible_schools">
            <div class="tag-input-container">
              <el-tag
                v-for="school in selectedSchools"
                :key="school"
                class="tag-item"
                closable
                @close="removeSchool(school)"
              >
                {{ school }}
              </el-tag>
              <el-dropdown
                trigger="hover"
                v-if="unselectedSchools.length > 0"
                ref="schoolDropdown"
              >
                <el-button type="primary" :icon="Plus" circle plain />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      v-for="school in unselectedSchools"
                      :key="school"
                      @click="handleAddSchool(school)"
                    >
                      {{ school }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </el-form-item>
          <el-divider>权限分配</el-divider>
          <el-row :gutter="20">
            <el-col :span="24" v-for="perm in permissionConfig" :key="perm.key">
              <el-form-item :label="perm.name" :label-position="isMobile ? 'top' : 'right'">
                <el-radio-group v-model="form[perm.key]" :size="isMobile ? 'small' : 'default'">
                  <el-radio :label="0">不可访问</el-radio>
                  <el-radio :label="1">只读</el-radio>
                  <el-radio :label="2">读写</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <template #footer>
          <span class="dialog-footer" :class="{'mobile-dialog-footer': isMobile}">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitForm">确定</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox, ElPopconfirm } from 'element-plus';
import { Search, Plus, ArrowLeft } from '@element-plus/icons-vue';
import service from '@/utils/request';

const router = useRouter();

// --- 响应式数据 ---
const searchKeyword = ref('');
const tableData = ref([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const dialogVisible = ref(false);
const isEditMode = ref(false);
const formRef = ref(null);

// 响应式布局
const isMobile = ref(false);
const checkScreenWidth = () => {
  isMobile.value = window.innerWidth < 768;
};

onMounted(() => {
  checkScreenWidth();
  window.addEventListener('resize', checkScreenWidth);
});

// 权限相关
const permissionManagementPermission = ref(0)
const hasPermission = ref(false)

const defaultForm = {
  id: '',
  email: '',
  accessible_schools: '',
  alumni_info_management_permission: 0,
  alumni_info_review_permission: 0,
  chapter_review_permission: 0,
  activity_management_permission: 0,
  tag_management_permission: 0,
  email_template_permission: 0,
  data_analysis_permission: 0,
  permission_management_permission: 0,
  campus_visit_record_permission: 0,
};
const form = reactive({ ...defaultForm });

const rules = {
  id: [{ required: true, message: '管理员ID不能为空', trigger: 'blur' }],
  email: [
    { required: true, message: '邮箱不能为空', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: ['blur', 'change'] }
  ],
};

// 在此处暂时硬编码学校列表，后续建议从API获取
const allSchools = ref([
  'YCIS Hong Kong', 'YCIS Shanghai', 'YCIS Beijing', 'YCIS Chongqing', 'YCIS Qingdao', 
  'YWIES Beijing', 'YWIES Guangzhou', 'YWIES Shanghai Gubei', 'YWIES Shanghai Lingang', 
  'YWIES Tongxiang', 'YWIES Yantai'
]);

const selectedSchools = ref([]);
const schoolDropdown = ref(null);

watch(() => form.accessible_schools, (newVal) => {
  if (newVal && typeof newVal === 'string') {
    selectedSchools.value = newVal.split(',').map(s => s.trim()).filter(Boolean);
  } else {
    selectedSchools.value = [];
  }
});

const unselectedSchools = computed(() => {
  return allSchools.value.filter(school => !selectedSchools.value.includes(school));
});

const addSchool = (school) => {
  if (!selectedSchools.value.includes(school)) {
    selectedSchools.value.push(school);
    form.accessible_schools = selectedSchools.value.join(',');
  }
};

const handleAddSchool = (school) => {
  addSchool(school);
  // 移动端选择后自动收起下拉框
  if (isMobile.value && schoolDropdown.value) {
    schoolDropdown.value.handleClose();
  }
};

const removeSchool = (school) => {
  selectedSchools.value = selectedSchools.value.filter(s => s !== school);
  form.accessible_schools = selectedSchools.value.join(',');
};

const getSchools = (schoolsStr) => {
  if (!schoolsStr || typeof schoolsStr !== 'string') return [];
  return schoolsStr.split(',').map(s => s.trim()).filter(Boolean);
};

// --- 权限配置 ---
const permissionConfig = [
  { key: 'alumni_info_management_permission', name: '校友信息管理' },
  { key: 'alumni_info_review_permission', name: '校友信息审核' },
  { key: 'chapter_review_permission', name: '分会审核' },
  { key: 'activity_management_permission', name: '活动管理' },
  { key: 'tag_management_permission', name: '标签管理' },
  { key: 'email_template_permission', name: '邮件模板' },
  { key: 'data_analysis_permission', name: '数据分析' },
  { key: 'permission_management_permission', name: '权限管理' },
  { key: 'campus_visit_record_permission', name: '校园参观记录' },
];

const getPermissionEntries = (row) => {
  return permissionConfig
    .map(({ key, name }) => {
      const levelValue = row[key];
      if (levelValue > 0) {
        return {
          key,
          name,
          level: levelValue === 2 ? '读写' : '只读',
          type: levelValue === 2 ? 'success' : 'primary',
        };
      }
      return null;
    })
    .filter(Boolean);
};

const dialogTitle = computed(() => (isEditMode.value ? '编辑管理员' : '添加管理员'));

// 获取权限
const fetchPermission = async () => {
  try {
    const res = await service.get('/admin/permissions')
    if (res.code === 0) {
      const permissions = res.data
      permissionManagementPermission.value = permissions.permission_management_permission || 0
      hasPermission.value = permissionManagementPermission.value > 0
    } else {
      permissionManagementPermission.value = 0
      hasPermission.value = false
    }
  } catch (e) {
    permissionManagementPermission.value = 0
    hasPermission.value = false
  }
}

// --- API 调用 ---
const fetchData = async () => {
  loading.value = true;
  try {
    await fetchPermission()
    const response = await service.get('/permissions/admins', {
      params: {
        query: searchKeyword.value,
        pageNum: currentPage.value,
        pageSize: pageSize.value,
      },
    });
    if (response.code === 0) {
      tableData.value = response.data.list;
      total.value = response.data.total;
    } else {
      ElMessage.error(response.msg || '获取数据失败');
    }
  } catch (error) {
    ElMessage.error('请求失败，请检查网络');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchPermission()
  fetchData();
});

// --- 事件处理 ---
const handleSearch = () => {
  currentPage.value = 1;
  fetchData();
};
const handleSizeChange = (val) => {
  pageSize.value = val;
  fetchData();
};
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchData();
};

const resetForm = () => {
  Object.assign(form, defaultForm);
  formRef.value?.clearValidate();
};

const openAddAdminDialog = async () => {
  await fetchPermission()
  if (permissionManagementPermission.value !== 2) {
    ElMessage.warning('权限不足，无法添加管理员')
    return
  }
  isEditMode.value = false;
  resetForm();
  dialogVisible.value = true;
};

const handleEdit = async (row) => {
  await fetchPermission()
  if (permissionManagementPermission.value !== 2) {
    ElMessage.warning('权限不足，无法编辑管理员')
    return
  }
  isEditMode.value = true;
  resetForm();
  const editData = { ...row };
  delete editData.email;
  Object.assign(form, editData);
  dialogVisible.value = true;
};

const submitForm = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      await fetchPermission()
      if (permissionManagementPermission.value !== 2) {
        ElMessage.warning('权限不足，无法进行操作')
        return
      }
      try {
        if (isEditMode.value) {
          await service.put(`/permissions/admins/${form.id}`, form);
          ElMessage.success('更新成功');
        } else {
          await service.post('/permissions/admins', form);
          ElMessage.success('添加成功');
        }
        dialogVisible.value = false;
        fetchData();
      } catch (error) {
        // 后端会通过GlobalExceptionHandler返回错误信息
        // ElMessage.error(error.response?.data?.msg || '操作失败');
        console.error(error);
      }
    }
  });
};

const handleDelete = async (row) => {
  await fetchPermission()
  if (permissionManagementPermission.value !== 2) {
    ElMessage.warning('权限不足，无法删除管理员')
    return
  }
  try {
    await service.delete(`/permissions/admins/${row.id}`);
    ElMessage.success('删除成功');
    fetchData();
  } catch (error) {
    console.error(error);
  }
};

const goHome = () => {
  router.push('/');
};
</script>

<style scoped>
.permission-manage-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: 100vh;
}
.page-header {
  margin-bottom: 20px;
}
.page-title {
  font-size: 24px;
  color: #303133;
}
.actions-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
.mobile-actions {
  flex-direction: column;
  gap: 10px;
}
.search-input {
  width: 300px;
}
.mobile-search {
  width: 100%;
}
.admin-table {
  width: 100%;
}
.user-info-cell {
  display: flex;
  flex-direction: column;
}
.user-name {
  font-weight: bold;
}
.user-id {
  font-size: 12px;
  color: #606266;
}
.user-email {
  font-size: 12px;
  color: #909399;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.mobile-pagination {
  justify-content: center;
}
.permission-tag {
  margin-bottom: 4px;
  margin-right: 4px;
}
.permission-popover {
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.school-tag {
  margin-right: 4px;
  margin-bottom: 4px;
}
.schools-container {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}
.permissions-container {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}
.tag-input-container {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  padding: 4px;
  border: 1px solid var(--el-border-color);
  border-radius: var(--el-border-radius-base);
  width: 100%;
  min-height: 32px;
}
.tag-item {
  margin: 2px;
}
.mobile-actions-buttons {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 6px;
  width: 100%;
}
.mobile-actions-buttons .action-btn {
  width: 100%;
  margin: 0;
  min-width: 60px;
  box-sizing: border-box;
}
.mobile-dialog-footer {
  display: flex;
  justify-content: space-between;
  width: 100%;
}
.mobile-dialog-footer button {
  flex: 1;
  margin: 0 5px;
}
</style> 