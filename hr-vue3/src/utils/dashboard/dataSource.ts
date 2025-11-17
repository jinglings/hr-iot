/**
 * 数据源管理工具
 */

import type { DataConfig, DataSourceType } from '@/types/dashboard'
import { request } from '@/config/axios'

/**
 * 数据源管理器
 */
export class DataSourceManager {
  private websocketConnections: Map<string, WebSocket> = new Map()
  private pollingTimers: Map<string, number> = new Map()
  private dataCache: Map<string, any> = new Map()

  /**
   * 获取数据
   * @param config 数据配置
   * @param componentId 组件ID
   * @returns 处理后的数据
   */
  async fetchData(config: DataConfig, componentId: string): Promise<any> {
    try {
      let rawData: any = null

      // 根据数据源类型获取数据
      switch (config.type) {
        case 'static':
          rawData = config.static
          break
        case 'api':
          rawData = await this.fetchApiData(config)
          break
        case 'websocket':
          rawData = await this.fetchWebSocketData(config, componentId)
          break
        default:
          console.warn(`不支持的数据源类型: ${config.type}`)
          rawData = null
      }

      // 数据转换
      if (rawData && config.transform) {
        rawData = this.transformData(rawData, config.transform)
      }

      // 数据过滤
      if (rawData && config.filter) {
        rawData = this.filterData(rawData, config.filter)
      }

      return rawData
    } catch (error) {
      console.error('获取数据失败:', error)
      return null
    }
  }

  /**
   * 获取API数据
   */
  private async fetchApiData(config: DataConfig): Promise<any> {
    if (!config.api) {
      throw new Error('API配置不完整')
    }

    const { url, method = 'GET', headers = {}, params = {}, dataPath } = config.api

    try {
      let response: any

      // 发送HTTP请求
      if (method === 'GET') {
        response = await request.get(url, { params, headers })
      } else if (method === 'POST') {
        response = await request.post(url, params, { headers })
      } else if (method === 'PUT') {
        response = await request.put(url, params, { headers })
      } else if (method === 'DELETE') {
        response = await request.delete(url, { params, headers })
      }

      // 如果指定了数据路径,提取对应数据
      if (dataPath && response) {
        return this.getDataByPath(response, dataPath)
      }

      return response
    } catch (error) {
      console.error('API请求失败:', error)
      throw error
    }
  }

  /**
   * 获取WebSocket数据
   */
  private async fetchWebSocketData(config: DataConfig, componentId: string): Promise<any> {
    if (!config.websocket) {
      throw new Error('WebSocket配置不完整')
    }

    const { url, event } = config.websocket
    const cacheKey = `ws_${componentId}`

    // 如果已有连接,返回缓存数据
    if (this.websocketConnections.has(cacheKey)) {
      return this.dataCache.get(cacheKey)
    }

    // 创建新的WebSocket连接
    return new Promise((resolve, reject) => {
      try {
        const ws = new WebSocket(url)

        ws.onopen = () => {
          console.log(`WebSocket连接成功: ${url}`)
          this.websocketConnections.set(cacheKey, ws)
        }

        ws.onmessage = (e) => {
          try {
            const data = JSON.parse(e.data)

            // 如果指定了事件名,只处理对应事件
            if (event) {
              if (data.event === event) {
                this.dataCache.set(cacheKey, data.data || data)
              }
            } else {
              this.dataCache.set(cacheKey, data)
            }
          } catch (error) {
            console.error('WebSocket消息解析失败:', error)
          }
        }

        ws.onerror = (error) => {
          console.error('WebSocket错误:', error)
          this.closeWebSocket(cacheKey)
          reject(error)
        }

        ws.onclose = () => {
          console.log('WebSocket连接关闭')
          this.closeWebSocket(cacheKey)
        }

        // 等待首次数据
        const checkData = setInterval(() => {
          const data = this.dataCache.get(cacheKey)
          if (data) {
            clearInterval(checkData)
            resolve(data)
          }
        }, 100)

        // 超时处理
        setTimeout(() => {
          clearInterval(checkData)
          if (!this.dataCache.has(cacheKey)) {
            reject(new Error('WebSocket数据获取超时'))
          }
        }, 10000)
      } catch (error) {
        reject(error)
      }
    })
  }

  /**
   * 启动数据轮询
   * @param config 数据配置
   * @param componentId 组件ID
   * @param callback 数据更新回调
   */
  startPolling(
    config: DataConfig,
    componentId: string,
    callback: (data: any) => void
  ): void {
    // 如果没有设置刷新间隔或间隔为0,不启动轮询
    if (!config.refresh || config.refresh <= 0) {
      return
    }

    // 清除已有的定时器
    this.stopPolling(componentId)

    // 创建新的定时器
    const timer = window.setInterval(async () => {
      try {
        const data = await this.fetchData(config, componentId)
        callback(data)
      } catch (error) {
        console.error('轮询数据更新失败:', error)
      }
    }, config.refresh * 1000)

    this.pollingTimers.set(componentId, timer)
  }

  /**
   * 停止数据轮询
   * @param componentId 组件ID
   */
  stopPolling(componentId: string): void {
    const timer = this.pollingTimers.get(componentId)
    if (timer) {
      clearInterval(timer)
      this.pollingTimers.delete(componentId)
    }
  }

  /**
   * 关闭WebSocket连接
   * @param key 连接key
   */
  closeWebSocket(key: string): void {
    const ws = this.websocketConnections.get(key)
    if (ws) {
      ws.close()
      this.websocketConnections.delete(key)
      this.dataCache.delete(key)
    }
  }

  /**
   * 关闭所有WebSocket连接
   */
  closeAllWebSockets(): void {
    this.websocketConnections.forEach((ws, key) => {
      ws.close()
    })
    this.websocketConnections.clear()
    this.dataCache.clear()
  }

  /**
   * 清理所有资源
   */
  destroy(): void {
    // 停止所有轮询
    this.pollingTimers.forEach((timer) => {
      clearInterval(timer)
    })
    this.pollingTimers.clear()

    // 关闭所有WebSocket
    this.closeAllWebSockets()
  }

  /**
   * 根据路径获取数据
   * @param data 数据对象
   * @param path 路径字符串,如 "data.list"
   */
  private getDataByPath(data: any, path: string): any {
    const keys = path.split('.')
    let result = data

    for (const key of keys) {
      if (result && typeof result === 'object' && key in result) {
        result = result[key]
      } else {
        return null
      }
    }

    return result
  }

  /**
   * 数据转换
   * @param data 原始数据
   * @param transformScript 转换脚本
   */
  private transformData(data: any, transformScript: string): any {
    try {
      // 创建沙箱环境
      const func = new Function('data', `
        try {
          ${transformScript}
          return data;
        } catch (error) {
          console.error('数据转换脚本执行失败:', error);
          return data;
        }
      `)

      return func(data)
    } catch (error) {
      console.error('数据转换失败:', error)
      return data
    }
  }

  /**
   * 数据过滤
   * @param data 原始数据
   * @param filterScript 过滤脚本
   */
  private filterData(data: any, filterScript: string): any {
    try {
      // 创建沙箱环境
      const func = new Function('data', `
        try {
          return ${filterScript};
        } catch (error) {
          console.error('数据过滤脚本执行失败:', error);
          return true;
        }
      `)

      // 如果是数组,进行过滤
      if (Array.isArray(data)) {
        return data.filter((item) => func(item))
      }

      // 如果是对象,判断是否保留
      if (func(data)) {
        return data
      }

      return null
    } catch (error) {
      console.error('数据过滤失败:', error)
      return data
    }
  }
}

/**
 * 全局数据源管理器实例
 */
export const dataSourceManager = new DataSourceManager()

/**
 * 数据源工具函数
 */

/**
 * 验证数据源配置
 * @param config 数据配置
 * @returns 验证结果
 */
export function validateDataConfig(config: DataConfig): {
  valid: boolean
  message?: string
} {
  if (!config.type) {
    return { valid: false, message: '请选择数据源类型' }
  }

  switch (config.type) {
    case 'static':
      if (!config.static) {
        return { valid: false, message: '请配置静态数据' }
      }
      break

    case 'api':
      if (!config.api?.url) {
        return { valid: false, message: '请输入API地址' }
      }
      // 验证URL格式
      try {
        new URL(config.api.url)
      } catch {
        return { valid: false, message: 'API地址格式不正确' }
      }
      break

    case 'websocket':
      if (!config.websocket?.url) {
        return { valid: false, message: '请输入WebSocket地址' }
      }
      // 验证WebSocket URL格式
      if (
        !config.websocket.url.startsWith('ws://') &&
        !config.websocket.url.startsWith('wss://')
      ) {
        return { valid: false, message: 'WebSocket地址必须以ws://或wss://开头' }
      }
      break

    case 'database':
      if (!config.database?.query) {
        return { valid: false, message: '请输入查询语句' }
      }
      break

    case 'mqtt':
      if (!config.mqtt?.host || !config.mqtt?.topic) {
        return { valid: false, message: '请配置MQTT服务器和主题' }
      }
      break
  }

  return { valid: true }
}

/**
 * 获取数据源类型显示名称
 */
export function getDataSourceTypeName(type: DataSourceType): string {
  const names: Record<DataSourceType, string> = {
    static: '静态数据',
    api: 'API接口',
    database: '数据库查询',
    websocket: 'WebSocket',
    mqtt: 'MQTT'
  }
  return names[type] || type
}

/**
 * 模拟数据生成器
 */
export function generateMockData(type: string): any {
  switch (type) {
    case 'chart':
      return {
        xAxis: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
        series: [
          {
            name: '销量',
            data: [120, 200, 150, 80, 70, 110, 130]
          }
        ]
      }

    case 'pie':
      return {
        series: [
          { name: '直接访问', value: 335 },
          { name: '邮件营销', value: 310 },
          { name: '联盟广告', value: 234 },
          { name: '视频广告', value: 135 },
          { name: '搜索引擎', value: 1548 }
        ]
      }

    case 'number':
      return {
        value: Math.floor(Math.random() * 10000)
      }

    default:
      return {}
  }
}
