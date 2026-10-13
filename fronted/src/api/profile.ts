import request from '@/utils/request'

export function getMyFee(userId: number) {
  return request.get('/student/fee', { params: { userId } })
}

export function updateStudentInfo(data: any) {
  return request.put('/student/info', data)
}
