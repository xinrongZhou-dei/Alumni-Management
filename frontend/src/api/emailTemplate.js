import axios from 'axios'

export function getEmailTemplates(targetType) {
  return axios.get('/api/email-template/list', { params: { targetType } })
}

export function addEmailTemplate(data) {
  return axios.post('/api/email-template', data)
}

export function updateEmailTemplate(id, data) {
  return axios.put(`/api/email-template/${id}`, data)
}

export function deleteEmailTemplate(id) {
  return axios.delete(`/api/email-template/${id}`)
}

export function getEmailVariables(data) {
  return axios.post('/api/email-template/variables', data)
}

export function getEmailVariablesDynamic(data) {
  return axios.post('/api/email-template/variables-dynamic', data);
}

export function getActivityList() {
  return axios.get('/api/activity/list');
} 