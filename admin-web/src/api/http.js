import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({ baseURL: '', timeout: 20000 })

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use((response) => {
  const body = response.data
  if (body && typeof body.code === 'number' && body.code !== 200) {
    if (body.code === 401) {
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_profile')
      if (!location.pathname.endsWith('/login')) {
        location.href = '/login'
      }
    }
    ElMessage.error(body.message || '请求失败')
    return Promise.reject(body)
  }
  return body
}, (error) => {
  ElMessage.error(error.message || '网络错误')
  return Promise.reject(error)
})

export default http
