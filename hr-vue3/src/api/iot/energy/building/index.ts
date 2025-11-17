import request from '@/config/axios'

// IoT 能源建筑 VO
export interface IotEnergyBuildingVO {
  id: number // 建筑 ID，主键，自增
  name: string // 建筑名称
  code: string // 建筑编码
  type: string // 建筑类型：office-办公楼，factory-厂房，warehouse-仓库，dormitory-宿舍，other-其他
  area: number // 建筑面积（平方米）
  address: string // 建筑地址
  latitude: number // 纬度
  longitude: number // 经度
  floors: number // 楼层数
  image: string // 建筑图片
  description: string // 建筑描述
  sort: number // 排序
  status: number // 建筑状态：0-禁用 1-启用
  createTime: Date // 创建时间
  updateTime: Date // 更新时间
}

// IoT 能源建筑 分页查询请求 VO
export interface IotEnergyBuildingPageReqVO extends PageParam {
  name?: string // 建筑名称
  code?: string // 建筑编码
  type?: string // 建筑类型
  status?: number // 建筑状态
  createTime?: string[] // 创建时间
}

// IoT 能源建筑 导出请求 VO
export interface IotEnergyBuildingExportReqVO {
  name?: string // 建筑名称
  code?: string // 建筑编码
  type?: string // 建筑类型
  status?: number // 建筑状态
  createTime?: string[] // 创建时间
}

// IoT 能源建筑 简化 VO（用于下拉选择）
export interface IotEnergyBuildingSimpleVO {
  id: number // 建筑 ID
  name: string // 建筑名称
  code: string // 建筑编码
}

// IoT 能源空间树 VO
export interface IotEnergySpaceTreeVO {
  id: number // ID
  name: string // 名称
  type: string // 类型：building-建筑，area-区域，floor-楼层，room-房间
  parentId?: number // 父级 ID
  children?: IotEnergySpaceTreeVO[] // 子节点
}

// 查询 IoT 能源建筑分页
export const getIotEnergyBuildingPage = (params: IotEnergyBuildingPageReqVO) => {
  return request.get({ url: '/iot/energy/building/page', params })
}

// 查询 IoT 能源建筑详情
export const getIotEnergyBuilding = (id: number) => {
  return request.get({ url: `/iot/energy/building/get?id=${id}` })
}

// 新增 IoT 能源建筑
export const createIotEnergyBuilding = (data: IotEnergyBuildingVO) => {
  return request.post({ url: '/iot/energy/building/create', data })
}

// 修改 IoT 能源建筑
export const updateIotEnergyBuilding = (data: IotEnergyBuildingVO) => {
  return request.put({ url: '/iot/energy/building/update', data })
}

// 删除 IoT 能源建筑
export const deleteIotEnergyBuilding = (id: number) => {
  return request.delete({ url: `/iot/energy/building/delete?id=${id}` })
}

// 导出 IoT 能源建筑 Excel
export const exportIotEnergyBuilding = (params: IotEnergyBuildingExportReqVO) => {
  return request.download({ url: '/iot/energy/building/export-excel', params })
}

// 获取启用的建筑简化列表
export const getIotEnergyBuildingSimpleList = () => {
  return request.get({ url: '/iot/energy/building/simple-list' })
}

// 获取空间层级树形结构
export const getIotEnergySpaceTree = () => {
  return request.get({ url: '/iot/energy/building/space-tree' })
}
