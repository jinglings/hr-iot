<template>
  <div class="editor-toolbar">
    <!-- 左侧操作区 -->
    <div class="toolbar-left">
      <el-button-group>
        <el-tooltip content="撤销 (Ctrl+Z)">
          <el-button :disabled="!canUndo" @click="handleUndo">
            <Icon icon="ep:back" />
          </el-button>
        </el-tooltip>
        <el-tooltip content="重做 (Ctrl+Y)">
          <el-button :disabled="!canRedo" @click="handleRedo">
            <Icon icon="ep:right" />
          </el-button>
        </el-tooltip>
      </el-button-group>

      <el-divider direction="vertical" />

      <el-button-group>
        <el-tooltip content="清空画布">
          <el-button @click="handleClear">
            <Icon icon="ep:delete" />
          </el-button>
        </el-tooltip>
        <el-tooltip content="导入">
          <el-button @click="handleImport">
            <Icon icon="ep:upload" />
          </el-button>
        </el-tooltip>
        <el-dropdown @command="handleExportCommand">
          <el-button>
            <Icon icon="ep:download" />
            导出
            <Icon icon="ep:arrow-down" class="ml-5px" />
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="json">
                <Icon icon="ep:document" />
                导出为JSON
              </el-dropdown-item>
              <el-dropdown-item command="image">
                <Icon icon="ep:picture" />
                导出为图片
              </el-dropdown-item>
              <el-dropdown-item command="copy">
                <Icon icon="ep:document-copy" />
                复制到剪贴板
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-button-group>
    </div>

    <!-- 中间标题区 -->
    <div class="toolbar-center">
      <el-input
        v-model="canvasName"
        class="canvas-name-input"
        placeholder="请输入大屏名称"
        @blur="handleUpdateName"
      />
    </div>

    <!-- 右侧操作区 -->
    <div class="toolbar-right">
      <!-- 缩放控制 -->
      <div class="zoom-control">
        <el-button size="small" @click="handleZoomOut">
          <Icon icon="ep:minus" />
        </el-button>
        <span class="zoom-value">{{ Math.round(zoom * 100) }}%</span>
        <el-button size="small" @click="handleZoomIn">
          <Icon icon="ep:plus" />
        </el-button>
        <el-button size="small" @click="handleResetZoom">
          <Icon icon="ep:refresh-right" />
        </el-button>
      </div>

      <el-divider direction="vertical" />

      <!-- 屏幕适配模式 -->
      <el-select
        v-model="scaleMode"
        class="scale-mode-select"
        placeholder="适配模式"
        size="small"
        @change="handleScaleModeChange"
      >
        <el-option label="等比缩放" value="scale" />
        <el-option label="宽度适配" value="width" />
        <el-option label="高度适配" value="height" />
        <el-option label="拉伸铺满" value="stretch" />
      </el-select>

      <el-divider direction="vertical" />

      <!-- 预览和保存 -->
      <el-button type="primary" @click="handlePreview">
        <Icon icon="ep:view" />
        预览
      </el-button>
      <el-button type="success" @click="handleSave">
        <Icon icon="ep:check" />
        保存
      </el-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { storeToRefs } from 'pinia'
import { useDashboardStore } from '@/store/modules/dashboard'
import { ElMessageBox, ElMessage } from 'element-plus'
import { downloadJSON, readJSONFile } from '@/utils/dashboard'
import { exportToJSON, exportToImage, copyToClipboard } from '@/utils/dashboard/export'
import type { CanvasConfig } from '@/types/dashboard'

const dashboardStore = useDashboardStore()
const { canvas, zoom, canUndo, canRedo } = storeToRefs(dashboardStore)

// Emit事件
const emit = defineEmits(['preview', 'save', 'export'])

// 画布名称
const canvasName = ref(canvas.value.name)

// 适配模式
const scaleMode = ref(canvas.value.scale.mode)

// 更新画布名称
const handleUpdateName = () => {
  dashboardStore.updateCanvas({ name: canvasName.value })
}

// 撤销
const handleUndo = () => {
  dashboardStore.undo()
}

// 重做
const handleRedo = () => {
  dashboardStore.redo()
}

// 清空画布
const handleClear = async () => {
  try {
    await ElMessageBox.confirm('确定要清空画布吗?此操作不可恢复!', '警告', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    dashboardStore.clearCanvas()
    ElMessage.success('画布已清空')
  } catch {
    // 用户取消
  }
}

// 导入
const handleImport = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.json'
  input.onchange = async (e: any) => {
    const file = e.target.files[0]
    if (!file) return

    try {
      const data: CanvasConfig = await readJSONFile(file)
      dashboardStore.setCanvas(data)
      ElMessage.success('导入成功')
    } catch (error: any) {
      ElMessage.error(error.message || '导入失败')
    }
  }
  input.click()
}

// 导出命令处理
const handleExportCommand = async (command: string) => {
  const filename = `${canvas.value.name || '大屏'}_${Date.now()}`

  try {
    switch (command) {
      case 'json':
        exportToJSON(canvas.value, filename)
        ElMessage.success('导出JSON成功')
        break

      case 'image':
        // 通过emit让父组件提供canvas元素
        emit('export', 'image')
        break

      case 'copy':
        // 通过emit让父组件提供canvas元素
        emit('export', 'copy')
        break
    }
  } catch (error: any) {
    ElMessage.error(error.message || '导出失败')
  }
}

// 放大
const handleZoomIn = () => {
  dashboardStore.setZoom(zoom.value + 0.1)
}

// 缩小
const handleZoomOut = () => {
  dashboardStore.setZoom(zoom.value - 0.1)
}

// 重置缩放
const handleResetZoom = () => {
  dashboardStore.setZoom(1)
}

// 切换适配模式
const handleScaleModeChange = (mode: string) => {
  dashboardStore.updateCanvas({
    scale: {
      ...canvas.value.scale,
      mode: mode as any
    }
  })
}

// 预览
const handlePreview = () => {
  emit('preview')
}

// 保存
const handleSave = () => {
  emit('save')
}

// 监听快捷键
onMounted(() => {
  const handleKeydown = (e: KeyboardEvent) => {
    // Ctrl+Z 撤销
    if (e.ctrlKey && e.key === 'z') {
      e.preventDefault()
      handleUndo()
    }
    // Ctrl+Y 重做
    if (e.ctrlKey && e.key === 'y') {
      e.preventDefault()
      handleRedo()
    }
    // Ctrl+S 保存
    if (e.ctrlKey && e.key === 's') {
      e.preventDefault()
      handleSave()
    }
  }

  window.addEventListener('keydown', handleKeydown)

  onUnmounted(() => {
    window.removeEventListener('keydown', handleKeydown)
  })
})
</script>

<style lang="scss" scoped>
.editor-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 16px;
  background-color: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);

  .toolbar-left,
  .toolbar-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .toolbar-center {
    flex: 1;
    display: flex;
    justify-content: center;
    padding: 0 20px;

    .canvas-name-input {
      max-width: 300px;
      text-align: center;

      :deep(.el-input__inner) {
        text-align: center;
        font-size: 16px;
        font-weight: 500;
      }
    }
  }

  .zoom-control {
    display: flex;
    align-items: center;
    gap: 8px;

    .zoom-value {
      min-width: 50px;
      text-align: center;
      font-size: 14px;
      color: var(--el-text-color-regular);
    }
  }

  .scale-mode-select {
    width: 120px;
  }

  .el-divider {
    height: 20px;
    margin: 0;
  }
}
</style>
