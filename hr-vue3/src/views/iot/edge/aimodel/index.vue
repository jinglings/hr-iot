<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="80px"
    >
      <el-form-item label="模型名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入模型名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="模型类型" prop="modelType">
        <el-select
          v-model="queryParams.modelType"
          placeholder="请选择模型类型"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="(value, key) in EdgeAiModelTypeMap"
            :key="key"
            :label="value.label"
            :value="key"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          class="!w-240px"
        >
          <el-option label="禁用" :value="0" />
          <el-option label="启用" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" />
          重置
        </el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['iot:edge-ai-model:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" />
          新增模型
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="模型名称" prop="name" min-width="150">
        <template #default="{ row }">
          <el-link type="primary" @click="openDetail(row.id)">
            {{ row.name }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column label="模型标识" prop="modelKey" min-width="180" />
      <el-table-column label="模型类型" align="center" prop="modelType" width="140">
        <template #default="{ row }">
          <div class="flex items-center justify-center">
            <Icon :icon="EdgeAiModelTypeMap[row.modelType]?.icon" class="mr-1" />
            <span>{{ EdgeAiModelTypeMap[row.modelType]?.label }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="模型格式" align="center" prop="modelFormat" width="120">
        <template #default="{ row }">
          {{ EdgeAiModelFormatMap[row.modelFormat]?.label }}
        </template>
      </el-table-column>
      <el-table-column label="版本" align="center" prop="modelVersion" width="100" />
      <el-table-column label="模型大小" align="center" prop="modelSize" width="120">
        <template #default="{ row }">
          {{ formatSize(row.modelSize) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        prop="createTime"
        :formatter="dateFormatter"
        width="180"
      />
      <el-table-column label="操作" align="center" fixed="right" width="220">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openDetail(scope.row.id)"
            v-hasPermi="['iot:edge-ai-model:query']"
          >
            详情
          </el-button>
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['iot:edge-ai-model:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            :type="scope.row.status === 1 ? 'warning' : 'success'"
            @click="handleToggleStatus(scope.row)"
            v-hasPermi="['iot:edge-ai-model:update']"
          >
            {{ scope.row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['iot:edge-ai-model:delete']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <ModelForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import {
  EdgeAiModelApi,
  EdgeAiModelVO,
  EdgeAiModelPageReqVO,
  EdgeAiModelTypeMap,
  EdgeAiModelFormatMap
} from '@/api/iot/edge/aimodel'
import ModelForm from './ModelForm.vue'

defineOptions({ name: 'IoTEdgeAiModel' })

const message = useMessage()
const { t } = useI18n()
const router = useRouter()

const loading = ref(true)
const list = ref<EdgeAiModelVO[]>([])
const total = ref(0)
const queryParams = reactive<EdgeAiModelPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  modelType: undefined,
  status: undefined
})
const queryFormRef = ref()
const selectedIds = ref<number[]>([])

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await EdgeAiModelApi.getPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 格式化文件大小 */
const formatSize = (sizeKB: number) => {
  if (!sizeKB) return '-'
  if (sizeKB < 1024) return `${sizeKB} KB`
  if (sizeKB < 1024 * 1024) return `${(sizeKB / 1024).toFixed(2)} MB`
  return `${(sizeKB / 1024 / 1024).toFixed(2)} GB`
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 打开详情 */
const openDetail = (id: number) => {
  router.push({ path: '/iot/edge/aimodel/detail/' + id })
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await EdgeAiModelApi.delete(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

/** 启用/禁用模型 */
const handleToggleStatus = async (row: EdgeAiModelVO) => {
  try {
    const action = row.status === 1 ? '禁用' : '启用'
    await message.confirm(`确认${action}模型"${row.name}"吗？`)
    if (row.status === 1) {
      await EdgeAiModelApi.disable(row.id)
    } else {
      await EdgeAiModelApi.enable(row.id)
    }
    message.success(`${action}成功`)
    await getList()
  } catch {}
}

/** 表格选择事件 */
const handleSelectionChange = (selection: EdgeAiModelVO[]) => {
  selectedIds.value = selection.map((item) => item.id)
}

/** 初始化 */
onMounted(() => {
  getList()
})
</script>
