import request from '../utils/request'

// 1. 获取PPT列表
export function getPPTList() {
  return request({
    url: '/api/ppts',
    method: 'get'
  })
}

// 2. 上传PPT文件
export function uploadPPT(file, displayName) {
  const formData = new FormData()
  formData.append('file', file)
  if (displayName) {
    formData.append('displayName', displayName)
  }
  return request({
    url: '/api/ppts',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: formData
  })
}

// 3. 删除PPT文件
export function deletePPT(id) {
  return request({
    url: `/api/ppts/${id}`,
    method: 'delete'
  })
}

// 4. 获取声音样本列表
export function getAudioList() {
  return request({
    url: '/api/audios',
    method: 'get'
  })
}

// 5. 上传声音样本
export function uploadAudio(file, displayName) {
  const formData = new FormData()
  formData.append('file', file)
  if (displayName) {
    formData.append('displayName', displayName)
  }
  return request({
    url: '/api/audios',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: formData
  })
}

// 6. 修改声音样本名称
export function updateAudioName(id, name) {
  return request({
    url: `/api/audios/${id}/name`,
    method: 'patch',
    data: {
      name
    }
  })
}

// 7. 删除声音样本
export function deleteAudio(id) {
  return request({
    url: `/api/audios/${id}`,
    method: 'delete'
  })
}

// 8. 获取基础声音列表
export function getBaseVoices() {
  return request({
    url: '/api/base-voices',
    method: 'get'
  })
}
