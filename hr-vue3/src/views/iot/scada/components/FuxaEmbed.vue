<template>
  <div class="fuxa-embed" :class="{ 'fullscreen': isFullscreen }">
    <!-- 工具栏 -->
    <div class="fuxa-toolbar" v-if="showToolbar">
      <div class="toolbar-left">
        <el-tag v-if="connectionStatus === 'connected'" type="success" size="small">
          <Icon icon="ep:circle-check" class="mr-1" />
          已连接
        </el-tag>
        <el-tag v-else-if="connectionStatus === 'connecting'" type="warning" size="small">
          <Icon icon="ep:loading" class="mr-1 spin" />
          连接中...
        </el-tag>
        <el-tag v-else type="danger" size="small">
          <Icon icon="ep:circle-close" class="mr-1" />
          已断开
        </el-tag>
        <span class="dashboard-name" v-if="dashboardName">{{ dashboardName }}</span>
      </div>
      <div class="toolbar-right">
        <el-button size="small" @click="refreshIframe" :loading="loading">
          <Icon icon="ep:refresh" class="mr-1" />
          刷新
        </el-button>
        <el-button size="small" @click="toggleFullscreen">
          <Icon :icon="isFullscreen ? 'ep:close' : 'ep:full-screen'" class="mr-1" />
          {{ isFullscreen ? '退出全屏' : '全屏' }}
        </el-button>
        <el-button size="small" type="primary" @click="openInNewWindow">
          <Icon icon="ep:position" class="mr-1" />
          新窗口
        </el-button>
      </div>
    </div>

    <!-- iframe 容器 -->
    <div class="iframe-container" v-loading="loading" element-loading-text="加载中...">
      <iframe
        ref="fuxaIframe"
        :src="iframeSrc"
        class="fuxa-iframe"
        :sandbox="sandboxAttributes"
        @load="onIframeLoad"
        @error="onIframeError"
        allow="fullscreen"
        frameborder="0"
      />
      
      <!-- 错误遮罩 -->
      <div class="error-overlay" v-if="hasError">
        <div class="error-content">
          <Icon icon="ep:warning" class="error-icon" />
          <h3>加载失败</h3>
          <p>{{ errorMessage }}</p>
          <el-button type="primary" @click="retryLoad">
            <Icon icon="ep:refresh" class="mr-1" />
            重试
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ScadaApi } from '@/api/iot/scada'
import { getAccessToken } from '@/utils/auth'

defineOptions({ name: 'FuxaEmbed' })

// Props
const props = withDefaults(defineProps<{
  /** 仪表板 ID */
  dashboardId?: string
  /** 仪表板名称 */
  dashboardName?: string
  /** 是否显示工具栏 */
  showToolbar?: boolean
  /** 基础 URL */
  baseUrl?: string
  /** 自定义高度 */
  height?: string
  /** 是否自动重连 */
  autoReconnect?: boolean
  /** 重连间隔 (毫秒) */
  reconnectInterval?: number
}>(), {
  showToolbar: true,
  baseUrl: '/fuxa',
  height: '100%',
  autoReconnect: true,
  reconnectInterval: 5000
})

// Emits
const emit = defineEmits<{
  (e: 'load'): void
  (e: 'error', error: Error): void
  (e: 'message', data: any): void
  (e: 'connected'): void
  (e: 'disconnected'): void
}>()

// Refs
const fuxaIframe = ref<HTMLIFrameElement | null>(null)
const loading = ref(true)
const hasError = ref(false)
const errorMessage = ref('')
const isFullscreen = ref(false)
const connectionStatus = ref<'connected' | 'connecting' | 'disconnected'>('connecting')
const retryCount = ref(0)
const maxRetries = 3

// 重连定时器
let reconnectTimer: ReturnType<typeof setTimeout> | null = null

// Sandbox 属性
const sandboxAttributes = computed(() => {
  return 'allow-scripts allow-same-origin allow-forms allow-popups allow-modals'
})

// 计算 iframe URL
const iframeSrc = computed(() => {
  if (!props.dashboardId) {
    return `${props.baseUrl}/`
  }
  
  // 构建带认证的 URL
  const token = getAccessToken()
  const params = new URLSearchParams()
  if (token) {
    params.set('token', token)
  }
  params.set('embed', 'true')
  params.set('t', Date.now().toString()) // 防止缓存
  
  return `${props.baseUrl}/view/${props.dashboardId}?${params.toString()}`
})

// iframe 加载完成
const onIframeLoad = () => {
  loading.value = false
  hasError.value = false
  errorMessage.value = ''
  retryCount.value = 0
  connectionStatus.value = 'connected'
  
  emit('load')
  emit('connected')
  
  // 设置 postMessage 监听
  setupMessageListener()
}

// iframe 加载错误
const onIframeError = (event: Event) => {
  loading.value = false
  hasError.value = true
  errorMessage.value = '无法连接到 FUXA 服务'
  connectionStatus.value = 'disconnected'
  
  emit('error', new Error(errorMessage.value))
  emit('disconnected')
  
  // 自动重连
  if (props.autoReconnect && retryCount.value < maxRetries) {
    scheduleReconnect()
  }
}

// 设置消息监听
const setupMessageListener = () => {
  // postMessage 通信处理
}

// 处理来自 FUXA 的消息
const handleFuxaMessage = (event: MessageEvent) => {
  // 验证消息来源
  if (!event.origin.includes(window.location.host) && 
      !event.origin.includes('localhost')) {
    return
  }
  
  try {
    const data = event.data
    
    // 处理不同类型的消息
    switch (data.type) {
      case 'fuxa:ready':
        connectionStatus.value = 'connected'
        emit('connected')
        break
      case 'fuxa:tagValue':
        emit('message', data)
        break
      case 'fuxa:control':
        handleControlCommand(data.payload)
        break
      case 'fuxa:error':
        ElMessage.error(data.message || 'FUXA 发生错误')
        break
      default:
        emit('message', data)
    }
  } catch (error) {
    console.error('[FuxaEmbed] 处理消息失败:', error)
  }
}

// 处理控制命令
const handleControlCommand = async (payload: any) => {
  try {
    const { deviceId, commandName, params } = payload
    await ScadaApi.sendControlCommand(deviceId, {
      commandName,
      params
    })
    ElMessage.success('命令已发送')
  } catch (error: any) {
    ElMessage.error(error.message || '命令发送失败')
  }
}

// 向 FUXA 发送消息
const sendMessage = (type: string, payload: any) => {
  if (fuxaIframe.value?.contentWindow) {
    fuxaIframe.value.contentWindow.postMessage({
      type,
      payload,
      timestamp: Date.now()
    }, '*')
  }
}

// 刷新 iframe
const refreshIframe = () => {
  if (fuxaIframe.value) {
    loading.value = true
    connectionStatus.value = 'connecting'
    fuxaIframe.value.src = iframeSrc.value
  }
}

// 重试加载
const retryLoad = () => {
  retryCount.value++
  refreshIframe()
}

// 调度重连
const scheduleReconnect = () => {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
  }
  
  reconnectTimer = setTimeout(() => {
    retryCount.value++
    refreshIframe()
  }, props.reconnectInterval)
}

// 切换全屏
const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
  
  if (isFullscreen.value) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
}

// 新窗口打开
const openInNewWindow = () => {
  window.open(iframeSrc.value, '_blank')
}

// 更新 Tag 值 (供外部调用)
const updateTagValue = (tagName: string, value: any) => {
  sendMessage('hr-iot:tagUpdate', { tagName, value })
}

// 导出方法
defineExpose({
  refresh: refreshIframe,
  sendMessage,
  updateTagValue
})

// 生命周期
onMounted(() => {
  window.addEventListener('message', handleFuxaMessage)
})

onUnmounted(() => {
  window.removeEventListener('message', handleFuxaMessage)
  
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
  }
  
  if (isFullscreen.value) {
    document.body.style.overflow = ''
  }
})

// 监听 dashboardId 变化
watch(() => props.dashboardId, () => {
  if (props.dashboardId) {
    loading.value = true
    connectionStatus.value = 'connecting'
  }
})
</script>

<style lang="scss" scoped>
.fuxa-embed {
  display: flex;
  flex-direction: column;
  height: v-bind(height);
  background: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
  
  &.fullscreen {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 9999;
    border-radius: 0;
    background: #000;
  }
}

.fuxa-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  
  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .dashboard-name {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
    }
  }
  
  .toolbar-right {
    display: flex;
    gap: 8px;
  }
}

.iframe-container {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.fuxa-iframe {
  width: 100%;
  height: 100%;
  border: none;
  background: #fff;
}

.error-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.95);
  
  .error-content {
    text-align: center;
    
    .error-icon {
      font-size: 64px;
      color: #f56c6c;
      margin-bottom: 16px;
    }
    
    h3 {
      margin: 0 0 8px;
      font-size: 18px;
      color: #303133;
    }
    
    p {
      margin: 0 0 20px;
      color: #909399;
    }
  }
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
