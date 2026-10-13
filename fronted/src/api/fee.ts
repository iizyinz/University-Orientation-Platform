import request from '@/utils/request'

// Fee Item
export function getFeeItemList() {
  return request.get('/fee/item/list')
}
export function getFeeItem(id: number) {
  return request.get(`/fee/item/${id}`)
}
export function createFeeItem(data: any) {
  return request.post('/fee/item', data)
}
export function updateFeeItem(data: any) {
  return request.put('/fee/item', data)
}
export function deleteFeeItem(id: number) {
  return request.delete(`/fee/item/${id}`)
}

// Fee Major Config
export function getFeeConfigList() {
  return request.get('/fee/config/list')
}
export function getFeeConfigByMajor(majorId: number) {
  return request.get(`/fee/config/byMajor/${majorId}`)
}
export function getFeeConfig(id: number) {
  return request.get(`/fee/config/${id}`)
}
export function createFeeConfig(data: any) {
  return request.post('/fee/config', data)
}
export function updateFeeConfig(data: any) {
  return request.put('/fee/config', data)
}
export function deleteFeeConfig(id: number) {
  return request.delete(`/fee/config/${id}`)
}

// Fee Config Import
export function downloadFeeConfigTemplate() {
  return request.get('/fee/config/template', { responseType: 'blob' })
}
export function importFeeConfigs(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/fee/config/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}
