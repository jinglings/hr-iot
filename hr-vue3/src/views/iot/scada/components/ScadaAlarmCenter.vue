<template>
  <div class="scada-alarm-center">
    <!-- 告警筛选 -->
    <div class="filter-bar">
      <el-select v-model="priorityFilter" placeholder="告警级别" clearable class="priority-filter">
        <el-option label="全部" value="" />
        <el-option label="紧急" :value="4">
          <span class="priority-option urgent">紧急</span>
        </el-option>
        <el-option label="高" :value="3">
          <span class="priority-option high">高</span>
        </el-option>
        <el-option label="中" :value="2">
          <span class="priority-option medium">中</span>
        </el-option>
        <el-option label="低" :value="1">
          <span class="priority-option low">低</span>
        </el-option>
      </el-select>
      <el-select v-model="statusFilter" placeholder="告警状态" clearable class="status-filter">
        <el-option label="全部" value="" />
        <el-option label="活动" :value="1" />
        <el-option label="已确认" :value="2" />
        <el-option label="已恢复" :value="3" />
        <el-option label="已关闭" :value="4" />
      </el-select>
      <el-button type="primary" @click="fetchAlarms" :loading="loading">
        <Icon icon="ep:refresh" class="mr-1" />
        刷新
      </el-button>
    </div>

    <!-- 告警表格 -->
    <el-table 
      :data="filteredAlarms" 
      v-loading="loading"
      stripe
      :row-class-name="getRowClassName"
    >
      <el-table-column type="expand">
        <template #default="{ row }">
          <div class="alarm-detail">
            <p><strong>触发值:</strong> {{ row.tagValue }}</p>
            <p><strong>消息:</strong> {{ row.message }}</p>
            <p v-if="row.notes"><strong>备注:</strong> {{ row.notes }}</p>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column prop="alarmName" label="告警名称" min-width="150" />
      
      <el-table-column prop="priority" label="级别" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getPriorityType(row.priority)" effect="dark" size="small">
            {{ row.priorityDesc }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ row.statusDesc }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="deviceName" label="设备" min-width="120" />
      
      <el-table-column prop="triggeredAt" label="触发时间" width="160" align="center">
        <template #default="{ row }">
          {{ formatDateTime(row.triggeredAt) }}
        </template>
      </el-table-column>
      
      <el-table-column prop="durationSeconds" label="持续时间" width="100" align="center">
        <template #default="{ row }">
          {{ formatDuration(row.durationSeconds) }}
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 1"
            type="warning"
            size="small"
            plain
            @click="acknowledgeAlarm(row)"
            :loading="row.acknowledging"
          >
            确认
          </el-button>
          <el-button
            v-if="row.status !== 4"
            type="danger"
            size="small"
            plain
            @click="closeAlarm(row)"
            :loading="row.closing"
          >
            关闭
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <el-empty v-if="!loading && filteredAlarms.length === 0" description="暂无告警">
      <template #image>
        <Icon icon="ep:circle-check" class="empty-icon success" />
      </template>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ScadaAlarm } from '../types/scada'

defineOptions({ name: 'ScadaAlarmCenter' })

const loading = ref(false)
const alarms = ref<(ScadaAlarm & { acknowledging?: boolean; closing?: boolean })[]>([])
const priorityFilter = ref<number | ''>('')
const statusFilter = ref<number | ''>('')

// 刷新定时器
let refreshTimer: ReturnType<typeof setInterval> | null = null

// 过滤后的告警
const filteredAlarms = computed(() => {
  return alarms.value.filter(alarm => {
    const matchPriority = priorityFilter.value === '' || alarm.priority === priorityFilter.value
    const matchStatus = statusFilter.value === '' || alarm.status === statusFilter.value
    return matchPriority && matchStatus
  })
})

// 获取告警列表
const fetchAlarms = async () => {
  loading.value = true
  try {
    // TODO: 调用 API
    // alarms.value = await ScadaApi.getActiveAlarms()
    
    // 模拟数据
    alarms.value = [
      {
        id: 1,
        alarmId: 1,
        alarmName: '水箱液位过高',
        tagId: 'tag_tank_001',
        tagValue: '92.5',
        deviceId: 2,
        deviceName: 'Tank-001',
        priority: 3,
        priorityDesc: '高',
        message: '水箱液位超过 90%，当前值: 92.5%',
        status: 1,
        statusDesc: '活动',
        triggeredAt: new Date(Date.now() - 1800000),
        durationSeconds: 1800
      },
      {
        id: 2,
        alarmId: 2,
        alarmName: '温度过高',
        tagId: 'tag_temp_001',
        tagValue: '85.3',
        deviceId: 3,
        deviceName: 'Temp-Sensor-001',
        priority: 4,
        priorityDesc: '紧急',
        message: '温度超过 80°C，当前值: 85.3°C',
        status: 2,
        statusDesc: '已确认',
        triggeredAt: new Date(Date.now() - 3600000),
        acknowledgedAt: new Date(Date.now() - 3000000),
        acknowledgedBy: 'admin',
        durationSeconds: 3600
      },
      {
        id: 3,
        alarmId: 3,
        alarmName: '压力偏低',
        tagId: 'tag_pressure_001',
        tagValue: '45.2',
        deviceId: 4,
        deviceName: 'Pressure-001',
        priority: 2,
        priorityDesc: '中',
        message: '压力低于正常范围',
        status: 3,
        statusDesc: '已恢复',
        triggeredAt: new Date(Date.now() - 7200000),
        recoveredAt: new Date(Date.now() - 3600000),
        durationSeconds: 3600
      }
    ]
  } catch (error) {
    console.error('获取告警列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取行样式类
const getRowClassName = ({ row }: { row: ScadaAlarm }) => {
  if (row.status === 1) {
    if (row.priority === 4) return 'alarm-row-urgent'
    if (row.priority === 3) return 'alarm-row-high'
  }
  return ''
}

// 获取优先级类型
const getPriorityType = (priority: number) => {
  const types: Record<number, string> = {
    1: 'info',
    2: 'warning',
    3: 'danger',
    4: 'danger'
  }
  return types[priority] || 'info'
}

// 获取状态类型
const getStatusType = (status: number) => {
  const types: Record<number, string> = {
    1: 'danger',
    2: 'warning',
    3: 'success',
    4: 'info'
  }
  return types[status] || 'info'
}

// 格式化日期时间
const formatDateTime = (date: Date) => {
  if (!date) return '-'
  const d = new Date(date)
  return d.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 格式化持续时间
const formatDuration = (seconds?: number) => {
  if (!seconds) return '-'
  if (seconds < 60) return `${seconds}秒`
  if (seconds < 3600) return `${Math.floor(seconds / 60)}分钟`
  return `${Math.floor(seconds / 3600)}小时${Math.floor((seconds % 3600) / 60)}分钟`
}

// 确认告警
const acknowledgeAlarm = async (alarm: ScadaAlarm & { acknowledging?: boolean }) => {
  try {
    await ElMessageBox.confirm(`确认告警: ${alarm.alarmName}?`, '确认告警', {
      type: 'warning'
    })
    
    alarm.acknowledging = true
    // TODO: 调用 API
    // await ScadaApi.acknowledgeAlarm(alarm.id)
    
    await new Promise(resolve => setTimeout(resolve, 500)) // 模拟
    
    ElMessage.success('告警已确认')
    await fetchAlarms()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认告警失败:', error)
    }
  } finally {
    alarm.acknowledging = false
  }
}

// 关闭告警
const closeAlarm = async (alarm: ScadaAlarm & { closing?: boolean }) => {
  try {
    const { value: notes } = await ElMessageBox.prompt('请输入关闭备注:', '关闭告警', {
      inputPlaceholder: '可选',
      type: 'warning'
    })
    
    alarm.closing = true
    // TODO: 调用 API
    // await ScadaApi.closeAlarm(alarm.id, notes)
    
    await new Promise(resolve => setTimeout(resolve, 500)) // 模拟
    
    ElMessage.success('告警已关闭')
    await fetchAlarms()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('关闭告警失败:', error)
    }
  } finally {
    alarm.closing = false
  }
}

onMounted(() => {
  fetchAlarms()
  // 每 15 秒刷新一次
  refreshTimer = setInterval(fetchAlarms, 15000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style lang="scss" scoped>
.scada-alarm-center {
  .filter-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
    
    .priority-filter,
    .status-filter {
      width: 150px;
    }
  }
  
  .priority-option {
    &.urgent { color: #f56c6c; font-weight: bold; }
    &.high { color: #e6a23c; }
    &.medium { color: #909399; }
    &.low { color: #67c23a; }
  }
  
  .alarm-detail {
    padding: 12px 20px;
    color: #606266;
    
    p {
      margin: 4px 0;
    }
  }
  
  .empty-icon {
    font-size: 80px;
    
    &.success {
      color: #67c23a;
    }
  }
  
  :deep(.alarm-row-urgent) {
    background-color: rgba(245, 108, 108, 0.1) !important;
    animation: blink 2s infinite;
  }
  
  :deep(.alarm-row-high) {
    background-color: rgba(230, 162, 60, 0.1) !important;
  }
}

@keyframes blink {
  0%, 100% { background-color: rgba(245, 108, 108, 0.1); }
  50% { background-color: rgba(245, 108, 108, 0.2); }
}
</style>
