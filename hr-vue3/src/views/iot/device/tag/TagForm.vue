<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="90px"
    >
      <el-form-item label="标签键" prop="tagKey">
        <el-input v-model="formData.tagKey" placeholder="请输入标签键，如 location" />
      </el-form-item>
      <el-form-item label="标签值" prop="tagValue">
        <el-input v-model="formData.tagValue" placeholder="请输入标签值，如 车间A" />
      </el-form-item>
      <el-form-item label="标签颜色" prop="color">
        <el-color-picker v-model="formData.color" />
        <span class="ml-2">{{ formData.color }}</span>
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入标签描述"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" :loading="formLoading" @click="submitForm">确 定</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import * as TagApi from '@/api/iot/device/tag'

defineOptions({ name: 'IotDeviceTagForm' })

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive({
  id: undefined as number | undefined,
  tagKey: '',
  tagValue: '',
  color: '#409EFF',
  description: ''
})

const formRules: FormRules = {
  tagKey: [
    { required: true, message: '标签键不能为空', trigger: 'blur' },
    { max: 64, message: '标签键长度不能超过64个字符', trigger: 'blur' }
  ],
  tagValue: [{ max: 128, message: '标签值长度不能超过128个字符', trigger: 'blur' }],
  description: [{ max: 255, message: '描述长度不能超过255个字符', trigger: 'blur' }]
}

const emit = defineEmits(['success'])

/** 打开弹窗 */
const open = async (type: 'create' | 'update', id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '创建标签' : '编辑标签'
  resetForm()

  if (id) {
    formLoading.value = true
    try {
      const data = await TagApi.getTag(id)
      Object.assign(formData, data)
    } finally {
      formLoading.value = false
    }
  }
}

/** 提交表单 */
const submitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return

  formLoading.value = true
  try {
    if (formData.id) {
      await TagApi.updateTag(formData as any)
      ElMessage.success('更新成功')
    } else {
      await TagApi.createTag(formData as any)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.id = undefined
  formData.tagKey = ''
  formData.tagValue = ''
  formData.color = '#409EFF'
  formData.description = ''
  formRef.value?.resetFields()
}

defineExpose({ open })
</script>
