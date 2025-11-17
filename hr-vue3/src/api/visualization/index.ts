import request from '@/config/axios'
import type { CanvasConfig } from '@/types/dashboard'

/**
 * 大屏可视化 API
 */

// 获取大屏列表
export const getDashboardListApi = (params: any) => {
  return request.get({ url: '/visualization/dashboard/list', params })
}

// 获取大屏详情
export const getDashboardApi = (id: string | number) => {
  return request.get({ url: `/visualization/dashboard/get?id=${id}` })
}

// 创建大屏
export const createDashboardApi = (data: CanvasConfig) => {
  return request.post({ url: '/visualization/dashboard/create', data })
}

// 更新大屏
export const updateDashboardApi = (data: CanvasConfig) => {
  return request.put({ url: '/visualization/dashboard/update', data })
}

// 删除大屏
export const deleteDashboardApi = (id: string | number) => {
  return request.delete({ url: `/visualization/dashboard/delete?id=${id}` })
}

// 发布大屏
export const publishDashboardApi = (id: string | number) => {
  return request.post({ url: `/visualization/dashboard/publish?id=${id}` })
}
