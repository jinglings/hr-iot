import request from '@/config/axios'

// IoT 能源实时数据 VO
export interface IotEnergyRealtimeDataVO {
  ts: Date // 时间戳
  meterId: number // 计量点 ID
  meterName?: string // 计量点名称（用于显示）
  instantPower: number // 瞬时功率/流量
  cumulativeValue: number // 累计值
  voltage: number // 电压（电表专用）
  current: number // 电流（电表专用）
  powerFactor: number // 功率因数（电表专用）
  dataQuality: number // 数据质量：0-正常，1-异常，2-估算
  energyTypeId: number // 能源类型 ID
  energyTypeName?: string // 能源类型名称（用于显示）
  buildingId: number // 建筑 ID
  buildingName?: string // 建筑名称（用于显示）
  areaId: number // 区域 ID
  areaName?: string // 区域名称（用于显示）
  floorId: number // 楼层 ID
  floorName?: string // 楼层名称（用于显示）
  roomId: number // 房间 ID
  roomName?: string // 房间名称（用于显示）
}

// IoT 能源数据 分页查询请求 VO
export interface IotEnergyDataPageReqVO extends PageParam {
  meterId?: number // 计量点 ID
  energyTypeId?: number // 能源类型 ID
  buildingId?: number // 建筑 ID
  areaId?: number // 区域 ID
  floorId?: number // 楼层 ID
  roomId?: number // 房间 ID
  startTime?: Date // 开始时间
  endTime?: Date // 结束时间
  dataQuality?: number // 数据质量
}

// IoT 能源数据 时间范围查询请求 VO
export interface IotEnergyDataRangeReqVO {
  meterId: number // 计量点 ID
  startTime: Date // 开始时间
  endTime: Date // 结束时间
}

// IoT 能源数据 聚合查询请求 VO
export interface IotEnergyDataAggregateReqVO {
  meterIds: number[] // 计量点 ID 列表
  startTime: Date // 开始时间
  endTime: Date // 结束时间
  interval: string // 聚合间隔：1m-1分钟，1h-1小时，1d-1天
}

// IoT 能源数据 聚合结果 VO
export interface IotEnergyDataAggregateVO {
  ts: Date // 时间戳
  meterId: number // 计量点 ID
  avgValue: number // 平均值
  maxValue: number // 最大值
  minValue: number // 最小值
  sumValue: number // 总和
}

// IoT 建筑能耗统计 VO
export interface IotBuildingEnergyStatsVO {
  buildingId: number // 建筑 ID
  buildingName: string // 建筑名称
  energyTypeId: number // 能源类型 ID
  energyTypeName: string // 能源类型名称
  totalEnergy: number // 总能耗
  unit: string // 单位
}

// IoT 能源类型能耗统计 VO
export interface IotEnergyTypeStatsVO {
  energyTypeId: number // 能源类型 ID
  energyTypeName: string // 能源类型名称
  totalEnergy: number // 总能耗
  unit: string // 单位
  percentage: number // 占比（百分比）
}

// 查询 IoT 能源实时数据分页
export const getIotEnergyDataPage = (params: IotEnergyDataPageReqVO) => {
  return request.get({ url: '/iot/energy/data/page', params })
}

// 获取计量点最新数据
export const getIotEnergyMeterLatestData = (meterId: number) => {
  return request.get({ url: `/iot/energy/data/latest?meterId=${meterId}` })
}

// 查询时间范围内的能源数据
export const getIotEnergyDataByRange = (params: IotEnergyDataRangeReqVO) => {
  return request.get({ url: '/iot/energy/data/range', params })
}

// 查询聚合数据（按时间间隔统计）
export const getIotEnergyDataAggregate = (params: IotEnergyDataAggregateReqVO) => {
  return request.get({ url: '/iot/energy/data/aggregate', params })
}

// 按建筑统计能耗
export const getIotEnergyStatsByBuilding = (params: { startTime: Date, endTime: Date }) => {
  return request.get({ url: '/iot/energy/data/stats-by-building', params })
}

// 按能源类型统计能耗
export const getIotEnergyStatsByType = (params: { startTime: Date, endTime: Date }) => {
  return request.get({ url: '/iot/energy/data/stats-by-type', params })
}
