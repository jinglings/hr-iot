import request from '@/config/axios'

// 物模型模板分类 VO
export interface ThingModelTemplateCategoryVO {
    id: number
    name: string
    code: string
    icon: string
    sort?: number
    isSystem?: boolean
    templateCount?: number
}

// 物模型模板 VO
export interface ThingModelTemplateVO {
    id?: number
    name: string
    code: string
    categoryId: number
    categoryName?: string
    description?: string
    icon?: string
    tsl: string
    isSystem?: boolean
    usageCount?: number
    status?: number
    createTime?: string
}

// 模板列表查询参数
export interface ThingModelTemplateListReqVO {
    categoryId?: number
    name?: string
    status?: number
    isSystem?: boolean
}

// 模板应用参数
export interface ThingModelTemplateApplyReqVO {
    templateId: number
    productId: number
    overwrite?: boolean
}

// ========== 分类接口 ==========

// 获取分类列表
export const getCategoryList = () => {
    return request.get({ url: '/iot/thing-model-template/category/list' })
}

// ========== 模板接口 ==========

// 创建模板
export const createTemplate = (data: ThingModelTemplateVO) => {
    return request.post({ url: '/iot/thing-model-template/create', data })
}

// 更新模板
export const updateTemplate = (data: ThingModelTemplateVO, id: number) => {
    return request.put({ url: '/iot/thing-model-template/update', params: { id }, data })
}

// 删除模板
export const deleteTemplate = (id: number) => {
    return request.delete({ url: '/iot/thing-model-template/delete', params: { id } })
}

// 获取模板详情
export const getTemplate = (id: number) => {
    return request.get({ url: '/iot/thing-model-template/get', params: { id } })
}

// 获取模板列表
export const getTemplateList = (params: ThingModelTemplateListReqVO) => {
    return request.get({ url: '/iot/thing-model-template/list', params })
}

// 应用模板到产品
export const applyTemplate = (data: ThingModelTemplateApplyReqVO) => {
    return request.post({ url: '/iot/thing-model-template/apply', data })
}

// 导出产品物模型
export const exportThingModel = (productId: number) => {
    return request.download({ url: '/iot/thing-model-template/export', params: { productId } })
}

// 导入物模型到产品
export const importThingModel = (productId: number, file: File, overwrite?: boolean) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('productId', String(productId))
    if (overwrite !== undefined) {
        formData.append('overwrite', String(overwrite))
    }
    return request.upload({ url: '/iot/thing-model-template/import', data: formData })
}
