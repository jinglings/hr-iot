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
          <el-tag :type="EdgeModelDeployStatusMap[row.deployStatus]?.type">
            {{ EdgeModelDeployStatusMap[row.deployStatus]?.label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="运行状态" align="center" width="100">
        <template #default="{ row }">
          <el-tag :type="EdgeModelRunStatusMap[row.status]?.type">
            {{ EdgeModelRunStatusMap[row.status]?.label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="部署时间"
        prop="deployTime"
        :formatter="dateFormatter"
        width="180"
      />
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
import {
  EdgeModelDeploymentApi,
  EdgeModelDeploymentVO,
  EdgeModelDeployStatusMap,
  EdgeModelRunStatusMap
} from '@/api/iot/edge/deployment'
import { EdgeGatewayApi } from '@/api/iot/edge/gateway'

const props = defineProps<{
  modelId: number
}>()

const router = useRouter()
const message = useMessage()

const loading = ref(true)
const list = ref<any[]>([])

/** 查看网关 */
const viewGateway = (gatewayId: number) => {
  router.push({ path: `/iot/edge/gateway/detail/${gatewayId}` })
}

/** 获取部署列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await EdgeModelDeploymentApi.getPage({
      pageNo: 1,
      pageSize: 100,
      modelId: props.modelId
    })

    // 获取网关信息
    const deployments = data.list
    for (const deployment of deployments) {
      try {
        const gateway = await EdgeGatewayApi.get(deployment.gatewayId)
        deployment.gatewayName = gateway.name
        deployment.gatewaySerialNumber = gateway.serialNumber
      } catch (e) {
        deployment.gatewayName = '-'
        deployment.gatewaySerialNumber = '-'
      }
    }
    list.value = deployments
  } finally {
    loading.value = false
  }
}

/** 初始化 */
onMounted(() => {
  getList()
})
</script>
