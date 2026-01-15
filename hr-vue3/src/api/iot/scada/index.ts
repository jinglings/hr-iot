import request from '@/config/axios'

/**
 * SCADA API 服务层
 * 
 * Part of SCADA-018: Create SCADA API Service Layer
 */

// ==========================================
// 类型定义
// ==========================================

/** SCADA 设备 VO */
export interface ScadaDeviceVO {
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
export interface ScadaHistoryPointVO {
    timestamp: Date
    value: any
    quality?: number
}

/** 控制命令请求 */
export interface ScadaControlCommandReqVO {
    deviceId?: number
    commandName: string
    params?: Record<string, any>
    oldValue?: any
    newValue?: any
    source?: string
}

/** 控制命令响应 */
export interface ScadaControlCommandRespVO {
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

/** SCADA 告警 VO */
export interface ScadaAlarmVO {
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

/** SCADA 仪表板 VO */
export interface ScadaDashboardVO {
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

/** SCADA Tag 映射 VO */
export interface ScadaTagMappingVO {
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

/** SCADA 控制日志 VO */
export interface ScadaControlLogVO {
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
export interface ScadaHistoryQueryReqVO {
    deviceId: number
    properties: string[]
    startTime: Date | string
    endTime: Date | string
    interval?: string
    aggregation?: string
}

/** 控制日志分页查询请求 */
export interface ScadaControlLogPageReqVO {
    pageNo: number
    pageSize: number
    deviceId?: number
    deviceName?: string
    commandName?: string
    executionStatus?: number
    userId?: number
    userName?: string
    source?: string
    startTime?: Date | string
    endTime?: Date | string
}

// ==========================================
// 告警优先级和状态枚举
// ==========================================

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

/** 设备状态 */
export enum DeviceState {
    INACTIVE = 0,
    ONLINE = 1,
    OFFLINE = 2,
    DISABLED = 3
}

// ==========================================
// SCADA API
// ==========================================

export const ScadaApi = {
    // ========== 设备相关 ==========

    /** 获取 SCADA 设备列表 */
    getDevices: async (): Promise<ScadaDeviceVO[]> => {
        return await request.get({ url: `/iot/scada/devices` })
    },

    /** 获取设备详情 */
    getDevice: async (deviceId: number): Promise<ScadaDeviceVO> => {
        return await request.get({ url: `/iot/scada/devices/${deviceId}` })
    },

    /** 获取设备实时数据 */
    getDeviceRealtime: async (deviceId: number): Promise<Record<string, any>> => {
        return await request.get({ url: `/iot/scada/devices/${deviceId}/realtime` })
    },

    /** 批量获取设备实时数据 */
    getDevicesRealtime: async (deviceIds: number[]): Promise<Record<number, Record<string, any>>> => {
        return await request.post({ url: `/iot/scada/devices/realtime/batch`, data: deviceIds })
    },

    /** 获取设备历史数据 */
    getDeviceHistory: async (deviceId: number, params: Omit<ScadaHistoryQueryReqVO, 'deviceId'>): Promise<ScadaHistoryPointVO[]> => {
        return await request.get({ url: `/iot/scada/devices/${deviceId}/history`, params })
    },

    /** 获取设备 Tag 映射 */
    getDeviceTagMappings: async (deviceId: number): Promise<ScadaTagMappingVO[]> => {
        return await request.get({ url: `/iot/scada/devices/${deviceId}/tags` })
    },

    // ========== 控制相关 ==========

    /** 发送控制命令 */
    sendControlCommand: async (deviceId: number, command: ScadaControlCommandReqVO): Promise<ScadaControlCommandRespVO> => {
        return await request.post({ url: `/iot/scada/devices/${deviceId}/control`, data: command })
    },

    /** 设置设备属性 */
    setDeviceProperty: async (deviceId: number, property: string, value: any): Promise<boolean> => {
        return await request.post({
            url: `/iot/scada/devices/${deviceId}/property`,
            params: { property, value }
        })
    },

    /** 调用设备服务 */
    invokeDeviceService: async (deviceId: number, serviceName: string, params?: Record<string, any>): Promise<boolean> => {
        return await request.post({
            url: `/iot/scada/devices/${deviceId}/service/${serviceName}`,
            data: params || {}
        })
    },

    // ========== Tag 相关 ==========

    /** 获取 Tag 值 */
    getTagValue: async (tagName: string): Promise<any> => {
        return await request.get({ url: `/iot/scada/tags/${tagName}` })
    },

    /** 批量获取 Tag 值 */
    getTagValues: async (tagNames: string[]): Promise<Record<string, any>> => {
        return await request.post({ url: `/iot/scada/tags/batch`, data: tagNames })
    },

    // ========== 告警相关 ==========

    /** 获取活动告警列表 */
    getActiveAlarms: async (): Promise<ScadaAlarmVO[]> => {
        return await request.get({ url: `/iot/scada/alarms` })
    },

    /** 获取设备活动告警 */
    getDeviceActiveAlarms: async (deviceId: number): Promise<ScadaAlarmVO[]> => {
        return await request.get({ url: `/iot/scada/devices/${deviceId}/alarms` })
    },

    /** 确认告警 */
    acknowledgeAlarm: async (alarmId: number): Promise<boolean> => {
        return await request.post({ url: `/iot/scada/alarms/${alarmId}/acknowledge` })
    },

    /** 关闭告警 */
    closeAlarm: async (alarmId: number, notes?: string): Promise<boolean> => {
        return await request.post({ url: `/iot/scada/alarms/${alarmId}/close`, params: { notes } })
    },

    /** 获取告警统计 */
    getAlarmStatistics: async (): Promise<Record<number, number>> => {
        return await request.get({ url: `/iot/scada/alarms/statistics` })
    },

    // ========== 控制日志相关 ==========

    /** 获取控制日志分页 */
    getControlLogPage: async (params: ScadaControlLogPageReqVO): Promise<{ list: ScadaControlLogVO[]; total: number }> => {
        return await request.get({ url: `/iot/scada/control-logs`, params })
    },

    /** 获取控制日志统计 */
    getControlLogStatistics: async (startTime?: string, endTime?: string): Promise<Record<number, number>> => {
        return await request.get({ url: `/iot/scada/control-logs/statistics`, params: { startTime, endTime } })
    },

    // ========== 仪表板相关 ==========

    /** 获取仪表板列表 */
    getDashboards: async (): Promise<ScadaDashboardVO[]> => {
        return await request.get({ url: `/iot/scada/dashboards` })
    },

    /** 获取默认仪表板 */
    getDefaultDashboard: async (): Promise<ScadaDashboardVO> => {
        return await request.get({ url: `/iot/scada/dashboards/default` })
    },

    /** 获取仪表板访问 URL */
    getFuxaUrl: async (dashboardId: string, token?: string): Promise<string> => {
        return await request.get({ url: `/iot/scada/dashboards/${dashboardId}/url`, params: { token } })
    }
}
