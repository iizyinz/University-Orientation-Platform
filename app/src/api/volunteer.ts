import request from '@/utils/request'

export function scanKey(idCard: string) {
  return request.get('/volunteer/scan/key', { params: { idCard } })
}
export function distributeKey(data: { studentId: number; roomId: number; volunteerId: number }) {
  return request.post('/volunteer/scan/key', data)
}
export function getKeyStats(volunteerId: number) {
  return request.get('/volunteer/stats/key', { params: { volunteerId } })
}

export function scanUniform(idCard: string) {
  return request.get('/volunteer/scan/uniform', { params: { idCard } })
}
export function distributeUniform(data: { studentId: number; volunteerId: number; clothingSize: string; shoeSize: string }) {
  return request.post('/volunteer/scan/uniform', data)
}
export function getUniformStats(volunteerId: number) {
  return request.get('/volunteer/stats/uniform', { params: { volunteerId } })
}
