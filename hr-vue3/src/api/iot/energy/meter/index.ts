import request from '@/config/axios'

// IoT 能源计量点 VO
export interface IotEnergyMeterVO {
  id: number // 计量点 ID，主键，自增
  name: string // 计量点名称
  code: string // 计量点编码
  energyTypeId: number // 能源类型 ID
  energyTypeName?: string // 能源类型名称（用于显示）
  deviceId: number // 设备 ID
  deviceName?: string // 设备名称（用于显示）
  deviceProperty: string // 设备属性（物模型属性标识符）
  buildingId: number // 建筑 ID
  buildingName?: string // 建筑名称（用于显示）
  areaId: number // 区域 ID
  areaName?: string // 区域名称（用于显示）
  floorId: number // 楼层 ID
  floorName?: string // 楼层名称（用于显示）
  roomId: number // 房间 ID
  roomName?: string // 房间名称（用于显示）
  meterLevel: number // 计量点级别：1-一级表，2-二级表，3-三级表
  parentId: number // 父级计量点 ID
  parentName?: string // 父级计量点名称（用于显示）
  isVirtual: boolean // 是否虚拟表
  formula: string // 计算公式（虚拟表专用，如：meter1 + meter2）
  coefficient: number // 计量倍率
  description: string // 计量点描述
  status: number // 计量点状态：0-禁用 1-启用
  createTime: Date // 创建时间
  updateTime: Date // 更新时间
}

// IoT 能源计量点 分页查询请求 VO
export interface IotEnergyMeterPageReqVO extends PageParam {
  name?: string // 计量点名称
  code?: string // 计量点编码
  energyTypeId?: number // 能源类型 ID
  deviceId?: number // 设备 ID
  buildingId?: number // 建筑 ID
  areaId?: number // 区域 ID
  floorId?: number // 楼层 ID
  roomId?: number // 房间 ID
  meterLevel?: number // 计量点级别
  parentId?: number // 父级计量点 ID
  isVirtual?: boolean // 是否虚拟表
  status?: number // 计量点状态
  createTime?: Date[] // 创建时间
}

// IoT 能源计量点 简化 VO（用于下拉选择）
export interface IotEnergyMeterSimpleVO {
  id: number // 计量点 ID
  name: string // 计量点名称
  code: string // 计量点编码
  energyTypeId: number // 能源类型 ID
  isVirtual: boolean // 是否虚拟表
}

// 查询 IoT 能源计量点分页
export const getIotEnergyMeterPage = (params: IotEnergyMeterPageReqVO) => {
  return request.get({ url: '/iot/energy-meter/page', params })
}

// 查询 IoT 能源计量点详情
export const getIotEnergyMeter = (id: number) => {
  return request.get({ url: `/iot/energy-meter/get?id=${id}` })
}

// 新增 IoT 能源计量点
export const createIotEnergyMeter = (data: IotEnergyMeterVO) => {
  return request.post({ url: '/iot/energy-meter/create', data })
}

// 修改 IoT 能源计量点
export const updateIotEnergyMeter = (data: IotEnergyMeterVO) => {
  return request.put({ url: '/iot/energy-meter/update', data })
}

// 删除 IoT 能源计量点
export const deleteIotEnergyMeter = (id: number) => {
  return request.delete({ url: `/iot/energy-meter/delete?id=${id}` })
}

// 根据能源类型ID获取计量点列表
export const getIotEnergyMeterListByEnergyTypeId = (energyTypeId: number) => {
  return request.get({ url: `/iot/energy-meter/list-by-energy-type-id?energyTypeId=${energyTypeId}` })
}

// 根据设备ID获取计量点列表
export const getIotEnergyMeterListByDeviceId = (deviceId: number) => {
  return request.get({ url: `/iot/energy-meter/list-by-device-id?deviceId=${deviceId}` })
}

// 根据建筑ID获取计量点列表
export const getIotEnergyMeterListByBuildingId = (buildingId: number) => {
  return request.get({ url: `/iot/energy-meter/list-by-building-id?buildingId=${buildingId}` })
}

// 根据区域ID获取计量点列表
export const getIotEnergyMeterListByAreaId = (areaId: number) => {
  return request.get({ url: `/iot/energy-meter/list-by-area-id?areaId=${areaId}` })
}

// 根据楼层ID获取计量点列表
export const getIotEnergyMeterListByFloorId = (floorId: number) => {
  return request.get({ url: `/iot/energy-meter/list-by-floor-id?floorId=${floorId}` })
}

// 根据房间ID获取计量点列表
export const getIotEnergyMeterListByRoomId = (roomId: number) => {
  return request.get({ url: `/iot/energy-meter/list-by-room-id?roomId=${roomId}` })
}

// 根据父级计量点ID获取子计量点列表
export const getIotEnergyMeterListByParentId = (parentId: number) => {
  return request.get({ url: `/iot/energy-meter/list-by-parent-id?parentId=${parentId}` })
}

// 获取启用的计量点简化列表
export const getIotEnergyMeterSimpleList = () => {
  return request.get({ url: '/iot/energy-meter/simple-list' })
}
