/**
 * 大屏组件注册中心
 */

// 图表组件
import BarChart from './charts/BarChart.vue'
import LineChart from './charts/LineChart.vue'
import PieChart from './charts/PieChart.vue'

// 文本组件
import TextBox from './text/TextBox.vue'
import NumberFlip from './text/NumberFlip.vue'

// 装饰组件
import BorderBox from './decorations/BorderBox.vue'

// 表格组件
import DataTable from './tables/DataTable.vue'

// 统计组件
import StatCard from './stats/StatCard.vue'
import ProgressBar from './stats/ProgressBar.vue'
import LiquidFill from './stats/LiquidFill.vue'

// 组件映射表
export const componentMap = {
  // 图表组件
  BarChart,
  LineChart,
  PieChart,

  // 文本组件
  TextBox,
  NumberFlip,

  // 装饰组件
  BorderBox,

  // 表格组件
  DataTable,

  // 统计组件
  StatCard,
  ProgressBar,
  LiquidFill
}

// 组件类型
export type ComponentType = keyof typeof componentMap

/**
 * 获取组件
 */
export function getComponent(type: string) {
  return componentMap[type as ComponentType]
}

/**
 * 检查组件是否存在
 */
export function hasComponent(type: string): boolean {
  return type in componentMap
}

/**
 * 获取所有组件类型
 */
export function getAllComponentTypes(): string[] {
  return Object.keys(componentMap)
}

// 导出所有组件
export {
  BarChart,
  LineChart,
  PieChart,
  TextBox,
  NumberFlip,
  BorderBox,
  DataTable,
  StatCard,
  ProgressBar,
  LiquidFill
}

// 导出组件配置
export { componentLibrary, getComponentConfig, getComponentsByCategory } from './config'
