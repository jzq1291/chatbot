import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import { getActivePinia } from 'pinia'

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8082',
  timeout: 300000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 不需要认证的接口
const publicApis = ['/ai/auth/login', '/ai/auth/register']

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 如果是公开接口，不添加 token
    if (config.url && publicApis.includes(config.url)) {
      return config
    }

    const pinia = getActivePinia()
    if (pinia) {
      const authStore = useAuthStore(pinia)
      const token = authStore.token
      if (token) {
        config.headers['Authorization'] = `Bearer ${token}`
      }
    }
    return config
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    return response.data
  },
  (error) => {
    console.error('Response error:', error)
    const message = error.response?.data?.message || '请求失败'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

// 封装 GET 请求
const get = <T>(url: string, config?: AxiosRequestConfig): Promise<T> => {
  return service.get(url, config)
}

// 封装 POST 请求
const post = <T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> => {
  return service.post(url, data, config)
}

// 封装 PUT 请求
const put = <T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> => {
  return service.put(url, data, config)
}

// 封装 DELETE 请求
const del = <T>(url: string, config?: AxiosRequestConfig): Promise<T> => {
  return service.delete(url, config)
}

export default {
  get,
  post,
  put,
  delete: del
} 