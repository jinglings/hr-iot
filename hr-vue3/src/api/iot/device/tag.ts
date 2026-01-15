import request from '@/config/axios'

// 设备标签 VO
export interface DeviceTagVO {
    id?: number
    tagKey: string
    tagValue?: string
    description?: string
    color?: string
    isPreset?: boolean
    usageCount?: number
    createTime?: string
}

// 设备标签分页查询参数
export interface DeviceTagPageReqVO {
    pageNo: number
    pageSize: number
    tagKey?: string
    tagValue?: string
    isPreset?: boolean
}

// 设备标签绑定参数
export interface DeviceTagBindReqVO {
    deviceId: number
    tagIds: number[]
}

// 设备标签批量绑定参数
export interface DeviceTagBatchBindReqVO {
    deviceIds: number[]
    tagIds: number[]
}

// 创建设备标签
export const createTag = (data: DeviceTagVO) => {
    return request.post({ url: '/iot/device-tag/create', data })
}

// 更新设备标签
export const updateTag = (data: DeviceTagVO) => {
    return request.put({ url: '/iot/device-tag/update', data })
}

// 删除设备标签
export const deleteTag = (id: number) => {
    return request.delete({ url: '/iot/device-tag/delete', params: { id } })
}

// 获取设备标签
export const getTag = (id: number) => {
    return request.get({ url: '/iot/device-tag/get', params: { id } })
}

// 获取设备标签分页
export const getTagPage = (params: DeviceTagPageReqVO) => {
    return request.get({ url: '/iot/device-tag/page', params })
}

// 获取设备标签列表
export const getTagList = () => {
    return request.get({ url: '/iot/device-tag/list' })
}

// 获取设备标签简单列表
export const getTagSimpleList = () => {
    return request.get({ url: '/iot/device-tag/simple-list' })
}

// 为设备绑定标签
export const bindTagsToDevice = (data: DeviceTagBindReqVO) => {
    return request.post({ url: '/iot/device-tag/bind', data })
}

// 为设备解绑标签
export const unbindTagsFromDevice = (data: DeviceTagBindReqVO) => {
    return request.post({ url: '/iot/device-tag/unbind', data })
}

// 批量为设备绑定标签
export const batchBindTags = (data: DeviceTagBatchBindReqVO) => {
    return request.post({ url: '/iot/device-tag/batch-bind', data })
}

// 批量解绑设备标签
export const batchUnbindTags = (data: DeviceTagBatchBindReqVO) => {
    return request.post({ url: '/iot/device-tag/batch-unbind', data })
}

// 获取设备的所有标签
export const getTagsByDevice = (deviceId: number) => {
    return request.get({ url: '/iot/device-tag/by-device', params: { deviceId } })
}
