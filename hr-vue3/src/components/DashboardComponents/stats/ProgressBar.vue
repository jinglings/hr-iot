<template>
  <div class="progress-bar-container" :style="containerStyle">
    <!-- 标题 -->
    <div v-if="title" class="progress-title" :style="titleStyle">
      {{ title }}
    </div>

    <!-- 进度条主体 -->
    <div class="progress-wrapper">
      <!-- 线性进度条 -->
      <template v-if="type === 'line'">
        <div class="progress-line" :style="lineStyle">
          <div class="progress-track" :style="trackStyle">
            <div
              class="progress-bar"
              :style="barStyle"
              :class="{ 'progress-animated': animated }"
            >
              <!-- 条纹动画 -->
              <div v-if="striped" class="progress-stripes"></div>
            </div>
          </div>
          <div v-if="showText" class="progress-text" :style="textStyle">
            {{ displayText }}
          </div>
        </div>
      </template>

      <!-- 圆形进度条 -->
      <template v-else-if="type === 'circle'">
        <div class="progress-circle" :style="circleContainerStyle">
          <svg :width="circleSize" :height="circleSize">
            <!-- 背景圆 -->
            <circle
              :cx="circleSize / 2"
              :cy="circleSize / 2"
              :r="radius"
              :stroke="trackColor"
              :stroke-width="strokeWidth"
              fill="none"
            />
            <!-- 进度圆 -->
            <circle
              :cx="circleSize / 2"
              :cy="circleSize / 2"
              :r="radius"
              :stroke="barColor"
              :stroke-width="strokeWidth"
              :stroke-dasharray="circumference"
              :stroke-dashoffset="dashOffset"
              :class="{ 'circle-animated': animated }"
              fill="none"
              stroke-linecap="round"
              transform-origin="center"
              :transform="`rotate(-90 ${circleSize / 2} ${circleSize / 2})`"
            />
          </svg>
          <!-- 中心文字 -->
          <div class="progress-circle-text" :style="circleTextStyle">
            {{ displayText }}
          </div>
        </div>
      </template>

      <!-- 仪表盘进度条 -->
      <template v-else-if="type === 'dashboard'">
        <div class="progress-dashboard" :style="circleContainerStyle">
          <svg :width="circleSize" :height="circleSize * 0.75">
            <!-- 背景弧 -->
            <path
              :d="dashboardPath"
              :stroke="trackColor"
              :stroke-width="strokeWidth"
              fill="none"
              stroke-linecap="round"
            />
            <!-- 进度弧 -->
            <path
              :d="dashboardPath"
              :stroke="barColor"
              :stroke-width="strokeWidth"
              :stroke-dasharray="dashboardLength"
              :stroke-dashoffset="dashboardOffset"
              :class="{ 'circle-animated': animated }"
              fill="none"
              stroke-linecap="round"
            />
          </svg>
          <!-- 中心文字 -->
          <div class="progress-dashboard-text" :style="circleTextStyle">
            {{ displayText }}
          </div>
        </div>
      </template>
    </div>

    <!-- 描述 -->
    <div v-if="description" class="progress-description" :style="descriptionStyle">
      {{ description }}
    </div>
  </div>
</template>

<script lang="ts" setup>
import type { DashboardComponent } from '@/types/dashboard'
import { dataSourceManager } from '@/utils/dashboard/dataSource'

interface Props {
  component: DashboardComponent
  options?: any
  data?: any
}

const props = defineProps<Props>()

// 动态数据
const dynamicData = ref<any>(null)

// 配置项
const type = computed(() => props.component.options?.type || 'line') // line, circle, dashboard
const title = computed(() => props.component.options?.title || '')
const description = computed(() => props.component.options?.description || '')
const showText = computed(() => props.component.options?.showText !== false)
const animated = computed(() => props.component.options?.animated !== false)
const striped = computed(() => props.component.options?.striped === true)
const format = computed(() => props.component.options?.format || 'percent') // percent, value
const circleSize = computed(() => props.component.options?.circleSize || 120)
const strokeWidth = computed(() => props.component.options?.strokeWidth || 8)

// 获取进度值
const rawValue = computed(() => {
  // 优先使用动态数据
  if (dynamicData.value !== null && dynamicData.value !== undefined) {
    if (typeof dynamicData.value === 'object') {
      return Number(dynamicData.value.value ?? dynamicData.value.percent ?? 0)
    }
    return Number(dynamicData.value)
  }

  // 使用静态数据
  const staticData = props.data?.static || props.component.data?.static
  if (staticData !== null && staticData !== undefined) {
    if (typeof staticData === 'object') {
      return Number(staticData.value ?? staticData.percent ?? 0)
    }
    return Number(staticData)
  }

  return 0
})

// 最大值
const maxValue = computed(() => {
  const data = dynamicData.value || props.component.data?.static
  if (data && typeof data === 'object' && data.max) {
    return Number(data.max)
  }
  return props.component.options?.max || 100
})

// 百分比
const percentage = computed(() => {
  const value = rawValue.value
  const max = maxValue.value
  return Math.min(Math.max((value / max) * 100, 0), 100)
})

// 显示文本
const displayText = computed(() => {
  if (format.value === 'percent') {
    return `${percentage.value.toFixed(0)}%`
  } else {
    return `${rawValue.value}/${maxValue.value}`
  }
})

// 进度条颜色
const barColor = computed(() => {
  const colors = props.component.options?.colors
  if (colors && Array.isArray(colors)) {
    // 根据百分比选择颜色
    for (const item of colors) {
      if (percentage.value <= item.percentage) {
        return item.color
      }
    }
    return colors[colors.length - 1].color
  }
  return props.component.options?.barColor || '#3b82f6'
})

const trackColor = computed(
  () => props.component.options?.trackColor || 'rgba(59, 130, 246, 0.1)'
)

// 样式
const containerStyle = computed(() => ({
  width: '100%',
  height: '100%',
  padding: '16px',
  backgroundColor: props.component.style?.backgroundColor || 'transparent',
  borderRadius: `${props.component.style?.borderRadius || 4}px`,
  display: 'flex',
  flexDirection: 'column',
  gap: '12px'
}))

const titleStyle = computed(() => ({
  fontSize: props.component.options?.titleSize || '14px',
  color: props.component.options?.titleColor || '#9ca3af',
  fontWeight: '500'
}))

const lineStyle = computed(() => ({
  display: 'flex',
  alignItems: 'center',
  gap: '12px'
}))

const trackStyle = computed(() => ({
  flex: '1',
  height: `${props.component.options?.height || 20}px`,
  backgroundColor: trackColor.value,
  borderRadius: `${props.component.options?.borderRadius || 10}px`,
  overflow: 'hidden',
  position: 'relative'
}))

const barStyle = computed(() => ({
  width: `${percentage.value}%`,
  height: '100%',
  backgroundColor: barColor.value,
  borderRadius: `${props.component.options?.borderRadius || 10}px`,
  transition: animated.value ? 'width 0.6s ease' : 'none',
  position: 'relative',
  overflow: 'hidden'
}))

const textStyle = computed(() => ({
  fontSize: props.component.options?.textSize || '14px',
  color: props.component.options?.textColor || '#fff',
  fontWeight: 'bold',
  minWidth: '50px',
  textAlign: 'right'
}))

const descriptionStyle = computed(() => ({
  fontSize: '12px',
  color: props.component.options?.descriptionColor || '#6b7280'
}))

// 圆形进度条计算
const radius = computed(() => (circleSize.value - strokeWidth.value) / 2)
const circumference = computed(() => 2 * Math.PI * radius.value)
const dashOffset = computed(
  () => circumference.value - (percentage.value / 100) * circumference.value
)

const circleContainerStyle = computed(() => ({
  position: 'relative',
  width: `${circleSize.value}px`,
  height: type.value === 'dashboard' ? `${circleSize.value * 0.75}px` : `${circleSize.value}px`,
  margin: '0 auto'
}))

const circleTextStyle = computed(() => ({
  position: 'absolute',
  top: '50%',
  left: '50%',
  transform: 'translate(-50%, -50%)',
  fontSize: props.component.options?.textSize || '20px',
  color: props.component.options?.textColor || '#fff',
  fontWeight: 'bold'
}))

// 仪表盘路径计算
const dashboardPath = computed(() => {
  const cx = circleSize.value / 2
  const cy = circleSize.value / 2
  const r = radius.value
  const startAngle = (225 * Math.PI) / 180 // -135度
  const endAngle = (315 * Math.PI) / 180 // 135度

  const x1 = cx + r * Math.cos(startAngle)
  const y1 = cy + r * Math.sin(startAngle)
  const x2 = cx + r * Math.cos(endAngle)
  const y2 = cy + r * Math.sin(endAngle)

  return `M ${x1} ${y1} A ${r} ${r} 0 1 1 ${x2} ${y2}`
})

const dashboardLength = computed(() => {
  return (270 / 360) * circumference.value // 270度弧长
})

const dashboardOffset = computed(() => {
  return dashboardLength.value - (percentage.value / 100) * dashboardLength.value
})

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
.progress-bar-container {
  .progress-wrapper {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .progress-stripes {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: linear-gradient(
      45deg,
      rgba(255, 255, 255, 0.15) 25%,
      transparent 25%,
      transparent 50%,
      rgba(255, 255, 255, 0.15) 50%,
      rgba(255, 255, 255, 0.15) 75%,
      transparent 75%,
      transparent
    );
    background-size: 1rem 1rem;
    animation: progress-stripes 1s linear infinite;
  }

  @keyframes progress-stripes {
    0% {
      background-position: 0 0;
    }
    100% {
      background-position: 1rem 0;
    }
  }

  .circle-animated {
    transition: stroke-dashoffset 0.6s ease;
  }

  .progress-circle,
  .progress-dashboard {
    svg {
      display: block;
    }
  }
}
</style>
