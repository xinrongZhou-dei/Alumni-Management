<template>
  <div>
    <el-button type="text" @click="goManage" style="position: absolute; left: 24px; top: 24px; font-size: 18px;"><el-icon style="vertical-align: middle; margin-right: 4px;"><ArrowLeft /></el-icon>返回</el-button>
    <div class="centered-submit">
      <form @submit.prevent="onSubmit" class="submit-card">
        <h1 class="submit-title">管理员添加校友</h1>
        <div class="submit-desc">请完善校友信息，学号自动生成，邮箱和密码由管理员填写。</div>
        <h2 class="submit-subtitle">基本信息</h2>
        <div class="form-row">
          <div class="form-col">
            <label>Alumni ID *</label>
            <input v-model="form.alumniId" readonly style="background:#f1f5f9; color:#888;" />
            <button type="button" class="generate-btn" @click="generateId">生成学号</button>
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
          <div class="form-col"><label>电子邮箱 *</label><input v-model="form.email" required /></div>
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
          <div class="form-col"><label>就读时间 *</label><input v-model="form.studyStart" placeholder="yyyy/m" required /></div>
          <div class="form-col"><label>就读时间 *</label><input v-model="form.studyEnd" placeholder="yyyy/m" required /></div>
          <div class="form-col"><label>最后在校日期</label><input v-model="form.lastDate" placeholder="yyyy/m/日" /></div>
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
          <div class="form-col"><label>出生日期 *</label><input v-model="form.birthday" type="date" required /></div>
        </div>
        <!-- 密码设置 -->
        <h2 class="submit-subtitle">设置初始密码</h2>
        <div class="form-row">
          <div class="form-col"><label>密码 *</label><input v-model="form.password" type="password" required /></div>
          <div class="form-col"><label>确认密码 *</label><input v-model="form.confirmPassword" type="password" required /></div>
        </div>
        <!-- 教育经历 -->
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
        <!-- 工作经历 -->
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
          <button type="button" class="cancel-btn" @click="goManage">取消</button>
          <button type="submit" class="submit-btn">提交</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import draggable from 'vuedraggable'
import { ElMessage } from 'element-plus'
import { generateAlumniId, adminAddAlumni } from '../../api/register'
import axios from 'axios'

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
  birthday: '',
  password: '',
  confirmPassword: ''
})

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
const educations = ref([])
const works = ref([])
function addEducation() {
  educations.value.push({ id: Date.now() + Math.random(), university: '', degree: '', major: '', gradYear: '', eduCountry: '' })
}
function removeEducation(idx) {
  educations.value.splice(idx, 1);
}
function addWork() {
  works.value.push({ id: Date.now() + Math.random(), company: '', position: '', industry: '', workCountry: '' })
}
function removeWork(idx) {
  works.value.splice(idx, 1);
}
const alumniIdError = ref('')
const alumniIdSuccess = ref('')
const isAlumniIdValid = ref(false)
let validateTimeout = null

function validateAlumniId() {
  if (validateTimeout) clearTimeout(validateTimeout)
  const alumniId = form.value.alumniId
  if (!/^\d{9}$/.test(alumniId)) {
    isAlumniIdValid.value = false
    return
  }
  validateTimeout = setTimeout(async () => {
    try {
      const res = await axios.get('/api/register/verify-alumni-id', { params: { alumniId } })
      if (res.data.code === 0) {
        alumniIdSuccess.value = res.data.msg || '学号有效'
        alumniIdError.value = ''
        isAlumniIdValid.value = true
      } else {
        alumniIdError.value = res.data.msg || '学号验证失败'
        alumniIdSuccess.value = ''
        isAlumniIdValid.value = false
      }
    } catch (e) {
      alumniIdError.value = '网络错误，请稍后重试'
      alumniIdSuccess.value = ''
      isAlumniIdValid.value = false
    }
  }, 500)
}

async function generateId() {
  try {
    const res = await generateAlumniId()
    if (res.code === 0) {
      form.value.alumniId = res.data
      alumniIdError.value = ''
      alumniIdSuccess.value = '学号生成成功'
      isAlumniIdValid.value = true
      ElMessage.success('学号生成成功')
    } else {
      alumniIdError.value = res.data.msg || '学号生成失败'
      alumniIdSuccess.value = ''
      isAlumniIdValid.value = false
      ElMessage.error(alumniIdError.value)
    }
  } catch (error) {
    alumniIdError.value = '网络错误，请重试'
    alumniIdSuccess.value = ''
    isAlumniIdValid.value = false
    ElMessage.error(alumniIdError.value)
  }
}
function goManage() { router.push('/alumni/manage') }
function validateAlumniIdFormat() {
  const alumniId = form.value.alumniId
  if (!alumniId) {
    alumniIdError.value = '学号不能为空'
    alumniIdSuccess.value = ''
    isAlumniIdValid.value = false
    return
  }
  if (!/^\d{9}$/.test(alumniId)) {
    alumniIdError.value = '学号格式不正确，必须是9位数字'
    alumniIdSuccess.value = ''
    isAlumniIdValid.value = false
    return
  }
  validateAlumniId()
}
async function onSubmit() {
  validateAlumniIdFormat()
  if (!isAlumniIdValid.value) {
    ElMessage.error(alumniIdError.value || '学号无效，请输入有效的9位数字学号')
    return
  }
  if (!form.value.email || !form.value.password || !form.value.confirmPassword) {
    ElMessage.error('请填写邮箱和密码');
    return;
  }
  if (form.value.password !== form.value.confirmPassword) {
    ElMessage.error('两次输入的密码不一致');
    return;
  }
  // 其余字段校验可参考注册页面
  try {
    const payload = {
      ...form.value,
      showYearLeft: String(form.value.showYearLeft),
      showTertiaryUniversity: String(form.value.showTertiaryUniversity),
      showTertiaryMajor: String(form.value.showTertiaryMajor),
      showCareerCompany: String(form.value.showCareerCompany),
      showJobTitle: String(form.value.showJobTitle),
      showIndustry: String(form.value.showIndustry),
      showCountry: String(form.value.showCountry),
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
    }
    const res = await adminAddAlumni(payload)
    if (res.code === 0) {
      ElMessage.success('添加成功！')
      router.push('/alumni/manage')
    } else {
      ElMessage.error(res.msg || '添加失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，请重试')
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
@media (max-width: 900px) {
  .submit-card {
    width: 100% !important;
    min-width: 0;
    padding: 16px 4vw;
    border-radius: 8px;
  }
  .form-row {
    flex-direction: column;
    gap: 8px;
    margin-bottom: 12px;
  }
  .form-col {
    width: 100%;
    max-width: 100%;
  }
  input, select {
    font-size: 0.98rem;
    padding: 8px 6px;
  }
  .submit-title {
    font-size: 1.3rem;
  }
  .submit-desc {
    font-size: 1rem;
    margin-bottom: 16px;
  }
  .submit-subtitle {
    font-size: 1rem;
    margin: 18px 0 8px;
  }
  .add-btn, .remove-btn, .submit-btn, .cancel-btn {
    font-size: 1rem;
    padding: 8px 0;
    width: 100%;
    margin-left: 0;
  }
  .centered-submit {
    padding: 0;
    min-height: 0;
  }
}
@media (max-width: 600px) {
  .submit-card {
    padding: 8px 2vw;
    border-radius: 4px;
  }
  .submit-title {
    font-size: 1.1rem;
  }
}
</style> 