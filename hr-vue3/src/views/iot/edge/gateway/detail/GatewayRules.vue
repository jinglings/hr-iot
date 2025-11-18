<template>
  <div>
    <el-table :data="ruleList" v-loading="loading">
      <el-table-column label="规则名称" prop="name" min-width="150" />
      <el-table-column label="规则类型" prop="ruleType" width="120" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="部署状态" align="center" prop="deployStatus" width="100">
        <template #default="{ row }">
          <el-tag
            :type="
              row.deployStatus === 1 ? 'success' : row.deployStatus === 2 ? 'danger' : 'info'
            "
          >
            {{
              row.deployStatus === 1 ? '已部署' : row.deployStatus === 2 ? '部署失败' : '未部署'
            }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="执行次数" prop="executeCount" width="100" />
      <el-table-column label="最后执行时间" prop="lastExecuteTime" width="180" />
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { EdgeRuleApi, EdgeRuleVO } from '@/api/iot/edge/rule'

const props = defineProps<{
  gatewayId: number
}>()

const loading = ref(true)
const ruleList = ref<EdgeRuleVO[]>([])

/** 获取规则列表 */
const getRuleList = async () => {
  loading.value = true
  try {
    const data = await EdgeRuleApi.getPage({
      pageNo: 1,
      pageSize: 100,
      gatewayId: props.gatewayId
    })
    ruleList.value = data.list
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getRuleList()
})
</script>
