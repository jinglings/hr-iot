import request from '@/config/axios'

/**
 * 设备影子响应VO
 */
export interface DeviceShadowVO {
  id: number // 主键
  deviceId: number // 设备ID
  desired: Record<string, any> // 期望状态（JSON对象）
  reported: Record<string, any> // 实际状态（JSON对象）
  delta: Record<string, any> // 差量状态（JSON对象）
  metadata: {
    // 元数据
    desired: Record<string, { timestamp: number }>
    reported: Record<string, { timestamp: number }>
  }
  version: number // 版本号
  desiredVersion: number // 期望状态版本号
  reportedVersion: number // 实际状态版本号
  lastDesiredTime: Date // 最后期望状态更新时间
  lastReportedTime: Date // 最后实际状态上报时间
  createTime: Date // 创建时间
}

/**
 * 设备影子历史记录VO
 */
export interface DeviceShadowHistoryVO {
  id: number // 主键
  deviceId: number // 设备ID
  shadowId: number // 影子ID
  changeType: string // 变更类型：DESIRED, REPORTED, DELTA
  beforeState: Record<string, any> // 变更前状态（JSON）
  afterState: Record<string, any> // 变更后状态（JSON）
  deltaState: Record<string, any> // 差量状态（JSON）
  version: number // 版本号
  creator: string // 创建者
  createTime: Date // 创建时间
}

/**
 * 更新期望状态请求VO
 */
export interface UpdateDesiredReqVO {
  deviceId: number // 设备ID
  desired: Record<string, any> // 期望状态（JSON对象）
}

/**
 * 更新实际状态请求VO（设备上报）
 */
export interface UpdateReportedReqVO {
  deviceId: number // 设备ID
  reported: Record<string, any> // 实际状态（JSON对象）
}

// 影子变更类型枚举
export enum ShadowChangeType {
  DESIRED = 'DESIRED', // 期望状态变更
  REPORTED = 'REPORTED', // 实际状态变更
  DELTA = 'DELTA' // 差量变更
}

// 影子变更类型映射
export const ShadowChangeTypeMap = {
  [ShadowChangeType.DESIRED]: { label: '期望状态', type: 'primary' },
  [ShadowChangeType.REPORTED]: { label: '实际状态', type: 'success' },
  [ShadowChangeType.DELTA]: { label: '差量', type: 'warning' }
}

// 设备影子 API
export const DeviceShadowApi = {
  // 获取设备影子
  getShadow: async (deviceId: number) => {
    return await request.get({
      url: '/iot/device-shadow/get',
      params: { deviceId }
    })
  },

  // 更新期望状态
  updateDesired: async (data: UpdateDesiredReqVO) => {
    return await request.put({
      url: '/iot/device-shadow/update-desired',
      data
    })
  },

  // 更新实际状态（设备上报，一般由设备调用，前端较少使用）
  updateReported: async (data: UpdateReportedReqVO) => {
    return await request.put({
      url: '/iot/device-shadow/update-reported',
      data
    })
  },

  // 删除期望状态中的某个属性
  deleteDesiredProperty: async (deviceId: number, property: string) => {
    return await request.delete({
      url: '/iot/device-shadow/delete-desired-property',
      params: { deviceId, property }
    })
  },

  // 获取差量状态
  getDelta: async (deviceId: number) => {
    return await request.get({
      url: '/iot/device-shadow/get-delta',
      params: { deviceId }
    })
  },

  // 获取影子历史记录（分页）
  getHistory: async (params: { deviceId: number; pageNo: number; pageSize: number }) => {
    return await request.get({
      url: '/iot/device-shadow/history',
      params
    })
  },

  // 清除设备影子
  clearShadow: async (deviceId: number) => {
    return await request.delete({
      url: '/iot/device-shadow/clear',
      params: { deviceId }
    })
  }
}
