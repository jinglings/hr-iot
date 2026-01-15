<template>
  <div class="device-debug">
    <el-row :gutter="20">
      <!-- 左侧设备选择 -->
      <el-col :span="6">
        <el-card shadow="never" class="device-select-card">
          <template #header>
            <span>选择设备</span>
          </template>
          <el-form label-width="70px" size="small">
            <el-form-item label="产品">
              <el-select
                v-model="selectedProductId"
                placeholder="请选择产品"
                clearable
                class="!w-full"
                @change="onProductChange"
              >
                <el-option
                  v-for="product in productList"
                  :key="product.id"
                  :label="product.name"
                  :value="product.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="设备">
              <el-select
                v-model="selectedDeviceId"
                placeholder="请选择设备"
                clearable
                filterable
                class="!w-full"
                @change="onDeviceChange"
              >
                <el-option
                  v-for="device in deviceList"
                  :key="device.id"
                  :label="device.deviceName"
                  :value="device.id"
                />
              </el-select>
            </el-form-item>
          </el-form>
          <div v-if="selectedDevice" class="device-info">
            <div class="info-item">
              <span class="label">设备名称:</span>
              <span>{{ selectedDevice.deviceName }}</span>
            </div>
            <div class="info-item">
              <span class="label">状态:</span>
              <el-tag :type="selectedDevice.state === 1 ? 'success' : 'info'" size="small">
                {{ selectedDevice.state === 1 ? '在线' : '离线' }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧调试面板 -->
      <el-col :span="18">
        <el-card shadow="never">
          <template #header>
            <span>调试面板</span>
          </template>

          <el-tabs v-model="activeTab" type="border-card">
            <!-- 属性调试 -->
            <el-tab-pane label="属性调试" name="property">
              <div class="debug-panel">
                <el-form label-width="100px" size="small">
                  <el-form-item label="属性">
                    <el-select v-model="propertyForm.identifier" placeholder="请选择属性" class="!w-300px">
                      <el-option
                        v-for="prop in properties"
                        :key="prop.identifier"
                        :label="`${prop.name} (${prop.identifier})`"
                        :value="prop.identifier"
                      />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="属性值">
                    <el-input v-model="propertyForm.value" placeholder="请输入属性值" class="!w-300px" />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" :loading="propertyLoading" @click="simulatePropertyReport">
                      模拟上报
                    </el-button>
                    <el-button :loading="propertyLoading" @click="sendPropertySet">
                      下发设置
                    </el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-tab-pane>

            <!-- 服务调试 -->
            <el-tab-pane label="服务调用" name="service">
              <div class="debug-panel">
                <el-form label-width="100px" size="small">
                  <el-form-item label="服务">
                    <el-select v-model="serviceForm.identifier" placeholder="请选择服务" class="!w-300px">
                      <el-option
                        v-for="srv in services"
                        :key="srv.identifier"
                        :label="`${srv.name} (${srv.identifier})`"
                        :value="srv.identifier"
                      />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="输入参数">
                    <el-input
                      v-model="serviceForm.inputParams"
                      type="textarea"
                      :rows="4"
                      placeholder="JSON格式输入参数"
                      class="!w-400px"
                    />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" :loading="serviceLoading" @click="invokeService">
                      调用服务
                    </el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-tab-pane>

            <!-- 调试日志 -->
            <el-tab-pane label="调试日志" name="log">
              <el-table v-loading="logLoading" :data="logList" border size="small" max-height="400">
                <el-table-column label="时间" prop="createTime" width="160">
                  <template #default="{ row }">
                    {{ formatTime(row.createTime) }}
                  </template>
                </el-table-column>
                <el-table-column label="方向" width="60">
                  <template #default="{ row }">
                    <el-tag :type="row.direction === 1 ? 'success' : 'warning'" size="small">
                      {{ row.direction === 1 ? '上行' : '下行' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="类型" prop="type" width="120" />
                <el-table-column label="标识" prop="identifier" width="100" />
                <el-table-column label="内容" prop="payload" show-overflow-tooltip />
                <el-table-column label="状态" width="60">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                      {{ row.status === 1 ? '成功' : '失败' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="延迟" width="70">
                  <template #default="{ row }">
                    {{ row.latency }}ms
                  </template>
                </el-table-column>
              </el-table>
              <div class="mt-3 flex justify-between">
                <el-button size="small" @click="loadDebugLogs">
                  <Icon icon="ep:refresh" class="mr-1" /> 刷新
                </el-button>
                <Pagination
                  v-model:limit="logPageParams.pageSize"
                  v-model:page="logPageParams.pageNo"
                  :total="logTotal"
                  small
                  @pagination="loadDebugLogs"
                />
              </div>
            </el-tab-pane>
          </el-tabs>

          <!-- 调试结果 -->
          <div v-if="debugResult" class="debug-result mt-4">
            <el-alert :type="debugResult.success ? 'success' : 'error'" :closable="false">
              <template #title>
                {{ debugResult.success ? '操作成功' : '操作失败' }}
                <span v-if="debugResult.latency" class="ml-2 text-gray-400">
                  ({{ debugResult.latency }}ms)
                </span>
              </template>
              <pre v-if="debugResult.data">{{ formatJson(debugResult.data) }}</pre>
              <span v-if="debugResult.errorMessage">{{ debugResult.errorMessage }}</span>
            </el-alert>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { formatDate } from '@/utils/formatTime'
import * as DebugApi from '@/api/iot/device/debug'
import * as ProductApi from '@/api/iot/product'
import * as DeviceApi from '@/api/iot/device'

defineOptions({ name: 'IotDeviceDebug' })

// 产品和设备
const productList = ref<any[]>([])
const deviceList = ref<any[]>([])
const selectedProductId = ref<number>()
const selectedDeviceId = ref<number>()
const selectedDevice = ref<any>(null)

// 物模型
const properties = ref<any[]>([])
const services = ref<any[]>([])

// 调试面板
const activeTab = ref('property')
const debugResult = ref<DebugApi.DebugResultVO | null>(null)

// 属性调试
const propertyLoading = ref(false)
const propertyForm = reactive({
  identifier: '',
  value: ''
})

// 服务调试
const serviceLoading = ref(false)
const serviceForm = reactive({
  identifier: '',
  inputParams: ''
})

// 日志
const logLoading = ref(false)
const logList = ref<DebugApi.DeviceDebugLogVO[]>([])
const logTotal = ref(0)
const logPageParams = reactive({
  pageNo: 1,
  pageSize: 10
})

// 加载产品列表
const loadProducts = async () => {
  try {
    productList.value = await ProductApi.getProductSimpleList()
  } catch (e) {
    console.error('加载产品失败', e)
  }
}

// 产品变化时加载设备
const onProductChange = async () => {
  selectedDeviceId.value = undefined
  selectedDevice.value = null
  deviceList.value = []
  properties.value = []
  services.value = []

  if (!selectedProductId.value) return

  try {
    const data = await DeviceApi.getDevicePage({ productId: selectedProductId.value, pageNo: 1, pageSize: 100 })
    deviceList.value = data.list || []
  } catch (e) {
    console.error('加载设备失败', e)
  }
}

// 设备变化时加载物模型
const onDeviceChange = async () => {
  debugResult.value = null

  if (!selectedDeviceId.value) {
    selectedDevice.value = null
    properties.value = []
    services.value = []
    return
  }

  // 获取设备详情
  selectedDevice.value = deviceList.value.find((d) => d.id === selectedDeviceId.value)

  // 加载物模型
  try {
    // TODO: 调用物模型API获取属性和服务列表
    // const thingModel = await ThingModelApi.getThingModelByProductId(selectedProductId.value)
    // properties.value = thingModel.properties || []
    // services.value = thingModel.services || []
    
    // 模拟数据
    properties.value = [
      { identifier: 'temperature', name: '温度' },
      { identifier: 'humidity', name: '湿度' }
    ]
    services.value = [
      { identifier: 'reset', name: '重置' }
    ]
  } catch (e) {
    console.error('加载物模型失败', e)
  }

  loadDebugLogs()
}

// 模拟属性上报
const simulatePropertyReport = async () => {
  if (!selectedDeviceId.value || !propertyForm.identifier || !propertyForm.value) {
    ElMessage.warning('请选择设备和属性并输入值')
    return
  }

  propertyLoading.value = true
  try {
    debugResult.value = await DebugApi.simulatePropertyReport({
      deviceId: selectedDeviceId.value,
      identifier: propertyForm.identifier,
      value: propertyForm.value
    })
    ElMessage.success('上报成功')
    loadDebugLogs()
  } finally {
    propertyLoading.value = false
  }
}

// 下发属性设置
const sendPropertySet = async () => {
  if (!selectedDeviceId.value || !propertyForm.identifier || !propertyForm.value) {
    ElMessage.warning('请选择设备和属性并输入值')
    return
  }

  propertyLoading.value = true
  try {
    debugResult.value = await DebugApi.sendPropertySet(
      selectedDeviceId.value,
      propertyForm.identifier,
      propertyForm.value
    )
    ElMessage.success('设置下发成功')
    loadDebugLogs()
  } finally {
    propertyLoading.value = false
  }
}

// 调用服务
const invokeService = async () => {
  if (!selectedDeviceId.value || !serviceForm.identifier) {
    ElMessage.warning('请选择设备和服务')
    return
  }

  serviceLoading.value = true
  try {
    debugResult.value = await DebugApi.invokeService({
      deviceId: selectedDeviceId.value,
      identifier: serviceForm.identifier,
      inputParams: serviceForm.inputParams
    })
    ElMessage.success('服务调用成功')
    loadDebugLogs()
  } finally {
    serviceLoading.value = false
  }
}

// 加载调试日志
const loadDebugLogs = async () => {
  if (!selectedDeviceId.value) return

  logLoading.value = true
  try {
    const data = await DebugApi.getDebugLogPage({
      ...logPageParams,
      deviceId: selectedDeviceId.value
    })
    logList.value = data.list || []
    logTotal.value = data.total || 0
  } finally {
    logLoading.value = false
  }
}

// 格式化时间
const formatTime = (time: string) => {
  return formatDate(time, 'YYYY-MM-DD HH:mm:ss')
}

// 格式化JSON
const formatJson = (str: string) => {
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped lang="scss">
.device-debug {
  padding: 16px;
}

.device-select-card {
  min-height: 400px;

  .device-info {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid var(--el-border-color-light);

    .info-item {
      margin-bottom: 8px;
      font-size: 13px;

      .label {
        color: var(--el-text-color-secondary);
        margin-right: 8px;
      }
    }
  }
}

.debug-panel {
  padding: 16px;
}

.debug-result {
  pre {
    margin: 8px 0 0 0;
    font-size: 12px;
    background: var(--el-fill-color-light);
    padding: 8px;
    border-radius: 4px;
    max-height: 200px;
    overflow: auto;
  }
}
</style>
