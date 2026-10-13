import request from '@/utils/request'

// Process Config
export function getProcessList() {
  return request.get('/process/list')
}
export function getProcess(id: number) {
  return request.get(`/process/${id}`)
}
export function createProcess(data: any) {
  return request.post('/process', data)
}
export function updateProcess(data: any) {
  return request.put('/process', data)
}
export function deleteProcess(id: number) {
  return request.delete(`/process/${id}`)
}
