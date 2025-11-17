<template>
  <div ref="chartRef" class="line-chart" :style="chartStyle"></div>
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

  if (staticData) {
    return {
      xAxis: staticData.xAxis || [],
      series: staticData.series || []
    }
  }

  if (props.options?.series && props.options.series[0]?.data) {
    return {
      xAxis: props.options.xAxis?.data || [],
      series: [{ data: props.options.series[0].data }]
    }
  }

  // 默认数据
  return {
    xAxis: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
    series: [{ name: '温度', data: [22, 24, 26, 25, 23, 22, 24] }]
  }
}

// 获取 ECharts 配置
const getChartOptions = (): EChartsOption => {
  const processedData = processData()
  const baseOptions = props.options || props.component.options || {}

  const chartOptions: EChartsOption = {
    title: baseOptions.title || {
      text: '折线图',
      textStyle: {
        color: '#fff',
        fontSize: 16
      }
    },
    tooltip: baseOptions.tooltip || {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: baseOptions.legend || {
      show: processedData.series.length > 1,
      textStyle: {
        color: '#9ca3af'
      },
      top: '5%'
    },
    grid: baseOptions.grid || {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: processedData.series.length > 1 ? '20%' : '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: processedData.xAxis,
      axisLine: {
        lineStyle: {
          color: '#4a5568'
        }
      },
      axisLabel: {
        color: '#9ca3af',
        interval: 0,
        rotate: processedData.xAxis.length > 10 ? 45 : 0
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
      type: 'line',
      data: item.data,
      smooth: baseOptions.series?.[index]?.smooth !== false,
      symbol: 'circle',
      symbolSize: 6,
      itemStyle: {
        color: item.color || (baseOptions.series?.[index]?.itemStyle?.color) || '#10b981'
      },
      lineStyle: {
        width: 2,
        color: item.color || (baseOptions.series?.[index]?.itemStyle?.color) || '#10b981'
      },
      areaStyle: baseOptions.series?.[index]?.areaStyle !== false
        ? {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                {
                  offset: 0,
                  color:
                    item.color || (baseOptions.series?.[index]?.itemStyle?.color)
                      ? `${item.color || baseOptions.series[index].itemStyle.color}40`
                      : 'rgba(16, 185, 129, 0.3)'
                },
                {
                  offset: 1,
                  color: 'rgba(16, 185, 129, 0)'
                }
              ]
            }
          }
        : undefined,
      emphasis: {
        focus: 'series',
        itemStyle: {
          borderWidth: 2,
          borderColor: '#fff'
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
.line-chart {
  width: 100%;
  height: 100%;
}
</style>
