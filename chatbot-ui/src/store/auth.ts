import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'
import { authApi } from '@/api/auth'
import { useRouter } from 'vue-router'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const username = ref<string | null>(localStorage.getItem('username'))
  const router = useRouter()
  const loading = ref(false)

  const setToken = (newToken: string | null) => {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
      axios.defaults.headers.common['Authorization'] = `Bearer ${newToken}`
    } else {
      localStorage.removeItem('token')
      delete axios.defaults.headers.common['Authorization']
    }
  }

  const setUsername = (newUsername: string | null) => {
    username.value = newUsername
    if (newUsername) {
      localStorage.setItem('username', newUsername)
    } else {
      localStorage.removeItem('username')
    }
  }

  const clearToken = () => {
    setToken(null)
    setUsername(null)
  }

  // 初始化时设置 axios 默认请求头
  if (token.value) {
    axios.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
  }

  const login = async (username: string, password: string) => {
    loading.value = true
    try {
      const response = await authApi.login({ username, password })
      setToken(response.token)
      setUsername(response.username)
      return response
    } finally {
      loading.value = false
    }
  }

  const register = async (username: string, password: string) => {
    loading.value = true
    try {
      const response = await authApi.register({ username, password })
      setToken(response.token)
      setUsername(response.username)
      return response
    } finally {
      loading.value = false
    }
  }

  const logout = async () => {
    loading.value = true
    try {
      // 在清除 token 之前调用登出接口
      if (token.value) {
        await authApi.logout()
      }
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      // 无论登出接口是否成功，都清除本地 token
      clearToken()
      // 清除 token 后立即跳转到登录页
      await router.push('/login')
      loading.value = false
    }
  }

  return {
    token,
    username,
    loading,
    login,
    register,
    logout,
    clearToken
  }
}) 