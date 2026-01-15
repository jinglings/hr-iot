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
        <el-select
          v-model="formData.buildingId"
          placeholder="请选择建筑"
          style="width: 100%"
          @change="handleBuildingChange"
        >
          <el-option
            v-for="building in buildingList"
            :key="building.id"
            :label="building.buildingName"
            :value="building.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="所属区域" prop="areaId">
        <el-select v-model="formData.areaId" placeholder="请选择区域" style="width: 100%">
          <el-option
            v-for="area in areaList"
            :key="area.id"
            :label="area.areaName"
            :value="area.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="楼层名称" prop="floorName">
        <el-input v-model="formData.floorName" placeholder="请输入楼层名称" />
      </el-form-item>

      <el-form-item label="楼层编码" prop="floorCode">
        <el-input v-model="formData.floorCode" placeholder="请输入楼层编码" />
      </el-form-item>

      <el-form-item label="楼层号" prop="floorNumber">
        <el-input-number v-model="formData.floorNumber" controls-position="right" placeholder="负数表示地下楼层" />
      </el-form-item>

      <el-form-item label="楼层面积" prop="area">
        <el-input v-model="formData.area" placeholder="请输入楼层面积" type="number">
          <template #append>平方米</template>
        </el-input>
      </el-form-item>

      <el-form-item label="楼层状态" prop="status">
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

      <el-form-item label="楼层描述" prop="description">
        <el-input type="textarea" v-model="formData.description" placeholder="请输入楼层描述" :rows="3" />
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
import { IotEnergyFloorVO, getIotEnergyFloor, createIotEnergyFloor, updateIotEnergyFloor } from '@/api/iot/energy/floor'
import { getIotEnergyBuildingSimpleList } from '@/api/iot/energy/building'
import { getIotEnergyAreaListByBuildingId } from '@/api/iot/energy/area'
import { CommonStatusEnum } from '@/utils/constants'

/** IoT 能源楼层 表单 */
defineOptions({ name: 'FloorForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中
const formType = ref('') // 表单的类型
const buildingList = ref([]) // 建筑列表
const areaList = ref([]) // 区域列表
const formData = ref({
  id: undefined,
  buildingId: undefined,
  areaId: undefined,
  floorName: undefined,
  floorCode: undefined,
  floorNumber: 1,
  area: undefined,
  description: undefined,
  sort: 0,
  status: CommonStatusEnum.ENABLE
})
const formRules = reactive({
  buildingId: [{ required: true, message: '所属建筑不能为空', trigger: 'change' }],
  areaId: [{ required: true, message: '所属区域不能为空', trigger: 'change' }],
  floorName: [{ required: true, message: '楼层名称不能为空', trigger: 'blur' }],
  floorCode: [{ required: true, message: '楼层编码不能为空', trigger: 'blur' }],
  floorNumber: [{ required: true, message: '楼层号不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '楼层状态不能为空', trigger: 'blur' }],
  sort: [{ required: true, message: '排序不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

/** 查询建筑列表 */
const getBuildingList = async () => {
  buildingList.value = await getIotEnergyBuildingSimpleList()
}

/** 建筑变化时，加载区域列表 */
const handleBuildingChange = async (buildingId: number) => {
  formData.value.areaId = undefined
  if (buildingId) {
    areaList.value = await getIotEnergyAreaListByBuildingId(buildingId)
  } else {
    areaList.value = []
  }
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
      formData.value = await getIotEnergyFloor(id)
      // 加载对应的区域列表(保存原始的areaId,避免被清空)
      if (formData.value.buildingId) {
        const savedAreaId = formData.value.areaId
        areaList.value = await getIotEnergyAreaListByBuildingId(formData.value.buildingId)
        formData.value.areaId = savedAreaId
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
  await formRef.value.validate()
  formLoading.value = true
  try {
    const data = formData.value as unknown as IotEnergyFloorVO
    if (formType.value === 'create') {
      await createIotEnergyFloor(data)
      message.success(t('common.createSuccess'))
    } else {
      await updateIotEnergyFloor(data)
      message.success(t('common.updateSuccess'))
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
    buildingId: undefined,
    areaId: undefined,
    floorName: undefined,
    floorCode: undefined,
    floorNumber: 1,
    area: undefined,
    description: undefined,
    sort: 0,
    status: CommonStatusEnum.ENABLE
  }
  areaList.value = []
  formRef.value?.resetFields()
}
</script>
