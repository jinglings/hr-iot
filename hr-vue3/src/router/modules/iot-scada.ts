import { Layout } from '@/utils/routerHelper'

/**
 * SCADA 路由配置
 * 
 * Part of SCADA-023: Add SCADA Route Configuration
 * 
 * @author HR-IoT Team
 */
const scadaRouter: AppRouteRecordRaw[] = [
    {
        path: '/iot/scada',
        component: Layout,
        name: 'IotScada',
        redirect: '/iot/scada/index',
        meta: {
            title: 'SCADA 监控',
            icon: 'ep:monitor',
            alwaysShow: true
        },
        children: [
            // SCADA 主页面（三合一：仪表板/设备监控/告警中心）
            {
                path: 'index',
                component: () => import('@/views/iot/scada/index.vue'),
                name: 'IotScadaIndex',
                meta: {
                    title: 'SCADA 监控中心',
                    icon: 'ep:monitor',
                    noCache: false
                }
            },
            // FUXA 仪表板全屏模式
            {
                path: 'dashboard/:dashboardId?',
                component: () => import('@/views/iot/scada/dashboard.vue'),
                name: 'IotScadaDashboard',
                meta: {
                    title: '仪表板详情',
                    icon: 'ep:data-board',
                    noCache: true,
                    hidden: true,
                    canTo: true,
                    activeMenu: '/iot/scada/index'
                }
            },
            // 设备详情页面
            {
                path: 'device/:deviceId',
                component: () => import('@/views/iot/scada/device.vue'),
                name: 'IotScadaDeviceDetail',
                meta: {
                    title: '设备监控详情',
                    icon: 'ep:cpu',
                    noCache: true,
                    hidden: true,
                    canTo: true,
                    activeMenu: '/iot/scada/index'
                }
            },
            // 告警历史页面
            {
                path: 'alarms',
                component: () => import('@/views/iot/scada/alarms.vue'),
                name: 'IotScadaAlarms',
                meta: {
                    title: '告警历史',
                    icon: 'ep:bell',
                    noCache: false,
                    hidden: true,
                    canTo: true,
                    activeMenu: '/iot/scada/index'
                }
            },
            // 控制日志页面
            {
                path: 'control-logs',
                component: () => import('@/views/iot/scada/control-logs.vue'),
                name: 'IotScadaControlLogs',
                meta: {
                    title: '控制日志',
                    icon: 'ep:document',
                    noCache: false,
                    hidden: true,
                    canTo: true,
                    activeMenu: '/iot/scada/index'
                }
            }
        ]
    }
]

export default scadaRouter
