import request from '@/utils/request'

// 获取预约记录列表
export function getVisitRecordList(params) {
  return request({
    url: '/visitrecord/admin/visit-records',
    method: 'get',
    params
  })
}

// 获取校友的预约记录列表
export function getAlumniVisitRecordList(alumniId) {
  return request({
    url: '/visitrecord/admin/visit-records/alumni',
    method: 'get',
    params: { alumniId }
  })
}

// 更新预约状态
export function updateVisitStatus(data) {
  return request({
    url: '/visitrecord/admin/visit-records/status',
    method: 'put',
    data
  })
}

// 批量更新预约状态
export function batchUpdateVisitStatus(data) {
  return request({
    url: '/visitrecord/admin/visit-records/batch-status',
    method: 'put',
    data
  })
}

// 获取预约记录详情
export function getVisitRecordDetail(visitUUID) {
  return request({
    url: `/visitrecord/admin/visit-records/${visitUUID}`,
    method: 'get'
  })
} 