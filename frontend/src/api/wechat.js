import service from '../utils/request'

// 验证微信关注状态
export function verifyFollow(alumniId) {
  return service.post('/wechat/verify-follow', {
    alumni_id: alumniId
  })
}

// 获取二维码URL
export function getQRCode(alumniId) {
  return service.get(`/wechat/qrcode/${alumniId}`)
}

// 更新openid
export function updateOpenid(alumniId, openid) {
  return service.post('/wechat/update-openid', {
    alumni_id: alumniId,
    openid: openid
  })
} 