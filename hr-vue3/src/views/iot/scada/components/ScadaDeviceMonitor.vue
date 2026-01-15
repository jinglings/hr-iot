<template>
  <div class="scada-device-monitor">
    <!-- 设备筛选 -->
    <div class="filter-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索设备名称"
        prefix-icon="Search"
        clearable
        class="search-input"
        @input="handleSearch"
      />
      <el-select v-model="stateFilter" placeholder="设备状态" clearable class="state-filter">
        <el-option label="全部" value="" />
        <el-option label="在线" :value="1" />
        <el-option label="离线" :value="2" />
      </el-select>
      <el-button type="primary" @click="fetchDevices" :loading="loading">
        <Icon icon="ep:refresh" class="mr-1" />
        刷新
      </el-button>
    </div>

    <!-- 设备卡片列表 -->
    <el-row :gutter="16" v-loading="loading">
      <el-col 
        v-for="device in filteredDevices" 
        :key="device.deviceId" 
        :xs="24" :sm="12" :md="8" :lg="6"
        class="device-col"
      >
        <div class="device-card" :class="{ online: device.state === 1, offline: device.state !== 1 }">
          <!-- 状态指示条 -->
          <div class="status-bar" :class="device.state === 1 ? 'online' : 'offline'" />
          
          <!-- 设备头部 -->
          <div class="device-header">
            <div class="device-name">{{ device.deviceName }}</div>
            <el-tag :type="device.state === 1 ? 'success' : 'info'" size="small">
              {{ device.stateDesc }}
            </el-tag>
          </div>
          
          <!-- 设备信息 -->
          <div class="device-info">
            <div class="info-item">
              <span class="label">产品:</span>
              <span class="value">{{ device.productName || '未知' }}</span>
            </div>
            <div class="info-item">
              <span class="label">别名:</span>
              <span class="value">{{ device.nickname || device.deviceName }}</span>
            </div>
          </div>
          
          <!-- 实时数据 -->
          <div class="realtime-data" v-if="device.properties">
            <div class="data-title">实时数据</div>
            <div class="data-grid">
              <div 
                v-for="(value, key) in getLimitedProperties(device.properties)" 
                :key="key"
                class="data-item"
              >
                <div class="data-value">{{ formatValue(value) }}</div>
                <div class="data-label">{{ key }}</div>
              </div>
            </div>
          </div>
          
          <!-- 告警指示 -->
          <div class="alarm-indicator" v-if="device.hasActiveAlarm">
            <Icon icon="ep:warning-filled" class="alarm-icon" />
            <span>{{ device.activeAlarmCount }} 个活动告警</span>
          </div>
          
          <!-- 操作按钮 -->
          <div class="device-actions">
            <el-button size="small" type="primary" plain @click="showDeviceDetail(device)">
              <Icon icon="ep:view" class="mr-1" />
              详情
            </el-button>
            <el-button 
              size="small" 
              type="warning" 
              plain 
              :disabled="device.state !== 1"
              @click="showControlPanel(device)"
            >
              <Icon icon="ep:operation" class="mr-1" />
              控制
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <el-empty v-if="!loading && filteredDevices.length === 0" description="暂无设备数据" />

    <!-- 设备详情抽屉 -->
    <el-drawer
      v-model="detailDrawerVisible"
      title="设备详情"
      size="40%"
    >
      <template v-if="selectedDevice">
        <DeviceDetailPanel :device="selectedDevice" />
      </template>
    </el-drawer>

    <!-- 控制面板对话框 -->
    <el-dialog
      v-model="controlDialogVisible"
      title="设备控制"
      width="500px"
    >
      <template v-if="selectedDevice">
        <DeviceControlPanel 
          :device="selectedDevice" 
          @command-sent="handleCommandSent"
        />
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import type { ScadaDevice } from '../types/scada'
import DeviceDetailPanel from './DeviceDetailPanel.vue'
import DeviceControlPanel from './DeviceControlPanel.vue'

defineOptions({ name: 'ScadaDeviceMonitor' })

const loading = ref(false)
const devices = ref<ScadaDevice[]>([])
const searchKeyword = ref('')
const stateFilter = ref<number | ''>('')
const detailDrawerVisible = ref(false)
const controlDialogVisible = ref(false)
const selectedDevice = ref<ScadaDevice | null>(null)

// 刷新定时器
let refreshTimer: ReturnType<typeof setInterval> | null = null

// 过滤后的设备
const filteredDevices = computed(() => {
  return devices.value.filter(device => {
    const matchKeyword = !searchKeyword.value || 
      device.deviceName.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      device.nickname?.toLowerCase().includes(searchKeyword.value.toLowerCase())
    const matchState = stateFilter.value === '' || device.state === stateFilter.value
    return matchKeyword && matchState
  })
})

// 获取设备列表
const fetchDevices = async () => {
  loading.value = true
  try {
    // TODO: 调用 API
    // devices.value = await ScadaApi.getDevices()
    
    // 模拟数据
    devices.value = [
      {
        deviceId: 1,
        deviceName: 'Pump-001',
        nickname: '1号水泵',
        productId: 1,
        productName: '智能水泵',
        productKey: 'pump-xxx',
        deviceType: 1,
        state: 1,
        stateDesc: '在线',
        controllable: true,
        properties: { status: true, speed: 1200, power: 5.5 },
        activeAlarmCount: 0,
        hasActiveAlarm: false
      },
      {
        deviceId: 2,
        deviceName: 'Sensor-001',
        nickname: '液位传感器1',
        productId: 2,
        productName: '液位传感器',
        productKey: 'level-xxx',
        deviceType: 1,
        state: 1,
        stateDesc: '在线',
        controllable: false,
        properties: { level: 75.5, temperature: 25.3 },
        activeAlarmCount: 1,
        hasActiveAlarm: true
      },
      {
        deviceId: 3,
        deviceName: 'Valve-001',
        nickname: '进水阀门',
        productId: 3,
        productName: '电动阀门',
        productKey: 'valve-xxx',
        deviceType: 1,
        state: 2,
        stateDesc: '离线',
        controllable: true,
        properties: { position: 100, status: 'open' },
        activeAlarmCount: 0,
        hasActiveAlarm: false
      }
    ]
  } catch (error) {
    console.error('获取设备列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  // 已通过 computed 实现过滤
}

// 格式化值
const formatValue = (value: any): string => {
  if (typeof value === 'boolean') return value ? '开' : '关'
  if (typeof value === 'number') return value.toFixed(1)
  return String(value)
}

// 获取有限的属性（最多显示4个）
const getLimitedProperties = (properties: Record<string, any>): Record<string, any> => {
  const entries = Object.entries(properties).slice(0, 4)
  return Object.fromEntries(entries)
}

// 显示设备详情
const showDeviceDetail = (device: ScadaDevice) => {
  selectedDevice.value = device
  detailDrawerVisible.value = true
}

// 显示控制面板
const showControlPanel = (device: ScadaDevice) => {
  selectedDevice.value = device
  controlDialogVisible.value = true
}

// 处理命令发送
const handleCommandSent = () => {
  controlDialogVisible.value = false
  fetchDevices()
}

onMounted(() => {
  fetchDevices()
  // 每 10 秒刷新一次
  refreshTimer = setInterval(fetchDevices, 10000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style lang="scss" scoped>
.scada-device-monitor {
  .filter-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
    
    .search-input {
      width: 250px;
    }
    
    .state-filter {
      width: 150px;
    }
  }
  
  .device-col {
    margin-bottom: 16px;
  }
  
  .device-card {
    background: #fff;
    border-radius: 12px;
    padding: 16px;
    border: 1px solid #ebeef5;
    position: relative;
    overflow: hidden;
    transition: all 0.3s ease;
    
    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
      transform: translateY(-2px);
    }
    
    .status-bar {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 4px;
      
      &.online {
        background: linear-gradient(90deg, #67c23a, #95d475);
      }
      
      &.offline {
        background: linear-gradient(90deg, #909399, #c0c4cc);
      }
    }
    
    .device-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      
      .device-name {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
    }
    
    .device-info {
      margin-bottom: 12px;
      
      .info-item {
        font-size: 13px;
        color: #606266;
        margin-bottom: 4px;
        
        .label {
          color: #909399;
          margin-right: 8px;
        }
      }
    }
    
    .realtime-data {
      background: #f5f7fa;
      border-radius: 8px;
      padding: 12px;
      margin-bottom: 12px;
      
      .data-title {
        font-size: 12px;
        color: #909399;
        margin-bottom: 8px;
      }
      
      .data-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 8px;
      }
      
      .data-item {
        text-align: center;
        
        .data-value {
          font-size: 16px;
          font-weight: 600;
          color: #409eff;
        }
        
        .data-label {
          font-size: 11px;
          color: #909399;
        }
      }
    }
    
    .alarm-indicator {
      display: flex;
      align-items: center;
      color: #f56c6c;
      font-size: 12px;
      margin-bottom: 12px;
      
      .alarm-icon {
        margin-right: 4px;
        animation: pulse 2s infinite;
      }
    }
    
    .device-actions {
      display: flex;
      gap: 8px;
      
      .el-button {
        flex: 1;
      }
    }
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
