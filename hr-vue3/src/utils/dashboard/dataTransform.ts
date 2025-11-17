/**
 * 数据转换和处理工具
 */

/**
 * 图表数据转换器
 */
export class ChartDataTransformer {
  /**
   * 转换为ECharts柱状图/折线图数据格式
   * @param data 原始数据
   * @param options 配置选项
   * @returns ECharts数据格式
   */
  static toBarLineChart(
    data: any[],
    options: {
      xField: string // X轴字段名
      yField: string | string[] // Y轴字段名(支持多个)
      seriesNames?: string[] // 系列名称
    }
  ): { xAxis: any[]; series: any[] } {
    if (!Array.isArray(data) || data.length === 0) {
      return { xAxis: [], series: [] }
    }

    const { xField, yField, seriesNames } = options
    const xAxis = data.map((item) => item[xField])

    // 单系列
    if (typeof yField === 'string') {
      const series = [
        {
          name: seriesNames?.[0] || yField,
          data: data.map((item) => item[yField])
        }
      ]
      return { xAxis, series }
    }

    // 多系列
    const series = yField.map((field, index) => ({
      name: seriesNames?.[index] || field,
      data: data.map((item) => item[field])
    }))

    return { xAxis, series }
  }

  /**
   * 转换为ECharts饼图数据格式
   * @param data 原始数据
   * @param options 配置选项
   * @returns ECharts饼图数据格式
   */
  static toPieChart(
    data: any[],
    options: {
      nameField: string // 名称字段
      valueField: string // 值字段
    }
  ): { series: any[] } {
    if (!Array.isArray(data) || data.length === 0) {
      return { series: [] }
    }

    const { nameField, valueField } = options
    const series = data.map((item) => ({
      name: item[nameField],
      value: item[valueField]
    }))

    return { series }
  }

  /**
   * 时间序列数据转换
   * @param data 原始数据
   * @param options 配置选项
   */
  static toTimeSeriesChart(
    data: any[],
    options: {
      timeField: string // 时间字段
      valueField: string | string[] // 值字段
      timeFormat?: 'YYYY-MM-DD' | 'YYYY-MM-DD HH:mm:ss' | 'HH:mm:ss' // 时间格式
      seriesNames?: string[]
    }
  ): { xAxis: any[]; series: any[] } {
    if (!Array.isArray(data) || data.length === 0) {
      return { xAxis: [], series: [] }
    }

    const { timeField, valueField, timeFormat = 'YYYY-MM-DD HH:mm:ss', seriesNames } = options

    // 排序数据
    const sortedData = [...data].sort(
      (a, b) => new Date(a[timeField]).getTime() - new Date(b[timeField]).getTime()
    )

    // 格式化时间
    const xAxis = sortedData.map((item) => {
      return this.formatTime(item[timeField], timeFormat)
    })

    // 单系列
    if (typeof valueField === 'string') {
      const series = [
        {
          name: seriesNames?.[0] || valueField,
          data: sortedData.map((item) => item[valueField])
        }
      ]
      return { xAxis, series }
    }

    // 多系列
    const series = valueField.map((field, index) => ({
      name: seriesNames?.[index] || field,
      data: sortedData.map((item) => item[field])
    }))

    return { xAxis, series }
  }

  /**
   * 格式化时间
   */
  private static formatTime(time: string | Date, format: string): string {
    const date = new Date(time)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hour = String(date.getHours()).padStart(2, '0')
    const minute = String(date.getMinutes()).padStart(2, '0')
    const second = String(date.getSeconds()).padStart(2, '0')

    return format
      .replace('YYYY', String(year))
      .replace('MM', month)
      .replace('DD', day)
      .replace('HH', hour)
      .replace('mm', minute)
      .replace('ss', second)
  }
}

/**
 * 数据聚合器
 */
export class DataAggregator {
  /**
   * 求和
   */
  static sum(data: any[], field: string): number {
    return data.reduce((sum, item) => sum + (Number(item[field]) || 0), 0)
  }

  /**
   * 平均值
   */
  static avg(data: any[], field: string): number {
    if (data.length === 0) return 0
    return this.sum(data, field) / data.length
  }

  /**
   * 最大值
   */
  static max(data: any[], field: string): number {
    if (data.length === 0) return 0
    return Math.max(...data.map((item) => Number(item[field]) || 0))
  }

  /**
   * 最小值
   */
  static min(data: any[], field: string): number {
    if (data.length === 0) return 0
    return Math.min(...data.map((item) => Number(item[field]) || 0))
  }

  /**
   * 计数
   */
  static count(data: any[], field?: string): number {
    if (!field) return data.length
    return data.filter((item) => item[field] != null).length
  }

  /**
   * 分组聚合
   */
  static groupBy(
    data: any[],
    groupField: string,
    aggField: string,
    aggType: 'sum' | 'avg' | 'count' | 'max' | 'min' = 'sum'
  ): any[] {
    const groups = new Map<any, any[]>()

    // 分组
    data.forEach((item) => {
      const key = item[groupField]
      if (!groups.has(key)) {
        groups.set(key, [])
      }
      groups.get(key)!.push(item)
    })

    // 聚合
    const result: any[] = []
    groups.forEach((items, key) => {
      let value: number
      switch (aggType) {
        case 'sum':
          value = this.sum(items, aggField)
          break
        case 'avg':
          value = this.avg(items, aggField)
          break
        case 'max':
          value = this.max(items, aggField)
          break
        case 'min':
          value = this.min(items, aggField)
          break
        case 'count':
          value = this.count(items, aggField)
          break
        default:
          value = 0
      }

      result.push({
        [groupField]: key,
        [aggField]: value
      })
    })

    return result
  }
}

/**
 * 数据过滤器
 */
export class DataFilter {
  /**
   * 按字段值过滤
   */
  static filterByValue(data: any[], field: string, value: any, operator: '=' | '!=' | '>' | '<' | '>=' | '<=' = '='): any[] {
    return data.filter((item) => {
      const itemValue = item[field]
      switch (operator) {
        case '=':
          return itemValue === value
        case '!=':
          return itemValue !== value
        case '>':
          return itemValue > value
        case '<':
          return itemValue < value
        case '>=':
          return itemValue >= value
        case '<=':
          return itemValue <= value
        default:
          return true
      }
    })
  }

  /**
   * 按范围过滤
   */
  static filterByRange(data: any[], field: string, min: number, max: number): any[] {
    return data.filter((item) => {
      const value = Number(item[field])
      return value >= min && value <= max
    })
  }

  /**
   * 按时间范围过滤
   */
  static filterByTimeRange(
    data: any[],
    timeField: string,
    startTime: string | Date,
    endTime: string | Date
  ): any[] {
    const start = new Date(startTime).getTime()
    const end = new Date(endTime).getTime()

    return data.filter((item) => {
      const time = new Date(item[timeField]).getTime()
      return time >= start && time <= end
    })
  }

  /**
   * 按关键词搜索
   */
  static search(data: any[], fields: string[], keyword: string): any[] {
    const lowerKeyword = keyword.toLowerCase()
    return data.filter((item) => {
      return fields.some((field) => {
        const value = String(item[field] || '').toLowerCase()
        return value.includes(lowerKeyword)
      })
    })
  }

  /**
   * 去重
   */
  static unique(data: any[], field?: string): any[] {
    if (!field) {
      return Array.from(new Set(data))
    }

    const seen = new Set()
    return data.filter((item) => {
      const value = item[field]
      if (seen.has(value)) {
        return false
      }
      seen.add(value)
      return true
    })
  }

  /**
   * 排序
   */
  static sort(data: any[], field: string, order: 'asc' | 'desc' = 'asc'): any[] {
    return [...data].sort((a, b) => {
      const aValue = a[field]
      const bValue = b[field]

      if (aValue === bValue) return 0

      const comparison = aValue > bValue ? 1 : -1
      return order === 'asc' ? comparison : -comparison
    })
  }

  /**
   * 分页
   */
  static paginate(data: any[], page: number, pageSize: number): any[] {
    const start = (page - 1) * pageSize
    return data.slice(start, start + pageSize)
  }
}

/**
 * 数据格式化器
 */
export class DataFormatter {
  /**
   * 格式化数字
   */
  static formatNumber(
    value: number,
    options: {
      decimals?: number // 小数位数
      separator?: string // 千分位分隔符
      prefix?: string // 前缀
      suffix?: string // 后缀
    } = {}
  ): string {
    const { decimals = 0, separator = ',', prefix = '', suffix = '' } = options

    // 保留小数
    let result = value.toFixed(decimals)

    // 添加千分位分隔符
    if (separator) {
      const parts = result.split('.')
      parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, separator)
      result = parts.join('.')
    }

    return `${prefix}${result}${suffix}`
  }

  /**
   * 格式化百分比
   */
  static formatPercent(value: number, decimals: number = 2): string {
    return `${(value * 100).toFixed(decimals)}%`
  }

  /**
   * 格式化文件大小
   */
  static formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 B'

    const units = ['B', 'KB', 'MB', 'GB', 'TB']
    const k = 1024
    const i = Math.floor(Math.log(bytes) / Math.log(k))

    return `${(bytes / Math.pow(k, i)).toFixed(2)} ${units[i]}`
  }

  /**
   * 格式化时长
   */
  static formatDuration(seconds: number): string {
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    const secs = seconds % 60

    const parts: string[] = []
    if (hours > 0) parts.push(`${hours}小时`)
    if (minutes > 0) parts.push(`${minutes}分钟`)
    if (secs > 0 || parts.length === 0) parts.push(`${secs}秒`)

    return parts.join('')
  }
}

/**
 * 常用转换函数导出(可在脚本中使用)
 */
export const transformHelpers = {
  // 转换器
  toBarLineChart: ChartDataTransformer.toBarLineChart,
  toPieChart: ChartDataTransformer.toPieChart,
  toTimeSeriesChart: ChartDataTransformer.toTimeSeriesChart,

  // 聚合
  sum: DataAggregator.sum,
  avg: DataAggregator.avg,
  max: DataAggregator.max,
  min: DataAggregator.min,
  count: DataAggregator.count,
  groupBy: DataAggregator.groupBy,

  // 过滤
  filterByValue: DataFilter.filterByValue,
  filterByRange: DataFilter.filterByRange,
  filterByTimeRange: DataFilter.filterByTimeRange,
  search: DataFilter.search,
  unique: DataFilter.unique,
  sort: DataFilter.sort,
  paginate: DataFilter.paginate,

  // 格式化
  formatNumber: DataFormatter.formatNumber,
  formatPercent: DataFormatter.formatPercent,
  formatFileSize: DataFormatter.formatFileSize,
  formatDuration: DataFormatter.formatDuration
}
