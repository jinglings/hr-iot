import request from '@/config/axios'

/** 备份记录信息 */
export interface BackupRecord {
    id: number
    backupName: string
    backupType: number // 1-全量备份，2-数据库备份，3-表备份
    backupScope?: string[]
    backupMode: number // 1-手动，2-自动定时
    backupStatus: number // 0-备份中，1-成功，2-失败
    filePath?: string
    fileSize?: number
    fileFormat?: string
    startTime: string
    endTime?: string
    duration?: number
    recordCount?: number
    errorMessage?: string
    remark?: string
    creator?: string
    createTime: string
}

/** 备份配置信息 */
export interface BackupConfig {
    id: number
    configName: string
    enabled: boolean
    backupType: number
    backupScope?: string[]
    cronExpression: string
    retentionDays: number
    maxBackupCount: number
    compressionEnabled: boolean
    notifyOnSuccess: boolean
    notifyOnFailure: boolean
    notifyEmails?: string
    createTime?: string
}

/** 恢复记录信息 */
export interface RestoreRecord {
    id: number
    backupId: number
    backupName?: string
    restoreType: number // 1-完整恢复，2-选择性恢复
    restoreTarget?: string[]
    restoreStatus: number // 0-恢复中，1-成功，2-失败
    startTime: string
    endTime?: string
    duration?: number
    recordCount?: number
    errorMessage?: string
    remark?: string
    creator?: string
    createTime: string
}

/** 备份验证结果 */
export interface BackupValidation {
    valid: boolean
    backupTime?: string
    tdengineVersion?: string
    databases?: string[]
    tables?: string[]
    recordCount?: number
    fileSize?: number
    errorMessage?: string
}

// IoT 备份管理 API
export const BackupApi = {
    // 创建备份（手动）
    createBackup: async (data: any) => {
        return await request.post({ url: `/iot/backup/create`, data })
    },

    // 查询备份分页列表
    getBackupPage: async (params: any) => {
        return await request.get({ url: `/iot/backup/page`, params })
    },

    // 查询备份详情
    getBackup: async (id: number) => {
        return await request.get({ url: `/iot/backup/get?id=` + id })
    },

    // 删除备份
    deleteBackup: async (id: number) => {
        return await request.delete({ url: `/iot/backup/delete?id=` + id })
    },

    // 下载备份
    downloadBackup: async (id: number) => {
        return await request.download({ url: `/iot/backup/download?id=` + id })
    },

    // 验证备份文件
    validateBackup: async (id: number) => {
        return await request.post({ url: `/iot/backup/validate?id=` + id })
    },

    // 获取TDengine数据库列表
    getTDengineDatabases: async () => {
        return await request.get({ url: `/iot/backup/databases` })
    },

    // 获取TDengine超级表列表
    getTDengineStables: async (database: string) => {
        return await request.get({ url: `/iot/backup/stables?database=` + database })
    }
}

// IoT 恢复管理 API
export const RestoreApi = {
    // 执行恢复
    executeRestore: async (data: any) => {
        return await request.post({ url: `/iot/restore/execute`, data })
    },

    // 查询恢复记录分页列表
    getRestorePage: async (params: any) => {
        return await request.get({ url: `/iot/restore/page`, params })
    },

    // 查询恢复记录详情
    getRestore: async (id: number) => {
        return await request.get({ url: `/iot/restore/get?id=` + id })
    }
}

// IoT 备份配置 API
export const BackupConfigApi = {
    // 创建配置
    createBackupConfig: async (data: BackupConfig) => {
        return await request.post({ url: `/iot/backup/config/create`, data })
    },

    // 更新配置
    updateBackupConfig: async (data: BackupConfig) => {
        return await request.put({ url: `/iot/backup/config/update`, data })
    },

    // 删除配置
    deleteBackupConfig: async (id: number) => {
        return await request.delete({ url: `/iot/backup/config/delete?id=` + id })
    },

    // 查询配置列表
    getBackupConfigList: async () => {
        return await request.get({ url: `/iot/backup/config/list` })
    },

    // 查询配置详情
    getBackupConfig: async (id: number) => {
        return await request.get({ url: `/iot/backup/config/get?id=` + id })
    },

    // 启用/禁用配置
    enableBackupConfig: async (id: number, enabled: boolean) => {
        return await request.put({ url: `/iot/backup/config/enable?id=${id}&enabled=${enabled}` })
    },

    // 立即执行配置
    executeBackupConfig: async (id: number) => {
        return await request.post({ url: `/iot/backup/config/execute?id=` + id })
    }
}
