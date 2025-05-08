import { defineStore } from 'pinia'

export interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: number
}

export interface ChatSession {
  id: string
  title: string
  messages: Message[]
  lastUpdated: number
}

export const useChatStore = defineStore('chat', {
  state: () => ({
    sessions: [] as ChatSession[],
    currentSessionId: null as string | null,
  }),

  getters: {
    currentSession: (state) => 
      state.sessions.find(session => session.id === state.currentSessionId),
  },

  actions: {
    createNewSession(sessionId?: string) {
      const newSession: ChatSession = {
        id: sessionId || Date.now().toString(),
        title: 'New Chat',
        messages: [],
        lastUpdated: Date.now()
      }
      this.sessions.push(newSession)
      this.currentSessionId = newSession.id
      return newSession
    },

    addMessage(sessionId: string, message: Omit<Message, 'id' | 'timestamp'>) {
      const session = this.sessions.find(s => s.id === sessionId)
      if (session) {
        const newMessage: Message = {
          ...message,
          id: Date.now().toString(),
          timestamp: Date.now()
        }
        session.messages.push(newMessage)
        session.lastUpdated = Date.now()
      }
    },

    setCurrentSession(sessionId: string) {
      this.currentSessionId = sessionId
    },

    deleteSession(sessionId: string) {
      const index = this.sessions.findIndex(s => s.id === sessionId)
      if (index !== -1) {
        this.sessions.splice(index, 1)
        if (this.currentSessionId === sessionId) {
          this.currentSessionId = this.sessions[0]?.id || null
        }
      }
    }
  }
}) 