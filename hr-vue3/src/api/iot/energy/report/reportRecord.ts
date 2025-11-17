import request from '@/config/axios'

// IoT 能源报表记录 VO
export interface IotEnergyReportRecordVO {
  id: number // 报表记录 ID，主键，自增
  templateId: number // 报表模板 ID
  templateName?: string // 报表模板名称（用于显示）
  name: string // 报表名称
  type: string // 报表类型：daily-日报，weekly-周报，monthly-月报，yearly-年报，custom-自定义
  startTime: Date // 统计开始时间
  endTime: Date // 统计结束时间
  buildingId: number // 建筑 ID
  buildingName?: string // 建筑名称（用于显示）
  data: string // 报表数据（JSON格式）
  filePath: string // 报表文件路径（Excel或PDF）
  generateTime: Date // 生成时间
  status: number // 报表状态：0-生成中 1-生成成功 2-生成失败
  errorMessage: string // 错误信息（生成失败时）
  createTime: Date // 创建时间
  createBy: string // 创建人
}

// IoT 能源报表记录 分页查询请求 VO
export interface IotEnergyReportRecordPageReqVO extends PageParam {
  templateId?: number // 报表模板 ID
  name?: string // 报表名称
  type?: string // 报表类型
  buildingId?: number // 建筑 ID
  status?: number // 报表状态
  startTime?: Date[] // 统计开始时间范围
  endTime?: Date[] // 统计结束时间范围
  createTime?: Date[] // 创建时间
}

// IoT 能源报表生成请求 VO
export interface IotEnergyReportGenerateReqVO {
  templateId: number // 报表模板 ID
  name: string // 报表名称
  startTime: Date // 统计开始时间
  endTime: Date // 统计结束时间
  buildingId?: number // 建筑 ID（可选）
}

// 查询 IoT 能源报表记录分页
export const getIotEnergyReportRecordPage = (params: IotEnergyReportRecordPageReqVO) => {
  return request.get({ url: '/iot/energy-report-record/page', params })
}

// 查询 IoT 能源报表记录详情
export const getIotEnergyReportRecord = (id: number) => {
  return request.get({ url: `/iot/energy-report-record/get?id=${id}` })
}

// 生成能源报表
export const generateIotEnergyReport = (data: IotEnergyReportGenerateReqVO) => {
  return request.post({ url: '/iot/energy-report-record/generate', data })
}

// 删除 IoT 能源报表记录
export const deleteIotEnergyReportRecord = (id: number) => {
  return request.delete({ url: `/iot/energy-report-record/delete?id=${id}` })
}

// 导出报表为 Excel
export const exportIotEnergyReportExcel = (id: number) => {
  return request.download({ url: `/iot/energy-report-record/export-excel?id=${id}` })
}

// 导出报表为 PDF
export const exportIotEnergyReportPdf = (id: number) => {
  return request.download({ url: `/iot/energy-report-record/export-pdf?id=${id}` })
}
