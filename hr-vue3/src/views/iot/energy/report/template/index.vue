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
      <el-form-item label="模板名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入模板名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="模板编码" prop="code">
        <el-input
          v-model="queryParams.code"
          placeholder="请输入模板编码"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="报表类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择报表类型" clearable class="!w-240px">
          <el-option label="日报" value="daily" />
          <el-option label="周报" value="weekly" />
          <el-option label="月报" value="monthly" />
          <el-option label="年报" value="yearly" />
          <el-option label="自定义" value="custom" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-240px">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['iot:energy-report-template:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="模板名称" align="center" prop="name" min-width="150" />
      <el-table-column label="模板编码" align="center" prop="code" width="150" />
      <el-table-column label="报表类型" align="center" prop="type" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.type === 'daily'" type="primary">日报</el-tag>
          <el-tag v-else-if="scope.row.type === 'weekly'" type="success">周报</el-tag>
          <el-tag v-else-if="scope.row.type === 'monthly'" type="warning">月报</el-tag>
          <el-tag v-else-if="scope.row.type === 'yearly'" type="danger">年报</el-tag>
          <el-tag v-else type="info">自定义</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="模板描述" align="center" prop="description" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" min-width="180px" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['iot:energy-report-template:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="success"
            @click="handleViewConfig(scope.row)"
            v-hasPermi="['iot:energy-report-template:query']"
          >
            查看配置
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['iot:energy-report-template:delete']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <ReportTemplateForm ref="formRef" @success="getList" />

  <!-- 配置查看对话框 -->
  <el-dialog v-model="configDialogVisible" title="报表配置详情" width="800px">
    <el-descriptions :column="1" border>
      <el-descriptions-item label="模板名称">{{ currentTemplate.name }}</el-descriptions-item>
      <el-descriptions-item label="模板编码">{{ currentTemplate.code }}</el-descriptions-item>
      <el-descriptions-item label="报表类型">
        <el-tag v-if="currentTemplate.type === 'daily'" type="primary">日报</el-tag>
        <el-tag v-else-if="currentTemplate.type === 'weekly'" type="success">周报</el-tag>
        <el-tag v-else-if="currentTemplate.type === 'monthly'" type="warning">月报</el-tag>
        <el-tag v-else-if="currentTemplate.type === 'yearly'" type="danger">年报</el-tag>
        <el-tag v-else type="info">自定义</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="模板描述">{{ currentTemplate.description }}</el-descriptions-item>
      <el-descriptions-item label="报表配置">
        <el-input
          type="textarea"
          :value="formatConfig(currentTemplate.config)"
          :rows="15"
          readonly
        />
      </el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <el-button @click="configDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import {
  IotEnergyReportTemplateVO,
  getIotEnergyReportTemplatePage,
  deleteIotEnergyReportTemplate
} from '@/api/iot/energy/report/reportTemplate'
import ReportTemplateForm from './ReportTemplateForm.vue'

/** IoT 能源报表模板管理 */
defineOptions({ name: 'IotEnergyReportTemplate' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<IotEnergyReportTemplateVO[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const configDialogVisible = ref(false) // 配置对话框显示
const currentTemplate = ref<IotEnergyReportTemplateVO>({} as IotEnergyReportTemplateVO) // 当前模板
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  code: undefined,
  type: undefined,
  status: undefined
})
const queryFormRef = ref() // 搜索的表单

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await getIotEnergyReportTemplatePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
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

/** 查看配置 */
const handleViewConfig = (row: IotEnergyReportTemplateVO) => {
  currentTemplate.value = row
  configDialogVisible.value = true
}

/** 格式化配置JSON */
const formatConfig = (config: string) => {
  try {
    if (!config) return ''
    const obj = JSON.parse(config)
    return JSON.stringify(obj, null, 2)
  } catch (error) {
    return config
  }
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await deleteIotEnergyReportTemplate(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>
