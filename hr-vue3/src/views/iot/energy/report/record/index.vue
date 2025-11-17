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
      <el-form-item label="报表模板" prop="templateId">
        <el-select v-model="queryParams.templateId" placeholder="请选择报表模板" clearable class="!w-240px">
          <el-option
            v-for="template in templateList"
            :key="template.id"
            :label="template.name"
            :value="template.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="报表名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入报表名称"
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
      <el-form-item label="所属建筑" prop="buildingId">
        <el-select v-model="queryParams.buildingId" placeholder="请选择建筑" clearable class="!w-240px">
          <el-option
            v-for="building in buildingList"
            :key="building.id"
            :label="building.name"
            :value="building.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="报表状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-240px">
          <el-option label="生成中" :value="0" />
          <el-option label="生成成功" :value="1" />
          <el-option label="生成失败" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openGenerateDialog"
          v-hasPermi="['iot:energy-report-record:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 生成报表
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="报表名称" align="center" prop="name" min-width="180" />
      <el-table-column label="报表模板" align="center" prop="templateName" width="150" />
      <el-table-column label="报表类型" align="center" prop="type" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.type === 'daily'" type="primary">日报</el-tag>
          <el-tag v-else-if="scope.row.type === 'weekly'" type="success">周报</el-tag>
          <el-tag v-else-if="scope.row.type === 'monthly'" type="warning">月报</el-tag>
          <el-tag v-else-if="scope.row.type === 'yearly'" type="danger">年报</el-tag>
          <el-tag v-else type="info">自定义</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="所属建筑" align="center" prop="buildingName" width="120" />
      <el-table-column label="统计时间" align="center" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.startTime, 'YYYY-MM-DD') }} 至<br />
          {{ formatDate(scope.row.endTime, 'YYYY-MM-DD') }}
        </template>
      </el-table-column>
      <el-table-column label="报表状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0" type="info">生成中</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="success">生成成功</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="danger">生成失败</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="生成时间"
        align="center"
        prop="generateTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="创建人" align="center" prop="createBy" width="100" />
      <el-table-column label="操作" align="center" min-width="240px" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="handleViewData(scope.row)"
            v-hasPermi="['iot:energy-report-record:query']"
            :disabled="scope.row.status !== 1"
          >
            查看数据
          </el-button>
          <el-button
            link
            type="success"
            @click="handleExportExcel(scope.row.id)"
            v-hasPermi="['iot:energy-report-record:export']"
            :disabled="scope.row.status !== 1"
          >
            导出Excel
          </el-button>
          <el-button
            link
            type="warning"
            @click="handleExportPdf(scope.row.id)"
            v-hasPermi="['iot:energy-report-record:export']"
            :disabled="scope.row.status !== 1"
          >
            导出PDF
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['iot:energy-report-record:delete']"
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

  <!-- 生成报表对话框 -->
  <el-dialog v-model="generateDialogVisible" title="生成能源报表" width="700px">
    <el-form
      ref="generateFormRef"
      :model="generateForm"
      :rules="generateRules"
      label-width="110px"
    >
      <el-form-item label="报表模板" prop="templateId">
        <el-select v-model="generateForm.templateId" placeholder="请选择报表模板" style="width: 100%">
          <el-option
            v-for="template in templateList"
            :key="template.id"
            :label="template.name + ' (' + getTypeLabel(template.type) + ')'"
            :value="template.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="报表名称" prop="name">
        <el-input v-model="generateForm.name" placeholder="请输入报表名称" />
      </el-form-item>
      <el-form-item label="统计时间" prop="timeRange">
        <el-date-picker
          v-model="generateForm.timeRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="所属建筑" prop="buildingId">
        <el-select v-model="generateForm.buildingId" placeholder="请选择建筑（可选）" clearable style="width: 100%">
          <el-option
            v-for="building in buildingList"
            :key="building.id"
            :label="building.name"
            :value="building.id"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleGenerate" type="primary" :loading="generateLoading">
        <Icon icon="ep:document-add" class="mr-5px" /> 开始生成
      </el-button>
      <el-button @click="generateDialogVisible = false">取 消</el-button>
    </template>
  </el-dialog>

  <!-- 报表数据查看对话框 -->
  <el-dialog v-model="dataDialogVisible" title="报表数据详情" width="900px">
    <el-descriptions :column="2" border>
      <el-descriptions-item label="报表名称">{{ currentRecord.name }}</el-descriptions-item>
      <el-descriptions-item label="报表模板">{{ currentRecord.templateName }}</el-descriptions-item>
      <el-descriptions-item label="报表类型">
        <el-tag v-if="currentRecord.type === 'daily'" type="primary">日报</el-tag>
        <el-tag v-else-if="currentRecord.type === 'weekly'" type="success">周报</el-tag>
        <el-tag v-else-if="currentRecord.type === 'monthly'" type="warning">月报</el-tag>
        <el-tag v-else-if="currentRecord.type === 'yearly'" type="danger">年报</el-tag>
        <el-tag v-else type="info">自定义</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="所属建筑">{{ currentRecord.buildingName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="统计时间" :span="2">
        {{ formatDate(currentRecord.startTime, 'YYYY-MM-DD HH:mm') }} 至 {{ formatDate(currentRecord.endTime, 'YYYY-MM-DD HH:mm') }}
      </el-descriptions-item>
      <el-descriptions-item label="生成时间" :span="2">
        {{ formatDate(currentRecord.generateTime, 'YYYY-MM-DD HH:mm:ss') }}
      </el-descriptions-item>
    </el-descriptions>
    <el-divider />
    <div style="margin-bottom: 10px; font-weight: 600;">报表数据</div>
    <el-input
      type="textarea"
      :value="formatReportData(currentRecord.data)"
      :rows="15"
      readonly
    />
    <template #footer>
      <el-button @click="dataDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { formatDate, dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import {
  IotEnergyReportRecordVO,
  getIotEnergyReportRecordPage,
  generateIotEnergyReport,
  deleteIotEnergyReportRecord,
  exportIotEnergyReportExcel,
  exportIotEnergyReportPdf
} from '@/api/iot/energy/report/reportRecord'
import { getIotEnergyReportTemplateSimpleList } from '@/api/iot/energy/report/reportTemplate'
import { getIotEnergyBuildingSimpleList } from '@/api/iot/energy/building'

/** IoT 能源报表记录管理 */
defineOptions({ name: 'IotEnergyReportRecord' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<IotEnergyReportRecordVO[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const templateList = ref([]) // 报表模板列表
const buildingList = ref([]) // 建筑列表
const generateDialogVisible = ref(false) // 生成报表对话框
const dataDialogVisible = ref(false) // 数据查看对话框
const generateLoading = ref(false) // 生成加载中
const currentRecord = ref<IotEnergyReportRecordVO>({} as IotEnergyReportRecordVO) // 当前记录
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  templateId: undefined,
  name: undefined,
  type: undefined,
  buildingId: undefined,
  status: undefined
})
const queryFormRef = ref() // 搜索的表单
const generateFormRef = ref() // 生成表单
const generateForm = ref({
  templateId: undefined,
  name: undefined,
  timeRange: [],
  buildingId: undefined
})
const generateRules = reactive({
  templateId: [{ required: true, message: '请选择报表模板', trigger: 'change' }],
  name: [{ required: true, message: '请输入报表名称', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择统计时间', trigger: 'change' }]
})

/** 查询模板列表 */
const getTemplateList = async () => {
  templateList.value = await getIotEnergyReportTemplateSimpleList()
}

/** 查询建筑列表 */
const getBuildingList = async () => {
  buildingList.value = await getIotEnergyBuildingSimpleList()
}

/** 获取类型标签 */
const getTypeLabel = (type: string) => {
  const typeMap = {
    daily: '日报',
    weekly: '周报',
    monthly: '月报',
    yearly: '年报',
    custom: '自定义'
  }
  return typeMap[type] || type
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await getIotEnergyReportRecordPage(queryParams)
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

/** 打开生成报表对话框 */
const openGenerateDialog = () => {
  generateForm.value = {
    templateId: undefined,
    name: undefined,
    timeRange: [],
    buildingId: undefined
  }
  generateDialogVisible.value = true
}

/** 生成报表 */
const handleGenerate = async () => {
  await generateFormRef.value.validate()

  generateLoading.value = true
  try {
    const data = {
      templateId: generateForm.value.templateId,
      name: generateForm.value.name,
      startTime: generateForm.value.timeRange[0],
      endTime: generateForm.value.timeRange[1],
      buildingId: generateForm.value.buildingId
    }
    await generateIotEnergyReport(data)
    message.success('报表生成任务已提交，请稍后查看')
    generateDialogVisible.value = false
    // 刷新列表
    await getList()
  } finally {
    generateLoading.value = false
  }
}

/** 查看报表数据 */
const handleViewData = (row: IotEnergyReportRecordVO) => {
  currentRecord.value = row
  dataDialogVisible.value = true
}

/** 格式化报表数据 */
const formatReportData = (data: string) => {
  try {
    if (!data) return ''
    const obj = JSON.parse(data)
    return JSON.stringify(obj, null, 2)
  } catch (error) {
    return data
  }
}

/** 导出Excel */
const handleExportExcel = async (id: number) => {
  try {
    message.loading('正在导出Excel，请稍候...')
    const data = await exportIotEnergyReportExcel(id)
    download.excel(data, `能源报表_${id}.xlsx`)
    message.success('导出Excel成功')
  } catch (error) {
    console.error('导出Excel失败:', error)
    message.error('导出Excel失败')
  }
}

/** 导出PDF */
const handleExportPdf = async (id: number) => {
  try {
    message.loading('正在导出PDF，请稍候...')
    const data = await exportIotEnergyReportPdf(id)
    download.pdf(data, `能源报表_${id}.pdf`)
    message.success('导出PDF成功')
  } catch (error) {
    console.error('导出PDF失败:', error)
    message.error('导出PDF失败')
  }
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await deleteIotEnergyReportRecord(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 初始化 **/
onMounted(() => {
  getTemplateList()
  getBuildingList()
  getList()
})
</script>
