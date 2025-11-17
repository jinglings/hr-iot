<template>
  <div class="number-flip" :style="numberStyle">
    <span class="number-display">{{ displayValue }}</span>
  </div>
</template>

<script lang="ts" setup>
import type { DashboardComponent } from '@/types/dashboard'
import { CountTo } from '@/components/CountTo'

interface Props {
  component: DashboardComponent
  options?: any
  data?: any
}

const props = defineProps<Props>()

// 当前显示的值
const displayValue = ref('0')

// 目标值
const targetValue = computed(() => {
  const value =
    props.data?.static?.value ||
    props.component.data?.static?.value ||
    props.options?.value ||
    0
  return Number(value)
})

// 配置项
const config = computed(() => {
  const opts = props.options || props.component.options || {}
  return {
    duration: opts.duration || 2000, // 动画时长
    decimals: opts.decimals || 0, // 小数位数
    separator: opts.separator || ',', // 千分位分隔符
    prefix: opts.prefix || '', // 前缀
    suffix: opts.suffix || '' // 后缀
  }
})

// 格式化数字
const formatNumber = (num: number): string => {
  const { decimals, separator, prefix, suffix } = config.value

  // 保留小数位
  let formatted = num.toFixed(decimals)

  // 添加千分位分隔符
  if (separator) {
    const parts = formatted.split('.')
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, separator)
    formatted = parts.join('.')
  }

  // 添加前缀和后缀
  return `${prefix}${formatted}${suffix}`
}

// 数字样式
const numberStyle = computed(() => ({
  width: '100%',
  height: '100%',
  display: 'flex',
  alignItems: 'center',
  justifyContent: props.component.style.textAlign || 'center',
  padding: '8px'
}))

// 数字动画
let animationFrameId: number | null = null
let startTime: number | null = null
let startValue = 0

const animate = (timestamp: number) => {
  if (!startTime) startTime = timestamp

  const elapsed = timestamp - startTime
  const duration = config.value.duration

  if (elapsed < duration) {
    // 缓动函数 (easeOutQuart)
    const progress = 1 - Math.pow(1 - elapsed / duration, 4)
    const currentValue = startValue + (targetValue.value - startValue) * progress
    displayValue.value = formatNumber(currentValue)
    animationFrameId = requestAnimationFrame(animate)
  } else {
    displayValue.value = formatNumber(targetValue.value)
    startTime = null
  }
}

// 开始动画
const startAnimation = () => {
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
  }

  startTime = null
  startValue = Number(displayValue.value.replace(/[^0-9.-]/g, '')) || 0

  animationFrameId = requestAnimationFrame(animate)
}

// 监听目标值变化
watch(targetValue, () => {
  startAnimation()
})

// 初始化
onMounted(() => {
  displayValue.value = formatNumber(0)
  nextTick(() => {
    startAnimation()
  })
})

// 清理
onBeforeUnmount(() => {
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
  }
})
</script>

<style lang="scss" scoped>
.number-flip {
  user-select: none;

  .number-display {
    font-variant-numeric: tabular-nums;
    font-feature-settings: 'tnum';
    letter-spacing: 0.05em;
  }
}
</style>
