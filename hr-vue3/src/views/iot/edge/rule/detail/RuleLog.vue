<template>
  <div>
    <!-- 搜索栏 -->
    <el-form class="-mb-15px" :model="queryParams" :inline="true">
      <el-form-item label="执行结果" prop="executeResult">
        <el-select
          v-model="queryParams.executeResult"
          placeholder="请选择执行结果"
          clearable
          class="!w-200px"
        >
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" />
          重置
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 日志列表 -->
    <el-table v-loading="loading" :data="list" class="mt-4">
      <el-table-column
        label="执行时间"
        prop="executeTime"
        :formatter="dateFormatter"
        width="180"
      />
      <el-table-column label="执行结果" align="center" width="100">
        <template #default="{ row }">
          <el-tag :type="row.executeResult === 1 ? 'success' : 'danger'">
            {{ row.executeResult === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="触发数据" prop="triggerData" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <el-text class="text-xs">{{ row.triggerData }}</el-text>
        </template>
      </el-table-column>
      <el-table-column label="执行耗时(ms)" align="center" prop="executeDuration" width="120" />
      <el-table-column label="错误信息" prop="errorMessage" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <el-text v-if="row.errorMessage" type="danger" class="text-xs">
            {{ row.errorMessage }}
          </el-text>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="100">
        <template #default="scope">
          <el-button link type="primary" @click="viewDetail(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="getList"
    />

    <!-- 日志详情对话框 -->
    <el-dialog v-model="dialogVisible" title="日志详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="执行时间" :span="2">
          {{ formatDate(currentLog.executeTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="执行结果">
          <el-tag :type="currentLog.executeResult === 1 ? 'success' : 'danger'">
            {{ currentLog.executeResult === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="执行耗时">
          {{ currentLog.executeDuration }} ms
        </el-descriptions-item>
        <el-descriptions-item label="触发数据" :span="2">
          <pre class="text-xs">{{ formatJson(currentLog.triggerData) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="执行结果数据" :span="2">
          <pre class="text-xs">{{ formatJson(currentLog.resultData) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="currentLog.errorMessage">
          <el-text type="danger">{{ currentLog.errorMessage }}</el-text>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { dateFormatter, formatDate } from '@/utils/formatTime'

const props = defineProps<{
  ruleId: number
}>()

// 规则日志VO接口（基于后端API推断）
interface EdgeRuleLogVO {
  id: number
  ruleId: number
  executeTime: Date
  executeResult: number // 0=失败, 1=成功
  executeDuration: number // 执行耗时(ms)
  triggerData: string // 触发数据(JSON)
  resultData: string // 执行结果数据(JSON)
  errorMessage: string // 错误信息
}

// 注意：这里假设后端有规则日志查询API，如果没有需要创建
// 目前使用模拟数据
const loading = ref(true)
const list = ref<EdgeRuleLogVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  ruleId: props.ruleId,
  executeResult: undefined
})

const dialogVisible = ref(false)
const currentLog = ref<EdgeRuleLogVO>({} as EdgeRuleLogVO)

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    // TODO: 调用实际的规则日志API
    // const data = await EdgeRuleLogApi.getPage(queryParams)
    // list.value = data.list
    // total.value = data.total

    // 模拟数据
    await new Promise((resolve) => setTimeout(resolve, 500))
    list.value = [
      {
        id: 1,
        ruleId: props.ruleId,
        executeTime: new Date(),
        executeResult: 1,
        executeDuration: 120,
        triggerData: '{"temperature": 32, "humidity": 65}',
        resultData: '{"action": "turnOnFan", "success": true}',
        errorMessage: ''
      }
    ]
    total.value = 1
  } finally {
    loading.value = false
  }
}

/** 搜索 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置 */
const resetQuery = () => {
  queryParams.executeResult = undefined
  handleQuery()
}

/** 查看详情 */
const viewDetail = (row: EdgeRuleLogVO) => {
  currentLog.value = row
  dialogVisible.value = true
}

/** 格式化JSON */
const formatJson = (str: string) => {
  if (!str) return '-'
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

/** 初始化 */
onMounted(() => {
  getList()
})
</script>
