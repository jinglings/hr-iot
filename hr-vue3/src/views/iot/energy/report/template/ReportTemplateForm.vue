<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="900px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="110px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="模板名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入模板名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="模板编码" prop="code">
            <el-input v-model="formData.code" placeholder="请输入模板编码，如: RPT001" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="报表类型" prop="type">
            <el-select v-model="formData.type" placeholder="请选择报表类型" style="width: 100%">
              <el-option label="日报" value="daily" />
              <el-option label="周报" value="weekly" />
              <el-option label="月报" value="monthly" />
              <el-option label="年报" value="yearly" />
              <el-option label="自定义" value="custom" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="模板状态" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio
                v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
                :key="dict.value"
                :label="dict.value"
              >
                {{ dict.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="模板描述" prop="description">
        <el-input type="textarea" v-model="formData.description" placeholder="请输入模板描述" :rows="2" />
      </el-form-item>

      <el-form-item label="报表配置" prop="config">
        <div style="width: 100%">
          <el-tabs v-model="configTab" type="border-card">
            <el-tab-pane label="可视化配置" name="visual">
              <el-form label-width="120px">
                <el-form-item label="包含字段">
                  <el-checkbox-group v-model="visualConfig.fields">
                    <el-checkbox label="totalEnergy">总能耗</el-checkbox>
                    <el-checkbox label="avgPower">平均功率</el-checkbox>
                    <el-checkbox label="maxPower">最大功率</el-checkbox>
                    <el-checkbox label="minPower">最小功率</el-checkbox>
                    <el-checkbox label="coalConsumption">折标煤</el-checkbox>
                    <el-checkbox label="carbonEmission">碳排放</el-checkbox>
                    <el-checkbox label="cost">费用</el-checkbox>
                  </el-checkbox-group>
                </el-form-item>
                <el-form-item label="统计维度">
                  <el-checkbox-group v-model="visualConfig.dimensions">
                    <el-checkbox label="meter">计量点</el-checkbox>
                    <el-checkbox label="building">建筑</el-checkbox>
                    <el-checkbox label="energyType">能源类型</el-checkbox>
                    <el-checkbox label="area">区域</el-checkbox>
                  </el-checkbox-group>
                </el-form-item>
                <el-form-item label="图表类型">
                  <el-checkbox-group v-model="visualConfig.charts">
                    <el-checkbox label="line">折线图</el-checkbox>
                    <el-checkbox label="bar">柱状图</el-checkbox>
                    <el-checkbox label="pie">饼图</el-checkbox>
                    <el-checkbox label="table">数据表格</el-checkbox>
                  </el-checkbox-group>
                </el-form-item>
                <el-form-item label="时间粒度">
                  <el-select v-model="visualConfig.timeGranularity" placeholder="请选择时间粒度" style="width: 100%">
                    <el-option label="小时" value="hour" />
                    <el-option label="天" value="day" />
                    <el-option label="周" value="week" />
                    <el-option label="月" value="month" />
                  </el-select>
                </el-form-item>
                <el-form-item label="是否包含对比">
                  <el-switch v-model="visualConfig.includeComparison" />
                  <span style="margin-left: 10px; color: #909399; font-size: 12px">
                    同比/环比数据对比
                  </span>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            <el-tab-pane label="JSON配置" name="json">
              <el-input
                type="textarea"
                v-model="formData.config"
                placeholder="请输入JSON格式的报表配置"
                :rows="12"
              />
              <div style="margin-top: 10px; color: #909399; font-size: 12px">
                提示：支持JSON格式的自定义配置，也可以通过"可视化配置"标签页进行配置
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'
import {
  IotEnergyReportTemplateVO,
  getIotEnergyReportTemplate,
  createIotEnergyReportTemplate,
  updateIotEnergyReportTemplate
} from '@/api/iot/energy/report/reportTemplate'
import { CommonStatusEnum } from '@/utils/constants'

/** IoT 能源报表模板 表单 */
defineOptions({ name: 'ReportTemplateForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中
const formType = ref('') // 表单的类型
const configTab = ref('visual') // 配置标签页
const visualConfig = ref({
  fields: ['totalEnergy', 'avgPower', 'coalConsumption', 'carbonEmission'],
  dimensions: ['meter', 'building'],
  charts: ['line', 'table'],
  timeGranularity: 'day',
  includeComparison: false
})
const formData = ref({
  id: undefined,
  name: undefined,
  code: undefined,
  type: 'daily',
  description: undefined,
  config: undefined,
  status: CommonStatusEnum.ENABLE
})
const formRules = reactive({
  name: [{ required: true, message: '模板名称不能为空', trigger: 'blur' }],
  code: [{ required: true, message: '模板编码不能为空', trigger: 'blur' }],
  type: [{ required: true, message: '报表类型不能为空', trigger: 'change' }],
  status: [{ required: true, message: '模板状态不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await getIotEnergyReportTemplate(id)
      // 解析配置JSON到可视化配置
      if (formData.value.config) {
        try {
          const config = JSON.parse(formData.value.config)
          if (config.fields) visualConfig.value.fields = config.fields
          if (config.dimensions) visualConfig.value.dimensions = config.dimensions
          if (config.charts) visualConfig.value.charts = config.charts
          if (config.timeGranularity) visualConfig.value.timeGranularity = config.timeGranularity
          if (config.includeComparison !== undefined) visualConfig.value.includeComparison = config.includeComparison
        } catch (error) {
          console.error('解析配置JSON失败:', error)
        }
      }
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件
const submitForm = async () => {
  // 如果在可视化配置标签页，自动生成JSON配置
  if (configTab.value === 'visual') {
    formData.value.config = JSON.stringify(visualConfig.value, null, 2)
  }

  // 校验表单
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as IotEnergyReportTemplateVO
    if (formType.value === 'create') {
      await createIotEnergyReportTemplate(data)
      message.success(t('common.createSuccess'))
    } else {
      await updateIotEnergyReportTemplate(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    name: undefined,
    code: undefined,
    type: 'daily',
    description: undefined,
    config: undefined,
    status: CommonStatusEnum.ENABLE
  }
  visualConfig.value = {
    fields: ['totalEnergy', 'avgPower', 'coalConsumption', 'carbonEmission'],
    dimensions: ['meter', 'building'],
    charts: ['line', 'table'],
    timeGranularity: 'day',
    includeComparison: false
  }
  configTab.value = 'visual'
  formRef.value?.resetFields()
}
</script>
