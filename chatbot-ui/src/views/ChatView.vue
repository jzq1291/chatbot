<template>
  <div class="chat-container">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <div class="new-chat">
        <el-button type="primary" @click="createNewChat">
          <el-icon><Plus /></el-icon>
          New Chat
        </el-button>
      </div>
      <div class="chat-history">
        <div
          v-for="session in sessions"
          :key="session.id"
          class="chat-item"
          :class="{ active: session.id === currentSessionId }"
          @click="selectSession(session.id)"
        >
          <div class="chat-title">{{ session.title }}</div>
          <el-button
            class="delete-btn"
            type="danger"
            size="small"
            @click.stop="deleteSession(session.id)"
          >
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 主聊天区域 -->
    <div class="main-chat">
      <div class="messages" ref="messagesContainer">
        <div
          v-for="message in currentSession?.messages"
          :key="message.id"
          :class="['message', message.role]"
        >
          <div class="message-content">{{ message.content }}</div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="3"
          placeholder="Type your message..."
          @keyup.enter.ctrl="sendMessage"
        />
        <el-button type="primary" @click="sendMessage" :loading="loading">
          Send
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { Plus, Delete } from '@element-plus/icons-vue'
import { useChatStore } from '../store/chat'
import { chatApi } from '../api/chat'
import type { Message } from '../store/chat'

const store = useChatStore()
const inputMessage = ref('')
const loading = ref(false)
const messagesContainer = ref<HTMLElement | null>(null)

const sessions = computed(() => store.sessions)
const currentSessionId = computed(() => store.currentSessionId)
const currentSession = computed(() => store.currentSession)

// 创建新会话
const createNewChat = () => {
  store.createNewSession()
}

// 选择会话
const selectSession = (sessionId: string) => {
  store.setCurrentSession(sessionId)
}

// 删除会话
const deleteSession = (sessionId: string) => {
  store.deleteSession(sessionId)
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || !currentSessionId.value) return

  const message: Omit<Message, 'id' | 'timestamp'> = {
    role: 'user',
    content: inputMessage.value
  }

  store.addMessage(currentSessionId.value, message)
  inputMessage.value = ''
  await scrollToBottom()

  try {
    loading.value = true
    const response = await chatApi.sendMessage({
      message: message.content,
      sessionId: currentSessionId.value
    })

    store.addMessage(currentSessionId.value, {
      role: 'assistant',
      content: response.message
    })
    await scrollToBottom()
  } catch (error) {
    console.error('Error sending message:', error)
    ElMessage.error('Failed to send message')
  } finally {
    loading.value = false
  }
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 初始化
onMounted(() => {
  if (sessions.value.length === 0) {
    createNewChat()
  }
})
</script>

<style scoped>
.chat-container {
  display: flex;
  height: 100vh;
  background-color: #f5f7fa;
}

.sidebar {
  width: 260px;
  background-color: #fff;
  border-right: 1px solid #e4e7ed;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.new-chat {
  margin-bottom: 20px;
}

.chat-history {
  flex: 1;
  overflow-y: auto;
}

.chat-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px;
  margin-bottom: 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.chat-item:hover {
  background-color: #f5f7fa;
}

.chat-item.active {
  background-color: #ecf5ff;
}

.chat-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.delete-btn {
  opacity: 0;
  transition: opacity 0.3s;
}

.chat-item:hover .delete-btn {
  opacity: 1;
}

.main-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.message {
  max-width: 80%;
  padding: 12px 16px;
  border-radius: 8px;
  word-wrap: break-word;
}

.message.user {
  align-self: flex-end;
  background-color: #409eff;
  color: white;
}

.message.assistant {
  align-self: flex-start;
  background-color: #fff;
  border: 1px solid #e4e7ed;
}

.input-area {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.input-area .el-button {
  align-self: flex-end;
}
</style> 