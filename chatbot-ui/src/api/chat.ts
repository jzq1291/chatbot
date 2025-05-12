import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8082/ai/chat', // 添加 /chat
  timeout: 300000, // 超时时间
  headers: {
    'Content-Type': 'application/json'
  }
})

export interface ChatRequest {
  message: string
  sessionId: string
  modelId?: string
}

export interface ChatResponse {
  message: string
  sessionId: string
  role: string
  modelId: string
}

export const chatApi = {
  sendMessage: async (request: ChatRequest): Promise<ChatResponse> => {
    const response = await api.post<ChatResponse>('', request) // 移除 /chat
    return response.data
  },

  getHistory: async (sessionId: string) => {
    const response = await api.get(`/history/${sessionId}`) // 移除 /chat
    return response.data
  },

  getAllSessions: async () => {
    const response = await api.get<string[]>('/sessions') // 移除 /chat
    return response.data
  },

  deleteSession: async (sessionId: string) => {
    const response = await api.delete(`/session/${sessionId}`) // 移除 /chat
    return response.data
  },

  getAvailableModels: async () => {
    const response = await api.get<string[]>('/models') // 移除 /chat
    return response.data
  }
} 