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
          <el-form-item label="模型名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入模型名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="模型标识" prop="modelKey">
            <el-input
              v-model="formData.modelKey"
              placeholder="请输入模型标识"
              :disabled="formType === 'update'"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="模型类型" prop="modelType">
            <el-select
              v-model="formData.modelType"
              placeholder="请选择模型类型"
              :disabled="formType === 'update'"
              class="w-full"
            >
              <el-option
                v-for="(value, key) in EdgeAiModelTypeMap"
                :key="key"
                :label="value.label"
                :value="key"
              >
                <div class="flex items-center">
                  <Icon :icon="value.icon" class="mr-2" />
                  <span>{{ value.label }}</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="模型格式" prop="modelFormat">
            <el-select
              v-model="formData.modelFormat"
              placeholder="请选择模型格式"
              :disabled="formType === 'update'"
              class="w-full"
            >
              <el-option
                v-for="(value, key) in EdgeAiModelFormatMap"
                :key="key"
                :label="value.label"
                :value="key"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="模型版本" prop="modelVersion">
            <el-input v-model="formData.modelVersion" placeholder="例如: v1.0.0" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="模型大小(KB)" prop="modelSize">
            <el-input-number
              v-model="formData.modelSize"
              :min="0"
              placeholder="模型文件大小"
              class="w-full"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="模型文件URL" prop="modelUrl">
        <el-input
          v-model="formData.modelUrl"
          placeholder="请输入模型文件下载地址，例如: https://example.com/model.onnx"
        />
        <span class="text-gray-500 text-xs">模型文件的下载地址，将被边缘网关下载并部署</span>
      </el-form-item>

      <el-form-item label="输入配置" prop="inputConfig">
        <el-input
          v-model="formData.inputConfig"
          type="textarea"
          :rows="4"
          placeholder='请输入输入配置（JSON格式），例如：{"shape": [1, 3, 224, 224], "dtype": "float32"}'
        />
        <span class="text-gray-500 text-xs">定义模型输入的格式和要求（JSON格式）</span>
      </el-form-item>

      <el-form-item label="输出配置" prop="outputConfig">
        <el-input
          v-model="formData.outputConfig"
          type="textarea"
          :rows="4"
          placeholder='请输入输出配置（JSON格式），例如：{"classes": ["cat", "dog"], "threshold": 0.5}'
        />
        <span class="text-gray-500 text-xs">定义模型输出的格式和处理方式（JSON格式）</span>
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入模型描述"
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
  EdgeAiModelApi,
  EdgeAiModelVO,
  EdgeAiModelCreateReqVO,
  EdgeAiModelUpdateReqVO,
  EdgeAiModelTypeMap,
  EdgeAiModelFormatMap
} from '@/api/iot/edge/aimodel'

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<EdgeAiModelCreateReqVO | EdgeAiModelUpdateReqVO>({
  id: undefined,
  name: '',
  modelKey: '',
  modelType: 'IMAGE_CLASSIFICATION',
  modelFormat: 'ONNX',
  modelVersion: '',
  modelUrl: '',
  modelSize: undefined,
  inputConfig: '',
  outputConfig: '',
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

// 验证URL格式
const validateUrl = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('模型文件URL不能为空'))
    return
  }
  const urlPattern = /^(https?:\/\/)?([\da-z.-]+)\.([a-z.]{2,6})([/\w .-]*)*\/?$/
  if (!urlPattern.test(value)) {
    callback(new Error('请输入有效的URL地址'))
  } else {
    callback()
  }
}

const formRules = reactive({
  name: [{ required: true, message: '模型名称不能为空', trigger: 'blur' }],
  modelKey: [
    { required: true, message: '模型标识不能为空', trigger: 'blur' },
    { min: 3, max: 64, message: '模型标识长度必须介于 3 和 64 之间', trigger: 'blur' }
  ],
  modelType: [{ required: true, message: '请选择模型类型', trigger: 'change' }],
  modelFormat: [{ required: true, message: '请选择模型格式', trigger: 'change' }],
  modelVersion: [{ required: true, message: '模型版本不能为空', trigger: 'blur' }],
  modelUrl: [
    { required: true, validator: validateUrl, trigger: 'blur' }
  ],
  inputConfig: [{ validator: validateJson, trigger: 'blur' }],
  outputConfig: [{ validator: validateJson, trigger: 'blur' }]
})
const formRef = ref()

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增AI模型' : '编辑AI模型'
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      const data = await EdgeAiModelApi.get(id)
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
      await EdgeAiModelApi.create(data)
      message.success('新增成功')
    } else {
      await EdgeAiModelApi.update(data)
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
    modelKey: '',
    modelType: 'IMAGE_CLASSIFICATION',
    modelFormat: 'ONNX',
    modelVersion: '',
    modelUrl: '',
    modelSize: undefined,
    inputConfig: '',
    outputConfig: '',
    description: ''
  }
  formRef.value?.resetFields()
}
</script>
