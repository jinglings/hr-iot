import request from '@/config/axios'

// IoT 能源区域 VO
export interface IotEnergyAreaVO {
  id: number // 区域 ID，主键，自增
  buildingId: number // 建筑 ID
  buildingName?: string // 建筑名称（用于显示）
  name: string // 区域名称
  code: string // 区域编码
  description: string // 区域描述
  sort: number // 排序
  status: number // 区域状态：0-禁用 1-启用
  createTime: Date // 创建时间
  updateTime: Date // 更新时间
}

// IoT 能源区域 分页查询请求 VO
export interface IotEnergyAreaPageReqVO extends PageParam {
  buildingId?: number // 建筑 ID
  name?: string // 区域名称
  code?: string // 区域编码
  status?: number // 区域状态
  createTime?: Date[] // 创建时间
}

// IoT 能源区域 简化 VO（用于下拉选择）
export interface IotEnergyAreaSimpleVO {
  id: number // 区域 ID
  name: string // 区域名称
  code: string // 区域编码
  buildingId: number // 建筑 ID
}

// 查询 IoT 能源区域分页
export const getIotEnergyAreaPage = (params: IotEnergyAreaPageReqVO) => {
  return request.get({ url: '/iot/energy/area/page', params })
}

// 查询 IoT 能源区域详情
export const getIotEnergyArea = (id: number) => {
  return request.get({ url: `/iot/energy/area/get?id=${id}` })
}

// 新增 IoT 能源区域
export const createIotEnergyArea = (data: IotEnergyAreaVO) => {
  return request.post({ url: '/iot/energy/area/create', data })
}

// 修改 IoT 能源区域
export const updateIotEnergyArea = (data: IotEnergyAreaVO) => {
  return request.put({ url: '/iot/energy/area/update', data })
}

// 删除 IoT 能源区域
export const deleteIotEnergyArea = (id: number) => {
  return request.delete({ url: `/iot/energy/area/delete?id=${id}` })
}

// 根据建筑ID获取区域列表
export const getIotEnergyAreaListByBuildingId = (buildingId: number) => {
  return request.get({ url: `/iot/energy/area/list-by-building-id?buildingId=${buildingId}` })
}

// 获取启用的区域简化列表
export const getIotEnergyAreaSimpleList = () => {
  return request.get({ url: '/iot/energy/area/simple-list' })
}
