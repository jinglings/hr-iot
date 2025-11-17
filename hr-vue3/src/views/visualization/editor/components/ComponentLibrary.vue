<template>
  <div class="component-library">
    <div class="library-header">
      <span class="header-title">组件库</span>
    </div>

    <el-scrollbar class="library-content">
      <!-- 组件分类 -->
      <div v-for="category in categories" :key="category.value" class="component-category">
        <div class="category-title">
          <Icon :icon="category.icon" class="category-icon" />
          <span>{{ category.label }}</span>
        </div>

        <div class="component-list">
          <div
            v-for="component in getComponentsByCategory(category.value)"
            :key="component.type"
            class="component-item"
            draggable="true"
            @dragstart="handleDragStart($event, component)"
            @dragend="handleDragEnd"
          >
            <div class="component-icon">
              <Icon :icon="component.icon" :size="24" />
            </div>
            <div class="component-name">{{ component.name }}</div>
          </div>
        </div>
      </div>
    </el-scrollbar>
  </div>
</template>

<script lang="ts" setup>
import { componentLibrary } from '@/components/DashboardComponents/config'
import { ComponentCategory } from '@/types/dashboard'
import type { ComponentLibraryItem } from '@/types/dashboard'

// 组件分类
const categories = [
  {
    label: '图表组件',
    value: ComponentCategory.CHART,
    icon: 'ep:data-line'
  },
  {
    label: '文本组件',
    value: ComponentCategory.TEXT,
    icon: 'ep:document'
  },
  {
    label: '装饰组件',
    value: ComponentCategory.DECORATION,
    icon: 'ep:picture'
  },
  {
    label: '媒体组件',
    value: ComponentCategory.MEDIA,
    icon: 'ep:video-camera'
  },
  {
    label: '表格组件',
    value: ComponentCategory.TABLE,
    icon: 'ep:tickets'
  }
]

// 根据分类获取组件列表
const getComponentsByCategory = (category: ComponentCategory) => {
  return componentLibrary.filter((item) => item.category === category)
}

// 拖拽开始
const handleDragStart = (e: DragEvent, component: ComponentLibraryItem) => {
  if (!e.dataTransfer) return

  // 设置拖拽数据
  e.dataTransfer.effectAllowed = 'copy'
  e.dataTransfer.setData(
    'application/json',
    JSON.stringify({
      type: 'new',
      component
    })
  )

  // 设置拖拽样式
  const target = e.currentTarget as HTMLElement
  target.classList.add('dragging')
}

// 拖拽结束
const handleDragEnd = (e: DragEvent) => {
  const target = e.currentTarget as HTMLElement
  target.classList.remove('dragging')
}
</script>

<style lang="scss" scoped>
.component-library {
  display: flex;
  flex-direction: column;
  width: 280px;
  height: 100%;
  background-color: var(--el-bg-color);
  border-right: 1px solid var(--el-border-color);

  .library-header {
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

  .library-content {
    flex: 1;
    padding: 12px;
  }

  .component-category {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }

    .category-title {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 12px;
      padding: 8px 12px;
      font-size: 14px;
      font-weight: 500;
      color: var(--el-text-color-regular);
      background-color: var(--el-fill-color-light);
      border-radius: 4px;

      .category-icon {
        color: var(--el-color-primary);
      }
    }

    .component-list {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;
    }

    .component-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 16px 8px;
      background-color: var(--el-bg-color-page);
      border: 1px solid var(--el-border-color-lighter);
      border-radius: 6px;
      cursor: move;
      transition: all 0.2s;

      &:hover {
        border-color: var(--el-color-primary);
        box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
        transform: translateY(-2px);
      }

      &.dragging {
        opacity: 0.5;
      }

      .component-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 40px;
        height: 40px;
        margin-bottom: 8px;
        color: var(--el-color-primary);
        background-color: var(--el-color-primary-light-9);
        border-radius: 8px;
      }

      .component-name {
        font-size: 12px;
        color: var(--el-text-color-regular);
        text-align: center;
      }
    }
  }
}
</style>
