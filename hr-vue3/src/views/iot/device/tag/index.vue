<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      :inline="true"
      label-width="68px"
      class="-mb-15px"
    >
      <el-form-item label="标签键" prop="tagKey">
        <el-input
          v-model="queryParams.tagKey"
          placeholder="请输入标签键"
          clearable
          class="!w-200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="标签值" prop="tagValue">
        <el-input
          v-model="queryParams.tagValue"
          placeholder="请输入标签值"
          clearable
          class="!w-200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="预置标签" prop="isPreset">
        <el-select
          v-model="queryParams.isPreset"
          placeholder="全部"
          clearable
          class="!w-160px"
        >
          <el-option label="是" :value="true" />
          <el-option label="否" :value="false" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" /> 搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" /> 重置
        </el-button>
        <el-button
          v-hasPermi="['iot:device-tag:create']"
          type="primary"
          plain
          @click="openForm('create')"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true">
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="标签" align="center" min-width="180">
        <template #default="{ row }">
          <el-tag :color="row.color" effect="dark" style="border: none">
            {{ row.tagKey }}{{ row.tagValue ? '=' + row.tagValue : '' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="标签键" align="center" prop="tagKey" width="120" />
      <el-table-column label="标签值" align="center" prop="tagValue" width="120" />
      <el-table-column label="颜色" align="center" width="80">
        <template #default="{ row }">
          <div
            class="w-6 h-6 rounded mx-auto"
            :style="{ backgroundColor: row.color }"
          ></div>
        </template>
      </el-table-column>
      <el-table-column label="预置" align="center" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.isPreset" type="success" size="small">是</el-tag>
          <el-tag v-else type="info" size="small">否</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="使用次数" align="center" prop="usageCount" width="100" />
      <el-table-column label="描述" align="center" prop="description" show-overflow-tooltip />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column label="操作" align="center" width="140" fixed="right">
        <template #default="{ row }">
          <el-button
            v-hasPermi="['iot:device-tag:update']"
            link
            type="primary"
            @click="openForm('update', row.id)"
          >
            编辑
          </el-button>
          <el-button
            v-if="!row.isPreset"
            v-hasPermi="['iot:device-tag:delete']"
            link
            type="danger"
            @click="handleDelete(row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <TagForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { dateFormatter } from '@/utils/formatTime'
import * as TagApi from '@/api/iot/device/tag'
import TagForm from './TagForm.vue'

defineOptions({ name: 'IotDeviceTag' })

const loading = ref(false)
const list = ref<TagApi.DeviceTagVO[]>([])
const total = ref(0)
const queryFormRef = ref<FormInstance>()
const formRef = ref<InstanceType<typeof TagForm>>()

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  tagKey: undefined as string | undefined,
  tagValue: undefined as string | undefined,
  isPreset: undefined as boolean | undefined
})

/** 获取列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await TagApi.getTagPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置 */
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

/** 打开表单 */
const openForm = (type: 'create' | 'update', id?: number) => {
  formRef.value?.open(type, id)
}

/** 删除 */
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除该标签吗？删除后将解除所有设备的关联。', '提示', {
      type: 'warning'
    })
    await TagApi.deleteTag(id)
    ElMessage.success('删除成功')
    await getList()
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  getList()
})
</script>
