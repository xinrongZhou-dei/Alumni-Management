import axios from 'axios';

export function searchAlumni(params) {
  return axios.post('/api/alumni/search', params);
}

export function searchChapterMembers(params) {
  return axios.post('/api/alumni/search-by-chapter', params);
}

export function getCurrentUserInfo(alumniId) {
  return axios.get(`/api/user/info?alumniId=${alumniId}`);
}

// 分会申请相关API
export function applyForChapter(params) {
  return axios.post('/api/chapters/apply', params);
}

export function getAllApplications() {
  return axios.get('/api/chapters/applications');
}

export function getMyApplications(alumniId) {
  return axios.get(`/api/chapters/my-applications?alumniId=${alumniId}`);
}

export function getChapterApplications(tagId) {
  return axios.get(`/api/chapters/chapter-applications?tagId=${tagId}`);
}

export function reviewApplication(params) {
  return axios.post('/api/chapters/review-application', params);
} 