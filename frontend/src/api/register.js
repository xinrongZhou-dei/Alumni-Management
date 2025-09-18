import request from '../utils/request'

// 生成唯一学号
export function generateAlumniId() {
  return request({
    url: '/register/generate-alumni-id',
    method: 'post'
  })
}

// 管理员添加校友
export function adminAddAlumni(data) {
  return request({
    url: '/alumni-add',
    method: 'post',
    data
  })
}

// 校验学号唯一性
export function verifyAlumniId(alumniId) {
  return request({
    url: '/register/verify-alumni-id',
    method: 'get',
    params: { alumniId }
  })
} 