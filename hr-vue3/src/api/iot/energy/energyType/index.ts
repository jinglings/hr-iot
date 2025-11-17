import request from '@/config/axios'

// IoT 能源类型 VO
export interface IotEnergyTypeVO {
  id: number // 能源类型 ID，主键，自增
  name: string // 能源类型名称
  code: string // 能源类型编码
  parentId: number // 父级能源类型 ID
  parentName?: string // 父级能源类型名称（用于显示）
  unit: string // 计量单位：kWh-千瓦时，m³-立方米，t-吨，L-升，Nm³-标准立方米
  coalFactor: number // 折标煤系数（kgce/单位）
  carbonFactor: number // 碳排放系数（kgCO2/单位）
  icon: string // 图标
  color: string // 颜色（用于可视化展示）
  description: string // 能源类型描述
  sort: number // 排序
  status: number // 能源类型状态：0-禁用 1-启用
  createTime: Date // 创建时间
  updateTime: Date // 更新时间
  children?: IotEnergyTypeVO[] // 子节点（树形结构）
}

// IoT 能源类型 分页查询请求 VO
export interface IotEnergyTypePageReqVO extends PageParam {
  name?: string // 能源类型名称
  code?: string // 能源类型编码
  parentId?: number // 父级能源类型 ID
  status?: number // 能源类型状态
  createTime?: Date[] // 创建时间
}

// IoT 能源类型 简化 VO（用于下拉选择）
export interface IotEnergyTypeSimpleVO {
  id: number // 能源类型 ID
  name: string // 能源类型名称
  code: string // 能源类型编码
  unit: string // 计量单位
  parentId: number // 父级能源类型 ID
}

// 查询 IoT 能源类型分页
export const getIotEnergyTypePage = (params: IotEnergyTypePageReqVO) => {
  return request.get({ url: '/iot/energy/type/page', params })
}

// 查询 IoT 能源类型详情
export const getIotEnergyType = (id: number) => {
  return request.get({ url: `/iot/energy/type/get?id=${id}` })
}

// 新增 IoT 能源类型
export const createIotEnergyType = (data: IotEnergyTypeVO) => {
  return request.post({ url: '/iot/energy/type/create', data })
}

// 修改 IoT 能源类型
export const updateIotEnergyType = (data: IotEnergyTypeVO) => {
  return request.put({ url: '/iot/energy/type/update', data })
}

// 删除 IoT 能源类型
export const deleteIotEnergyType = (id: number) => {
  return request.delete({ url: `/iot/energy/type/delete?id=${id}` })
}

// 查询能源类型树形结构
export const getIotEnergyTypeTree = () => {
  return request.get({ url: '/iot/energy/type/tree' })
}

// 根据父级ID查询能源类型列表
export const getIotEnergyTypeListByParentId = (parentId: number) => {
  return request.get({ url: `/iot/energy/type/list-by-parent-id?parentId=${parentId}` })
}

// 获取启用的能源类型简化列表
export const getIotEnergyTypeSimpleList = () => {
  return request.get({ url: '/iot/energy/type/simple-list' })
}
