<template>
  <div class="text-box" :style="textStyle">
    {{ content }}
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

// 文本内容
const content = computed(() => {
  return props.options?.content || props.data?.static?.text || '文本内容'
})

// 文本样式
const textStyle = computed(() => ({
  width: '100%',
  height: '100%',
  display: 'flex',
  alignItems: 'center',
  justifyContent: props.component.style.textAlign || 'left',
  padding: '8px',
  wordBreak: 'break-all',
  overflow: 'hidden'
}))
</script>

<style lang="scss" scoped>
.text-box {
  user-select: none;
}
</style>
