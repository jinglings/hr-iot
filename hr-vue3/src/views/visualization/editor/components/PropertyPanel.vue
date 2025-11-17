<template>
  <div v-if="selectedComponent" class="property-panel">
    <div class="panel-header">
      <Icon :icon="selectedComponent.icon" />
      <span class="panel-title">{{ selectedComponent.name }}</span>
    </div>

    <el-scrollbar class="panel-content">
      <!-- 基础属性 -->
      <el-collapse v-model="activeNames" accordion>
        <!-- 位置属性 -->
        <el-collapse-item title="位置属性" name="position">
          <el-form label-width="60px" size="small">
            <el-form-item label="X坐标">
              <el-input-number
                v-model="selectedComponent.position.x"
                :min="0"
                :step="1"
                controls-position="right"
                class="w-full"
              />
            </el-form-item>
            <el-form-item label="Y坐标">
              <el-input-number
                v-model="selectedComponent.position.y"
                :min="0"
                :step="1"
                controls-position="right"
                class="w-full"
              />
            </el-form-item>
            <el-form-item label="宽度">
              <el-input-number
                v-model="selectedComponent.position.w"
                :min="20"
                :step="1"
                controls-position="right"
                class="w-full"
              />
            </el-form-item>
            <el-form-item label="高度">
              <el-input-number
                v-model="selectedComponent.position.h"
                :min="20"
                :step="1"
                controls-position="right"
                class="w-full"
              />
            </el-form-item>
            <el-form-item label="旋转">
              <el-input-number
                v-model="selectedComponent.position.rotate"
                :min="-180"
                :max="180"
                :step="1"
                controls-position="right"
                class="w-full"
              />
            </el-form-item>
            <el-form-item label="层级">
              <el-input-number
                v-model="selectedComponent.position.zIndex"
                :min="0"
                :step="1"
                controls-position="right"
                class="w-full"
              />
            </el-form-item>
          </el-form>
        </el-collapse-item>

        <!-- 样式属性 -->
        <el-collapse-item title="样式属性" name="style">
          <el-form label-width="80px" size="small">
            <el-form-item label="背景色">
              <el-color-picker v-model="selectedComponent.style.backgroundColor" />
            </el-form-item>
            <el-form-item label="边框颜色">
              <el-color-picker v-model="selectedComponent.style.borderColor" />
            </el-form-item>
            <el-form-item label="边框宽度">
              <el-input-number
                v-model="selectedComponent.style.borderWidth"
                :min="0"
                :step="1"
                controls-position="right"
                class="w-full"
              />
            </el-form-item>
            <el-form-item label="圆角">
              <el-input-number
                v-model="selectedComponent.style.borderRadius"
                :min="0"
                :step="1"
                controls-position="right"
                class="w-full"
              />
            </el-form-item>
            <el-form-item label="透明度">
              <el-slider v-model="opacityValue" :min="0" :max="100" />
            </el-form-item>
          </el-form>
        </el-collapse-item>

        <!-- 数据配置 -->
        <el-collapse-item title="数据配置" name="data">
          <el-form label-width="80px" size="small">
            <el-form-item label="数据源">
              <el-select v-model="selectedComponent.data.type" class="w-full">
                <el-option label="静态数据" value="static" />
                <el-option label="API接口" value="api" />
                <el-option label="数据库" value="database" />
              </el-select>
            </el-form-item>
            <el-form-item label="刷新间隔">
              <el-input-number
                v-model="selectedComponent.data.refresh"
                :min="0"
                :step="5"
                controls-position="right"
                class="w-full"
              />
              <span class="text-xs text-gray-400">秒(0表示不刷新)</span>
            </el-form-item>
          </el-form>
        </el-collapse-item>
      </el-collapse>
    </el-scrollbar>
  </div>

  <div v-else class="empty-panel">
    <el-empty description="请选择一个组件" />
  </div>
</template>

<script lang="ts" setup>
import { storeToRefs } from 'pinia'
import { useDashboardStore } from '@/store/modules/dashboard'

const dashboardStore = useDashboardStore()
const { selectedComponent } = storeToRefs(dashboardStore)

// 激活的折叠面板
const activeNames = ref(['position'])

// 透明度值(0-100)
const opacityValue = computed({
  get: () => {
    return selectedComponent.value
      ? (selectedComponent.value.style.opacity || 1) * 100
      : 100
  },
  set: (val) => {
    if (selectedComponent.value) {
      selectedComponent.value.style.opacity = val / 100
    }
  }
})
</script>

<style lang="scss" scoped>
.property-panel {
  display: flex;
  flex-direction: column;
  width: 320px;
  height: 100%;
  background-color: var(--el-bg-color);
  border-left: 1px solid var(--el-border-color);

  .panel-header {
    display: flex;
    align-items: center;
    gap: 8px;
    height: 48px;
    padding: 0 16px;
    border-bottom: 1px solid var(--el-border-color);

    .panel-title {
      font-size: 16px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }
  }

  .panel-content {
    flex: 1;
    padding: 16px;

    :deep(.el-collapse) {
      border: none;

      .el-collapse-item__header {
        background-color: var(--el-fill-color-light);
        padding: 0 12px;
        border-radius: 4px;
        margin-bottom: 8px;
      }

      .el-collapse-item__wrap {
        border: none;
      }

      .el-collapse-item__content {
        padding: 12px 0;
      }
    }

    :deep(.el-form-item) {
      margin-bottom: 16px;

      .el-form-item__label {
        font-size: 12px;
      }
    }
  }
}

.empty-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 320px;
  height: 100%;
  background-color: var(--el-bg-color);
  border-left: 1px solid var(--el-border-color);
}
</style>
