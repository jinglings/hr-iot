/**
 * 预定义大屏模板配置
 */

import type { DashboardTemplate, TemplateLibraryConfig } from '@/types/template'
import { TemplateCategory } from '@/types/template'
import { DataSourceType, ComponentCategory, ScaleMode } from '@/types/dashboard'

/**
 * KPI仪表板模板
 */
const kpiDashboardTemplate: DashboardTemplate = {
  id: 'kpi-dashboard-01',
  name: 'KPI数据监控',
  description: '适用于企业KPI指标监控的仪表板模板，包含多个统计卡片和趋势图表',
  category: TemplateCategory.KPI,
  thumbnail: '',
  isBuiltIn: true,
  tags: ['KPI', '数据监控', '统计'],
  author: 'System',
  config: {
    name: 'KPI数据监控',
    width: 1920,
    height: 1080,
    backgroundColor: '#0e1117',
    scale: {
      mode: ScaleMode.SCALE,
      ratio: 1
    },
    grid: {
      enabled: true,
      size: 10,
      color: 'rgba(255, 255, 255, 0.05)'
    },
    components: [
      // 顶部标题
      {
        id: 'title-1',
        type: 'TextBox',
        name: '标题',
        icon: 'ep:document',
        category: ComponentCategory.TEXT,
        position: { x: 760, y: 30, w: 400, h: 60, rotate: 0, zIndex: 1 },
        style: {
          backgroundColor: 'transparent',
          color: '#ffffff',
          fontSize: 36,
          fontWeight: 'bold',
          textAlign: 'center'
        },
        data: {
          type: DataSourceType.STATIC,
          static: { text: 'KPI数据监控大屏' },
          refresh: 0
        },
        options: { content: 'KPI数据监控大屏' }
      },
      // 统计卡片 - 总销售额
      {
        id: 'stat-card-1',
        type: 'StatCard',
        name: '总销售额',
        icon: 'ep:data-board',
        category: ComponentCategory.TEXT,
        position: { x: 50, y: 120, w: 280, h: 120, rotate: 0, zIndex: 1 },
        style: { backgroundColor: 'rgba(13, 25, 43, 0.8)', borderRadius: 8 },
        data: {
          type: DataSourceType.STATIC,
          static: {
            value: 1234567,
            trend: { direction: 'up', value: 12.5, unit: '%', label: '较昨日' }
          },
          refresh: 0
        },
        options: {
          title: '总销售额',
          unit: '元',
          icon: 'ep:trend-charts',
          iconSize: 48,
          iconColor: '#60a5fa',
          iconBgColor: 'rgba(59, 130, 246, 0.2)',
          animated: true,
          showTrend: true,
          separator: true,
          decimals: 0
        }
      },
      // 统计卡片 - 订单数
      {
        id: 'stat-card-2',
        type: 'StatCard',
        name: '订单数',
        icon: 'ep:data-board',
        category: ComponentCategory.TEXT,
        position: { x: 360, y: 120, w: 280, h: 120, rotate: 0, zIndex: 1 },
        style: { backgroundColor: 'rgba(13, 25, 43, 0.8)', borderRadius: 8 },
        data: {
          type: DataSourceType.STATIC,
          static: {
            value: 8765,
            trend: { direction: 'up', value: 8.3, unit: '%', label: '较昨日' }
          },
          refresh: 0
        },
        options: {
          title: '订单数',
          unit: '笔',
          icon: 'ep:shopping-cart',
          iconSize: 48,
          iconColor: '#10b981',
          iconBgColor: 'rgba(16, 185, 129, 0.2)',
          animated: true,
          showTrend: true,
          separator: true,
          decimals: 0
        }
      },
      // 统计卡片 - 客户数
      {
        id: 'stat-card-3',
        type: 'StatCard',
        name: '客户数',
        icon: 'ep:data-board',
        category: ComponentCategory.TEXT,
        position: { x: 670, y: 120, w: 280, h: 120, rotate: 0, zIndex: 1 },
        style: { backgroundColor: 'rgba(13, 25, 43, 0.8)', borderRadius: 8 },
        data: {
          type: DataSourceType.STATIC,
          static: {
            value: 45678,
            trend: { direction: 'up', value: 15.2, unit: '%', label: '较昨日' }
          },
          refresh: 0
        },
        options: {
          title: '客户数',
          unit: '人',
          icon: 'ep:user',
          iconSize: 48,
          iconColor: '#f59e0b',
          iconBgColor: 'rgba(245, 158, 11, 0.2)',
          animated: true,
          showTrend: true,
          separator: true,
          decimals: 0
        }
      },
      // 统计卡片 - 转化率
      {
        id: 'stat-card-4',
        type: 'StatCard',
        name: '转化率',
        icon: 'ep:data-board',
        category: ComponentCategory.TEXT,
        position: { x: 980, y: 120, w: 280, h: 120, rotate: 0, zIndex: 1 },
        style: { backgroundColor: 'rgba(13, 25, 43, 0.8)', borderRadius: 8 },
        data: {
          type: DataSourceType.STATIC,
          static: {
            value: 68.5,
            trend: { direction: 'down', value: 2.1, unit: '%', label: '较昨日' }
          },
          refresh: 0
        },
        options: {
          title: '转化率',
          unit: '%',
          icon: 'ep:data-analysis',
          iconSize: 48,
          iconColor: '#8b5cf6',
          iconBgColor: 'rgba(139, 92, 246, 0.2)',
          animated: true,
          showTrend: true,
          separator: false,
          decimals: 1
        }
      },
      // 柱状图 - 销售趋势
      {
        id: 'bar-chart-1',
        type: 'BarChart',
        name: '销售趋势',
        icon: 'ep:histogram',
        category: ComponentCategory.CHART,
        position: { x: 50, y: 270, w: 600, h: 350, rotate: 0, zIndex: 1 },
        style: { backgroundColor: 'rgba(13, 25, 43, 0.6)', borderRadius: 8 },
        data: {
          type: DataSourceType.STATIC,
          static: {
            xAxis: ['1月', '2月', '3月', '4月', '5月', '6月'],
            series: [
              { name: '销售额', data: [320, 280, 350, 420, 380, 450] }
            ]
          },
          refresh: 0
        },
        options: {
          title: { text: '月度销售趋势', textStyle: { color: '#fff', fontSize: 16 } },
          tooltip: { trigger: 'axis' },
          grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
          xAxis: {
            type: 'category',
            data: [],
            axisLine: { lineStyle: { color: '#4a5568' } },
            axisLabel: { color: '#9ca3af' }
          },
          yAxis: {
            type: 'value',
            axisLine: { lineStyle: { color: '#4a5568' } },
            axisLabel: { color: '#9ca3af' },
            splitLine: { lineStyle: { color: '#2d3748' } }
          },
          series: [{ type: 'bar', data: [], itemStyle: { color: '#3b82f6' } }]
        }
      },
      // 折线图 - 用户增长
      {
        id: 'line-chart-1',
        type: 'LineChart',
        name: '用户增长',
        icon: 'ep:data-line',
        category: ComponentCategory.CHART,
        position: { x: 680, y: 270, w: 600, h: 350, rotate: 0, zIndex: 1 },
        style: { backgroundColor: 'rgba(13, 25, 43, 0.6)', borderRadius: 8 },
        data: {
          type: DataSourceType.STATIC,
          static: {
            xAxis: ['1月', '2月', '3月', '4月', '5月', '6月'],
            series: [
              { name: '新增用户', data: [1200, 1400, 1300, 1600, 1800, 2000] }
            ]
          },
          refresh: 0
        },
        options: {
          title: { text: '用户增长趋势', textStyle: { color: '#fff', fontSize: 16 } },
          tooltip: { trigger: 'axis' },
          grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
          xAxis: {
            type: 'category',
            data: [],
            axisLine: { lineStyle: { color: '#4a5568' } },
            axisLabel: { color: '#9ca3af' }
          },
          yAxis: {
            type: 'value',
            axisLine: { lineStyle: { color: '#4a5568' } },
            axisLabel: { color: '#9ca3af' },
            splitLine: { lineStyle: { color: '#2d3748' } }
          },
          series: [
            {
              type: 'line',
              data: [],
              smooth: true,
              itemStyle: { color: '#10b981' },
              areaStyle: {
                color: {
                  type: 'linear',
                  x: 0,
                  y: 0,
                  x2: 0,
                  y2: 1,
                  colorStops: [
                    { offset: 0, color: 'rgba(16, 185, 129, 0.3)' },
                    { offset: 1, color: 'rgba(16, 185, 129, 0)' }
                  ]
                }
              }
            }
          ]
        }
      },
      // 饼图 - 销售渠道
      {
        id: 'pie-chart-1',
        type: 'PieChart',
        name: '销售渠道',
        icon: 'ep:data-analysis',
        category: ComponentCategory.CHART,
        position: { x: 50, y: 650, w: 400, h: 350, rotate: 0, zIndex: 1 },
        style: { backgroundColor: 'rgba(13, 25, 43, 0.6)', borderRadius: 8 },
        data: {
          type: DataSourceType.STATIC,
          static: {
            series: [
              { name: '线上渠道', value: 4200 },
              { name: '线下门店', value: 3100 },
              { name: '代理商', value: 2300 },
              { name: '直销', value: 1800 }
            ]
          },
          refresh: 0
        },
        options: {
          title: { text: '销售渠道占比', textStyle: { color: '#fff', fontSize: 16 } },
          tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
          legend: {
            orient: 'vertical',
            right: 10,
            top: 'center',
            textStyle: { color: '#9ca3af' }
          },
          series: [
            {
              name: '销售渠道',
              type: 'pie',
              radius: ['40%', '70%'],
              avoidLabelOverlap: false,
              label: { show: true, color: '#9ca3af' },
              emphasis: {
                label: { show: true, fontSize: 14, fontWeight: 'bold' }
              },
              labelLine: { show: true },
              data: [],
              itemStyle: {
                borderRadius: 10,
                borderColor: '#0e1117',
                borderWidth: 2
              }
            }
          ]
        }
      },
      // 数据表格
      {
        id: 'data-table-1',
        type: 'DataTable',
        name: '销售排行',
        icon: 'ep:grid',
        category: ComponentCategory.TABLE,
        position: { x: 480, y: 650, w: 800, h: 350, rotate: 0, zIndex: 1 },
        style: {
          backgroundColor: 'rgba(13, 25, 43, 0.8)',
          borderRadius: 8,
          color: '#e5e7eb'
        },
        data: {
          type: DataSourceType.STATIC,
          static: [
            { rank: 1, product: '产品A', sales: 125000, growth: '+15%' },
            { rank: 2, product: '产品B', sales: 98000, growth: '+12%' },
            { rank: 3, product: '产品C', sales: 87000, growth: '+8%' },
            { rank: 4, product: '产品D', sales: 76000, growth: '+5%' },
            { rank: 5, product: '产品E', sales: 65000, growth: '+3%' }
          ],
          refresh: 0
        },
        options: {
          title: '产品销售排行',
          columns: [
            { prop: 'rank', label: '排名', width: '80px', align: 'center' },
            { prop: 'product', label: '产品名称', align: 'left' },
            { prop: 'sales', label: '销售额', align: 'right' },
            { prop: 'growth', label: '增长率', width: '100px', align: 'center' }
          ],
          pagination: false,
          fontSize: '14px',
          headerBgColor: 'rgba(59, 130, 246, 0.2)',
          headerColor: '#60a5fa',
          borderColor: 'rgba(59, 130, 246, 0.3)'
        }
      }
    ]
  }
}

/**
 * 数据分析模板
 */
const dataAnalysisTemplate: DashboardTemplate = {
  id: 'data-analysis-01',
  name: '数据分析看板',
  description: '适用于数据分析和可视化展示的看板模板，包含多种图表类型',
  category: TemplateCategory.DATA_ANALYSIS,
  thumbnail: '',
  isBuiltIn: true,
  tags: ['数据分析', '可视化', '报表'],
  author: 'System',
  config: {
    name: '数据分析看板',
    width: 1920,
    height: 1080,
    backgroundColor: '#0e1117',
    scale: {
      mode: ScaleMode.SCALE,
      ratio: 1
    },
    grid: {
      enabled: true,
      size: 10,
      color: 'rgba(255, 255, 255, 0.05)'
    },
    components: [
      // 标题
      {
        id: 'title-1',
        type: 'TextBox',
        name: '标题',
        icon: 'ep:document',
        category: ComponentCategory.TEXT,
        position: { x: 760, y: 30, w: 400, h: 60, rotate: 0, zIndex: 1 },
        style: {
          backgroundColor: 'transparent',
          color: '#ffffff',
          fontSize: 36,
          fontWeight: 'bold',
          textAlign: 'center'
        },
        data: {
          type: DataSourceType.STATIC,
          static: { text: '数据分析看板' },
          refresh: 0
        },
        options: { content: '数据分析看板' }
      },
      // 柱状图
      {
        id: 'bar-chart-1',
        type: 'BarChart',
        name: '数据对比',
        icon: 'ep:histogram',
        category: ComponentCategory.CHART,
        position: { x: 50, y: 120, w: 900, h: 450, rotate: 0, zIndex: 1 },
        style: { backgroundColor: 'rgba(13, 25, 43, 0.6)', borderRadius: 8 },
        data: {
          type: DataSourceType.STATIC,
          static: {
            xAxis: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
            series: [
              { name: '指标A', data: [120, 200, 150, 80, 70, 110, 130] },
              { name: '指标B', data: [90, 180, 120, 100, 90, 130, 150] }
            ]
          },
          refresh: 0
        },
        options: {
          title: { text: '周数据对比', textStyle: { color: '#fff', fontSize: 16 } },
          tooltip: { trigger: 'axis' },
          legend: { top: 30, textStyle: { color: '#9ca3af' } },
          grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
          xAxis: {
            type: 'category',
            data: [],
            axisLine: { lineStyle: { color: '#4a5568' } },
            axisLabel: { color: '#9ca3af' }
          },
          yAxis: {
            type: 'value',
            axisLine: { lineStyle: { color: '#4a5568' } },
            axisLabel: { color: '#9ca3af' },
            splitLine: { lineStyle: { color: '#2d3748' } }
          },
          series: [
            { type: 'bar', data: [], itemStyle: { color: '#3b82f6' } },
            { type: 'bar', data: [], itemStyle: { color: '#10b981' } }
          ]
        }
      },
      // 折线图
      {
        id: 'line-chart-1',
        type: 'LineChart',
        name: '趋势分析',
        icon: 'ep:data-line',
        category: ComponentCategory.CHART,
        position: { x: 970, y: 120, w: 900, h: 450, rotate: 0, zIndex: 1 },
        style: { backgroundColor: 'rgba(13, 25, 43, 0.6)', borderRadius: 8 },
        data: {
          type: DataSourceType.STATIC,
          static: {
            xAxis: ['1月', '2月', '3月', '4月', '5月', '6月'],
            series: [
              { name: '系列1', data: [22, 24, 26, 25, 23, 27] },
              { name: '系列2', data: [18, 20, 22, 23, 24, 25] }
            ]
          },
          refresh: 0
        },
        options: {
          title: { text: '趋势分析', textStyle: { color: '#fff', fontSize: 16 } },
          tooltip: { trigger: 'axis' },
          legend: { top: 30, textStyle: { color: '#9ca3af' } },
          grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
          xAxis: {
            type: 'category',
            data: [],
            axisLine: { lineStyle: { color: '#4a5568' } },
            axisLabel: { color: '#9ca3af' }
          },
          yAxis: {
            type: 'value',
            axisLine: { lineStyle: { color: '#4a5568' } },
            axisLabel: { color: '#9ca3af' },
            splitLine: { lineStyle: { color: '#2d3748' } }
          },
          series: [
            {
              type: 'line',
              data: [],
              smooth: true,
              itemStyle: { color: '#f59e0b' }
            },
            {
              type: 'line',
              data: [],
              smooth: true,
              itemStyle: { color: '#8b5cf6' }
            }
          ]
        }
      },
      // 饼图
      {
        id: 'pie-chart-1',
        type: 'PieChart',
        name: '占比分析',
        icon: 'ep:data-analysis',
        category: ComponentCategory.CHART,
        position: { x: 50, y: 600, w: 560, h: 420, rotate: 0, zIndex: 1 },
        style: { backgroundColor: 'rgba(13, 25, 43, 0.6)', borderRadius: 8 },
        data: {
          type: DataSourceType.STATIC,
          static: {
            series: [
              { name: '类别A', value: 335 },
              { name: '类别B', value: 310 },
              { name: '类别C', value: 234 },
              { name: '类别D', value: 135 }
            ]
          },
          refresh: 0
        },
        options: {
          title: { text: '类别占比', textStyle: { color: '#fff', fontSize: 16 } },
          tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
          legend: {
            orient: 'vertical',
            right: 10,
            top: 'center',
            textStyle: { color: '#9ca3af' }
          },
          series: [
            {
              name: '占比',
              type: 'pie',
              radius: ['40%', '70%'],
              label: { show: true, color: '#9ca3af' },
              data: [],
              itemStyle: {
                borderRadius: 10,
                borderColor: '#0e1117',
                borderWidth: 2
              }
            }
          ]
        }
      },
      // 水位图
      {
        id: 'liquid-fill-1',
        type: 'LiquidFill',
        name: '完成率',
        icon: 'ep:water-cup',
        category: ComponentCategory.CHART,
        position: { x: 640, y: 600, w: 400, h: 420, rotate: 0, zIndex: 1 },
        style: { backgroundColor: 'rgba(13, 25, 43, 0.6)', borderRadius: 8 },
        data: {
          type: DataSourceType.STATIC,
          static: { value: 75, max: 100 },
          refresh: 0
        },
        options: {
          title: '完成率',
          shape: 'circle',
          waveCount: 2,
          waveHeight: 0.05,
          waveSpeed: 0.02,
          waveColors: ['rgba(59, 130, 246, 0.6)', 'rgba(37, 99, 235, 0.4)'],
          borderColor: 'rgba(59, 130, 246, 0.5)',
          borderWidth: 3,
          format: 'percent'
        }
      },
      // 进度条
      {
        id: 'progress-1',
        type: 'ProgressBar',
        name: '项目进度',
        icon: 'ep:data-line',
        category: ComponentCategory.TEXT,
        position: { x: 1070, y: 600, w: 800, h: 420, rotate: 0, zIndex: 1 },
        style: { backgroundColor: 'rgba(13, 25, 43, 0.6)', borderRadius: 8 },
        data: {
          type: DataSourceType.STATIC,
          static: { value: 65, max: 100 },
          refresh: 0
        },
        options: {
          type: 'line',
          title: '项目整体进度',
          showText: true,
          animated: true,
          striped: false,
          format: 'percent',
          height: 30,
          barColor: '#3b82f6',
          trackColor: 'rgba(59, 130, 246, 0.1)',
          textColor: '#fff',
          titleColor: '#9ca3af'
        }
      }
    ]
  }
}

/**
 * 物联网监控模板
 */
const iotMonitorTemplate: DashboardTemplate = {
  id: 'iot-monitor-01',
  name: '物联网设备监控',
  description: '适用于物联网设备实时监控的大屏模板，包含实时数据展示和设备状态监控',
  category: TemplateCategory.IOT_MONITOR,
  thumbnail: '',
  isBuiltIn: true,
  tags: ['物联网', '设备监控', '实时数据'],
  author: 'System',
  config: {
    name: '物联网设备监控',
    width: 1920,
    height: 1080,
    backgroundColor: '#0e1117',
    scale: {
      mode: ScaleMode.SCALE,
      ratio: 1
    },
    grid: {
      enabled: true,
      size: 10,
      color: 'rgba(255, 255, 255, 0.05)'
    },
    components: [
      // 标题
      {
        id: 'title-1',
        type: 'TextBox',
        name: '标题',
        icon: 'ep:document',
        category: ComponentCategory.TEXT,
        position: { x: 710, y: 30, w: 500, h: 60, rotate: 0, zIndex: 1 },
        style: {
          backgroundColor: 'transparent',
          color: '#ffffff',
          fontSize: 36,
          fontWeight: 'bold',
          textAlign: 'center'
        },
        data: {
          type: DataSourceType.STATIC,
          static: { text: '物联网设备监控中心' },
          refresh: 0
        },
        options: { content: '物联网设备监控中心' }
      },
      // 在线设备数
      {
        id: 'number-1',
        type: 'NumberFlip',
        name: '在线设备',
        icon: 'ep:odometer',
        category: ComponentCategory.TEXT,
        position: { x: 50, y: 120, w: 300, h: 100, rotate: 0, zIndex: 1 },
        style: {
          backgroundColor: 'rgba(13, 25, 43, 0.8)',
          color: '#00d4ff',
          fontSize: 48,
          fontWeight: 'bold',
          textAlign: 'center',
          borderRadius: 8
        },
        data: {
          type: DataSourceType.STATIC,
          static: { value: 1258 },
          refresh: 0
        },
        options: {
          value: 1258,
          duration: 2000,
          separator: ',',
          decimals: 0,
          prefix: '',
          suffix: ' 台'
        }
      },
      // 报警次数
      {
        id: 'number-2',
        type: 'NumberFlip',
        name: '报警次数',
        icon: 'ep:odometer',
        category: ComponentCategory.TEXT,
        position: { x: 380, y: 120, w: 300, h: 100, rotate: 0, zIndex: 1 },
        style: {
          backgroundColor: 'rgba(13, 25, 43, 0.8)',
          color: '#ff4757',
          fontSize: 48,
          fontWeight: 'bold',
          textAlign: 'center',
          borderRadius: 8
        },
        data: {
          type: DataSourceType.STATIC,
          static: { value: 23 },
          refresh: 0
        },
        options: {
          value: 23,
          duration: 2000,
          separator: ',',
          decimals: 0,
          prefix: '',
          suffix: ' 次'
        }
      },
      // 温度监控
      {
        id: 'line-chart-1',
        type: 'LineChart',
        name: '温度监控',
        icon: 'ep:data-line',
        category: ComponentCategory.CHART,
        position: { x: 50, y: 250, w: 630, h: 380, rotate: 0, zIndex: 1 },
        style: { backgroundColor: 'rgba(13, 25, 43, 0.6)', borderRadius: 8 },
        data: {
          type: DataSourceType.STATIC,
          static: {
            xAxis: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00'],
            series: [
              { name: '温度', data: [22, 24, 26, 28, 26, 24] }
            ]
          },
          refresh: 0
        },
        options: {
          title: { text: '实时温度监控', textStyle: { color: '#fff', fontSize: 16 } },
          tooltip: { trigger: 'axis' },
          grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
          xAxis: {
            type: 'category',
            data: [],
            axisLine: { lineStyle: { color: '#4a5568' } },
            axisLabel: { color: '#9ca3af' }
          },
          yAxis: {
            type: 'value',
            axisLine: { lineStyle: { color: '#4a5568' } },
            axisLabel: { color: '#9ca3af', formatter: '{value}°C' },
            splitLine: { lineStyle: { color: '#2d3748' } }
          },
          series: [
            {
              type: 'line',
              data: [],
              smooth: true,
              itemStyle: { color: '#f59e0b' },
              areaStyle: {
                color: {
                  type: 'linear',
                  x: 0,
                  y: 0,
                  x2: 0,
                  y2: 1,
                  colorStops: [
                    { offset: 0, color: 'rgba(245, 158, 11, 0.3)' },
                    { offset: 1, color: 'rgba(245, 158, 11, 0)' }
                  ]
                }
              }
            }
          ]
        }
      },
      // 设备状态表格
      {
        id: 'data-table-1',
        type: 'DataTable',
        name: '设备状态',
        icon: 'ep:grid',
        category: ComponentCategory.TABLE,
        position: { x: 50, y: 660, w: 630, h: 360, rotate: 0, zIndex: 1 },
        style: {
          backgroundColor: 'rgba(13, 25, 43, 0.8)',
          borderRadius: 8,
          color: '#e5e7eb'
        },
        data: {
          type: DataSourceType.STATIC,
          static: [
            { id: 'DEV001', name: '温控设备-1', status: '运行中', temp: '25.6°C' },
            { id: 'DEV002', name: '温控设备-2', status: '运行中', temp: '24.8°C' },
            { id: 'DEV003', name: '温控设备-3', status: '报警', temp: '32.1°C' },
            { id: 'DEV004', name: '温控设备-4', status: '运行中', temp: '26.3°C' },
            { id: 'DEV005', name: '温控设备-5', status: '离线', temp: '--' }
          ],
          refresh: 0
        },
        options: {
          title: '设备运行状态',
          columns: [
            { prop: 'id', label: '设备ID', width: '120px', align: 'center' },
            { prop: 'name', label: '设备名称', align: 'left' },
            { prop: 'status', label: '状态', width: '100px', align: 'center' },
            { prop: 'temp', label: '当前温度', width: '120px', align: 'center' }
          ],
          pagination: false,
          fontSize: '14px',
          headerBgColor: 'rgba(59, 130, 246, 0.2)',
          headerColor: '#60a5fa',
          borderColor: 'rgba(59, 130, 246, 0.3)'
        }
      },
      // 湿度水位图
      {
        id: 'liquid-fill-1',
        type: 'LiquidFill',
        name: '湿度监控',
        icon: 'ep:water-cup',
        category: ComponentCategory.CHART,
        position: { x: 710, y: 250, w: 480, h: 380, rotate: 0, zIndex: 1 },
        style: { backgroundColor: 'rgba(13, 25, 43, 0.6)', borderRadius: 8 },
        data: {
          type: DataSourceType.STATIC,
          static: { value: 68, max: 100 },
          refresh: 0
        },
        options: {
          title: '环境湿度',
          shape: 'circle',
          waveCount: 2,
          waveHeight: 0.05,
          waveSpeed: 0.02,
          waveColors: ['rgba(59, 130, 246, 0.6)', 'rgba(37, 99, 235, 0.4)'],
          borderColor: 'rgba(59, 130, 246, 0.5)',
          borderWidth: 3,
          format: 'percent'
        }
      },
      // 能耗统计
      {
        id: 'bar-chart-1',
        type: 'BarChart',
        name: '能耗统计',
        icon: 'ep:histogram',
        category: ComponentCategory.CHART,
        position: { x: 710, y: 660, w: 480, h: 360, rotate: 0, zIndex: 1 },
        style: { backgroundColor: 'rgba(13, 25, 43, 0.6)', borderRadius: 8 },
        data: {
          type: DataSourceType.STATIC,
          static: {
            xAxis: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
            series: [
              { name: '能耗', data: [320, 280, 350, 300, 280, 260, 240] }
            ]
          },
          refresh: 0
        },
        options: {
          title: { text: '每日能耗统计', textStyle: { color: '#fff', fontSize: 16 } },
          tooltip: { trigger: 'axis' },
          grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
          xAxis: {
            type: 'category',
            data: [],
            axisLine: { lineStyle: { color: '#4a5568' } },
            axisLabel: { color: '#9ca3af' }
          },
          yAxis: {
            type: 'value',
            axisLine: { lineStyle: { color: '#4a5568' } },
            axisLabel: { color: '#9ca3af', formatter: '{value}kWh' },
            splitLine: { lineStyle: { color: '#2d3748' } }
          },
          series: [
            {
              type: 'bar',
              data: [],
              itemStyle: {
                color: {
                  type: 'linear',
                  x: 0,
                  y: 0,
                  x2: 0,
                  y2: 1,
                  colorStops: [
                    { offset: 0, color: '#10b981' },
                    { offset: 1, color: '#059669' }
                  ]
                }
              }
            }
          ]
        }
      },
      // 设备分布饼图
      {
        id: 'pie-chart-1',
        type: 'PieChart',
        name: '设备分布',
        icon: 'ep:data-analysis',
        category: ComponentCategory.CHART,
        position: { x: 1220, y: 250, w: 650, h: 770, rotate: 0, zIndex: 1 },
        style: { backgroundColor: 'rgba(13, 25, 43, 0.6)', borderRadius: 8 },
        data: {
          type: DataSourceType.STATIC,
          static: {
            series: [
              { name: '温控设备', value: 425 },
              { name: '传感器', value: 368 },
              { name: '控制器', value: 285 },
              { name: '监控摄像头', value: 180 }
            ]
          },
          refresh: 0
        },
        options: {
          title: { text: '设备类型分布', textStyle: { color: '#fff', fontSize: 16 } },
          tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
          legend: {
            orient: 'vertical',
            right: 20,
            top: 'center',
            textStyle: { color: '#9ca3af' }
          },
          series: [
            {
              name: '设备类型',
              type: 'pie',
              radius: ['40%', '70%'],
              center: ['40%', '55%'],
              label: { show: true, color: '#9ca3af' },
              data: [],
              itemStyle: {
                borderRadius: 10,
                borderColor: '#0e1117',
                borderWidth: 2
              }
            }
          ]
        }
      }
    ]
  }
}

/**
 * 模板库配置
 */
export const templateLibraryConfig: TemplateLibraryConfig = {
  templates: [kpiDashboardTemplate, dataAnalysisTemplate, iotMonitorTemplate],
  categories: [
    {
      value: TemplateCategory.KPI,
      label: 'KPI仪表板',
      icon: 'ep:data-board'
    },
    {
      value: TemplateCategory.DATA_ANALYSIS,
      label: '数据分析',
      icon: 'ep:data-analysis'
    },
    {
      value: TemplateCategory.IOT_MONITOR,
      label: '物联网监控',
      icon: 'ep:monitor'
    },
    {
      value: TemplateCategory.BUSINESS,
      label: '业务监控',
      icon: 'ep:office-building'
    },
    {
      value: TemplateCategory.CUSTOM,
      label: '自定义模板',
      icon: 'ep:edit'
    }
  ]
}

/**
 * 根据分类获取模板
 */
export function getTemplatesByCategory(category?: TemplateCategory): DashboardTemplate[] {
  if (!category) {
    return templateLibraryConfig.templates
  }
  return templateLibraryConfig.templates.filter((t) => t.category === category)
}

/**
 * 根据ID获取模板
 */
export function getTemplateById(id: string): DashboardTemplate | undefined {
  return templateLibraryConfig.templates.find((t) => t.id === id)
}
