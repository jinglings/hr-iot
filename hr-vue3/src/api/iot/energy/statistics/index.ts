import request from '@/config/axios'

// IoT 能源统计数据 VO
export interface IotEnergyStatisticsVO {
  id: number // 统计数据 ID，主键，自增
  meterId: number // 计量点 ID
  meterName?: string // 计量点名称（用于显示）
  energyTypeId: number // 能源类型 ID
  energyTypeName?: string // 能源类型名称（用于显示）
  buildingId: number // 建筑 ID
  buildingName?: string // 建筑名称（用于显示）
  period: string // 统计周期：hour-小时，day-天，month-月，year-年
  periodValue: string // 周期值（如：2024-01-01）
  startTime: Date // 统计开始时间
  endTime: Date // 统计结束时间
  totalEnergy: number // 总能耗
  avgPower: number // 平均功率
  maxPower: number // 最大功率
  minPower: number // 最小功率
  coalConsumption: number // 折标煤（吨标煤）
  carbonEmission: number // 碳排放（吨CO2）
  unit: string // 单位
  createTime: Date // 创建时间
}

// IoT 能源统计 分页查询请求 VO
export interface IotEnergyStatisticsPageReqVO extends PageParam {
  meterId?: number // 计量点 ID
  energyTypeId?: number // 能源类型 ID
  buildingId?: number // 建筑 ID
  period?: string // 统计周期
  startTime?: Date // 开始时间
  endTime?: Date // 结束时间
}

// IoT 能源统计 按计量点查询请求 VO
export interface IotEnergyStatsByMeterReqVO {
  meterId: number // 计量点 ID
  period: string // 统计周期：hour-小时，day-天，month-月，year-年
  startTime: Date // 开始时间
  endTime: Date // 结束时间
}

// IoT 能源统计 按建筑查询请求 VO
export interface IotEnergyStatsByBuildingReqVO {
  buildingId: number // 建筑 ID
  period: string // 统计周期：hour-小时，day-天，month-月，year-年
  startTime: Date // 开始时间
  endTime: Date // 结束时间
}

// IoT 能源统计 按能源类型查询请求 VO
export interface IotEnergyStatsByTypeReqVO {
  energyTypeId: number // 能源类型 ID
  period: string // 统计周期：hour-小时，day-天，month-月，year-年
  startTime: Date // 开始时间
  endTime: Date // 结束时间
}

// 查询 IoT 能源统计数据分页
export const getIotEnergyStatisticsPage = (params: IotEnergyStatisticsPageReqVO) => {
  return request.get({ url: '/iot/energy/statistics/page', params })
}

// 按计量点和周期查询统计数据
export const getIotEnergyStatsByMeter = (params: IotEnergyStatsByMeterReqVO) => {
  return request.get({ url: '/iot/energy/statistics/by-meter', params })
}

// 按建筑和周期查询统计数据
export const getIotEnergyStatsByBuilding = (params: IotEnergyStatsByBuildingReqVO) => {
  return request.get({ url: '/iot/energy/statistics/by-building', params })
}

// 按能源类型和周期查询统计数据
export const getIotEnergyStatsByType = (params: IotEnergyStatsByTypeReqVO) => {
  return request.get({ url: '/iot/energy/statistics/by-type', params })
}
