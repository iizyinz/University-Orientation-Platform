import request from '@/utils/request'

// ==================== 管理端：楼栋 ====================

export function getBuildingList() {
  return request.get('/dorm/building/list')
}

export function createBuilding(data: any) {
  return request.post('/dorm/building', data)
}

export function updateBuilding(data: any) {
  return request.put('/dorm/building', data)
}

export function deleteBuilding(id: number) {
  return request.delete(`/dorm/building/${id}`)
}

// ==================== 管理端：房间 ====================

export function getRoomList() {
  return request.get('/dorm/room/list')
}

export function getRoomByBuilding(buildingId: number) {
  return request.get(`/dorm/room/byBuilding/${buildingId}`)
}

export function createRoom(data: any) {
  return request.post('/dorm/room', data)
}

export function updateRoom(data: any) {
  return request.put('/dorm/room', data)
}

export function deleteRoom(id: number) {
  return request.delete(`/dorm/room/${id}`)
}

export function batchCreateRooms(data: { buildingId: number; floorNo: number; count: number; bedCount: number; roomType: string }) {
  return request.post('/dorm/room/batch', data)
}

// Alloc Import
export function downloadAllocTemplate() {
  return request.get('/dorm/alloc/template', { responseType: 'blob' })
}
export function importAllocs(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/dorm/alloc/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}

// ==================== 管理端：分配 ====================

export function getAllocList() {
  return request.get('/dorm/alloc/list')
}

export function createAlloc(data: any) {
  return request.post('/dorm/alloc', data)
}

export function updateAlloc(data: any) {
  return request.put('/dorm/alloc', data)
}

export function deleteAlloc(id: number) {
  return request.delete(`/dorm/alloc/${id}`)
}

// ==================== 管理端：选房配置 ====================

export function getConfigList() {
  return request.get('/dorm/config/list')
}

export function createConfig(data: any) {
  return request.post('/dorm/config', data)
}

export function updateConfig(data: any) {
  return request.put('/dorm/config', data)
}

export function deleteConfig(id: number) {
  return request.delete(`/dorm/config/${id}`)
}

// 兼容 DormTime.vue 的导入名
export const getDormConfigList = getConfigList
export const createDormConfig = createConfig
export const updateDormConfig = updateConfig
export const deleteDormConfig = deleteConfig

// ==================== 学生端：宿舍选择 ====================

export function getDormConfig() {
  return request.get('/student/dorm/config')
}

export function getDormAlloc(userId: number) {
  return request.get('/student/dorm/alloc', { params: { userId } })
}

export function getDormRooms(buildingId: number, userId: number) {
  return request.get('/student/dorm/rooms', { params: { buildingId, userId } })
}

export function chooseDorm(data: { userId: number; roomId: number; bedNo: number }) {
  return request.post('/student/dorm/choose', data)
}

export function getMyDorm(userId: number) {
  return request.get('/student/dorm/my', { params: { userId } })
}
