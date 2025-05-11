<template>
  <div class="chat-container">
    <!-- 左侧边栏：包含新建聊天按钮和聊天历史列表 -->
    <div class="sidebar">
      <!-- 新建聊天按钮区域 -->
      <div class="new-chat">
        <div class="logo">
          <span class="logo-text">AI Bot</span>
        </div>
        <el-button type="primary" class="new-chat-btn" @click="createNewChat">
          <el-icon><Plus /></el-icon>
          New Chat
        </el-button>
      </div>
      <!-- 聊天历史列表区域 -->
      <div class="chat-history">
        <!-- 遍历所有会话，为每个会话创建一个列表项 -->
        <div
          v-for="session in sessions"
          :key="session.id"
          class="chat-item"
          :class="{ active: session.id === currentSessionId }"
          @click="selectSession(session.id)"
        >
          <!-- 显示会话标题（使用会话ID的后6位） -->
          <div class="chat-title">Chat {{ session.id.slice(-6) }}</div>
          <!-- 删除会话按钮 -->
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

    <!-- 主聊天区域：包含消息列表和输入框 -->
    <div class="main-chat">
      <!-- 消息列表容器 -->
      <div class="messages" ref="messagesContainer">
        <!-- 遍历当前会话的所有消息 -->
        <div
          v-for="message in currentSession?.messages"
          :key="message.id"
          :class="['message', message.role]"
        >
          <!-- 显示消息内容 -->
          <div class="message-content">{{ message.content }}</div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <!-- 消息输入框 -->
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="3"
          placeholder="Type your message..."
          @keyup.enter="handleEnterKey"
        />
        <!-- 发送按钮 -->
        <el-button type="primary" @click="sendMessage" :loading="loading">
          Send
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 导入必要的 Vue 组合式 API
import { ref, onMounted, nextTick, computed } from 'vue'
// 导入 Element Plus 图标
import { Plus, Delete } from '@element-plus/icons-vue'
// 导入 Element Plus 消息提示组件
import { ElMessage } from 'element-plus'
// 导入状态管理 store
import { useChatStore } from '../store/chat'
// 导入 API 服务
import { chatApi } from '../api/chat'
// 导入消息类型定义
import type { Message } from '../store/chat'

// 初始化 store
const store = useChatStore()
// 创建响应式变量：输入消息
const inputMessage = ref('')
// 创建响应式变量：加载状态
const loading = ref(false)
// 创建消息容器的引用
const messagesContainer = ref<HTMLElement | null>(null)

// 计算属性：获取所有会话
const sessions = computed(() => store.sessions)
// 计算属性：获取当前会话ID
const currentSessionId = computed(() => store.currentSessionId)
// 计算属性：获取当前会话
const currentSession = computed(() => store.currentSession)

// 创建新会话的方法
const createNewChat = () => {
  store.createNewSession()
}

// 选择会话的方法
const selectSession = async (sessionId: string) => {
  // 设置当前会话
  store.setCurrentSession(sessionId)
  try {
    // 获取会话历史
    const history = await chatApi.getHistory(sessionId)
    // 找到当前会话
    const session = store.sessions.find(s => s.id === sessionId)
    if (session) {
      // 清空当前会话的消息
      session.messages = []
      // 将历史消息添加到会话中
      history.forEach((item: any) => {
        store.addMessage(sessionId, {
          role: item.role || 'assistant',
          content: item.message
        })
      })
    }
  } catch (error) {
    console.error('Error loading chat history:', error)
    ElMessage.error('Failed to load chat history')
  }
}

// 删除会话的方法
const deleteSession = async (sessionId: string) => {
  try {
    // 调用后端 API 删除会话
    await chatApi.deleteSession(sessionId)
    // 从 store 中删除会话
    store.deleteSession(sessionId)
    ElMessage.success('Session deleted successfully')
  } catch (error) {
    console.error('Error deleting session:', error)
    ElMessage.error('Failed to delete session')
  }
}

// 发送消息的方法
const sendMessage = async () => {
  // 如果消息为空或没有当前会话，则返回
  if (!inputMessage.value.trim() || !currentSessionId.value) return

  // 创建消息对象
  const message: Omit<Message, 'id' | 'timestamp'> = {
    role: 'user',
    content: inputMessage.value
  }

  // 添加消息到当前会话
  store.addMessage(currentSessionId.value, message)
  // 清空输入框
  inputMessage.value = ''
  // 滚动到底部
  await scrollToBottom()

  try {
    // 设置加载状态
    loading.value = true

    // 发送消息到后端
    const response = await chatApi.sendMessage({
      message: message.content,
      sessionId: currentSessionId.value
    })

    // 添加 AI 响应到会话
    store.addMessage(currentSessionId.value, {
      role: 'assistant',
      content: response.message
    })
    // 滚动到底部
    await scrollToBottom()
  } catch (error) {
    console.error('Error sending message:', error)
    ElMessage.error('Failed to send message')
  } finally {
    loading.value = false
  }
}

// 处理回车键的方法
const handleEnterKey = (event: KeyboardEvent) => {
  // 如果按下 Shift + Enter，则插入换行
  if (event.shiftKey) {
    return
  }
  // 否则发送消息
  event.preventDefault()
  sendMessage()
}

// 滚动到底部的方法
const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 组件挂载时的初始化
onMounted(async () => {
  try {
    // 获取所有会话
    const sessionIds = await chatApi.getAllSessions()
    if (sessionIds.length > 0) {
      // 加载所有会话
      for (const sessionId of sessionIds) {
        const session = store.sessions.find(s => s.id === sessionId)
        if (!session) {
          store.createNewSession(sessionId)
        }
      }
      // 选择第一个会话
      selectSession(sessionIds[0])
    } else {
      // 如果没有会话，创建新会话
      store.createNewSession()
    }
  } catch (error) {
    console.error('Error loading sessions:', error)
    ElMessage.error('Failed to load sessions')
    store.createNewSession()
  }
})
</script>

<style scoped>
/* 聊天容器样式 */
.chat-container {
  display: flex;
  height: 100vh;
  background-color: #f5f7fa;
}

/* 侧边栏样式 */
.sidebar {
  width: 260px;
  background-color: #fff;
  border-right: 1px solid #e4e7ed;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

/* 新建聊天按钮区域样式 */
.new-chat {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

/* Logo 样式 */
.logo {
  width: 100%;
  text-align: center;
  padding: 12px;
  background: linear-gradient(135deg, #a8edea, #fed6e3);
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.logo-text {
  font-size: 28px;
  font-weight: 600;
  background: linear-gradient(135deg, #6a11cb, #2575fc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 1px;
}

/* New Chat 按钮样式 */
.new-chat-btn {
  width: 100%;
  background: linear-gradient(135deg, #6a11cb, #2575fc);
  border: none;
  height: 40px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.new-chat-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(106, 17, 203, 0.2);
}

.new-chat-btn:active {
  transform: translateY(0);
}

/* 聊天历史列表区域样式 */
.chat-history {
  flex: 1;
  overflow-y: auto;
}

/* 聊天项样式 */
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

/* 聊天项悬停效果 */
.chat-item:hover {
  background-color: #f5f7fa;
}

/* 当前选中的聊天项样式 */
.chat-item.active {
  background-color: #ecf5ff;
}

/* 聊天标题样式 */
.chat-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 删除按钮样式 */
.delete-btn {
  opacity: 0;
  transition: opacity 0.3s;
}

/* 聊天项悬停时显示删除按钮 */
.chat-item:hover .delete-btn {
  opacity: 1;
}

/* 主聊天区域样式 */
.main-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

/* 消息列表容器样式 */
.messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 消息样式 */
.message {
  max-width: 80%;
  padding: 12px 16px;
  border-radius: 8px;
  word-wrap: break-word;
}

/* 用户消息样式 */
.message.user {
  align-self: flex-end;
  background-color: #409eff;
  color: white;
}

/* AI 助手消息样式 */
.message.assistant {
  align-self: flex-start;
  background-color: #fff;
  border: 1px solid #e4e7ed;
}

/* 输入区域样式 */
.input-area {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

/* 发送按钮样式 */
.input-area .el-button {
  align-self: flex-end;
}
</style> 