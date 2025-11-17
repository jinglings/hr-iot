import { Layout } from '@/utils/routerHelper'
import type { AppRouteRecordRaw } from '@/router/types'

const { t } = useI18n()

/**
 * IoT 能源管理路由配置
 */
const energyRouter: AppRouteRecordRaw = {
  path: '/iot/energy',
  component: Layout,
  name: 'IotEnergy',
  meta: {
    title: '能源管理',
    icon: 'ep:lightning',
    alwaysShow: true
  },
  children: [
    // 能源仪表板
    {
      path: 'dashboard',
      component: () => import('@/views/iot/energy/dashboard/index.vue'),
      name: 'IotEnergyDashboard',
      meta: {
        title: '能源仪表板',
        icon: 'ep:data-analysis',
        noCache: false
      }
    },

    // 空间层级管理
    {
      path: 'space',
      component: () => import('@/views/iot/energy/space/index.vue'),
      name: 'IotEnergySpace',
      meta: {
        title: '空间管理',
        icon: 'ep:office-building',
        alwaysShow: true
      }
    },

    // 建筑管理
    {
      path: 'building',
      component: () => import('@/views/iot/energy/building/index.vue'),
      name: 'IotEnergyBuilding',
      meta: {
        title: '建筑管理',
        icon: 'ep:office-building',
        activeMenu: '/iot/energy/space'
      }
    },

    // 区域管理
    {
      path: 'area',
      component: () => import('@/views/iot/energy/area/index.vue'),
      name: 'IotEnergyArea',
      meta: {
        title: '区域管理',
        icon: 'ep:location',
        activeMenu: '/iot/energy/space'
      }
    },

    // 楼层管理
    {
      path: 'floor',
      component: () => import('@/views/iot/energy/floor/index.vue'),
      name: 'IotEnergyFloor',
      meta: {
        title: '楼层管理',
        icon: 'ep:files',
        activeMenu: '/iot/energy/space'
      }
    },

    // 房间管理
    {
      path: 'room',
      component: () => import('@/views/iot/energy/room/index.vue'),
      name: 'IotEnergyRoom',
      meta: {
        title: '房间管理',
        icon: 'ep:document',
        activeMenu: '/iot/energy/space'
      }
    },

    // 能源类型管理
    {
      path: 'energy-type',
      component: () => import('@/views/iot/energy/energyType/index.vue'),
      name: 'IotEnergyType',
      meta: {
        title: '能源类型',
        icon: 'ep:files'
      }
    },

    // 计量点管理
    {
      path: 'meter',
      component: () => import('@/views/iot/energy/meter/index.vue'),
      name: 'IotEnergyMeter',
      meta: {
        title: '计量点管理',
        icon: 'ep:monitor'
      }
    },

    // 能源数据查询
    {
      path: 'data',
      component: () => import('@/views/iot/energy/data/index.vue'),
      name: 'IotEnergyData',
      meta: {
        title: '能源数据',
        icon: 'ep:document-copy'
      }
    },

    // 能源统计分析
    {
      path: 'statistics',
      component: () => import('@/views/iot/energy/statistics/index.vue'),
      name: 'IotEnergyStatistics',
      meta: {
        title: '统计分析',
        icon: 'ep:data-line'
      }
    },

    // 能效分析
    {
      path: 'analysis',
      component: () => import('@/views/iot/energy/analysis/index.vue'),
      name: 'IotEnergyAnalysis',
      meta: {
        title: '能效分析',
        icon: 'ep:trend-charts'
      }
    },

    // 报表管理
    {
      path: 'report',
      component: () => import('@/views/iot/energy/report/index.vue'),
      name: 'IotEnergyReport',
      meta: {
        title: '报表管理',
        icon: 'ep:document',
        alwaysShow: true
      }
    },

    // 报表模板
    {
      path: 'report/template',
      component: () => import('@/views/iot/energy/report/template/index.vue'),
      name: 'IotEnergyReportTemplate',
      meta: {
        title: '报表模板',
        icon: 'ep:document-copy',
        activeMenu: '/iot/energy/report'
      }
    },

    // 报表记录
    {
      path: 'report/record',
      component: () => import('@/views/iot/energy/report/record/index.vue'),
      name: 'IotEnergyReportRecord',
      meta: {
        title: '报表记录',
        icon: 'ep:tickets',
        activeMenu: '/iot/energy/report'
      }
    }
  ]
}

export default energyRouter
