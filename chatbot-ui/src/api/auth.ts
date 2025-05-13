import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8082/ai/auth',
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  }
})

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  username: string
}

export const authApi = {
  login: async (request: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/login', request)
    return response.data
  },

  register: async (request: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/register', request)
    return response.data
  }
} 