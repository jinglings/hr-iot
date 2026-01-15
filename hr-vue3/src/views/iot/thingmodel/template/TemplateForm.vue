<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="模板名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入模板名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="模板编码" prop="code">
            <el-input v-model="formData.code" placeholder="请输入模板编码" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="所属分类" prop="categoryId">
            <el-select v-model="formData.categoryId" placeholder="请选择分类" class="!w-full">
              <el-option
                v-for="category in categoryList"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="模板图标" prop="icon">
            <el-input v-model="formData.icon" placeholder="如 ep:thermometer" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="模板描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="2"
          placeholder="请输入模板描述"
        />
      </el-form-item>
      <el-form-item label="TSL定义" prop="tsl">
        <el-input
          v-model="formData.tsl"
          type="textarea"
          :rows="12"
          placeholder="请输入物模型TSL定义（JSON格式）"
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import * as TemplateApi from '@/api/iot/thingmodel/template'

defineOptions({ name: 'IotThingModelTemplateForm' })

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formRef = ref<FormInstance>()
const categoryList = ref<TemplateApi.ThingModelTemplateCategoryVO[]>([])

const formData = reactive({
  id: undefined as number | undefined,
  name: '',
  code: '',
  categoryId: undefined as number | undefined,
  description: '',
  icon: '',
  tsl: ''
})

const formRules: FormRules = {
  name: [
    { required: true, message: '模板名称不能为空', trigger: 'blur' },
    { max: 64, message: '模板名称长度不能超过64个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '模板编码不能为空', trigger: 'blur' },
    { max: 32, message: '模板编码长度不能超过32个字符', trigger: 'blur' }
  ],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  tsl: [{ required: true, message: 'TSL定义不能为空', trigger: 'blur' }]
}

const emit = defineEmits(['success'])

/** 加载分类列表 */
const loadCategories = async () => {
  try {
    categoryList.value = await TemplateApi.getCategoryList()
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

/** 打开弹窗 */
const open = async (type: 'create' | 'update', id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '创建模板' : '编辑模板'
  resetForm()

  if (id) {
    formLoading.value = true
    try {
      const data = await TemplateApi.getTemplate(id)
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

  // 校验TSL JSON格式
  try {
    JSON.parse(formData.tsl)
  } catch {
    ElMessage.error('TSL格式无效，请输入有效的JSON')
    return
  }

  formLoading.value = true
  try {
    if (formData.id) {
      await TemplateApi.updateTemplate(formData as any, formData.id)
      ElMessage.success('更新成功')
    } else {
      await TemplateApi.createTemplate(formData as any)
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
  formData.name = ''
  formData.code = ''
  formData.categoryId = undefined
  formData.description = ''
  formData.icon = ''
  formData.tsl = ''
  formRef.value?.resetFields()
}

onMounted(() => {
  loadCategories()
})

defineExpose({ open })
</script>
