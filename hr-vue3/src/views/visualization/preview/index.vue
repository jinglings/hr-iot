<template>
  <div class="dashboard-preview">
    <div class="preview-wrapper" :style="previewWrapperStyle">
      <div class="preview-canvas" :style="canvasStyle">
        <!-- 渲染所有组件 -->
        <div
          v-for="component in sortedComponents"
          :key="component.id"
          class="preview-component"
          :style="getComponentStyle(component)"
        >
          <component
            :is="getComponentType(component.type)"
            :component="component"
            :options="component.options"
            :data="component.data"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import type { CanvasConfig, DashboardComponent } from '@/types/dashboard'
import { useScreenAdapter } from '@/utils/dashboard/screenAdapter'

interface Props {
  canvas: CanvasConfig
}

const props = defineProps<Props>()

// 排序后的组件列表
const sortedComponents = computed(() => {
  return [...props.canvas.components].sort((a, b) => a.position.zIndex - b.position.zIndex)
})

// 屏幕适配
const { style: previewWrapperStyle } = useScreenAdapter(
  computed(() => ({
    width: props.canvas.width,
    height: props.canvas.height,
    mode: props.canvas.scale.mode
  }))
)

// 画布样式
const canvasStyle = computed(() => ({
  width: `${props.canvas.width}px`,
  height: `${props.canvas.height}px`,
  backgroundColor: props.canvas.backgroundColor,
  backgroundImage: props.canvas.backgroundImage ? `url(${props.canvas.backgroundImage})` : 'none',
  backgroundSize: 'cover',
  backgroundPosition: 'center',
  position: 'relative',
  overflow: 'hidden'
}))

// 获取组件样式
const getComponentStyle = (component: DashboardComponent) => {
  const { position, style } = component

  return {
    position: 'absolute',
    left: `${position.x}px`,
    top: `${position.y}px`,
    width: `${position.w}px`,
    height: `${position.h}px`,
    transform: `rotate(${position.rotate}deg)`,
    zIndex: position.zIndex,
    backgroundColor: style.backgroundColor,
    backgroundImage: style.backgroundImage ? `url(${style.backgroundImage})` : undefined,
    border: style.borderWidth
      ? `${style.borderWidth}px ${style.borderStyle || 'solid'} ${style.borderColor}`
      : undefined,
    borderRadius: style.borderRadius ? `${style.borderRadius}px` : undefined,
    opacity: style.opacity !== undefined ? style.opacity : 1,
    boxShadow: style.boxShadow,
    fontSize: style.fontSize ? `${style.fontSize}px` : undefined,
    fontWeight: style.fontWeight,
    color: style.color,
    textAlign: style.textAlign,
    display: component.hidden ? 'none' : 'block'
  }
}

// 获取组件类型
const getComponentType = (type: string) => {
  // 这里返回对应的组件
  // 暂时返回 div
  return 'div'
}
</script>

<style lang="scss" scoped>
.dashboard-preview {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #000;
  overflow: auto;

  .preview-wrapper {
    margin: auto;
  }

  .preview-canvas {
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.5);
  }

  .preview-component {
    pointer-events: all;
  }
}
</style>
