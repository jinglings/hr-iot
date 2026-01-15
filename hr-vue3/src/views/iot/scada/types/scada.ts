/**
 * SCADA 类型定义
 * 
 * Part of SCADA-017: Create SCADA Module Structure
 */

/** SCADA 设备信息 */
export interface ScadaDevice {
    deviceId: number
    deviceName: string
    nickname?: string
    productId: number
    productName?: string
    productKey: string
    deviceType: number
    state: number
    stateDesc?: string
    picUrl?: string
    latitude?: number
    longitude?: number
    address?: string
    lastOnlineTime?: Date
    lastOfflineTime?: Date
    controllable: boolean
    properties?: Record<string, any>
    activeAlarmCount?: number
    hasActiveAlarm?: boolean
}

/** SCADA 历史数据点 */
export interface ScadaHistoryPoint {
    timestamp: Date
    value: any
    quality?: number
}

/** SCADA 控制命令请求 */
export interface ScadaControlCommandReq {
    deviceId: number
    commandName: string
    params?: Record<string, any>
    oldValue?: any
    newValue?: any
    source?: string
}

/** SCADA 控制命令响应 */
export interface ScadaControlCommandResp {
    success: boolean
    deviceId: number
    deviceName?: string
    commandName: string
    oldValue?: any
    newValue?: any
    errorMessage?: string
    executionTime?: number
    executeTime?: Date
    logId?: number
}

/** SCADA 告警信息 */
export interface ScadaAlarm {
    id: number
    alarmId: number
    alarmName: string
    tagId: string
    tagValue?: string
    deviceId?: number
    deviceName?: string
    priority: number
    priorityDesc?: string
    message?: string
    status: number
    statusDesc?: string
    triggeredAt: Date
    acknowledgedAt?: Date
    acknowledgedBy?: string
    recoveredAt?: Date
    closedAt?: Date
    closedBy?: string
    notes?: string
    durationSeconds?: number
}

/** 告警优先级 */
export enum AlarmPriority {
    LOW = 1,
    MEDIUM = 2,
    HIGH = 3,
    URGENT = 4
}

/** 告警状态 */
export enum AlarmStatus {
    ACTIVE = 1,
    ACKNOWLEDGED = 2,
    RECOVERED = 3,
    CLOSED = 4
}

/** SCADA 仪表板 */
export interface ScadaDashboard {
    id: number
    dashboardId: string
    name: string
    description?: string
    dashboardType: string
    dashboardTypeDesc?: string
    thumbnailUrl?: string
    isDefault: boolean
    sortOrder: number
    status: number
    statusDesc?: string
    fuxaUrl?: string
    createTime?: Date
    updateTime?: Date
    creator?: string
}

/** SCADA Tag 映射 */
export interface ScadaTagMapping {
    id: number
    tagName: string
    tagId?: string
    deviceId: number
    deviceName?: string
    propertyIdentifier: string
    dataType: string
    unit?: string
    scaleFactor?: number
    offset?: number
    minValue?: number
    maxValue?: number
    description?: string
    currentValue?: any
}

/** SCADA 控制日志 */
export interface ScadaControlLog {
    id: number
    deviceId: number
    deviceName?: string
    commandName: string
    commandParams?: string
    oldValue?: string
    newValue?: string
    executionStatus: number
    executionStatusDesc?: string
    errorMessage?: string
    userId?: number
    userName?: string
    clientIp?: string
    userAgent?: string
    executionTime?: number
    source?: string
    createTime: Date
}

/** 历史数据查询请求 */
export interface ScadaHistoryQueryReq {
    deviceId: number
    properties: string[]
    startTime: Date
    endTime: Date
    interval?: string
    aggregation?: string
}

/** 控制日志分页查询请求 */
export interface ScadaControlLogPageReq {
    pageNo: number
    pageSize: number
    deviceId?: number
    deviceName?: string
    commandName?: string
    executionStatus?: number
    userId?: number
    userName?: string
    source?: string
    startTime?: Date
    endTime?: Date
}

/** SCADA 统计数据 */
export interface ScadaStats {
    onlineCount: number
    offlineCount: number
    alarmCount: number
    controlCount: number
}

/** 设备状态枚举 */
export enum DeviceState {
    INACTIVE = 0,
    ONLINE = 1,
    OFFLINE = 2,
    DISABLED = 3
}
