<template>
  <div class="stat-card" :style="cardStyle">
    <!-- 图标 -->
    <div v-if="iconName" class="stat-icon" :style="iconStyle">
      <Icon :icon="iconName" :size="iconSize" />
    </div>

    <!-- 内容 -->
    <div class="stat-content">
      <!-- 标题 -->
      <div class="stat-title" :style="titleStyle">
        {{ title }}
      </div>

      <!-- 数值 -->
      <div class="stat-value" :style="valueStyle">
        <template v-if="animated">
          <span ref="valueRef">{{ displayValue }}</span>
        </template>
        <template v-else>
          {{ formattedValue }}
        </template>
        <span v-if="unit" class="stat-unit" :style="unitStyle">{{ unit }}</span>
      </div>

      <!-- 趋势 -->
      <div v-if="showTrend && trend" class="stat-trend" :style="trendStyle">
        <Icon
          :icon="trend.direction === 'up' ? 'ep:top' : 'ep:bottom'"
          :size="14"
        />
        <span>{{ trend.value }}{{ trend.unit || '%' }}</span>
        <span class="trend-label">{{ trend.label || '较昨日' }}</span>
      </div>

      <!-- 副标题/说明 -->
      <div v-if="description" class="stat-description" :style="descriptionStyle">
        {{ description }}
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import type { DashboardComponent } from '@/types/dashboard'
import { dataSourceManager } from '@/utils/dashboard/dataSource'

interface TrendData {
  direction: 'up' | 'down'
  value: number
  unit?: string
  label?: string
}

interface Props {
  component: DashboardComponent
  options?: any
  data?: any
}

const props = defineProps<Props>()

// 动态数据
const dynamicData = ref<any>(null)
const valueRef = ref<HTMLElement>()

// 配置项
const title = computed(() => props.component.options?.title || '统计指标')
const unit = computed(() => props.component.options?.unit || '')
const description = computed(() => props.component.options?.description || '')
const iconName = computed(() => props.component.options?.icon || '')
const iconSize = computed(() => props.component.options?.iconSize || 48)
const animated = computed(() => props.component.options?.animated !== false)
const showTrend = computed(() => props.component.options?.showTrend !== false)
const decimals = computed(() => props.component.options?.decimals ?? 0)

// 获取数据值
const rawValue = computed(() => {
  // 优先使用动态数据
  if (dynamicData.value !== null && dynamicData.value !== undefined) {
    if (typeof dynamicData.value === 'object') {
      return dynamicData.value.value ?? dynamicData.value.count ?? 0
    }
    return dynamicData.value
  }

  // 使用静态数据
  const staticData = props.data?.static || props.component.data?.static
  if (staticData !== null && staticData !== undefined) {
    if (typeof staticData === 'object') {
      return staticData.value ?? staticData.count ?? 0
    }
    return staticData
  }

  return 0
})

// 趋势数据
const trend = computed<TrendData | null>(() => {
  const data = dynamicData.value || props.component.data?.static
  if (data && typeof data === 'object' && data.trend) {
    return data.trend
  }
  return null
})

// 格式化后的值
const formattedValue = computed(() => {
  const value = Number(rawValue.value)
  if (isNaN(value)) return '0'

  // 千分位格式化
  if (props.component.options?.separator) {
    const parts = value.toFixed(decimals.value).split('.')
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    return parts.join('.')
  }

  return value.toFixed(decimals.value)
})

// 动画显示的值
const displayValue = ref('0')

// 数值动画
const animateValue = (start: number, end: number, duration: number = 1000) => {
  if (!animated.value || !valueRef.value) {
    displayValue.value = formattedValue.value
    return
  }

  const startTime = Date.now()
  const diff = end - start

  const step = () => {
    const now = Date.now()
    const progress = Math.min((now - startTime) / duration, 1)

    // 使用easeOutQuart缓动函数
    const easeProgress = 1 - Math.pow(1 - progress, 4)
    const current = start + diff * easeProgress

    // 格式化当前值
    if (props.component.options?.separator) {
      const parts = current.toFixed(decimals.value).split('.')
      parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',')
      displayValue.value = parts.join('.')
    } else {
      displayValue.value = current.toFixed(decimals.value)
    }

    if (progress < 1) {
      requestAnimationFrame(step)
    }
  }

  requestAnimationFrame(step)
}

// 监听数值变化
watch(
  rawValue,
  (newVal, oldVal) => {
    if (animated.value) {
      animateValue(Number(oldVal) || 0, Number(newVal) || 0)
    }
  },
  { immediate: true }
)

// 样式配置
const cardStyle = computed(() => ({
  display: 'flex',
  alignItems: 'center',
  gap: '16px',
  width: '100%',
  height: '100%',
  padding: '20px',
  backgroundColor: props.component.style?.backgroundColor || 'rgba(13, 25, 43, 0.8)',
  borderRadius: `${props.component.style?.borderRadius || 8}px`,
  border: `1px solid ${props.component.options?.borderColor || 'rgba(59, 130, 246, 0.3)'}`,
  boxShadow: props.component.options?.shadow || '0 4px 6px rgba(0, 0, 0, 0.1)'
}))

const iconStyle = computed(() => ({
  flexShrink: '0',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  width: `${iconSize.value + 16}px`,
  height: `${iconSize.value + 16}px`,
  borderRadius: '50%',
  backgroundColor: props.component.options?.iconBgColor || 'rgba(59, 130, 246, 0.2)',
  color: props.component.options?.iconColor || '#60a5fa'
}))

const titleStyle = computed(() => ({
  fontSize: props.component.options?.titleSize || '14px',
  color: props.component.options?.titleColor || '#9ca3af',
  marginBottom: '8px'
}))

const valueStyle = computed(() => ({
  fontSize: props.component.options?.valueSize || '32px',
  fontWeight: 'bold',
  color: props.component.options?.valueColor || '#fff',
  lineHeight: '1.2'
}))

const unitStyle = computed(() => ({
  fontSize: props.component.options?.unitSize || '16px',
  color: props.component.options?.unitColor || '#9ca3af',
  marginLeft: '4px'
}))

const trendStyle = computed(() => {
  const isUp = trend.value?.direction === 'up'
  return {
    display: 'flex',
    alignItems: 'center',
    gap: '4px',
    marginTop: '8px',
    fontSize: '12px',
    color: isUp
      ? props.component.options?.trendUpColor || '#10b981'
      : props.component.options?.trendDownColor || '#ef4444'
  }
})

const descriptionStyle = computed(() => ({
  marginTop: '8px',
  fontSize: '12px',
  color: props.component.options?.descriptionColor || '#6b7280'
}))

// 加载数据
const loadData = async () => {
  const dataConfig = props.component.data
  if (dataConfig.type === 'static') {
    dynamicData.value = null
    return
  }
  try {
    const data = await dataSourceManager.fetchData(dataConfig, props.component.id)
    dynamicData.value = data
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

// 启动数据轮询
const startDataPolling = () => {
  const dataConfig = props.component.data
  dataSourceManager.stopPolling(props.component.id)
  if (dataConfig.refresh && dataConfig.refresh > 0 && dataConfig.type !== 'static') {
    dataSourceManager.startPolling(dataConfig, props.component.id, (data) => {
      dynamicData.value = data
    })
  }
}

// 监听数据配置变化
watch(
  () => props.component.data,
  () => {
    loadData()
    startDataPolling()
  },
  { deep: true }
)

// 生命周期
onMounted(() => {
  loadData()
  startDataPolling()
})

onBeforeUnmount(() => {
  dataSourceManager.stopPolling(props.component.id)
  dataSourceManager.closeWebSocket(`ws_${props.component.id}`)
})
</script>

<style lang="scss" scoped>
.stat-card {
  .stat-content {
    flex: 1;
    min-width: 0;
  }

  .stat-title {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .stat-value {
    display: flex;
    align-items: baseline;
  }

  .stat-trend {
    .trend-label {
      margin-left: 4px;
      opacity: 0.7;
    }
  }

  .stat-description {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}
</style>
