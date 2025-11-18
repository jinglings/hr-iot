/**
 * 大屏模板相关类型定义
 */

import type { CanvasConfig, DashboardComponent } from './dashboard'

/**
 * 模板分类
 */
export enum TemplateCategory {
  KPI = 'kpi', // KPI仪表板
  DATA_ANALYSIS = 'data_analysis', // 数据分析
  IOT_MONITOR = 'iot_monitor', // 物联网监控
  BUSINESS = 'business', // 业务监控
  CUSTOM = 'custom' // 自定义模板
}

/**
 * 模板配置
 */
export interface DashboardTemplate {
  id: string // 模板ID
  name: string // 模板名称
  description: string // 模板描述
  category: TemplateCategory // 模板分类
  thumbnail?: string // 缩略图
  preview?: string // 预览图
  config: Omit<CanvasConfig, 'id' | 'createTime' | 'updateTime'> // 画布配置(不包含ID和时间)
  tags?: string[] // 标签
  author?: string // 作者
  isBuiltIn: boolean // 是否内置模板
  createTime?: string // 创建时间
  updateTime?: string // 更新时间
}

/**
 * 模板库配置
 */
export interface TemplateLibraryConfig {
  templates: DashboardTemplate[] // 模板列表
  categories: {
    value: TemplateCategory
    label: string
    icon: string
  }[] // 分类配置
}
