import request from '@/config/axios'

// IoT 能源房间 VO
export interface IotEnergyRoomVO {
  id: number // 房间 ID，主键，自增
  buildingId: number // 建筑 ID
  buildingName?: string // 建筑名称（用于显示）
  areaId: number // 区域 ID
  areaName?: string // 区域名称（用于显示）
  floorId: number // 楼层 ID
  floorName?: string // 楼层名称（用于显示）
  roomName: string // 房间名称
  roomCode: string // 房间编码
  roomType: string // 房间类型：office-办公室，meeting-会议室，warehouse-仓库，lab-实验室，other-其他
  area: number // 房间面积（平方米）
  description: string // 房间描述
  sort: number // 排序
  status: number // 房间状态：0-禁用 1-启用
  createTime: Date // 创建时间
  updateTime: Date // 更新时间
}

// IoT 能源房间 分页查询请求 VO
export interface IotEnergyRoomPageReqVO extends PageParam {
  buildingId?: number // 建筑 ID
  areaId?: number // 区域 ID
  floorId?: number // 楼层 ID
  roomName?: string // 房间名称
  roomCode?: string // 房间编码
  roomType?: string // 房间类型
  status?: number // 房间状态
  createTime?: string[] // 创建时间
}

// IoT 能源房间 简化 VO（用于下拉选择）
export interface IotEnergyRoomSimpleVO {
  id: number // 房间 ID
  roomName: string // 房间名称
  roomCode: string // 房间编码
  floorId: number // 楼层 ID
}

// 查询 IoT 能源房间分页
export const getIotEnergyRoomPage = (params: IotEnergyRoomPageReqVO) => {
  return request.get({ url: '/iot/energy/room/page', params })
}

// 查询 IoT 能源房间详情
export const getIotEnergyRoom = (id: number) => {
  return request.get({ url: `/iot/energy/room/get?id=${id}` })
}

// 新增 IoT 能源房间
export const createIotEnergyRoom = (data: IotEnergyRoomVO) => {
  return request.post({ url: '/iot/energy/room/create', data })
}

// 修改 IoT 能源房间
export const updateIotEnergyRoom = (data: IotEnergyRoomVO) => {
  return request.put({ url: '/iot/energy/room/update', data })
}

// 删除 IoT 能源房间
export const deleteIotEnergyRoom = (id: number) => {
  return request.delete({ url: `/iot/energy/room/delete?id=${id}` })
}

// 根据楼层ID获取房间列表
export const getIotEnergyRoomListByFloorId = (floorId: number) => {
  return request.get({ url: `/iot/energy/room/list-by-floor-id?floorId=${floorId}` })
}

// 获取启用的房间简化列表
export const getIotEnergyRoomSimpleList = () => {
  return request.get({ url: '/iot/energy/room/simple-list' })
}
