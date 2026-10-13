import request from '@/utils/request'

export function uploadImage(file: File, dir: string = 'common') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('dir', dir)
  return request.post('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
