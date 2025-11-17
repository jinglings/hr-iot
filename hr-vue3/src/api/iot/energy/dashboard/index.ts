import request from '@/config/axios'

// IoT 能源仪表板 总览数据 VO
export interface IotEnergyDashboardOverviewVO {
  totalEnergy: number // 总能耗
  totalCoal: number // 总标煤（吨标煤）
  totalCarbon: number // 总碳排放（吨CO2）
  totalCost: number // 总费用（元）
  todayEnergy: number // 今日能耗
  yesterdayEnergy: number // 昨日能耗
  dayOnDayRate: number // 日环比增长率（%）
  monthEnergy: number // 本月能耗
  lastMonthEnergy: number // 上月能耗
  monthOnMonthRate: number // 月环比增长率（%）
  onlineMeterCount: number // 在线计量点数量
  offlineMeterCount: number // 离线计量点数量
  alertCount: number // 告警数量
}

// IoT 能源仪表板 排名数据 VO
export interface IotEnergyDashboardRankingVO {
  id: number // ID（建筑ID或计量点ID）
  name: string // 名称（建筑名称或计量点名称）
  energyTypeId: number // 能源类型 ID
  energyTypeName: string // 能源类型名称
  totalEnergy: number // 总能耗
  unit: string // 单位
  rank: number // 排名
  percentage: number // 占比（%）
}

// IoT 能源仪表板 趋势数据 VO
export interface IotEnergyDashboardTrendVO {
  time: string // 时间
  energyValue: number // 能耗值
  coalValue: number // 标煤值
  carbonValue: number // 碳排放值
}

// IoT 能源仪表板 分项能耗数据 VO
export interface IotEnergyDashboardItemVO {
  energyTypeId: number // 能源类型 ID
  energyTypeName: string // 能源类型名称
  totalEnergy: number // 总能耗
  unit: string // 单位
  percentage: number // 占比（%）
  color: string // 颜色（用于可视化）
}

// IoT 能源仪表板 排名查询请求 VO
export interface IotEnergyDashboardRankingReqVO {
  type: string // 排名类型：building-建筑，meter-计量点
  energyTypeId?: number // 能源类型 ID
  startTime: Date // 开始时间
  endTime: Date // 结束时间
  topN: number // 前N名（默认10）
}

// IoT 能源仪表板 趋势查询请求 VO
export interface IotEnergyDashboardTrendReqVO {
  buildingId?: number // 建筑 ID（可选）
  energyTypeId?: number // 能源类型 ID（可选）
  period: string // 时间段：24h-最近24小时，7d-最近7天，30d-最近30天，custom-自定义
  startTime?: Date // 开始时间（自定义时使用）
  endTime?: Date // 结束时间（自定义时使用）
}

// IoT 能源仪表板 分项能耗查询请求 VO
export interface IotEnergyDashboardItemReqVO {
  buildingId?: number // 建筑 ID（可选）
  startTime: Date // 开始时间
  endTime: Date // 结束时间
}

// 获取能耗总览数据
export const getIotEnergyDashboardOverview = () => {
  return request.get({ url: '/iot/energy-dashboard/overview' })
}

// 获取能耗排名数据
export const getIotEnergyDashboardRanking = (params: IotEnergyDashboardRankingReqVO) => {
  return request.get({ url: '/iot/energy-dashboard/ranking', params })
}

// 获取能耗趋势数据
export const getIotEnergyDashboardTrend = (params: IotEnergyDashboardTrendReqVO) => {
  return request.get({ url: '/iot/energy-dashboard/trend', params })
}

// 获取分项能耗数据
export const getIotEnergyDashboardItem = (params: IotEnergyDashboardItemReqVO) => {
  return request.get({ url: '/iot/energy-dashboard/item', params })
}
