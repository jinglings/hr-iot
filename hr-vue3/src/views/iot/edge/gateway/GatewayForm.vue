<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="网关名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入网关名称" />
      </el-form-item>
      <el-form-item label="序列号" prop="serialNumber">
        <el-input
          v-model="formData.serialNumber"
          placeholder="请输入网关序列号"
          :disabled="formType === 'update'"
        />
      </el-form-item>
      <el-form-item label="设备型号" prop="deviceType">
        <el-input v-model="formData.deviceType" placeholder="请输入设备型号" />
      </el-form-item>
      <el-form-item label="安装位置" prop="location">
        <el-input v-model="formData.location" placeholder="请输入安装位置" />
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入描述"
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
import { EdgeGatewayApi, EdgeGatewayVO, EdgeGatewayCreateReqVO, EdgeGatewayUpdateReqVO } from '@/api/iot/edge/gateway'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref<EdgeGatewayCreateReqVO | EdgeGatewayUpdateReqVO>({
  id: undefined,
  name: '',
  serialNumber: '',
  deviceType: '',
  location: '',
  description: ''
})
const formRules = reactive({
  name: [{ required: true, message: '网关名称不能为空', trigger: 'blur' }],
  serialNumber: [
    { required: true, message: '序列号不能为空', trigger: 'blur' },
    { min: 6, max: 64, message: '序列号长度必须介于 6 和 64 之间', trigger: 'blur' }
  ]
})
const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增网关' : '编辑网关'
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await EdgeGatewayApi.get(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as any
    if (formType.value === 'create') {
      await EdgeGatewayApi.create(data)
      message.success('新增成功')
    } else {
      await EdgeGatewayApi.update(data)
      message.success('修改成功')
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
    name: '',
    serialNumber: '',
    deviceType: '',
    location: '',
    description: ''
  }
  formRef.value?.resetFields()
}
</script>
