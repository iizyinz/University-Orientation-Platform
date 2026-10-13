import request from '@/utils/request'

// Handbook
export function getHandbookList() { return request.get('/cms/handbook/list') }
export function createHandbook(data: any) { return request.post('/cms/handbook', data) }
export function updateHandbook(data: any) { return request.put('/cms/handbook', data) }
export function deleteHandbook(id: number) { return request.delete(`/cms/handbook/${id}`) }

// Campus Map
export function getMapList() { return request.get('/cms/map/list') }
export function createMap(data: any) { return request.post('/cms/map', data) }
export function updateMap(data: any) { return request.put('/cms/map', data) }
export function deleteMap(id: number) { return request.delete(`/cms/map/${id}`) }

// Notice
export function getNoticeList() { return request.get('/cms/notice/list') }
export function createNotice(data: any) { return request.post('/cms/notice', data) }
export function updateNotice(data: any) { return request.put('/cms/notice', data) }
export function deleteNotice(id: number) { return request.delete(`/cms/notice/${id}`) }
