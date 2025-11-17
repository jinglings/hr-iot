<template>
  <div ref="chartRef" class="pie-chart" :style="chartStyle"></div>
</template>

<script lang="ts" setup>
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import type { DashboardComponent } from '@/types/dashboard'

interface Props {
  component: DashboardComponent
  options?: any
  data?: any
}

const props = defineProps<Props>()

const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

// 图表样式
const chartStyle = computed(() => ({
  width: '100%',
  height: '100%'
}))

// 处理数据
const processData = () => {
  const staticData = props.data?.static || props.component.data?.static

  if (staticData?.series) {
    return staticData.series
  }

  if (props.options?.series && props.options.series[0]?.data) {
    return props.options.series[0].data
  }

  // 默认数据
  return [
    { name: '直接访问', value: 335 },
    { name: '邮件营销', value: 310 },
    { name: '联盟广告', value: 234 },
    { name: '视频广告', value: 135 },
    { name: '搜索引擎', value: 1548 }
  ]
}

// 获取 ECharts 配置
const getChartOptions = (): EChartsOption => {
  const processedData = processData()
  const baseOptions = props.options || props.component.options || {}

  // 颜色方案
  const colorPalette = [
    '#3b82f6',
    '#10b981',
    '#f59e0b',
    '#ef4444',
    '#8b5cf6',
    '#ec4899',
    '#14b8a6',
    '#f97316'
  ]

  const chartOptions: EChartsOption = {
    title: baseOptions.title || {
      text: '饼图',
      textStyle: {
        color: '#fff',
        fontSize: 16
      }
    },
    tooltip: baseOptions.tooltip || {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: baseOptions.legend || {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: {
        color: '#9ca3af'
      },
      formatter: (name: string) => {
        const item = processedData.find((d: any) => d.name === name)
        return item ? `${name}: ${item.value}` : name
      }
    },
    color: baseOptions.color || colorPalette,
    series: [
      {
        name: baseOptions.series?.[0]?.name || '访问来源',
        type: 'pie',
        radius: baseOptions.series?.[0]?.radius || ['40%', '70%'],
        center: baseOptions.series?.[0]?.center || ['40%', '50%'],
        avoidLabelOverlap: false,
        label: {
          show: baseOptions.series?.[0]?.label?.show !== false,
          color: '#9ca3af',
          formatter: '{b}: {d}%'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold',
            color: '#fff'
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        labelLine: {
          show: baseOptions.series?.[0]?.labelLine?.show !== false,
          lineStyle: {
            color: '#4a5568'
          }
        },
        data: processedData,
        itemStyle: {
          borderRadius: baseOptions.series?.[0]?.itemStyle?.borderRadius || 10,
          borderColor: baseOptions.series?.[0]?.itemStyle?.borderColor || '#0e1117',
          borderWidth: baseOptions.series?.[0]?.itemStyle?.borderWidth || 2
        },
        ...(baseOptions.series?.[0] || {})
      }
    ]
  }

  return chartOptions
}

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return

  if (chartInstance) {
    chartInstance.dispose()
  }

  chartInstance = echarts.init(chartRef.value)
  const options = getChartOptions()
  chartInstance.setOption(options)
}

// 更新图表
const updateChart = () => {
  if (!chartInstance) {
    initChart()
    return
  }

  const options = getChartOptions()
  chartInstance.setOption(options, true)
}

// 监听数据变化
watch(
  () => [props.data, props.options],
  () => {
    updateChart()
  },
  { deep: true }
)

// 监听窗口大小变化
const handleResize = () => {
  chartInstance?.resize()
}

// 生命周期
onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})

// 暴露方法
defineExpose({
  updateChart,
  getInstance: () => chartInstance
})
</script>

<style lang="scss" scoped>
.pie-chart {
  width: 100%;
  height: 100%;
}
</style>
