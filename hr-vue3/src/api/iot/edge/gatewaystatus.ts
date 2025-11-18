import request from '@/config/axios'

/**
 * 边缘网关状态响应VO
 */
export interface EdgeGatewayStatusVO {
  id: number // 主键
  gatewayId: number // 边缘网关编号
  status: number // 状态: 0=离线, 1=在线, 2=异常
  cpuUsage: number // CPU使用率 (%)
  memoryUsage: number // 内存使用率 (%)
  diskUsage: number // 磁盘使用率 (%)
  lastHeartbeatTime: Date // 最后心跳时间
  createTime: Date // 创建时间
}

/**
 * 边缘网关状态分页查询请求VO
 */
export interface EdgeGatewayStatusPageReqVO {
  pageNo: number // 页码
  pageSize: number // 每页数量
  gatewayId?: number // 网关ID
  status?: number // 状态
}

// 边缘网关状态API
export const EdgeGatewayStatusApi = {
  // 获取详情
  get: async (id: number) => {
    return await request.get({ url: '/iot/edge-gateway-status/get', params: { id } })
  },

  // 根据网关ID获取状态
  getByGatewayId: async (gatewayId: number) => {
    return await request.get({
      url: '/iot/edge-gateway-status/get-by-gateway',
      params: { gatewayId }
    })
  },

  // 分页查询
  getPage: async (params: EdgeGatewayStatusPageReqVO) => {
    return await request.get({ url: '/iot/edge-gateway-status/page', params })
  }
}
