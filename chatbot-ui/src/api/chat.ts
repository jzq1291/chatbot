import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8082/ai', // 后端 API 地址
  timeout: 300000, // 超时时间
  headers: {
    'Content-Type': 'application/json'
  }
})

export interface ChatRequest {
  message: string
  sessionId: string
}

export interface ChatResponse {
  message: string
  sessionId: string
  role: string
}

export const chatApi = {
  sendMessage: async (request: ChatRequest): Promise<ChatResponse> => {
    const response = await api.post<ChatResponse>('/chat', request)
    return response.data
  },

  getHistory: async (sessionId: string) => {
    const response = await api.get(`/chat/history/${sessionId}`)
    return response.data
  },

  getAllSessions: async () => {
    const response = await api.get<string[]>('/chat/sessions')
    return response.data
  },

  deleteSession: async (sessionId: string) => {
    const response = await api.delete(`/chat/session/${sessionId}`)
    return response.data
  }
} 