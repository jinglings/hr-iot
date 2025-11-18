import request from '@/config/axios'

/**
 * 边缘AI模型响应VO
 */
export interface EdgeAiModelVO {
  id: number // 主键
  name: string // 模型名称
  modelKey: string // 模型标识
  modelType: string // 模型类型
  modelFormat: string // 模型格式
  modelVersion: string // 模型版本
  modelUrl: string // 模型文件URL
  modelSize: number // 模型大小(KB)
  inputConfig: string // 输入配置(JSON)
  outputConfig: string // 输出配置(JSON)
  status: number // 状态: 0=禁用, 1=启用
  description: string // 描述
  createTime: Date // 创建时间
  updateTime: Date // 更新时间
}

/**
 * 边缘AI模型分页查询请求VO
 */
export interface EdgeAiModelPageReqVO {
  pageNo: number // 页码
  pageSize: number // 每页数量
  name?: string // 模型名称（模糊搜索）
  modelType?: string // 模型类型
  status?: number // 状态
}

/**
 * 边缘AI模型创建请求VO
 */
export interface EdgeAiModelCreateReqVO {
  name: string // 模型名称
  modelKey: string // 模型标识
  modelType: string // 模型类型
  modelFormat: string // 模型格式
  modelVersion: string // 模型版本
  modelUrl: string // 模型文件URL
  modelSize?: number // 模型大小(KB)
  inputConfig?: string // 输入配置(JSON)
  outputConfig?: string // 输出配置(JSON)
  description?: string // 描述
}

/**
 * 边缘AI模型更新请求VO
 */
export interface EdgeAiModelUpdateReqVO {
  id: number // 主键
  name?: string // 模型名称
  modelVersion?: string // 模型版本
  modelUrl?: string // 模型文件URL
  modelSize?: number // 模型大小(KB)
  inputConfig?: string // 输入配置(JSON)
  outputConfig?: string // 输出配置(JSON)
  status?: number // 状态
  description?: string // 描述
}

// 边缘AI模型类型枚举
export enum EdgeAiModelType {
  IMAGE_CLASSIFICATION = 'IMAGE_CLASSIFICATION', // 图像分类
  OBJECT_DETECTION = 'OBJECT_DETECTION', // 目标检测
  FACE_RECOGNITION = 'FACE_RECOGNITION', // 人脸识别
  ANOMALY_DETECTION = 'ANOMALY_DETECTION', // 异常检测
  PREDICTIVE_MAINTENANCE = 'PREDICTIVE_MAINTENANCE', // 预测性维护
  CUSTOM = 'CUSTOM' // 自定义
}

// 边缘AI模型类型映射
export const EdgeAiModelTypeMap = {
  [EdgeAiModelType.IMAGE_CLASSIFICATION]: { label: '图像分类', icon: 'ep:picture' },
  [EdgeAiModelType.OBJECT_DETECTION]: { label: '目标检测', icon: 'ep:aim' },
  [EdgeAiModelType.FACE_RECOGNITION]: { label: '人脸识别', icon: 'ep:user' },
  [EdgeAiModelType.ANOMALY_DETECTION]: { label: '异常检测', icon: 'ep:warning' },
  [EdgeAiModelType.PREDICTIVE_MAINTENANCE]: { label: '预测性维护', icon: 'ep:tools' },
  [EdgeAiModelType.CUSTOM]: { label: '自定义', icon: 'ep:setting' }
}

// 边缘AI模型格式枚举
export enum EdgeAiModelFormat {
  ONNX = 'ONNX',
  TENSORFLOW = 'TENSORFLOW',
  PYTORCH = 'PYTORCH',
  TENSORRT = 'TENSORRT',
  OPENVINO = 'OPENVINO'
}

// 边缘AI模型格式映射
export const EdgeAiModelFormatMap = {
  [EdgeAiModelFormat.ONNX]: { label: 'ONNX' },
  [EdgeAiModelFormat.TENSORFLOW]: { label: 'TensorFlow' },
  [EdgeAiModelFormat.PYTORCH]: { label: 'PyTorch' },
  [EdgeAiModelFormat.TENSORRT]: { label: 'TensorRT' },
  [EdgeAiModelFormat.OPENVINO]: { label: 'OpenVINO' }
}

// 边缘AI模型 API
export const EdgeAiModelApi = {
  // 分页查询
  getPage: async (params: EdgeAiModelPageReqVO) => {
    return await request.get({ url: '/iot/edge-ai-model/page', params })
  },

  // 获取详情
  get: async (id: number) => {
    return await request.get({ url: '/iot/edge-ai-model/get', params: { id } })
  },

  // 创建模型
  create: async (data: EdgeAiModelCreateReqVO) => {
    return await request.post({ url: '/iot/edge-ai-model/create', data })
  },

  // 更新模型
  update: async (data: EdgeAiModelUpdateReqVO) => {
    return await request.put({ url: '/iot/edge-ai-model/update', data })
  },

  // 删除模型
  delete: async (id: number) => {
    return await request.delete({ url: '/iot/edge-ai-model/delete', params: { id } })
  },

  // 启用模型
  enable: async (id: number) => {
    return await request.put({ url: '/iot/edge-ai-model/enable', params: { id } })
  },

  // 禁用模型
  disable: async (id: number) => {
    return await request.put({ url: '/iot/edge-ai-model/disable', params: { id } })
  }
}
