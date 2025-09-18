// 安全token操作工具，统一管理alumni_token和register_token
// 已切换为cookie存储

const TOKEN_KEYS = {
  ALUMNI: 'alumni_token',
  REGISTER: 'register_token'
}

// 仅允许存储JWT格式的token
function isValidJWT(token) {
  return typeof token === 'string' && token.split('.').length === 3
}

// 设置cookie的通用函数
function setCookie(name, value, maxAge = 43200) { // 默认12小时
  document.cookie = `${name}=${value}; path=/; SameSite=Lax; max-age=${maxAge}` 
  // 注意：在生产环境HTTPS下，应添加 ;secure 标志
}

// 获取cookie的通用函数
function getCookie(name) {
  const cookies = document.cookie.split(';')
  for (let cookie of cookies) {
    const [cookieName, cookieValue] = cookie.trim().split('=')
    if (cookieName === name) {
      return cookieValue
    }
  }
  return null
}

// 删除cookie的通用函数
function deleteCookie(name) {
  document.cookie = `${name}=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT`
}

// alumni_token相关方法仅用于兼容register_token等，alumni_token由后端Set-Cookie实现
export function setToken(type, token) {
  if (!TOKEN_KEYS[type]) throw new Error('未知token类型')
  if (type === 'REGISTER') return // 注册token只由后端Set-Cookie写入
  if (isValidJWT(token)) {
    setCookie(TOKEN_KEYS[type], token)
  }
}

export function getToken(type) {
  if (!TOKEN_KEYS[type]) throw new Error('未知token类型')
  const token = getCookie(TOKEN_KEYS[type])
  return isValidJWT(token) ? token : null
}

export function removeToken(type) {
  if (!TOKEN_KEYS[type]) throw new Error('未知token类型')
  if (type === 'REGISTER') return // 注册token只由后端Set-Cookie清除
  deleteCookie(TOKEN_KEYS[type])
}

// 一键清除所有token
export function clearAllTokens() {
  Object.values(TOKEN_KEYS).forEach(key => deleteCookie(key))
}

// 通用信息cookie操作
export function setInfo(key, value, maxAge = 43200) {
  setCookie(key, encodeURIComponent(JSON.stringify(value)), maxAge)
}

export function getInfo(key) {
  const val = getCookie(key)
  if (!val) return null
  try {
    return JSON.parse(decodeURIComponent(val))
  } catch {
    return null
  }
}

export function removeInfo(key) {
  deleteCookie(key)
} 