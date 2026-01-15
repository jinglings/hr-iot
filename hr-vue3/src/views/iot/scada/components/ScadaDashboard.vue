<template>
  <div class="scada-dashboard">
    <!-- 仪表板选择器 -->
    <div class="dashboard-selector">
      <el-select v-model="selectedDashboardId" placeholder="选择仪表板" class="dashboard-select">
        <el-option
          v-for="dashboard in dashboards"
          :key="dashboard.dashboardId"
          :label="dashboard.name"
          :value="dashboard.dashboardId"
        >
          <div class="dashboard-option">
            <Icon :icon="getDashboardIcon(dashboard.dashboardType)" class="option-icon" />
            <span>{{ dashboard.name }}</span>
            <el-tag v-if="dashboard.isDefault" size="small" type="success" class="ml-2">默认</el-tag>
          </div>
        </el-option>
      </el-select>
      <el-button type="primary" plain @click="refreshDashboard" :loading="loading">
        <Icon icon="ep:refresh" class="mr-1" />
        刷新
      </el-button>
    </div>

    <!-- FUXA 嵌入区域 -->
    <div class="fuxa-container" v-loading="loading">
      <template v-if="selectedDashboardId">
        <iframe
          ref="fuxaFrame"
          :src="fuxaUrl"
          class="fuxa-iframe"
          frameborder="0"
          allowfullscreen
        />
      </template>
      <template v-else>
        <el-empty description="请选择一个仪表板" :image-size="200">
          <template #image>
            <Icon icon="ep:data-board" class="empty-icon" />
          </template>
        </el-empty>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import type { ScadaDashboard } from '../types/scada'

defineOptions({ name: 'ScadaDashboard' })

const loading = ref(false)
const dashboards = ref<ScadaDashboard[]>([])
const selectedDashboardId = ref<string>('')
const fuxaFrame = ref<HTMLIFrameElement | null>(null)

// 计算 FUXA URL
const fuxaUrl = computed(() => {
  if (!selectedDashboardId.value) return ''
  // TODO: 使用 API 获取带认证的 URL
  return `/fuxa/view/${selectedDashboardId.value}`
})

// 获取仪表板图标
const getDashboardIcon = (type: string): string => {
  const icons: Record<string, string> = {
    water: 'ep:cold-drink',
    hvac: 'ep:wind-power',
    energy: 'ep:lightning',
    custom: 'ep:data-board'
  }
  return icons[type] || icons.custom
}

// 获取仪表板列表
const fetchDashboards = async () => {
  loading.value = true
  try {
    // TODO: 调用 API
    // dashboards.value = await ScadaApi.getDashboards()
    
    // 模拟数据
    dashboards.value = [
      {
        id: 1,
        dashboardId: 'demo-water-system',
        name: '供水系统监控',
        description: '供水系统 SCADA 仪表板',
        dashboardType: 'water',
        dashboardTypeDesc: '供水系统',
        isDefault: true,
        sortOrder: 1,
        status: 1,
        statusDesc: '启用',
        fuxaUrl: '/fuxa/view/demo-water-system'
      },
      {
        id: 2,
        dashboardId: 'demo-hvac-system',
        name: '暖通空调监控',
        description: 'HVAC 系统 SCADA 仪表板',
        dashboardType: 'hvac',
        dashboardTypeDesc: '暖通空调',
        isDefault: false,
        sortOrder: 2,
        status: 1,
        statusDesc: '启用',
        fuxaUrl: '/fuxa/view/demo-hvac-system'
      }
    ]
    
    // 自动选择默认仪表板
    const defaultDashboard = dashboards.value.find(d => d.isDefault)
    if (defaultDashboard) {
      selectedDashboardId.value = defaultDashboard.dashboardId
    } else if (dashboards.value.length > 0) {
      selectedDashboardId.value = dashboards.value[0].dashboardId
    }
  } catch (error) {
    console.error('获取仪表板列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 刷新仪表板
const refreshDashboard = () => {
  if (fuxaFrame.value) {
    fuxaFrame.value.contentWindow?.location.reload()
  }
}

onMounted(() => {
  fetchDashboards()
})
</script>

<style lang="scss" scoped>
.scada-dashboard {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.dashboard-selector {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  
  .dashboard-select {
    width: 300px;
  }
  
  .dashboard-option {
    display: flex;
    align-items: center;
    
    .option-icon {
      margin-right: 8px;
      color: var(--el-color-primary);
    }
  }
}

.fuxa-container {
  flex: 1;
  min-height: 500px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
  
  .fuxa-iframe {
    width: 100%;
    height: 100%;
    min-height: 500px;
  }
  
  .empty-icon {
    font-size: 80px;
    color: #c0c4cc;
  }
}
</style>
