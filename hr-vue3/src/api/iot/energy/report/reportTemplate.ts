import request from '@/config/axios'

// IoT 能源报表模板 VO
export interface IotEnergyReportTemplateVO {
  id: number // 报表模板 ID，主键，自增
  name: string // 模板名称
  code: string // 模板编码
  type: string // 报表类型：daily-日报，weekly-周报，monthly-月报，yearly-年报，custom-自定义
  description: string // 模板描述
  config: string // 报表配置（JSON格式，包含报表字段、计算规则等）
  status: number // 模板状态：0-禁用 1-启用
  createTime: Date // 创建时间
  updateTime: Date // 更新时间
}

// IoT 能源报表模板 分页查询请求 VO
export interface IotEnergyReportTemplatePageReqVO extends PageParam {
  name?: string // 模板名称
  code?: string // 模板编码
  type?: string // 报表类型
  status?: number // 模板状态
  createTime?: Date[] // 创建时间
}

// IoT 能源报表模板 简化 VO（用于下拉选择）
export interface IotEnergyReportTemplateSimpleVO {
  id: number // 报表模板 ID
  name: string // 模板名称
  code: string // 模板编码
  type: string // 报表类型
}

// 查询 IoT 能源报表模板分页
export const getIotEnergyReportTemplatePage = (params: IotEnergyReportTemplatePageReqVO) => {
  return request.get({ url: '/iot/energy/report/template/page', params })
}

// 查询 IoT 能源报表模板详情
export const getIotEnergyReportTemplate = (id: number) => {
  return request.get({ url: `/iot/energy/report/template/get?id=${id}` })
}

// 新增 IoT 能源报表模板
export const createIotEnergyReportTemplate = (data: IotEnergyReportTemplateVO) => {
  return request.post({ url: '/iot/energy/report/template/create', data })
}

// 修改 IoT 能源报表模板
export const updateIotEnergyReportTemplate = (data: IotEnergyReportTemplateVO) => {
  return request.put({ url: '/iot/energy/report/template/update', data })
}

// 删除 IoT 能源报表模板
export const deleteIotEnergyReportTemplate = (id: number) => {
  return request.delete({ url: `/iot/energy/report/template/delete?id=${id}` })
}

// 获取启用的报表模板简化列表
export const getIotEnergyReportTemplateSimpleList = () => {
  return request.get({ url: '/iot/energy/report/template/simple-list' })
}
