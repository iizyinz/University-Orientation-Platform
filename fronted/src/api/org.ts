import request from '@/utils/request'

// College
export function getCollegeList() {
  return request.get('/org/college/list')
}
export function getCollege(id: number) {
  return request.get(`/org/college/${id}`)
}
export function createCollege(data: any) {
  return request.post('/org/college', data)
}
export function updateCollege(data: any) {
  return request.put('/org/college', data)
}
export function deleteCollege(id: number) {
  return request.delete(`/org/college/${id}`)
}

// Major
export function getMajorList() {
  return request.get('/org/major/list')
}
export function getMajorByCollege(collegeId: number) {
  return request.get(`/org/major/byCollege/${collegeId}`)
}
export function getMajor(id: number) {
  return request.get(`/org/major/${id}`)
}
export function createMajor(data: any) {
  return request.post('/org/major', data)
}
export function updateMajor(data: any) {
  return request.put('/org/major', data)
}
export function deleteMajor(id: number) {
  return request.delete(`/org/major/${id}`)
}

// Class
export function getClassList() {
  return request.get('/org/class/list')
}
export function getClassByMajor(majorId: number) {
  return request.get(`/org/class/byMajor/${majorId}`)
}
export function getClass(id: number) {
  return request.get(`/org/class/${id}`)
}
export function createClass(data: any) {
  return request.post('/org/class', data)
}
export function updateClass(data: any) {
  return request.put('/org/class', data)
}
export function deleteClass(id: number) {
  return request.delete(`/org/class/${id}`)
}

// Major Import
export function downloadMajorTemplate() {
  return request.get('/org/major/template', { responseType: 'blob' })
}
export function importMajors(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/org/major/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}

// Class Import
export function downloadClassTemplate() {
  return request.get('/org/class/template', { responseType: 'blob' })
}
export function importClasses(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/org/class/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}

// Counselor-Class Assignment
export function getCounselorClassList(collegeId?: number) {
  return request.get('/org/counselor-class/list', { params: collegeId ? { collegeId } : {} })
}
export function assignCounselorClasses(data: { counselorId: number; classIds: number[] }) {
  return request.put('/org/counselor-class/assign', data)
}
export function downloadCounselorClassTemplate() {
  return request.get('/org/counselor-class/template', { responseType: 'blob' })
}
export function importCounselorClasses(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/org/counselor-class/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}
