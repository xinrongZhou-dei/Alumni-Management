<template>
  <div>
    <el-button type="text" @click="goHome" style="position: absolute; left: 24px; top: 24px; font-size: 18px;"><el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回</el-button>
    <div class="centered-submit">
      <form @submit.prevent="onSubmit" class="submit-card">
        <h1 class="submit-title">填写校友信息</h1>
        <div class="submit-desc">欢迎您来到校友平台，请按照提示进行信息注册。<br>请完善您的校友信息。</div>
        <h2 class="submit-subtitle">基本信息</h2>
        <div class="form-row">
          <div class="form-col">
            <label>Alumni ID *</label>
            <input v-model="form.alumniId" @blur="validateAlumniIdFormat" :class="{'input-error': alumniIdError}" />
            <button type="button" class="generate-btn" @click="handleGenerateAlumniId">自动生成学号</button>
            <div v-if="alumniIdError" class="error-message">{{ alumniIdError }}</div>
            <div v-if="alumniIdSuccess" class="success-message">{{ alumniIdSuccess }}</div>
          </div>
          <div class="form-col"><label>称谓 *</label>
            <select v-model="form.title" required>
              <option value="">请选择</option>
              <option v-for="t in titles" :key="t" :value="t">{{ t }}</option>
            </select>
          </div>
          <div class="form-col"><label>英文姓名 *</label>
            <div style="display: flex; gap: 8px;">
              <input v-model="form.firstName" placeholder="First Name" required />
              <input v-model="form.lastName" placeholder="Last Name" required />
            </div>
          </div>
        </div>
        <div class="form-row">
          <div class="form-col"><label>中文姓名 *</label><input v-model="form.chineseName" required /></div>
          <div class="form-col"><label>电子邮箱 *</label><input v-model="form.email" required readonly style="background:#f1f5f9; color:#888;" /></div>
          <div class="form-col"><label>联系电话 *</label><input v-model="form.phone" required /></div>
        </div>
        <div class="form-row">
          <div class="form-col"><label>微信ID</label><input v-model="form.wechatId" /></div>
          <div class="form-col"><label>通讯地址</label><input v-model="form.address" /></div>
          <div class="form-col"><label>当前所在地</label><input v-model="form.currentLocation" /></div>
        </div>
        <div class="form-row">
          <div class="form-col"><label>Zoho校友编号</label><input v-model="form.zohoId" /></div>
          <div class="form-col"><label>身份类型 *</label>
            <select v-model="form.identity" required>
              <option value="">请选择</option>
              <option v-for="i in identities" :key="i.value" :value="i.value">{{ i.label }}</option>
            </select>
          </div>
          <div class="form-col"><label>就读学校 *</label>
            <select v-model="form.school" required>
              <option value="">请选择</option>
              <option v-for="s in schools" :key="s" :value="s">{{ s }}</option>
            </select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-col">
            <label>就读时间 *</label>
            <el-date-picker
              v-model="form.studyStart"
              type="month"
              placeholder="请选择年月"
              style="width: 100%;"
              value-format="YYYY-MM"
            />
          </div>
          <div class="form-col">
            <label>就读时间 *</label>
            <el-date-picker
              v-model="form.studyEnd"
              type="month"
              placeholder="请选择年月"
              style="width: 100%;"
              value-format="YYYY-MM"
            />
          </div>
          <div class="form-col">
            <label>最后在校日期</label>
            <el-date-picker
              v-model="form.lastDate"
              type="date"
              placeholder="请选择日期"
              style="width: 100%;"
              value-format="YYYY-MM-DD"
            />
          </div>
        </div>
        <div class="form-row">
          <div class="form-col"><label>离校年份</label><input v-model="form.leaveYear" /></div>
          <div class="form-col"><label>婚姻状况</label>
            <select v-model="form.maritalStatus">
              <option value="">请选择</option>
              <option value="单身">单身</option>
              <option value="已婚">已婚</option>
              <option value="其它">其它</option>
            </select>
          </div>
          <div class="form-col"><label>所在地区 *</label>
            <select v-model="form.region" required>
              <option value="">请选择</option>
              <option v-for="opt in regionOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
        </div>
        <div class="form-row">
          <div class="form-col">
            <label>出生日期 *</label>
            <el-date-picker
              v-model="form.birthday"
              type="date"
              placeholder="请选择日期"
              style="width: 100%;"
              value-format="YYYY-MM-DD"
              required
            />
          </div>
        </div>
        <div v-if="educations.length > 0">
          <h2 class="submit-subtitle">教育信息</h2>
          <div class="tip">注：校友名录中将展示第一条教育经历，如需展示最新教育，请将其拖动到最前面。</div>
          <draggable v-model="educations" item-key="id" :animation="200" handle=".drag-handle">
            <template #item="{element: edu, index: idx}">
              <div class="edu-block">
                <div class="form-row">
                  <span class="drag-handle" style="cursor:move;margin-right:8px;">☰</span>
                  <div class="form-col"><label>大学/学院 *</label><input v-model="edu.university" required /></div>
                  <div class="form-col"><label>学位 *</label><input v-model="edu.degree" required /></div>
                  <div class="form-col"><label>毕业年份 *</label><input v-model="edu.gradYear" required /></div>
                </div>
                <div class="form-row">
                  <div class="form-col"><label>专业 *</label><input v-model="edu.major" required /></div>
                  <div class="form-col"><label>国家/地区 *</label><input v-model="edu.eduCountry" required /></div>
                  <button type="button" class="remove-btn" @click="removeEducation(idx)">删除</button>
                </div>
              </div>
            </template>
          </draggable>
        </div>
        <button type="button" class="add-btn" @click="addEducation">添加教育经历</button>
        
        <div v-if="works.length > 0">
          <h2 class="submit-subtitle">工作信息</h2>
          <div class="tip">注：校友名录中将展示第一条工作经历，如需展示最新工作，请将其拖动到最前面。</div>
          <draggable v-model="works" item-key="id" :animation="200" handle=".drag-handle">
            <template #item="{element: work, index: idx}">
              <div class="work-block">
                <div class="form-row">
                  <span class="drag-handle" style="cursor:move;margin-right:8px;">☰</span>
                  <div class="form-col"><label>公司/组织 *</label><input v-model="work.company" required /></div>
                  <div class="form-col"><label>职位 *</label><input v-model="work.position" required /></div>
                  <div class="form-col"><label>行业 *</label>
                    <select v-model="work.industry" required>
                      <option value="">请选择</option>
                      <option v-for="industry in industryOptions" :key="industry" :value="industry">{{ industry }}</option>
                    </select>
                  </div>
                  <div class="form-col"><label>国家/地区 *</label><input v-model="work.workCountry" required /></div>
                  <button type="button" class="remove-btn" @click="removeWork(idx)">删除</button>
                </div>
              </div>
            </template>
          </draggable>
        </div>
        <button type="button" class="add-btn" @click="addWork">添加工作经历</button>
        <h2 class="submit-subtitle">信息展示设置</h2>
        <div class="form-row">
          <div class="form-col">
            <label><input type="checkbox" checked disabled /> 姓名（必展示）</label>
            <label><input type="checkbox" checked disabled /> YCYW Schools Attended（必展示）</label>
            <label><input type="checkbox" v-model="form.showYearLeft" /> 离校年份</label>
            <label><input type="checkbox" v-model="form.showTertiaryUniversity" /> 大学/学院</label>
            <label><input type="checkbox" v-model="form.showTertiaryMajor" /> 专业</label>
            <label><input type="checkbox" v-model="form.showCareerCompany" /> 公司/组织</label>
            <label><input type="checkbox" v-model="form.showJobTitle" /> 职位</label>
            <label><input type="checkbox" v-model="form.showIndustry" /> 行业</label>
            <label><input type="checkbox" v-model="form.showCountry" /> 国家/地区</label>
          </div>
        </div>
        <div style="text-align: right; margin-top: 32px;">
          <button type="button" class="cancel-btn" @click="onCancel">取消</button>
          <button type="submit" class="submit-btn">提交</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ArrowLeft } from '@element-plus/icons-vue'
import draggable from 'vuedraggable'
import { ElMessage } from 'element-plus'
import { generateAlumniId, verifyAlumniId } from '../../api/register'

const route = useRoute()
const router = useRouter()
const form = ref({
  alumniId: '',
  title: '',
  firstName: '',
  lastName: '',
  chineseName: '',
  email: '',
  phone: '',
  wechatId: '',
  address: '',
  currentLocation: '',
  zohoId: '',
  identity: '',
  school: '',
  studyStart: '',
  studyEnd: '',
  lastDate: '',
  leaveYear: '',
  maritalStatus: '',
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

const userRole = ref('alumni'); // 默认角色为alumni

const titles = ['Prof', 'Dr', 'Mr', 'Mrs', 'Ms']
const identities = [
  { label: '校友', value: 'Alumni' },
  { label: '捐赠者', value: 'Donor' },
  { label: '教职工', value: 'Staff' }
]
const schools = [
  'YCIS Hong Kong', 'YCIS Beijing', 'YCIS Chongqing', 'YCIS Qingdao', 'YCIS Shanghai',
  'YWIES Beijing', 'YWIES Guangzhou', 'YWIES Shanghai Gubei', 'YWIES Shanghai Lingang',
  'YWIES Tongxiang', 'YWIES Yantai'
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

// 教育经历
const educations = ref([])
function addEducation() {
  educations.value.push({ id: Date.now() + Math.random(), university: '', degree: '', major: '', gradYear: '', eduCountry: '' })
}
function removeEducation(idx) {
  educations.value.splice(idx, 1);
}

// 工作经历
const works = ref([])
function addWork() {
  works.value.push({ id: Date.now() + Math.random(), company: '', position: '', industry: '', workCountry: '' })
}
function removeWork(idx) {
  works.value.splice(idx, 1);
}

// 学号验证相关
const alumniIdError = ref('')
const alumniIdSuccess = ref('')
const alumniIdChecked = ref(false)
const alumniIdValid = ref(false)

const checkAlumniIdUnique = async () => {
  const id = form.value.alumniId
  alumniIdChecked.value = false
  alumniIdValid.value = false
  if (!/^\d{9}$/.test(id)) return
  try {
    const res = await verifyAlumniId(id)
    if ( res.code === 0) {
      alumniIdError.value = ''
      alumniIdSuccess.value = '学号有效且未被占用'
      alumniIdValid.value = true
    } else {
      alumniIdError.value = res.msg || '学号已被占用'
      alumniIdSuccess.value = ''
      alumniIdValid.value = false
    }
  } catch {
    alumniIdError.value = '网络错误，无法校验学号'
    alumniIdSuccess.value = ''
    alumniIdValid.value = false
  }
  alumniIdChecked.value = true
}

watch(() => form.value.alumniId, (val) => {
  alumniIdError.value = ''
  alumniIdSuccess.value = ''
  alumniIdChecked.value = false
  alumniIdValid.value = false
  if (val && val.length === 9) {
    checkAlumniIdUnique()
  }
})

const validateAlumniIdFormat = () => {
  const id = form.value.alumniId
  if (!id || !/^\d{9}$/.test(id)) {
    alumniIdError.value = '学号格式不正确，必须为9位数字'
    alumniIdSuccess.value = ''
    return false
  }
  if (!alumniIdValid.value) {
    alumniIdError.value = alumniIdError.value || '学号未查重或已被占用'
    alumniIdSuccess.value = ''
    return false
  }
  return true
}

function ensureTokenCookie() {
  const token = sessionStorage.getItem('register_token');
  if (token) {
    document.cookie = `alumni-token=${token}; path=/`;
  }
}

onMounted(async () => {
  ensureTokenCookie();
  if (route.query.email) {
    form.value.email = route.query.email
  }

  // 尝试获取当前用户的完整信息
  try {
    const infoRes = await axios.get('/api/register/info');
    if (infoRes.data.code === 0 && infoRes.data.data) {
        Object.assign(form.value, infoRes.data.data);
        if (infoRes.data.data.role) {
            userRole.value = infoRes.data.data.role; // 保存用户角色
        }
    }
  } catch (error) {
  }

  // 移除自动获取学号的逻辑，学号由用户手动输入
})

function formatMonth(val) {
  // yyyy-MM => yyyy/M
  if (!val) return '';
  const [y, m] = val.split('-');
  return y + '/' + (m.startsWith('0') ? m.slice(1) : m);
}
function formatDate(val) {
  // yyyy-MM-dd => yyyy/M/d
  if (!val) return '';
  const [y, m, d] = val.split('-');
  return y + '/' + (m.startsWith('0') ? m.slice(1) : m) + '/' + (d.startsWith('0') ? d.slice(1) : d);
}

async function onSubmit() {
  if (!validateAlumniIdFormat()) {
    return
  }
  for (const work of works.value) {
    if (!work.industry) {
      ElMessage.error('请为每条工作经历选择正确的行业（不可留空、不可自填）');
      return;
    }
  }
  // 密码校验
  const password = sessionStorage.getItem('register_password');
  if (!password) {
    window.alert('密码信息丢失，请返回上一步重新设置密码！');
    router.push('/alumni/register/set-password?email=' + encodeURIComponent(form.value.email));
    return;
  }
  try {
    const payload = {
      ...form.value,
      studyStart: formatMonth(form.value.studyStart),
      studyEnd: formatMonth(form.value.studyEnd),
      lastDate: formatDate(form.value.lastDate),
      birthday: formatDate(form.value.birthday),
      role: userRole.value === 'admin' ? 'admin' : 'alumni',
      tertiary_information: educations.value.map(e => ({
        university_college: e.university,
        degree: e.degree,
        major: e.major,
        graduation_year: e.gradYear,
        country_region: e.eduCountry
      })),
      carrer_information: works.value
        .filter(w => industryOptions.includes(w.industry))
        .map(w => ({
          company_organization: w.company,
          job_title: w.position,
          industry: w.industry,
          country_region: w.workCountry
        }))
    };
    const token = sessionStorage.getItem('register_token');
    const res = await axios.put('/api/register/alumni-detail', payload, {
      headers: { Authorization: 'Bearer ' + token }
    });
    if (res.data.code === 0) {
      ElMessage.success('注册成功');
      // 清除所有注册相关的token和密码
      sessionStorage.removeItem('register_token');
      sessionStorage.removeItem('register_password');
      sessionStorage.removeItem('alumni_token');
      sessionStorage.removeItem('alumni_id');
      sessionStorage.removeItem('user_info');
      // 清除cookie中的token
      document.cookie = 'alumni-token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
      router.push('/login');
    } else {
      ElMessage.error('注册失败：' + res.data.msg);
    }
  } catch (e) {
    ElMessage.error('注册失败：' + e.message);
  }
}
function onCancel() {
  window.history.back()
}

function goHome() { router.push('/') }

const handleGenerateAlumniId = async () => {
  try {
    const res = await generateAlumniId()
    if (res.code === 0 && res.data) {
      form.value.alumniId = res.data
      alumniIdError.value = ''
      alumniIdSuccess.value = '学号已自动生成'
    } else {
      alumniIdError.value = res.data.msg || '生成学号失败'
    }
  } catch {
    alumniIdError.value = '网络错误，生成学号失败'
  }
}
</script>

<style scoped>
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
  margin-bottom: 24px;
}
.submit-subtitle {
  font-size: 1.1rem;
  font-weight: bold;
  margin: 24px 0 12px;
  text-align: left;
  width: 100%;
}
.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  width: 100%;
}
.form-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
input, select {
  padding: 8px;
  border-radius: 6px;
  border: 1px solid #d1d5db;
  font-size: 1rem;
}
.input-error {
  border-color: #e11d48 !important;
}
.error-message {
  color: #e11d48;
  font-size: 0.9rem;
  margin-top: 4px;
}
.success-message {
  color: #059669;
  font-size: 0.9rem;
  margin-top: 4px;
}
.add-btn {
  background: #f1f5f9;
  color: #222;
  border: none;
  border-radius: 6px;
  padding: 8px 18px;
  margin-bottom: 12px;
  cursor: pointer;
}
.remove-btn {
  background: #e11d48;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 8px 12px;
  margin-left: 8px;
  cursor: pointer;
}
.submit-btn {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 10px 32px;
  font-size: 1.1rem;
  margin-left: 16px;
  cursor: pointer;
}
.cancel-btn {
  background: #f1f5f9;
  color: #222;
  border: none;
  border-radius: 6px;
  padding: 10px 32px;
  font-size: 1.1rem;
  cursor: pointer;
}
.edu-block, .work-block {
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 2px 8px #eee;
  padding: 16px;
  margin-bottom: 16px;
}
.drag-handle {
  cursor: move;
  margin-right: 8px;
}
.tip {
  color: #64748b;
  font-size: 0.9rem;
  text-align: center;
  margin-bottom: 12px;
}
.generate-btn {
  margin-left: 8px;
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 4px;
  padding: 4px 12px;
  font-size: 0.95rem;
  cursor: pointer;
}
@media (max-width: 700px) {
  .centered-submit {
    min-height: 60vh;
  }
  .submit-card {
    width: 100%;
    min-width: 0;
    padding: 10px 2px;
    border-radius: 0;
    box-shadow: none;
    align-items: stretch;
  }
  .submit-title {
    font-size: 1.2rem;
    margin-bottom: 8px;
  }
  .submit-desc {
    font-size: 0.95rem;
    margin-bottom: 12px;
  }
  .submit-subtitle {
    font-size: 1rem;
    margin: 16px 0 8px;
  }
  .form-row {
    flex-direction: column;
    gap: 6px;
    margin-bottom: 8px;
  }
  .form-col {
    width: 100%;
    max-width: 100%;
    gap: 2px;
  }
  label {
    font-size: 0.98rem;
    color: #222;
    margin-bottom: 2px;
    text-align: left;
    font-weight: 500;
  }
  input, select {
    font-size: 0.98rem;
    padding: 8px;
  }
  .add-btn, .remove-btn, .submit-btn, .cancel-btn {
    width: 100%;
    margin: 8px 0 0 0;
    font-size: 1rem;
    padding: 10px 0;
    box-sizing: border-box;
  }
  .edu-block, .work-block {
    padding: 8px;
    margin-bottom: 8px;
  }
  .tip {
    font-size: 0.88rem;
    margin-bottom: 6px;
  }
  .generate-btn {
    width: 100%;
    margin: 6px 0 0 0;
    font-size: 0.95rem;
    padding: 6px 0;
  }
  .error-message, .success-message {
    font-size: 0.88rem;
    margin-top: 2px;
  }
  .drag-handle {
    margin-right: 4px;
  }
  .submit-card > div[style*='text-align: right'] {
    text-align: center !important;
    margin-top: 12px !important;
  }
}
</style> 