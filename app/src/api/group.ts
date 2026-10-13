import request from '@/utils/request'

export function getGroupList() { return request.get('/group/list') }
export function getGroup(id: number) { return request.get(`/group/${id}`) }
export function createGroup(data: any) { return request.post('/group', data) }
export function updateGroup(data: any) { return request.put('/group', data) }
export function deleteGroup(id: number) { return request.delete(`/group/${id}`) }
