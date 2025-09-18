import './assets/main.css'
import axios from 'axios'
import { createApp } from 'vue'
import App from './App.vue'
import router from './router' // 只用router/index.js
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import service from './utils/request'

// 路由页面组件（后续会新建）
import AlumniMain from './components/AlumniMain.vue'
import AlumniRegister from './components/alumnus/AlumniRegister.vue'
import AlumniRegisterVerify from './components/alumnus/AlumniRegisterVerify.vue'
import AlumniRegisterSetPassword from './components/alumnus/AlumniRegisterSetPassword.vue'
import AlumniRegisterSubmitInfo from './components/alumnus/AlumniRegisterSubmitInfo.vue'

// 设置axios基础URL为空，所有/api请求都通过vite代理
axios.defaults.baseURL = ''

// 全局请求拦截器，自动加token
axios.interceptors.request.use(config => {
  const token = sessionStorage.getItem('alumni_token')
  if (token) {
    config.headers['Authorization'] = token
  }
  return config
}, error => Promise.reject(error))

const routes = [
  { path: '/', component: AlumniMain },
  { path: '/alumni', component: AlumniMain },
  { path: '/alumni/register', component: AlumniRegister },
  { path: '/alumni/register/verify', component: AlumniRegisterVerify },
  { path: '/alumni/register/set-password', component: AlumniRegisterSetPassword },
  { path: '/alumni/register/submit-info', component: AlumniRegisterSubmitInfo },
  { path: '/:catchAll(.*)', redirect: '/' }
]

async function initializeApp() {
  const token = sessionStorage.getItem('alumni_token')
  if(!token){
    const app = createApp(App)
    app.use(router)
    app.use(ElementPlus)
    app.mount('#app')
    return
  }
  try {
    const response = await service.get('/check-auth');
    if (response.code === 0) {
      const userInfo = {
        chineseName: response.data.chineseName,
        affiliation: response.data.affiliation,
        role: response.data.role,
        alumniId: response.data.alumniId,
      };
      sessionStorage.setItem('user_info', JSON.stringify(userInfo));
    } else {
      // Token无效或过期，清除本地存储
      sessionStorage.removeItem('user_info');
    }
  } catch (error) {
    // 请求失败（网络错误或后端服务未启动），也清除本地存储
    sessionStorage.removeItem('user_info');
  }

  const app = createApp(App)
  app.use(router)
  app.use(ElementPlus)
  app.mount('#app')
}

initializeApp();
