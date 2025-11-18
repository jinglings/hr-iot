<template>
  <svg class="alignment-guides" :style="svgStyle">
    <!-- 垂直辅助线 -->
    <line
      v-for="(line, index) in verticalLines"
      :key="`v-${index}`"
      :x1="line.position"
      :y1="0"
      :x2="line.position"
      :y2="canvasHeight"
      :stroke="line.color"
      stroke-width="1"
      stroke-dasharray="4 2"
      class="guide-line"
    />

    <!-- 水平辅助线 -->
    <line
      v-for="(line, index) in horizontalLines"
      :key="`h-${index}`"
      :x1="0"
      :y1="line.position"
      :x2="canvasWidth"
      :y2="line.position"
      :stroke="line.color"
      stroke-width="1"
      stroke-dasharray="4 2"
      class="guide-line"
    />
  </svg>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import type { AlignmentLine } from '@/composables/useAlignmentGuides'

interface Props {
  lines: AlignmentLine[]
  canvasWidth: number
  canvasHeight: number
}

const props = defineProps<Props>()

// 分离垂直和水平线
const verticalLines = computed(() =>
  props.lines.filter((line) => line.type === 'vertical')
)

const horizontalLines = computed(() =>
  props.lines.filter((line) => line.type === 'horizontal')
)

// SVG样式
const svgStyle = computed(() => ({
  width: `${props.canvasWidth}px`,
  height: `${props.canvasHeight}px`
}))
</script>

<style lang="scss" scoped>
.alignment-guides {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
  z-index: 9999;

  .guide-line {
    animation: guide-appear 0.2s ease-out;
  }
}

@keyframes guide-appear {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
</style>
