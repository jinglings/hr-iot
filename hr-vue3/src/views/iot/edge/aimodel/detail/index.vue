<template>
  <ContentWrap>
    <!-- 模型基本信息卡片 -->
    <el-card class="mb-4">
      <template #header>
        <div class="flex justify-between items-center">
          <div class="flex items-center">
            <Icon :icon="EdgeAiModelTypeMap[model.modelType]?.icon" class="text-2xl mr-2" />
            <span class="text-lg font-bold">{{ model.name }}</span>
          </div>
          <div>
            <el-tag :type="model.status === 1 ? 'success' : 'info'">
              {{ model.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </div>
        </div>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="模型ID">{{ model.id }}</el-descriptions-item>
        <el-descriptions-item label="模型标识">{{ model.modelKey }}</el-descriptions-item>
        <el-descriptions-item label="模型类型">
          {{ EdgeAiModelTypeMap[model.modelType]?.label }}
        </el-descriptions-item>
        <el-descriptions-item label="模型格式">
          {{ EdgeAiModelFormatMap[model.modelFormat]?.label }}
        </el-descriptions-item>
        <el-descriptions-item label="模型版本">{{ model.modelVersion }}</el-descriptions-item>
        <el-descriptions-item label="模型大小">
          {{ formatSize(model.modelSize) }}
        </el-descriptions-item>
        <el-descriptions-item label="模型文件URL" :span="3">
          <el-link :href="model.modelUrl" target="_blank" type="primary">
            {{ model.modelUrl }}
          </el-link>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ formatDate(model.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ formatDate(model.updateTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="3">
          {{ model.description || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 配置信息卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="12">
        <el-card header="输入配置">
          <el-input
            v-model="inputJson"
            type="textarea"
            :rows="10"
            readonly
            placeholder="输入配置（JSON格式）"
          />
          <div class="mt-2 text-sm text-gray-500">
            <Icon icon="ep:info-filled" class="mr-1" />
            定义模型输入的格式和要求
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="输出配置">
          <el-input
            v-model="outputJson"
            type="textarea"
            :rows="10"
            readonly
            placeholder="输出配置（JSON格式）"
          />
          <div class="mt-2 text-sm text-gray-500">
            <Icon icon="ep:info-filled" class="mr-1" />
            定义模型输出的格式和处理方式
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 部署情况 -->
    <el-card header="部署情况" class="mb-4">
      <ModelDeployment v-if="activeTab === 'deployment'" :model-id="model.id" />
    </el-card>

    <!-- 操作按钮 -->
    <div class="text-center mt-4">
      <el-button type="primary" @click="handleEdit" v-hasPermi="['iot:edge-ai-model:update']">
        <Icon icon="ep:edit" class="mr-5px" />
        编辑模型
      </el-button>
      <el-button
        :type="model.status === 1 ? 'warning' : 'success'"
        @click="handleToggleStatus"
        v-hasPermi="['iot:edge-ai-model:update']"
      >
        <Icon icon="ep:switch-button" class="mr-5px" />
        {{ model.status === 1 ? '禁用模型' : '启用模型' }}
      </el-button>
      <el-button type="danger" @click="handleDelete" v-hasPermi="['iot:edge-ai-model:delete']">
        <Icon icon="ep:delete" class="mr-5px" />
        删除模型
      </el-button>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import {
  EdgeAiModelApi,
  EdgeAiModelVO,
  EdgeAiModelTypeMap,
  EdgeAiModelFormatMap
} from '@/api/iot/edge/aimodel'
import { formatDate } from '@/utils/formatTime'
import ModelDeployment from './ModelDeployment.vue'

defineOptions({ name: 'IoTEdgeAiModelDetail' })

const route = useRoute()
const router = useRouter()
const message = useMessage()
const { t } = useI18n()

const id = Number(route.params.id)
const loading = ref(true)
const model = ref<EdgeAiModelVO>({} as EdgeAiModelVO)
const activeTab = ref('deployment')

/** 格式化文件大小 */
const formatSize = (sizeKB: number) => {
  if (!sizeKB) return '-'
  if (sizeKB < 1024) return `${sizeKB} KB`
  if (sizeKB < 1024 * 1024) return `${(sizeKB / 1024).toFixed(2)} MB`
  return `${(sizeKB / 1024 / 1024).toFixed(2)} GB`
}

// JSON格式化显示
const inputJson = computed(() => {
  try {
    return model.value.inputConfig
      ? JSON.stringify(JSON.parse(model.value.inputConfig), null, 2)
      : ''
  } catch {
    return model.value.inputConfig
  }
})

const outputJson = computed(() => {
  try {
    return model.value.outputConfig
      ? JSON.stringify(JSON.parse(model.value.outputConfig), null, 2)
      : ''
  } catch {
    return model.value.outputConfig
  }
})

/** 获取模型详情 */
const getModelData = async () => {
  loading.value = true
  try {
    model.value = await EdgeAiModelApi.get(id)
  } finally {
    loading.value = false
  }
}

/** 编辑模型 */
const handleEdit = () => {
  router.push({ path: '/iot/edge/aimodel', query: { edit: model.value.id } })
}

/** 启用/禁用模型 */
const handleToggleStatus = async () => {
  try {
    const action = model.value.status === 1 ? '禁用' : '启用'
    await message.confirm(`确认${action}该模型吗？`)
    if (model.value.status === 1) {
      await EdgeAiModelApi.disable(model.value.id)
    } else {
      await EdgeAiModelApi.enable(model.value.id)
    }
    message.success(`${action}成功`)
    await getModelData()
  } catch {}
}

/** 删除模型 */
const handleDelete = async () => {
  try {
    await message.delConfirm()
    await EdgeAiModelApi.delete(model.value.id)
    message.success(t('common.delSuccess'))
    router.back()
  } catch {}
}

/** 初始化 */
onMounted(async () => {
  if (!id) {
    message.warning('参数错误，模型不能为空！')
    return
  }
  await getModelData()
})
</script>
