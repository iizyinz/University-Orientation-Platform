import request from '@/utils/request'

export function getStudentInfo(userId: number) {
  return request.get('/student/info', { params: { userId } })
}

export function updateStudentInfo(data: any) {
  return request.put('/student/info', data)
}

export function updateUniformSize(data: { userId: number; clothingSize: string; shoeSize: string }) {
  return request.put('/student/uniform-size', data)
}

export function getReportCode(userId: number) {
  return request.get('/student/report-code', { params: { userId } })
}

export function getFlowStatus(userId: number) {
  return request.get('/student/flow-status', { params: { userId } })
}

export function getNotices(limit: number = 10) {
  return request.get('/student/notices', { params: { limit } })
}

export function getNoticeDetail(id: number) {
  return request.get(`/student/notices/${id}`)
}
