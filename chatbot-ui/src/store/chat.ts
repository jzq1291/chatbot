import { defineStore } from 'pinia'

// 定义消息接口
export interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: number
  modelId?: string
}

// 定义聊天会话接口
export interface ChatSession {
  id: string
  title: string
  messages: Message[]
  lastUpdated: number
  selectedModel: string
}

// 创建聊天 store
export const useChatStore = defineStore('chat', {
  // 定义状态
  state: () => ({
    sessions: [] as ChatSession[], // 所有会话列表
    currentSessionId: null as string | null, // 当前选中的会话ID
    availableModels: [] as string[], // 可用的模型列表
  }),

  // 定义 getter
  getters: {
    // 获取当前会话
    currentSession: (state) => 
      state.sessions.find(session => session.id === state.currentSessionId),
  },

  // 定义 actions
  actions: {
    // 创建新会话
    createNewSession(sessionId?: string) {
      const newSession: ChatSession = {
        id: sessionId || Date.now().toString(),
        title: 'New Chat',
        messages: [],
        lastUpdated: Date.now(),
        selectedModel: 'qwen3' // 默认使用 qwen3
      }
      this.sessions.push(newSession)
      this.currentSessionId = newSession.id
      return newSession
    },

    // 添加消息到会话
    addMessage(sessionId: string, message: Omit<Message, 'id' | 'timestamp'>) {
      const session = this.sessions.find(s => s.id === sessionId)
      if (session) {
        const newMessage: Message = {
          ...message,
          id: `${sessionId}-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
          timestamp: Date.now(),
          modelId: session.selectedModel
        }
        session.messages.push(newMessage)
        session.lastUpdated = Date.now()
      }
    },

    // 设置当前会话
    setCurrentSession(sessionId: string) {
      this.currentSessionId = sessionId
    },

    // 删除会话
    deleteSession(sessionId: string) {
      const index = this.sessions.findIndex(s => s.id === sessionId)
      if (index !== -1) {
        this.sessions.splice(index, 1)
        if (this.currentSessionId === sessionId) {
          this.currentSessionId = this.sessions[0]?.id || null
        }
      }
    },

    // 设置会话的模型
    setSessionModel(sessionId: string, modelId: string) {
      const session = this.sessions.find(s => s.id === sessionId)
      if (session) {
        session.selectedModel = modelId
      }
    },

    // 设置可用的模型列表
    setAvailableModels(models: string[]) {
      this.availableModels = models
    }
  }
}) 