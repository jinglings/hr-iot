<template>
  <div class="alarm-indicator" :class="alarmClass" @click="handleClick">
    <!-- 紧凑模式 -->
    <template v-if="mode === 'compact'">
      <el-tooltip :content="tooltipContent" placement="top">
        <div class="indicator-dot" :class="severityClass">
          <div class="pulse-ring" v-if="isActive && severity === 'critical'" />
        </div>
      </el-tooltip>
    </template>

    <!-- 标准模式 -->
    <template v-else-if="mode === 'standard'">
      <div class="indicator-standard" :class="severityClass">
        <Icon :icon="alarmIcon" class="indicator-icon" />
        <span class="indicator-text">{{ displayText }}</span>
      </div>
    </template>

    <!-- 卡片模式 -->
    <template v-else-if="mode === 'card'">
      <div class="indicator-card" :class="severityClass">
        <div class="card-header">
          <Icon :icon="alarmIcon" class="card-icon" />
          <span class="card-title">{{ title }}</span>
          <el-tag v-if="isActive" :type="tagType" size="small" effect="dark">
            {{ severityText }}
          </el-tag>
        </div>
        <div class="card-body">
          <div class="card-message">{{ message }}</div>
          <div class="card-meta" v-if="timestamp">
            <Icon icon="ep:clock" class="meta-icon" />
            <span>{{ formatTime(timestamp) }}</span>
          </div>
        </div>
        <div class="card-actions" v-if="showActions && isActive">
          <el-button size="small" type="primary" plain @click.stop="handleAcknowledge">
            确认
          </el-button>
          <el-button size="small" @click.stop="handleViewDetail">
            详情
          </el-button>
        </div>
      </div>
    </template>

    <!-- 内联模式 (用于设备标签) -->
    <template v-else-if="mode === 'inline'">
      <el-badge :value="count" :type="badgeType" :hidden="!isActive" class="inline-badge">
        <slot />
      </el-badge>
    </template>

    <!-- 边框模式 (给设备添加告警边框) -->
    <template v-else-if="mode === 'border'">
      <div class="indicator-border" :class="[severityClass, { active: isActive }]">
        <slot />
        <div class="border-indicator" v-if="isActive">
          <Icon :icon="alarmIcon" class="border-icon" />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

defineOptions({ name: 'AlarmIndicator' })

// 告警等级类型
type AlarmSeverity = 'critical' | 'high' | 'medium' | 'low' | 'info'

// Props
const props = withDefaults(defineProps<{
  /** 是否激活（有告警） */
  isActive?: boolean
  /** 告警等级 */
  severity?: AlarmSeverity
  /** 显示模式: compact | standard | card | inline | border */
  mode?: 'compact' | 'standard' | 'card' | 'inline' | 'border'
  /** 标题 */
  title?: string
  /** 告警消息 */
  message?: string
  /** 时间戳 */
  timestamp?: Date | string | null
  /** 告警数量 (inline模式) */
  count?: number
  /** 是否显示操作按钮 */
  showActions?: boolean
  /** 是否可点击 */
  clickable?: boolean
}>(), {
  isActive: false,
  severity: 'medium',
  mode: 'compact',
  title: '告警',
  message: '',
  timestamp: null,
  count: 0,
  showActions: true,
  clickable: true
})

// Emits
const emit = defineEmits<{
  (e: 'click'): void
  (e: 'acknowledge'): void
  (e: 'view-detail'): void
}>()

// 计算属性

// 告警类
const alarmClass = computed(() => ({
  active: props.isActive,
  clickable: props.clickable && props.isActive
}))

// 等级类
const severityClass = computed(() => `severity-${props.severity}`)

// 告警图标
const alarmIcon = computed(() => {
  const icons: Record<AlarmSeverity, string> = {
    critical: 'ep:warning-filled',
    high: 'ep:warning',
    medium: 'ep:bell-filled',
    low: 'ep:info-filled',
    info: 'ep:info-filled'
  }
  return icons[props.severity]
})

// 等级文本
const severityText = computed(() => {
  const texts: Record<AlarmSeverity, string> = {
    critical: '紧急',
    high: '高',
    medium: '中',
    low: '低',
    info: '信息'
  }
  return texts[props.severity]
})

// 标签类型
const tagType = computed(() => {
  const types: Record<AlarmSeverity, 'danger' | 'warning' | 'info' | 'success'> = {
    critical: 'danger',
    high: 'danger',
    medium: 'warning',
    low: 'info',
    info: 'info'
  }
  return types[props.severity]
})

// 徽章类型
const badgeType = computed(() => {
  return props.severity === 'critical' || props.severity === 'high' ? 'danger' : 'warning'
})

// 显示文本
const displayText = computed(() => {
  if (props.isActive) {
    return props.message || `${severityText.value}告警`
  }
  return '正常'
})

// 工具提示内容
const tooltipContent = computed(() => {
  if (!props.isActive) return '正常'
  return `${severityText.value}告警: ${props.message || '检查设备状态'}`
})

// 格式化时间
const formatTime = (time: Date | string | null): string => {
  if (!time) return '-'
  const d = new Date(time)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  return d.toLocaleString('zh-CN')
}

// 事件处理
const handleClick = () => {
  if (props.clickable && props.isActive) {
    emit('click')
  }
}

const handleAcknowledge = () => {
  emit('acknowledge')
}

const handleViewDetail = () => {
  emit('view-detail')
}
</script>

<style lang="scss" scoped>
// 颜色变量
$colors: (
  critical: #e53e3e,
  high: #f56c6c,
  medium: #e6a23c,
  low: #909399,
  info: #409eff
);

.alarm-indicator {
  &.clickable {
    cursor: pointer;
  }
}

// 紧凑模式 - 圆点
.indicator-dot {
  position: relative;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  transition: all 0.3s ease;
  
  @each $name, $color in $colors {
    &.severity-#{$name} {
      background-color: $color;
      box-shadow: 0 0 8px rgba($color, 0.5);
    }
  }
  
  // 默认正常状态
  &:not(.severity-critical):not(.severity-high):not(.severity-medium):not(.severity-low):not(.severity-info) {
    background-color: #67c23a;
  }
  
  .pulse-ring {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 100%;
    height: 100%;
    border-radius: 50%;
    background: inherit;
    transform: translate(-50%, -50%);
    animation: pulse-animation 1.5s ease-out infinite;
  }
}

@keyframes pulse-animation {
  0% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(2.5);
    opacity: 0;
  }
}

// 标准模式
.indicator-standard {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 13px;
  transition: all 0.3s ease;
  
  @each $name, $color in $colors {
    &.severity-#{$name} {
      background: rgba($color, 0.15);
      color: $color;
      border: 1px solid rgba($color, 0.3);
    }
  }
  
  .indicator-icon {
    font-size: 14px;
  }
}

// 卡片模式
.indicator-card {
  background: #1a1a2e;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
  
  @each $name, $color in $colors {
    &.severity-#{$name} {
      border-left: 4px solid $color;
      
      .card-icon {
        color: $color;
      }
      
      &.active {
        animation: card-blink 2s ease-in-out infinite;
      }
    }
  }
  
  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 12px;
    background: rgba(255, 255, 255, 0.03);
    
    .card-icon {
      font-size: 18px;
    }
    
    .card-title {
      flex: 1;
      font-size: 14px;
      font-weight: 500;
      color: #e4e7ed;
    }
  }
  
  .card-body {
    padding: 10px 12px;
    
    .card-message {
      font-size: 13px;
      color: #a0aec0;
      line-height: 1.5;
    }
    
    .card-meta {
      display: flex;
      align-items: center;
      gap: 4px;
      margin-top: 8px;
      font-size: 11px;
      color: #718096;
      
      .meta-icon {
        font-size: 12px;
      }
    }
  }
  
  .card-actions {
    display: flex;
    gap: 8px;
    padding: 8px 12px;
    background: rgba(255, 255, 255, 0.02);
    border-top: 1px solid #2d3748;
  }
}

@keyframes card-blink {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0);
  }
  50% {
    box-shadow: 0 0 12px 0 rgba(245, 108, 108, 0.3);
  }
}

// 内联模式
.inline-badge {
  :deep(.el-badge__content) {
    font-size: 10px;
  }
}

// 边框模式
.indicator-border {
  position: relative;
  border: 2px solid transparent;
  border-radius: 8px;
  transition: all 0.3s ease;
  
  &.active {
    @each $name, $color in $colors {
      &.severity-#{$name} {
        border-color: $color;
        box-shadow: 0 0 12px rgba($color, 0.3);
        
        &.severity-critical {
          animation: border-blink 1s ease-in-out infinite;
        }
      }
    }
  }
  
  .border-indicator {
    position: absolute;
    top: -10px;
    right: -10px;
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f56c6c;
    border-radius: 50%;
    color: #fff;
    font-size: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
    
    .border-icon {
      animation: shake 0.5s ease-in-out infinite;
    }
  }
}

@keyframes border-blink {
  0%, 100% {
    border-color: rgba(245, 108, 108, 0.3);
  }
  50% {
    border-color: rgba(245, 108, 108, 1);
  }
}

@keyframes shake {
  0%, 100% { transform: rotate(0); }
  25% { transform: rotate(-10deg); }
  75% { transform: rotate(10deg); }
}
</style>
