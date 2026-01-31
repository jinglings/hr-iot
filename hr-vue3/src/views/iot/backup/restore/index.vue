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
      <el-form-item label="恢复状态" prop="restoreStatus">
        <el-select
          v-model="queryParams.restoreStatus"
          placeholder="请选择恢复状态"
          clearable
          class="!w-160px"
        >
          <el-option label="恢复中" :value="0">
            <el-tag type="warning" size="small">恢复中</el-tag>
          </el-option>
          <el-option label="成功" :value="1">
            <el-tag type="success" size="small">成功</el-tag>
          </el-option>
          <el-option label="失败" :value="2">
            <el-tag type="danger" size="small">失败</el-tag>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-220px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
      row-key="id"
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
    >
      <el-table-column label="恢复编号" align="center" prop="id" width="100" />
      <el-table-column label="备份编号" align="center" prop="backupId" width="100" />
      <el-table-column label="备份名称" align="center" prop="backupName" min-width="180" />
      <el-table-column label="恢复类型" align="center" prop="restoreType" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.restoreType === 1" type="primary">完整恢复</el-tag>
          <el-tag v-else-if="scope.row.restoreType === 2" type="success">选择性恢复</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="恢复状态" align="center" prop="restoreStatus" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.restoreStatus === 0" type="warning">
            <Icon icon="ep:loading" class="mr-3px animate-spin" /> 恢复中
          </el-tag>
          <el-tag v-else-if="scope.row.restoreStatus === 1" type="success">成功</el-tag>
          <el-tag v-else-if="scope.row.restoreStatus === 2" type="danger">失败</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="数据条数" align="center" prop="recordCount" width="120">
        <template #default="scope">
          {{ scope.row.recordCount ? scope.row.recordCount.toLocaleString() : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="耗时" align="center" prop="duration" width="100">
        <template #default="scope">
          {{ scope.row.duration ? formatDuration(scope.row.duration) : '-' }}
        </template>
      </el-table-column>
      <el-table-column
        label="开始时间"
        align="center"
        prop="startTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column
        label="结束时间"
        align="center"
        prop="endTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" min-width="100px" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="handleDetail(scope.row)"
          >
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 恢复详情弹窗 -->
  <RestoreDetailDialog ref="detailDialogRef" />
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import { RestoreApi, RestoreRecord } from '@/api/iot/backup'
import RestoreDetailDialog from './RestoreDetailDialog.vue'

/** IoT TDengine 恢复记录列表 */
defineOptions({ name: 'IotBackupRestore' })

const loading = ref(true)
const list = ref<RestoreRecord[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  restoreStatus: undefined,
  createTime: []
})
const queryFormRef = ref()

/** 格式化耗时 */
const formatDuration = (seconds: number): string => {
  if (seconds < 60) return seconds + '秒'
  if (seconds < 3600) return Math.floor(seconds / 60) + '分' + (seconds % 60) + '秒'
  const hours = Math.floor(seconds / 3600)
  const mins = Math.floor((seconds % 3600) / 60)
  return hours + '时' + mins + '分'
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await RestoreApi.getRestorePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 查看详情 */
const detailDialogRef = ref()
const handleDetail = (row: RestoreRecord) => {
  detailDialogRef.value.open(row)
}

/** 初始化 */
onMounted(() => {
  getList()
})
</script>

<style scoped>
.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
