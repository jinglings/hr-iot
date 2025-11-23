<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="800px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="建筑名称" prop="buildingName">
            <el-input v-model="formData.buildingName" placeholder="请输入建筑名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="建筑编码" prop="buildingCode">
            <el-input v-model="formData.buildingCode" placeholder="请输入建筑编码" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="建筑类型" prop="buildingType">
            <el-select v-model="formData.buildingType" placeholder="请选择建筑类型" style="width: 100%">
              <el-option label="办公楼" value="office" />
              <el-option label="厂房" value="factory" />
              <el-option label="仓库" value="warehouse" />
              <el-option label="宿舍" value="dormitory" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="建筑面积" prop="area">
            <el-input v-model="formData.area" placeholder="请输入建筑面积" type="number">
              <template #append>平方米</template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="楼层数" prop="floors">
            <el-input v-model="formData.floors" placeholder="请输入楼层数" type="number" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="建筑状态" prop="status">
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

      <el-form-item label="建筑地址" prop="address">
        <el-input v-model="formData.address" placeholder="请输入建筑地址" />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="纬度" prop="latitude">
            <el-input v-model="formData.latitude" placeholder="请输入纬度" type="number" step="0.000001" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="经度" prop="longitude">
            <el-input v-model="formData.longitude" placeholder="请输入经度" type="number" step="0.000001" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="建筑图片" prop="image">
        <UploadImg v-model="formData.image" :limit="1" />
      </el-form-item>

      <el-form-item label="建筑描述" prop="description">
        <el-input type="textarea" v-model="formData.description" placeholder="请输入建筑描述" :rows="3" />
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
import { IotEnergyBuildingVO, getIotEnergyBuilding, createIotEnergyBuilding, updateIotEnergyBuilding } from '@/api/iot/energy/building'
import { CommonStatusEnum } from '@/utils/constants'

/** IoT 能源建筑 表单 */
defineOptions({ name: 'BuildingForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  buildingName: undefined,
  buildingCode: undefined,
  buildingType: 'office',
  area: undefined,
  address: undefined,
  latitude: undefined,
  longitude: undefined,
  floors: undefined,
  image: undefined,
  description: undefined,
  sort: 0,
  status: CommonStatusEnum.ENABLE
})
const formRules = reactive({
  buildingName: [{ required: true, message: '建筑名称不能为空', trigger: 'blur' }],
  buildingCode: [{ required: true, message: '建筑编码不能为空', trigger: 'blur' }],
  buildingType: [{ required: true, message: '建筑类型不能为空', trigger: 'change' }],
  area: [{ required: true, message: '建筑面积不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '建筑状态不能为空', trigger: 'blur' }],
  sort: [{ required: true, message: '排序不能为空', trigger: 'blur' }]
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
      formData.value = await getIotEnergyBuilding(id)
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
    const data = formData.value as unknown as IotEnergyBuildingVO
    if (formType.value === 'create') {
      await createIotEnergyBuilding(data)
      message.success(t('common.createSuccess'))
    } else {
      await updateIotEnergyBuilding(data)
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
    buildingName: undefined,
    buildingCode: undefined,
    buildingType: 'office',
    area: undefined,
    address: undefined,
    latitude: undefined,
    longitude: undefined,
    floors: undefined,
    image: undefined,
    description: undefined,
    sort: 0,
    status: CommonStatusEnum.ENABLE
  }
  formRef.value?.resetFields()
}
</script>
