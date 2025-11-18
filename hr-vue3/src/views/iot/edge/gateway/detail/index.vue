<template>
  <ContentWrap>
    <!-- 网关基本信息 -->
    <el-descriptions :column="3" border v-loading="loading">
      <el-descriptions-item label="网关名称">
        <span class="font-bold">{{ gateway?.name }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="序列号">
        {{ gateway?.serialNumber }}
      </el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="EdgeGatewayStatusMap[gateway?.status]?.type">
          {{ EdgeGatewayStatusMap[gateway?.status]?.label }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="网关标识">
        {{ gateway?.gatewayKey }}
      </el-descriptions-item>
      <el-descriptions-item label="设备型号">
        {{ gateway?.deviceType || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="IP地址">
        {{ gateway?.ipAddress || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="硬件版本">
        {{ gateway?.hardwareVersion || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="软件版本">
        {{ gateway?.softwareVersion || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="安装位置">
        {{ gateway?.location || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="最后在线时间" :span="2">
        {{ formatDate(gateway?.lastOnlineTime) }}
      </el-descriptions-item>
      <el-descriptions-item label="激活时间">
        {{ formatDate(gateway?.activeTime) }}
      </el-descriptions-item>
      <el-descriptions-item label="描述" :span="3">
        {{ gateway?.description || '-' }}
      </el-descriptions-item>
    </el-descriptions>

    <!-- Tab标签页 -->
    <el-tabs v-model="activeTab" class="mt-4">
      <!-- 网关信息Tab -->
      <el-tab-pane label="网关信息" name="info">
        <GatewayInfo :gateway="gateway" />
      </el-tab-pane>

      <!-- 关联规则Tab -->
      <el-tab-pane label="关联规则" name="rules">
        <GatewayRules :gateway-id="gatewayId" />
      </el-tab-pane>

      <!-- 实时监控Tab -->
      <el-tab-pane label="实时监控" name="monitor">
        <GatewayMonitor :gateway-id="gatewayId" />
      </el-tab-pane>
    </el-tabs>

    <!-- 操作按钮 -->
    <el-row class="mt-4" :gutter="10">
      <el-col :span="1.5">
        <el-button @click="goBack">
          <Icon icon="ep:back" class="mr-5px" />
          返回
        </el-button>
      </el-col>
    </el-row>
  </ContentWrap>
</template>

<script setup lang="ts" name="EdgeGatewayDetail">
import { EdgeGatewayApi, EdgeGatewayVO, EdgeGatewayStatusMap } from '@/api/iot/edge/gateway'
import { formatDate } from '@/utils/formatTime'
import GatewayInfo from './GatewayInfo.vue'
import GatewayRules from './GatewayRules.vue'
import GatewayMonitor from './GatewayMonitor.vue'

const route = useRoute()
const router = useRouter()
const gatewayId = Number(route.params.id)

const loading = ref(true)
const gateway = ref<EdgeGatewayVO>()
const activeTab = ref('info')

/** 获取网关详情 */
const getGatewayDetail = async () => {
  loading.value = true
  try {
    gateway.value = await EdgeGatewayApi.get(gatewayId)
  } finally {
    loading.value = false
  }
}

/** 返回 */
const goBack = () => {
  router.back()
}

/** 初始化 */
onMounted(() => {
  getGatewayDetail()
})
</script>
