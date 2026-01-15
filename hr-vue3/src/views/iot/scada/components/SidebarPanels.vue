<template>
  <div class="sidebar-panels">
    <!-- 历史曲线面板 -->
    <div class="panel history-curve-panel">
      <div class="panel-header">
        <h3 class="panel-title">
          <Icon icon="ep:data-line" class="title-icon" />
          历史曲线
        </h3>
        <div class="panel-actions">
          <el-select v-model="timeRange" size="small" class="time-select">
            <el-option label="1小时" value="1h" />
            <el-option label="6小时" value="6h" />
            <el-option label="24小时" value="24h" />
            <el-option label="7天" value="7d" />
          </el-select>
        </div>
      </div>
      <div class="panel-content">
        <div ref="historyChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 能耗排名面板 -->
    <div class="panel energy-ranking-panel">
      <div class="panel-header">
        <h3 class="panel-title">
          <Icon icon="ep:histogram" class="title-icon" />
          能耗排名
        </h3>
      </div>
      <div class="panel-content">
        <div ref="energyChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 故障统计面板 -->
    <div class="panel fault-stats-panel">
      <div class="panel-header">
        <h3 class="panel-title">
          <Icon icon="ep:warning" class="title-icon" />
          故障统计
        </h3>
        <div class="panel-actions">
          <el-tag type="danger" size="small">{{ totalFaults }} 项</el-tag>
        </div>
      </div>
      <div class="panel-content">
        <div ref="faultChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 数据报表面板 -->
    <div class="panel data-report-panel">
      <div class="panel-header">
        <h3 class="panel-title">
          <Icon icon="ep:document" class="title-icon" />
          数据报表
        </h3>
        <div class="panel-actions">
          <el-button type="primary" size="small" text @click="exportReport">
            <Icon icon="ep:download" />
          </el-button>
        </div>
      </div>
      <div class="panel-content">
        <div class="report-stats">
          <div class="stat-item">
            <div class="stat-value">{{ reportData.totalFlow }}</div>
            <div class="stat-label">今日用水 (m³)</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ reportData.totalEnergy }}</div>
            <div class="stat-label">今日用电 (kWh)</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ reportData.avgPressure }}</div>
            <div class="stat-label">平均压力 (MPa)</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ reportData.avgTemp }}</div>
            <div class="stat-label">平均温度 (℃)</div>
          </div>
        </div>
        <div ref="reportChartRef" class="chart-container mini"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'SidebarPanels' })

// Props
const props = withDefaults(defineProps<{
  /** 压力数据 */
  pressureData?: number[]
  /** 流量数据 */
  flowData?: number[]
  /** 温度数据 */
  temperatureData?: number[]
  /** 能耗数据 */
  energyData?: { water: number; electricity: number; gas: number }
  /** 故障数据 */
  faultData?: { name: string; count: number }[]
  /** 报表数据 */
  reportData?: { totalFlow: number; totalEnergy: number; avgPressure: number; avgTemp: number }
  /** 刷新间隔 (ms) */
  refreshInterval?: number
}>(), {
  pressureData: () => [],
  flowData: () => [],
  temperatureData: () => [],
  energyData: () => ({ water: 0, electricity: 0, gas: 0 }),
  faultData: () => [],
  reportData: () => ({ totalFlow: 0, totalEnergy: 0, avgPressure: 0, avgTemp: 0 }),
  refreshInterval: 5000
})

// Refs
const historyChartRef = ref<HTMLElement | null>(null)
const energyChartRef = ref<HTMLElement | null>(null)
const faultChartRef = ref<HTMLElement | null>(null)
const reportChartRef = ref<HTMLElement | null>(null)

// State
const timeRange = ref('1h')
let historyChart: echarts.ECharts | null = null
let energyChart: echarts.ECharts | null = null
let faultChart: echarts.ECharts | null = null
let reportChart: echarts.ECharts | null = null
let refreshTimer: ReturnType<typeof setInterval> | null = null

// Computed
const totalFaults = computed(() => {
  return props.faultData.reduce((sum, item) => sum + item.count, 0)
})

// 初始化历史曲线图
const initHistoryChart = () => {
  if (!historyChartRef.value) return
  
  historyChart = echarts.init(historyChartRef.value)
  
  const option: echarts.EChartsOption = {
    backgroundColor: 'transparent',
    grid: {
      left: 40,
      right: 10,
      top: 30,
      bottom: 25
    },
    legend: {
      data: ['压力', '流量', '温度'],
      textStyle: { color: '#a0aec0', fontSize: 10 },
      top: 0,
      itemWidth: 12,
      itemHeight: 8
    },
    xAxis: {
      type: 'category',
      data: generateTimeLabels(),
      axisLine: { lineStyle: { color: '#4a5568' } },
      axisLabel: { color: '#a0aec0', fontSize: 9 },
      splitLine: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#a0aec0', fontSize: 9 },
      splitLine: { lineStyle: { color: '#2d3748', type: 'dashed' } }
    },
    series: [
      {
        name: '压力',
        type: 'line',
        data: generateRandomData(20, 0.3, 0.8),
        smooth: true,
        symbol: 'none',
        lineStyle: { width: 2, color: '#409eff' },
        areaStyle: { color: 'rgba(64, 158, 255, 0.1)' }
      },
      {
        name: '流量',
        type: 'line',
        data: generateRandomData(20, 100, 300),
        smooth: true,
        symbol: 'none',
        lineStyle: { width: 2, color: '#67c23a' }
      },
      {
        name: '温度',
        type: 'line',
        data: generateRandomData(20, 20, 35),
        smooth: true,
        symbol: 'none',
        lineStyle: { width: 2, color: '#e6a23c' }
      }
    ],
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#1a1a2e',
      borderColor: '#4a5568',
      textStyle: { color: '#e4e7ed', fontSize: 11 }
    }
  }
  
  historyChart.setOption(option)
}

// 初始化能耗排名图
const initEnergyChart = () => {
  if (!energyChartRef.value) return
  
  energyChart = echarts.init(energyChartRef.value)
  
  const option: echarts.EChartsOption = {
    backgroundColor: 'transparent',
    grid: {
      left: 45,
      right: 15,
      top: 10,
      bottom: 25
    },
    xAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#a0aec0', fontSize: 9 },
      splitLine: { lineStyle: { color: '#2d3748', type: 'dashed' } }
    },
    yAxis: {
      type: 'category',
      data: ['耗水', '耗电', '耗燃'],
      axisLine: { lineStyle: { color: '#4a5568' } },
      axisLabel: { color: '#a0aec0', fontSize: 10 }
    },
    series: [
      {
        type: 'bar',
        data: [
          { value: 856, itemStyle: { color: '#409eff' } },
          { value: 1234, itemStyle: { color: '#67c23a' } },
          { value: 456, itemStyle: { color: '#e6a23c' } }
        ],
        barWidth: 16,
        label: {
          show: true,
          position: 'right',
          color: '#a0aec0',
          fontSize: 10
        }
      }
    ],
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#1a1a2e',
      borderColor: '#4a5568',
      textStyle: { color: '#e4e7ed' }
    }
  }
  
  energyChart.setOption(option)
}

// 初始化故障统计图
const initFaultChart = () => {
  if (!faultChartRef.value) return
  
  faultChart = echarts.init(faultChartRef.value)
  
  const faultNames = ['水泵故障', '阀门故障', '传感器', '通信异常', '过压', '过温']
  const faultCounts = generateRandomData(6, 0, 10).map(v => Math.floor(v))
  
  const option: echarts.EChartsOption = {
    backgroundColor: 'transparent',
    grid: {
      left: 60,
      right: 15,
      top: 10,
      bottom: 25
    },
    xAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#a0aec0', fontSize: 9 },
      splitLine: { lineStyle: { color: '#2d3748', type: 'dashed' } },
      max: 10
    },
    yAxis: {
      type: 'category',
      data: faultNames,
      axisLine: { lineStyle: { color: '#4a5568' } },
      axisLabel: { color: '#a0aec0', fontSize: 9 }
    },
    series: [
      {
        type: 'bar',
        data: faultCounts.map(v => ({
          value: v,
          itemStyle: {
            color: v >= 5 ? '#f56c6c' : v >= 3 ? '#e6a23c' : '#67c23a'
          }
        })),
        barWidth: 12,
        label: {
          show: true,
          position: 'right',
          color: '#a0aec0',
          fontSize: 9
        }
      }
    ],
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#1a1a2e',
      borderColor: '#4a5568',
      textStyle: { color: '#e4e7ed' }
    }
  }
  
  faultChart.setOption(option)
}

// 初始化报表图
const initReportChart = () => {
  if (!reportChartRef.value) return
  
  reportChart = echarts.init(reportChartRef.value)
  
  const option: echarts.EChartsOption = {
    backgroundColor: 'transparent',
    grid: {
      left: 35,
      right: 10,
      top: 10,
      bottom: 20
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      axisLine: { lineStyle: { color: '#4a5568' } },
      axisLabel: { color: '#a0aec0', fontSize: 8 },
      splitLine: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#a0aec0', fontSize: 8 },
      splitLine: { lineStyle: { color: '#2d3748', type: 'dashed' } }
    },
    series: [
      {
        type: 'line',
        data: generateRandomData(7, 500, 1000),
        smooth: true,
        symbol: 'circle',
        symbolSize: 4,
        lineStyle: { width: 2, color: '#409eff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        }
      }
    ]
  }
  
  reportChart.setOption(option)
}

// 生成时间标签
const generateTimeLabels = (): string[] => {
  const labels: string[] = []
  const now = new Date()
  for (let i = 19; i >= 0; i--) {
    const time = new Date(now.getTime() - i * 3 * 60 * 1000)
    labels.push(`${time.getHours()}:${time.getMinutes().toString().padStart(2, '0')}`)
  }
  return labels
}

// 生成随机数据
const generateRandomData = (count: number, min: number, max: number): number[] => {
  return Array(count).fill(0).map(() => 
    Number((Math.random() * (max - min) + min).toFixed(2))
  )
}

// 更新图表数据
const updateCharts = () => {
  if (historyChart) {
    historyChart.setOption({
      xAxis: { data: generateTimeLabels() },
      series: [
        { data: generateRandomData(20, 0.3, 0.8) },
        { data: generateRandomData(20, 100, 300) },
        { data: generateRandomData(20, 20, 35) }
      ]
    })
  }
}

// 导出报表
const exportReport = () => {
  ElMessage.success('报表导出功能开发中')
}

// 监听时间范围变化
watch(timeRange, () => {
  updateCharts()
})

// 调整图表大小
const resizeCharts = () => {
  historyChart?.resize()
  energyChart?.resize()
  faultChart?.resize()
  reportChart?.resize()
}

// 生命周期
onMounted(() => {
  // 初始化所有图表
  setTimeout(() => {
    initHistoryChart()
    initEnergyChart()
    initFaultChart()
    initReportChart()
  }, 100)
  
  // 设置刷新定时器
  refreshTimer = setInterval(updateCharts, props.refreshInterval)
  
  // 监听窗口大小变化
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  // 清理资源
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  
  historyChart?.dispose()
  energyChart?.dispose()
  faultChart?.dispose()
  reportChart?.dispose()
  
  window.removeEventListener('resize', resizeCharts)
})

// 导出方法
defineExpose({
  updateCharts,
  resizeCharts
})
</script>

<style lang="scss" scoped>
.sidebar-panels {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
  padding: 12px;
  background: #16213e;
  overflow-y: auto;
  
  &::-webkit-scrollbar {
    width: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #4a5568;
    border-radius: 2px;
  }
}

.panel {
  background: #1a1a2e;
  border: 1px solid #2d4a6f;
  border-radius: 8px;
  overflow: hidden;
  
  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 12px;
    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
    border-bottom: 1px solid #2d4a6f;
    
    .panel-title {
      display: flex;
      align-items: center;
      gap: 6px;
      margin: 0;
      font-size: 13px;
      font-weight: 500;
      color: #e4e7ed;
      
      .title-icon {
        color: #409eff;
        font-size: 14px;
      }
    }
    
    .panel-actions {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }
  
  .panel-content {
    padding: 10px;
  }
}

.chart-container {
  width: 100%;
  height: 150px;
  
  &.mini {
    height: 80px;
  }
}

.time-select {
  width: 80px;
  
  :deep(.el-input__inner) {
    background: #2d3748;
    border-color: #4a5568;
    color: #e4e7ed;
  }
}

.report-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  margin-bottom: 10px;
  
  .stat-item {
    text-align: center;
    padding: 8px;
    background: #0f3460;
    border-radius: 6px;
    
    .stat-value {
      font-size: 16px;
      font-weight: 600;
      color: #67c23a;
    }
    
    .stat-label {
      font-size: 10px;
      color: #909399;
      margin-top: 2px;
    }
  }
}

// 面板特定样式
.history-curve-panel {
  .panel-title .title-icon { color: #409eff; }
}

.energy-ranking-panel {
  .panel-title .title-icon { color: #67c23a; }
}

.fault-stats-panel {
  .panel-title .title-icon { color: #e6a23c; }
}

.data-report-panel {
  .panel-title .title-icon { color: #909399; }
}
</style>
