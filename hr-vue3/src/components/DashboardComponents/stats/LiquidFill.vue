<template>
  <div class="liquid-fill-container" :style="containerStyle">
    <canvas ref="canvasRef" class="liquid-canvas"></canvas>
    <div class="liquid-text" :style="textStyle">
      <div class="liquid-value">{{ displayValue }}</div>
      <div v-if="title" class="liquid-title">{{ title }}</div>
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
const canvasRef = ref<HTMLCanvasElement>()
let animationId: number | null = null
let waveOffset = 0

// 配置项
const title = computed(() => props.component.options?.title || '')
const shape = computed(() => props.component.options?.shape || 'circle') // circle, rect, diamond
const waveCount = computed(() => props.component.options?.waveCount || 2)
const waveHeight = computed(() => props.component.options?.waveHeight || 0.05)
const waveSpeed = computed(() => props.component.options?.waveSpeed || 0.02)

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

// 显示值
const displayValue = computed(() => {
  const format = props.component.options?.format || 'percent'
  if (format === 'percent') {
    return `${percentage.value.toFixed(0)}%`
  } else {
    return `${rawValue.value}/${maxValue.value}`
  }
})

// 水波颜色
const waveColors = computed(() => {
  const colors = props.component.options?.waveColors
  if (colors && Array.isArray(colors)) {
    return colors
  }
  return ['rgba(59, 130, 246, 0.6)', 'rgba(37, 99, 235, 0.4)']
})

// 样式
const containerStyle = computed(() => ({
  position: 'relative',
  width: '100%',
  height: '100%',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  backgroundColor: props.component.style?.backgroundColor || 'transparent',
  borderRadius: `${props.component.style?.borderRadius || 4}px`
}))

const textStyle = computed(() => ({
  position: 'absolute',
  top: '50%',
  left: '50%',
  transform: 'translate(-50%, -50%)',
  textAlign: 'center',
  pointerEvents: 'none',
  zIndex: '10'
}))

// 绘制液体填充
const drawLiquidFill = () => {
  const canvas = canvasRef.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const width = canvas.width
  const height = canvas.height
  const centerX = width / 2
  const centerY = height / 2
  const radius = Math.min(width, height) / 2 - 10

  // 清除画布
  ctx.clearRect(0, 0, width, height)

  // 创建形状路径
  ctx.save()
  ctx.beginPath()

  if (shape.value === 'circle') {
    // 圆形
    ctx.arc(centerX, centerY, radius, 0, Math.PI * 2)
  } else if (shape.value === 'rect') {
    // 方形
    const size = radius * 1.4
    ctx.rect(centerX - size / 2, centerY - size / 2, size, size)
  } else if (shape.value === 'diamond') {
    // 菱形
    ctx.moveTo(centerX, centerY - radius)
    ctx.lineTo(centerX + radius, centerY)
    ctx.lineTo(centerX, centerY + radius)
    ctx.lineTo(centerX - radius, centerY)
    ctx.closePath()
  }

  // 绘制外边框
  ctx.strokeStyle = props.component.options?.borderColor || 'rgba(59, 130, 246, 0.5)'
  ctx.lineWidth = props.component.options?.borderWidth || 3
  ctx.stroke()

  // 设置裁剪区域
  ctx.clip()

  // 计算水位高度
  const waterLevel = height - (height * percentage.value) / 100

  // 绘制多个波浪
  for (let i = 0; i < waveCount.value; i++) {
    drawWave(
      ctx,
      width,
      height,
      waterLevel,
      waveOffset + (i * Math.PI) / waveCount.value,
      waveColors.value[i % waveColors.value.length]
    )
  }

  ctx.restore()

  // 更新波浪偏移
  waveOffset += waveSpeed.value

  // 继续动画
  animationId = requestAnimationFrame(drawLiquidFill)
}

// 绘制单个波浪
const drawWave = (
  ctx: CanvasRenderingContext2D,
  width: number,
  height: number,
  waterLevel: number,
  offset: number,
  color: string
) => {
  ctx.beginPath()
  ctx.moveTo(0, waterLevel)

  // 绘制波浪曲线
  for (let x = 0; x <= width; x++) {
    const y =
      waterLevel +
      Math.sin((x / width) * Math.PI * 4 + offset) * (height * waveHeight.value)
    ctx.lineTo(x, y)
  }

  // 完成路径
  ctx.lineTo(width, height)
  ctx.lineTo(0, height)
  ctx.closePath()

  // 填充
  ctx.fillStyle = color
  ctx.fill()
}

// 初始化画布
const initCanvas = () => {
  const canvas = canvasRef.value
  if (!canvas) return

  // 设置画布大小
  const container = canvas.parentElement
  if (container) {
    const rect = container.getBoundingClientRect()
    canvas.width = rect.width
    canvas.height = rect.height
  }

  // 开始动画
  drawLiquidFill()
}

// 处理窗口大小变化
const handleResize = () => {
  initCanvas()
}

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
  initCanvas()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  dataSourceManager.stopPolling(props.component.id)
  dataSourceManager.closeWebSocket(`ws_${props.component.id}`)

  if (animationId !== null) {
    cancelAnimationFrame(animationId)
  }

  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.liquid-fill-container {
  .liquid-canvas {
    display: block;
    width: 100%;
    height: 100%;
  }

  .liquid-text {
    .liquid-value {
      font-size: 32px;
      font-weight: bold;
      color: #fff;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
      margin-bottom: 4px;
    }

    .liquid-title {
      font-size: 14px;
      color: #e5e7eb;
      text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
    }
  }
}
</style>
