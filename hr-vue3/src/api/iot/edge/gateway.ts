import request from '@/config/axios'

/**
 * 边缘网关响应VO
 */
export interface EdgeGatewayVO {
  id: number // 主键
  name: string // 网关名称
  serialNumber: string // 网关序列号
  gatewayKey: string // 网关标识（唯一）
  gatewaySecret: string // 网关密钥

  // 设备信息
  deviceType: string // 设备型号
  hardwareVersion: string // 硬件版本
  softwareVersion: string // 软件版本

  // 网络信息
  ipAddress: string // IP地址
  macAddress: string // MAC地址
  location: string // 安装位置

  // 状态信息
  status: number // 状态: 0=未激活, 1=在线, 2=离线
  lastOnlineTime: Date // 最后在线时间
  lastOfflineTime: Date // 最后离线时间
  activeTime: Date // 激活时间

  // 资源信息
  cpuCores: number // CPU核心数
  memoryTotal: number // 总内存(MB)
  diskTotal: number // 总磁盘(GB)

  // 配置信息
  config: string // 网关配置(JSON字符串)
  description: string // 描述

  // 标准字段
  creator: string // 创建者
  createTime: Date // 创建时间
  updater: string // 更新者
  updateTime: Date // 更新时间
}

/**
 * 边缘网关分页查询请求VO
 */
export interface EdgeGatewayPageReqVO {
  pageNo: number // 页码
  pageSize: number // 每页数量
  name?: string // 网关名称（模糊搜索）
  status?: number // 状态
  serialNumber?: string // 序列号
}

/**
 * 边缘网关创建请求VO
 */
export interface EdgeGatewayCreateReqVO {
  name: string // 网关名称
  serialNumber: string // 网关序列号
  deviceType?: string // 设备型号
  location?: string // 安装位置
  description?: string // 描述
}

/**
 * 边缘网关更新请求VO
 */
export interface EdgeGatewayUpdateReqVO {
  id: number // 主键
  name?: string // 网关名称
  deviceType?: string // 设备型号
  location?: string // 安装位置
  description?: string // 描述
}

// 边缘网关状态枚举
export enum EdgeGatewayStatus {
  INACTIVE = 0, // 未激活
  ONLINE = 1, // 在线
  OFFLINE = 2 // 离线
}

// 边缘网关状态映射
export const EdgeGatewayStatusMap = {
  [EdgeGatewayStatus.INACTIVE]: { label: '未激活', type: 'warning' },
  [EdgeGatewayStatus.ONLINE]: { label: '在线', type: 'success' },
  [EdgeGatewayStatus.OFFLINE]: { label: '离线', type: 'info' }
}

// 边缘网关 API
export const EdgeGatewayApi = {
  // 分页查询
  getPage: async (params: EdgeGatewayPageReqVO) => {
    return await request.get({ url: '/iot/edge-gateway/page', params })
  },

  // 获取详情
  get: async (id: number) => {
    return await request.get({ url: '/iot/edge-gateway/get', params: { id } })
  },

  // 通过key获取
  getByKey: async (gatewayKey: string) => {
    return await request.get({
      url: '/iot/edge-gateway/get-by-key',
      params: { gatewayKey }
    })
  },

  // 创建网关
  create: async (data: EdgeGatewayCreateReqVO) => {
    return await request.post({ url: '/iot/edge-gateway/create', data })
  },

  // 更新网关
  update: async (data: EdgeGatewayUpdateReqVO) => {
    return await request.put({ url: '/iot/edge-gateway/update', data })
  },

  // 删除网关
  delete: async (id: number) => {
    return await request.delete({
      url: '/iot/edge-gateway/delete',
      params: { id }
    })
  },

  // 启用网关
  enable: async (id: number) => {
    return await request.put({ url: '/iot/edge-gateway/enable', params: { id } })
  },

  // 禁用网关
  disable: async (id: number) => {
    return await request.put({ url: '/iot/edge-gateway/disable', params: { id } })
  }
}
