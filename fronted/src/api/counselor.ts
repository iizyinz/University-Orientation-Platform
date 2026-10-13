import request from '@/utils/request'

export function getCounselorDashboardStats(userId: number) {
  return request.get('/counselor/dashboard/stats', { params: { userId } })
}

export function getCounselorClasses(userId: number) {
  return request.get('/counselor/classes', { params: { userId } })
}

export function getCounselorStudents(userId: number, params?: { classId?: number; keyword?: string }) {
  return request.get('/counselor/students', { params: { userId, ...params } })
}

export function getStudentDocuments(studentId: number) {
  return request.get('/counselor/student/documents', { params: { studentId } })
}

export function exportStudents(userId: number, params?: { classId?: number; keyword?: string }) {
  return request.get('/counselor/students/export', { params: { userId, ...params }, responseType: 'blob' })
}

export function scanStudent(idCard: string) {
  return request.get('/counselor/scan/student', { params: { idCard } })
}

export function checkin(studentId: number) {
  return request.post('/counselor/scan/checkin', { studentId })
}

export function getCounselorGroups(userId: number) {
  return request.get('/counselor/groups', { params: { userId } })
}

export function createCounselorGroup(data: any, userId: number) {
  return request.post('/counselor/groups/create', data, { params: { userId } })
}

export function updateCounselorGroup(data: any) {
  return request.put('/counselor/groups/update', data)
}

export function uploadGroupQr(file: File, groupId: number) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('groupId', String(groupId))
  return request.post('/counselor/groups/upload-qr', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
