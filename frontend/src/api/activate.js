import service from '../utils/request'

export function activateAccount(data) {
  return service.post('/activate/verify-info', data)
}
export function setActivatePassword(data) {
  return service.post('/activate/set-password', data)
}
export function verifyActivateCode(data) {
  return service.post('/activate/verify-code', data)
}
export function finalizeActivate(data) {
  return service.post('/activate/finish', data)
}
export function setActivateEmail(data) {
  // 假设后端发送验证码也是/activate/verify-code或/activate/send-code，需与后端实际接口一致
  // 这里暂用/activate/verify-code，若有单独接口请告知
  return service.post('/activate/verify-code', data)
} 