<!-- BACnet 配置 -->
<template>
  <div v-loading="loading">
    <!-- 设备配置卡片 -->
    <el-card class="mb-4">
      <template #header>
        <div class="flex justify-between items-center">
          <span class="font-bold">BACnet 设备配置</span>
          <el-button
            v-if="deviceConfig && !editingConfig"
            type="primary"
            size="small"
            @click="handleEditConfig"
          >
            编辑配置
          </el-button>
        </div>
      </template>

      <!-- 配置表单 -->
      <el-form
        v-if="editingConfig || !deviceConfig"
        ref="configFormRef"
        :model="configForm"
        :rules="configRules"
        label-width="140px"
      >
        <el-form-item label="BACnet 实例号" prop="instanceNumber">
          <el-input-number
            v-model="configForm.instanceNumber"
            :min="0"
            :max="4194303"
            :disabled="!!deviceConfig"
            style="width: 100%"
          />
          <el-text type="info" size="small">设备的 BACnet 实例号（0-4194303）</el-text>
        </el-form-item>

        <el-form-item label="IP 地址" prop="ipAddress">
          <el-input v-model="configForm.ipAddress" placeholder="192.168.1.10" />
          <el-text type="info" size="small">BACnet 设备的 IP 地址</el-text>
        </el-form-item>

        <el-form-item label="启用轮询" prop="pollingEnabled">
          <el-switch v-model="configForm.pollingEnabled" />
        </el-form-item>

        <el-form-item label="轮询间隔(ms)" prop="pollingInterval" v-if="configForm.pollingEnabled">
          <el-input-number
            v-model="configForm.pollingInterval"
            :min="1000"
            :max="3600000"
            :step="1000"
            style="width: 100%"
          />
          <el-text type="info" size="small">数据采集轮询间隔（1000-3600000毫秒）</el-text>
        </el-form-item>

        <el-form-item label="配置描述" prop="description">
          <el-input
            v-model="configForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入配置描述"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSaveConfig" :loading="saveLoading">
            保存配置
          </el-button>
          <el-button v-if="editingConfig" @click="handleCancelEdit">取消</el-button>
          <el-button v-if="deviceConfig" type="success" @click="handleTestConnection">
            测试连接
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 配置展示 -->
      <el-descriptions v-else :column="2" border>
        <el-descriptions-item label="BACnet 实例号">{{
          deviceConfig.instanceNumber
        }}</el-descriptions-item>
        <el-descriptions-item label="IP 地址">{{
          deviceConfig.ipAddress
        }}</el-descriptions-item>
        <el-descriptions-item label="启用轮询">
          <el-tag :type="deviceConfig.pollingEnabled ? 'success' : 'info'">
            {{ deviceConfig.pollingEnabled ? '是' : '否' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="轮询间隔" v-if="deviceConfig.pollingEnabled">
          {{ deviceConfig.pollingInterval }} ms
        </el-descriptions-item>
        <el-descriptions-item label="配置描述" :span="2">{{
          deviceConfig.description || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{
          formatDate(deviceConfig.createTime)
        }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{
          formatDate(deviceConfig.updateTime)
        }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 属性映射列表 -->
    <el-card>
      <template #header>
        <div class="flex justify-between items-center">
          <span class="font-bold">BACnet 属性映射配置</span>
          <el-button
            type="primary"
            size="small"
            @click="handleAddMapping"
            v-hasPermi="['iot:bacnet:mapping:create']"
            :disabled="!deviceConfig"
          >
            <Icon icon="ep:plus" class="mr-1" />
            添加映射
          </el-button>
        </div>
      </template>

      <el-alert
        v-if="!deviceConfig"
        title="请先配置 BACnet 设备信息"
        type="warning"
        show-icon
        class="mb-4"
      />

      <el-table v-else :data="mappingList" style="width: 100%">
        <el-table-column label="物模型属性" prop="identifier" width="150" />
        <el-table-column label="BACnet 对象类型" prop="objectType" width="150" />
        <el-table-column label="对象实例号" prop="objectInstance" width="120" />
        <el-table-column label="属性标识符" prop="propertyIdentifier" width="130" />
        <el-table-column label="数据类型" prop="dataType" width="100" />
        <el-table-column label="访问模式" width="90">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.accessMode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="启用轮询" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.pollingEnabled ? 'success' : 'info'" size="small">
              {{ scope.row.pollingEnabled ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="handleTestRead(scope.row)"
              v-hasPermi="['iot:bacnet:test']"
            >
              测试读取
            </el-button>
            <el-button
              link
              type="success"
              @click="handleTestWrite(scope.row)"
              v-hasPermi="['iot:bacnet:test']"
              :disabled="!scope.row.accessMode.includes('w')"
            >
              测试写入
            </el-button>
            <el-button
              link
              type="primary"
              @click="handleEditMapping(scope.row)"
              v-hasPermi="['iot:bacnet:mapping:update']"
            >
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDeleteMapping(scope.row.id)"
              v-hasPermi="['iot:bacnet:mapping:delete']"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>

  <!-- 属性映射编辑弹窗 -->
  <el-dialog
    v-model="mappingDialogVisible"
    :title="mappingFormMode === 'create' ? '添加属性映射' : '编辑属性映射'"
    width="700px"
  >
    <el-form ref="mappingFormRef" :model="mappingForm" :rules="mappingRules" label-width="140px">
      <el-form-item label="物模型属性" prop="identifier">
        <el-select
          v-model="mappingForm.identifier"
          placeholder="请选择物模型属性"
          filterable
          style="width: 100%"
        >
          <el-option
            v-for="prop in thingModelProperties"
            :key="prop.identifier"
            :label="`${prop.name} (${prop.identifier})`"
            :value="prop.identifier"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="BACnet 对象类型" prop="objectType">
        <el-select
          v-model="mappingForm.objectType"
          placeholder="请选择对象类型"
          style="width: 100%"
        >
          <el-option label="ANALOG_INPUT" value="ANALOG_INPUT" />
          <el-option label="ANALOG_OUTPUT" value="ANALOG_OUTPUT" />
          <el-option label="ANALOG_VALUE" value="ANALOG_VALUE" />
          <el-option label="BINARY_INPUT" value="BINARY_INPUT" />
          <el-option label="BINARY_OUTPUT" value="BINARY_OUTPUT" />
          <el-option label="BINARY_VALUE" value="BINARY_VALUE" />
          <el-option label="MULTI_STATE_INPUT" value="MULTI_STATE_INPUT" />
          <el-option label="MULTI_STATE_OUTPUT" value="MULTI_STATE_OUTPUT" />
        </el-select>
      </el-form-item>

      <el-form-item label="对象实例号" prop="objectInstance">
        <el-input-number
          v-model="mappingForm.objectInstance"
          :min="0"
          style="width: 100%"
          placeholder="请输入对象实例号"
        />
      </el-form-item>

      <el-form-item label="属性标识符" prop="propertyIdentifier">
        <el-input
          v-model="mappingForm.propertyIdentifier"
          placeholder="presentValue"
          style="width: 100%"
        />
        <el-text type="info" size="small">常用：presentValue, statusFlags, description</el-text>
      </el-form-item>

      <el-form-item label="数据类型" prop="dataType">
        <el-select v-model="mappingForm.dataType" placeholder="请选择数据类型" style="width: 100%">
          <el-option label="float" value="float" />
          <el-option label="int" value="int" />
          <el-option label="boolean" value="boolean" />
          <el-option label="string" value="string" />
        </el-select>
      </el-form-item>

      <el-form-item label="访问模式" prop="accessMode">
        <el-select v-model="mappingForm.accessMode" placeholder="请选择访问模式" style="width: 100%">
          <el-option label="只读 (r)" value="r" />
          <el-option label="只写 (w)" value="w" />
          <el-option label="读写 (rw)" value="rw" />
        </el-select>
      </el-form-item>

      <el-form-item label="启用轮询" prop="pollingEnabled">
        <el-switch v-model="mappingForm.pollingEnabled" />
      </el-form-item>

      <el-form-item label="单位转换公式" prop="unitConversion">
        <el-input
          v-model="mappingForm.unitConversion"
          placeholder="value * 0.1"
          style="width: 100%"
        />
        <el-text type="info" size="small">例如：value * 0.1 或 (value - 32) * 5 / 9</el-text>
      </el-form-item>

      <el-form-item label="值映射(JSON)" prop="valueMapping">
        <el-input
          v-model="mappingForm.valueMapping"
          type="textarea"
          :rows="3"
          placeholder='{"0":"关","1":"开"}'
        />
        <el-text type="info" size="small">用于枚举值映射，JSON 格式</el-text>
      </el-form-item>

      <el-form-item label="写入优先级" prop="priority" v-if="mappingForm.accessMode?.includes('w')">
        <el-input-number
          v-model="mappingForm.priority"
          :min="1"
          :max="16"
          placeholder="8"
          style="width: 100%"
        />
        <el-text type="info" size="small">BACnet 写入优先级（1-16，推荐 8）</el-text>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="mappingDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSaveMapping" :loading="mappingSaveLoading">
        保存
      </el-button>
    </template>
  </el-dialog>

  <!-- 测试写入弹窗 -->
  <el-dialog v-model="testWriteDialogVisible" title="测试写入属性" width="500px">
    <el-form label-width="100px">
      <el-form-item label="属性标识符">
        <el-text>{{ testWriteMapping?.identifier }}</el-text>
      </el-form-item>
      <el-form-item label="写入值">
        <el-input v-model="testWriteValue" placeholder="请输入写入值" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="testWriteDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitTestWrite" :loading="testWriteLoading">
        写入
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  BACnetApi,
  type BACnetDeviceConfigVO,
  type BACnetPropertyMappingVO
} from '@/api/iot/bacnet'
import type { DeviceVO } from '@/api/iot/device/device'
import { ThingModelApi } from '@/api/iot/thingmodel'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'DeviceDetailBACnetConfig' })

const props = defineProps<{
  device: DeviceVO
}>()

// ========== 设备配置相关 ==========
const loading = ref(false)
const saveLoading = ref(false)
const editingConfig = ref(false)
const deviceConfig = ref<BACnetDeviceConfigVO | null>(null)

const configForm = reactive<Partial<BACnetDeviceConfigVO>>({
  deviceId: 0,
  instanceNumber: 0,
  ipAddress: '',
  pollingEnabled: true,
  pollingInterval: 5000,
  description: ''
})

const configRules = {
  instanceNumber: [{ required: true, message: '请输入 BACnet 实例号', trigger: 'blur' }],
  ipAddress: [{ required: true, message: '请输入 IP 地址', trigger: 'blur' }],
  pollingInterval: [{ required: true, message: '请输入轮询间隔', trigger: 'blur' }]
}

const configFormRef = ref()

// ========== 属性映射相关 ==========
const mappingList = ref<BACnetPropertyMappingVO[]>([])
const mappingDialogVisible = ref(false)
const mappingFormMode = ref<'create' | 'edit'>('create')
const mappingSaveLoading = ref(false)
const thingModelProperties = ref<any[]>([])

const mappingForm = reactive<Partial<BACnetPropertyMappingVO>>({
  deviceId: 0,
  identifier: '',
  objectType: '',
  objectInstance: 0,
  propertyIdentifier: 'presentValue',
  dataType: 'float',
  accessMode: 'r',
  pollingEnabled: true,
  unitConversion: '',
  valueMapping: '',
  priority: 8
})

const mappingRules = {
  identifier: [{ required: true, message: '请选择物模型属性', trigger: 'change' }],
  objectType: [{ required: true, message: '请选择对象类型', trigger: 'change' }],
  objectInstance: [{ required: true, message: '请输入对象实例号', trigger: 'blur' }],
  propertyIdentifier: [{ required: true, message: '请输入属性标识符', trigger: 'blur' }],
  dataType: [{ required: true, message: '请选择数据类型', trigger: 'change' }],
  accessMode: [{ required: true, message: '请选择访问模式', trigger: 'change' }]
}

const mappingFormRef = ref()

// ========== 测试相关 ==========
const testWriteDialogVisible = ref(false)
const testWriteMapping = ref<BACnetPropertyMappingVO | null>(null)
const testWriteValue = ref('')
const testWriteLoading = ref(false)

// ========== 生命周期 ==========
onMounted(() => {
  loadDeviceConfig()
  loadThingModelProperties()
})

watch(
  () => props.device,
  () => {
    loadDeviceConfig()
  }
)

// ========== 方法 ==========

/** 加载设备配置 */
const loadDeviceConfig = async () => {
  if (!props.device?.id) return

  loading.value = true
  try {
    const data = await BACnetApi.getDeviceConfigByDeviceId(props.device.id)
    deviceConfig.value = data
    if (data) {
      await loadPropertyMappings()
    }
  } catch (error) {
    // 没有配置时会返回错误，忽略即可
    deviceConfig.value = null
  } finally {
    loading.value = false
  }
}

/** 加载物模型属性 */
const loadThingModelProperties = async () => {
  if (!props.device?.productId) return

  try {
    const data = await ThingModelApi.getThingModelList({
      productId: props.device.productId,
      type: 1 // 1表示属性类型
    })
    // 过滤出属性类型的物模型,并提取必要字段
    thingModelProperties.value =
      data?.filter((item: any) => item.type === 1).map((item: any) => ({
        identifier: item.identifier,
        name: item.name,
        dataType: item.dataType
      })) || []
  } catch (error) {
    console.error('加载物模型属性失败', error)
  }
}

/** 加载属性映射列表 */
const loadPropertyMappings = async () => {
  if (!props.device?.id) return

  try {
    const data = await BACnetApi.getPropertyMappingListByDevice(props.device.id)
    mappingList.value = data || []
  } catch (error) {
    console.error('加载属性映射失败', error)
  }
}

/** 编辑配置 */
const handleEditConfig = () => {
  if (deviceConfig.value) {
    Object.assign(configForm, deviceConfig.value)
  }
  editingConfig.value = true
}

/** 取消编辑 */
const handleCancelEdit = () => {
  editingConfig.value = false
}

/** 保存配置 */
const handleSaveConfig = async () => {
  const valid = await configFormRef.value?.validate()
  if (!valid) return

  saveLoading.value = true
  try {
    configForm.deviceId = props.device.id

    if (deviceConfig.value) {
      // 更新
      await BACnetApi.updateDeviceConfig(configForm as BACnetDeviceConfigVO)
      ElMessage.success('更新成功')
    } else {
      // 创建
      await BACnetApi.createDeviceConfig(configForm as BACnetDeviceConfigVO)
      ElMessage.success('创建成功')
    }

    editingConfig.value = false
    await loadDeviceConfig()
  } catch (error) {
    console.error('保存配置失败', error)
    ElMessage.error('保存失败')
  } finally {
    saveLoading.value = false
  }
}

/** 测试连接 */
const handleTestConnection = async () => {
  try {
    await BACnetApi.testConnection({ deviceId: props.device.id })
    ElMessage.success('连接测试成功')
  } catch (error) {
    console.error('连接测试失败', error)
    ElMessage.error('连接测试失败')
  }
}

/** 添加映射 */
const handleAddMapping = () => {
  mappingFormMode.value = 'create'
  Object.assign(mappingForm, {
    deviceId: props.device.id,
    identifier: '',
    objectType: '',
    objectInstance: 0,
    propertyIdentifier: 'presentValue',
    dataType: 'float',
    accessMode: 'r',
    pollingEnabled: true,
    unitConversion: '',
    valueMapping: '',
    priority: 8
  })
  mappingDialogVisible.value = true
}

/** 编辑映射 */
const handleEditMapping = (mapping: BACnetPropertyMappingVO) => {
  mappingFormMode.value = 'edit'
  Object.assign(mappingForm, mapping)
  mappingDialogVisible.value = true
}

/** 保存映射 */
const handleSaveMapping = async () => {
  const valid = await mappingFormRef.value?.validate()
  if (!valid) return

  mappingSaveLoading.value = true
  try {
    if (mappingFormMode.value === 'create') {
      await BACnetApi.createPropertyMapping(mappingForm as BACnetPropertyMappingVO)
      ElMessage.success('添加成功')
    } else {
      await BACnetApi.updatePropertyMapping(mappingForm as BACnetPropertyMappingVO)
      ElMessage.success('更新成功')
    }

    mappingDialogVisible.value = false
    await loadPropertyMappings()
  } catch (error) {
    console.error('保存映射失败', error)
    ElMessage.error('保存失败')
  } finally {
    mappingSaveLoading.value = false
  }
}

/** 删除映射 */
const handleDeleteMapping = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该属性映射吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await BACnetApi.deletePropertyMapping(id)
    ElMessage.success('删除成功')
    await loadPropertyMappings()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }
}

/** 测试读取 */
const handleTestRead = async (mapping: BACnetPropertyMappingVO) => {
  try {
    const result = await BACnetApi.readProperty({
      deviceId: props.device.id,
      mappingId: mapping.id!
    })
    ElMessage.success(`读取成功：${JSON.stringify(result)}`)
  } catch (error) {
    console.error('读取失败', error)
    ElMessage.error('读取失败')
  }
}

/** 测试写入 */
const handleTestWrite = (mapping: BACnetPropertyMappingVO) => {
  testWriteMapping.value = mapping
  testWriteValue.value = ''
  testWriteDialogVisible.value = true
}

/** 提交测试写入 */
const submitTestWrite = async () => {
  if (!testWriteValue.value) {
    ElMessage.warning('请输入写入值')
    return
  }

  testWriteLoading.value = true
  try {
    await BACnetApi.writeProperty({
      deviceId: props.device.id,
      mappingId: testWriteMapping.value!.id!,
      value: testWriteValue.value
    })
    ElMessage.success('写入成功')
    testWriteDialogVisible.value = false
  } catch (error) {
    console.error('写入失败', error)
    ElMessage.error('写入失败')
  } finally {
    testWriteLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.mb-4 {
  margin-bottom: 1rem;
}
</style>
