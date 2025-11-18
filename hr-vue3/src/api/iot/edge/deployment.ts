import request from '@/config/axios'

/**
 * 边缘模型部署响应VO
 */
export interface EdgeModelDeploymentVO {
  id: number // 主键
  modelId: number // AI模型编号
  gatewayId: number // 边缘网关编号
  deployStatus: number // 部署状态: 1=部署中, 2=部署成功, 3=部署失败
  deployTime: Date // 部署时间
  status: number // 状态: 0=停止, 1=运行
  createTime: Date // 创建时间
}

/**
 * 边缘模型部署分页查询请求VO
 */
export interface EdgeModelDeploymentPageReqVO {
  pageNo: number // 页码
  pageSize: number // 每页数量
  modelId?: number // 模型ID
  gatewayId?: number // 网关ID
  deployStatus?: number // 部署状态
  status?: number // 运行状态
}

// 边缘模型部署状态枚举
export enum EdgeModelDeployStatus {
  DEPLOYING = 1, // 部署中
  DEPLOYED = 2, // 部署成功
  DEPLOY_FAILED = 3 // 部署失败
}

// 边缘模型部署状态映射
export const EdgeModelDeployStatusMap = {
  [EdgeModelDeployStatus.DEPLOYING]: { label: '部署中', type: 'warning' },
  [EdgeModelDeployStatus.DEPLOYED]: { label: '部署成功', type: 'success' },
  [EdgeModelDeployStatus.DEPLOY_FAILED]: { label: '部署失败', type: 'danger' }
}

// 边缘模型运行状态映射
export const EdgeModelRunStatusMap = {
  0: { label: '停止', type: 'info' },
  1: { label: '运行中', type: 'success' }
}

// 边缘模型部署 API
export const EdgeModelDeploymentApi = {
  // 部署模型到网关
  deploy: async (modelId: number, gatewayId: number) => {
    return await request.post({
      url: '/iot/edge-model-deployment/deploy',
      params: { modelId, gatewayId }
    })
  },

  // 取消部署
  undeploy: async (id: number) => {
    return await request.post({
      url: '/iot/edge-model-deployment/undeploy',
      params: { id }
    })
  },

  // 获取详情
  get: async (id: number) => {
    return await request.get({ url: '/iot/edge-model-deployment/get', params: { id } })
  },

  // 分页查询
  getPage: async (params: EdgeModelDeploymentPageReqVO) => {
    return await request.get({ url: '/iot/edge-model-deployment/page', params })
  },

  // 启动模型
  start: async (id: number) => {
    return await request.put({ url: '/iot/edge-model-deployment/start', params: { id } })
  },

  // 停止模型
  stop: async (id: number) => {
    return await request.put({ url: '/iot/edge-model-deployment/stop', params: { id } })
  }
}
