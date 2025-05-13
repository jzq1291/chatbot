<template>
  <div class="chat-container">
    <!-- 左侧边栏：包含新建聊天按钮和聊天历史列表 -->
    <div class="sidebar">
      <el-button type="primary" @click="createNewChat" class="new-chat-btn">
        新建聊天
      </el-button>
      <div class="chat-history">
        <div
          v-for="sessionId in store.sessions"
          :key="sessionId"
          class="chat-session"
          :class="{ active: sessionId === store.currentSessionId }"
          @click="switchSession(sessionId)"
        >
          <span class="session-title">会话 {{ sessionId.slice(0, 8) }}</span>
          <el-button
            type="link"
            class="delete-btn"
            @click.stop="deleteSession(sessionId)"
          >
            删除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 右侧聊天区域 -->
    <div class="chat-main">
      <div class="model-selector" v-if="store.currentSessionId">
        <el-select
          v-model="store.selectedModel"
          placeholder="选择模型"
          @change="handleModelChange"
        >
          <el-option
            v-for="model in store.availableModels"
            :key="model"
            :label="model"
            :value="model"
          />
        </el-select>
      </div>
      <div class="messages" ref="messagesContainer">
        <div
          v-for="(message, index) in store.messages"
          :key="index"
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
          placeholder="请输入消息..."
          @keyup.enter.ctrl="sendMessage"
        />
        <div class="button-group">
          <el-button type="primary" @click="sendMessage" :loading="loading">
            发送
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useChatStore } from '@/store/chat'

// 初始化 store
const store = useChatStore()
// 创建响应式变量：输入消息
const inputMessage = ref('')
// 创建响应式变量：消息容器引用
const messagesContainer = ref<HTMLElement | null>(null)
// 创建响应式变量：加载状态
const loading = ref(false)

// 处理模型变更
const handleModelChange = (model: string) => {
  store.selectedModel = model
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }

  loading.value = true
  try {
    await store.sendMessage(inputMessage.value, store.selectedModel)
    inputMessage.value = ''
    await nextTick()
    scrollToBottom()
  } catch (error) {
    ElMessage.error('发送消息失败：' + (error as Error).message)
  } finally {
    loading.value = false
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 切换会话
const switchSession = async (sessionId: string) => {
  await store.switchSession(sessionId)
  await nextTick()
  scrollToBottom()
}

// 创建新会话
const createNewChat = () => {
  store.createNewChat()
}

// 删除会话
const deleteSession = async (sessionId: string) => {
  try {
    await store.deleteSession(sessionId)
    ElMessage.success('会话已删除')
  } catch (error) {
    ElMessage.error('删除会话失败：' + (error as Error).message)
  }
}

// 组件挂载时的初始化
onMounted(async () => {
  try {
    await Promise.all([
      store.loadSessions(),
      store.loadAvailableModels()
    ])
    if (store.sessions.length > 0) {
      await store.switchSession(store.sessions[0])
    } else {
      store.createNewChat()
    }
  } catch (error) {
    ElMessage.error('初始化失败：' + (error as Error).message)
  }
})
</script>

<style scoped>
.chat-container {
  height: 100%;
  display: flex;
  background-color: #f5f7fa;
}

.sidebar {
  width: 200px;
  background-color: #fff;
  border-right: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.new-chat-btn {
  margin-bottom: 20px;
}

.chat-history {
  flex: 1;
  overflow-y: auto;
}

.chat-session {
  padding: 10px;
  margin-bottom: 10px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f5f7fa;
}

.chat-session:hover {
  background-color: #ecf5ff;
}

.chat-session.active {
  background-color: #ecf5ff;
  border: 1px solid #409EFF;
}

.session-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.delete-btn {
  opacity: 0;
  transition: opacity 0.3s;
}

.chat-session:hover .delete-btn {
  opacity: 1;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.model-selector {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  margin-bottom: 20px;
}

.message {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
}

.message.user {
  align-items: flex-end;
}

.message.assistant {
  align-items: flex-start;
}

.message-content {
  max-width: 80%;
  padding: 10px 15px;
  border-radius: 4px;
  word-wrap: break-word;
}

.user .message-content {
  background-color: #409EFF;
  color: #fff;
}

.assistant .message-content {
  background-color: #f5f7fa;
  color: #303133;
}

.input-area {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
}

.button-group {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}
</style> 