<template>
  <div class="alumni-edit">
    <el-button type="text" @click="goManage" class="back-btn">
      <el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回
    </el-button>
    <el-card class="edit-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">校友详细信息</span>
          <div v-if="route.query.from === '/alumni/review' && alumni && alumni.state !== 3" class="review-actions">
            <el-button type="primary" @click="handleMatchGMIS" class="review-btn">匹配GMIS数据</el-button>
            <el-button type="success" @click="handleApprove" class="review-btn">审核通过</el-button>
            <el-button type="danger" @click="handleReject" class="review-btn">审核拒绝</el-button>
          </div>
        </div>
      </template>
      <div v-if="loading">加载中...</div>
      <template v-else-if="alumni">
        <div class="form-section">
          <div class="section-title">基本信息</div>
          <div class="info-grid">
            <div class="info-item" v-for="(item, idx) in basicInfoList" :key="idx">
              <div class="info-label">{{ item.label }}</div>
              <div :class="['info-value', item.value ? 'info-value-filled' : 'info-value-empty']">{{ item.value || '-' }}</div>
            </div>
          </div>
        </div>
        <div class="form-section">
          <div class="section-title">教育信息</div>
          <div v-if="tertiaryList.length === 0" style="color:#888;">暂无学籍信息</div>
          <div v-for="(edu, idx) in tertiaryList" :key="idx" class="info-grid">
            <div class="info-item">
              <div class="info-label">大学/学院</div>
              <div :class="['info-value', edu.universityCollege ? 'info-value-filled' : 'info-value-empty']">{{ edu.universityCollege || '-' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">学位</div>
              <div :class="['info-value', edu.degree ? 'info-value-filled' : 'info-value-empty']">{{ edu.degree || '-' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">专业</div>
              <div :class="['info-value', edu.major ? 'info-value-filled' : 'info-value-empty']">{{ edu.major || '-' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">毕业年份</div>
              <div :class="['info-value', edu.graduationYear ? 'info-value-filled' : 'info-value-empty']">{{ edu.graduationYear || '-' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">国家/地区</div>
              <div :class="['info-value', edu.countryRegion ? 'info-value-filled' : 'info-value-empty']">{{ edu.countryRegion || '-' }}</div>
            </div>
          </div>
        </div>
        <div class="form-section">
          <div class="section-title">工作信息</div>
          <div v-if="careerList.length === 0" style="color:#888;">暂无工作经历</div>
          <div v-for="(work, idx) in careerList" :key="idx" class="info-grid">
            <div class="info-item">
              <div class="info-label">公司/组织</div>
              <div :class="['info-value', work.companyOrganization ? 'info-value-filled' : 'info-value-empty']">{{ work.companyOrganization || '-' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">职位</div>
              <div :class="['info-value', work.jobTitle ? 'info-value-filled' : 'info-value-empty']">{{ work.jobTitle || '-' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">行业</div>
              <div :class="['info-value', work.industry ? 'info-value-filled' : 'info-value-empty']">{{ work.industry || '-' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">国家/地区</div>
              <div :class="['info-value', work.countryRegion ? 'info-value-filled' : 'info-value-empty']">{{ work.countryRegion || '-' }}</div>
            </div>
          </div>
        </div>
        <div class="form-section" v-if="alumni.tagIds">
          <div class="section-title">标签</div>
          <div>
            <el-tag
              v-for="tag in renderTagObjects(alumni.tagIds)"
              :key="tag.id"
              :style="{ backgroundColor: tag.color || '#409eff', color: getTagFontColor(tag.color), border: 'none', fontWeight: 'bold', fontSize: '1rem', padding: '0 14px', marginRight: '6px', marginBottom: '6px' }"
              effect="plain"
            >
              {{ tag.name }}
            </el-tag>
          </div>
        </div>
      </template>
      <div v-else>未找到该校友信息</div>
    </el-card>
    <el-dialog v-model="matchGmiDialogVisible" title="GMI数据匹配结果" width="60%" :close-on-click-modal="false">
      <el-table :data="matchGmiResult" v-if="matchGmiResult.length">
        <el-table-column prop="alumniId" label="学号" />
        <el-table-column prop="chineseName" label="中文姓名" />
        <el-table-column label="英文姓名">
          <template #default="scope">{{ scope.row.firstName }}/{{ scope.row.lastName }}</template>
        </el-table-column>
        <el-table-column prop="birthday" label="出生日期" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button type="success" size="small" @click="handleMatchSuccess(scope.row)">匹配</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-else style="color:#888;">未匹配到任何GMI数据</div>
      <template #footer>
        <el-button @click="matchGmiDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElButton, ElTag, ElMessage, ElDialog, ElTable, ElTableColumn } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const alumni = ref(null)
const tertiaryList = ref([])
const careerList = ref([])
const loading = ref(true)
const tagMap = ref({})
const tagColorMap = ref({})
const matchGmiDialogVisible = ref(false)
const matchGmiResult = ref([])

const basicInfoList = computed(() => [
  { label: 'Alumni ID', value: alumni.value?.alumniId },
  { label: '称谓', value: alumni.value?.salutation },
  { label: '英文姓名', value: alumni.value ? alumni.value.firstName + '/' + alumni.value.lastName : '' },
  { label: '中文姓名', value: alumni.value?.chineseName },
  { label: '电子邮箱', value: alumni.value?.email },
  { label: '联系电话', value: alumni.value?.contactNumber },
  { label: '出生日期', value: alumni.value?.birthday },
  { label: '微信ID', value: alumni.value?.wechatId },
  { label: '通讯地址', value: alumni.value?.correspondenceAddress },
  { label: '当前所在地', value: alumni.value?.currentLocation },
  { label: 'Zoho校友编号', value: alumni.value?.zohoAlumniNumber },
  { label: '身份类型', value: alumni.value?.affiliation },
  { label: '就读学校', value: alumni.value?.ycywSchoolsAttended },
  { label: '所在地区', value: alumni.value?.region },
  { label: '就读时间', value: alumni.value ? (alumni.value.studyPeriodStart + ' ~ ' + alumni.value.studyPeriodEnd) : '' },
  { label: '最后在校日期', value: alumni.value?.lastSchoolDay },
  { label: '婚姻状况', value: alumni.value?.maritalStatus },
  { label: '信息更新时间', value: alumni.value?.updateTime },
])

async function fetchTagsForMap() {
  const res = await axios.get('/api/tag/list', { params: { page: 1, pageSize: 1000 } })
  if (res.data.code === 0) {
    tagMap.value = {}
    tagColorMap.value = {}
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
onMounted(async () => {
  const id = route.params.id
  try {
    const res = await axios.get(`/api/register/alumni-detail/${id}`)
    if (res.data.code === 0 && res.data.data) {
      alumni.value = res.data.data.alumni
      tertiaryList.value = res.data.data.tertiary_information || []
      careerList.value = res.data.data.carrer_information || []
    }
    await fetchTagsForMap()
  } finally {
    loading.value = false
  }
})
function goBack() {
  router.back()
}
function goManage() {
  const from = route.query.from
  if (from === '/alumni/review') {
    router.push('/alumni/review')
  } else {
    router.push('/alumni/manage')
  }
}
async function handleMatchGMIS() {
  if (!alumni.value) return
  const req = {
    chineseName: alumni.value.chineseName,
    englishName: (alumni.value.firstName || '') + (alumni.value.lastName || ''),
    birthday: alumni.value.birthday
  }
  try {
    const res = await axios.post('/api/register/match-gmi', req)
    if (res.data.code === 0) {
      matchGmiResult.value = res.data.data || []
      matchGmiDialogVisible.value = true
    } else {
      ElMessage.error(res.data.msg || '匹配失败')
    }
  } catch {
    ElMessage.error('网络错误，匹配失败')
  }
}
function handleApprove() {
  if (!alumni.value || !alumni.value.alumniId) return
  ElMessage.info('正在提交审核...')
  axios.post('/api/register/audit-approve', { alumniId: alumni.value.alumniId })
    .then(res => {
      if (res.data.code === 0) {
        ElMessage.success('审核通过，已激活！')
        router.push({ path: '/email/send', query: { alumniId: alumni.value.alumniId, from: 'review' } })
      } else {
        ElMessage.error(res.data.msg || '审核失败')
      }
    })
    .catch(() => {
      ElMessage.error('网络错误，审核失败')
    })
}
function handleReject() {
  if (!alumni.value || !alumni.value.alumniId) return
  ElMessage.info('正在提交审核拒绝...')
  axios.post('/api/register/audit-reject', { alumniId: alumni.value.alumniId })
    .then(res => {
      if (res.data.code === 0) {
        ElMessage.success('审核已拒绝，已归档！')
        router.push({ path: '/email/send', query: { alumniId: res.data.newAlumniId, from: 'review' } })
      } else {
        ElMessage.error(res.data.msg || '审核拒绝失败')
      }
    })
    .catch(() => {
      ElMessage.error('网络错误，审核拒绝失败')
    })
}
async function handleMatchSuccess(row) {
  try {
    const res = await axios.post('/api/register/match-and-activate', {
      gmiAlumniId: row.alumniId,
      reviewAlumniId: alumni.value.alumniId
    })
    if (res.data.code === 0) {
      ElMessage.success('匹配成功，已激活并删除原GMI数据！')
      matchGmiResult.value = matchGmiResult.value.filter(item => item.alumniId !== row.alumniId)
      // 跳转到邮件发送页面
      router.push({ path: '/email/send', query: { alumniId: alumni.value.alumniId, from: 'review' } })
    } else {
      ElMessage.error(res.data.msg || '操作失败')
    }
  } catch {
    ElMessage.error('网络错误，操作失败')
  }
}
</script>

<style scoped>
.alumni-edit {
  padding: 24px;
  min-height: 100vh;
  background-color: #f5f7fa;
}
.back-btn {
  position: absolute;
  left: 24px;
  top: 24px;
  font-size: 18px;
}
.edit-card {
  max-width: 1200px;
  margin: 60px auto 0;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}
.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
.form-section {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
  padding-left: 12px;
  border-left: 4px solid #409eff;
}
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px 24px;
  margin-bottom: 8px;
}
.info-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}
.info-label {
  font-size: 1rem;
  font-weight: bold;
  color: #444;
  margin-bottom: 2px;
}
.info-value {
  width: 100%;
  min-height: 32px;
  border-radius: 6px;
  border: 1.5px solid #e5e7eb;
  font-size: 1.08rem;
  padding: 6px 12px;
  background: #f3f4f6;
  color: #222;
  transition: background 0.2s;
}
.info-value-filled {
  background: #f3f4f6;
  border-color: #e5e7eb;
}
.info-value-empty {
  background: #fafbfc;
  color: #bbb;
  border-style: dashed;
}
.review-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.review-btn {
  min-width: 120px;
  font-size: 1rem;
}
@media (max-width: 900px) {
  .alumni-edit {
    padding: 4px;
    min-height: 100vh;
    background-color: #f5f7fa;
  }
  .back-btn {
    position: static;
    margin-bottom: 8px;
    font-size: 16px;
    left: unset;
    top: unset;
  }
  .edit-card {
    max-width: 100vw;
    margin: 0;
    border-radius: 0;
    box-shadow: none;
    padding: 0;
  }
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    padding: 12px 8px;
  }
  .header-title {
    font-size: 1.1rem;
  }
  .form-section {
    padding: 10px 4px;
    margin-bottom: 12px;
    border-radius: 0;
    box-shadow: none;
  }
  .section-title {
    font-size: 1rem;
    margin-bottom: 10px;
    padding-left: 6px;
    border-left-width: 3px;
  }
  .info-grid {
    grid-template-columns: 1fr;
    gap: 10px 0;
  }
  .info-label {
    font-size: 0.98rem;
    color: #444;
  }
  .info-value {
    font-size: 0.98rem;
    padding: 6px 8px;
    background: #f3f4f6;
    border-color: #e5e7eb;
  }
  .review-actions {
    position: static;
    flex-direction: column;
    gap: 8px;
    width: 100%;
    margin-top: 12px;
  }
  .review-btn {
    width: 100%;
    min-width: unset;
    margin: 0 !important;
    padding: 10px 0;
    font-size: 1rem;
    height: auto;
  }
}
</style> 