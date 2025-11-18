<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="800px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="规则名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入规则名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属网关" prop="gatewayId">
            <el-select
              v-model="formData.gatewayId"
              placeholder="请选择网关"
              :disabled="formType === 'update'"
              class="w-full"
            >
              <el-option
                v-for="gateway in gatewayList"
                :key="gateway.id"
                :label="gateway.name"
                :value="gateway.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="规则类型" prop="ruleType">
            <el-select
              v-model="formData.ruleType"
              placeholder="请选择规则类型"
              :disabled="formType === 'update'"
              class="w-full"
            >
              <el-option
                v-for="(value, key) in EdgeRuleTypeMap"
                :key="key"
                :label="value.label"
                :value="key"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="优先级" prop="priority">
            <el-input-number
              v-model="formData.priority"
              :min="0"
              :max="100"
              placeholder="0-100"
              class="w-full"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="本地执行" prop="executeLocal">
        <el-switch v-model="formData.executeLocal" :active-value="1" :inactive-value="0" />
        <span class="ml-2 text-gray-500 text-sm">启用后规则将在边缘网关本地执行</span>
      </el-form-item>

      <el-form-item label="触发器配置" prop="triggerConfig">
        <el-input
          v-model="formData.triggerConfig"
          type="textarea"
          :rows="4"
          placeholder='请输入触发器配置（JSON格式），例如：{"deviceId": 1, "property": "temperature", "operator": ">", "value": 30}'
        />
        <span class="text-gray-500 text-xs">定义规则的触发条件（JSON格式）</span>
      </el-form-item>

      <el-form-item label="条件配置" prop="conditionConfig">
        <el-input
          v-model="formData.conditionConfig"
          type="textarea"
          :rows="3"
          placeholder='请输入条件配置（JSON格式，可选），例如：{"time": "08:00-18:00", "week": [1,2,3,4,5]}'
        />
        <span class="text-gray-500 text-xs">定义额外的执行条件，如时间范围、星期等（可选）</span>
      </el-form-item>

      <el-form-item label="动作配置" prop="actionConfig">
        <el-input
          v-model="formData.actionConfig"
          type="textarea"
          :rows="4"
          placeholder='请输入动作配置（JSON格式），例如：{"type": "DEVICE_CONTROL", "deviceId": 2, "command": "turnOn"}'
        />
        <span class="text-gray-500 text-xs">定义规则触发后的动作（JSON格式）</span>
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入规则描述"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm" :loading="formLoading">确 定</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import {
  EdgeRuleApi,
  EdgeRuleVO,
  EdgeRuleCreateReqVO,
  EdgeRuleUpdateReqVO,
  EdgeRuleTypeMap
} from '@/api/iot/edge/rule'
import { EdgeGatewayApi, EdgeGatewayVO } from '@/api/iot/edge/gateway'

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<EdgeRuleCreateReqVO | EdgeRuleUpdateReqVO>({
  id: undefined,
  name: '',
  gatewayId: undefined,
  ruleType: 'SCENE',
  triggerConfig: '',
  conditionConfig: '',
  actionConfig: '',
  executeLocal: 0,
  priority: 50,
  description: ''
})

// 验证JSON格式
const validateJson = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback()
    return
  }
  try {
    JSON.parse(value)
    callback()
  } catch (e) {
    callback(new Error('请输入有效的JSON格式'))
  }
}

const formRules = reactive({
  name: [{ required: true, message: '规则名称不能为空', trigger: 'blur' }],
  gatewayId: [{ required: true, message: '请选择网关', trigger: 'change' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  triggerConfig: [
    { required: true, message: '触发器配置不能为空', trigger: 'blur' },
    { validator: validateJson, trigger: 'blur' }
  ],
  conditionConfig: [{ validator: validateJson, trigger: 'blur' }],
  actionConfig: [
    { required: true, message: '动作配置不能为空', trigger: 'blur' },
    { validator: validateJson, trigger: 'blur' }
  ]
})
const formRef = ref()

// 网关列表
const gatewayList = ref<EdgeGatewayVO[]>([])

/** 获取网关列表 */
const getGatewayList = async () => {
  const data = await EdgeGatewayApi.getPage({ pageNo: 1, pageSize: 1000 })
  gatewayList.value = data.list
}

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增规则' : '编辑规则'
  formType.value = type
  resetForm()
  await getGatewayList()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      const data = await EdgeRuleApi.get(id)
      formData.value = data
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  // 校验表单
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as any
    if (formType.value === 'create') {
      await EdgeRuleApi.create(data)
      message.success('新增成功')
    } else {
      await EdgeRuleApi.update(data)
      message.success('修改成功')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    name: '',
    gatewayId: undefined,
    ruleType: 'SCENE',
    triggerConfig: '',
    conditionConfig: '',
    actionConfig: '',
    executeLocal: 0,
    priority: 50,
    description: ''
  }
  formRef.value?.resetFields()
}
</script>
