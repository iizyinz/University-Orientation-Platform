import request from '@/utils/request'

export function login(data: { username: string; password: string }) {
  return request.post('/auth/login', data)
}

export function getUserInfo(id: number) {
  return request.get('/auth/info', { params: { id } })
}

export function updateProfile(data: any) {
  return request.put('/auth/profile', data)
}

export function updatePassword(data: { id: number; oldPassword: string; newPassword: string }) {
  return request.put('/auth/password', data)
}
