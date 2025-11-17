<template>
  <el-drawer
    v-model="visible"
    title="模板库"
    :size="900"
    direction="rtl"
    :close-on-click-modal="false"
  >
    <div class="template-library">
      <!-- 分类标签 -->
      <div class="category-tabs">
        <el-button
          v-for="category in categories"
          :key="category.value"
          :type="selectedCategory === category.value ? 'primary' : ''"
          @click="selectedCategory = category.value"
        >
          <Icon :icon="category.icon" class="mr-5px" />
          {{ category.label }}
        </el-button>
        <el-button
          :type="selectedCategory === null ? 'primary' : ''"
          @click="selectedCategory = null"
        >
          <Icon icon="ep:grid" class="mr-5px" />
          全部模板
        </el-button>
      </div>

      <!-- 模板列表 -->
      <div class="template-list">
        <el-empty v-if="filteredTemplates.length === 0" description="暂无模板" />
        <div v-else class="template-grid">
          <div
            v-for="template in filteredTemplates"
            :key="template.id"
            class="template-card"
            @click="handlePreviewTemplate(template)"
          >
            <div class="template-thumbnail">
              <img v-if="template.thumbnail" :src="template.thumbnail" alt="" />
              <div v-else class="template-placeholder">
                <Icon icon="ep:picture" :size="48" />
              </div>
              <!-- 内置标签 -->
              <div v-if="template.isBuiltIn" class="built-in-tag">
                <Icon icon="ep:star-filled" />
                内置
              </div>
            </div>
            <div class="template-info">
              <div class="template-name">{{ template.name }}</div>
              <div class="template-description">{{ template.description }}</div>
              <div v-if="template.tags && template.tags.length > 0" class="template-tags">
                <el-tag
                  v-for="tag in template.tags"
                  :key="tag"
                  size="small"
                  type="info"
                  effect="plain"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </div>
            <div class="template-actions">
              <el-button type="primary" size="small" @click.stop="handleApplyTemplate(template)">
                <Icon icon="ep:check" class="mr-5px" />
                应用模板
              </el-button>
              <el-button size="small" @click.stop="handlePreviewTemplate(template)">
                <Icon icon="ep:view" class="mr-5px" />
                预览
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 模板预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      :title="previewTemplate?.name"
      width="80%"
      :append-to-body="true"
    >
      <div v-if="previewTemplate" class="template-preview">
        <div class="preview-info">
          <p><strong>描述：</strong>{{ previewTemplate.description }}</p>
          <p><strong>分类：</strong>{{ getCategoryLabel(previewTemplate.category) }}</p>
          <p v-if="previewTemplate.author">
            <strong>作者：</strong>{{ previewTemplate.author }}
          </p>
          <p>
            <strong>组件数量：</strong>{{ previewTemplate.config.components.length }} 个
          </p>
          <p>
            <strong>画布尺寸：</strong>{{ previewTemplate.config.width }} x
            {{ previewTemplate.config.height }}
          </p>
        </div>
        <div class="preview-image">
          <img v-if="previewTemplate.preview" :src="previewTemplate.preview" alt="" />
          <div v-else class="preview-placeholder">暂无预览图</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">取消</el-button>
        <el-button type="primary" @click="handleApplyTemplate(previewTemplate!)">
          应用模板
        </el-button>
      </template>
    </el-dialog>
  </el-drawer>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { DashboardTemplate } from '@/types/template'
import { TemplateCategory } from '@/types/template'
import { templateLibraryConfig, getTemplatesByCategory } from '@/config/templates'
import { useDashboardStore } from '@/store/modules/dashboard'

interface Props {
  modelValue: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const dashboardStore = useDashboardStore()

// 显示控制
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 分类
const categories = templateLibraryConfig.categories
const selectedCategory = ref<TemplateCategory | null>(null)

// 过滤后的模板
const filteredTemplates = computed(() => {
  if (selectedCategory.value === null) {
    return templateLibraryConfig.templates
  }
  return getTemplatesByCategory(selectedCategory.value)
})

// 预览
const previewVisible = ref(false)
const previewTemplate = ref<DashboardTemplate>()

// 获取分类标签
const getCategoryLabel = (category: TemplateCategory) => {
  return categories.find((c) => c.value === category)?.label || category
}

// 预览模板
const handlePreviewTemplate = (template: DashboardTemplate) => {
  previewTemplate.value = template
  previewVisible.value = true
}

// 应用模板
const handleApplyTemplate = async (template: DashboardTemplate) => {
  try {
    await ElMessageBox.confirm(
      `确定要应用模板"${template.name}"吗？这将替换当前画布的所有内容。`,
      '确认应用模板',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )

    // 应用模板
    dashboardStore.applyTemplate(template)

    ElMessage.success('模板应用成功')
    visible.value = false
    previewVisible.value = false
  } catch (error) {
    // 用户取消
  }
}
</script>

<style lang="scss" scoped>
.template-library {
  height: 100%;
  display: flex;
  flex-direction: column;

  .category-tabs {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--el-border-color);
    margin-bottom: 16px;
  }

  .template-list {
    flex: 1;
    overflow-y: auto;
  }

  .template-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
    gap: 16px;
  }

  .template-card {
    border: 1px solid var(--el-border-color);
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      transform: translateY(-2px);
    }

    .template-thumbnail {
      position: relative;
      width: 100%;
      height: 200px;
      background-color: #f5f5f5;
      overflow: hidden;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .template-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #999;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        opacity: 0.6;
      }

      .built-in-tag {
        position: absolute;
        top: 8px;
        right: 8px;
        background: rgba(245, 158, 11, 0.9);
        color: #fff;
        padding: 4px 12px;
        border-radius: 12px;
        font-size: 12px;
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }

    .template-info {
      padding: 16px;

      .template-name {
        font-size: 16px;
        font-weight: 600;
        color: var(--el-text-color-primary);
        margin-bottom: 8px;
      }

      .template-description {
        font-size: 13px;
        color: var(--el-text-color-regular);
        line-height: 1.5;
        margin-bottom: 12px;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      .template-tags {
        display: flex;
        gap: 6px;
        flex-wrap: wrap;
      }
    }

    .template-actions {
      padding: 0 16px 16px;
      display: flex;
      gap: 8px;

      .el-button {
        flex: 1;
      }
    }
  }
}

.template-preview {
  .preview-info {
    margin-bottom: 16px;
    padding: 16px;
    background-color: var(--el-fill-color-light);
    border-radius: 8px;

    p {
      margin: 8px 0;
      font-size: 14px;
      color: var(--el-text-color-regular);

      strong {
        color: var(--el-text-color-primary);
        margin-right: 8px;
      }
    }
  }

  .preview-image {
    width: 100%;
    min-height: 400px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f5f5f5;
    border-radius: 8px;

    img {
      max-width: 100%;
      max-height: 600px;
    }

    .preview-placeholder {
      color: #999;
      font-size: 14px;
    }
  }
}
</style>
