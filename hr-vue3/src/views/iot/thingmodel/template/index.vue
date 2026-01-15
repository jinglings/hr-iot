<template>
  <div class="template-library">
    <el-row :gutter="20">
      <!-- 左侧分类 -->
      <el-col :span="5">
        <el-card shadow="never" class="category-card">
          <template #header>
            <div class="card-header">
              <span>模板分类</span>
            </div>
          </template>
          <div class="category-list">
            <div
              v-for="category in categoryList"
              :key="category.id"
              class="category-item"
              :class="{ active: selectedCategoryId === category.id }"
              @click="selectCategory(category.id)"
            >
              <Icon :icon="category.icon || 'ep:folder'" class="mr-2" />
              <span>{{ category.name }}</span>
              <el-badge :value="getCategoryTemplateCount(category.id)" class="ml-auto" />
            </div>
            <div
              class="category-item"
              :class="{ active: selectedCategoryId === null }"
              @click="selectCategory(null)"
            >
              <Icon icon="ep:grid" class="mr-2" />
              <span>全部模板</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧模板列表 -->
      <el-col :span="19">
        <el-card shadow="never">
          <template #header>
            <div class="flex items-center justify-between">
              <span>物模型模板库</span>
              <div class="flex items-center gap-3">
                <el-input
                  v-model="searchName"
                  placeholder="搜索模板"
                  clearable
                  class="!w-200px"
                  @keyup.enter="loadTemplates"
                >
                  <template #prefix>
                    <Icon icon="ep:search" />
                  </template>
                </el-input>
                <el-button
                  v-hasPermi="['iot:thing-model-template:create']"
                  type="primary"
                  @click="openForm('create')"
                >
                  <Icon icon="ep:plus" class="mr-1" /> 创建模板
                </el-button>
              </div>
            </div>
          </template>

          <!-- 模板卡片网格 -->
          <div v-loading="loading" class="template-grid">
            <div
              v-for="template in templateList"
              :key="template.id"
              class="template-card"
              @click="openDetail(template)"
            >
              <div class="template-icon">
                <Icon :icon="template.icon || 'ep:document'" :size="32" />
              </div>
              <div class="template-info">
                <div class="template-name">{{ template.name }}</div>
                <div class="template-desc">{{ template.description || '暂无描述' }}</div>
                <div class="template-meta">
                  <el-tag v-if="template.isSystem" type="success" size="small">系统</el-tag>
                  <el-tag v-else type="info" size="small">自定义</el-tag>
                  <span class="usage-count">
                    <Icon icon="ep:download" class="mr-1" />{{ template.usageCount || 0 }}次
                  </span>
                </div>
              </div>
            </div>
            <el-empty v-if="templateList.length === 0 && !loading" description="暂无模板" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 模板详情抽屉 -->
    <el-drawer v-model="detailVisible" title="模板详情" size="600px">
      <div v-if="currentTemplate" class="template-detail">
        <div class="detail-header">
          <div class="detail-icon">
            <Icon :icon="currentTemplate.icon || 'ep:document'" :size="48" />
          </div>
          <div class="detail-info">
            <h3>{{ currentTemplate.name }}</h3>
            <p>{{ currentTemplate.description || '暂无描述' }}</p>
            <div class="detail-tags">
              <el-tag v-if="currentTemplate.isSystem" type="success">系统模板</el-tag>
              <el-tag type="info">{{ getCategoryName(currentTemplate.categoryId) }}</el-tag>
            </div>
          </div>
        </div>

        <el-divider />

        <h4>物模型定义</h4>
        <TslPreview :tsl="currentTemplate.tsl" />

        <el-divider />

        <div class="detail-actions">
          <el-button type="primary" size="large" @click="openApplyDialog">
            <Icon icon="ep:download" class="mr-1" /> 应用到产品
          </el-button>
          <el-button
            v-if="!currentTemplate.isSystem"
            v-hasPermi="['iot:thing-model-template:update']"
            @click="openForm('update', currentTemplate.id)"
          >
            编辑
          </el-button>
          <el-button
            v-if="!currentTemplate.isSystem"
            v-hasPermi="['iot:thing-model-template:delete']"
            type="danger"
            plain
            @click="handleDelete(currentTemplate.id)"
          >
            删除
          </el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 应用模板对话框 -->
    <el-dialog v-model="applyVisible" title="应用模板到产品" width="500px">
      <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="100px">
        <el-form-item label="目标产品" prop="productId">
          <el-select v-model="applyForm.productId" placeholder="请选择产品" class="!w-full">
            <el-option
              v-for="product in productList"
              :key="product.id"
              :label="product.name"
              :value="product.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="覆盖模式">
          <el-switch v-model="applyForm.overwrite" />
          <span class="text-gray-400 text-sm ml-2">覆盖产品现有物模型</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" :loading="applyLoading" @click="handleApply">确定应用</el-button>
      </template>
    </el-dialog>

    <!-- 创建/编辑模板表单 -->
    <TemplateForm ref="formRef" @success="loadTemplates" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import * as TemplateApi from '@/api/iot/thingmodel/template'
import * as ProductApi from '@/api/iot/product'
import TemplateForm from './TemplateForm.vue'
import TslPreview from './TslPreview.vue'

defineOptions({ name: 'IotThingModelTemplate' })

const loading = ref(false)
const categoryList = ref<TemplateApi.ThingModelTemplateCategoryVO[]>([])
const templateList = ref<TemplateApi.ThingModelTemplateVO[]>([])
const productList = ref<any[]>([])
const selectedCategoryId = ref<number | null>(null)
const searchName = ref('')

// 模板详情
const detailVisible = ref(false)
const currentTemplate = ref<TemplateApi.ThingModelTemplateVO | null>(null)

// 应用模板
const applyVisible = ref(false)
const applyLoading = ref(false)
const applyFormRef = ref<FormInstance>()
const applyForm = reactive({
  productId: undefined as number | undefined,
  overwrite: false
})
const applyRules: FormRules = {
  productId: [{ required: true, message: '请选择产品', trigger: 'change' }]
}

const formRef = ref()

// 获取分类模板数量
const getCategoryTemplateCount = (categoryId: number) => {
  return templateList.value.filter((t) => t.categoryId === categoryId).length
}

// 获取分类名称
const getCategoryName = (categoryId: number) => {
  return categoryList.value.find((c) => c.id === categoryId)?.name || ''
}

// 加载分类
const loadCategories = async () => {
  try {
    categoryList.value = await TemplateApi.getCategoryList()
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

// 加载模板
const loadTemplates = async () => {
  loading.value = true
  try {
    templateList.value = await TemplateApi.getTemplateList({
      categoryId: selectedCategoryId.value || undefined,
      name: searchName.value || undefined
    })
  } finally {
    loading.value = false
  }
}

// 加载产品列表
const loadProducts = async () => {
  try {
    const data = await ProductApi.getProductSimpleList()
    productList.value = data
  } catch (e) {
    console.error('加载产品列表失败', e)
  }
}

// 选择分类
const selectCategory = (categoryId: number | null) => {
  selectedCategoryId.value = categoryId
  loadTemplates()
}

// 打开模板详情
const openDetail = (template: TemplateApi.ThingModelTemplateVO) => {
  currentTemplate.value = template
  detailVisible.value = true
}

// 打开创建/编辑表单
const openForm = (type: 'create' | 'update', id?: number) => {
  detailVisible.value = false
  formRef.value?.open(type, id)
}

// 打开应用对话框
const openApplyDialog = () => {
  applyForm.productId = undefined
  applyForm.overwrite = false
  applyVisible.value = true
  loadProducts()
}

// 应用模板
const handleApply = async () => {
  if (!applyFormRef.value) return
  const valid = await applyFormRef.value.validate()
  if (!valid || !currentTemplate.value) return

  applyLoading.value = true
  try {
    await TemplateApi.applyTemplate({
      templateId: currentTemplate.value.id!,
      productId: applyForm.productId!,
      overwrite: applyForm.overwrite
    })
    ElMessage.success('应用成功')
    applyVisible.value = false
    detailVisible.value = false
    loadTemplates()
  } finally {
    applyLoading.value = false
  }
}

// 删除模板
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除该模板吗？', '提示', { type: 'warning' })
    await TemplateApi.deleteTemplate(id)
    ElMessage.success('删除成功')
    detailVisible.value = false
    await loadTemplates()
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  loadCategories()
  loadTemplates()
})
</script>

<style scoped lang="scss">
.template-library {
  padding: 16px;
}

.category-card {
  min-height: 400px;
}

.category-list {
  .category-item {
    display: flex;
    align-items: center;
    padding: 10px 12px;
    cursor: pointer;
    border-radius: 4px;
    margin-bottom: 4px;
    transition: all 0.2s;

    &:hover {
      background-color: var(--el-fill-color-light);
    }

    &.active {
      background-color: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
    }
  }
}

.template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  min-height: 300px;
}

.template-card {
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--el-color-primary);
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  }

  .template-icon {
    width: 48px;
    height: 48px;
    background: var(--el-fill-color-light);
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 12px;
    color: var(--el-color-primary);
  }

  .template-name {
    font-weight: 500;
    font-size: 14px;
    margin-bottom: 4px;
  }

  .template-desc {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-bottom: 8px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .template-meta {
    display: flex;
    align-items: center;
    gap: 8px;

    .usage-count {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      margin-left: auto;
    }
  }
}

.template-detail {
  .detail-header {
    display: flex;
    gap: 16px;

    .detail-icon {
      width: 80px;
      height: 80px;
      background: var(--el-fill-color-light);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: var(--el-color-primary);
    }

    .detail-info {
      h3 {
        margin: 0 0 8px 0;
      }
      p {
        color: var(--el-text-color-secondary);
        margin: 0 0 8px 0;
      }
      .detail-tags {
        display: flex;
        gap: 8px;
      }
    }
  }

  .detail-actions {
    display: flex;
    gap: 12px;
  }
}
</style>
