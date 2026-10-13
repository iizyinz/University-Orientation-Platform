import request from '@/utils/request'

// 入学手册
export function getHandbookList() {
  return request.get('/student/handbook/list')
}

export function getHandbookDetail(id: number) {
  return request.get(`/student/handbook/${id}`)
}

// 校园地图
export function getCampusMapList() {
  return request.get('/student/campus-map/list')
}

// 新生群
export function getMyGroups(userId: number) {
  return request.get('/student/groups', { params: { userId } })
}

// FAQ 智能问答
export function getFaqCategories() {
  return request.get('/student/faq/categories')
}

export function searchFaq(keyword: string) {
  return request.get('/student/faq/search', { params: { keyword } })
}

export function getFaqByCategory(categoryId: number) {
  return request.get(`/student/faq/by-category/${categoryId}`)
}

export function hitFaq(id: number) {
  return request.post(`/student/faq/hit/${id}`)
}
