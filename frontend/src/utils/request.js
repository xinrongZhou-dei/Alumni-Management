import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // 后端API地址
  timeout: 5000, // 请求超时时间
  withCredentials: true, // 允许跨域携带cookie
  headers: {
    'Content-Type': 'application/json',
    'X-Requested-With': 'XMLHttpRequest'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从sessionStorage获取token并添加到请求头
    const token = sessionStorage.getItem('alumni_token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    // 确保CORS相关头部设置正确
    config.headers['Accept'] = 'application/json, text/plain, */*'
    
    return config
  },
  error => {
    // 对请求错误做些什么
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    // 只要有code，直接返回业务层，由业务层决定如何处理
    return res
  },
  error => {
    console.error('响应错误:', error)
    if (error.response) {
      console.error('错误状态码:', error.response.status)
      console.error('错误数据:', error.response.data)
      // token失效处理
      if (error.response.status === 401 || (error.response.data && (error.response.data.code === 401 || error.response.data.msg?.includes('token无效') || error.response.data.msg?.includes('未登录') || error.response.data.msg?.includes('账号已在其他地方登录')))) {
        // 清除所有token相关sessionStorage
        sessionStorage.removeItem('alumni_token')
        sessionStorage.removeItem('user_info')
        sessionStorage.removeItem('login_success_flag')
        localStorage.removeItem('admin_permission')
        // 跳转到登录页
        router.push('/login')
        return // 阻止后续ElMessage弹窗
      }
    }
    ElMessage({
      message: error.message || '请求失败',
      type: 'error',
      duration: 2000
    })
    return Promise.reject(error)
  }
)

export default service 