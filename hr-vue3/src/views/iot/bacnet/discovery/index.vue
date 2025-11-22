<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="100px"
    >
      <el-form-item>
        <el-button type="primary" @click="handleDiscover" :loading="discovering">
          <Icon icon="ep:search" class="mr-5px" />
          立即发现设备
        </el-button>
        <el-button @click="handleQuery">
          <Icon icon="ep:refresh" class="mr-5px" />
          刷新列表
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- Tabs 切换 -->
  <ContentWrap>
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <!-- 未绑定设备列表 -->
      <el-tab-pane label="未绑定设备" name="unbound">
        <el-table
          v-loading="loading"
          :data="unboundList"
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column label="实例号" prop="instanceNumber" width="100" />
          <el-table-column label="设备名称" prop="deviceName" min-width="150" />
          <el-table-column label="IP地址" prop="ipAddress" width="140" />
          <el-table-column label="厂商名称" prop="vendorName" min-width="120" />
          <el-table-column label="型号" prop="modelName" min-width="120" />
          <el-table-column label="在线状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.onlineStatus === 1 ? 'success' : 'danger'">
                {{ scope.row.onlineStatus === 1 ? '在线' : '离线' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="最后发现时间" prop="lastSeenTime" width="180">
            <template #default="scope">
              <span>{{ formatDate(scope.row.lastSeenTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button
                link
                type="primary"
                @click="handleBind(scope.row)"
                v-hasPermi="['iot:bacnet:bind']"
              >
                绑定
              </el-button>
              <el-button
                link
                type="danger"
                @click="handleDelete(scope.row.id)"
                v-hasPermi="['iot:bacnet:delete']"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 已绑定设备列表 -->
      <el-tab-pane label="已绑定设备" name="bound">
        <el-table v-loading="loading" :data="boundList" style="width: 100%">
          <el-table-column label="实例号" prop="instanceNumber" width="100" />
          <el-table-column label="设备名称" prop="deviceName" min-width="150" />
          <el-table-column label="IP地址" prop="ipAddress" width="140" />
          <el-table-column label="厂商名称" prop="vendorName" min-width="120" />
          <el-table-column label="型号" prop="modelName" min-width="120" />
          <el-table-column label="绑定的IoT设备ID" prop="deviceId" width="150" />
          <el-table-column label="在线状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.onlineStatus === 1 ? 'success' : 'danger'">
                {{ scope.row.onlineStatus === 1 ? '在线' : '离线' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="最后发现时间" prop="lastSeenTime" width="180">
            <template #default="scope">
              <span>{{ formatDate(scope.row.lastSeenTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button
                link
                type="primary"
                @click="handleViewDevice(scope.row.deviceId)"
                v-hasPermi="['iot:device:query']"
              >
                查看设备
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </ContentWrap>

  <!-- 绑定设备弹窗 -->
  <el-dialog v-model="bindDialogVisible" title="绑定BACnet设备" width="600px">
    <el-form ref="bindFormRef" :model="bindForm" :rules="bindRules" label-width="120px">
      <el-form-item label="BACnet设备">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="实例号">{{
            selectedRecord?.instanceNumber
          }}</el-descriptions-item>
          <el-descriptions-item label="设备名称">{{
            selectedRecord?.deviceName
          }}</el-descriptions-item>
          <el-descriptions-item label="IP地址">{{
            selectedRecord?.ipAddress
          }}</el-descriptions-item>
          <el-descriptions-item label="厂商">{{ selectedRecord?.vendorName }}</el-descriptions-item>
          <el-descriptions-item label="型号">{{ selectedRecord?.modelName }}</el-descriptions-item>
        </el-descriptions>
      </el-form-item>

      <el-form-item label="IoT设备" prop="deviceId">
        <el-select
          v-model="bindForm.deviceId"
          placeholder="选择现有设备或创建新设备"
          clearable
          filterable
          style="width: 100%"
        >
          <el-option
            v-for="device in iotDeviceList"
            :key="device.id"
            :label="`${device.deviceName} (${device.productKey})`"
            :value="device.id"
          />
        </el-select>
        <el-link type="primary" @click="handleCreateDevice" style="margin-top: 5px">
          或创建新设备
        </el-link>
      </el-form-item>

      <el-form-item label="启用轮询" prop="pollingEnabled">
        <el-switch v-model="bindForm.pollingEnabled" />
      </el-form-item>

      <el-form-item label="轮询间隔(ms)" prop="pollingInterval" v-if="bindForm.pollingEnabled">
        <el-input-number
          v-model="bindForm.pollingInterval"
          :min="1000"
          :max="3600000"
          :step="1000"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="自动映射属性" prop="autoMap">
        <el-switch v-model="bindForm.autoMap" />
        <el-text type="info" size="small" style="margin-left: 10px">
          自动创建基本的属性映射配置
        </el-text>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="bindDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitBind" :loading="bindLoading">确定绑定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { BACnetApi, type BACnetDiscoveryRecordVO } from '@/api/iot/bacnet'
import { DeviceApi } from '@/api/iot/device/device'
import { formatDate } from '@/utils/formatTime'
import { useRouter } from 'vue-router'

const router = useRouter()

// ========== 列表相关 ==========
const loading = ref(false)
const discovering = ref(false)
const activeTab = ref('unbound')
const unboundList = ref<BACnetDiscoveryRecordVO[]>([])
const boundList = ref<BACnetDiscoveryRecordVO[]>([])
const selectedIds = ref<number[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10
})

// ========== 绑定相关 ==========
const bindDialogVisible = ref(false)
const bindLoading = ref(false)
const selectedRecord = ref<BACnetDiscoveryRecordVO | null>(null)
const iotDeviceList = ref<any[]>([])

const bindForm = reactive({
  discoveryRecordId: 0,
  deviceId: undefined,
  pollingEnabled: true,
  pollingInterval: 5000,
  autoMap: false
})

const bindRules = reactive({
  deviceId: [{ required: true, message: '请选择IoT设备', trigger: 'change' }],
  pollingInterval: [{ required: true, message: '请输入轮询间隔', trigger: 'blur' }]
})

const bindFormRef = ref()

// ========== 生命周期 ==========
onMounted(() => {
  loadData()
  loadIotDevices()
})

// ========== 方法 ==========

/** 加载数据 */
const loadData = async () => {
  if (activeTab.value === 'unbound') {
    await loadUnboundList()
  } else {
    await loadBoundList()
  }
}

/** 加载未绑定设备列表 */
const loadUnboundList = async () => {
  loading.value = true
  try {
    const data = await BACnetApi.getUnboundList()
    unboundList.value = data
  } catch (error) {
    console.error('加载未绑定设备列表失败', error)
  } finally {
    loading.value = false
  }
}

/** 加载已绑定设备列表 */
const loadBoundList = async () => {
  loading.value = true
  try {
    const data = await BACnetApi.getBoundList()
    boundList.value = data
  } catch (error) {
    console.error('加载已绑定设备列表失败', error)
  } finally {
    loading.value = false
  }
}

/** 加载IoT设备列表 */
const loadIotDevices = async () => {
  try {
    const data = await DeviceApi.getDevicePage({
      pageNo: 1,
      pageSize: 100
    })
    iotDeviceList.value = data.list || []
  } catch (error) {
    console.error('加载IoT设备列表失败', error)
  }
}

/** 立即发现设备 */
const handleDiscover = async () => {
  discovering.value = true
  try {
    await BACnetApi.discoverNow()
    ElMessage.success('设备发现已启动，请稍后刷新查看结果')
    // 延迟3秒后自动刷新列表
    setTimeout(() => {
      loadData()
    }, 3000)
  } catch (error) {
    console.error('设备发现失败', error)
    ElMessage.error('设备发现失败')
  } finally {
    discovering.value = false
  }
}

/** 查询列表 */
const handleQuery = () => {
  loadData()
}

/** Tab切换 */
const handleTabClick = () => {
  loadData()
}

/** 选择变化 */
const handleSelectionChange = (selection: BACnetDiscoveryRecordVO[]) => {
  selectedIds.value = selection.map((item) => item.id)
}

/** 绑定设备 */
const handleBind = (record: BACnetDiscoveryRecordVO) => {
  selectedRecord.value = record
  bindForm.discoveryRecordId = record.id
  bindForm.deviceId = undefined
  bindForm.pollingEnabled = true
  bindForm.pollingInterval = 5000
  bindForm.autoMap = false
  bindDialogVisible.value = true
}

/** 提交绑定 */
const submitBind = async () => {
  const valid = await bindFormRef.value?.validate()
  if (!valid) return

  bindLoading.value = true
  try {
    await BACnetApi.bindDevice(bindForm)
    ElMessage.success('绑定成功')
    bindDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('绑定失败', error)
    ElMessage.error('绑定失败')
  } finally {
    bindLoading.value = false
  }
}

/** 创建新设备 */
const handleCreateDevice = () => {
  ElMessage.info('请前往设备管理页面创建新设备')
  // TODO: 可以跳转到设备创建页面
  // router.push('/iot/device/create')
}

/** 查看设备 */
const handleViewDevice = (deviceId: number) => {
  router.push(`/iot/device/${deviceId}`)
}

/** 删除 */
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该发现记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await BACnetApi.deleteDiscoveryRecord(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped lang="scss">
.el-descriptions {
  margin-bottom: 10px;
}
</style>
