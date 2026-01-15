import request from '@/config/axios'

// 调试日志 VO
export interface DeviceDebugLogVO {
    id: number
    deviceId: number
    deviceKey: string
    productKey: string
    direction: number
    type: string
    identifier: string
    payload: string
    result: string
    status: number
    errorMessage: string
    latency: number
    createTime: string
}

// 调试日志分页查询参数
export interface DeviceDebugLogPageReqVO {
    pageNo: number
    pageSize: number
    deviceId?: number
    direction?: number
    type?: string
    status?: number
    createTime?: string[]
}

// 属性上报请求
export interface PropertyReportReqVO {
    deviceId: number
    identifier: string
    value: string
}

// 服务调用请求
export interface ServiceInvokeReqVO {
    deviceId: number
    identifier: string
    inputParams?: string
}

// 调试结果
export interface DebugResultVO {
    success: boolean
    messageId?: string
    data?: string
    errorMessage?: string
    latency?: number
}

// 模拟属性上报
export const simulatePropertyReport = (data: PropertyReportReqVO) => {
    return request.post<DebugResultVO>({ url: '/iot/device-debug/property-report', data })
}

// 下发属性设置
export const sendPropertySet = (deviceId: number, identifier: string, value: string) => {
    return request.post<DebugResultVO>({
        url: '/iot/device-debug/property-set',
        params: { deviceId, identifier, value }
    })
}

// 调用设备服务
export const invokeService = (data: ServiceInvokeReqVO) => {
    return request.post<DebugResultVO>({ url: '/iot/device-debug/service-invoke', data })
}

// 获取调试日志分页
export const getDebugLogPage = (params: DeviceDebugLogPageReqVO) => {
    return request.get({ url: '/iot/device-debug/log/page', params })
}

// 清理过期日志
export const cleanExpiredLogs = (days: number) => {
    return request.delete({ url: '/iot/device-debug/log/clean', params: { days } })
}
