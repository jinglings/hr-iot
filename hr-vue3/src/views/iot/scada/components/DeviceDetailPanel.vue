<template>
  <div class="device-detail-panel">
    <el-descriptions :column="1" border>
      <el-descriptions-item label="设备名称">{{ device.deviceName }}</el-descriptions-item>
      <el-descriptions-item label="备注名称">{{ device.nickname || '-' }}</el-descriptions-item>
      <el-descriptions-item label="所属产品">{{ device.productName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="设备状态">
        <el-tag :type="device.state === 1 ? 'success' : 'info'">
          {{ device.stateDesc }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="最后在线" v-if="device.lastOnlineTime">
        {{ formatDateTime(device.lastOnlineTime) }}
      </el-descriptions-item>
    </el-descriptions>

    <div class="section-title">实时数据</div>
    <el-table :data="propertiesList" stripe size="small">
      <el-table-column prop="key" label="属性" />
      <el-table-column prop="value" label="值">
        <template #default="{ row }">
          <span class="property-value">{{ formatValue(row.value) }}</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ScadaDevice } from '../types/scada'

defineOptions({ name: 'DeviceDetailPanel' })

const props = defineProps<{
  device: ScadaDevice
}>()

// 属性列表
const propertiesList = computed(() => {
  if (!props.device.properties) return []
  return Object.entries(props.device.properties).map(([key, value]) => ({
    key,
    value
  }))
})

// 格式化值
const formatValue = (value: any): string => {
  if (typeof value === 'boolean') return value ? '开' : '关'
  if (typeof value === 'number') return value.toFixed(2)
  return String(value)
}

// 格式化日期时间
const formatDateTime = (date: Date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}
</script>

<style lang="scss" scoped>
.device-detail-panel {
  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin: 20px 0 12px;
    padding-left: 8px;
    border-left: 3px solid var(--el-color-primary);
  }
  
  .property-value {
    font-family: 'Monaco', 'Consolas', monospace;
    color: var(--el-color-primary);
  }
}
</style>
