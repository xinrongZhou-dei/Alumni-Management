<template>
  <div class="alumni-main-root">
    <!-- 顶部栏 -->
    <header class="alumni-header">
      <div class="alumni-header-inner">
        <div class="alumni-header-title">校友平台</div>
        <div class="alumni-header-actions">
          <template v-if="isLogin">
            <span class="alumni-header-name">{{ alumniName }}</span>
            <el-button v-if="!isAdmin" type="primary" @click="goUpdateInfo">更新信息</el-button>
            <el-button type="danger" @click="logout">退出登录</el-button>
          </template>
          <template v-else>
            <el-button type="text" @click="goLogin">登录</el-button>
            <el-button type="text" @click="goRegister">注册</el-button>
            <el-button type="text" @click="goActivate">账号激活</el-button>
            <el-button type="default" disabled>信息更新</el-button>
          </template>
        </div>
      </div>
    </header>
    <main class="alumni-main-center">
      <template v-if="isAdmin">
        <div class="admin-dashboard">
          <el-row :gutter="32">
            <el-col :span="8" v-for="card in adminCards" :key="card.key">
              <div
                class="admin-card3 card-with-badge"
                :class="{ 'admin-card3-disabled': card.disabled }"
                @click="!card.disabled && card.action && card.action()"
              >
                <!-- 右上角徽标，仅校友信息审核卡片显示 -->
                <span v-if="card.key === 'alumniReview' && pendingAlumniCount !== null && pendingAlumniCount > 0" class="badge-corner">{{ pendingAlumniCount }}</span>
                <span v-if="card.key === 'chapterReview' && pendingChapterCount !== null && pendingChapterCount > 0" class="badge-corner">{{ pendingChapterCount }}</span>
                <span v-if="card.key === 'visit' && pendingVisitRecordCount !== null && pendingVisitRecordCount > 0" class="badge-corner">{{ pendingVisitRecordCount }}</span>
                <div class="admin-card3-icon" :style="{ background: card.iconBg }">
                  <component :is="card.icon" :style="{ color: card.iconColor, fontSize: '2rem' }" />
                </div>
                <div class="admin-card3-title">{{ card.title }}</div>
                <div class="admin-card3-desc">{{ card.desc }}</div>
                <el-button
                  class="admin-card3-btn"
                  type="primary"
                  :disabled="card.disabled"
                  @click.stop="!card.disabled && card.action && card.action()"
                >访问页面</el-button>
              </div>
            </el-col>
          </el-row>
        </div>
      </template>
      <template v-else>
        <!-- 欢迎区 -->
        <section class="alumni-welcome">
          <div class="alumni-welcome-content">
            <div class="alumni-welcome-title">欢迎加入校友会</div>
            <div class="alumni-welcome-sub">连接校友，共创未来</div>
            <div class="action-buttons">
              <el-button 
                type="primary" 
                size="large" 
                @click="handleAppointment"
                class="welcome-button"
                :icon="Calendar"
              >
                <span class="button-text">预约参观</span>
              </el-button>
              <el-button 
                type="success" 
                size="large" 
                @click="handleViewRecords"
                class="welcome-button"
                :icon="Tickets"
              >
                <span class="button-text">查看预约记录</span>
              </el-button>
            </div>
          </div>
        </section>
        <!-- 最新活动区 -->
        <section class="alumni-section alumni-activity-section">
          <div class="alumni-activity-title">最新活动</div>
          <div class="activity-section-outer">
            <div class="activity-list">
              <div class="activity-card" v-for="item in activities" :key="item.uuid" @click="goActivityDetail(item.uuid)">
                <div class="activity-card-img">
                  <img :src="item.coverUrl" alt="活动封面" />
                </div>
                <div class="activity-card-title">{{ item.name }}</div>
                <div class="activity-card-desc">{{ getShortDesc(item.detail) }}</div>
                <div class="activity-card-date">{{ formatDate(item.activityDate) }}</div>
                <div class="activity-card-status">
                  <span class="status-label">报名状态：</span>
                  <el-tag :type="getStatusTagType(item.signupStatus)" effect="plain" size="small">{{ item.signupStatus }}</el-tag>
                </div>
              </div>
            </div>
          </div>
        </section>
        <!-- 分会区 -->
        <section class="alumni-section alumni-activity-section">
          <div class="alumni-activity-title">分会</div>
          <div class="activity-section-outer">
            <div class="activity-list">
              <div class="activity-card" v-for="item in hotChapters" :key="item.tag_id" @click="handleChapterClick(item)">
                <div class="activity-card-img">
                  <img :src="getChapterCoverUrl(item.branch_name)" alt="分会封面" />
                </div>
                <div class="activity-card-title">{{ item.branch_name }}</div>
                <div class="activity-card-desc">{{ getShortDesc('欢迎加入' + item.branch_name + '分会，与校友们一起交流分享！') }}</div>
                <div class="activity-card-date">{{ formatDate(item.create_time) }}</div>
                <div class="activity-card-status">
                  <span class="status-label">成员数：</span>
                  <el-tag type="info" effect="plain" size="small">{{ item.total || 0 }}</el-tag>
                </div>
                <div class="activity-card-action">
                  <el-button 
                    v-if="isInChapter(item.tag_id)" 
                    type="info" 
                    size="small" 
                    disabled
                  >
                    已在分会
                  </el-button>
                  <el-button 
                    v-else-if="getApplicationStatus(item.tag_id) === 'PENDING'"
                    type="warning" 
                    size="small" 
                    disabled
                  >
                    审核中
                  </el-button>
                  <el-button 
                    v-else-if="getApplicationStatus(item.tag_id) === 'REJECTED'"
                    type="danger" 
                    size="small" 
                    @click.stop="handleApplyChapter(item)"
                  >
                    申请被拒绝
                  </el-button>
                  <el-button 
                    v-else 
                    type="primary" 
                    size="small" 
                    @click.stop="handleApplyChapter(item)"
                  >
                    申请加入
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </section>
        <!-- 快速入口区 -->
        <section class="alumni-section alumni-quick-entry">
          <div class="alumni-quick-title">快速入口</div>
          <div class="alumni-quick-list">
            <div
              class="admin-card3"
              v-for="card in quickCards"
              :key="card.key"
              :class="{ 'admin-card3-disabled': card.disabled }"
              @click="!card.disabled && card.action && card.action()"
            >
              <div class="admin-card3-icon" :style="{ background: card.iconBg }">
                <component :is="card.icon" :style="{ color: card.iconColor, fontSize: '2rem' }" />
              </div>
              <div class="admin-card3-title">{{ card.title }}</div>
              <div class="admin-card3-desc">{{ card.desc }}</div>
              <el-button
                class="admin-card3-btn"
                type="primary"
                :disabled="card.disabled"
                @click.stop="!card.disabled && card.action && card.action()"
              >{{ card.btnText }}</el-button>
              <span v-if="card.key === 'activity-signup' && ongoingSignupActivityCount !== null && ongoingSignupActivityCount > 0" class="badge-corner">{{ ongoingSignupActivityCount }}</span>
              <span v-if="card.key === 'branch' && myPendingChapterApplicationCount !== null && myPendingChapterApplicationCount > 0" class="badge-corner">{{ myPendingChapterApplicationCount }}</span>
            </div>
          </div>
        </section>
      </template>
    </main>
    <!-- 底栏 -->
    <footer class="alumni-footer">
      <div class="alumni-footer-inner">
        <span>关于我们</span>
        <span>联系方式</span>
        <span>关注我们</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled, CircleCheckFilled, Calendar, Tickets, Message, Notebook, Key, HomeFilled, Edit, Refresh, Lock, Search, TrendCharts } from '@element-plus/icons-vue'
import service from '../utils/request' // 导入配置好的axios实例

const router = useRouter()
const role = ref('alumni')
const isLogin = ref(false)
const alumniName = ref('')
const isAdmin = computed(() => {
  const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')
  return userInfo.role === 'admin'
})
const activities = ref([])
const branches = ref([])
const hotChapters = ref([])
const joinedChapterIds = ref([])
const applicationStatus = ref({})

const adminCards = ref([])
const accessibleSchools = ref([])
const pendingAlumniCount = ref(null)
const pendingChapterCount = ref(null)
const pendingVisitRecordCount = ref(null)
const ongoingSignupActivityCount = ref(null)
const myPendingChapterApplicationCount = ref(null)

// 定义所有可能的卡片模板
const cardTemplates = {
  alumniInfo: { key: 'alumniInfo', icon: UserFilled, iconBg: '#e3f0fe', iconColor: '#409eff', title: '校友信息管理', desc: '管理校友信息', action: () => router.push('/alumni/manage') },
  alumniReview: { key: 'alumniReview', icon: CircleCheckFilled, iconBg: '#f3e8ff', iconColor: '#a855f7', title: '校友信息审核', desc: '审核新注册的校友信息', action: () => router.push('/alumni/review'), disabled: true },
  activity: { key: 'activity', icon: Calendar, iconBg: '#d1fae5', iconColor: '#10b981', title: '活动管理', desc: '管理和查看活动', action: () => router.push('/admin/activity-manage') },
  tag: { key: 'tag', icon: Key, iconBg: '#fef3c7', iconColor: '#f59e42', title: '标签管理', desc: '管理分会/兴趣标签', action: () => router.push('/admin/tag-manage') },
  chapterReview: { key: 'chapterReview', icon: Notebook, iconBg: '#e0eaff', iconColor: '#409eff', title: '分会审核', desc: '分会创建审核与管理', action: () => router.push('/admin/chapter-review') },
  mail: { key: 'mail', icon: Message, iconBg: '#fee2e2', iconColor: '#ef4444', title: '邮件模板', desc: '设置发送邮件的模板', action: () => router.push('/admin/email-template-manage') },
  analysis: { key: 'analysis', icon: TrendCharts, iconBg: '#ede9fe', iconColor: '#6366f1', title: '数据分析', desc: '数据统计与可视化', action: () => router.push('/admin/data-analysis') },
  permission: { key: 'permission', icon: Key, iconBg: '#ffe4e6', iconColor: '#f472b6', title: '权限管理', desc: '管理管理员权限', action: () => router.push('/admin/permission-manage'), disabled: false },
  visit: { key: 'visit', icon: HomeFilled, iconBg: '#fef6e4', iconColor: '#f59e42', title: '校园参观预约记录', desc: '查看校友预约记录', action: () => router.push('/admin/visit-records') },
};

const buildAdminCards = (permissions) => {
  const cards = [];
  if (!permissions) return [];

  // 校友信息管理 - 无论权限级别如何，都允许访问
  if (permissions.alumni_info_management_permission > 0) {
    cards.push({ ...cardTemplates.alumniInfo, disabled: false }); // 不禁用卡片，允许访问
  }
  // 校友信息审核 
  if (permissions.alumni_info_review_permission > 0) {
    cards.push({ ...cardTemplates.alumniReview, disabled: false });
  }
  // 活动管理
  if (permissions.activity_management_permission > 0) {
    // 权限为1或2都允许点击跳转
    cards.push({ ...cardTemplates.activity, disabled: false });
  } else {
    // 权限为0时禁用
    cards.push({ ...cardTemplates.activity, disabled: true });
  }
  // 标签管理
  if (permissions.tag_management_permission > 0) {
    cards.push({ ...cardTemplates.tag, disabled: false });
  }
  // 分会审核
  if (permissions.chapter_review_permission > 0) {
    cards.push({ ...cardTemplates.chapterReview, disabled: false });
  }
  // 邮件模板
  if (permissions.email_template_permission > 0) {
    cards.push({ ...cardTemplates.mail, disabled: false });
  }
  // 数据分析
  if (permissions.data_analysis_permission > 0) {
    cards.push({ ...cardTemplates.analysis, disabled: false });
  }
   // 权限管理
  if (permissions.permission_management_permission > 0) {
    cards.push({ ...cardTemplates.permission, disabled: false });
  }
  // 校园参观记录
  if (permissions.campus_visit_record_permission > 0) {
    cards.push({ ...cardTemplates.visit, disabled: false });
  }

  return cards;
};

// 登录成功后获取权限和学校范围
const fetchAdminPermissions = async () => {
  try {
    const res = await service.get('/admin/permissions')
    if (res.code === 0) {
      const permissions = res.data
      
      // 处理可访问学校
      if (permissions.accessible_schools) {
        accessibleSchools.value = permissions.accessible_schools.split(',').map(s => s.trim()).filter(s => s)
      } else {
        accessibleSchools.value = []
      }
      
      // 保存权限信息到localStorage
      localStorage.setItem('admin_permissions', JSON.stringify(permissions));
      
      // 构建管理卡片
      adminCards.value = buildAdminCards(permissions);
    }
  } catch (error) {
    console.error('获取管理员权限失败:', error)
  }
}

// 获取默认学校列表
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

const goLogin = () => {
  router.push('/login')
}
const goRegister = () => {
  router.push('/alumni/register')
}
const goUpdateInfo = () => {
  router.push('/alumni/edit')
}
const goActivate = () => {
  router.push('/alumni/activate')
}

const quickCards = [
  {
    key: 'alumni-search',
    icon: Search,
    iconBg: '#e3f0fe',
    iconColor: '#409eff',
    title: '查找校友',
    desc: '浏览和查找校友信息',
    btnText: '去查找',
    action: () => router.push('/alumni/search'),
    disabled: false,
  },
  {
    key: 'activity-signup',
    icon: Calendar,
    iconBg: '#d1fae5',
    iconColor: '#10b981',
    title: '活动报名',
    desc: '报名参加校友活动',
    btnText: '去报名',
    action: () => {
      const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')
      const isAdmin = userInfo.role === 'admin'
      if (isAdmin) {
        sessionStorage.setItem('activity_manage_from', 'admin');
        router.push('/admin/activity-manage');
      } else {
        router.push('/alumni/activity-signup');
      }
    },
    disabled: false,
  },
  {
    key: 'branch',
    icon: HomeFilled,
    iconBg: '#f3e8ff',
    iconColor: '#a855f7',
    title: '分会列表',
    desc: '查看所有分会信息',
    btnText: '查看分会',
    action: () => router.push('/alumni/branch-list'),
    disabled: false,
  },
  {
    key: 'alumni-update',
    icon: Refresh,
    iconBg: '#fef9c3',
    iconColor: '#eab308',
    title: '信息更新',
    desc: '更新个人校友信息',
    btnText: '去更新',
    action: goUpdateInfo,
    disabled: false,
  },
  {
    key: 'forgot-password',
    icon: Lock,
    iconBg: '#fee2e2',
    iconColor: '#ef4444',
    title: '忘记密码',
    desc: '重置登录密码',
    btnText: '去重置',
    action: () => router.push('/alumni/forgot-password'),
    disabled: false,
  },
  {
    key: 'change-password',
    icon: Edit,
    iconBg: '#ede9fe',
    iconColor: '#6366f1',
    title: '修改密码',
    desc: '修改当前登录密码',
    btnText: '去修改',
    action: () => router.push('/alumni/change-password'),
    disabled: false,
  }
]

// 登出
const logout = async () => {
  try {
    await service.post('/logout') // 调用后端登出接口
    sessionStorage.removeItem('user_info')
    sessionStorage.removeItem('alumni_token') // 以防万一，也清除旧的token
    sessionStorage.removeItem('admin_permissions') // 清除管理员权限信息
    sessionStorage.removeItem('alumni_id') // 清除校友ID
    isLogin.value = false
    alumniName.value = ''
    ElMessage.success('已成功退出登录')
    router.push('/login')
  } catch (error) {
    console.error('登出失败', error)
    ElMessage.error('登出操作失败，请稍后再试')
  }
}

// 获取活动列表
const fetchActivities = async () => {
  try {
    // 只请求4条
    const response = await service.get('/activity/list', { params: { page: 1, size: 4 } })
    if (response.code === 0 && Array.isArray(response.data)) {
      // 如果后端没排序，前端按 activityDate 排序，取前4个
      activities.value = (response.data || []).sort((a, b) => new Date(b.activityDate) - new Date(a.activityDate)).slice(0, 4)
    }
  } catch (error) {
    console.error(error)
  }
}
// 获取热门分会和分会状态
const fetchHotChapters = async () => {
  try {
    const alumniId = JSON.parse(sessionStorage.getItem('user_info') || '{}').alumniId;
    if (!alumniId) return
    const res = await service.get('/chapters/hot-chapters', {
      params: { alumniId }
    });
    if (res.code === 0) {
      hotChapters.value = res.data
      joinedChapterIds.value = res.joinedTagIds || []
      applicationStatus.value = res.applicationStatus || {}
    }
  } catch (error) {
    console.error('Failed to fetch hot chapters:', error)
  }
}
// 获取用户已加入的分会和申请状态（已合并到 fetchHotChapters，不再单独请求）
const fetchUserChaptersAndApplications = async () => {
  // 不再单独请求，留空或直接调用 fetchHotChapters
}

async function fetchPendingAlumniCount() {
  try {
    const res = await service.get('/register/pending-alumni-count')
    if (res.code === 0) {
      pendingAlumniCount.value = res.data
    } else {
      pendingAlumniCount.value = null
    }
  } catch (e) {
    pendingAlumniCount.value = null
  }
}
async function fetchPendingChapterCount() {
  try {
    const res = await service.get('/chapters/pending-chapter-count')
    if (res.code === 0) {
      pendingChapterCount.value = res.data
    } else {
      pendingChapterCount.value = null
    }
  } catch (e) {
    pendingChapterCount.value = null
  }
}
async function fetchPendingVisitRecordCount() {
  try {
    const res = await service.get('/visitrecord/pending-visit-record-count')
    if (res.code === 0) {
      pendingVisitRecordCount.value = res.data
    } else {
      pendingVisitRecordCount.value = null
    }
  } catch (e) {
    pendingVisitRecordCount.value = null
  }
}
async function fetchOngoingSignupActivityCount() {
  try {
    const res = await service.get('/activity/ongoing-signup-count')
    if (res.code === 0) {
      ongoingSignupActivityCount.value = Number(res.data)
    } else {
      ongoingSignupActivityCount.value = null
    }
  } catch (e) {
    ongoingSignupActivityCount.value = null
  }
}
async function fetchMyPendingChapterApplicationCount() {
  try {
    const res = await service.get('/chapters/my-pending-application-count')
    if (res.code === 0) {
      myPendingChapterApplicationCount.value = Number(res.data)
    } else {
      myPendingChapterApplicationCount.value = null
    }
  } catch (e) {
    myPendingChapterApplicationCount.value = null
  }
}

onMounted(() => {
  const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')
  if (userInfo && userInfo.alumniId) {
    isLogin.value = true
    alumniName.value = userInfo.chineseName
    role.value = userInfo.role
    fetchActivities()
    fetchHotChapters()
    // fetchUserChaptersAndApplications() // 已合并到 fetchHotChapters
  }
  // 登录成功提示只弹一次
  if (sessionStorage.getItem('login_success_flag')) {
    ElMessage({ message: '登录成功', type: 'success', duration: 2000, showClose: false })
    sessionStorage.removeItem('login_success_flag')
  }
  getBranches()
  if (isAdmin.value) {
    fetchAdminPermissions()
    fetchPendingAlumniCount()
    fetchPendingChapterCount()
    fetchPendingVisitRecordCount()
  }
  if (!isAdmin.value) {
    fetchOngoingSignupActivityCount()
    fetchMyPendingChapterApplicationCount()
  }
})

function formatDate(val) {
  if (!val) return ''
  const d = new Date(val)
  if (isNaN(d.getTime())) return ''
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function getShortDesc(detail) {
  if (!detail) return ''
  const maxLen = 32 // 可根据实际UI调整
  if (detail.length <= maxLen) return detail
  // 保证最后三个字符是...
  return detail.slice(0, maxLen - 3) + '...'
}

function goActivityDetail(uuid) {
  const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')
  const isAdmin = userInfo.role === 'admin'
  // 首页活动卡片点击，设为'home'
  sessionStorage.setItem('activity_detail_from', 'home')
  router.push(isAdmin ? `/admin/activity-detail/${uuid}` : `/alumni/activity-detail/${uuid}`)
}

function getStatusTagType(status) {
  if (status === '进行中') return 'success'
  if (status === '未开始') return 'info'
  if (status === '即将结束') return 'warning'
  return 'danger'
}

function initWebSocket() {
  // WebSocket 连接初始化
  const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const wsHost = window.location.host;
  const wsPath = '/ws/database-updates';
  const ws = new WebSocket(wsProtocol + '//' + wsHost + wsPath);
  ws.onmessage = (event) => {
    const msg = event.data;
    if (["alumni changed", "tertiary changed", "career changed"].includes(msg)) {
      fetchAlumniData();
    }
    if (["chapter changed", "alumni joined chapter", "alumni left chapter"].includes(msg)) {
      getHotChapters();
    }
  };
  ws.onclose = () => {
    setTimeout(initWebSocket, 3000);
  };
}

function fetchAlumniData() {
  // ... 你原有的获取校友数据的方法 ...
}

// 跳转到预约表单页面
const handleAppointment = () => {
  if (!isLogin.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/visit-record/form')
}

const handleViewRecords = () => {
  router.push('/alumni/visit-records')
}

// 获取分会列表
const getBranches = async () => {
  try {
    // TODO: 实现获取分会列表的接口
    // const res = await axios.get('/api/branches')
    // branches.value = res.data.data
  } catch (error) {
    console.error('获取分会列表失败', error)
    ElMessage.error('获取分会列表失败')
  }
}

// 跳转到分会详情
const goBranchDetail = (uuid) => {
  router.push(`/alumni/branch-detail/${uuid}`)
}

// 处理分会卡片点击
const handleChapterClick = (item) => {
  if (isInChapter(item.tag_id)) {
    // 已在分会，跳转到分会成员页面，传递来源标识
    sessionStorage.setItem('chapter_members_from', 'home')
    router.push(`/alumni/chapter-members/${item.tag_id}`)
  }
}

// 处理申请加入分会
const handleApplyChapter = async (item) => {
  try {
    const userInfo = JSON.parse(sessionStorage.getItem('user_info') || '{}')
    if (!userInfo.alumniId) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }
    const res = await service.post('/chapters/apply', {
      alumniId: userInfo.alumniId,
      tagId: item.tag_id
    })
    if (res.code === 0) {
      ElMessage.success(res.msg || '申请成功')
      // 刷新热门分会数据
      await fetchHotChapters()
    } else {
      ElMessage.error(res.msg || '申请失败')
    }
  } catch (error) {
    console.error('申请加入分会失败', error, error.response)
    if (error.response && error.response.data && error.response.data.msg) {
      ElMessage.error(error.response.data.msg)
    } else {
      ElMessage.error('申请加入分会失败')
    }
  }
}

// 获取申请状态
const getApplicationStatus = (tagId) => {
  return applicationStatus.value[String(tagId)] || null
}

// 获取分会封面URL
const getChapterCoverUrl = (branchName) => {
  // 根据分会名称生成默认封面，这里使用一个默认图片
  return '/logo.png'
}

// 判断是否在分会中
const isInChapter = (tagId) => {
  return joinedChapterIds.value.includes(String(tagId))
}
</script>

<style scoped>
.alumni-main-root {
  background: #f5f6fa;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.alumni-header {
  width: 100%;
  background: #fff;
  box-shadow: 0 2px 8px #c7d2fe22;
  display: flex;
  justify-content: center;
}
.alumni-header-inner {
  width: 100%;
  max-width: 1200px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  padding: 0 32px;
}
.alumni-header-title {
  font-size: 2.2rem;
  font-weight: bold;
  color: #2563eb;
  letter-spacing: 2px;
  white-space: nowrap;
}
.alumni-header-actions {
  display: flex;
  gap: 18px;
  align-items: center;
}
.alumni-header-name {
  font-size: 1.1rem;
  color: #2563eb;
  font-weight: bold;
  margin-right: 16px;
}
@media (max-width: 700px) {
  .alumni-header-inner {
    flex-direction: column;
    align-items: center;
    height: auto;
    padding: 12px 8px 8px 8px;
  }
  .alumni-header-title {
    font-size: 1.5rem;
    margin-bottom: 8px;
    text-align: center;
  }
  .alumni-header-actions {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 10px;
    align-items: center;
    justify-content: center;
    width: 100%;
  }
  .alumni-header-name {
    margin-right: 0;
    margin-bottom: 4px;
    display: block;
    text-align: center;
  }
  .alumni-header-actions .el-button {
    width: auto;
    min-width: 100px;
    max-width: 48vw;
    flex: 1 1 auto;
  }
}
.alumni-main-center {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.admin-dashboard {
  width: 100%;
  max-width: 1200px;
  margin: 48px auto 0 auto;
}
.admin-card3 {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 12px #c7d2fe22;
  padding: 24px 24px 18px 24px;
  margin-bottom: 32px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  transition: box-shadow 0.2s, transform 0.2s;
  cursor: pointer;
  min-height: 140px;
  position: relative;
}
.admin-card3:hover {
  box-shadow: 0 4px 24px #2563eb22;
  transform: translateY(-2px) scale(1.02);
}
.admin-card3-disabled {
  cursor: not-allowed;
  opacity: 0.6;
  pointer-events: none;
}
.admin-card3-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}
.admin-card3-title {
  font-size: 1.1rem;
  font-weight: bold;
  margin-bottom: 6px;
  color: #222;
}
.admin-card3-desc {
  color: #64748b;
  font-size: 1rem;
  margin-bottom: 28px;
  min-height: 24px;
}
.admin-card3-btn {
  width: 100%;
  font-size: 1rem;
  border-radius: 8px;
  margin-top: auto;
  padding: 8px 0;
}
.alumni-welcome {
  width: 100%;
  max-width: 900px;
  background: #2563eb;
  color: #fff;
  border-radius: 0 0 16px 16px;
  margin: 32px auto;
  display: flex;
  justify-content: center;
}
.alumni-welcome-content {
  width: 100%;
  max-width: 900px;
  text-align: center;
  padding: 32px 0 24px 0;
}
.alumni-welcome-title {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 16px;
}
.alumni-welcome-sub {
  font-size: 1.3rem;
  margin-bottom: 28px;
}
.alumni-welcome-btn {
  display: none;
}
.alumni-section {
  width: 100%;
  max-width: 900px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px #c7d2fe22;
  padding: 40px 48px;
  min-height: 80px;
  margin-bottom: 32px;
  font-size: 1.2rem;
}
.alumni-section-placeholder {
  color: #b0b3c6;
  text-align: center;
  font-style: italic;
}
.alumni-quick-entry {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.alumni-quick-title {
  font-size: 1.2rem;
  font-weight: bold;
  margin-bottom: 18px;
}
.alumni-quick-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 32px;
  width: 100%;
}
.alumni-footer {
  width: 100%;
  background: #f1f5f9;
  color: #222;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 60px;
  margin-top: 48px;
  font-size: 1rem;
  border-top: 1px solid #e5e7eb;
}
.alumni-footer-inner {
  width: 100%;
  max-width: 1200px;
  display: flex;
  justify-content: center;
  gap: 64px;
}
.alumni-activity-section {
  margin-bottom: 32px;
}
.alumni-activity-title {
  font-size: 1.3rem;
  font-weight: bold;
  margin-bottom: 18px;
  color: #2563eb;
}
.activity-section-outer {
  max-width: 1000px;
  margin: 0 auto;
  width: 100%;
}
.activity-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32px;
  width: 100%;
  justify-items: center;
}
.activity-card {
  width: 100%;
  min-width: 0;
  min-height: 320px;
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 2px 12px #e0e7ef22;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 18px 12px 16px 12px;
  transition: box-shadow 0.2s;
  border: 1px solid #f1f5fa;
  position: relative;
  box-sizing: border-box;
}
.activity-card:hover {
  box-shadow: 0 4px 24px #a5b4fc33;
  border-color: #c7d2fe;
}
.activity-card-img {
  width: 150px;
  height: 90px;
  border-radius: 10px;
  margin-bottom: 14px;
  overflow: hidden;
  background: #f5f6fa;
  display: flex;
  align-items: center;
  justify-content: center;
}
.activity-card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 10px;
  display: block;
}
.activity-card-title {
  font-size: 1.08rem;
  font-weight: 600;
  color: #222;
  margin-bottom: 8px;
  text-align: center;
  max-height: 2.8em;
  line-height: 1.4em;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-all;
}
.activity-card-desc {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 8px;
  min-height: 4.5em;
  max-height: 4.5em;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  word-break: break-all;
}
.activity-card-date {
  font-size: 1rem;
  color: #2563eb;
  width: 100%;
  text-align: center;
  margin-top: auto;
  margin-bottom: 0;
  position: static;
}
.activity-card-status {
  margin-top: 8px;
  margin-bottom: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 4px;
}
.status-label {
  color: #666;
  font-size: 0.9rem;
}
.activity-card-action {
  margin-top: 8px;
  display: flex;
  justify-content: center;
}

.activity-card-action .el-button {
  width: 100%;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
}

.activity-card-action .el-button--info.is-disabled {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  color: #c0c4cc;
}

@media (max-width: 900px) {
  .admin-dashboard .el-row {
    display: grid !important;
    grid-template-columns: 1fr !important;
    gap: 14px !important;
    margin-left: 0 !important;
    margin-right: 0 !important;
  }
  .admin-dashboard .el-col {
    width: 100% !important;
    max-width: 100% !important;
    flex: 1 1 100% !important;
    padding-left: 0 !important;
    padding-right: 0 !important;
    margin: 0 !important;
  }
  .admin-card3 {
    width: 100% !important;
    box-sizing: border-box !important;
    margin-bottom: 0;
    border-radius: 10px;
    padding: 16px 8px 12px 8px;
  }
}
@media (max-width: 600px) {
  .activity-list {
    grid-template-columns: 1fr;
    gap: 14px;
  }
  .activity-section-outer {
    max-width: 100vw;
    padding: 0 4vw;
  }
  .activity-card-img {
    width: 90vw;
    max-width: 320px;
    height: 24vw;
    max-height: 120px;
  }
  .alumni-quick-list {
    grid-template-columns: 1fr;
    gap: 14px;
  }
}
.action-buttons {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 24px;
  margin-top: 32px;
  width: 100%;
}

.welcome-button {
  min-width: 180px;
  height: 50px;
  font-size: 16px;
  border-radius: 25px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 32px;
}

.welcome-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.button-text {
  margin-left: 8px;
}

@media (max-width: 600px) {
  .action-buttons {
    flex-direction: column;
    gap: 16px;
  }
  
  .welcome-button {
    width: 100%;
    max-width: 280px;
  }
}
.pending-count-tip {
  margin-top: 4px;
  font-size: 1.1em;
}
.card-with-badge {
  position: relative;
}
.badge-corner {
  position: absolute;
  top: 12px;
  right: 18px;
  background: #f56c6c;
  color: #fff;
  border-radius: 50%;
  min-width: 22px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  font-size: 1em;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(245,108,108,0.15);
  z-index: 2;
  padding: 0 6px;
  pointer-events: none;
}
</style> 