import request from '@/utils/request'

// Category
export function getFaqCategoryList() { return request.get('/faq/category/list') }
export function createFaqCategory(data: any) { return request.post('/faq/category', data) }
export function updateFaqCategory(data: any) { return request.put('/faq/category', data) }
export function deleteFaqCategory(id: number) { return request.delete(`/faq/category/${id}`) }

// Knowledge
export function getFaqKnowledgeList() { return request.get('/faq/knowledge/list') }
export function getFaqKnowledgeByCategory(categoryId: number) { return request.get(`/faq/knowledge/byCategory/${categoryId}`) }
export function createFaqKnowledge(data: any) { return request.post('/faq/knowledge', data) }
export function updateFaqKnowledge(data: any) { return request.put('/faq/knowledge', data) }
export function deleteFaqKnowledge(id: number) { return request.delete(`/faq/knowledge/${id}`) }

// Import
export function downloadFaqTemplate() { return request.get('/faq/knowledge/template', { responseType: 'blob' }) }
export function importFaqKnowledge(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/faq/knowledge/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}
