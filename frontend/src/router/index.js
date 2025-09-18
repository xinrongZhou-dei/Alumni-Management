import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/alumnus/Login.vue'
import AlumniMain from '../components/AlumniMain.vue'
import AlumniRegister from '../components/alumnus/AlumniRegister.vue'
import AlumniRegisterVerify from '../components/alumnus/AlumniRegisterVerify.vue'
import AlumniRegisterSetPassword from '../components/alumnus/AlumniRegisterSetPassword.vue'
import AlumniRegisterSubmitInfo from '../components/alumnus/AlumniRegisterSubmitInfo.vue'
import AlumniChangePassword from '../components/alumnus/AlumniChangePassword.vue'
import AlumniForgotPassword from '../components/alumnus/AlumniForgotPassword.vue'
import AlumniManage from '../components/alumnimanager/AlumniManage.vue'
import AdminAlumniAdd from '../components/alumnimanager/AdminAlumniAdd.vue'
import TagManage from '../components/chapter/TagManage.vue'
import AlumniEdit from '../components/alumnus/AlumniEdit.vue'
import AlumniEditSelf from '../components/alumnus/AlumniEditSelf.vue'
import ActivityEdit from '../components/activity/ActivityEdit.vue'
import AlumniSearch from '../components/alumnibranchlist/AlumniSearch.vue'
import { ElMessage } from 'element-plus'
import PermissionManage from '../components/PermissionManage.vue'
import AlumniActivate from '../components/activate/AlumniActivate.vue'
import AlumniActivateSetPassword from '../components/activate/AlumniActivateSetPassword.vue'
import AlumniActivateSetEmail from '../components/activate/AlumniActivateSetEmail.vue'
import AlumniActivateSuccess from '../components/alumnimanager/AlumniActivateSuccess.vue'
import VisitRecordList from '../components/visit/VisitRecordList.vue'

const routes = [
  { path: '/', component: AlumniMain },
  { path: '/login', component: Login },
  { path: '/alumni/register', component: AlumniRegister },
  { path: '/alumni/register/verify', component: AlumniRegisterVerify },
  { path: '/alumni/register/set-password', component: AlumniRegisterSetPassword },
  { path: '/alumni/register/submit-info', component: AlumniRegisterSubmitInfo },
  { path: '/alumni/change-password', component: AlumniChangePassword },
  { path: '/alumni/forgot-password', component: AlumniForgotPassword },
  { path: '/alumni/manage', component: AlumniManage },
  {
    path: '/alumni/detail/:id',
    name: 'AlumniDetail',
    component: () => import('../components/alumnus/AlumniDetail.vue')
  },
  {
    path: '/alumni/edit',
    name: 'AlumniEditSelf',
    component: AlumniEditSelf
  },
  {
    path: '/admin/alumni-add',
    name: 'AdminAlumniAdd',
    component: AdminAlumniAdd
  },
  {
    path: '/admin/tag-manage',
    name: 'TagManage',
    component: TagManage
  },
  {
    path: '/alumni/edit/:id',
    name: 'AlumniEdit',
    component: AlumniEdit
  },
  {
    path: '/alumni/search',
    name: 'AlumniSearch',
    component: AlumniSearch
  },
  {
    path: '/alumni/chapter-members/:chapterId',
    name: 'ChapterMemberList',
    component: () => import('../components/chapter/ChapterMemberList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/activity-manage',
    name: 'ActivityManage',
    component: () => import('../components/activity/ActivityManage.vue')
  },
  {
    path: '/admin/activity-detail/:id',
    name: 'ActivityDetail',
    component: () => import('../components/activity/ActivityDetail.vue')
  },
  {
    path: '/admin/activity-edit/:id',
    name: 'ActivityEdit',
    component: ActivityEdit
  },
  {
    path: '/admin/activity-add',
    name: 'ActivityAdd',
    component: () => import('../components/activity/ActivityAdd.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/alumni/activity-detail/:id',
    name: 'AlumniActivityDetail',
    component: () => import('../components/activity/AlumniActivityDetail.vue')
  },
  {
    path: '/alumni/activity-signup',
    name: 'AlumniActivitySignup',
    component: () => import('../components/activity/AlumniActivitySignup.vue')
  },
  {
    path: '/admin/email-template-manage',
    name: 'EmailTemplateManage',
    component: () => import('../components/email/EmailTemplateManage.vue')
  },
  {
    path: '/admin/email-template-add',
    name: 'EmailTemplateAdd',
    component: () => import('../components/email/EmailTemplateAdd.vue')
  },
  {
    path: '/admin/email-send',
    name: 'EmailSend',
    component: () => import('../components/email/EmailSend.vue')
  },
  {
    path: '/email/send',
    name: 'EmailSendDirect',
    component: () => import('../components/email/EmailSend.vue')
  },
  {
    path: '/admin/data-analysis',
    name: 'AdminDataAnalysis',
    component: () => import('../components/AdminDataAnalysis.vue')
  },
  // 修改预约记录相关路由
  {
    path: '/admin/visit-records',
    name: 'VisitRecordList',
    // 注意：本页面所有API请求必须使用/visitrecord/admin/visit-records作为接口路径，切勿写/api/admin/visit-records，否则会404
    component: VisitRecordList,
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/visit-records/:id',
    name: 'VisitRecordDetail',
    // 注意：本页面所有API请求必须使用/visitrecord/admin/visit-records作为接口路径，切勿写/api/admin/visit-records，否则会404
    component: () => import('@/components/visit/VisitRecordDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/visit-record/form',
    name: 'VisitRecordForm',
    component: () => import('@/components/visit/VisitRecordForm.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/alumni/visit-records',
    name: 'AlumniVisitRecordList',
    component: () => import('@/components/AlumniVisitRecordList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/alumni/branch-list',
    name: 'AlumniBranchList',
    component: () => import('@/components/alumnibranchlist/AlumniBranchList.vue'),
    meta: { requiresAuth: true }
  },

  {
    path: '/admin/chapter-review',
    component: () => import('@/components/chapter/ChapterReview.vue')
  },
  {
    path: '/admin/permission-manage',
    name: 'PermissionManage',
    component: PermissionManage,
    meta: { requiresAuth: true }
  },
  { path: '/alumni/activate', component: AlumniActivate },
  { path: '/alumni/activate/set-password', component: AlumniActivateSetPassword },
  { path: '/alumni/activate/set-email', component: AlumniActivateSetEmail },
  { path: '/alumni/activate/success', component: AlumniActivateSuccess },
  {
    path: '/admin/alumni-review',
    component: () => import('@/components/AlumniReview.vue')
  },
  {
    path: '/alumni/review',
    name: 'AlumniReview',
    component: () => import('@/components/AlumniReview.vue')
  },
  // 其他页面路由...
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // 如果有保存的位置（比如用户点击了浏览器的后退按钮），则恢复到该位置
    if (savedPosition) {
      return savedPosition
    }
    // 否则滚动到页面顶部
    return { top: 0 }
  }
})

// 全局路由守卫
router.beforeEach((to, from, next) => {
  // 激活相关页面路径
  const activatePaths = [
    '/alumni/activate',
    '/alumni/activate/set-password',
    '/alumni/activate/set-email',
    '/alumni/activate/success'
  ]
  // 如果离开激活相关页面，自动清除token
  if (!activatePaths.includes(to.path)) {
    sessionStorage.removeItem('activate_token')
    sessionStorage.removeItem('activate_token_expire')
  }
  // 激活流程守卫：除第一个页面外，其他激活页面都必须有token
  if (
    activatePaths.includes(to.path) &&
    to.path !== '/alumni/activate'
  ) {
    const token = sessionStorage.getItem('activate_token')
    const expire = sessionStorage.getItem('activate_token_expire')
    if (!token || !expire || Date.now() > Number(expire)) {
      sessionStorage.removeItem('activate_token')
      sessionStorage.removeItem('activate_token_expire')
      ElMessage.error('请重新激活')
      return next('/')
    }
  }
  // 首页和白名单页面
  const whiteList = [
    '/',
    '/login',
    '/alumni/forgot-password',
    '/alumni/register',
    '/alumni/register/verify',
    '/alumni/register/set-password',
    '/alumni/register/submit-info',
    '/alumni/activate',
    '/alumni/activate/set-password',
    '/alumni/activate/set-email',
    '/alumni/activate/success'
  ]
  if (whiteList.includes(to.path)) {
    return next()
  }
  
  // 其他页面校验用户是否登录
  const userInfo = sessionStorage.getItem('user_info')
  if (!userInfo) {
    sessionStorage.setItem('redirect_url', to.fullPath)
    ElMessage({ message: '请先登录', type: 'warning', duration: 2000, showClose: false })
    return next('/login') // 没有用户信息跳转到首页
  }

  // 检查权限（如果需要）
  const user = JSON.parse(userInfo);
  const isAdmin = user.role === 'admin';
  
  const adminRoutes = [
    '/admin/activity-manage',
    '/admin/activity-detail',
    '/admin/activity-edit',
    '/admin/activity-add',
    '/admin/alumni-add',
    '/admin/tag-manage'
    // ... 其他管理员路由
  ];

  if (to.path.startsWith('/admin') && adminRoutes.some(route => to.path.startsWith(route)) && !isAdmin) {
    ElMessage({ message: '无权访问', type: 'error', duration: 2000, showClose: false });
    return next(from.path || '/'); // 跳转回之前的页面或首页
  }

  next()
})

export default router 