<template>
  <div>
    <!-- 搜索栏 -->
    <el-form class="-mb-15px" :model="queryParams" :inline="true">
      <el-form-item label="执行状态" prop="executeStatus">
        <el-select
          v-model="queryParams.executeStatus"
          placeholder="请选择执行状态"
          clearable
          class="!w-200px"
        >
          <el-option
            v-for="(value, key) in EdgeRuleExecuteStatusMap"
            :key="key"
            :label="value.label"
            :value="Number(key)"
          />
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
      <el-table-column label="执行状态" align="center" width="100">
        <template #default="{ row }">
          <el-tag :type="EdgeRuleExecuteStatusMap[row.executeStatus]?.type">
            {{ EdgeRuleExecuteStatusMap[row.executeStatus]?.label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="执行结果" prop="executeResult" min-width="250" show-overflow-tooltip>
        <template #default="{ row }">
          <el-text class="text-xs">{{ row.executeResult || '-' }}</el-text>
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
        <el-descriptions-item label="执行状态">
          <el-tag :type="EdgeRuleExecuteStatusMap[currentLog.executeStatus]?.type">
            {{ EdgeRuleExecuteStatusMap[currentLog.executeStatus]?.label }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDate(currentLog.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="执行结果" :span="2">
          <pre class="text-xs">{{ currentLog.executeResult || '-' }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { dateFormatter, formatDate } from '@/utils/formatTime'
import {
  EdgeRuleLogApi,
  EdgeRuleLogVO,
  EdgeRuleLogPageReqVO,
  EdgeRuleExecuteStatusMap
} from '@/api/iot/edge/rulelog'

const props = defineProps<{
  ruleId: number
}>()

const message = useMessage()

const loading = ref(true)
const list = ref<EdgeRuleLogVO[]>([])
const total = ref(0)
const queryParams = reactive<EdgeRuleLogPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  ruleId: props.ruleId,
  executeStatus: undefined
})

const dialogVisible = ref(false)
const currentLog = ref<EdgeRuleLogVO>({} as EdgeRuleLogVO)

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await EdgeRuleLogApi.getPage(queryParams)
    list.value = data.list
    total.value = data.total
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
  queryParams.executeStatus = undefined
  handleQuery()
}

/** 查看详情 */
const viewDetail = (row: EdgeRuleLogVO) => {
  currentLog.value = row
  dialogVisible.value = true
}

/** 初始化 */
onMounted(() => {
  getList()
})
</script>
