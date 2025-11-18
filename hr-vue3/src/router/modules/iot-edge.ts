import { Layout } from '@/utils/routerHelper'
import type { AppRouteRecordRaw } from '@/router/types'

const { t } = useI18n()

/**
 * IoT 边缘计算路由配置
 */
const edgeRouter: AppRouteRecordRaw = {
  path: '/iot/edge',
  component: Layout,
  name: 'IotEdge',
  meta: {
    title: '边缘计算',
    icon: 'ep:cpu',
    alwaysShow: true
  },
  children: [
    // 边缘网关管理
    {
      path: 'gateway',
      component: () => import('@/views/iot/edge/gateway/index.vue'),
      name: 'IotEdgeGateway',
      meta: {
        title: '边缘网关',
        icon: 'ep:box',
        noCache: false
      }
    },

    // 边缘网关详情（隐藏在菜单中）
    {
      path: 'gateway/detail/:id',
      component: () => import('@/views/iot/edge/gateway/detail/index.vue'),
      name: 'IotEdgeGatewayDetail',
      meta: {
        title: '网关详情',
        noCache: true,
        hidden: true,
        canTo: true,
        activeMenu: '/iot/edge/gateway'
      }
    }

    // 边缘规则管理 - 后续添加
    // {
    //   path: 'rule',
    //   component: () => import('@/views/iot/edge/rule/index.vue'),
    //   name: 'IotEdgeRule',
    //   meta: {
    //     title: '边缘规则',
    //     icon: 'ep:operation',
    //     noCache: false
    //   }
    // },

    // 边缘AI模型管理 - 后续添加
    // {
    //   path: 'aimodel',
    //   component: () => import('@/views/iot/edge/aimodel/index.vue'),
    //   name: 'IotEdgeAiModel',
    //   meta: {
    //     title: '边缘AI模型',
    //     icon: 'ep:cpu',
    //     noCache: false
    //   }
    // }
  ]
}

export default edgeRouter
