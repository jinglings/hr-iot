<template>
  <div class="dashboard-selector">
    <!-- 下拉选择模式 -->
    <template v-if="mode === 'dropdown'">
      <el-select
        v-model="selectedId"
        :placeholder="placeholder"
        :loading="loading"
        :filterable="filterable"
        :clearable="clearable"
        :disabled="disabled"
        class="dashboard-dropdown"
        @change="handleChange"
      >
        <template #prefix>
          <Icon icon="ep:data-board" class="prefix-icon" />
        </template>
        <el-option
          v-for="dashboard in dashboards"
          :key="dashboard.dashboardId"
          :label="dashboard.name"
          :value="dashboard.dashboardId"
        >
          <div class="dashboard-option">
            <div class="option-left">
              <Icon :icon="getDashboardIcon(dashboard.dashboardType)" class="option-icon" />
              <span class="option-name">{{ dashboard.name }}</span>
            </div>
            <div class="option-right">
              <el-tag v-if="dashboard.isDefault" type="success" size="small">默认</el-tag>
              <el-tag :type="getTypeTagColor(dashboard.dashboardType)" size="small">
                {{ dashboard.dashboardTypeDesc || dashboard.dashboardType }}
              </el-tag>
            </div>
          </div>
        </el-option>
      </el-select>
    </template>

    <!-- 标签页模式 -->
    <template v-else-if="mode === 'tabs'">
      <div class="dashboard-tabs" :class="{ 'has-scroll': hasScroll }">
        <el-button-group>
          <el-button
            v-for="dashboard in dashboards"
            :key="dashboard.dashboardId"
            :type="selectedId === dashboard.dashboardId ? 'primary' : 'default'"
            @click="selectDashboard(dashboard.dashboardId)"
          >
            <Icon :icon="getDashboardIcon(dashboard.dashboardType)" class="tab-icon" />
            <span class="tab-name">{{ dashboard.name }}</span>
            <el-badge 
              v-if="dashboard.isDefault" 
              value="默认" 
              type="success" 
              class="default-badge"
            />
          </el-button>
        </el-button-group>
      </div>
    </template>

    <!-- 卡片模式 -->
    <template v-else-if="mode === 'cards'">
      <div class="dashboard-cards">
        <div
          v-for="dashboard in dashboards"
          :key="dashboard.dashboardId"
          class="dashboard-card"
          :class="{ active: selectedId === dashboard.dashboardId }"
          @click="selectDashboard(dashboard.dashboardId)"
        >
          <div class="card-thumbnail">
            <img 
              v-if="dashboard.thumbnailUrl" 
              :src="dashboard.thumbnailUrl" 
              :alt="dashboard.name"
            />
            <div v-else class="card-placeholder">
              <Icon :icon="getDashboardIcon(dashboard.dashboardType)" />
            </div>
            <div class="card-overlay">
              <Icon icon="ep:view" />
            </div>
          </div>
          <div class="card-info">
            <div class="card-name">{{ dashboard.name }}</div>
            <div class="card-meta">
              <el-tag :type="getTypeTagColor(dashboard.dashboardType)" size="small">
                {{ dashboard.dashboardTypeDesc || dashboard.dashboardType }}
              </el-tag>
              <el-tag v-if="dashboard.isDefault" type="success" size="small">默认</el-tag>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 刷新按钮 -->
    <el-button 
      v-if="showRefresh"
      :icon="Refresh"
      circle
      size="small"
      @click="fetchDashboards"
      :loading="loading"
      class="refresh-btn"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ScadaApi, type ScadaDashboardVO } from '@/api/iot/scada'

defineOptions({ name: 'DashboardSelector' })

// Props
const props = withDefaults(defineProps<{
  /** 选中的仪表板 ID */
  modelValue?: string
  /** 显示模式: dropdown | tabs | cards */
  mode?: 'dropdown' | 'tabs' | 'cards'
  /** 占位符文本 */
  placeholder?: string
  /** 是否可筛选 */
  filterable?: boolean
  /** 是否可清除 */
  clearable?: boolean
  /** 是否禁用 */
  disabled?: boolean
  /** 是否显示刷新按钮 */
  showRefresh?: boolean
  /** 是否自动加载 */
  autoLoad?: boolean
  /** 是否自动选择默认仪表板 */
  autoSelectDefault?: boolean
}>(), {
  mode: 'dropdown',
  placeholder: '选择仪表板',
  filterable: true,
  clearable: false,
  disabled: false,
  showRefresh: true,
  autoLoad: true,
  autoSelectDefault: true
})

// Emits
const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'change', dashboard: ScadaDashboardVO | undefined): void
  (e: 'load', dashboards: ScadaDashboardVO[]): void
}>()

// State
const loading = ref(false)
const dashboards = ref<ScadaDashboardVO[]>([])
const selectedId = ref(props.modelValue || '')

// 是否有滚动（标签页模式）
const hasScroll = computed(() => dashboards.value.length > 5)

// 获取仪表板图标
const getDashboardIcon = (type: string): string => {
  const icons: Record<string, string> = {
    water: 'ep:cold-drink',
    hvac: 'ep:wind-power',
    energy: 'ep:lightning',
    security: 'ep:lock',
    production: 'ep:setting',
    custom: 'ep:data-board'
  }
  return icons[type] || icons.custom
}

// 获取类型标签颜色
const getTypeTagColor = (type: string): string => {
  const colors: Record<string, string> = {
    water: 'primary',
    hvac: 'success',
    energy: 'warning',
    security: 'danger',
    production: 'info',
    custom: ''
  }
  return colors[type] || ''
}

// 获取仪表板列表
const fetchDashboards = async () => {
  loading.value = true
  try {
    dashboards.value = await ScadaApi.getDashboards()
    emit('load', dashboards.value)
    
    // 自动选择默认仪表板
    if (props.autoSelectDefault && !selectedId.value && dashboards.value.length > 0) {
      const defaultDashboard = dashboards.value.find(d => d.isDefault)
      if (defaultDashboard) {
        selectDashboard(defaultDashboard.dashboardId)
      } else {
        selectDashboard(dashboards.value[0].dashboardId)
      }
    }
  } catch (error) {
    console.error('获取仪表板列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 选择仪表板
const selectDashboard = (dashboardId: string) => {
  selectedId.value = dashboardId
  emit('update:modelValue', dashboardId)
  
  const dashboard = dashboards.value.find(d => d.dashboardId === dashboardId)
  emit('change', dashboard)
}

// 处理下拉变化
const handleChange = (value: string) => {
  const dashboard = dashboards.value.find(d => d.dashboardId === value)
  emit('change', dashboard)
}

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  if (newVal !== selectedId.value) {
    selectedId.value = newVal || ''
  }
})

// 初始化
onMounted(() => {
  if (props.autoLoad) {
    fetchDashboards()
  }
})

// 导出方法
defineExpose({
  refresh: fetchDashboards,
  dashboards
})
</script>

<style lang="scss" scoped>
.dashboard-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dashboard-dropdown {
  min-width: 250px;
  
  .prefix-icon {
    color: var(--el-color-primary);
  }
}

.dashboard-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  
  .option-left {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .option-icon {
      color: var(--el-color-primary);
      font-size: 16px;
    }
    
    .option-name {
      font-size: 14px;
    }
  }
  
  .option-right {
    display: flex;
    gap: 4px;
  }
}

.dashboard-tabs {
  display: flex;
  overflow-x: auto;
  
  &.has-scroll {
    max-width: 600px;
    
    &::-webkit-scrollbar {
      height: 4px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: #c0c4cc;
      border-radius: 2px;
    }
  }
  
  .tab-icon {
    margin-right: 4px;
  }
  
  .tab-name {
    max-width: 100px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.dashboard-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  
  .dashboard-card {
    background: #fff;
    border: 2px solid #e4e7ed;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      border-color: var(--el-color-primary-light-3);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      
      .card-overlay {
        opacity: 1;
      }
    }
    
    &.active {
      border-color: var(--el-color-primary);
      box-shadow: 0 0 0 2px var(--el-color-primary-light-5);
    }
    
    .card-thumbnail {
      position: relative;
      height: 120px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
      
      .card-placeholder {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 100%;
        color: rgba(255, 255, 255, 0.6);
        font-size: 48px;
      }
      
      .card-overlay {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        background: rgba(0, 0, 0, 0.4);
        color: #fff;
        font-size: 32px;
        opacity: 0;
        transition: opacity 0.3s ease;
      }
    }
    
    .card-info {
      padding: 12px;
      
      .card-name {
        font-size: 14px;
        font-weight: 500;
        color: #303133;
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .card-meta {
        display: flex;
        gap: 4px;
        flex-wrap: wrap;
      }
    }
  }
}

.refresh-btn {
  flex-shrink: 0;
}
</style>
