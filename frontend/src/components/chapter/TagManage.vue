<template>
  <div>
    <el-button type="text" @click="goHome" class="back-btn">
      <el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回
    </el-button>
    <div class="tag-manage-outer">
      <el-card class="tag-manage-card">
        <div class="tag-manage-header">
          <div class="tag-manage-title">标签管理</div>
          <el-button v-if="tagPermission === 2" type="primary" @click="showAddDialog = true" size="large" round>添加标签</el-button>
        </div>
        <div class="tag-manage-table-wrap">
          <el-table :data="tagList" stripe border class="tag-manage-table" v-loading="loading" empty-text="暂无标签">
            <el-table-column prop="name" label="标签名称" min-width="120" align="center">
              <template #default="scope">
                <el-tag :style="getTagStyle(scope.row.color)" effect="plain">
                  {{ scope.row.name }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="color" label="颜色" min-width="120" align="center">
              <template #default="scope">
                <span class="color-square" :style="getColorSquareStyle(scope.row.color)"></span>
                <span :style="getColorTextStyle(scope.row.color)">{{ getColorLabel(scope.row.color) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="usage_count" label="使用次数" min-width="100" align="center"/>
            <el-table-column prop="create_time" label="创建时间" min-width="120" align="center">
              <template #default="scope">{{ (scope.row.create_time || scope.row.createTime || '-')?.slice(0, 10) }}</template>
            </el-table-column>
            <el-table-column label="操作" min-width="120" align="center" v-if="tagPermission === 2">
              <template #default="scope">
                <div class="action-buttons">
                  <el-button type="primary" size="small" @click="editTag(scope.row)" round>编辑</el-button>
                  <el-button type="danger" size="small" @click="deleteTag(scope.row.id)" round>删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div class="tag-manage-pagination">
          <el-pagination
            background
            :layout="isMobile ? 'prev, pager, next' : 'prev, pager, next, sizes, total'"
            :total="total"
            :page-size="pageSize"
            :current-page="page"
            :page-sizes="[10, 20, 50]"
            @size-change="changePageSize"
            @current-change="changePage"
          />
        </div>
        <el-dialog 
          v-model="showAddDialog" 
          title="添加标签" 
          :width="dialogWidth" 
          :close-on-click-modal="false" 
          center
          class="tag-dialog"
        >
          <el-form :model="newTag" label-width="80px" class="tag-add-form" :label-position="isMobile ? 'top' : 'right'">
            <el-form-item label="标签名称" required>
              <el-input v-model="newTag.name" maxlength="20" show-word-limit placeholder="请输入标签名称" clearable />
            </el-form-item>
            <el-form-item label="标签颜色" required>
              <div class="color-blocks">
                <div
                  v-for="item in colorOptions"
                  :key="item.value"
                  class="color-block"
                  :class="{ selected: newTag.color === item.value }"
                  :style="{ background: item.value }"
                  @click="newTag.color = item.value"
                ></div>
              </div>
            </el-form-item>
          </el-form>
          <template #footer>
            <div class="dialog-footer">
              <el-button @click="showAddDialog = false" class="dialog-btn">取消</el-button>
              <el-button type="primary" @click="addTag" v-if="tagPermission === 2" class="dialog-btn">确定</el-button>
            </div>
          </template>
        </el-dialog>
        <el-dialog 
          v-model="showEditDialog" 
          title="编辑标签" 
          :width="dialogWidth" 
          :close-on-click-modal="false" 
          center
          class="tag-dialog"
        >
          <el-form :model="editTagData" label-width="80px" class="tag-add-form" :label-position="isMobile ? 'top' : 'right'">
            <el-form-item label="标签名称" required>
              <el-input v-model="editTagData.name" maxlength="20" show-word-limit placeholder="请输入标签名称" clearable />
            </el-form-item>
            <el-form-item label="标签颜色" required>
              <div class="color-blocks">
                <div
                  v-for="item in colorOptions"
                  :key="item.value"
                  class="color-block"
                  :class="{ selected: editTagData.color === item.value }"
                  :style="{ background: item.value }"
                  @click="editTagData.color = item.value"
                ></div>
              </div>
            </el-form-item>
          </el-form>
          <template #footer>
            <div class="dialog-footer">
              <el-button @click="showEditDialog = false" class="dialog-btn">取消</el-button>
              <el-button type="primary" @click="updateTag" v-if="tagPermission === 2" class="dialog-btn">保存</el-button>
            </div>
          </template>
        </el-dialog>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'

// 判断是否为移动设备
const isMobile = ref(window.innerWidth <= 768)

// 根据屏幕宽度设置对话框宽度
const dialogWidth = computed(() => {
  if (window.innerWidth <= 480) {
    return '90%'
  } else if (window.innerWidth <= 768) {
    return '80%'
  } else {
    return '400px'
  }
})

// 监听窗口大小变化
window.addEventListener('resize', () => {
  isMobile.value = window.innerWidth <= 768
})

const tagList = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const showAddDialog = ref(false)
const showEditDialog = ref(false)
const newTag = ref({ name: '', color: '' })
const editTagData = ref({ id: '', name: '', color: '' })
const router = useRouter()

// 新增：获取本地权限
const tagPermission = ref(0)

const colorOptions = [
  { label: '红色', value: '#f56c6c' },
  { label: '橙色', value: '#e6a23c' },
  { label: '黄色', value: '#f7ba2a' },
  { label: '绿色', value: '#67c23a' },
  { label: '青色', value: '#17c0ae' },
  { label: '蓝色', value: '#409eff' },
  { label: '灰色', value: '#909399' }
]

function getColorValue(color) {
  if (!color) return '#409eff'
  return color
}
function getColorLabel(color) {
  const found = colorOptions.find(c => c.value === color)
  return found ? found.label : '自定义色'
}
function getColorDotStyle(color) {
  return {
    display: 'inline-block',
    width: '18px',
    height: '18px',
    borderRadius: '50%',
    background: color || '#409eff',
    marginRight: '8px',
    border: '1.5px solid #e5e7eb',
    verticalAlign: 'middle',
  }
}
function getColorTextStyle(color) {
  // 判断背景色亮度，自动切换黑白字体
  if (!color) return { color: '#333' }
  const rgb = color.startsWith('#') ? hexToRgb(color) : [64, 158, 255]
  const brightness = rgb[0] * 0.299 + rgb[1] * 0.587 + rgb[2] * 0.114
  return { color: brightness > 186 ? '#333' : '#fff', fontWeight: 'bold' }
}
function getTagStyle(color) {
  // 标签名称用真实颜色，自动切换字体色
  const rgb = color ? hexToRgb(color) : [64, 158, 255]
  const brightness = rgb[0] * 0.299 + rgb[1] * 0.587 + rgb[2] * 0.114
  return {
    backgroundColor: color || '#409eff',
    color: brightness > 186 ? '#333' : '#fff',
    border: 'none',
    fontWeight: 'bold',
    fontSize: '0.9em',
    padding: '0 18px',
  }
}
function hexToRgb(hex) {
  let c = hex.substring(1)
  if (c.length === 3) c = c.split('').map(x => x + x).join('')
  const num = parseInt(c, 16)
  return [(num >> 16) & 255, (num >> 8) & 255, num & 255]
}

async function fetchTagList() {
  loading.value = true
  try {
    // 从后端获取权限信息
    const permissionRes = await axios.get('/api/admin/permissions')
    if (permissionRes.data.code === 0) {
      const permissions = permissionRes.data.data
      
      // 设置权限级别 - 使用标签管理权限
      if (permissions.tag_management_permission !== undefined) {
        tagPermission.value = permissions.tag_management_permission
      } else {
        // 如果找不到具体权限，使用默认值0（无权限）
        tagPermission.value = 0
      }
    } else {
      console.error('获取权限信息失败:', permissionRes.data.msg)
      // 权限获取失败时，设置为无权限
      tagPermission.value = 0
    }
    
    const res = await axios.get('/api/tag/list', { params: { page: page.value, pageSize: pageSize.value } })
    if (res.data.code === 0) {
      tagList.value = res.data.data.rows
      total.value = res.data.data.total
    } else {
      tagList.value = []
      total.value = 0
      ElMessage.error(res.data.msg || '获取标签列表失败')
    }
  } catch (error) {
    console.error('获取标签列表出错:', error)
    ElMessage.error('网络错误，获取标签列表失败')
    tagList.value = []
    total.value = 0
  }
  loading.value = false
}

async function addTag() {
  if (tagPermission.value !== 2) {
    ElMessage.warning('权限不足，无法添加标签')
    return
  }
  
  if (!newTag.value.name) {
    ElMessage.warning('标签名称不能为空')
    return
  }
  if (!newTag.value.color) {
    ElMessage.warning('请选择标签颜色')
    return
  }
  const res = await axios.post('/api/tag/add', { name: newTag.value.name, color: newTag.value.color })
  if (res.data.code === 0) {
    ElMessage.success('添加成功')
    newTag.value.name = ''
    newTag.value.color = ''
    showAddDialog.value = false
    fetchTagList()
  } else {
    ElMessage.error(res.data.msg || '添加失败')
  }
}

async function deleteTag(id) {
  if (tagPermission.value !== 2) {
    ElMessage.warning('权限不足，无法删除标签')
    return
  }
  
  if (!window.confirm('确定要删除该标签吗？')) return
  const res = await axios.delete('/api/tag/delete', { data: { id } })
  if (res.data.code === 0) {
    ElMessage.success('删除成功')
    fetchTagList()
  } else {
    ElMessage.error(res.data.msg || '删除失败')
  }
}

function changePage(val) {
  page.value = val
  fetchTagList()
}
function changePageSize(val) {
  pageSize.value = val
  page.value = 1
  fetchTagList()
}

function getColorSquareStyle(color) {
  return {
    display: 'inline-block',
    width: '22px',
    height: '22px',
    borderRadius: '6px',
    background: color || '#409eff',
    marginRight: '8px',
    border: '1.5px solid #e5e7eb',
    verticalAlign: 'middle',
  }
}

function editTag(row) {
  editTagData.value = { id: row.id, name: row.name, color: row.color }
  showEditDialog.value = true
}

async function updateTag() {
  if (tagPermission.value !== 2) {
    ElMessage.warning('权限不足，无法修改标签')
    return
  }
  
  if (!editTagData.value.name) {
    ElMessage.warning('标签名称不能为空')
    return
  }
  if (!editTagData.value.color) {
    ElMessage.warning('请选择标签颜色')
    return
  }
  const res = await axios.post('/api/tag/update', {
    id: editTagData.value.id,
    name: editTagData.value.name,
    color: editTagData.value.color
  })
  if (res.data.code === 0) {
    ElMessage.success('修改成功')
    showEditDialog.value = false
    fetchTagList()
  } else {
    ElMessage.error(res.data.msg || '修改失败')
  }
}

function goHome() { router.push('/') }

onMounted(() => {
  fetchTagList()
})
</script>

<style scoped>
.back-btn {
  position: absolute;
  left: 24px;
  top: 24px;
  font-size: 18px;
  z-index: 1000;
  color: #409eff;
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 0 8px;
}

.tag-manage-outer {
  width: 100%;
  min-height: 80vh;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  background: transparent;
}

.tag-manage-card {
  width: 1200px;
  margin: 48px auto;
  border-radius: 18px;
  box-shadow: 0 8px 40px #c7d2fe22;
  padding: 40px 40px 40px 40px;
  background: #fff;
  display: block;
}

.tag-manage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-bottom: 24px;
}

.tag-manage-title {
  font-size: 2rem;
  font-weight: bold;
  color: #2563eb;
  letter-spacing: 1px;
}

.tag-manage-table-wrap {
  width: 100%;
  overflow-x: auto;
}

.tag-manage-table {
  width: 100%;
  font-size: 1.15rem;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: nowrap;
}

.tag-add-form {
  margin-top: 18px;
}

.tag-manage-pagination {
  margin-top: 24px;
  width: 100%;
  display: flex;
  justify-content: center;
}

.color-square {
  display: inline-block;
  width: 22px;
  height: 22px;
  border-radius: 6px;
  margin-right: 8px;
  border: 1.5px solid #e5e7eb;
  vertical-align: middle;
}

.color-blocks {
  display: flex;
  flex-wrap: wrap;
  gap: 18px 24px;
  margin-top: 4px;
  max-width: 320px;
}

.color-block {
  width: 72px;
  height: 36px;
  border-radius: 8px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: border 0.2s;
  box-sizing: border-box;
}

.color-block.selected {
  border: 2px solid #3b5cff;
  box-shadow: 0 0 0 2px #e0e7ff;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  width: 100%;
  gap: 10px;
}

.dialog-btn {
  min-width: 80px;
}

.tag-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.tag-dialog :deep(.el-dialog__header) {
  padding: 16px 20px;
}

.tag-dialog :deep(.el-dialog__footer) {
  padding: 10px 20px 20px;
}

@media (max-width: 768px) {
  .back-btn {
    position: static;
    margin-bottom: 8px;
    font-size: 16px;
  }
  
  .tag-manage-card {
    width: 100%;
    margin: 0;
    border-radius: 0;
    box-shadow: none;
    padding: 16px 8px;
  }
  
  .tag-manage-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    margin-bottom: 16px;
  }
  
  .tag-manage-title {
    font-size: 1.5rem;
  }
  
  .tag-manage-table {
    font-size: 0.9rem;
  }
  
  .tag-manage-table :deep(.el-table__header) th {
    padding: 6px 0;
    font-size: 13px;
  }
  
  .tag-manage-table :deep(.el-table__body) td {
    padding: 6px 0;
  }
  
  .tag-manage-table :deep(.el-button--small) {
    padding: 6px 10px;
    font-size: 12px;
    min-height: 28px;
  }
  
  .action-buttons {
    flex-direction: row;
    gap: 4px;
    justify-content: center;
  }
  
  .tag-manage-pagination {
    justify-content: center;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .color-blocks {
    gap: 12px;
    justify-content: center;
  }
  
  .color-block {
    width: 60px;
    height: 30px;
  }
  
  .dialog-footer {
    justify-content: center;
  }
  
  .dialog-btn {
    flex: 0 0 auto;
    min-width: 80px;
  }
  
  .tag-dialog :deep(.el-dialog__body) {
    padding: 15px 10px;
  }
  
  .tag-dialog :deep(.el-dialog__header) {
    padding: 12px 15px;
  }
  
  .tag-dialog :deep(.el-dialog__title) {
    font-size: 16px;
  }
  
  .tag-dialog :deep(.el-dialog__footer) {
    padding: 10px 15px 15px;
  }
  
  .color-blocks {
    gap: 10px;
    justify-content: space-between;
    max-width: 100%;
  }
  
  .color-block {
    width: calc(25% - 8px);
    height: 28px;
  }
}

@media (max-width: 480px) {
  .tag-dialog :deep(.el-dialog__body) {
    padding: 10px 8px;
  }
  
  .color-block {
    width: calc(33.33% - 7px);
    height: 24px;
  }
  
  .dialog-footer {
    flex-direction: row;
    justify-content: space-between;
  }
  
  .dialog-btn {
    flex: 1;
    min-width: unset;
    padding-left: 10px;
    padding-right: 10px;
  }
}
</style> 