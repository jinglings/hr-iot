<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="1000px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="110px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="计量点名称" prop="meterName">
            <el-input v-model="formData.meterName" placeholder="请输入计量点名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="计量点编码" prop="meterCode">
            <el-input v-model="formData.meterCode" placeholder="请输入计量点编码" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="能源类型" prop="energyTypeId">
            <el-select v-model="formData.energyTypeId" placeholder="请选择能源类型" style="width: 100%">
              <el-option
                v-for="type in energyTypeList"
                :key="type.id"
                :label="type.name"
                :value="type.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="是否虚拟表" prop="isVirtual">
            <el-switch v-model="formData.isVirtual" @change="handleVirtualChange" />
            <span style="margin-left: 10px; color: #909399; font-size: 12px">
              虚拟表通过公式计算其他计量点的值
            </span>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 非虚拟表：设备绑定 -->
      <el-row :gutter="20" v-if="!formData.isVirtual">
        <el-col :span="12">
          <el-form-item label="绑定设备" prop="deviceId">
            <el-select
              v-model="formData.deviceId"
              placeholder="请选择设备"
              filterable
              remote
              :remote-method="searchDevices"
              :loading="deviceLoading"
              @change="handleDeviceChange"
              style="width: 100%"
            >
              <el-option
                v-for="device in deviceList"
                :key="device.id"
                :label="device.deviceName"
                :value="device.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="设备属性" prop="deviceProperty">
            <el-select v-model="formData.deviceProperty" placeholder="请选择设备属性" style="width: 100%">
              <el-option
                v-for="prop in deviceProperties"
                :key="prop.identifier"
                :label="prop.name"
                :value="prop.identifier"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 虚拟表：计算公式 -->
      <el-row :gutter="20" v-if="formData.isVirtual">
        <el-col :span="24">
          <el-form-item label="计算公式" prop="formula">
            <el-input
              v-model="formData.formula"
              type="textarea"
              placeholder="请输入计算公式，例如：meter1 + meter2 - meter3"
              :rows="2"
            />
            <div style="color: #909399; font-size: 12px; margin-top: 5px">
              提示：使用计量点编码进行计算，支持 +、-、*、/ 运算符
            </div>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 空间位置选择 -->
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
                :label="building.buildingName"
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
                :label="area.areaName"
                :value="area.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="所属楼层" prop="floorId">
            <el-select v-model="formData.floorId" placeholder="请选择楼层" style="width: 100%" @change="handleFloorChange">
              <el-option
                v-for="floor in floorList"
                :key="floor.id"
                :label="floor.floorName"
                :value="floor.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属房间" prop="roomId">
            <el-select v-model="formData.roomId" placeholder="请选择房间" style="width: 100%">
              <el-option
                v-for="room in roomList"
                :key="room.id"
                :label="room.roomName"
                :value="room.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 计量点层级 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="计量点级别" prop="meterLevel">
            <el-select v-model="formData.meterLevel" placeholder="请选择计量点级别" style="width: 100%">
              <el-option label="一级表" :value="1" />
              <el-option label="二级表" :value="2" />
              <el-option label="三级表" :value="3" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="父级计量点" prop="parentId">
            <el-select
              v-model="formData.parentId"
              placeholder="请选择父级计量点"
              filterable
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="meter in parentMeterList"
                :key="meter.id"
                :label="meter.name + ' (' + meter.code + ')'"
                :value="meter.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="计量倍率" prop="coefficient">
            <el-input-number v-model="formData.coefficient" :min="0.01" :step="0.1" :precision="2" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="计量点状态" prop="status">
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

      <el-form-item label="计量点描述" prop="description">
        <el-input type="textarea" v-model="formData.description" placeholder="请输入计量点描述" :rows="3" />
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
import { IotEnergyMeterVO, IotEnergyMeterSimpleVO, getIotEnergyMeter, createIotEnergyMeter, updateIotEnergyMeter, getIotEnergyMeterSimpleList } from '@/api/iot/energy/meter'
import { IotEnergyTypeSimpleVO, getIotEnergyTypeSimpleList } from '@/api/iot/energy/energyType'
import { IotEnergyBuildingSimpleVO, getIotEnergyBuildingSimpleList } from '@/api/iot/energy/building'
import { IotEnergyAreaVO, getIotEnergyAreaListByBuildingId } from '@/api/iot/energy/area'
import { IotEnergyFloorVO, getIotEnergyFloorListByBuildingId, getIotEnergyFloorListByAreaId } from '@/api/iot/energy/floor'
import { IotEnergyRoomVO, getIotEnergyRoomListByFloorId } from '@/api/iot/energy/room'
import { DeviceApi, DeviceVO } from '@/api/iot/device/device'
import { CommonStatusEnum } from '@/utils/constants'

/** IoT 能源计量点 表单 */
defineOptions({ name: 'MeterForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中
const formType = ref('') // 表单的类型
const deviceLoading = ref(false) // 设备加载中
const energyTypeList = ref<IotEnergyTypeSimpleVO[]>([]) // 能源类型列表
const deviceList = ref<DeviceVO[]>([]) // 设备列表
const deviceProperties = ref<{ identifier: string; name: string }[]>([]) // 设备属性列表
const buildingList = ref<IotEnergyBuildingSimpleVO[]>([]) // 建筑列表
const areaList = ref<IotEnergyAreaVO[]>([]) // 区域列表
const floorList = ref<IotEnergyFloorVO[]>([]) // 楼层列表
const roomList = ref<IotEnergyRoomVO[]>([]) // 房间列表
const parentMeterList = ref<IotEnergyMeterSimpleVO[]>([]) // 父级计量点列表
const formData = ref({
  id: undefined,
  meterName: undefined,
  meterCode: undefined,
  energyTypeId: undefined,
  deviceId: undefined,
  deviceProperty: undefined,
  buildingId: undefined,
  areaId: undefined,
  floorId: undefined,
  roomId: undefined,
  meterLevel: 1,
  parentId: undefined,
  isVirtual: false,
  formula: undefined,
  coefficient: 1,
  description: undefined,
  status: CommonStatusEnum.ENABLE
})
const formRules = reactive({
  meterName: [{ required: true, message: '计量点名称不能为空', trigger: 'blur' }],
  meterCode: [{ required: true, message: '计量点编码不能为空', trigger: 'blur' }],
  energyTypeId: [{ required: true, message: '能源类型不能为空', trigger: 'change' }],
  deviceId: [{ required: true, message: '绑定设备不能为空', trigger: 'change' }],
  deviceProperty: [{ required: true, message: '设备属性不能为空', trigger: 'change' }],
  buildingId: [{ required: true, message: '所属建筑不能为空', trigger: 'change' }],
  meterLevel: [{ required: true, message: '计量点级别不能为空', trigger: 'change' }],
  coefficient: [{ required: true, message: '计量倍率不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '计量点状态不能为空', trigger: 'blur' }],
  formula: [{ required: true, message: '计算公式不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

/** 虚拟表状态变化 */
const handleVirtualChange = (val: boolean) => {
  if (val) {
    // 切换为虚拟表，清空设备相关字段，并移除必填验证
    formData.value.deviceId = undefined
    formData.value.deviceProperty = undefined
    formRules.deviceId[0].required = false
    formRules.deviceProperty[0].required = false
    formRules.formula[0].required = true
  } else {
    // 切换为物理表，清空公式，并添加必填验证
    formData.value.formula = undefined
    formRules.deviceId[0].required = true
    formRules.deviceProperty[0].required = true
    formRules.formula[0].required = false
  }
}

/** 搜索设备 */
const searchDevices = async (query: string) => {
  if (query) {
    deviceLoading.value = true
    try {
      const data = await DeviceApi.getDevicePage({ deviceName: query, pageNo: 1, pageSize: 20 })
      deviceList.value = data.list
    } finally {
      deviceLoading.value = false
    }
  }
}

/** 设备变化时，加载设备属性 */
const handleDeviceChange = async (deviceId: number, preserveProperty: boolean = false) => {
  // 如果不是保留模式，才清空设备属性
  if (!preserveProperty) {
    formData.value.deviceProperty = undefined
  }
  deviceProperties.value = []

  if (deviceId) {
    try {
      const device = await DeviceApi.getDevice(deviceId)
      // 获取设备的物模型属性
      if (device.productId) {
        // 这里假设有获取产品物模型的接口，实际需要根据项目调整
        // 暂时使用模拟数据
        deviceProperties.value = [
          { identifier: 'power', name: '功率' },
          { identifier: 'energy', name: '电能' },
          { identifier: 'voltage', name: '电压' },
          { identifier: 'current', name: '电流' }
        ]
      }
    } catch (error) {
      console.error('获取设备属性失败:', error)
    }
  }
}

/** 查询能源类型列表 */
const getEnergyTypeList = async () => {
  energyTypeList.value = await getIotEnergyTypeSimpleList()
}

/** 查询建筑列表 */
const getBuildingList = async () => {
  buildingList.value = await getIotEnergyBuildingSimpleList()
}

/** 查询父级计量点列表 */
const getParentMeterList = async () => {
  parentMeterList.value = await getIotEnergyMeterSimpleList()
}

/** 建筑变化时，加载区域列表 */
const handleBuildingChange = async (buildingId: number) => {
  formData.value.areaId = undefined
  formData.value.floorId = undefined
  formData.value.roomId = undefined
  areaList.value = []
  floorList.value = []
  roomList.value = []

  if (buildingId) {
    areaList.value = await getIotEnergyAreaListByBuildingId(buildingId)
    // 同时加载该建筑下的所有楼层
    const allFloors = await getIotEnergyFloorListByBuildingId(buildingId)
    floorList.value = allFloors
  }
}

/** 区域变化时，加载楼层列表 */
const handleAreaChange = async (areaId: number) => {
  formData.value.floorId = undefined
  formData.value.roomId = undefined
  roomList.value = []

  if (areaId) {
    floorList.value = await getIotEnergyFloorListByAreaId(areaId)
  } else if (formData.value.buildingId) {
    const allFloors = await getIotEnergyFloorListByBuildingId(formData.value.buildingId)
    floorList.value = allFloors
  } else {
    floorList.value = []
  }
}

/** 楼层变化时，加载房间列表 */
const handleFloorChange = async (floorId: number) => {
  formData.value.roomId = undefined

  if (floorId) {
    roomList.value = await getIotEnergyRoomListByFloorId(floorId)
  } else {
    roomList.value = []
  }
}

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 加载下拉列表数据
  await Promise.all([
    getEnergyTypeList(),
    getBuildingList(),
    getParentMeterList()
  ])
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      const data = await getIotEnergyMeter(id)
      // 确保 coefficient 有默认值
      if (data.coefficient === undefined || data.coefficient === null) {
        data.coefficient = 1
      }
      formData.value = data
      // 加载对应的级联数据
      if (formData.value.buildingId) {
        areaList.value = await getIotEnergyAreaListByBuildingId(formData.value.buildingId)
        if (formData.value.areaId) {
          floorList.value = await getIotEnergyFloorListByAreaId(formData.value.areaId)
        } else {
          floorList.value = await getIotEnergyFloorListByBuildingId(formData.value.buildingId)
        }
        if (formData.value.floorId) {
          roomList.value = await getIotEnergyRoomListByFloorId(formData.value.floorId)
        }
      }
      // 加载设备信息
      if (formData.value.deviceId) {
        const deviceData = await DeviceApi.getDevicePage({ id: formData.value.deviceId, pageNo: 1, pageSize: 1 })
        if (deviceData.list && deviceData.list.length > 0) {
          deviceList.value = deviceData.list
        }
        // 使用 preserveProperty=true 保留已有的 deviceProperty 值
        await handleDeviceChange(formData.value.deviceId, true)
      }
      // 处理虚拟表规则
      handleVirtualChange(formData.value.isVirtual || false)
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
    const data = formData.value as unknown as IotEnergyMeterVO
    if (formType.value === 'create') {
      await createIotEnergyMeter(data)
      message.success(t('common.createSuccess'))
    } else {
      await updateIotEnergyMeter(data)
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
    meterName: undefined,
    meterCode: undefined,
    energyTypeId: undefined,
    deviceId: undefined,
    deviceProperty: undefined,
    buildingId: undefined,
    areaId: undefined,
    floorId: undefined,
    roomId: undefined,
    meterLevel: 1,
    parentId: undefined,
    isVirtual: false,
    formula: undefined,
    coefficient: 1,
    description: undefined,
    status: CommonStatusEnum.ENABLE
  }
  areaList.value = []
  floorList.value = []
  roomList.value = []
  deviceList.value = []
  deviceProperties.value = []
  // 重置验证规则
  formRules.deviceId[0].required = true
  formRules.deviceProperty[0].required = true
  formRules.formula[0].required = false
  formRef.value?.resetFields()
}
</script>
