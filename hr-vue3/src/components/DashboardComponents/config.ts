/**
 * 大屏组件库配置
 */
import type { ComponentLibraryItem } from '@/types/dashboard'
import { ComponentCategory, DataSourceType } from '@/types/dashboard'

/**
 * 组件库列表
 */
export const componentLibrary: ComponentLibraryItem[] = [
  // ==================== 图表组件 ====================
  {
    type: 'BarChart',
    name: '柱状图',
    icon: 'ep:histogram',
    category: ComponentCategory.CHART,
    defaultConfig: {
      type: 'BarChart',
      name: '柱状图',
      icon: 'ep:histogram',
      category: ComponentCategory.CHART,
      position: {
        x: 0,
        y: 0,
        w: 400,
        h: 300,
        rotate: 0,
        zIndex: 1
      },
      style: {
        backgroundColor: 'transparent',
        borderRadius: 0
      },
      data: {
        type: DataSourceType.STATIC,
        static: {
          xAxis: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
          series: [
            {
              name: '销量',
              data: [120, 200, 150, 80, 70, 110, 130]
            }
          ]
        },
        refresh: 0
      },
      options: {
        title: {
          text: '柱状图',
          textStyle: {
            color: '#fff',
            fontSize: 16
          }
        },
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
          data: [],
          axisLine: {
            lineStyle: {
              color: '#4a5568'
            }
          },
          axisLabel: {
            color: '#9ca3af'
          }
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
          }
        },
        series: [
          {
            type: 'bar',
            data: [],
            itemStyle: {
              color: '#3b82f6'
            }
          }
        ]
      }
    }
  },
  {
    type: 'LineChart',
    name: '折线图',
    icon: 'ep:data-line',
    category: ComponentCategory.CHART,
    defaultConfig: {
      type: 'LineChart',
      name: '折线图',
      icon: 'ep:data-line',
      category: ComponentCategory.CHART,
      position: {
        x: 0,
        y: 0,
        w: 400,
        h: 300,
        rotate: 0,
        zIndex: 1
      },
      style: {
        backgroundColor: 'transparent',
        borderRadius: 0
      },
      data: {
        type: DataSourceType.STATIC,
        static: {
          xAxis: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
          series: [
            {
              name: '温度',
              data: [22, 24, 26, 25, 23, 22, 24]
            }
          ]
        },
        refresh: 0
      },
      options: {
        title: {
          text: '折线图',
          textStyle: {
            color: '#fff',
            fontSize: 16
          }
        },
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
          data: [],
          axisLine: {
            lineStyle: {
              color: '#4a5568'
            }
          },
          axisLabel: {
            color: '#9ca3af'
          }
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
          }
        },
        series: [
          {
            type: 'line',
            data: [],
            smooth: true,
            itemStyle: {
              color: '#10b981'
            },
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
    }
  },
  {
    type: 'PieChart',
    name: '饼图',
    icon: 'ep:data-analysis',
    category: ComponentCategory.CHART,
    defaultConfig: {
      type: 'PieChart',
      name: '饼图',
      icon: 'ep:data-analysis',
      category: ComponentCategory.CHART,
      position: {
        x: 0,
        y: 0,
        w: 400,
        h: 300,
        rotate: 0,
        zIndex: 1
      },
      style: {
        backgroundColor: 'transparent',
        borderRadius: 0
      },
      data: {
        type: DataSourceType.STATIC,
        static: {
          series: [
            { name: '直接访问', value: 335 },
            { name: '邮件营销', value: 310 },
            { name: '联盟广告', value: 234 },
            { name: '视频广告', value: 135 },
            { name: '搜索引擎', value: 1548 }
          ]
        },
        refresh: 0
      },
      options: {
        title: {
          text: '饼图',
          textStyle: {
            color: '#fff',
            fontSize: 16
          }
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center',
          textStyle: {
            color: '#9ca3af'
          }
        },
        series: [
          {
            name: '访问来源',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            label: {
              show: true,
              color: '#9ca3af'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 14,
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: true
            },
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
  },

  // ==================== 文本组件 ====================
  {
    type: 'TextBox',
    name: '文本框',
    icon: 'ep:document',
    category: ComponentCategory.TEXT,
    defaultConfig: {
      type: 'TextBox',
      name: '文本框',
      icon: 'ep:document',
      category: ComponentCategory.TEXT,
      position: {
        x: 0,
        y: 0,
        w: 200,
        h: 50,
        rotate: 0,
        zIndex: 1
      },
      style: {
        backgroundColor: 'transparent',
        color: '#fff',
        fontSize: 16,
        fontWeight: 'normal',
        textAlign: 'left',
        borderRadius: 0
      },
      data: {
        type: DataSourceType.STATIC,
        static: {
          text: '文本内容'
        },
        refresh: 0
      },
      options: {
        content: '文本内容'
      }
    }
  },
  {
    type: 'NumberFlip',
    name: '数字翻牌器',
    icon: 'ep:odometer',
    category: ComponentCategory.TEXT,
    defaultConfig: {
      type: 'NumberFlip',
      name: '数字翻牌器',
      icon: 'ep:odometer',
      category: ComponentCategory.TEXT,
      position: {
        x: 0,
        y: 0,
        w: 300,
        h: 80,
        rotate: 0,
        zIndex: 1
      },
      style: {
        backgroundColor: 'transparent',
        color: '#00d4ff',
        fontSize: 48,
        fontWeight: 'bold',
        textAlign: 'center',
        borderRadius: 0
      },
      data: {
        type: DataSourceType.STATIC,
        static: {
          value: 12345
        },
        refresh: 0
      },
      options: {
        value: 12345,
        duration: 2000,
        separator: ',',
        decimals: 0,
        prefix: '',
        suffix: ''
      }
    }
  },

  // ==================== 装饰组件 ====================
  {
    type: 'BorderBox',
    name: '边框',
    icon: 'ep:picture',
    category: ComponentCategory.DECORATION,
    defaultConfig: {
      type: 'BorderBox',
      name: '边框',
      icon: 'ep:picture',
      category: ComponentCategory.DECORATION,
      position: {
        x: 0,
        y: 0,
        w: 400,
        h: 300,
        rotate: 0,
        zIndex: 1
      },
      style: {
        backgroundColor: 'rgba(0, 0, 0, 0.3)',
        borderRadius: 0
      },
      data: {
        type: DataSourceType.STATIC,
        static: {},
        refresh: 0
      },
      options: {
        borderType: 1, // 边框样式 1-12
        borderColor: ['#00d4ff', '#00a0e9'],
        backgroundColor: 'rgba(0, 0, 0, 0.3)'
      }
    }
  },

  // ==================== 表格组件 ====================
  {
    type: 'DataTable',
    name: '数据表格',
    icon: 'ep:grid',
    category: ComponentCategory.TABLE,
    defaultConfig: {
      type: 'DataTable',
      name: '数据表格',
      icon: 'ep:grid',
      category: ComponentCategory.TABLE,
      position: {
        x: 0,
        y: 0,
        w: 600,
        h: 400,
        rotate: 0,
        zIndex: 1
      },
      style: {
        backgroundColor: 'rgba(13, 25, 43, 0.8)',
        borderRadius: 4,
        color: '#e5e7eb'
      },
      data: {
        type: DataSourceType.STATIC,
        static: [
          { id: 1, name: '张三', age: 28, dept: '技术部', status: '在职' },
          { id: 2, name: '李四', age: 32, dept: '销售部', status: '在职' },
          { id: 3, name: '王五', age: 25, dept: '市场部', status: '在职' }
        ],
        refresh: 0
      },
      options: {
        title: '员工列表',
        columns: [
          { prop: 'id', label: 'ID', width: '80px', align: 'center' },
          { prop: 'name', label: '姓名', align: 'left' },
          { prop: 'age', label: '年龄', width: '100px', align: 'center' },
          { prop: 'dept', label: '部门', align: 'left' },
          { prop: 'status', label: '状态', width: '100px', align: 'center' }
        ],
        pagination: true,
        fontSize: '14px',
        headerBgColor: 'rgba(59, 130, 246, 0.2)',
        headerColor: '#60a5fa',
        borderColor: 'rgba(59, 130, 246, 0.3)'
      }
    }
  },

  // ==================== 统计组件 ====================
  {
    type: 'StatCard',
    name: '统计卡片',
    icon: 'ep:data-board',
    category: ComponentCategory.TEXT,
    defaultConfig: {
      type: 'StatCard',
      name: '统计卡片',
      icon: 'ep:data-board',
      category: ComponentCategory.TEXT,
      position: {
        x: 0,
        y: 0,
        w: 280,
        h: 120,
        rotate: 0,
        zIndex: 1
      },
      style: {
        backgroundColor: 'rgba(13, 25, 43, 0.8)',
        borderRadius: 8
      },
      data: {
        type: DataSourceType.STATIC,
        static: {
          value: 12345,
          trend: {
            direction: 'up',
            value: 12.5,
            unit: '%',
            label: '较昨日'
          }
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
        decimals: 0,
        valueSize: '32px',
        valueColor: '#fff',
        titleSize: '14px',
        titleColor: '#9ca3af'
      }
    }
  },
  {
    type: 'ProgressBar',
    name: '进度条',
    icon: 'ep:data-line',
    category: ComponentCategory.TEXT,
    defaultConfig: {
      type: 'ProgressBar',
      name: '进度条',
      icon: 'ep:data-line',
      category: ComponentCategory.TEXT,
      position: {
        x: 0,
        y: 0,
        w: 400,
        h: 120,
        rotate: 0,
        zIndex: 1
      },
      style: {
        backgroundColor: 'transparent',
        borderRadius: 4
      },
      data: {
        type: DataSourceType.STATIC,
        static: {
          value: 75,
          max: 100
        },
        refresh: 0
      },
      options: {
        type: 'line', // line, circle, dashboard
        title: '完成进度',
        showText: true,
        animated: true,
        striped: false,
        format: 'percent',
        height: 20,
        barColor: '#3b82f6',
        trackColor: 'rgba(59, 130, 246, 0.1)',
        textColor: '#fff',
        titleColor: '#9ca3af'
      }
    }
  },
  {
    type: 'LiquidFill',
    name: '水位图',
    icon: 'ep:water-cup',
    category: ComponentCategory.CHART,
    defaultConfig: {
      type: 'LiquidFill',
      name: '水位图',
      icon: 'ep:water-cup',
      category: ComponentCategory.CHART,
      position: {
        x: 0,
        y: 0,
        w: 300,
        h: 300,
        rotate: 0,
        zIndex: 1
      },
      style: {
        backgroundColor: 'transparent',
        borderRadius: 4
      },
      data: {
        type: DataSourceType.STATIC,
        static: {
          value: 65,
          max: 100
        },
        refresh: 0
      },
      options: {
        title: '水位',
        shape: 'circle', // circle, rect, diamond
        waveCount: 2,
        waveHeight: 0.05,
        waveSpeed: 0.02,
        waveColors: ['rgba(59, 130, 246, 0.6)', 'rgba(37, 99, 235, 0.4)'],
        borderColor: 'rgba(59, 130, 246, 0.5)',
        borderWidth: 3,
        format: 'percent'
      }
    }
  }
]

/**
 * 根据类型获取组件配置
 */
export function getComponentConfig(type: string): ComponentLibraryItem | undefined {
  return componentLibrary.find((item) => item.type === type)
}

/**
 * 根据分类获取组件列表
 */
export function getComponentsByCategory(category: ComponentCategory): ComponentLibraryItem[] {
  return componentLibrary.filter((item) => item.category === category)
}
