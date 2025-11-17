import request from '@/config/axios'

// IoT 能源楼层 VO
export interface IotEnergyFloorVO {
  id: number // 楼层 ID，主键，自增
  buildingId: number // 建筑 ID
  buildingName?: string // 建筑名称（用于显示）
  areaId: number // 区域 ID
  areaName?: string // 区域名称（用于显示）
  name: string // 楼层名称
  code: string // 楼层编码
  floorNumber: number // 楼层号（负数表示地下楼层）
  area: number // 楼层面积（平方米）
  description: string // 楼层描述
  sort: number // 排序
  status: number // 楼层状态：0-禁用 1-启用
  createTime: Date // 创建时间
  updateTime: Date // 更新时间
}

// IoT 能源楼层 分页查询请求 VO
export interface IotEnergyFloorPageReqVO extends PageParam {
  buildingId?: number // 建筑 ID
  areaId?: number // 区域 ID
  name?: string // 楼层名称
  code?: string // 楼层编码
  status?: number // 楼层状态
  createTime?: Date[] // 创建时间
}

// IoT 能源楼层 简化 VO（用于下拉选择）
export interface IotEnergyFloorSimpleVO {
  id: number // 楼层 ID
  name: string // 楼层名称
  code: string // 楼层编码
  buildingId: number // 建筑 ID
  areaId: number // 区域 ID
}

// 查询 IoT 能源楼层分页
export const getIotEnergyFloorPage = (params: IotEnergyFloorPageReqVO) => {
  return request.get({ url: '/iot/energy-floor/page', params })
}

// 查询 IoT 能源楼层详情
export const getIotEnergyFloor = (id: number) => {
  return request.get({ url: `/iot/energy-floor/get?id=${id}` })
}

// 新增 IoT 能源楼层
export const createIotEnergyFloor = (data: IotEnergyFloorVO) => {
  return request.post({ url: '/iot/energy-floor/create', data })
}

// 修改 IoT 能源楼层
export const updateIotEnergyFloor = (data: IotEnergyFloorVO) => {
  return request.put({ url: '/iot/energy-floor/update', data })
}

// 删除 IoT 能源楼层
export const deleteIotEnergyFloor = (id: number) => {
  return request.delete({ url: `/iot/energy-floor/delete?id=${id}` })
}

// 根据建筑ID获取楼层列表
export const getIotEnergyFloorListByBuildingId = (buildingId: number) => {
  return request.get({ url: `/iot/energy-floor/list-by-building-id?buildingId=${buildingId}` })
}

// 根据区域ID获取楼层列表
export const getIotEnergyFloorListByAreaId = (areaId: number) => {
  return request.get({ url: `/iot/energy-floor/list-by-area-id?areaId=${areaId}` })
}

// 获取启用的楼层简化列表
export const getIotEnergyFloorSimpleList = () => {
  return request.get({ url: '/iot/energy-floor/simple-list' })
}
