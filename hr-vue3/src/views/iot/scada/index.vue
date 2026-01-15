<template>
  <div class="scada-container">
    <!-- 页面头部 -->
    <div class="scada-header">
      <div class="header-left">
        <h2 class="page-title">
          <Icon icon="ep:monitor" class="title-icon" />
          SCADA 可视化监控
        </h2>
        <p class="page-desc">实时监控设备状态，支持远程控制和告警管理</p>
      </div>
      <div class="header-right">
        <el-button-group>
          <el-button 
            :type="activeTab === 'dashboard' ? 'primary' : 'default'"
            @click="activeTab = 'dashboard'"
          >
            <Icon icon="ep:data-board" class="mr-1" />
            仪表板
          </el-button>
          <el-button 
            :type="activeTab === 'devices' ? 'primary' : 'default'"
            @click="activeTab = 'devices'"
          >
            <Icon icon="ep:cpu" class="mr-1" />
            设备监控
          </el-button>
          <el-button 
            :type="activeTab === 'alarms' ? 'primary' : 'default'"
            @click="activeTab = 'alarms'"
          >
            <Icon icon="ep:bell" class="mr-1" />
            告警中心
            <el-badge v-if="activeAlarmCount > 0" :value="activeAlarmCount" class="ml-1" />
          </el-button>
        </el-button-group>
        <el-button type="success" plain @click="openFuxaDashboard" class="ml-4">
          <Icon icon="ep:full-screen" class="mr-1" />
          全屏模式
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <div class="stat-card online">
          <div class="stat-icon">
            <Icon icon="ep:circle-check" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.onlineCount }}</div>
            <div class="stat-label">在线设备</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <div class="stat-card offline">
          <div class="stat-icon">
            <Icon icon="ep:circle-close" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.offlineCount }}</div>
            <div class="stat-label">离线设备</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <div class="stat-card alarm">
          <div class="stat-icon">
            <Icon icon="ep:warning" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.alarmCount }}</div>
            <div class="stat-label">活动告警</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <div class="stat-card control">
          <div class="stat-icon">
            <Icon icon="ep:operation" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.controlCount }}</div>
            <div class="stat-label">今日控制</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 内容区域 -->
    <div class="scada-content">
      <!-- 仪表板标签页 -->
      <template v-if="activeTab === 'dashboard'">
        <ScadaDashboard />
      </template>

      <!-- 设备监控标签页 -->
      <template v-else-if="activeTab === 'devices'">
        <ScadaDeviceMonitor />
      </template>

      <!-- 告警中心标签页 -->
      <template v-else-if="activeTab === 'alarms'">
        <ScadaAlarmCenter />
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import ScadaDashboard from './components/ScadaDashboard.vue'
import ScadaDeviceMonitor from './components/ScadaDeviceMonitor.vue'
import ScadaAlarmCenter from './components/ScadaAlarmCenter.vue'

defineOptions({ name: 'IoTScada' })

// 当前激活的标签页
const activeTab = ref<'dashboard' | 'devices' | 'alarms'>('dashboard')

// 活动告警数量
const activeAlarmCount = ref(0)

// 统计数据
const stats = reactive({
  onlineCount: 0,
  offlineCount: 0,
  alarmCount: 0,
  controlCount: 0
})

// 刷新定时器
let refreshTimer: ReturnType<typeof setInterval> | null = null

// 获取统计数据
const fetchStats = async () => {
  try {
    // TODO: 调用 API 获取统计数据
    // const data = await ScadaApi.getStats()
    // stats.onlineCount = data.onlineCount
    // ...
    
    // 模拟数据
    stats.onlineCount = 42
    stats.offlineCount = 8
    stats.alarmCount = 3
    stats.controlCount = 127
    activeAlarmCount.value = 3
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 打开 FUXA 全屏仪表板
const openFuxaDashboard = () => {
  // TODO: 获取 FUXA URL 并打开
  window.open('/fuxa/', '_blank')
}

// 初始化
onMounted(() => {
  fetchStats()
  // 每 30 秒刷新一次统计数据
  refreshTimer = setInterval(fetchStats, 30000)
})

// 清理
onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style lang="scss" scoped>
.scada-container {
  padding: 16px;
  min-height: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
}

.scada-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
  
  .header-left {
    .page-title {
      display: flex;
      align-items: center;
      margin: 0 0 8px 0;
      color: #fff;
      font-size: 24px;
      font-weight: 600;
      
      .title-icon {
        margin-right: 10px;
        font-size: 28px;
      }
    }
    
    .page-desc {
      margin: 0;
      color: rgba(255, 255, 255, 0.8);
      font-size: 14px;
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
  }
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }
  
  .stat-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 56px;
    height: 56px;
    border-radius: 12px;
    font-size: 28px;
    margin-right: 16px;
  }
  
  .stat-content {
    .stat-value {
      font-size: 28px;
      font-weight: 700;
      line-height: 1.2;
    }
    
    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-top: 4px;
    }
  }
  
  &.online {
    .stat-icon {
      background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
      color: #fff;
    }
    .stat-value {
      color: #67c23a;
    }
  }
  
  &.offline {
    .stat-icon {
      background: linear-gradient(135deg, #909399 0%, #c0c4cc 100%);
      color: #fff;
    }
    .stat-value {
      color: #909399;
    }
  }
  
  &.alarm {
    .stat-icon {
      background: linear-gradient(135deg, #f56c6c 0%, #fab6b6 100%);
      color: #fff;
    }
    .stat-value {
      color: #f56c6c;
    }
  }
  
  &.control {
    .stat-icon {
      background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
      color: #fff;
    }
    .stat-value {
      color: #409eff;
    }
  }
}

.scada-content {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  min-height: 500px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}
</style>
