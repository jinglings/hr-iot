<template>
  <div>
    <div class="mb-4">
      <el-text type="info">该模型在以下边缘网关上部署：</el-text>
    </div>

    <!-- 部署列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="网关名称" prop="gatewayName" min-width="150" />
      <el-table-column label="网关序列号" prop="gatewaySerialNumber" min-width="180" />
      <el-table-column label="部署状态" align="center" width="120">
        <template #default="{ row }">
          <el-tag :type="getDeployStatusType(row.deployStatus)">
            {{ getDeployStatusLabel(row.deployStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="部署时间"
        prop="deployTime"
        :formatter="dateFormatter"
        width="180"
      />
      <el-table-column label="部署版本" align="center" prop="deployVersion" width="120" />
      <el-table-column label="错误信息" prop="deployError" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <el-text v-if="row.deployError" type="danger">{{ row.deployError }}</el-text>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="120">
        <template #default="scope">
          <el-button link type="primary" @click="viewGateway(scope.row.gatewayId)">
            查看网关
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 暂无部署提示 -->
    <el-empty v-if="!loading && list.length === 0" description="该模型尚未部署到任何网关">
      <el-button type="primary">部署到网关</el-button>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'

const props = defineProps<{
  modelId: number
}>()

const router = useRouter()

// 模型部署VO接口（基于后端API推断）
interface ModelDeploymentVO {
  id: number
  modelId: number
  gatewayId: number
  gatewayName: string
  gatewaySerialNumber: string
  deployStatus: number // 0=未部署, 1=部署中, 2=已部署, 3=部署失败
  deployTime: Date
  deployVersion: string
  deployError: string
}

const loading = ref(true)
const list = ref<ModelDeploymentVO[]>([])

/** 获取部署状态类型 */
const getDeployStatusType = (status: number) => {
  const statusMap = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger'
  }
  return statusMap[status] || 'info'
}

/** 获取部署状态标签 */
const getDeployStatusLabel = (status: number) => {
  const statusMap = {
    0: '未部署',
    1: '部署中',
    2: '已部署',
    3: '部署失败'
  }
  return statusMap[status] || '未知'
}

/** 查看网关 */
const viewGateway = (gatewayId: number) => {
  router.push({ path: `/iot/edge/gateway/detail/${gatewayId}` })
}

/** 获取部署列表 */
const getList = async () => {
  loading.value = true
  try {
    // TODO: 调用实际的模型部署API
    // const data = await EdgeModelDeploymentApi.getByModelId(props.modelId)
    // list.value = data

    // 模拟数据
    await new Promise((resolve) => setTimeout(resolve, 500))
    list.value = [
      {
        id: 1,
        modelId: props.modelId,
        gatewayId: 1,
        gatewayName: '边缘网关-01',
        gatewaySerialNumber: 'GW2024001',
        deployStatus: 2,
        deployTime: new Date(),
        deployVersion: 'v1.0.0',
        deployError: ''
      }
    ]
  } finally {
    loading.value = false
  }
}

/** 初始化 */
onMounted(() => {
  getList()
})
</script>
