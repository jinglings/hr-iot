<template>
  <div ref="chartRef" class="bar-chart" :style="chartStyle"></div>
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

  // 如果有静态数据，返回处理后的数据
  if (staticData) {
    return {
      xAxis: staticData.xAxis || [],
      series: staticData.series || []
    }
  }

  // 从 options 中获取数据
  if (props.options?.series && props.options.series[0]?.data) {
    return {
      xAxis: props.options.xAxis?.data || [],
      series: [{ data: props.options.series[0].data }]
    }
  }

  // 默认数据
  return {
    xAxis: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
    series: [{ name: '销量', data: [120, 200, 150, 80, 70, 110, 130] }]
  }
}

// 获取 ECharts 配置
const getChartOptions = (): EChartsOption => {
  const processedData = processData()
  const baseOptions = props.options || props.component.options || {}

  // 合并配置
  const chartOptions: EChartsOption = {
    title: baseOptions.title || {
      text: '柱状图',
      textStyle: {
        color: '#fff',
        fontSize: 16
      }
    },
    tooltip: baseOptions.tooltip || {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: baseOptions.grid || {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: processedData.xAxis,
      axisLine: {
        lineStyle: {
          color: '#4a5568'
        }
      },
      axisLabel: {
        color: '#9ca3af',
        interval: 0,
        rotate: processedData.xAxis.length > 7 ? 45 : 0
      },
      ...(baseOptions.xAxis || {})
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#4a5568'
        }
      },
      axisLabel: {
        color: '#9ca3af'
      },
      splitLine: {
        lineStyle: {
          color: '#2d3748'
        }
      },
      ...(baseOptions.yAxis || {})
    },
    series: processedData.series.map((item, index) => ({
      name: item.name || `系列${index + 1}`,
      type: 'bar',
      data: item.data,
      itemStyle: {
        color: item.color || (baseOptions.series?.[index]?.itemStyle?.color) || '#3b82f6',
        borderRadius: [4, 4, 0, 0]
      },
      label: {
        show: baseOptions.series?.[index]?.label?.show || false,
        position: 'top',
        color: '#fff'
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.3)'
        }
      },
      ...(baseOptions.series?.[index] || {})
    }))
  }

  return chartOptions
}

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return

  // 销毁旧实例
  if (chartInstance) {
    chartInstance.dispose()
  }

  // 创建新实例
  chartInstance = echarts.init(chartRef.value)

  // 设置配置项
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
.bar-chart {
  width: 100%;
  height: 100%;
}
</style>
