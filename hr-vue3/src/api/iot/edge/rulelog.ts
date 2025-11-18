import request from '@/config/axios'

/**
 * 边缘规则执行日志响应VO
 */
export interface EdgeRuleLogVO {
  id: number // 主键
  ruleId: number // 边缘规则编号
  gatewayId: number // 边缘网关编号
  executeTime: Date // 执行时间
  executeStatus: number // 执行状态: 1=成功, 2=失败
  executeResult: string // 执行结果
  createTime: Date // 创建时间
}

/**
 * 边缘规则执行日志分页查询请求VO
 */
export interface EdgeRuleLogPageReqVO {
  pageNo: number // 页码
  pageSize: number // 每页数量
  ruleId?: number // 规则ID
  gatewayId?: number // 网关ID
  executeStatus?: number // 执行状态
}

// 边缘规则执行状态映射
export const EdgeRuleExecuteStatusMap = {
  1: { label: '成功', type: 'success' },
  2: { label: '失败', type: 'danger' }
}

// 边缘规则日志 API
export const EdgeRuleLogApi = {
  // 获取详情
  get: async (id: number) => {
    return await request.get({ url: '/iot/edge-rule-log/get', params: { id } })
  },

  // 分页查询
  getPage: async (params: EdgeRuleLogPageReqVO) => {
    return await request.get({ url: '/iot/edge-rule-log/page', params })
  },

  // 删除日志
  delete: async (id: number) => {
    return await request.delete({ url: '/iot/edge-rule-log/delete', params: { id } })
  },

  // 清空规则的所有日志
  clear: async (ruleId: number) => {
    return await request.delete({ url: '/iot/edge-rule-log/clear', params: { ruleId } })
  }
}
