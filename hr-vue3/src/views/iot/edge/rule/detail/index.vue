<template>
  <ContentWrap>
    <!-- 规则基本信息卡片 -->
    <el-card class="mb-4">
      <template #header>
        <div class="flex justify-between items-center">
          <span class="text-lg font-bold">{{ rule.name }}</span>
          <div>
            <el-tag :type="rule.status === 1 ? 'success' : 'info'" class="mr-2">
              {{ rule.status === 1 ? '启用' : '禁用' }}
            </el-tag>
            <el-tag :type="EdgeRuleDeployStatusMap[rule.deployStatus]?.type">
              {{ EdgeRuleDeployStatusMap[rule.deployStatus]?.label }}
            </el-tag>
          </div>
        </div>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="规则ID">{{ rule.id }}</el-descriptions-item>
        <el-descriptions-item label="所属网关">{{ gatewayName }}</el-descriptions-item>
        <el-descriptions-item label="规则类型">
          <el-tag :color="EdgeRuleTypeMap[rule.ruleType]?.color" style="color: white">
            {{ EdgeRuleTypeMap[rule.ruleType]?.label }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">{{ rule.priority }}</el-descriptions-item>
        <el-descriptions-item label="本地执行">
          {{ rule.executeLocal === 1 ? '是' : '否' }}
        </el-descriptions-item>
        <el-descriptions-item label="执行次数">{{ rule.executeCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="最后执行时间" :span="2">
          {{ rule.lastExecuteTime ? formatDate(rule.lastExecuteTime) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="部署时间">
          {{ rule.deployTime ? formatDate(rule.deployTime) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ formatDate(rule.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ formatDate(rule.updateTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="3">
          {{ rule.description || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="部署错误" :span="3" v-if="rule.deployError">
          <el-text type="danger">{{ rule.deployError }}</el-text>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- Tab标签页 -->
    <el-tabs v-model="activeTab">
      <el-tab-pane label="规则配置" name="config">
        <RuleConfig v-if="activeTab === 'config'" :rule="rule" @refresh="getRuleData" />
      </el-tab-pane>
      <el-tab-pane label="执行日志" name="log">
        <RuleLog v-if="activeTab === 'log'" :rule-id="rule.id" />
      </el-tab-pane>
    </el-tabs>
  </ContentWrap>
</template>

<script setup lang="ts">
import { EdgeRuleApi, EdgeRuleVO, EdgeRuleTypeMap, EdgeRuleDeployStatusMap } from '@/api/iot/edge/rule'
import { EdgeGatewayApi } from '@/api/iot/edge/gateway'
import { formatDate } from '@/utils/formatTime'
import RuleConfig from './RuleConfig.vue'
import RuleLog from './RuleLog.vue'

defineOptions({ name: 'IoTEdgeRuleDetail' })

const route = useRoute()
const message = useMessage()
const id = Number(route.params.id)
const loading = ref(true)
const rule = ref<EdgeRuleVO>({} as EdgeRuleVO)
const activeTab = ref('config')
const gatewayName = ref('-')

/** 获取规则详情 */
const getRuleData = async () => {
  loading.value = true
  try {
    rule.value = await EdgeRuleApi.get(id)
    await getGatewayName(rule.value.gatewayId)
  } finally {
    loading.value = false
  }
}

/** 获取网关名称 */
const getGatewayName = async (gatewayId: number) => {
  try {
    const gateway = await EdgeGatewayApi.get(gatewayId)
    gatewayName.value = gateway.name
  } catch (e) {
    gatewayName.value = '-'
  }
}

/** 初始化 */
onMounted(async () => {
  if (!id) {
    message.warning('参数错误，规则不能为空！')
    return
  }
  await getRuleData()
  activeTab.value = (route.query.tab as string) || 'config'
})
</script>
