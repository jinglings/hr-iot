<template>
  <div
    ref="componentRef"
    class="canvas-component"
    :class="{ selected, locked: component.locked, hidden: component.hidden }"
    :style="componentStyle"
    @mousedown.stop="handleMouseDown"
    @click.stop="handleClick"
  >
    <!-- 组件内容 -->
    <div class="component-content">
      <component
        :is="componentType"
        :component="component"
        :options="component.options"
        :data="component.data"
      />
    </div>

    <!-- 选中状态的控制点 -->
    <template v-if="selected && !component.locked">
      <!-- 边框 -->
      <div class="selection-border"></div>

      <!-- 八个缩放控制点 -->
      <div
        v-for="handle in resizeHandles"
        :key="handle"
        class="resize-handle"
        :class="`resize-handle-${handle}`"
        @mousedown.stop="handleResizeStart($event, handle)"
      ></div>

      <!-- 旋转控制点 -->
      <div class="rotate-handle" @mousedown.stop="handleRotateStart">
        <Icon icon="ep:refresh-right" />
      </div>

      <!-- 删除按钮 -->
      <div class="delete-btn" @click.stop="handleDelete">
        <Icon icon="ep:delete" />
      </div>
    </template>
  </div>
</template>

<script lang="ts" setup>
import type { DashboardComponent } from '@/types/dashboard'
import { snapToGrid } from '@/utils/dashboard'

// Props
interface Props {
  component: DashboardComponent
  selected?: boolean
  zoom?: number
}

const props = withDefaults(defineProps<Props>(), {
  selected: false,
  zoom: 1
})

// Emits
const emit = defineEmits<{
  select: [componentId: string]
  update: [componentId: string, updates: Partial<DashboardComponent>]
  delete: [componentId: string]
}>()

const componentRef = ref<HTMLElement>()

// 组件类型(动态组件名称)
const componentType = computed(() => {
  // 这里根据组件类型返回对应的组件
  // 实际使用时需要导入真实的组件
  return 'div' // 临时返回div,后续替换为真实组件
})

// 组件样式
const componentStyle = computed(() => {
  const { position, style } = props.component

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
    textAlign: style.textAlign
  }
})

// 缩放控制点
const resizeHandles = ['nw', 'n', 'ne', 'e', 'se', 's', 'sw', 'w']

// 点击组件
const handleClick = () => {
  if (!props.component.locked) {
    emit('select', props.component.id)
  }
}

// ==================== 拖拽移动 ====================
const handleMouseDown = (e: MouseEvent) => {
  if (props.component.locked) return

  // 选中组件
  emit('select', props.component.id)

  const startX = e.clientX
  const startY = e.clientY
  const startPosX = props.component.position.x
  const startPosY = props.component.position.y

  const handleMouseMove = (e: MouseEvent) => {
    const deltaX = (e.clientX - startX) / props.zoom
    const deltaY = (e.clientY - startY) / props.zoom

    let newX = startPosX + deltaX
    let newY = startPosY + deltaY

    // 吸附网格
    newX = snapToGrid(newX, 10)
    newY = snapToGrid(newY, 10)

    // 限制在画布内
    newX = Math.max(0, newX)
    newY = Math.max(0, newY)

    emit('update', props.component.id, {
      position: {
        ...props.component.position,
        x: newX,
        y: newY
      }
    })
  }

  const handleMouseUp = () => {
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
  }

  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

// ==================== 缩放 ====================
const handleResizeStart = (e: MouseEvent, handle: string) => {
  e.stopPropagation()

  const startX = e.clientX
  const startY = e.clientY
  const startWidth = props.component.position.w
  const startHeight = props.component.position.h
  const startPosX = props.component.position.x
  const startPosY = props.component.position.y

  const handleMouseMove = (e: MouseEvent) => {
    const deltaX = (e.clientX - startX) / props.zoom
    const deltaY = (e.clientY - startY) / props.zoom

    let newWidth = startWidth
    let newHeight = startHeight
    let newX = startPosX
    let newY = startPosY

    // 根据不同的控制点计算新的尺寸和位置
    switch (handle) {
      case 'se': // 右下
        newWidth = startWidth + deltaX
        newHeight = startHeight + deltaY
        break
      case 'nw': // 左上
        newWidth = startWidth - deltaX
        newHeight = startHeight - deltaY
        newX = startPosX + deltaX
        newY = startPosY + deltaY
        break
      case 'ne': // 右上
        newWidth = startWidth + deltaX
        newHeight = startHeight - deltaY
        newY = startPosY + deltaY
        break
      case 'sw': // 左下
        newWidth = startWidth - deltaX
        newHeight = startHeight + deltaY
        newX = startPosX + deltaX
        break
      case 'e': // 右
        newWidth = startWidth + deltaX
        break
      case 'w': // 左
        newWidth = startWidth - deltaX
        newX = startPosX + deltaX
        break
      case 's': // 下
        newHeight = startHeight + deltaY
        break
      case 'n': // 上
        newHeight = startHeight - deltaY
        newY = startPosY + deltaY
        break
    }

    // 限制最小尺寸
    newWidth = Math.max(20, newWidth)
    newHeight = Math.max(20, newHeight)

    emit('update', props.component.id, {
      position: {
        ...props.component.position,
        x: newX,
        y: newY,
        w: newWidth,
        h: newHeight
      }
    })
  }

  const handleMouseUp = () => {
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
  }

  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

// ==================== 旋转 ====================
const handleRotateStart = (e: MouseEvent) => {
  e.stopPropagation()

  const rect = componentRef.value!.getBoundingClientRect()
  const centerX = rect.left + rect.width / 2
  const centerY = rect.top + rect.height / 2

  const startAngle = Math.atan2(e.clientY - centerY, e.clientX - centerX)
  const startRotate = props.component.position.rotate

  const handleMouseMove = (e: MouseEvent) => {
    const angle = Math.atan2(e.clientY - centerY, e.clientX - centerX)
    const rotation = startRotate + ((angle - startAngle) * 180) / Math.PI

    emit('update', props.component.id, {
      position: {
        ...props.component.position,
        rotate: Math.round(rotation)
      }
    })
  }

  const handleMouseUp = () => {
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
  }

  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

// 删除组件
const handleDelete = () => {
  emit('delete', props.component.id)
}
</script>

<style lang="scss" scoped>
.canvas-component {
  cursor: move;
  user-select: none;

  &.locked {
    cursor: not-allowed;
    opacity: 0.6;
  }

  &.hidden {
    display: none;
  }

  &.selected {
    .selection-border {
      display: block;
    }

    .resize-handle,
    .rotate-handle,
    .delete-btn {
      display: block;
    }
  }

  .component-content {
    width: 100%;
    height: 100%;
    pointer-events: none;
  }

  // 选中边框
  .selection-border {
    display: none;
    position: absolute;
    top: -2px;
    left: -2px;
    right: -2px;
    bottom: -2px;
    border: 2px solid #409eff;
    pointer-events: none;
  }

  // 缩放控制点
  .resize-handle {
    display: none;
    position: absolute;
    width: 8px;
    height: 8px;
    background-color: #fff;
    border: 2px solid #409eff;
    border-radius: 50%;

    &.resize-handle-nw {
      top: -4px;
      left: -4px;
      cursor: nw-resize;
    }

    &.resize-handle-n {
      top: -4px;
      left: 50%;
      transform: translateX(-50%);
      cursor: n-resize;
    }

    &.resize-handle-ne {
      top: -4px;
      right: -4px;
      cursor: ne-resize;
    }

    &.resize-handle-e {
      top: 50%;
      right: -4px;
      transform: translateY(-50%);
      cursor: e-resize;
    }

    &.resize-handle-se {
      bottom: -4px;
      right: -4px;
      cursor: se-resize;
    }

    &.resize-handle-s {
      bottom: -4px;
      left: 50%;
      transform: translateX(-50%);
      cursor: s-resize;
    }

    &.resize-handle-sw {
      bottom: -4px;
      left: -4px;
      cursor: sw-resize;
    }

    &.resize-handle-w {
      top: 50%;
      left: -4px;
      transform: translateY(-50%);
      cursor: w-resize;
    }
  }

  // 旋转控制点
  .rotate-handle {
    display: none;
    position: absolute;
    top: -30px;
    left: 50%;
    transform: translateX(-50%);
    width: 24px;
    height: 24px;
    background-color: #409eff;
    border-radius: 50%;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: grab;

    &:active {
      cursor: grabbing;
    }
  }

  // 删除按钮
  .delete-btn {
    display: none;
    position: absolute;
    top: -30px;
    right: -10px;
    width: 24px;
    height: 24px;
    background-color: #f56c6c;
    border-radius: 50%;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;

    &:hover {
      background-color: #f78989;
    }
  }
}
</style>
