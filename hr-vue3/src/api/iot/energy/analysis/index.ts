import request from '@/config/axios'

// IoT 能源分析 同比环比数据 VO
export interface IotEnergyAnalysisComparisonVO {
  currentPeriod: string // 当前周期
  currentValue: number // 当前值
  lastPeriod: string // 上一周期
  lastValue: number // 上一值
  yearOnYearRate: number // 同比增长率（%）
  monthOnMonthRate: number // 环比增长率（%）
  unit: string // 单位
}

// IoT 能源分析 能效指标 VO
export interface IotEnergyAnalysisIndicatorVO {
  indicatorName: string // 指标名称
  indicatorValue: number // 指标值
  unit: string // 单位
  buildingId?: number // 建筑 ID
  buildingName?: string // 建筑名称
  period: string // 统计周期
  benchmarkValue: number // 基准值
  deviationRate: number // 偏离率（%）
}

// IoT 能源分析 碳排放数据 VO
export interface IotEnergyAnalysisCarbonVO {
  energyTypeId: number // 能源类型 ID
  energyTypeName: string // 能源类型名称
  totalEnergy: number // 总能耗
  carbonEmission: number // 碳排放（吨CO2）
  carbonFactor: number // 碳排放系数
  percentage: number // 占比（%）
  unit: string // 单位
}

// IoT 能源分析 同比环比查询请求 VO
export interface IotEnergyAnalysisComparisonReqVO {
  buildingId?: number // 建筑 ID（可选）
  energyTypeId?: number // 能源类型 ID（可选）
  comparisonType: string // 对比类型：yoy-同比，mom-环比
  period: string // 统计周期：day-天，month-月，year-年
  currentPeriod: string // 当前周期值（如：2024-01-01）
}

// IoT 能源分析 能效指标查询请求 VO
export interface IotEnergyAnalysisIndicatorReqVO {
  buildingId?: number // 建筑 ID（可选）
  indicatorType: string // 指标类型：area-单位面积能耗，capita-人均能耗，output-单位产值能耗
  startTime: Date // 开始时间
  endTime: Date // 结束时间
}

// IoT 能源分析 碳排放查询请求 VO
export interface IotEnergyAnalysisCarbonReqVO {
  buildingId?: number // 建筑 ID（可选）
  startTime: Date // 开始时间
  endTime: Date // 结束时间
}

// 获取同比环比分析数据
export const getIotEnergyAnalysisComparison = (params: IotEnergyAnalysisComparisonReqVO) => {
  return request.get({ url: '/iot/energy/analysis/comparison', params })
}

// 获取能效指标评估数据
export const getIotEnergyAnalysisIndicator = (params: IotEnergyAnalysisIndicatorReqVO) => {
  return request.get({ url: '/iot/energy/analysis/indicator', params })
}

// 获取碳排放计算数据
export const getIotEnergyAnalysisCarbon = (params: IotEnergyAnalysisCarbonReqVO) => {
  return request.get({ url: '/iot/energy/analysis/carbon', params })
}
