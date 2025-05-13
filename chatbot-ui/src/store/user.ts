import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'

interface LoginForm {
  username: string
  password: string
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const username = ref<string | null>(localStorage.getItem('username'))

  const setAuth = (newToken: string, newUsername: string) => {
    token.value = newToken
    username.value = newUsername
    localStorage.setItem('token', newToken)
    localStorage.setItem('username', newUsername)
    
    // 设置 axios 默认请求头
    axios.defaults.headers.common['Authorization'] = `Bearer ${newToken}`
  }

  const clearAuth = () => {
    token.value = null
    username.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    delete axios.defaults.headers.common['Authorization']
  }

  const login = async (form: LoginForm) => {
    try {
      const response = await axios.post('/ai/auth/login', form)
      const { token: newToken, username: newUsername } = response.data
      setAuth(newToken, newUsername)
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(error.response?.data?.message || '登录失败')
      }
      throw error
    }
  }

  const logout = () => {
    clearAuth()
  }

  // 初始化时设置 axios 默认请求头
  if (token.value) {
    axios.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
  }

  return {
    token,
    username,
    login,
    logout,
    setAuth,
    clearAuth
  }
}) 