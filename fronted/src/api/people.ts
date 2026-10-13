import request from '@/utils/request'

// Counselor
export function getCounselorList() {
  return request.get('/people/counselor/list')
}
export function getCounselorByCollege(collegeId: number) {
  return request.get(`/people/counselor/byCollege/${collegeId}`)
}
export function getCounselor(id: number) {
  return request.get(`/people/counselor/${id}`)
}
export function createCounselor(data: any) {
  return request.post('/people/counselor', data)
}
export function updateCounselor(data: any) {
  return request.put('/people/counselor', data)
}
export function deleteCounselor(id: number) {
  return request.delete(`/people/counselor/${id}`)
}
export function resetCounselorPassword(data: { id: number; newPassword?: string }) {
  return request.put('/people/counselor/reset-password', data)
}

// Counselor Excel Import
export function downloadCounselorTemplate() {
  return request.get('/people/counselor/template', { responseType: 'blob' })
}
export function importCounselors(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/people/counselor/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// Volunteer
export function getVolunteerList() {
  return request.get('/people/volunteer/list')
}
export function getVolunteerByType(type: number) {
  return request.get(`/people/volunteer/byType/${type}`)
}
export function getVolunteer(id: number) {
  return request.get(`/people/volunteer/${id}`)
}
export function createVolunteer(data: any) {
  return request.post('/people/volunteer', data)
}
export function updateVolunteer(data: any) {
  return request.put('/people/volunteer', data)
}
export function deleteVolunteer(id: number) {
  return request.delete(`/people/volunteer/${id}`)
}
export function resetVolunteerPassword(data: { id: number; newPassword?: string }) {
  return request.put('/people/volunteer/reset-password', data)
}

// Volunteer Excel Import
export function downloadVolunteerTemplate() {
  return request.get('/people/volunteer/template', { responseType: 'blob' })
}
export function importVolunteers(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/people/volunteer/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// Admin
export function getAdminList() {
  return request.get('/people/admin/list')
}
export function createAdmin(data: any) {
  return request.post('/people/admin', data)
}
export function updateAdmin(data: any) {
  return request.put('/people/admin', data)
}
export function resetAdminPassword(data: { id: number; newPassword?: string }) {
  return request.put('/people/admin/reset-password', data)
}
export function deleteAdmin(id: number) {
  return request.delete(`/people/admin/${id}`)
}

// Student Info
export function getStudentList() {
  return request.get('/people/student/list')
}
export function getStudentStats() {
  return request.get('/people/student/stats')
}
export function getStudent(id: number) {
  return request.get(`/people/student/${id}`)
}
export function createStudent(data: any) {
  return request.post('/people/student', data)
}
export function updateStudent(data: any) {
  return request.put('/people/student', data)
}
export function deleteStudent(id: number) {
  return request.delete(`/people/student/${id}`)
}
export function resetStudentPassword(data: { id: number; newPassword?: string }) {
  return request.put('/people/student/reset-password', data)
}

// Student Excel Import
export function downloadStudentTemplate() {
  return request.get('/people/student/template', { responseType: 'blob' })
}
export function importStudents(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/people/student/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
