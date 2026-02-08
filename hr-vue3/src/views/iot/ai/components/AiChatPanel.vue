<template>
  <div class="ai-chat-panel">
    <!-- Header -->
    <div class="chat-header">
      <div class="chat-header-left">
        <el-icon v-if="currentConversationId" class="back-btn" @click="currentConversationId = null">
          <ArrowLeft />
        </el-icon>
        <span class="chat-title">{{ currentConversationId ? '对话' : 'IoT AI 助手' }}</span>
      </div>
      <div class="chat-header-right">
        <el-button v-if="currentConversationId" text size="small" @click="handleNewConversation">
          新对话
        </el-button>
        <el-icon class="close-btn" @click="$emit('close')"><Close /></el-icon>
      </div>
    </div>

    <!-- Conversation List -->
    <div v-if="!currentConversationId" class="conversation-list">
      <div class="new-chat-btn" @click="handleNewConversation">
        <el-icon><Plus /></el-icon>
        <span>开始新对话</span>
      </div>
      <div
        v-for="conv in conversations"
        :key="conv.id"
        class="conversation-item"
        @click="loadConversation(conv.id)"
      >
        <div class="conv-title">{{ conv.title || '新对话' }}</div>
        <div class="conv-meta">
          <span>{{ conv.messageCount || 0 }} 条消息</span>
          <el-icon class="delete-btn" @click.stop="deleteConversation(conv.id)"><Delete /></el-icon>
        </div>
      </div>
      <div v-if="conversations.length === 0" class="empty-state">
        <p>暂无对话记录</p>
        <p>点击上方按钮开始新对话</p>
      </div>
    </div>

    <!-- Chat Messages -->
    <div v-else class="chat-messages" ref="messagesRef">
      <div v-if="messages.length === 0 && !isLoading" class="welcome-message">
        <div class="welcome-icon">🤖</div>
        <h3>IoT 能源管理助手</h3>
        <p>你可以问我：</p>
        <div class="suggestion-chips">
          <span class="chip" @click="sendMessage('帮我查看所有建筑的用电情况')">查看建筑用电情况</span>
          <span class="chip" @click="sendMessage('现在有哪些未处理的告警?')">查看告警信息</span>
          <span class="chip" @click="sendMessage('今天的能耗总览是什么样的?')">今日能耗总览</span>
        </div>
      </div>

      <div v-for="msg in messages" :key="msg.id" :class="['message', `message-${msg.role}`]">
        <div class="message-avatar">
          {{ msg.role === 'user' ? '👤' : '🤖' }}
        </div>
        <div class="message-content">
          <div v-html="renderMarkdown(msg.content)" class="message-text"></div>
        </div>
      </div>

      <!-- Streaming message -->
      <div v-if="streamingContent" class="message message-assistant">
        <div class="message-avatar">🤖</div>
        <div class="message-content">
          <div v-html="renderMarkdown(streamingContent)" class="message-text"></div>
          <span class="typing-cursor">|</span>
        </div>
      </div>

      <!-- Tool calling indicator -->
      <div v-if="toolStatus" class="tool-indicator">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>{{ toolStatus }}</span>
      </div>
    </div>

    <!-- Input Area -->
    <div v-if="currentConversationId !== null || messages.length === 0" class="chat-input">
      <el-input
        v-model="inputContent"
        type="textarea"
        :rows="2"
        placeholder="输入消息，按 Enter 发送..."
        :disabled="isLoading"
        @keydown.enter.exact.prevent="sendMessage()"
        resize="none"
      />
      <el-button
        type="primary"
        :icon="Promotion"
        :loading="isLoading"
        :disabled="!inputContent.trim()"
        @click="sendMessage()"
        class="send-btn"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { ArrowLeft, Close, Plus, Delete, Promotion, Loading } from '@element-plus/icons-vue'
import { IotAiApi, type AiConversationVO, type AiMessageVO } from '@/api/iot/ai'
import { ElMessage } from 'element-plus'

defineEmits(['close'])

const conversations = ref<AiConversationVO[]>([])
const currentConversationId = ref<number | null>(null)
const messages = ref<AiMessageVO[]>([])
const inputContent = ref('')
const isLoading = ref(false)
const streamingContent = ref('')
const toolStatus = ref('')
const messagesRef = ref<HTMLElement>()
let abortController: AbortController | null = null

onMounted(() => {
  loadConversations()
})

const loadConversations = async () => {
  try {
    conversations.value = await IotAiApi.getConversationList()
  } catch (e) {
    console.error('加载对话列表失败', e)
  }
}

const loadConversation = async (id: number) => {
  currentConversationId.value = id
  try {
    messages.value = await IotAiApi.getMessageList(id)
    await nextTick()
    scrollToBottom()
  } catch (e) {
    console.error('加载消息失败', e)
  }
}

const handleNewConversation = () => {
  currentConversationId.value = -1 // -1 means new, will be set by backend
  messages.value = []
  streamingContent.value = ''
}

const deleteConversation = async (id: number) => {
  try {
    await IotAiApi.deleteConversation(id)
    conversations.value = conversations.value.filter((c) => c.id !== id)
    if (currentConversationId.value === id) {
      currentConversationId.value = null
    }
    ElMessage.success('删除成功')
  } catch (e) {
    console.error('删除对话失败', e)
  }
}

const sendMessage = (prefilled?: string) => {
  const content = prefilled || inputContent.value.trim()
  if (!content || isLoading.value) return

  // If not in a conversation yet, start new
  if (currentConversationId.value === null) {
    currentConversationId.value = -1
  }

  // Add user message to UI
  messages.value.push({
    id: Date.now(),
    conversationId: currentConversationId.value!,
    role: 'user',
    content,
    createTime: new Date()
  })
  inputContent.value = ''
  isLoading.value = true
  streamingContent.value = ''
  toolStatus.value = ''

  nextTick(() => scrollToBottom())

  // SSE call
  abortController = new AbortController()
  const convId = currentConversationId.value === -1 ? null : currentConversationId.value

  IotAiApi.chatStream(
    convId,
    content,
    abortController,
    // onMessage
    (event: any) => {
      try {
        const data = JSON.parse(event.data)
        switch (data.type) {
          case 'conversation':
            currentConversationId.value = data.conversationId
            break
          case 'content':
            streamingContent.value += data.content
            nextTick(() => scrollToBottom())
            break
          case 'tool_call':
            const tools = data.tools || []
            toolStatus.value = `正在查询: ${tools.join(', ')}...`
            break
          case 'done':
            // Move streaming content to messages
            if (streamingContent.value) {
              messages.value.push({
                id: Date.now(),
                conversationId: data.conversationId,
                role: 'assistant',
                content: streamingContent.value,
                createTime: new Date()
              })
            }
            streamingContent.value = ''
            toolStatus.value = ''
            isLoading.value = false
            loadConversations() // refresh list
            break
          case 'error':
            ElMessage.error(data.message || 'AI 响应失败')
            isLoading.value = false
            streamingContent.value = ''
            toolStatus.value = ''
            break
        }
      } catch (e) {
        // ignore parse errors for non-JSON data
      }
    },
    // onError
    (err: any) => {
      console.error('SSE error', err)
      isLoading.value = false
      streamingContent.value = ''
      toolStatus.value = ''
    },
    // onClose
    () => {
      isLoading.value = false
    }
  )
}

const scrollToBottom = () => {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

const renderMarkdown = (content: string) => {
  if (!content) return ''
  // Simple markdown rendering: bold, code blocks, newlines
  return content
    .replace(/```(\w*)\n([\s\S]*?)```/g, '<pre><code>$2</code></pre>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br/>')
}
</script>

<style scoped lang="scss">
.ai-chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--el-bg-color);
  border-radius: 8px;
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-light);
  background: var(--el-color-primary);
  color: white;

  .chat-header-left {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .chat-title {
    font-size: 15px;
    font-weight: 600;
  }

  .back-btn,
  .close-btn {
    cursor: pointer;
    font-size: 18px;
    &:hover {
      opacity: 0.8;
    }
  }

  .chat-header-right {
    display: flex;
    align-items: center;
    gap: 4px;

    .el-button {
      color: white;
    }
  }
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;

  .new-chat-btn {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    margin-bottom: 8px;
    border: 1px dashed var(--el-border-color);
    border-radius: 8px;
    cursor: pointer;
    color: var(--el-color-primary);
    font-size: 14px;

    &:hover {
      background: var(--el-fill-color-light);
    }
  }

  .conversation-item {
    padding: 10px 12px;
    border-radius: 8px;
    cursor: pointer;
    margin-bottom: 4px;

    &:hover {
      background: var(--el-fill-color-light);
    }

    .conv-title {
      font-size: 14px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .conv-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 4px;
      font-size: 12px;
      color: var(--el-text-color-secondary);

      .delete-btn {
        cursor: pointer;
        opacity: 0;
        transition: opacity 0.2s;
        &:hover {
          color: var(--el-color-danger);
        }
      }
    }

    &:hover .delete-btn {
      opacity: 1;
    }
  }

  .empty-state {
    text-align: center;
    padding: 40px 0;
    color: var(--el-text-color-secondary);
    font-size: 13px;
  }
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  scroll-behavior: smooth;

  .welcome-message {
    text-align: center;
    padding: 30px 16px;

    .welcome-icon {
      font-size: 40px;
      margin-bottom: 12px;
    }

    h3 {
      margin: 0 0 8px;
      color: var(--el-text-color-primary);
    }

    p {
      color: var(--el-text-color-secondary);
      font-size: 13px;
    }

    .suggestion-chips {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      justify-content: center;
      margin-top: 16px;

      .chip {
        padding: 6px 14px;
        border: 1px solid var(--el-border-color);
        border-radius: 16px;
        cursor: pointer;
        font-size: 13px;
        color: var(--el-color-primary);
        transition: all 0.2s;

        &:hover {
          background: var(--el-color-primary-light-9);
          border-color: var(--el-color-primary);
        }
      }
    }
  }
}

.message {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;

  .message-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    flex-shrink: 0;
    background: var(--el-fill-color-light);
  }

  .message-content {
    max-width: 85%;

    .message-text {
      padding: 10px 14px;
      border-radius: 12px;
      font-size: 14px;
      line-height: 1.6;
      word-break: break-word;

      :deep(pre) {
        background: var(--el-fill-color-darker);
        padding: 10px;
        border-radius: 6px;
        overflow-x: auto;
        margin: 8px 0;
      }

      :deep(code) {
        background: var(--el-fill-color);
        padding: 2px 4px;
        border-radius: 3px;
        font-size: 13px;
      }

      :deep(pre code) {
        background: none;
        padding: 0;
      }
    }

    .typing-cursor {
      animation: blink 1s infinite;
      color: var(--el-color-primary);
    }
  }

  &.message-user {
    flex-direction: row-reverse;

    .message-text {
      background: var(--el-color-primary);
      color: white;
    }
  }

  &.message-assistant .message-text {
    background: var(--el-fill-color-light);
    color: var(--el-text-color-primary);
  }
}

.tool-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  margin-bottom: 12px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.chat-input {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid var(--el-border-color-light);
  background: var(--el-bg-color);

  .el-textarea {
    flex: 1;
  }

  .send-btn {
    align-self: flex-end;
    height: 40px;
    width: 40px;
  }
}

@keyframes blink {
  0%,
  50% {
    opacity: 1;
  }
  51%,
  100% {
    opacity: 0;
  }
}
</style>
