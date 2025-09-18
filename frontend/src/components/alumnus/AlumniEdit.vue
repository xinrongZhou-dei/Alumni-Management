<template>
  <div class="alumni-edit">
    <el-button type="text" @click="goManage" class="back-btn">
      <el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回
    </el-button>
    <el-card class="edit-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">编辑校友信息</span>
        </div>
      </template>

      <el-form @submit.prevent="onSubmit" class="edit-form" size="large">
        <div class="form-section">
          <div class="section-title">基本信息</div>
          <div class="info-grid">
            <div class="info-item">
              <div class="info-label">Alumni ID</div>
              <div :class="['info-value', form.alumniId ? 'info-value-filled' : 'info-value-empty']"><el-input v-model="form.alumniId" disabled /></div>
            </div>
            <div class="info-item">
              <div class="info-label">称谓</div>
              <div :class="['info-value', form.salutation ? 'info-value-filled' : 'info-value-empty']"><el-select v-model="form.salutation" placeholder="请选择" class="full-width"><el-option v-for="t in titles" :key="t.value" :label="t.label" :value="t.value" /></el-select></div>
            </div>
            <div class="info-item">
              <div class="info-label">英文姓名</div>
              <div :class="['info-value', (form.firstName || form.lastName) ? 'info-value-filled' : 'info-value-empty']"><div class="name-inputs"><el-input v-model="form.firstName" placeholder="First Name" /><el-input v-model="form.lastName" placeholder="Last Name" /></div></div>
            </div>
            <div class="info-item">
              <div class="info-label">所在地区</div>
              <div :class="['info-value', form.region ? 'info-value-filled' : 'info-value-empty']"><el-select v-model="form.region" placeholder="请选择" class="full-width"><el-option v-for="region in regionOptions" :key="region.value" :label="region.label" :value="region.value" /></el-select></div>
            </div>
            <div class="info-item">
              <div class="info-label">中文姓名</div>
              <div :class="['info-value', form.chineseName ? 'info-value-filled' : 'info-value-empty']"><el-input v-model="form.chineseName" /></div>
            </div>
            <div class="info-item">
              <div class="info-label">电子邮箱</div>
              <div :class="['info-value', form.email ? 'info-value-filled' : 'info-value-empty']"><el-input v-model="form.email" /></div>
            </div>
            <div class="info-item">
              <div class="info-label">联系电话</div>
              <div :class="['info-value', form.contactNumber ? 'info-value-filled' : 'info-value-empty']"><el-input v-model="form.contactNumber" /></div>
            </div>
            <div class="info-item">
              <div class="info-label">微信ID</div>
              <div :class="['info-value', form.wechatId ? 'info-value-filled' : 'info-value-empty']"><el-input v-model="form.wechatId" /></div>
            </div>
            <div class="info-item">
              <div class="info-label">通讯地址</div>
              <div :class="['info-value', form.correspondenceAddress ? 'info-value-filled' : 'info-value-empty']"><el-input v-model="form.correspondenceAddress" /></div>
            </div>
            <div class="info-item">
              <div class="info-label">当前所在地</div>
              <div :class="['info-value', form.currentLocation ? 'info-value-filled' : 'info-value-empty']"><el-input v-model="form.currentLocation" /></div>
            </div>
            <div class="info-item">
              <div class="info-label">Zoho校友编号</div>
              <div :class="['info-value', form.zohoAlumniNumber ? 'info-value-filled' : 'info-value-empty']"><el-input v-model="form.zohoAlumniNumber" /></div>
            </div>
            <div class="info-item">
              <div class="info-label">身份类型</div>
              <div :class="['info-value', form.affiliation ? 'info-value-filled' : 'info-value-empty']"><el-select v-model="form.affiliation" placeholder="请选择" class="full-width"><el-option v-for="i in identities" :key="i.value" :label="i.label" :value="i.value" /></el-select></div>
            </div>
            <div class="info-item">
              <div class="info-label">就读学校</div>
              <div :class="['info-value', form.ycywSchoolsAttended ? 'info-value-filled' : 'info-value-empty']"><el-select v-model="form.ycywSchoolsAttended" placeholder="请选择" class="full-width"><el-option v-for="s in schools" :key="s" :label="s" :value="s" /></el-select></div>
            </div>
            <div class="info-item">
              <div class="info-label">就读时间</div>
              <div :class="['info-value', form.studyPeriodStart ? 'info-value-filled' : 'info-value-empty']"><el-date-picker v-model="form.studyPeriodStart" type="date" placeholder="开始日期" value-format="YYYY-MM-DD" class="full-width" /></div>
            </div>
            <div class="info-item">
              <div class="info-label">就读时间</div>
              <div :class="['info-value', form.studyPeriodEnd ? 'info-value-filled' : 'info-value-empty']"><el-date-picker v-model="form.studyPeriodEnd" type="date" placeholder="结束日期" value-format="YYYY-MM-DD" class="full-width" /></div>
            </div>
            <div class="info-item">
              <div class="info-label">最后在校日期</div>
              <div :class="['info-value', form.lastSchoolDay ? 'info-value-filled' : 'info-value-empty']"><el-date-picker v-model="form.lastSchoolDay" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" class="full-width" /></div>
            </div>
            <div class="info-item">
              <div class="info-label">离校年份</div>
              <div :class="['info-value', form.yearLeft ? 'info-value-filled' : 'info-value-empty']"><el-input v-model="form.yearLeft" /></div>
            </div>
            <div class="info-item">
              <div class="info-label">婚姻状况</div>
              <div :class="['info-value', form.maritalStatus ? 'info-value-filled' : 'info-value-empty']"><el-select v-model="form.maritalStatus" placeholder="请选择" class="full-width"><el-option v-for="m in maritalOptions" :key="m.value" :label="m.label" :value="m.value" /></el-select></div>
            </div>
            <div class="info-item">
              <div class="info-label">出生日期</div>
              <div :class="['info-value', form.birthday ? 'info-value-filled' : 'info-value-empty']"><el-date-picker v-model="form.birthday" type="date" placeholder="请选择出生日期" value-format="YYYY-MM-DD" class="full-width" /></div>
            </div>
                </div>
        </div>

        <div class="form-section">
          <div class="section-title">教育信息</div>
          <div class="section-tip">注：校友名录中将展示第一条教育经历，如需展示最新教育，请将其拖动到最前面。</div>
          <draggable v-model="tertiaryList" item-key="id" :animation="200" handle=".drag-handle">
            <template #item="{element: edu, index: idx}">
              <div v-if="!edu.deleted" class="edu-block">
                <el-row :gutter="20">
                  <el-col :span="1">
                    <span class="drag-handle"><el-icon><Rank /></el-icon></span>
                  </el-col>
                  <el-col :span="7">
                    <el-form-item label="大学/学院">
                      <el-input v-model="edu.universityCollege" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="7">
                    <el-form-item label="学位">
                      <el-input v-model="edu.degree" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="7">
                    <el-form-item label="毕业年份">
                      <el-input v-model="edu.graduationYear" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="2">
                    <el-button type="danger" circle @click="markDeleteEducation(idx)">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </el-col>
                </el-row>
                <el-row :gutter="20">
                  <el-col :span="11">
                    <el-form-item label="专业">
                      <el-input v-model="edu.major" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="11">
                    <el-form-item label="国家/地区">
                      <el-input v-model="edu.countryRegion" />
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>
            </template>
          </draggable>
          <el-button type="primary" plain @click="addEducation" class="add-btn">
            <el-icon><Plus /></el-icon>添加教育经历
          </el-button>
        </div>

        <div class="form-section">
          <div class="section-title">工作信息</div>
          <div class="section-tip">注：校友名录中将展示第一条工作经历，如需展示最新工作，请将其拖动到最前面。</div>
          <draggable v-model="careerList" item-key="id" :animation="200" handle=".drag-handle">
            <template #item="{element: work, index: idx}">
              <div v-if="!work.deleted" class="work-block">
                <el-row :gutter="20">
                  <el-col :span="1">
                    <span class="drag-handle"><el-icon><Rank /></el-icon></span>
                  </el-col>
                  <el-col :span="5">
                    <el-form-item label="公司/组织">
                      <el-input v-model="work.companyOrganization" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="5">
                    <el-form-item label="职位">
                      <el-input v-model="work.jobTitle" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="5">
                    <el-form-item label="行业">
                      <el-select v-model="work.industry" placeholder="请选择" class="full-width">
                        <el-option v-for="industry in industryOptions" :key="industry" :label="industry" :value="industry" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="5">
                    <el-form-item label="国家/地区">
                      <el-input v-model="work.countryRegion" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="3">
                    <el-button type="danger" circle @click="markDeleteWork(idx)">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </el-col>
                </el-row>
              </div>
            </template>
          </draggable>
          <el-button type="primary" plain @click="addWork" class="add-btn">
            <el-icon><Plus /></el-icon>添加工作经历
          </el-button>
        </div>

        <div class="form-section" v-if="isAdminEdit">
          <div class="section-title">标签</div>
          <div class="tag-container">
            <el-tag
              v-for="tag in selectedTags"
              :key="tag.id"
              :style="{ backgroundColor: tag.color || '#409eff', color: getContrastYIQ(tag.color) === 'black' ? '#222' : '#fff' }"
              effect="plain"
              closable
              @close="removeTag(tag.id)"
              class="custom-tag"
            >
              {{ tag.name }}
            </el-tag>
            <el-popover
              placement="bottom-start"
              width="220"
              :trigger="popoverTrigger"
              v-model:visible="tagPopoverVisible"
              :show-arrow="true"
              popper-class="tag-popover-menu"
            >
              <div class="tag-popover-list">
                <div
                    v-for="tag in availableTags"
                    :key="tag.id"
                  class="tag-popover-item"
                    @click="addTag(tag)"
                  :class="{ disabled: selectedTagIds.length >= 5 }"
                  >
                    <span class="color-dot" :style="{ background: tag.color }"></span>
                    {{ tag.name }}
                </div>
                <div v-if="availableTags.length === 0" style="color:#aaa;text-align:center;padding:12px 0;">暂无可选标签</div>
              </div>
              <template #reference>
                <el-button class="plus-btn" circle :disabled="selectedTagIds.length >= 5">
                  <el-icon><Plus /></el-icon>
                </el-button>
              </template>
            </el-popover>
          </div>
        </div>

        <div class="form-section">
          <div class="section-title">信息展示设置</div>
          <div class="section-tip">姓名和YCYW Schools Attended为必展示信息，无法修改。</div>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item>
                <el-checkbox v-model="form.showYearLeft">离校年份</el-checkbox>
                <el-checkbox v-model="form.showTertiaryUniversity">大学/学院</el-checkbox>
                <el-checkbox v-model="form.showTertiaryMajor">专业</el-checkbox>
                <el-checkbox v-model="form.showCareerCompany">公司/组织</el-checkbox>
                <el-checkbox v-model="form.showJobTitle">职位</el-checkbox>
                <el-checkbox v-model="form.showIndustry">行业</el-checkbox>
                <el-checkbox v-model="form.showCountry">国家/地区</el-checkbox>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div class="form-actions">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" @click="onSubmit">保存</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElTag, ElButton, ElDropdown, ElDropdownMenu, ElDropdownItem } from 'element-plus'
import { ArrowLeft, Plus, Delete, Rank } from '@element-plus/icons-vue'
import draggable from 'vuedraggable'

const router = useRouter()
const route = useRoute()
const form = ref({
  showYearLeft: false,
  showTertiaryUniversity: false,
  showTertiaryMajor: false,
  showCareerCompany: false,
  showJobTitle: false,
  showIndustry: false,
  showCountry: false,
  region: '',
  birthday: ''
})
const tertiaryList = ref([])
const careerList = ref([])
const loading = ref(true)
const tagList = ref([])
const selectedTagIds = ref([])
const isAdminEdit = computed(() => !!route.params.id)
const tagPopoverVisible = ref(false)
const popoverTrigger = ref('click')
const isPC = ref(false)

const titles = [
  { label: 'Prof', value: 'Prof' },
  { label: 'Dr', value: 'Dr' },
  { label: 'Mr', value: 'Mr' },
  { label: 'Mrs', value: 'Mrs' },
  { label: 'Ms', value: 'Ms' }
]
const identities = [
  { label: '校友', value: 'Alumni' },
  { label: '捐赠者', value: 'Donor' },
  { label: '教职工', value: 'Staff' }
]
const schools = [
  'YCIS Hong Kong', 'YCIS Shanghai', 'YCIS Beijing', 'YCIS Chongqing', 'YCIS Qingdao',
  'YWIES Beijing', 'YWIES Guangzhou', 'YWIES Shanghai Gubei', 'YWIES Shanghai Lingang',
  'YWIES Tongxiang', 'YWIES Yantai'
]
const maritalOptions = [
  { label: '单身', value: '单身' },
  { label: '已婚', value: '已婚' },
  { label: '其它', value: '其它' }
]
const regionOptions = [
  { label: '大陆', value: '大陆' },
  { label: '香港', value: '香港' },
  { label: '海外', value: '海外' }
]

// 行业选项
const industryOptions = [
  '信息技术与互联网',
  '金融与经济',
  '医疗与健康',
  '教育与科研',
  '工程与制造',
  '创意与媒体',
  '销售与市场',
  '法律与公共事务',
  '服务与旅游',
  '农林与牧渔',
  '交通与运输',
  '其他'
]

function addEducation() {
  tertiaryList.value.push({ universityCollege: '', degree: '', major: '', graduationYear: '', countryRegion: '' })
}
function removeEducation(idx) {
  if (tertiaryList.value.length > 1) {
    tertiaryList.value.splice(idx, 1)
  }
}
function addWork() {
  careerList.value.push({ companyOrganization: '', jobTitle: '', industry: '', countryRegion: '' })
}
function removeWork(idx) {
  if (careerList.value.length > 1) {
    careerList.value.splice(idx, 1)
  }
}

function getContrastYIQ(hexcolor) {
  if (!hexcolor) return 'black';
  hexcolor = hexcolor.replace('#', '');
  if (hexcolor.length === 3) hexcolor = hexcolor.split('').map(x => x + x).join('');
  const r = parseInt(hexcolor.substr(0,2),16);
  const g = parseInt(hexcolor.substr(2,2),16);
  const b = parseInt(hexcolor.substr(4,2),16);
  const yiq = ((r*299)+(g*587)+(b*114))/1000;
  return (yiq >= 186) ? 'black' : 'white';
}
const selectedTags = computed(() => tagList.value.filter(tag => selectedTagIds.value.includes(String(tag.id))))
const availableTags = computed(() => tagList.value.filter(tag => !selectedTagIds.value.includes(String(tag.id))))
function addTag(tag) {
  if (selectedTagIds.value.length < 5 && !selectedTagIds.value.includes(String(tag.id))) {
    selectedTagIds.value.push(String(tag.id))
    // 选择后立即收回popover，不依赖isPC判断
    tagPopoverVisible.value = false
  }
}
function removeTag(id) {
  const idx = selectedTagIds.value.indexOf(String(id))
  if (idx !== -1) selectedTagIds.value.splice(idx, 1)
}
async function fetchTagList() {
  const res = await axios.get('/api/tag/list', { params: { page: 1, pageSize: 1000 } })
  if (res.data.code === 0) tagList.value = res.data.data.rows
}

onMounted(async () => {
  // 优先使用路由参数id（管理员编辑），否则用当前登录用户id（自助编辑）
  let alumniId = route.params.id
  if (!alumniId) {
    try {
      const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')
      alumniId = userInfo.alumniId || userInfo.alumniID || userInfo.alumni_id || userInfo.id || ''
    } catch {}
  }
  if (!alumniId) {
    router.push('/login')
    return
  }
  try {
    const res = await axios.get(`/api/register/alumni-detail/${alumniId}`)
    if (res.data.code === 0 && res.data.data) {
      form.value = res.data.data.alumni || {}
      // 不再处理密码字段
      if (!form.value.salutation) form.value.salutation = ''
      if (!form.value.maritalStatus) form.value.maritalStatus = ''
      if (form.value.maritalStatus && !maritalOptions.some(opt => opt.value === form.value.maritalStatus)) {
        form.value.maritalStatus = '其它'
      }
      // 归一化信息展示设置字段为布尔值
      const showFields = [
        'showYearLeft', 'showTertiaryUniversity', 'showTertiaryMajor',
        'showCareerCompany', 'showJobTitle', 'showIndustry', 'showCountry'
      ]
      showFields.forEach(key => {
        form.value[key] = !!form.value[key]
      })
      // 教育经历数据类型归一化，所有字段转为字符串，避免类型不一致导致表单不显示
      if (res.data.data.tertiary_information && res.data.data.tertiary_information.length > 0) {
        tertiaryList.value = res.data.data.tertiary_information.map(item => ({
          universityCollege: item.universityCollege || '',
          degree: item.degree || '',
          major: item.major || '',
          graduationYear: item.graduationYear != null ? String(item.graduationYear) : '',
          countryRegion: item.countryRegion || '',
          deleted: false
        }))
      } else {
        tertiaryList.value = []
      }
      careerList.value = res.data.data.carrer_information && res.data.data.carrer_information.length > 0
        ? res.data.data.carrer_information.map(item => ({
          companyOrganization: item.companyOrganization || '',
          jobTitle: item.jobTitle || '',
          industry: item.industry || '',
          countryRegion: item.countryRegion || '',
          deleted: false
        }))
        : []
      if (isAdminEdit.value) {
        await fetchTagList()
        if (form.value.tagIds) {
          selectedTagIds.value = form.value.tagIds.split(',').map(id => String(id).trim()).filter(Boolean)
        } else {
          selectedTagIds.value = []
        }
      }
    }
  } finally {
    loading.value = false
  }
  // 判断是否为触屏设备，触屏用click，PC用hover
  if (window.matchMedia('(hover: hover) and (pointer: fine)').matches) {
    popoverTrigger.value = 'hover'
    isPC.value = true
  } else {
    popoverTrigger.value = 'click'
    isPC.value = false
  }
})

async function onSubmit() {
  try {
    let tag_ids = undefined
    if (isAdminEdit.value) {
      tag_ids = selectedTagIds.value.length > 0 ? selectedTagIds.value.join(',') : null
    }
    const alumni = {
      alumni_id: form.value.alumniId,
      salutation: form.value.salutation,
      first_name: form.value.firstName,
      last_name: form.value.lastName,
      chinese_name: form.value.chineseName,
      email: form.value.email,
      contact_number: form.value.contactNumber,
      wechat_id: form.value.wechatId,
      correspondence_address: form.value.correspondenceAddress,
      current_location: form.value.currentLocation,
      zoho_alumni_number: form.value.zohoAlumniNumber,
      affiliation: form.value.affiliation,
      ycyw_schools_attended: form.value.ycywSchoolsAttended,
      study_period_start: form.value.studyPeriodStart,
      study_period_end: form.value.studyPeriodEnd,
      last_school_day: form.value.lastSchoolDay,
      year_left: form.value.yearLeft,
      marital_status: form.value.maritalStatus,
      show_year_left: form.value.showYearLeft,
      show_tertiary_university: form.value.showTertiaryUniversity,
      show_tertiary_major: form.value.showTertiaryMajor,
      show_career_company: form.value.showCareerCompany,
      show_job_title: form.value.showJobTitle,
      show_industry: form.value.showIndustry,
      show_country: form.value.showCountry,
      region: form.value.region,
      birthday: form.value.birthday,
      role: form.value.role
    }
    if (isAdminEdit.value) alumni.tag_ids = tag_ids
    // 收集被标记删除的教育/工作经历id
    const delete_tertiary_ids = tertiaryList.value.filter(e => e.deleted && e.id).map(e => e.id)
    const delete_career_ids = careerList.value.filter(w => w.deleted && w.id).map(w => w.id)
    const payload = {
      alumni,
      tertiary_information: tertiaryList.value.filter(e => !e.deleted).map(e => ({
        university_college: e.universityCollege,
        degree: e.degree,
        major: e.major,
        graduation_year: e.graduationYear,
        country_region: e.countryRegion,
        id: e.id
      })),
      carrer_information: careerList.value.filter(w => !w.deleted).map(w => ({
        company_organization: w.companyOrganization,
        job_title: w.jobTitle,
        industry: w.industry,
        country_region: w.countryRegion,
        id: w.id
      })),
      delete_tertiary_ids,
      delete_career_ids
    }
    const res = await axios.put('/api/register/alumni-detail', payload, {
      headers: { Authorization: sessionStorage.getItem('alumni_token') }
    })
    if (res.data.code === 0) {
      ElMessage.success('保存成功')
      router.push('/alumni/manage')
    } else {
      window.alert(res.data.msg || '保存失败')
    }
  } catch {
    window.alert('请求失败')
  }
}
function goBack() {
  router.back()
}
function goManage() { router.push('/alumni/manage') }
function markDeleteEducation(idx) {
  tertiaryList.value[idx].deleted = true
}
function markDeleteWork(idx) {
  careerList.value[idx].deleted = true
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

.edit-form {
  padding: 20px;
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

.section-tip {
  color: #909399;
  font-size: 14px;
  margin-bottom: 16px;
  padding: 8px 12px;
  background-color: #f4f4f5;
  border-radius: 4px;
}

.full-width {
  width: 100%;
}

.name-inputs {
  display: flex;
  gap: 8px;
}

.drag-handle {
  cursor: move;
  color: #909399;
  display: flex;
  align-items: center;
  height: 100%;
}

.edu-block,
.work-block {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.add-btn {
  margin-top: 16px;
  width: 100%;
}

.tag-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.custom-tag {
  border: none;
  font-weight: 500;
  padding: 0 16px;
  height: 32px;
  line-height: 32px;
  border-radius: 16px;
}

.plus-btn {
  background: #fff;
  border: 1px solid #dcdfe6;
  color: #409eff;
  font-size: 16px;
  width: 32px;
  height: 32px;
  padding: 0;
}

.color-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-checkbox-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
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
  background: #fff;
  color: #222;
  transition: background 0.2s;
  box-sizing: border-box;
  padding: 0;
  margin-top: 2px;
}
.info-value-filled {
  background: #f8fafc;
  border-color: #e5e7eb;
}
.info-value-filled :deep(.el-input__wrapper),
.info-value-filled :deep(.el-select .el-input__wrapper),
.info-value-filled :deep(.el-date-editor.el-input__wrapper) {
  background: #f8fafc !important;
}
.info-value-empty {
  background: #fff;
  color: #bbb;
  border-style: solid;
}
.info-value > * {
  width: 100%;
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
  .edit-form {
    padding: 0;
  }
  .form-section {
    padding: 10px 4px;
    margin-bottom: 12px;
    border-radius: 0;
    box-shadow: none;
  }
  .section-title {
    font-size: 1rem;
    margin-bottom: 12px;
    padding-left: 8px;
    border-left-width: 3px;
  }
  .section-tip {
    font-size: 13px;
    padding: 6px 8px;
    margin-bottom: 10px;
  }
  .info-grid {
    grid-template-columns: 1fr;
    gap: 10px 0;
    margin-bottom: 4px;
  }
  .info-item {
    gap: 2px;
  }
  .form-actions {
    flex-direction: column;
    gap: 10px;
    margin-top: 18px;
    padding-top: 12px;
  }
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