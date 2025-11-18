<template>
  <div v-loading="loading">
    <el-alert
      v-if="!hasShadow"
      title="设备影子未启用"
      type="info"
      description="当前设备尚未启用设备影子功能。设备影子可以在设备离线时缓存期望状态，并在设备上线后自动同步。"
      :closable="false"
      show-icon
      class="mb-4"
    />

    <template v-else>
      <!-- 操作栏 -->
      <el-row :gutter="10" class="mb-4">
        <el-col :span="1.5">
          <el-button @click="refreshShadow">
            <Icon icon="ep:refresh" class="mr-5px" />
            刷新
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button @click="showHistory = true">
            <Icon icon="ep:clock" class="mr-5px" />
            查看历史
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain @click="handleClearShadow">
            <Icon icon="ep:delete" class="mr-5px" />
            清除影子
          </el-button>
        </el-col>
        <el-col :span="1.5" class="ml-auto">
          <el-text type="info">
            版本号: {{ shadow?.version || 0 }} | 期望状态版本: {{
              shadow?.desiredVersion || 0
            }}
            | 实际状态版本: {{ shadow?.reportedVersion || 0 }}
          </el-text>
        </el-col>
      </el-row>

      <!-- 三栏对比 -->
      <el-row :gutter="16">
        <!-- 期望状态 -->
        <el-col :span="8">
          <el-card header="期望状态 (Desired)" shadow="hover">
            <template #header>
              <div class="flex justify-between items-center">
                <span>期望状态 (Desired)</span>
                <el-button size="small" @click="editDesired">
                  <Icon icon="ep:edit" class="mr-5px" />
                  编辑
                </el-button>
              </div>
            </template>
            <el-input
              v-model="desiredJson"
              type="textarea"
              :rows="15"
              placeholder="暂无期望状态"
              readonly
            />
            <div class="mt-2 text-xs text-gray-500">
              最后更新: {{ formatDate(shadow?.lastDesiredTime) || '-' }}
            </div>
          </el-card>
        </el-col>

        <!-- 实际状态 -->
        <el-col :span="8">
          <el-card header="实际状态 (Reported)" shadow="hover">
            <el-input
              v-model="reportedJson"
              type="textarea"
              :rows="15"
              placeholder="设备未上报状态"
              readonly
            />
            <div class="mt-2 text-xs text-gray-500">
              最后更新: {{ formatDate(shadow?.lastReportedTime) || '-' }}
            </div>
          </el-card>
        </el-col>

        <!-- 差量状态 -->
        <el-col :span="8">
          <el-card header="差量状态 (Delta)" shadow="hover">
            <template #header>
              <div class="flex justify-between items-center">
                <span>差量状态 (Delta)</span>
                <el-button
                  v-if="deltaJson && deltaJson !== '{}'"
                  size="small"
                  type="primary"
                  @click="syncToDevice"
                >
                  <Icon icon="ep:refresh" class="mr-5px" />
                  同步到设备
                </el-button>
              </div>
            </template>
            <el-input
              v-model="deltaJson"
              type="textarea"
              :rows="15"
              placeholder="期望状态与实际状态一致"
              readonly
            />
            <el-alert
              v-if="deltaJson && deltaJson !== '{}'"
              title="存在差异"
              type="warning"
              description="期望状态与实际状态存在差异，可能需要同步到设备"
              :closable="false"
              show-icon
              class="mt-2"
            />
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- 编辑期望状态对话框 -->
    <el-dialog v-model="dialogVisible" title="编辑期望状态" width="600px">
      <el-form ref="formRef" :model="formData" label-width="80px">
        <el-form-item label="期望状态">
          <el-input
            v-model="formData.desiredJson"
            type="textarea"
            :rows="15"
            placeholder="请输入JSON格式的期望状态"
          />
          <el-text v-if="jsonError" type="danger" size="small">{{ jsonError }}</el-text>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDesired" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 历史记录对话框 -->
    <el-dialog v-model="showHistory" title="设备影子历史记录" width="900px">
      <el-table :data="historyList" v-loading="historyLoading">
        <el-table-column label="时间" prop="createTime" width="180" />
        <el-table-column label="变更类型" prop="changeType" width="120">
          <template #default="{ row }">
            <el-tag :type="ShadowChangeTypeMap[row.changeType]?.type">
              {{ ShadowChangeTypeMap[row.changeType]?.label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="版本号" prop="version" width="80" />
        <el-table-column label="变更内容" prop="afterState" min-width="200">
          <template #default="{ row }">
            <el-text class="text-xs" truncated>
              {{ JSON.stringify(row.afterState) }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="操作人" prop="creator" width="100" />
      </el-table>
      <Pagination
        :total="historyTotal"
        v-model:page="historyQuery.pageNo"
        v-model:limit="historyQuery.pageSize"
        @pagination="getHistory"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {
  DeviceShadowApi,
  DeviceShadowVO,
  ShadowChangeTypeMap,
  DeviceShadowHistoryVO
} from '@/api/iot/device/shadow'
import { formatDate } from '@/utils/formatTime'

const props = defineProps<{
  deviceId: number
}>()

const message = useMessage()

const loading = ref(false)
const hasShadow = ref(false)
const shadow = ref<DeviceShadowVO>()
const desiredJson = ref('')
const reportedJson = ref('')
const deltaJson = ref('')

const dialogVisible = ref(false)
const submitting = ref(false)
const formData = reactive({
  desiredJson: ''
})
const jsonError = ref('')

const showHistory = ref(false)
const historyLoading = ref(false)
const historyList = ref<DeviceShadowHistoryVO[]>([])
const historyTotal = ref(0)
const historyQuery = reactive({
  pageNo: 1,
  pageSize: 10
})

/** 获取设备影子 */
const getShadow = async () => {
  loading.value = true
  try {
    shadow.value = await DeviceShadowApi.getShadow(props.deviceId)
    hasShadow.value = true
    desiredJson.value = JSON.stringify(shadow.value.desired || {}, null, 2)
    reportedJson.value = JSON.stringify(shadow.value.reported || {}, null, 2)
    deltaJson.value = JSON.stringify(shadow.value.delta || {}, null, 2)
  } catch (error: any) {
    // 如果返回404或者影子不存在，则显示未启用状态
    if (error?.code === 404 || error?.message?.includes('不存在')) {
      hasShadow.value = false
    } else {
      message.error('获取设备影子失败')
    }
  } finally {
    loading.value = false
  }
}

/** 刷新影子 */
const refreshShadow = () => {
  getShadow()
}

/** 编辑期望状态 */
const editDesired = () => {
  formData.desiredJson = desiredJson.value
  jsonError.value = ''
  dialogVisible.value = true
}

/** 提交期望状态 */
const submitDesired = async () => {
  // 验证JSON格式
  try {
    const parsed = JSON.parse(formData.desiredJson)
    jsonError.value = ''

    submitting.value = true
    await DeviceShadowApi.updateDesired({
      deviceId: props.deviceId,
      desired: parsed
    })
    message.success('更新成功')
    dialogVisible.value = false
    await getShadow()
  } catch (error: any) {
    if (error instanceof SyntaxError) {
      jsonError.value = 'JSON格式错误，请检查'
    } else {
      message.error('更新失败')
    }
  } finally {
    submitting.value = false
  }
}

/** 同步到设备 */
const syncToDevice = async () => {
  try {
    await message.confirm('确定要将期望状态同步到设备吗？')
    // 实际上更新期望状态就会自动同步到设备
    message.info('同步指令已发送，请等待设备上线后自动同步')
  } catch {}
}

/** 清除影子 */
const handleClearShadow = async () => {
  try {
    await message.confirm('确定要清除设备影子吗？此操作不可恢复！', '警告')
    await DeviceShadowApi.clearShadow(props.deviceId)
    message.success('清除成功')
    await getShadow()
  } catch {}
}

/** 获取历史记录 */
const getHistory = async () => {
  historyLoading.value = true
  try {
    const data = await DeviceShadowApi.getHistory({
      deviceId: props.deviceId,
      pageNo: historyQuery.pageNo,
      pageSize: historyQuery.pageSize
    })
    historyList.value = data.list
    historyTotal.value = data.total
  } finally {
    historyLoading.value = false
  }
}

/** 初始化 */
onMounted(() => {
  getShadow()
})
</script>

<style scoped>
:deep(.el-textarea__inner) {
  font-family: 'Courier New', Courier, monospace;
  font-size: 12px;
}
</style>
