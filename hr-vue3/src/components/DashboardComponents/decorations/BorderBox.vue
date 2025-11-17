<template>
  <div class="border-box" :style="boxStyle">
    <svg class="border-svg" :width="width" :height="height">
      <!-- 边框路径 -->
      <defs>
        <linearGradient :id="`gradient-${uid}`" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" :style="{ stopColor: colors[0], stopOpacity: 1 }" />
          <stop offset="100%" :style="{ stopColor: colors[1], stopOpacity: 1 }" />
        </linearGradient>
      </defs>

      <!-- 边框样式 1: 矩形边框 -->
      <template v-if="borderType === 1">
        <rect
          x="2"
          y="2"
          :width="width - 4"
          :height="height - 4"
          fill="none"
          :stroke="`url(#gradient-${uid})`"
          stroke-width="2"
        />
        <!-- 四角装饰 -->
        <path
          :d="`M 10 2 L 2 2 L 2 10`"
          fill="none"
          :stroke="colors[0]"
          stroke-width="3"
          stroke-linecap="round"
        />
        <path
          :d="`M ${width - 10} 2 L ${width - 2} 2 L ${width - 2} 10`"
          fill="none"
          :stroke="colors[0]"
          stroke-width="3"
          stroke-linecap="round"
        />
        <path
          :d="`M 10 ${height - 2} L 2 ${height - 2} L 2 ${height - 10}`"
          fill="none"
          :stroke="colors[0]"
          stroke-width="3"
          stroke-linecap="round"
        />
        <path
          :d="`M ${width - 10} ${height - 2} L ${width - 2} ${height - 2} L ${width - 2} ${height - 10}`"
          fill="none"
          :stroke="colors[0]"
          stroke-width="3"
          stroke-linecap="round"
        />
      </template>

      <!-- 边框样式 2: 科技边框 -->
      <template v-else-if="borderType === 2">
        <!-- 外边框 -->
        <polygon
          :points="`20,2 ${width - 2},2 ${width - 2},${height - 20} ${width - 20},${height - 2} 2,${height - 2} 2,20`"
          fill="none"
          :stroke="`url(#gradient-${uid})`"
          stroke-width="2"
        />
        <!-- 内装饰线 -->
        <line x1="20" y1="10" :x2="width - 10" y2="10" :stroke="colors[0]" stroke-width="1" opacity="0.5" />
        <line x1="10" y1="20" x2="10" :y2="height - 10" :stroke="colors[0]" stroke-width="1" opacity="0.5" />
      </template>

      <!-- 边框样式 3: 双线边框 -->
      <template v-else-if="borderType === 3">
        <rect
          x="4"
          y="4"
          :width="width - 8"
          :height="height - 8"
          fill="none"
          :stroke="colors[0]"
          stroke-width="1"
        />
        <rect
          x="8"
          y="8"
          :width="width - 16"
          :height="height - 16"
          fill="none"
          :stroke="colors[1]"
          stroke-width="1"
          opacity="0.6"
        />
      </template>

      <!-- 默认边框 -->
      <template v-else>
        <rect
          x="2"
          y="2"
          :width="width - 4"
          :height="height - 4"
          fill="none"
          :stroke="`url(#gradient-${uid})`"
          stroke-width="2"
          rx="4"
        />
      </template>
    </svg>

    <!-- 内容插槽 -->
    <div class="border-content">
      <slot></slot>
    </div>
  </div>
</template>

<script lang="ts" setup>
import type { DashboardComponent } from '@/types/dashboard'

interface Props {
  component: DashboardComponent
  options?: any
  data?: any
}

const props = defineProps<Props>()

// 唯一ID(用于渐变)
const uid = ref(`border-${Math.random().toString(36).substr(2, 9)}`)

// 边框类型
const borderType = computed(() => {
  return props.options?.borderType || props.component.options?.borderType || 1
})

// 颜色
const colors = computed(() => {
  const borderColor = props.options?.borderColor || props.component.options?.borderColor
  if (Array.isArray(borderColor)) {
    return borderColor
  }
  return ['#00d4ff', '#00a0e9']
})

// 宽度和高度
const width = computed(() => props.component.position.w)
const height = computed(() => props.component.position.h)

// 盒子样式
const boxStyle = computed(() => ({
  width: '100%',
  height: '100%',
  position: 'relative',
  backgroundColor: props.options?.backgroundColor || props.component.options?.backgroundColor || 'transparent'
}))
</script>

<style lang="scss" scoped>
.border-box {
  .border-svg {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
  }

  .border-content {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    padding: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>
