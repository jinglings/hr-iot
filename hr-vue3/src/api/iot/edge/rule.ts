import request from '@/config/axios'

/**
 * 边缘规则响应VO
 */
export interface EdgeRuleVO {
  id: number // 主键
  name: string // 规则名称
  gatewayId: number // 网关ID

  // 规则配置
  ruleType: string // 规则类型: SCENE, DATA_FLOW, AI_INFERENCE
  triggerConfig: string // 触发器配置(JSON字符串)
  conditionConfig: string // 条件配置(JSON字符串)
  actionConfig: string // 动作配置(JSON字符串)

  // 执行配置
  executeLocal: number // 是否本地执行: 0=否, 1=是
  priority: number // 优先级

  // 状态信息
  status: number // 状态: 0=禁用, 1=启用
  deployStatus: number // 部署状态: 0=未部署, 1=已部署, 2=部署失败
  deployTime: Date // 部署时间
  deployError: string // 部署错误信息

  // 统计信息
  executeCount: number // 执行次数
  lastExecuteTime: Date // 最后执行时间

  description: string // 描述
  createTime: Date // 创建时间
  updateTime: Date // 更新时间
}

/**
 * 边缘规则分页查询请求VO
 */
export interface EdgeRulePageReqVO {
  pageNo: number // 页码
  pageSize: number // 每页数量
  name?: string // 规则名称（模糊搜索）
  gatewayId?: number // 网关ID
  ruleType?: string // 规则类型
  status?: number // 状态
  deployStatus?: number // 部署状态
}

/**
 * 边缘规则创建请求VO
 */
export interface EdgeRuleCreateReqVO {
  name: string // 规则名称
  gatewayId: number // 网关ID
  ruleType: string // 规则类型
  triggerConfig: string // 触发器配置(JSON)
  conditionConfig?: string // 条件配置(JSON)
  actionConfig: string // 动作配置(JSON)
  executeLocal?: number // 是否本地执行
  priority?: number // 优先级
  description?: string // 描述
}

/**
 * 边缘规则更新请求VO
 */
export interface EdgeRuleUpdateReqVO {
  id: number // 主键
  name?: string // 规则名称
  ruleType?: string // 规则类型
  triggerConfig?: string // 触发器配置(JSON)
  conditionConfig?: string // 条件配置(JSON)
  actionConfig?: string // 动作配置(JSON)
  executeLocal?: number // 是否本地执行
  priority?: number // 优先级
  description?: string // 描述
}

// 边缘规则类型枚举
export enum EdgeRuleType {
  SCENE = 'SCENE', // 场景联动
  DATA_FLOW = 'DATA_FLOW', // 数据流转
  AI_INFERENCE = 'AI_INFERENCE' // AI推理
}

// 边缘规则类型映射
export const EdgeRuleTypeMap = {
  [EdgeRuleType.SCENE]: { label: '场景联动', color: '#409EFF' },
  [EdgeRuleType.DATA_FLOW]: { label: '数据流转', color: '#67C23A' },
  [EdgeRuleType.AI_INFERENCE]: { label: 'AI推理', color: '#E6A23C' }
}

// 边缘规则部署状态枚举
export enum EdgeRuleDeployStatus {
  NOT_DEPLOYED = 0, // 未部署
  DEPLOYED = 1, // 已部署
  DEPLOY_FAILED = 2 // 部署失败
}

// 边缘规则部署状态映射
export const EdgeRuleDeployStatusMap = {
  [EdgeRuleDeployStatus.NOT_DEPLOYED]: { label: '未部署', type: 'info' },
  [EdgeRuleDeployStatus.DEPLOYED]: { label: '已部署', type: 'success' },
  [EdgeRuleDeployStatus.DEPLOY_FAILED]: { label: '部署失败', type: 'danger' }
}

// 边缘规则 API
export const EdgeRuleApi = {
  // 分页查询
  getPage: async (params: EdgeRulePageReqVO) => {
    return await request.get({ url: '/iot/edge-rule/page', params })
  },

  // 获取详情
  get: async (id: number) => {
    return await request.get({ url: '/iot/edge-rule/get', params: { id } })
  },

  // 创建规则
  create: async (data: EdgeRuleCreateReqVO) => {
    return await request.post({ url: '/iot/edge-rule/create', data })
  },

  // 更新规则
  update: async (data: EdgeRuleUpdateReqVO) => {
    return await request.put({ url: '/iot/edge-rule/update', data })
  },

  // 删除规则
  delete: async (id: number) => {
    return await request.delete({ url: '/iot/edge-rule/delete', params: { id } })
  },

  // 部署规则
  deploy: async (id: number) => {
    return await request.post({ url: '/iot/edge-rule/deploy', params: { id } })
  },

  // 取消部署
  undeploy: async (id: number) => {
    return await request.post({ url: '/iot/edge-rule/undeploy', params: { id } })
  },

  // 启用规则
  enable: async (id: number) => {
    return await request.put({ url: '/iot/edge-rule/enable', params: { id } })
  },

  // 禁用规则
  disable: async (id: number) => {
    return await request.put({ url: '/iot/edge-rule/disable', params: { id } })
  }
}
