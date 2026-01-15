<template>
  <div class="connection-status" :class="statusClass">
    <!-- 简洁模式 -->
    <el-tooltip
      v-if="mode === 'compact'"
      :content="tooltipContent"
      placement="bottom"
    >
      <div class="status-dot-container" @click="handleClick">
        <div class="status-dot" :class="statusClass">
          <div class="pulse" v-if="status === 'connecting'" />
        </div>
      </div>
    </el-tooltip>

    <!-- 标准模式 -->
    <div v-else-if="mode === 'standard'" class="status-standard" @click="handleClick">
      <el-tooltip :content="tooltipContent" placement="bottom">
        <div class="status-indicator">
          <div class="status-dot" :class="statusClass">
            <div class="pulse" v-if="status === 'connecting'" />
          </div>
          <span class="status-text">{{ statusText }}</span>
        </div>
      </el-tooltip>
    </div>

    <!-- 详细模式 -->
    <div v-else class="status-detailed">
      <div class="status-header">
        <div class="status-dot" :class="statusClass">
          <div class="pulse" v-if="status === 'connecting'" />
        </div>
        <span class="status-text">{{ statusText }}</span>
      </div>
      
      <div class="status-details">
        <div class="detail-item" v-if="lastUpdateTime">
          <Icon icon="ep:clock" class="detail-icon" />
          <span>最后更新: {{ formatTime(lastUpdateTime) }}</span>
        </div>
        <div class="detail-item" v-if="latency !== null">
          <Icon icon="ep:timer" class="detail-icon" />
          <span>延迟: {{ latency }}ms</span>
        </div>
        <div class="detail-item" v-if="reconnectCount > 0">
          <Icon icon="ep:refresh" class="detail-icon" />
          <span>重连次数: {{ reconnectCount }}</span>
        </div>
      </div>
      
      <el-button
        v-if="showReconnect && status === 'disconnected'"
        type="primary"
        size="small"
        @click="handleReconnect"
        :loading="reconnecting"
        class="reconnect-btn"
      >
        <Icon icon="ep:refresh" class="mr-1" />
        重新连接
      </el-button>
    </div>

    <!-- 悬浮菜单（紧凑模式和标准模式） -->
    <el-popover
      v-if="mode !== 'detailed' && showPopover"
      :visible="popoverVisible"
      placement="bottom"
      :width="220"
      trigger="click"
    >
      <template #reference>
        <span />
      </template>
      <div class="status-popover">
        <div class="popover-header">
          <span class="popover-title">连接状态</span>
          <el-tag :type="statusTagType" size="small">{{ statusText }}</el-tag>
        </div>
        <el-divider class="popover-divider" />
        <div class="popover-content">
          <div class="popover-item" v-if="lastUpdateTime">
            <span class="item-label">最后更新</span>
            <span class="item-value">{{ formatTime(lastUpdateTime) }}</span>
          </div>
          <div class="popover-item" v-if="latency !== null">
            <span class="item-label">延迟</span>
            <span class="item-value">{{ latency }}ms</span>
          </div>
          <div class="popover-item" v-if="reconnectCount > 0">
            <span class="item-label">重连次数</span>
            <span class="item-value">{{ reconnectCount }}</span>
          </div>
        </div>
        <el-button
          v-if="status === 'disconnected'"
          type="primary"
          size="small"
          @click="handleReconnect"
          :loading="reconnecting"
          class="popover-btn"
        >
          <Icon icon="ep:refresh" class="mr-1" />
          重新连接
        </el-button>
      </div>
    </el-popover>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'ConnectionStatus' })

// 状态类型
type ConnectionStatusType = 'connected' | 'connecting' | 'disconnected'

// Props
const props = withDefaults(defineProps<{
  /** 连接状态 */
  status?: ConnectionStatusType
  /** 显示模式: compact | standard | detailed */
  mode?: 'compact' | 'standard' | 'detailed'
  /** 最后更新时间 */
  lastUpdateTime?: Date | null
  /** 延迟 (毫秒) */
  latency?: number | null
  /** 重连次数 */
  reconnectCount?: number
  /** 是否显示重连按钮 */
  showReconnect?: boolean
  /** 是否显示弹出菜单 */
  showPopover?: boolean
  /** 自定义已连接文本 */
  connectedText?: string
  /** 自定义连接中文本 */
  connectingText?: string
  /** 自定义已断开文本 */
  disconnectedText?: string
}>(), {
  status: 'disconnected',
  mode: 'standard',
  lastUpdateTime: null,
  latency: null,
  reconnectCount: 0,
  showReconnect: true,
  showPopover: true,
  connectedText: '已连接',
  connectingText: '连接中...',
  disconnectedText: '已断开'
})

// Emits
const emit = defineEmits<{
  (e: 'reconnect'): void
  (e: 'click'): void
}>()

// State
const reconnecting = ref(false)
const popoverVisible = ref(false)

// 状态类
const statusClass = computed(() => {
  return {
    connected: props.status === 'connected',
    connecting: props.status === 'connecting',
    disconnected: props.status === 'disconnected'
  }
})

// 状态文本
const statusText = computed(() => {
  switch (props.status) {
    case 'connected': return props.connectedText
    case 'connecting': return props.connectingText
    case 'disconnected': return props.disconnectedText
    default: return props.disconnectedText
  }
})

// 工具提示内容
const tooltipContent = computed(() => {
  let content = statusText.value
  if (props.latency !== null) {
    content += ` (${props.latency}ms)`
  }
  if (props.lastUpdateTime) {
    content += ` - 更新于 ${formatTime(props.lastUpdateTime)}`
  }
  return content
})

// 状态标签类型
const statusTagType = computed(() => {
  switch (props.status) {
    case 'connected': return 'success'
    case 'connecting': return 'warning'
    case 'disconnected': return 'danger'
    default: return 'info'
  }
})

// 格式化时间
const formatTime = (date: Date | null): string => {
  if (!date) return '-'
  const d = new Date(date)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  
  if (diff < 60000) {
    return '刚刚'
  } else if (diff < 3600000) {
    return `${Math.floor(diff / 60000)} 分钟前`
  } else {
    return d.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  }
}

// 处理点击
const handleClick = () => {
  if (props.mode !== 'detailed' && props.showPopover) {
    popoverVisible.value = !popoverVisible.value
  }
  emit('click')
}

// 处理重连
const handleReconnect = async () => {
  reconnecting.value = true
  try {
    emit('reconnect')
    // 等待一小段时间以显示加载状态
    await new Promise(resolve => setTimeout(resolve, 1000))
  } finally {
    reconnecting.value = false
    popoverVisible.value = false
  }
}

// 监听状态变化
watch(() => props.status, (newStatus, oldStatus) => {
  if (oldStatus === 'disconnected' && newStatus === 'connected') {
    ElMessage.success('连接已恢复')
  } else if (oldStatus === 'connected' && newStatus === 'disconnected') {
    ElMessage.warning('连接已断开')
  }
})
</script>

<style lang="scss" scoped>
.connection-status {
  display: inline-flex;
  align-items: center;
}

.status-dot-container {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 4px;
}

.status-dot {
  position: relative;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  transition: background-color 0.3s ease;
  
  &.connected {
    background-color: #67c23a;
    box-shadow: 0 0 8px rgba(103, 194, 58, 0.5);
  }
  
  &.connecting {
    background-color: #e6a23c;
    box-shadow: 0 0 8px rgba(230, 162, 60, 0.5);
  }
  
  &.disconnected {
    background-color: #f56c6c;
    box-shadow: 0 0 8px rgba(245, 108, 108, 0.5);
  }
  
  .pulse {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 100%;
    height: 100%;
    border-radius: 50%;
    background-color: inherit;
    transform: translate(-50%, -50%);
    animation: pulse 1.5s ease-out infinite;
  }
}

@keyframes pulse {
  0% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(2.5);
    opacity: 0;
  }
}

.status-standard {
  cursor: pointer;
  
  .status-indicator {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 4px 12px;
    background: #f5f7fa;
    border-radius: 16px;
    transition: background-color 0.3s ease;
    
    &:hover {
      background: #e4e7ed;
    }
  }
  
  .status-text {
    font-size: 13px;
    color: #606266;
  }
}

.status-detailed {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 12px;
  min-width: 200px;
  
  .status-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    
    .status-text {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
    }
  }
  
  .status-details {
    .detail-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      color: #909399;
      margin-bottom: 6px;
      
      .detail-icon {
        font-size: 14px;
      }
    }
  }
  
  .reconnect-btn {
    width: 100%;
    margin-top: 12px;
  }
}

.status-popover {
  .popover-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .popover-title {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
    }
  }
  
  .popover-divider {
    margin: 12px 0;
  }
  
  .popover-content {
    .popover-item {
      display: flex;
      justify-content: space-between;
      font-size: 13px;
      margin-bottom: 8px;
      
      .item-label {
        color: #909399;
      }
      
      .item-value {
        color: #303133;
      }
    }
  }
  
  .popover-btn {
    width: 100%;
    margin-top: 12px;
  }
}
</style>
