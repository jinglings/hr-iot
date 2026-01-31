import { Layout } from '@/utils/routerHelper'
import type { AppRouteRecordRaw } from '@/router/types'

const { t } = useI18n()

/**
 * IoT TDengine 备份管理路由配置
 */
const backupRouter: AppRouteRecordRaw = {
    path: '/iot/backup',
    component: Layout,
    name: 'IotBackup',
    meta: {
        title: '数据备份',
        icon: 'ep:files',
        alwaysShow: true
    },
    children: [
        // 备份列表
        {
            path: 'list',
            component: () => import('@/views/iot/backup/list/index.vue'),
            name: 'IotBackupList',
            meta: {
                title: '备份管理',
                icon: 'ep:document-copy',
                noCache: false
            }
        },

        // 恢复记录
        {
            path: 'restore',
            component: () => import('@/views/iot/backup/restore/index.vue'),
            name: 'IotBackupRestore',
            meta: {
                title: '恢复记录',
                icon: 'ep:refresh-left',
                noCache: false
            }
        },

        // 备份配置
        {
            path: 'config',
            component: () => import('@/views/iot/backup/config/index.vue'),
            name: 'IotBackupConfig',
            meta: {
                title: '定时备份配置',
                icon: 'ep:setting',
                noCache: false
            }
        }
    ]
}

export default backupRouter
