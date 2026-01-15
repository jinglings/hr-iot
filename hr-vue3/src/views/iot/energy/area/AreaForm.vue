<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="所属建筑" prop="buildingId">
        <el-select v-model="formData.buildingId" placeholder="请选择建筑" style="width: 100%">
          <el-option
            v-for="building in buildingList"
            :key="building.id"
            :label="building.buildingName"
            :value="building.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="区域名称" prop="areaName">
        <el-input v-model="formData.areaName" placeholder="请输入区域名称" />
      </el-form-item>

      <el-form-item label="区域编码" prop="areaCode">
        <el-input v-model="formData.areaCode" placeholder="请输入区域编码" />
      </el-form-item>

      <el-form-item label="区域状态" prop="status">
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

      <el-form-item label="区域描述" prop="description">
        <el-input type="textarea" v-model="formData.description" placeholder="请输入区域描述" :rows="3" />
      </el-form-item>

      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="formData.sort" controls-position="right" :min="0" />
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
import { IotEnergyAreaVO, getIotEnergyArea, createIotEnergyArea, updateIotEnergyArea } from '@/api/iot/energy/area'
import { getIotEnergyBuildingSimpleList } from '@/api/iot/energy/building'
import { CommonStatusEnum } from '@/utils/constants'

/** IoT 能源区域 表单 */
defineOptions({ name: 'AreaForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const buildingList = ref([]) // 建筑列表
const formData = ref({
  id: undefined,
  buildingId: undefined,
  areaName: undefined,
  areaCode: undefined,
  description: undefined,
  sort: 0,
  status: CommonStatusEnum.ENABLE
})
const formRules = reactive({
  buildingId: [{ required: true, message: '所属建筑不能为空', trigger: 'change' }],
  areaName: [{ required: true, message: '区域名称不能为空', trigger: 'blur' }],
  areaCode: [{ required: true, message: '区域编码不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '区域状态不能为空', trigger: 'blur' }],
  sort: [{ required: true, message: '排序不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

/** 查询建筑列表 */
const getBuildingList = async () => {
  buildingList.value = await getIotEnergyBuildingSimpleList()
}

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 加载建筑列表
  await getBuildingList()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await getIotEnergyArea(id)
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
    const data = formData.value as unknown as IotEnergyAreaVO
    if (formType.value === 'create') {
      await createIotEnergyArea(data)
      message.success(t('common.createSuccess'))
    } else {
      await updateIotEnergyArea(data)
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
    buildingId: undefined,
    areaName: undefined,
    areaCode: undefined,
    description: undefined,
    sort: 0,
    status: CommonStatusEnum.ENABLE
  }
  formRef.value?.resetFields()
}
</script>
