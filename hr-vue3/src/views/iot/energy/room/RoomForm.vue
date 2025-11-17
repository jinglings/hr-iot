<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="700px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="12">
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
                :label="building.name"
                :value="building.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属区域" prop="areaId">
            <el-select
              v-model="formData.areaId"
              placeholder="请选择区域"
              style="width: 100%"
              @change="handleAreaChange"
            >
              <el-option
                v-for="area in areaList"
                :key="area.id"
                :label="area.name"
                :value="area.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="所属楼层" prop="floorId">
        <el-select v-model="formData.floorId" placeholder="请选择楼层" style="width: 100%">
          <el-option
            v-for="floor in floorList"
            :key="floor.id"
            :label="floor.name"
            :value="floor.id"
          />
        </el-select>
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="房间名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入房间名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="房间编码" prop="code">
            <el-input v-model="formData.code" placeholder="请输入房间编码" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="房间类型" prop="type">
            <el-select v-model="formData.type" placeholder="请选择房间类型" style="width: 100%">
              <el-option label="办公室" value="office" />
              <el-option label="会议室" value="meeting" />
              <el-option label="仓库" value="warehouse" />
              <el-option label="实验室" value="lab" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="房间面积" prop="area">
            <el-input v-model="formData.area" placeholder="请输入房间面积" type="number">
              <template #append>平方米</template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="房间状态" prop="status">
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

      <el-form-item label="房间描述" prop="description">
        <el-input type="textarea" v-model="formData.description" placeholder="请输入房间描述" :rows="3" />
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
import { IotEnergyRoomVO, getIotEnergyRoom, createIotEnergyRoom, updateIotEnergyRoom } from '@/api/iot/energy/room'
import { getIotEnergyBuildingSimpleList } from '@/api/iot/energy/building'
import { getIotEnergyAreaListByBuildingId } from '@/api/iot/energy/area'
import { getIotEnergyFloorListByBuildingId, getIotEnergyFloorListByAreaId } from '@/api/iot/energy/floor'
import { CommonStatusEnum } from '@/utils/constants'

/** IoT 能源房间 表单 */
defineOptions({ name: 'RoomForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中
const formType = ref('') // 表单的类型
const buildingList = ref([]) // 建筑列表
const areaList = ref([]) // 区域列表
const floorList = ref([]) // 楼层列表
const formData = ref({
  id: undefined,
  buildingId: undefined,
  areaId: undefined,
  floorId: undefined,
  name: undefined,
  code: undefined,
  type: 'office',
  area: undefined,
  description: undefined,
  sort: 0,
  status: CommonStatusEnum.ENABLE
})
const formRules = reactive({
  buildingId: [{ required: true, message: '所属建筑不能为空', trigger: 'change' }],
  areaId: [{ required: true, message: '所属区域不能为空', trigger: 'change' }],
  floorId: [{ required: true, message: '所属楼层不能为空', trigger: 'change' }],
  name: [{ required: true, message: '房间名称不能为空', trigger: 'blur' }],
  code: [{ required: true, message: '房间编码不能为空', trigger: 'blur' }],
  type: [{ required: true, message: '房间类型不能为空', trigger: 'change' }],
  status: [{ required: true, message: '房间状态不能为空', trigger: 'blur' }],
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
  formData.value.floorId = undefined
  areaList.value = []
  floorList.value = []

  if (buildingId) {
    areaList.value = await getIotEnergyAreaListByBuildingId(buildingId)
    // 同时加载该建筑下的所有楼层（用于未分区域的楼层）
    const allFloors = await getIotEnergyFloorListByBuildingId(buildingId)
    floorList.value = allFloors
  }
}

/** 区域变化时，加载楼层列表 */
const handleAreaChange = async (areaId: number) => {
  formData.value.floorId = undefined

  if (areaId) {
    floorList.value = await getIotEnergyFloorListByAreaId(areaId)
  } else if (formData.value.buildingId) {
    // 如果取消选择区域，则显示该建筑下的所有楼层
    const allFloors = await getIotEnergyFloorListByBuildingId(formData.value.buildingId)
    floorList.value = allFloors
  } else {
    floorList.value = []
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
      formData.value = await getIotEnergyRoom(id)
      // 加载对应的区域列表和楼层列表
      if (formData.value.buildingId) {
        areaList.value = await getIotEnergyAreaListByBuildingId(formData.value.buildingId)
        if (formData.value.areaId) {
          floorList.value = await getIotEnergyFloorListByAreaId(formData.value.areaId)
        } else {
          floorList.value = await getIotEnergyFloorListByBuildingId(formData.value.buildingId)
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
  // 校验表单
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as IotEnergyRoomVO
    if (formType.value === 'create') {
      await createIotEnergyRoom(data)
      message.success(t('common.createSuccess'))
    } else {
      await updateIotEnergyRoom(data)
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
    areaId: undefined,
    floorId: undefined,
    name: undefined,
    code: undefined,
    type: 'office',
    area: undefined,
    description: undefined,
    sort: 0,
    status: CommonStatusEnum.ENABLE
  }
  areaList.value = []
  floorList.value = []
  formRef.value?.resetFields()
}
</script>
