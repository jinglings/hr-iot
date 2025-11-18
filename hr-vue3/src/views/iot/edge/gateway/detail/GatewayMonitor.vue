<template>
  <div>
    <!-- 实时状态概览 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="CPU使用率" :value="currentStatus.cpuUsage || 0" suffix="%">
            <template #prefix>
              <Icon icon="ep:cpu" />
            </template>
          </el-statistic>
          <el-progress
            :percentage="currentStatus.cpuUsage || 0"
            :color="getProgressColor(currentStatus.cpuUsage)"
            class="mt-2"
          />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="内存使用率" :value="currentStatus.memoryUsage || 0" suffix="%">
            <template #prefix>
              <Icon icon="ep:memo" />
            </template>
          </el-statistic>
          <el-progress
            :percentage="currentStatus.memoryUsage || 0"
            :color="getProgressColor(currentStatus.memoryUsage)"
            class="mt-2"
          />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="磁盘使用率" :value="currentStatus.diskUsage || 0" suffix="%">
            <template #prefix>
              <Icon icon="ep:files" />
            </template>
          </el-statistic>
          <el-progress
            :percentage="currentStatus.diskUsage || 0"
            :color="getProgressColor(currentStatus.diskUsage)"
            class="mt-2"
          />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="网关状态" :value="getStatusLabel(currentStatus.status)">
            <template #prefix>
              <Icon icon="ep:connection" />
            </template>
          </el-statistic>
          <div class="mt-2 text-sm text-gray-500">
            最后心跳: {{ formatLastHeartbeat(currentStatus.lastHeartbeatTime) }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 历史趋势图表 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card header="CPU使用率趋势（最近1小时）">
          <div ref="cpuChartRef" style="width: 100%; height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-4">
      <el-col :span="12">
        <el-card header="内存使用率趋势（最近1小时）">
          <div ref="memoryChartRef" style="width: 100%; height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="磁盘使用率趋势（最近1小时）">
          <div ref="diskChartRef" style="width: 100%; height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import { EdgeGatewayStatusApi, EdgeGatewayStatusVO } from '@/api/iot/edge/gatewaystatus'
import { formatDate } from '@/utils/formatTime'

const props = defineProps<{
  gatewayId: number
}>()

const message = useMessage()

const currentStatus = ref<EdgeGatewayStatusVO>({} as EdgeGatewayStatusVO)
const cpuChartRef = ref<HTMLDivElement>()
const memoryChartRef = ref<HTMLDivElement>()
const diskChartRef = ref<HTMLDivElement>()

let cpuChart: echarts.ECharts | null = null
let memoryChart: echarts.ECharts | null = null
let diskChart: echarts.ECharts | null = null
let refreshTimer: any = null

// 历史数据（用于图表）
const historyData = ref({
  times: [] as string[],
  cpu: [] as number[],
  memory: [] as number[],
  disk: [] as number[]
})

/** 获取进度条颜色 */
const getProgressColor = (value: number) => {
  if (value >= 90) return '#F56C6C'
  if (value >= 70) return '#E6A23C'
  return '#67C23A'
}

/** 获取状态标签 */
const getStatusLabel = (status: number) => {
  const statusMap = {
    0: '离线',
    1: '在线',
    2: '异常'
  }
  return statusMap[status] || '未知'
}

/** 格式化最后心跳时间 */
const formatLastHeartbeat = (time: Date) => {
  if (!time) return '-'
  return formatDate(time)
}

/** 获取当前网关状态 */
const getCurrentStatus = async () => {
  try {
    const status = await EdgeGatewayStatusApi.getByGatewayId(props.gatewayId)
    currentStatus.value = status

    // 更新历史数据
    const now = new Date()
    const timeStr = `${now.getHours()}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`

    historyData.value.times.push(timeStr)
    historyData.value.cpu.push(status.cpuUsage || 0)
    historyData.value.memory.push(status.memoryUsage || 0)
    historyData.value.disk.push(status.diskUsage || 0)

    // 保持最近60个数据点（1小时，每分钟一个）
    if (historyData.value.times.length > 60) {
      historyData.value.times.shift()
      historyData.value.cpu.shift()
      historyData.value.memory.shift()
      historyData.value.disk.shift()
    }

    // 更新图表
    updateCharts()
  } catch (error) {
    console.error('获取网关状态失败:', error)
  }
}

/** 初始化图表 */
const initCharts = () => {
  if (cpuChartRef.value && !cpuChart) {
    cpuChart = echarts.init(cpuChartRef.value)
  }
  if (memoryChartRef.value && !memoryChart) {
    memoryChart = echarts.init(memoryChartRef.value)
  }
  if (diskChartRef.value && !diskChart) {
    diskChart = echarts.init(diskChartRef.value)
  }

  // 设置初始配置
  const baseOption = {
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: []
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      }
    }
  }

  cpuChart?.setOption({
    ...baseOption,
    series: [
      {
        name: 'CPU使用率',
        type: 'line',
        smooth: true,
        data: [],
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        },
        lineStyle: {
          color: '#409EFF'
        }
      }
    ]
  })

  memoryChart?.setOption({
    ...baseOption,
    series: [
      {
        name: '内存使用率',
        type: 'line',
        smooth: true,
        data: [],
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
          ])
        },
        lineStyle: {
          color: '#67C23A'
        }
      }
    ]
  })

  diskChart?.setOption({
    ...baseOption,
    series: [
      {
        name: '磁盘使用率',
        type: 'line',
        smooth: true,
        data: [],
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(230, 162, 60, 0.3)' },
            { offset: 1, color: 'rgba(230, 162, 60, 0.05)' }
          ])
        },
        lineStyle: {
          color: '#E6A23C'
        }
      }
    ]
  })
}

/** 更新图表数据 */
const updateCharts = () => {
  cpuChart?.setOption({
    xAxis: {
      data: historyData.value.times
    },
    series: [
      {
        data: historyData.value.cpu
      }
    ]
  })

  memoryChart?.setOption({
    xAxis: {
      data: historyData.value.times
    },
    series: [
      {
        data: historyData.value.memory
      }
    ]
  })

  diskChart?.setOption({
    xAxis: {
      data: historyData.value.times
    },
    series: [
      {
        data: historyData.value.disk
      }
    ]
  })
}

/** 窗口大小变化时重绘图表 */
const handleResize = () => {
  cpuChart?.resize()
  memoryChart?.resize()
  diskChart?.resize()
}

/** 初始化 */
onMounted(async () => {
  await nextTick()
  initCharts()
  await getCurrentStatus()

  // 每5秒刷新一次数据
  refreshTimer = setInterval(getCurrentStatus, 5000)

  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

/** 清理 */
onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  window.removeEventListener('resize', handleResize)
  cpuChart?.dispose()
  memoryChart?.dispose()
  diskChart?.dispose()
})
</script>
