<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="800px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="110px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="能源类型名称" prop="energyName">
            <el-input v-model="formData.energyName" placeholder="请输入能源类型名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="能源类型编码" prop="energyCode">
            <el-input v-model="formData.energyCode" placeholder="请输入能源类型编码" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="父级能源类型" prop="parentId">
            <el-tree-select
              v-model="formData.parentId"
              :data="energyTypeTree"
              :props="defaultProps"
              check-strictly
              default-expand-all
              placeholder="请选择父级能源类型"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="计量单位" prop="unit">
            <el-select v-model="formData.unit" placeholder="请选择计量单位" style="width: 100%">
              <el-option label="千瓦时 (kWh)" value="kWh" />
              <el-option label="立方米 (m³)" value="m³" />
              <el-option label="吨 (t)" value="t" />
              <el-option label="升 (L)" value="L" />
              <el-option label="标准立方米 (Nm³)" value="Nm³" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="折标煤系数" prop="coalConversionFactor">
            <el-input v-model="formData.coalConversionFactor" placeholder="请输入折标煤系数" type="number" step="0.0001">
              <template #append>kgce/单位</template>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="碳排放系数" prop="carbonEmissionFactor">
            <el-input v-model="formData.carbonEmissionFactor" placeholder="请输入碳排放系数" type="number" step="0.0001">
              <template #append>kgCO₂/单位</template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="图标" prop="icon">
            <el-input v-model="formData.icon" placeholder="请输入图标类名，如：ep:sunny">
              <template #prepend>
                <Icon v-if="formData.icon" :icon="formData.icon" />
              </template>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="颜色" prop="color">
            <el-color-picker v-model="formData.color" />
            <el-input v-model="formData.color" placeholder="请选择颜色或输入颜色值" style="width: calc(100% - 50px); margin-left: 10px" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="类型状态" prop="status">
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
        <el-col :span="12">
          <el-form-item label="排序" prop="sort">
            <el-input-number v-model="formData.sort" controls-position="right" :min="0" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="类型描述" prop="description">
        <el-input type="textarea" v-model="formData.description" placeholder="请输入能源类型描述" :rows="3" />
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
import { IotEnergyTypeVO, getIotEnergyType, createIotEnergyType, updateIotEnergyType, getIotEnergyTypePage } from '@/api/iot/energy/energyType'
import { CommonStatusEnum } from '@/utils/constants'
import { defaultProps, handleTree } from '@/utils/tree'

/** IoT 能源类型 表单 */
defineOptions({ name: 'EnergyTypeForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中
const formType = ref('') // 表单的类型
const energyTypeTree = ref([]) // 能源类型树
const formData = ref({
  id: undefined,
  energyName: undefined,
  energyCode: undefined,
  parentId: undefined,
  unit: 'kWh',
  coalConversionFactor: undefined,
  carbonEmissionFactor: undefined,
  icon: undefined,
  color: '#409EFF',
  description: undefined,
  sort: 0,
  status: CommonStatusEnum.ENABLE
})
const formRules = reactive({
  energyName: [{ required: true, message: '能源类型名称不能为空', trigger: 'blur' }],
  energyCode: [{ required: true, message: '能源类型编码不能为空', trigger: 'blur' }],
  parentId: [{ required: true, message: '父级能源类型不能为空', trigger: 'change' }],
  unit: [{ required: true, message: '计量单位不能为空', trigger: 'change' }],
  status: [{ required: true, message: '类型状态不能为空', trigger: 'blur' }],
  sort: [{ required: true, message: '排序不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

/** 获取能源类型树 */
const getEnergyTypeTree = async () => {
  energyTypeTree.value = []
  const data = await getIotEnergyTypePage({ pageNo: 1, pageSize: 100 })
  const root: Tree = { id: 0, name: '顶级能源类型', children: [] }
  root.children = handleTree(data.list, 'id', 'parentId')
  energyTypeTree.value.push(root)
}

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 加载能源类型树
  await getEnergyTypeTree()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await getIotEnergyType(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件
const submitForm = async () => {
  // 校验表单
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as IotEnergyTypeVO
    if (formType.value === 'create') {
      await createIotEnergyType(data)
      message.success(t('common.createSuccess'))
    } else {
      await updateIotEnergyType(data)
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
    energyName: undefined,
    energyCode: undefined,
    parentId: undefined,
    unit: 'kWh',
    coalConversionFactor: undefined,
    carbonEmissionFactor: undefined,
    icon: undefined,
    color: '#409EFF',
    description: undefined,
    sort: 0,
    status: CommonStatusEnum.ENABLE
  }
  formRef.value?.resetFields()
}
</script>
