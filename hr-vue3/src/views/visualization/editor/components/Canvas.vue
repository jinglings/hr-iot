<template>
  <div class="canvas-container" @click="handleCanvasClick">
    <el-scrollbar class="canvas-scrollbar">
      <div class="canvas-wrapper" :style="canvasWrapperStyle">
        <!-- 画布 -->
        <div
          ref="canvasRef"
          class="canvas"
          :style="canvasStyle"
          @drop="handleDrop"
          @dragover="handleDragOver"
        >
          <!-- 网格背景 -->
          <div v-if="showGrid" class="canvas-grid" :style="gridStyle"></div>

          <!-- 组件列表 -->
          <CanvasComponent
            v-for="component in sortedComponents"
            :key="component.id"
            :component="component"
            :selected="selectedComponentId === component.id"
            :zoom="zoom"
            @select="handleSelectComponent"
            @update="handleUpdateComponent"
            @delete="handleDeleteComponent"
            @dragging="handleComponentDragging"
            @drag-end="handleComponentDragEnd"
          />

          <!-- 对齐辅助线 -->
          <AlignmentGuides
            v-if="alignmentLines.length > 0"
            :lines="alignmentLines"
            :canvas-width="canvas.width"
            :canvas-height="canvas.height"
          />
        </div>
      </div>
    </el-scrollbar>
  </div>
</template>

<script lang="ts" setup>
import { storeToRefs } from 'pinia'
import { useDashboardStore } from '@/store/modules/dashboard'
import { cloneDeep } from 'lodash-es'
import type { DashboardComponent } from '@/types/dashboard'
import type { AlignmentLine } from '@/composables/useAlignmentGuides'
import { useAlignmentGuides } from '@/composables/useAlignmentGuides'
import CanvasComponent from './CanvasComponent.vue'
import AlignmentGuides from './AlignmentGuides.vue'

const dashboardStore = useDashboardStore()
const { canvas, selectedComponentId, sortedComponents, showGrid, zoom } =
  storeToRefs(dashboardStore)

const canvasRef = ref<HTMLElement>()

// 对齐辅助线
const alignmentLines = ref<AlignmentLine[]>([])
const { calculateAlignment } = useAlignmentGuides()

// 画布容器样式
const canvasWrapperStyle = computed(() => ({
  transform: `scale(${zoom.value})`,
  transformOrigin: 'center center'
}))

// 画布样式
const canvasStyle = computed(() => ({
  width: `${canvas.value.width}px`,
  height: `${canvas.value.height}px`,
  backgroundColor: canvas.value.backgroundColor,
  backgroundImage: canvas.value.backgroundImage
    ? `url(${canvas.value.backgroundImage})`
    : 'none',
  backgroundSize: 'cover',
  backgroundPosition: 'center'
}))

// 网格样式
const gridStyle = computed(() => {
  const gridSize = canvas.value.grid?.size || 10
  const gridColor = canvas.value.grid?.color || 'rgba(255, 255, 255, 0.05)'

  return {
    backgroundImage: `
      linear-gradient(${gridColor} 1px, transparent 1px),
      linear-gradient(90deg, ${gridColor} 1px, transparent 1px)
    `,
    backgroundSize: `${gridSize}px ${gridSize}px`
  }
})

// 处理拖拽悬停
const handleDragOver = (e: DragEvent) => {
  e.preventDefault()
  if (e.dataTransfer) {
    e.dataTransfer.dropEffect = 'copy'
  }
}

// 处理放置
const handleDrop = (e: DragEvent) => {
  e.preventDefault()

  if (!e.dataTransfer) return

  const data = e.dataTransfer.getData('application/json')
  if (!data) return

  try {
    const dragData = JSON.parse(data)

    if (dragData.type === 'new' && dragData.component) {
      // 新增组件
      const canvasRect = canvasRef.value?.getBoundingClientRect()
      if (!canvasRect) return

      // 计算组件在画布中的位置(考虑缩放)
      const x = (e.clientX - canvasRect.left) / zoom.value
      const y = (e.clientY - canvasRect.top) / zoom.value

      // 创建组件
      const newComponent = cloneDeep(dragData.component.defaultConfig) as DashboardComponent
      newComponent.position.x = Math.max(0, x - newComponent.position.w / 2)
      newComponent.position.y = Math.max(0, y - newComponent.position.h / 2)

      dashboardStore.addComponent(newComponent)
    }
  } catch (error) {
    console.error('解析拖拽数据失败:', error)
  }
}

// 处理画布点击
const handleCanvasClick = (e: MouseEvent) => {
  // 点击画布空白区域,取消选中
  if (e.target === canvasRef.value || (e.target as HTMLElement).classList.contains('canvas-grid')) {
    dashboardStore.selectComponent(null)
  }
}

// 选中组件
const handleSelectComponent = (componentId: string) => {
  dashboardStore.selectComponent(componentId)
}

// 更新组件
const handleUpdateComponent = (componentId: string, updates: Partial<DashboardComponent>) => {
  dashboardStore.updateComponent(componentId, updates)
}

// 删除组件
const handleDeleteComponent = (componentId: string) => {
  dashboardStore.deleteComponent(componentId)
}

// 处理组件拖拽中
const handleComponentDragging = (componentId: string, position: { x: number; y: number }) => {
  const component = sortedComponents.value.find((c) => c.id === componentId)
  if (!component) return

  // 计算对齐
  const result = calculateAlignment(
    component,
    position,
    sortedComponents.value.filter((c) => c.id !== componentId),
    canvas.value
  )

  // 更新对齐线
  alignmentLines.value = result.lines

  // 更新组件位置(应用对齐调整)
  dashboardStore.updateComponent(componentId, {
    position: {
      ...component.position,
      x: result.position.x,
      y: result.position.y
    }
  })
}

// 处理组件拖拽结束
const handleComponentDragEnd = () => {
  // 清除对齐线
  alignmentLines.value = []
}
</script>

<style lang="scss" scoped>
.canvas-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #f5f5f5;
  background-image: radial-gradient(circle, #e0e0e0 1px, transparent 1px);
  background-size: 20px 20px;

  .canvas-scrollbar {
    flex: 1;
  }

  .canvas-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    min-width: 100%;
    min-height: 100%;
    padding: 40px;
    transition: transform 0.1s ease-out;
  }

  .canvas {
    position: relative;
    background-color: #0e1117;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
    overflow: hidden;

    .canvas-grid {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      pointer-events: none;
    }
  }
}
</style>
