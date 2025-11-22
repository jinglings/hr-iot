import request from '@/config/axios'

// ========== BACnet 设备发现相关 VO ==========

// BACnet 设备发现记录 VO
export interface BACnetDiscoveryRecordVO {
  id: number // 主键ID
  instanceNumber: number // BACnet 设备实例号
  deviceName: string // 设备名称
  ipAddress: string // IP 地址
  vendorName: string // 厂商名称
  modelName: string // 型号
  deviceId?: number // 绑定的 IoT 设备 ID
  bindStatus: number // 绑定状态（0=未绑定，1=已绑定）
  onlineStatus: number // 在线状态（0=离线，1=在线）
  lastSeenTime: Date // 最后发现时间
  createTime?: Date // 创建时间
}

// BACnet 设备绑定请求 VO
export interface BACnetDeviceBindReqVO {
  discoveryRecordId: number // 发现记录 ID
  deviceId: number // IoT 设备 ID
  pollingEnabled: boolean // 是否启用轮询
  pollingInterval: number // 轮询间隔（毫秒）
  autoMap?: boolean // 是否自动映射属性
}

// ========== BACnet 设备配置相关 VO ==========

// BACnet 设备配置 VO
export interface BACnetDeviceConfigVO {
  id?: number // 主键ID
  deviceId: number // IoT 设备 ID
  instanceNumber: number // BACnet 设备实例号
  ipAddress: string // BACnet 设备 IP 地址
  pollingEnabled: boolean // 是否启用轮询
  pollingInterval: number // 轮询间隔（毫秒，默认 5000）
  description?: string // 配置描述
  createTime?: Date // 创建时间
  updateTime?: Date // 更新时间
}

// BACnet 设备配置分页请求 VO
export interface BACnetDeviceConfigPageReqVO {
  pageNo: number
  pageSize: number
  deviceId?: number
  pollingEnabled?: boolean
}

// ========== BACnet 属性映射相关 VO ==========

// BACnet 属性映射 VO
export interface BACnetPropertyMappingVO {
  id?: number // 主键ID
  deviceId: number // IoT 设备 ID
  identifier: string // 物模型属性标识符
  objectType: string // BACnet 对象类型
  objectInstance: number // BACnet 对象实例号
  propertyIdentifier: string // BACnet 属性标识符
  dataType: string // 数据类型
  accessMode: string // 访问模式（r/w/rw）
  pollingEnabled: boolean // 是否启用轮询
  unitConversion?: string // 单位转换公式
  valueMapping?: string // 值映射（JSON 格式）
  priority?: number // 写入优先级（1-16）
  createTime?: Date // 创建时间
  updateTime?: Date // 更新时间
}

// BACnet 属性映射分页请求 VO
export interface BACnetPropertyMappingPageReqVO {
  pageNo: number
  pageSize: number
  deviceId?: number
  identifier?: string
  pollingEnabled?: boolean
}

// ========== BACnet 测试相关 VO ==========

// BACnet 连接测试请求 VO
export interface BACnetConnectionTestReqVO {
  deviceId: number // 设备 ID
}

// BACnet 属性读取请求 VO
export interface BACnetPropertyReadReqVO {
  deviceId: number // 设备 ID
  mappingId: number // 属性映射 ID
}

// BACnet 属性写入请求 VO
export interface BACnetPropertyWriteReqVO {
  deviceId: number // 设备 ID
  mappingId: number // 属性映射 ID
  value: any // 写入值
}

// ========== API 接口定义 ==========

export const BACnetApi = {
  // ========== 设备发现接口 ==========

  /**
   * 立即执行设备发现
   */
  discoverNow: async () => {
    return await request.post({ url: `/iot/bacnet/discovery/discover-now` })
  },

  /**
   * 获取未绑定设备列表
   */
  getUnboundList: async () => {
    return await request.get({ url: `/iot/bacnet/discovery/unbound-list` })
  },

  /**
   * 获取已绑定设备列表
   */
  getBoundList: async () => {
    return await request.get({ url: `/iot/bacnet/discovery/bound-list` })
  },

  /**
   * 绑定 BACnet 设备
   */
  bindDevice: async (data: BACnetDeviceBindReqVO) => {
    return await request.post({ url: `/iot/bacnet/discovery/bind`, data })
  },

  /**
   * 删除发现记录
   */
  deleteDiscoveryRecord: async (id: number) => {
    return await request.delete({ url: `/iot/bacnet/discovery/delete?id=` + id })
  },

  // ========== 设备配置接口 ==========

  /**
   * 创建 BACnet 设备配置
   */
  createDeviceConfig: async (data: BACnetDeviceConfigVO) => {
    return await request.post({ url: `/iot/bacnet/config/device/create`, data })
  },

  /**
   * 更新 BACnet 设备配置
   */
  updateDeviceConfig: async (data: BACnetDeviceConfigVO) => {
    return await request.put({ url: `/iot/bacnet/config/device/update`, data })
  },

  /**
   * 删除 BACnet 设备配置
   */
  deleteDeviceConfig: async (id: number) => {
    return await request.delete({ url: `/iot/bacnet/config/device/delete?id=` + id })
  },

  /**
   * 获取 BACnet 设备配置详情
   */
  getDeviceConfig: async (id: number) => {
    return await request.get({ url: `/iot/bacnet/config/device/get?id=` + id })
  },

  /**
   * 根据设备 ID 获取 BACnet 配置
   */
  getDeviceConfigByDeviceId: async (deviceId: number) => {
    return await request.get({ url: `/iot/bacnet/config/device/get-by-device?deviceId=` + deviceId })
  },

  /**
   * 获取 BACnet 设备配置分页
   */
  getDeviceConfigPage: async (params: BACnetDeviceConfigPageReqVO) => {
    return await request.get({ url: `/iot/bacnet/config/device/page`, params })
  },

  // ========== 属性映射接口 ==========

  /**
   * 创建属性映射
   */
  createPropertyMapping: async (data: BACnetPropertyMappingVO) => {
    return await request.post({ url: `/iot/bacnet/config/mapping/create`, data })
  },

  /**
   * 更新属性映射
   */
  updatePropertyMapping: async (data: BACnetPropertyMappingVO) => {
    return await request.put({ url: `/iot/bacnet/config/mapping/update`, data })
  },

  /**
   * 删除属性映射
   */
  deletePropertyMapping: async (id: number) => {
    return await request.delete({ url: `/iot/bacnet/config/mapping/delete?id=` + id })
  },

  /**
   * 获取属性映射详情
   */
  getPropertyMapping: async (id: number) => {
    return await request.get({ url: `/iot/bacnet/config/mapping/get?id=` + id })
  },

  /**
   * 获取属性映射分页
   */
  getPropertyMappingPage: async (params: BACnetPropertyMappingPageReqVO) => {
    return await request.get({ url: `/iot/bacnet/config/mapping/page`, params })
  },

  /**
   * 根据设备 ID 获取属性映射列表
   */
  getPropertyMappingListByDevice: async (deviceId: number) => {
    return await request.get({
      url: `/iot/bacnet/config/mapping/list-by-device?deviceId=` + deviceId
    })
  },

  // ========== 测试功能接口 ==========

  /**
   * 测试 BACnet 连接
   */
  testConnection: async (data: BACnetConnectionTestReqVO) => {
    return await request.post({ url: `/iot/bacnet/test/connection`, data })
  },

  /**
   * 读取 BACnet 属性值
   */
  readProperty: async (data: BACnetPropertyReadReqVO) => {
    return await request.post({ url: `/iot/bacnet/test/read`, data })
  },

  /**
   * 写入 BACnet 属性值
   */
  writeProperty: async (data: BACnetPropertyWriteReqVO) => {
    return await request.post({ url: `/iot/bacnet/test/write`, data })
  }
}
