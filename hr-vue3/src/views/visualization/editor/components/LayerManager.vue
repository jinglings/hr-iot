<template>
  <div class="layer-manager">
    <div class="layer-header">
      <span class="header-title">图层</span>
      <el-tooltip content="刷新">
        <el-button size="small" text @click="handleRefresh">
          <Icon icon="ep:refresh" />
        </el-button>
      </el-tooltip>
    </div>

    <el-scrollbar class="layer-content">
      <div v-if="sortedLayers.length === 0" class="empty-layer">
        <el-empty description="暂无图层" />
      </div>

      <!-- 图层列表 -->
      <draggable
        v-else
        v-model="sortedLayers"
        :animation="200"
        class="layer-list"
        ghost-class="layer-ghost"
        handle=".layer-drag-handle"
        item-key="id"
        @end="handleDragEnd"
      >
        <template #item="{ element: layer }">
          <div
            class="layer-item"
            :class="{
              active: selectedComponentId === layer.id,
              locked: layer.locked,
              hidden: layer.hidden
            }"
            @click="handleSelectLayer(layer.id)"
            @dblclick="handleRenameStart(layer)"
          >
            <!-- 拖拽手柄 -->
            <div class="layer-drag-handle">
              <Icon icon="ep:rank" />
            </div>

            <!-- 图层信息 -->
            <div class="layer-info">
              <!-- 组件图标 -->
              <Icon :icon="layer.icon || 'ep:document'" class="layer-icon" />

              <!-- 组件名称 -->
              <div v-if="editingLayerId === layer.id" class="layer-name-edit">
                <el-input
                  ref="nameInputRef"
                  v-model="editingName"
                  size="small"
                  @blur="handleRenameSave(layer)"
                  @keyup.enter="handleRenameSave(layer)"
                  @keyup.esc="handleRenameCancel"
                />
              </div>
              <span v-else class="layer-name" :title="layer.name">
                {{ layer.name }}
              </span>

              <!-- 尺寸信息 -->
              <span class="layer-size">
                {{ Math.round(layer.position.w) }}×{{ Math.round(layer.position.h) }}
              </span>
            </div>

            <!-- 操作按钮组 -->
            <div class="layer-actions" @click.stop>
              <!-- 显示/隐藏 -->
              <el-tooltip :content="layer.hidden ? '显示' : '隐藏'">
                <el-button
                  :type="layer.hidden ? 'info' : 'primary'"
                  link
                  size="small"
                  @click="handleToggleVisible(layer.id)"
                >
                  <Icon :icon="layer.hidden ? 'ep:hide' : 'ep:view'" />
                </el-button>
              </el-tooltip>

              <!-- 锁定/解锁 -->
              <el-tooltip :content="layer.locked ? '解锁' : '锁定'">
                <el-button
                  :type="layer.locked ? 'warning' : 'info'"
                  link
                  size="small"
                  @click="handleToggleLock(layer.id)"
                >
                  <Icon :icon="layer.locked ? 'ep:lock' : 'ep:unlock'" />
                </el-button>
              </el-tooltip>

              <!-- 更多操作 -->
              <el-dropdown trigger="click" @command="handleCommand($event, layer)">
                <el-button link size="small">
                  <Icon icon="ep:more-filled" />
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="rename">
                      <Icon icon="ep:edit" />
                      重命名
                    </el-dropdown-item>
                    <el-dropdown-item command="duplicate">
                      <Icon icon="ep:document-copy" />
                      复制
                    </el-dropdown-item>
                    <el-dropdown-item command="moveTop">
                      <Icon icon="ep:top" />
                      置顶
                    </el-dropdown-item>
                    <el-dropdown-item command="moveBottom">
                      <Icon icon="ep:bottom" />
                      置底
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" divided>
                      <Icon icon="ep:delete" />
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </template>
      </draggable>
    </el-scrollbar>

    <!-- 统计信息 -->
    <div class="layer-footer">
      <span class="footer-info">共 {{ sortedLayers.length }} 个图层</span>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { storeToRefs } from 'pinia'
import { useDashboardStore } from '@/store/modules/dashboard'
import type { DashboardComponent } from '@/types/dashboard'
import draggable from 'vuedraggable'
import { ElMessage } from 'element-plus'

const dashboardStore = useDashboardStore()
const { canvas, selectedComponentId } = storeToRefs(dashboardStore)

// 排序后的图层列表（按 zIndex 倒序，从上到下）
const sortedLayers = computed({
  get: () => {
    return [...canvas.value.components].sort((a, b) => b.position.zIndex - a.position.zIndex)
  },
  set: (value) => {
    // 拖拽后更新 zIndex
    const maxZIndex = value.length
    value.forEach((layer, index) => {
      layer.position.zIndex = maxZIndex - index
    })
    canvas.value.components = value
  }
})

// 选中图层
const handleSelectLayer = (layerId: string) => {
  dashboardStore.selectComponent(layerId)
}

// 显示/隐藏
const handleToggleVisible = (layerId: string) => {
  dashboardStore.toggleComponentVisibility(layerId)
}

// 锁定/解锁
const handleToggleLock = (layerId: string) => {
  dashboardStore.toggleComponentLock(layerId)
}

// 重命名相关
const editingLayerId = ref<string | null>(null)
const editingName = ref('')
const nameInputRef = ref()

const handleRenameStart = (layer: DashboardComponent) => {
  editingLayerId.value = layer.id
  editingName.value = layer.name
  nextTick(() => {
    nameInputRef.value?.$el?.querySelector('input')?.focus()
  })
}

const handleRenameSave = (layer: DashboardComponent) => {
  if (editingName.value.trim()) {
    dashboardStore.updateComponent(layer.id, {
      name: editingName.value.trim()
    })
  }
  editingLayerId.value = null
  editingName.value = ''
}

const handleRenameCancel = () => {
  editingLayerId.value = null
  editingName.value = ''
}

// 下拉菜单命令处理
const handleCommand = (command: string, layer: DashboardComponent) => {
  switch (command) {
    case 'rename':
      handleRenameStart(layer)
      break
    case 'duplicate':
      dashboardStore.copyComponent(layer.id)
      ElMessage.success('复制成功')
      break
    case 'moveTop':
      dashboardStore.moveComponentLayer(layer.id, 'top')
      break
    case 'moveBottom':
      dashboardStore.moveComponentLayer(layer.id, 'bottom')
      break
    case 'delete':
      dashboardStore.deleteComponent(layer.id)
      ElMessage.success('删除成功')
      break
  }
}

// 拖拽结束
const handleDragEnd = () => {
  ElMessage.success('图层顺序已更新')
}

// 刷新
const handleRefresh = () => {
  ElMessage.success('已刷新')
}
</script>

<style lang="scss" scoped>
.layer-manager {
  display: flex;
  flex-direction: column;
  width: 280px;
  height: 100%;
  background-color: var(--el-bg-color);
  border-right: 1px solid var(--el-border-color);

  .layer-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 48px;
    padding: 0 16px;
    border-bottom: 1px solid var(--el-border-color);

    .header-title {
      font-size: 16px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }
  }

  .layer-content {
    flex: 1;
    padding: 8px;

    .empty-layer {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100%;
    }

    .layer-list {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .layer-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px;
      background-color: var(--el-bg-color-page);
      border: 1px solid var(--el-border-color-lighter);
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        background-color: var(--el-fill-color-light);
        border-color: var(--el-border-color);
      }

      &.active {
        background-color: var(--el-color-primary-light-9);
        border-color: var(--el-color-primary);

        .layer-name {
          color: var(--el-color-primary);
          font-weight: 500;
        }
      }

      &.locked {
        opacity: 0.7;

        .layer-drag-handle {
          cursor: not-allowed;
          opacity: 0.5;
        }
      }

      &.hidden {
        opacity: 0.5;
      }

      .layer-drag-handle {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 16px;
        color: var(--el-text-color-secondary);
        cursor: move;

        &:hover {
          color: var(--el-color-primary);
        }
      }

      .layer-info {
        flex: 1;
        display: flex;
        align-items: center;
        gap: 8px;
        min-width: 0;

        .layer-icon {
          flex-shrink: 0;
          font-size: 16px;
          color: var(--el-color-primary);
        }

        .layer-name {
          flex: 1;
          font-size: 13px;
          color: var(--el-text-color-regular);
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .layer-name-edit {
          flex: 1;
          min-width: 0;

          :deep(.el-input__wrapper) {
            padding: 2px 8px;
          }
        }

        .layer-size {
          flex-shrink: 0;
          font-size: 11px;
          color: var(--el-text-color-secondary);
          background-color: var(--el-fill-color);
          padding: 2px 6px;
          border-radius: 3px;
        }
      }

      .layer-actions {
        display: flex;
        align-items: center;
        gap: 4px;
        flex-shrink: 0;

        .el-button {
          padding: 4px;
        }
      }
    }

    .layer-ghost {
      opacity: 0.5;
      background-color: var(--el-color-primary-light-7);
    }
  }

  .layer-footer {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 36px;
    padding: 0 16px;
    border-top: 1px solid var(--el-border-color);
    background-color: var(--el-fill-color-lighter);

    .footer-info {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }
}
</style>
