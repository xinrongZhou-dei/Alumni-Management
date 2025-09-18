<template>
  <div>
    <el-button type="text" @click="goHome" style="position: absolute; left: 24px; top: 24px; font-size: 18px;"><el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回</el-button>
    <div v-if="ready" class="alumni-manage-container">
      <div class="alumni-manage-header">
        <div class="alumni-manage-search">
          <input v-model="search.alumniID" placeholder="按学号搜索" @input="debouncedSearch" />
          <input v-model="search.chinese_name" placeholder="按中文姓名搜索" @input="debouncedSearch" />
          <input v-model="search.email" placeholder="按邮箱搜索" @input="debouncedSearch" />
          <input v-model="search.contact_number" placeholder="按电话搜索" @input="debouncedSearch" />
          <select v-model="search.ycyw_schools_attended" @change="debouncedSearch">
            <option value="">按学校搜索</option>
            <option v-for="school in schoolOptions" :key="school" :value="school">{{ school }}</option>
          </select>
          <button @click="handleClear" style="margin-left: 4px; background: #f1f5f9; color: #222; border: 1px solid #cbd5e1;">清空</button>
        </div>
      </div>
      <div class="alumni-manage-header-btns">
        <button v-if="hasEditPermission" class="alumni-manage-add-btn header-action-btn" @click="handleAdd">新增校友</button>
        <button v-if="hasEditPermission" @click="handleBatchDelete" :disabled="selected.length === 0" class="danger-btn header-action-btn batch-btn">批量删除</button>
        <span class="batch-selected-info">已选 {{ selected.length }} 项</span>
      </div>
      
      <!-- 激活状态分类标签 -->
      <div class="alumni-status-tabs">
        <div 
          class="status-tab" 
          :class="{ active: activeStatusTab === 'all' }" 
          @click="setActiveStatusTab('all')"
        >
          全部校友 ({{ totalAlumni }})
        </div>
        <div 
          class="status-tab" 
          :class="{ active: activeStatusTab === 'activated' }" 
          @click="setActiveStatusTab('activated')"
        >
          已激活的校友 ({{ activatedAlumni.length }})
        </div>
        <div 
          class="status-tab" 
          :class="{ active: activeStatusTab === 'unactivated' }" 
          @click="setActiveStatusTab('unactivated')"
        >
          未激活的校友 ({{ unactivatedAlumni.length }})
        </div>
      </div>
      
      <!-- 标签筛选区（加号+下拉多选） -->
      <div class="tag-filter-bar">
        <el-tag
          v-for="tag in filterSelectedTags"
          :key="tag.id"
          :style="{ backgroundColor: tag.color || '#409eff', color: getTagFontColor(tag.color), border: 'none', fontWeight: 'bold', fontSize: '1rem', padding: '0 18px', marginRight: '10px', marginBottom: '8px', borderRadius: '18px', cursor: 'pointer' }"
          effect="plain"
          closable
          @close="removeFilterTag(tag.id)"
        >
          {{ tag.name }}
        </el-tag>
        <div
          v-if="filterSelectedTags.length < 5 && filterAvailableTags.length > 0"
          class="tag-plus-popover-wrap"
          @mouseenter="handlePopoverEnter"
          @mouseleave="handlePopoverLeave"
          style="display:inline-block;position:relative;"
        >
          <el-popover
            placement="bottom-start"
            width="220"
            :trigger="popoverTrigger"
            v-model:visible="tagPopoverVisible"
            :show-arrow="true"
            popper-class="tag-popover-menu"
          >
            <div
              class="tag-popover-list"
              @mouseenter="handlePopoverEnter"
              @mouseleave="handlePopoverLeave"
            >
              <div
                v-for="tag in filterAvailableTags"
                :key="tag.id"
                class="tag-popover-item"
                @click="addFilterTag(tag)"
                :class="{ disabled: filterSelectedTags.length >= 5 }"
              >
                <span class="color-dot" :style="{ background: tag.color, marginRight: '8px' }"></span>{{ tag.name }}
              </div>
              <div v-if="filterAvailableTags.length === 0" style="color:#aaa;text-align:center;padding:12px 0;">暂无可选标签</div>
            </div>
            <template #reference>
              <el-button class="plus-btn" circle size="large" style="margin-bottom: 8px; margin-right: 8px; background: #fff; border: 1.5px solid #d1d5db; color: #2563eb; font-size: 1.5rem; font-weight: bold;">+</el-button>
            </template>
          </el-popover>
        </div>
      </div>
      <div class="alumni-manage-table-wrap">
        <table class="alumni-manage-table">
          <thead>
            <tr>
              <th><input type="checkbox" v-model="selectAll" @change="toggleSelectAll" /></th>
              <th>学号</th>
              <th>中文姓名</th>
              <th>英文名/姓</th>
              <th>YCYW就读学校</th>
              <th>信息最后更新时间</th>
              <th>标签</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in displayedAlumniList" :key="row.alumniId">
              <td><input type="checkbox" v-model="selected" :value="row.alumniId" /></td>
              <td>{{ row.alumniId }}</td>
              <td>{{ row.chineseName }}</td>
              <td>{{ row.firstName }}/{{ row.lastName }}</td>
              <td>{{ row.ycywSchoolsAttended }}</td>
              <td>{{ row.updateTime || '-' }}</td>
              <td style="min-width: 150px; max-width: 300px;">
                <template v-if="row.tagIds">
                  <el-tag
                    v-for="tag in renderTagObjects(row.tagIds).slice(0, 2)"
                    :key="tag.id"
                    :style="{ backgroundColor: tag.color || '#409eff', color: getTagFontColor(tag.color), border: 'none', fontWeight: 'bold', padding: '0 14px', marginRight: '6px', fontSize: '0.82em' }"
                    effect="plain"
                  >
                    {{ tag.name }}
                  </el-tag>
                </template>
              </td>
              <td>
                <el-button type="primary" size="small" round @click="handleView(row)">查看</el-button>
                <el-button v-if="hasEditPermission" type="info" size="small" round @click="handleEdit(row)" style="margin: 0 6px;">编辑</el-button>
                <el-button v-if="hasEditPermission" type="danger" size="small" round @click="handleDelete([row.alumniId])">删除</el-button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="pagination-bar">
        <div class="pagination-info">
          显示第{{ total === 0 ? 0 : (page - 1) * pageSize + 1 }}到{{ Math.min(page * pageSize, total) }}条，共{{ total }}条记录
        </div>
        <el-pagination
          background
          layout="prev, pager, next, jumper"
          :current-page="page"
          :page-size="pageSize"
          :total="total"
          @current-change="val => { page = val; fetchAlumniList(); }"
          class="pagination-el"
        />
      </div>
      <!-- 新增校友弹窗 -->
      <div v-if="showAdd" class="centered-submit alumni-add-modal">
        <form class="submit-card" @submit.prevent="submitAdd">
          <h1 class="submit-title">添加校友邮箱</h1>
          <div class="submit-desc">请填写校友的邮箱，后续由校友本人通过邮箱验证并完善信息。</div>
          <div class="form-row">
            <div class="form-col"><label>电子邮箱 *</label><input v-model="addForm.email" required /></div>
          </div>
          <div class="form-row">
            <div class="form-col"><label>学号</label><input v-model="addForm.alumniId" /></div>
            <div class="form-col"><label>中文姓名</label><input v-model="addForm.chineseName" /></div>
            <div class="form-col"><label>英文名</label><input v-model="addForm.firstName" /></div>
            <div class="form-col"><label>英文姓</label><input v-model="addForm.lastName" /></div>
          </div>
          <div class="form-row">
            <div class="form-col"><label>联系电话</label><input v-model="addForm.contactNumber" /></div>
            <div class="form-col"><label>微信ID</label><input v-model="addForm.wechatId" /></div>
            <div class="form-col"><label>通讯地址</label><input v-model="addForm.correspondenceAddress" /></div>
          </div>
          <div class="form-row">
            <div class="form-col"><label>当前所在地</label><input v-model="addForm.currentLocation" /></div>
            <div class="form-col"><label>Zoho校友编号</label><input v-model="addForm.zohoAlumniNumber" /></div>
            <div class="form-col"><label>身份类型</label>
              <select v-model="addForm.affiliation">
                <option value="Alumni">校友</option>
                <option value="Donor">捐赠者</option>
                <option value="Staff">教职工</option>
              </select>
            </div>
            <div class="form-col"><label>角色</label>
              <select v-model="addForm.role">
                <option value="alumni">校友</option>
                <option value="Admin">管理员</option>
                <option value="SuperAdmin">超级管理员</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-col"><label>就读学校</label><input v-model="addForm.ycywSchoolsAttended" /></div>
            <div class="form-col"><label>就读时间起</label><input v-model="addForm.studyPeriodStart" placeholder="yyyy-MM-dd" /></div>
            <div class="form-col"><label>就读时间止</label><input v-model="addForm.studyPeriodEnd" placeholder="yyyy-MM-dd" /></div>
          </div>
          <div class="form-row">
            <div class="form-col"><label>最后在校日期</label><input v-model="addForm.lastSchoolDay" placeholder="yyyy-MM-dd" /></div>
            <div class="form-col"><label>离校年份</label><input v-model="addForm.yearLeft" type="number" /></div>
            <div class="form-col"><label>婚姻状况</label><input v-model="addForm.maritalStatus" /></div>
          </div>
          <div style="text-align: right; margin-top: 32px;">
            <button type="button" class="cancel-btn" @click="showAdd = false">取消</button>
            <button type="submit" class="submit-btn">提交</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onBeforeUnmount, watch, nextTick } from 'vue'
import axios from 'axios'
import { ElButton, ElTag, ElDropdown, ElDropdownMenu, ElDropdownItem } from 'element-plus'
import 'element-plus/dist/index.css'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createWS } from '@/utils/ws'

// 防抖函数
function debounce(fn, delay) {
  let timer = null
  return function() {
    if (timer) {
      clearTimeout(timer)
    }
    timer = setTimeout(() => {
      fn.apply(this, arguments)
      timer = null
    }, delay)
  }
}

// 预留学校选项
const schoolOptions = ref([])

// 添加管理员权限相关
const adminPermission = ref({
  level: 2, // 默认为完全权限，会被实际权限覆盖
  accessibleSchools: []
})

const search = ref({
  alumniID: '',
  chinese_name: '',
  email: '',
  contact_number: '',
  ycyw_schools_attended: ''
})
const alumniList = ref([])
const total = ref(0)
const selected = ref([])
const selectAll = ref(false)
const page = ref(1)
const pageSize = ref(10)
const router = useRouter()
const showAdd = ref(false)
const addForm = ref({
  alumniId: '',
  chineseName: '',
  firstName: '',
  lastName: '',
  email: '',
  contactNumber: '',
  wechatId: '',
  correspondenceAddress: '',
  currentLocation: '',
  zohoAlumniNumber: '',
  affiliation: 'Alumni',
  ycywSchoolsAttended: '',
  studyPeriodStart: '',
  studyPeriodEnd: '',
  lastSchoolDay: '',
  yearLeft: '',
  maritalStatus: '',
  role: 'alumni'
})
const tagMap = ref({})
const tagColorMap = ref({})
const filterTags = ref([])
const filterSelectedTags = ref([])
const filterAvailableTags = computed(() => filterTags.value.filter(t => !filterSelectedTags.value.some(sel => sel.id === t.id)))

// 判断是否有编辑权限
const hasEditPermission = computed(() => {
  return adminPermission.value.level === 2
})

// 激活状态分类相关
const activeStatusTab = ref('all') // 当前激活的标签：all, activated, unactivated
const activatedAlumni = computed(() => alumniList.value.filter(alumni => alumni.state === 1))
const unactivatedAlumni = computed(() => alumniList.value.filter(alumni => alumni.state === 0))
const totalAlumni = computed(() => alumniList.value.length)
const displayedAlumniList = computed(() => {
  if (activeStatusTab.value === 'all') return alumniList.value
  if (activeStatusTab.value === 'activated') return activatedAlumni.value
  if (activeStatusTab.value === 'unactivated') return unactivatedAlumni.value
  return alumniList.value
})

// 获取校友列表
async function fetchAlumniList() {
  try {
    // 从后端获取权限信息
    const permissionRes = await axios.get('/api/admin/permissions')
    if (permissionRes.data.code === 0) {
      const permissions = permissionRes.data.data
      
      // 设置权限级别 - 使用校友信息管理权限
      if (permissions.alumni_info_management_permission !== undefined) {
        adminPermission.value.level = permissions.alumni_info_management_permission
      } else {
        // 如果找不到具体权限，使用默认值0（无权限）
        adminPermission.value.level = 0
      }
      
      // 处理可访问学校
      if (permissions.accessible_schools) {
        const schools = permissions.accessible_schools.split(',').map(s => s.trim()).filter(s => s)
        adminPermission.value.accessibleSchools = schools
        
        // 更新下拉选项
        if (schools.length > 0) {
          schoolOptions.value = schools
        } else {
          // 使用默认学校列表
          schoolOptions.value = getDefaultSchoolList()
        }
      } else {
        // 如果找不到可访问学校信息，使用空数组
        adminPermission.value.accessibleSchools = []
        // 使用默认学校列表
        schoolOptions.value = getDefaultSchoolList()
      }
    } else {
      console.error('获取权限信息失败:', permissionRes.data.msg)
      // 权限获取失败时，设置为无权限
      adminPermission.value.level = 0
      adminPermission.value.accessibleSchools = []
      schoolOptions.value = getDefaultSchoolList()
    }
    
    // 构建请求参数，包括权限信息
    const params = {
      alumniId: search.value.alumniID || undefined,
      chineseName: search.value.chinese_name || undefined,
      email: search.value.email || undefined,
      contactNumber: search.value.contact_number || undefined,
      ycywSchoolsAttended: search.value.ycyw_schools_attended || undefined,
      page: page.value,
      pageSize: pageSize.value,
      tagIds: filterSelectedTags.value.length > 0 ? filterSelectedTags.value.map(t => t.id).join(',') : undefined
    }
    
    const res = await axios.get('/api/register/alumni-list', { params })
    
    if (res.data.code === 0) {
      alumniList.value = res.data.data.rows
      total.value = res.data.data.total
      selectAll.value = false
    } else {
      alumniList.value = []
      total.value = 0
      ElMessage.error(res.data.msg || '获取校友列表失败')
    }
  } catch (error) {
    console.error('获取校友列表出错:', error)
    ElMessage.error('网络错误，获取校友列表失败')
    alumniList.value = []
    total.value = 0
  }
}

// 获取默认学校列表的辅助函数
function getDefaultSchoolList() {
  return [
    'YCIS Hong Kong',
    'YCIS Shanghai',
    'YCIS Beijing',
    'YCIS Chongqing',
    'YCIS Qingdao',
    'YCIS Yantai',
    'YCIS Silicon Valley',
    'YK Pao School',
    'RDFZ Yew Wah',
    'YWIES Shanghai',
    'YWIES Guangzhou',
    'YWIES Yantai',
    'YWIES Rizhao',
    'YWIES Tongxiang',
    'YWIES Beijing',
    'YWIES Shenzhen',
    'YWIES Hangzhou',
    'YWIES Lingang'
  ]
}

// 添加防抖搜索函数
const debouncedSearch = debounce(() => {
  page.value = 1
  fetchAlumniList()
}, 500) // 500毫秒防抖

function handleSearch() {
  page.value = 1
  fetchAlumniList()
}
function handleAdd() {
  if (adminPermission.value.level !== 2) {
    ElMessage.warning('权限不足，无法新增校友信息')
    return
  }
  router.push('/admin/alumni-add')
}
function handleView(row) {
  router.push(`/alumni/detail/${row.alumniId}`)
}
function handleEdit(row) {
  if (adminPermission.value.level !== 2) {
    ElMessage.warning('权限不足，无法编辑校友信息')
    return
  }
  router.push(`/alumni/edit/${row.alumniId}`)
}
function handleDelete(ids) {
  if (adminPermission.value.level !== 2) {
    ElMessage.warning('权限不足，无法删除校友信息')
    return
  }
  
  if (!ids || ids.length === 0) return;
  ElMessageBox.confirm(`确定要删除选中的${ids.length > 1 ? '校友' : '校友'}信息吗？`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    axios.delete('/api/register/alumni-list', { data: { alumniIds: ids } })
      .then(res => {
        if (res.data.code === 0) {
          ElMessage.success('删除成功');
          fetchAlumniList();
        } else {
          ElMessage.error(res.data.msg || '删除失败');
        }
      })
      .catch(() => {
        ElMessage.error('请求失败');
      });
  }).catch(() => {});
}
function handleBatchDelete() {
  if (adminPermission.value.level !== 2) {
    ElMessage.warning('权限不足，无法批量删除校友信息')
    return
  }
  
  if (selected.value.length === 0) {
    ElMessage.warning('请选择要删除的校友');
    return;
  }
  handleDelete(selected.value);
}
function toggleSelectAll() {
  if (selectAll.value) {
    // 只选择当前显示的校友
    selected.value = displayedAlumniList.value.map(row => row.alumniId)
  } else {
    selected.value = []
  }
}
function prevPage() {
  if (page.value > 1) {
    page.value--
    fetchAlumniList()
  }
}
function nextPage() {
  if (page.value * pageSize.value < total.value) {
    page.value++
    fetchAlumniList()
  }
}
function handleClear() {
  search.value = {
    alumniID: '',
    chinese_name: '',
    email: '',
    contact_number: '',
    ycyw_schools_attended: ''
  }
  page.value = 1
  fetchAlumniList()
}
function handlePageSizeChange() {
  page.value = 1;
  fetchAlumniList();
}
async function submitAdd() {
  try {
    const payload = { ...addForm.value }
    const res = await axios.post('/api/alumni-add', payload)
    if (res.data.code === 0) {
      window.alert('添加成功')
      showAdd.value = false
      fetchAlumniList()
    } else {
      window.alert(res.data.msg || '添加失败')
    }
  } catch (e) {
    window.alert('网络错误')
  }
}
async function fetchTagsForMap() {
  const res = await axios.get('/api/tag/list', { params: { page: 1, pageSize: 1000 } })
  if (res.data.code === 0) {
    tagMap.value = {}
    tagColorMap.value = {}
    filterTags.value = res.data.data.rows.map(tag => ({ id: tag.id, name: tag.name, color: tag.color }))
    for (const tag of res.data.data.rows) {
      tagMap.value[tag.id] = tag.name
      tagColorMap.value[tag.id] = tag.color
    }
  }
}
function renderTagObjects(tagIds) {
  if (!tagIds) return []
  const ids = tagIds.split(',').map(id => id.trim()).filter(Boolean)
  return ids.map(id => ({ id, name: tagMap.value[id], color: tagColorMap.value[id] }))
}
function getTagFontColor(color) {
  if (!color) return '#fff'
  const rgb = color.startsWith('#') ? hexToRgb(color) : [64, 158, 255]
  const brightness = rgb[0] * 0.299 + rgb[1] * 0.587 + rgb[2] * 0.114
  return brightness > 186 ? '#333' : '#fff'
}
function hexToRgb(hex) {
  let c = hex.substring(1)
  if (c.length === 3) c = c.split('').map(x => x + x).join('')
  const num = parseInt(c, 16)
  return [(num >> 16) & 255, (num >> 8) & 255, num & 255]
}
function addFilterTag(tag) {
  if (filterSelectedTags.value.length < 5 && !filterSelectedTags.value.some(t => t.id === tag.id)) {
    filterSelectedTags.value.push(tag)
    // 选择后立即收回popover，不依赖isPC判断
    tagPopoverVisible.value = false
    // 触发搜索/筛选
    debouncedSearch()
  }
}
function removeFilterTag(id) {
  const idx = filterSelectedTags.value.findIndex(t => t.id === id)
  if (idx !== -1) {
    filterSelectedTags.value.splice(idx, 1)
    debouncedSearch()
  }
}
function goHome() { router.push('/') }

const ws = ref(null)
function initWebSocket() {
  ws.value = createWS({
    onMessage: (event) => {
      const msg = event.data;
      if (["alumni changed", "tertiary changed", "career changed"].includes(msg)) {
        fetchAlumniList();
      }
    },
    onClose: () => {
      // 可选：可加提示或其他处理
    }
  })
}

const ready = ref(false)
onMounted(async () => {
  try {
    // 先获取权限
    const permissionRes = await axios.get('/api/admin/permissions')
    if (permissionRes.data.code === 0 && permissionRes.data.data.alumni_info_management_permission > 0) {
      adminPermission.value.level = permissionRes.data.data.alumni_info_management_permission
      // 继续加载数据
      await fetchAlumniList()
      await fetchTagsForMap()
      initWebSocket()
      if (window.matchMedia('(hover: hover) and (pointer: fine)').matches) {
        popoverTrigger.value = 'manual'
        isPC.value = true
      } else {
        popoverTrigger.value = 'click'
        isPC.value = false
      }
      ready.value = true
    } else {
      ElMessage.warning('您没有访问校友信息管理的权限')
      router.push('/')
    }
  } catch (e) {
    ElMessage.error('网络错误，无法获取权限')
    router.push('/')
  }
})

// 监听搜索条件变化
watch(() => search.value, (newVal, oldVal) => {
  // 这里不需要做任何事情，因为我们已经在输入框和选择框上绑定了debouncedSearch
  // 这只是为了在Vue Devtools中能够看到搜索条件的变化
}, { deep: true })

onBeforeUnmount(() => {
  if (ws.value && ws.value._manualClose) ws.value._manualClose();
})

function camelToSnake(str) {
  return str.replace(/[A-Z]/g, letter => `_${letter.toLowerCase()}`);
}

// 获取管理员权限信息
const fetchAdminPermissions = async () => {
  try {
    const res = await service.get('/admin/permissions')
    if (res.code === 0) {
      const permissions = res.data
      adminPermission.value = {
        level: permissions.alumni_info_management_permission || 0,
        schools: permissions.accessible_schools ? permissions.accessible_schools.split(',').filter(s => s.trim()) : []
      }
    }
  } catch (error) {
    console.error('获取管理员权限失败:', error)
  }
}

// 设置当前激活的标签
function setActiveStatusTab(tab) {
  activeStatusTab.value = tab
  // 切换标签时重置选中状态
  selected.value = []
  selectAll.value = false
}

const tagPopoverVisible = ref(false)
const popoverTrigger = ref('click')
const isPC = ref(false)
let popoverCloseTimer = null
function handlePopoverEnter() {
  if (isPC.value) {
    clearTimeout(popoverCloseTimer)
    tagPopoverVisible.value = true
  }
}
function handlePopoverLeave() {
  if (isPC.value) {
    popoverCloseTimer = setTimeout(() => {
      tagPopoverVisible.value = false
    }, 250)
  }
}

</script>

<style scoped>
.alumni-manage-container {
  max-width: 1400px;
  margin: 40px auto 0 auto;
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 2px 16px 0 rgba(0,0,0,0.07);
  padding: 48px 40px 40px 40px;
}
.alumni-manage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}
.alumni-manage-search {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
}
.alumni-manage-search input,
.alumni-manage-search select {
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #cbd5e1;
  font-size: 0.98rem;
  max-width: 174px;
}
.alumni-manage-search button {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 8px 18px;
  font-size: 1rem;
  cursor: pointer;
  margin-left: 6px;
}
.alumni-manage-add-btn {
  background: #22c55e;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 8px 18px;
  font-size: 1rem;
  cursor: pointer;
  box-shadow: 0 2px 8px #c7d2fe33;
}
.alumni-manage-table-wrap {
  overflow-x: auto;
  width: 100%;
}
.alumni-manage-table {
  /* width: max-content;
  max-width: 100%; */
  max-width: 1120px;
  border-collapse: separate;
  border-spacing: 0;
  margin-bottom: 16px;
  font-size: 0.85em;
  background: #fff;
}
.alumni-manage-table th,
.alumni-manage-table td {
  border: 1px solid #e5e7eb;
  padding: 8px 10px;
  text-align: center;
  min-width: 110px;
  white-space: nowrap;
}
.alumni-manage-table th {
  background: #f1f5f9;
  font-size: 1.05rem;
  position: sticky;
  top: 0;
  z-index: 2;
}
.alumni-manage-table tr {
  height: 38px;
}
.alumni-manage-header-btns {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10px;
  margin: 18px 0 0 0;
  justify-content: flex-start;
}
.header-action-btn {
  height: 38px;
  min-width: 96px;
  padding: 0 18px;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  box-shadow: none;
  margin: 0;
  transition: background 0.2s, color 0.2s;
}
.alumni-manage-add-btn.header-action-btn {
  background: #22c55e;
  color: #fff;
  border: none;
}
.alumni-manage-add-btn.header-action-btn:active,
.alumni-manage-add-btn.header-action-btn:focus {
  background: #16a34a;
}
.danger-btn.header-action-btn {
  background: #f87171;
  color: #fff;
  border: none;
}
.danger-btn.header-action-btn:active,
.danger-btn.header-action-btn:focus {
  background: #dc2626;
}
.danger-btn.header-action-btn:disabled {
  background: #fca5a5;
  color: #fff;
  cursor: not-allowed;
}
.batch-selected-info {
  color: #888;
  font-size: 14px;
  margin-left: 12px;
  white-space: nowrap;
  align-self: center;
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
@media (max-width: 900px) {
  .alumni-manage-container {
    padding: 8px 2px;
    max-width: 100vw;
    border-radius: 0;
    box-shadow: none;
  }
  .alumni-manage-header {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  .alumni-manage-search {
    flex-wrap: wrap;
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
    width: 100%;
  }
  .alumni-manage-search input,
  .alumni-manage-search select {
    max-width: 100%;
    width: 100%;
    margin-bottom: 4px;
    font-size: 1rem;
  }
  .alumni-manage-search button {
    width: 100%;
    margin: 0 0 4px 0;
    font-size: 1rem;
    padding: 10px 0;
  }
  .alumni-manage-add-btn {
    width: 100%;
    font-size: 1rem;
    padding: 10px 0;
  }
  .alumni-status-tabs {
    flex-direction: column;
    gap: 4px;
    margin: 10px 0;
  }
  .status-tab {
    margin-right: 0;
    width: 100%;
    text-align: left;
    font-size: 1rem;
    padding: 10px 8px;
  }
  .tag-filter-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 8px 0;
    min-height: unset;
  }
  .alumni-manage-table-wrap {
    width: 100vw;
    margin-left: -8px;
    padding-bottom: 8px;
    overflow-x: auto;
  }
  .alumni-manage-table {
    min-width: 700px;
    font-size: 0.95em;
  }
  .alumni-manage-table th,
  .alumni-manage-table td {
    font-size: 0.92rem;
    padding: 6px 4px;
    min-width: 80px;
    white-space: nowrap;
  }
  .alumni-manage-header-btns {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
    width: 100%;
    margin: 10px 0 0 0;
  }
  .header-action-btn {
    width: 100%;
    min-width: 0;
    height: 38px;
    font-size: 1rem;
  }
  .batch-selected-info {
    margin-left: 0;
    font-size: 13px;
    text-align: left;
    margin-top: 2px;
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
  .centered-submit {
    min-height: 100vh;
    padding: 0;
  }
  .submit-card {
    width: 98vw;
    min-width: unset;
    padding: 18px 4px;
    border-radius: 8px;
    box-shadow: 0 2px 8px #eee;
  }
  .form-row {
    flex-direction: column;
    gap: 8px;
    margin-bottom: 10px;
  }
  .form-col {
    min-width: unset;
    width: 100%;
  }
  .submit-title {
    font-size: 1.3rem;
  }
  .submit-desc {
    font-size: 1rem;
  }
  .cancel-btn, .submit-btn {
    width: 48%;
    min-width: 90px;
    font-size: 1rem;
    padding: 8px 0;
    margin: 0 1%;
  }
}
.centered-submit {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
  background: none;
}
.submit-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 8px #eee;
  padding: 32px 40px;
  width: 900px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.submit-title {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 12px;
  text-align: center;
}
.submit-desc {
  color: #64748b;
  font-size: 1.1rem;
  text-align: center;
}
.submit-subtitle {
  font-size: 1.3rem;
  font-weight: bold;
  margin-bottom: 16px;
  text-align: left;
}
.form-row {
  margin-bottom: 16px;
  display: flex;
  flex-direction: row;
  gap: 18px;
}
.form-col {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 180px;
}
.cancel-btn {
  background: #f1f5f9;
  color: #222;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  padding: 8px 24px;
  font-size: 1rem;
  margin-right: 16px;
  cursor: pointer;
}
.submit-btn {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 8px 32px;
  font-size: 1rem;
  cursor: pointer;
}
.alumni-add-modal {
  position: fixed;
  left: 0; top: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}
.tag-filter-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0 8px;
  margin: 18px 0 10px 0;
  min-height: 44px;
}
.plus-btn {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: bold;
  box-shadow: 0 2px 8px #e0e7ff33;
  transition: border 0.2s;
}
.color-dot {
  display: inline-block;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  margin-right: 8px;
  border: 1.5px solid #e5e7eb;
  vertical-align: middle;
}
.alumni-status-tabs {
  display: flex;
  margin: 16px 0;
  border-bottom: 1px solid #e2e8f0;
}

.status-tab {
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  border-bottom: 2px solid transparent;
  margin-right: 24px;
  transition: all 0.2s;
}

.status-tab.active {
  color: #2563eb;
  border-bottom-color: #2563eb;
  font-weight: 500;
}

.status-tab:hover {
  color: #2563eb;
}
.danger-btn {
  background: #ef4444;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 8px 24px;
  font-size: 1rem;
  cursor: pointer;
  box-shadow: 0 2px 8px #fecaca33;
  transition: background 0.2s;
}
.danger-btn:disabled {
  background: #fca5a5;
  color: #fff;
  cursor: not-allowed;
}
.danger-btn:not(:disabled):hover {
  background: #dc2626;
}
.tag-popover-menu {
  padding: 0;
}
.tag-popover-list {
  max-height: 300px;
  overflow-y: auto;
  padding: 4px 0;
}
.tag-popover-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 15px;
  transition: background 0.15s;
}
.tag-popover-item:hover {
  background: #f5f7fa;
}
.tag-popover-item.disabled {
  pointer-events: none;
  opacity: 0.5;
}
</style> 