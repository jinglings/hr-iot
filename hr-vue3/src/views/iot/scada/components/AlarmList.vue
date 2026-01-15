<template>
  <div class="alarm-list">
    <!-- 头部 -->
    <div class="list-header">
      <h3 class="list-title">
        <Icon icon="ep:bell-filled" class="title-icon" />
        活动告警
        <el-badge :value="activeCount" :max="99" type="danger" class="count-badge" />
      </h3>
      <div class="header-actions">
        <el-button 
          v-if="activeCount > 0"
          type="primary" 
          size="small" 
          text 
          @click="acknowledgeAll"
        >
          全部确认
        </el-button>
      </div>
    </div>

    <!-- 筛选器 -->
    <div class="list-filters" v-if="showFilters">
      <el-select v-model="filterSeverity" placeholder="告警等级" size="small" clearable>
        <el-option label="紧急" value="critical" />
        <el-option label="高" value="high" />
        <el-option label="中" value="medium" />
        <el-option label="低" value="low" />
      </el-select>
      <el-select v-model="filterStatus" placeholder="状态" size="small" clearable>
        <el-option label="未确认" value="active" />
        <el-option label="已确认" value="acknowledged" />
      </el-select>
    </div>

    <!-- 告警列表 -->
    <div class="list-content" v-loading="loading">
      <template v-if="filteredAlarms.length > 0">
        <div 
          v-for="alarm in filteredAlarms" 
          :key="alarm.id"
          class="alarm-item"
          :class="[getSeverityClass(alarm.severity), { acknowledged: alarm.acknowledged }]"
          @click="selectAlarm(alarm)"
        >
          <div class="item-indicator">
            <div class="indicator-dot" :class="getSeverityClass(alarm.severity)">
              <div class="pulse" v-if="!alarm.acknowledged && alarm.severity === 'critical'" />
            </div>
          </div>
          <div class="item-content">
            <div class="item-header">
              <span class="item-title">{{ alarm.title }}</span>
              <el-tag :type="getTagType(alarm.severity)" size="small" effect="plain">
                {{ getSeverityText(alarm.severity) }}
              </el-tag>
            </div>
            <div class="item-message">{{ alarm.message }}</div>
            <div class="item-meta">
              <span class="meta-device">
                <Icon icon="ep:cpu" class="meta-icon" />
                {{ alarm.deviceName }}
              </span>
              <span class="meta-time">
                <Icon icon="ep:clock" class="meta-icon" />
                {{ formatTime(alarm.timestamp) }}
              </span>
            </div>
          </div>
          <div class="item-actions">
            <el-button 
              v-if="!alarm.acknowledged"
              type="primary" 
              size="small" 
              circle 
              @click.stop="acknowledgeAlarm(alarm)"
            >
              <Icon icon="ep:check" />
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 空状态 -->
      <div v-else class="empty-state">
        <Icon icon="ep:circle-check" class="empty-icon" />
        <div class="empty-text">暂无活动告警</div>
      </div>
    </div>

    <!-- 查看更多 -->
    <div class="list-footer" v-if="hasMore">
      <el-button type="primary" text @click="loadMore">
        查看更多 ({{ totalCount - alarms.length }})
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'AlarmList' })

// 告警接口
interface Alarm {
  id: number | string
  title: string
  message: string
  severity: 'critical' | 'high' | 'medium' | 'low'
  deviceId: number
  deviceName: string
  timestamp: Date | string
  acknowledged: boolean
  acknowledgedBy?: string
  acknowledgedAt?: Date | string
}

// Props
const props = withDefaults(defineProps<{
  /** 是否显示筛选器 */
  showFilters?: boolean
  /** 最大显示数量 */
  maxItems?: number
  /** 是否自动刷新 */
  autoRefresh?: boolean
  /** 刷新间隔 (ms) */
  refreshInterval?: number
}>(), {
  showFilters: true,
  maxItems: 10,
  autoRefresh: true,
  refreshInterval: 10000
})

// Emits
const emit = defineEmits<{
  (e: 'select', alarm: Alarm): void
  (e: 'acknowledge', alarm: Alarm): void
  (e: 'acknowledge-all'): void
}>()

// State
const loading = ref(false)
const alarms = ref<Alarm[]>([])
const totalCount = ref(0)
const filterSeverity = ref('')
const filterStatus = ref('')
let refreshTimer: ReturnType<typeof setInterval> | null = null

// 模拟数据
const mockAlarms: Alarm[] = [
  {
    id: 1,
    title: '压力过高',
    message: '出水压力超过上限值 0.8 MPa，当前值 0.95 MPa',
    severity: 'critical',
    deviceId: 11,
    deviceName: '出水压力传感器',
    timestamp: new Date(Date.now() - 5 * 60 * 1000),
    acknowledged: false
  },
  {
    id: 2,
    title: '水泵过载',
    message: '1号水泵电流过大，当前值 85A',
    severity: 'high',
    deviceId: 1,
    deviceName: 'P-001 水泵',
    timestamp: new Date(Date.now() - 15 * 60 * 1000),
    acknowledged: false
  },
  {
    id: 3,
    title: '温度偏高',
    message: '进水温度接近上限，当前值 32℃',
    severity: 'medium',
    deviceId: 20,
    deviceName: '进水温度传感器',
    timestamp: new Date(Date.now() - 30 * 60 * 1000),
    acknowledged: true,
    acknowledgedBy: 'admin',
    acknowledgedAt: new Date(Date.now() - 25 * 60 * 1000)
  },
  {
    id: 4,
    title: '通信中断',
    message: '与设备通信超时',
    severity: 'medium',
    deviceId: 42,
    deviceName: 'V-003 阀门',
    timestamp: new Date(Date.now() - 45 * 60 * 1000),
    acknowledged: false
  },
  {
    id: 5,
    title: '液位低',
    message: '进水箱液位低于 20%',
    severity: 'low',
    deviceId: 50,
    deviceName: 'TK-001 进水箱',
    timestamp: new Date(Date.now() - 60 * 60 * 1000),
    acknowledged: true
  }
]

// Computed
const filteredAlarms = computed(() => {
  let result = [...alarms.value]
  
  if (filterSeverity.value) {
    result = result.filter(a => a.severity === filterSeverity.value)
  }
  
  if (filterStatus.value === 'active') {
    result = result.filter(a => !a.acknowledged)
  } else if (filterStatus.value === 'acknowledged') {
    result = result.filter(a => a.acknowledged)
  }
  
  return result.slice(0, props.maxItems)
})

const activeCount = computed(() => {
  return alarms.value.filter(a => !a.acknowledged).length
})

const hasMore = computed(() => {
  return alarms.value.length < totalCount.value
})

// 方法
const getSeverityClass = (severity: string): string => `severity-${severity}`

const getSeverityText = (severity: string): string => {
  const texts: Record<string, string> = {
    critical: '紧急',
    high: '高',
    medium: '中',
    low: '低'
  }
  return texts[severity] || severity
}

const getTagType = (severity: string): 'danger' | 'warning' | 'info' => {
  if (severity === 'critical' || severity === 'high') return 'danger'
  if (severity === 'medium') return 'warning'
  return 'info'
}

const formatTime = (time: Date | string): string => {
  const d = new Date(time)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  return d.toLocaleTimeString('zh-CN')
}

const loadAlarms = async () => {
  loading.value = true
  try {
    // TODO: 替换为实际 API 调用
    await new Promise(resolve => setTimeout(resolve, 500))
    alarms.value = mockAlarms
    totalCount.value = mockAlarms.length
  } catch (error) {
    console.error('加载告警失败:', error)
  } finally {
    loading.value = false
  }
}

const selectAlarm = (alarm: Alarm) => {
  emit('select', alarm)
}

const acknowledgeAlarm = async (alarm: Alarm) => {
  try {
    // TODO: 调用 API 确认告警
    alarm.acknowledged = true
    alarm.acknowledgedAt = new Date()
    emit('acknowledge', alarm)
    ElMessage.success('告警已确认')
  } catch (error) {
    ElMessage.error('确认失败')
  }
}

const acknowledgeAll = async () => {
  try {
    alarms.value.forEach(a => {
      if (!a.acknowledged) {
        a.acknowledged = true
        a.acknowledgedAt = new Date()
      }
    })
    emit('acknowledge-all')
    ElMessage.success('已确认所有告警')
  } catch (error) {
    ElMessage.error('确认失败')
  }
}

const loadMore = () => {
  // TODO: 加载更多告警
}

// 生命周期
onMounted(() => {
  loadAlarms()
  
  if (props.autoRefresh) {
    refreshTimer = setInterval(loadAlarms, props.refreshInterval)
  }
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})

// 导出方法
defineExpose({
  refresh: loadAlarms
})
</script>

<style lang="scss" scoped>
$colors: (
  critical: #e53e3e,
  high: #f56c6c,
  medium: #e6a23c,
  low: #909399
);

.alarm-list {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #1a1a2e;
  border-radius: 8px;
  overflow: hidden;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border-bottom: 1px solid #2d4a6f;
  
  .list-title {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 0;
    font-size: 14px;
    font-weight: 500;
    color: #e4e7ed;
    
    .title-icon {
      color: #f56c6c;
    }
  }
  
  .count-badge {
    margin-left: 4px;
  }
}

.list-filters {
  display: flex;
  gap: 8px;
  padding: 10px 16px;
  background: rgba(255, 255, 255, 0.02);
  border-bottom: 1px solid #2d3748;
  
  .el-select {
    flex: 1;
  }
}

.list-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
  
  &::-webkit-scrollbar {
    width: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #4a5568;
    border-radius: 2px;
  }
}

.alarm-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  margin-bottom: 8px;
  background: #16213e;
  border-radius: 8px;
  border-left: 3px solid transparent;
  cursor: pointer;
  transition: all 0.3s ease;
  
  @each $name, $color in $colors {
    &.severity-#{$name} {
      border-left-color: $color;
      
      .indicator-dot {
        background: $color;
        box-shadow: 0 0 8px rgba($color, 0.5);
      }
    }
  }
  
  &:hover {
    background: #1e2d4d;
  }
  
  &.acknowledged {
    opacity: 0.7;
    
    .indicator-dot {
      opacity: 0.5;
    }
  }
  
  .item-indicator {
    display: flex;
    align-items: flex-start;
    padding-top: 4px;
    
    .indicator-dot {
      position: relative;
      width: 10px;
      height: 10px;
      border-radius: 50%;
      
      .pulse {
        position: absolute;
        top: 50%;
        left: 50%;
        width: 100%;
        height: 100%;
        border-radius: 50%;
        background: inherit;
        transform: translate(-50%, -50%);
        animation: pulse 1.5s ease-out infinite;
      }
    }
  }
  
  .item-content {
    flex: 1;
    min-width: 0;
    
    .item-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 4px;
      
      .item-title {
        font-size: 13px;
        font-weight: 500;
        color: #e4e7ed;
      }
    }
    
    .item-message {
      font-size: 12px;
      color: #a0aec0;
      line-height: 1.4;
      margin-bottom: 6px;
    }
    
    .item-meta {
      display: flex;
      gap: 12px;
      font-size: 11px;
      color: #718096;
      
      span {
        display: flex;
        align-items: center;
        gap: 4px;
      }
      
      .meta-icon {
        font-size: 10px;
      }
    }
  }
  
  .item-actions {
    display: flex;
    align-items: center;
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

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #718096;
  
  .empty-icon {
    font-size: 48px;
    color: #67c23a;
    margin-bottom: 12px;
  }
  
  .empty-text {
    font-size: 14px;
  }
}

.list-footer {
  padding: 10px;
  text-align: center;
  border-top: 1px solid #2d3748;
}
</style>
