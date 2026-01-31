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
      <el-form-item label="备份名称" prop="backupName">
        <el-input
          v-model="queryParams.backupName"
          placeholder="请输入备份名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item label="备份类型" prop="backupType">
        <el-select
          v-model="queryParams.backupType"
          placeholder="请选择备份类型"
          clearable
          class="!w-160px"
        >
          <el-option label="全量备份" :value="1" />
          <el-option label="数据库备份" :value="2" />
          <el-option label="表备份" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="备份状态" prop="backupStatus">
        <el-select
          v-model="queryParams.backupStatus"
          placeholder="请选择备份状态"
          clearable
          class="!w-160px"
        >
          <el-option label="备份中" :value="0">
            <el-tag type="warning" size="small">备份中</el-tag>
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
        <el-button
          type="primary"
          plain
          @click="handleCreate"
          v-hasPermi="['iot:backup:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 创建备份
        </el-button>
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
      <el-table-column label="备份编号" align="center" prop="id" width="100" />
      <el-table-column label="备份名称" align="center" prop="backupName" min-width="180" />
      <el-table-column label="备份类型" align="center" prop="backupType" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.backupType === 1" type="primary">全量备份</el-tag>
          <el-tag v-else-if="scope.row.backupType === 2" type="success">数据库备份</el-tag>
          <el-tag v-else-if="scope.row.backupType === 3" type="warning">表备份</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备份模式" align="center" prop="backupMode" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.backupMode === 1" type="info">手动</el-tag>
          <el-tag v-else-if="scope.row.backupMode === 2" type="primary">定时</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备份状态" align="center" prop="backupStatus" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.backupStatus === 0" type="warning">
            <Icon icon="ep:loading" class="mr-3px animate-spin" /> 备份中
          </el-tag>
          <el-tag v-else-if="scope.row.backupStatus === 1" type="success">成功</el-tag>
          <el-tag v-else-if="scope.row.backupStatus === 2" type="danger">失败</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="文件大小" align="center" prop="fileSize" width="120">
        <template #default="scope">
          {{ formatFileSize(scope.row.fileSize) }}
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
      <el-table-column label="操作" align="center" min-width="200px" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="handleDetail(scope.row)"
            v-hasPermi="['iot:backup:query']"
          >
            详情
          </el-button>
          <el-button
            v-if="scope.row.backupStatus === 1"
            link
            type="primary"
            @click="handleDownload(scope.row)"
            v-hasPermi="['iot:backup:download']"
          >
            下载
          </el-button>
          <el-button
            v-if="scope.row.backupStatus === 1"
            link
            type="success"
            @click="handleRestore(scope.row)"
            v-hasPermi="['iot:restore:execute']"
          >
            恢复
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['iot:backup:delete']"
          >
            删除
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

  <!-- 创建备份弹窗 -->
  <BackupCreateForm ref="createFormRef" @success="getList" />

  <!-- 备份详情弹窗 -->
  <BackupDetailDialog ref="detailDialogRef" />

  <!-- 恢复弹窗 -->
  <RestoreForm ref="restoreFormRef" @success="handleRestoreSuccess" />
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import { BackupApi, BackupRecord } from '@/api/iot/backup'
import BackupCreateForm from './BackupCreateForm.vue'
import BackupDetailDialog from './BackupDetailDialog.vue'
import RestoreForm from './RestoreForm.vue'
import download from '@/utils/download'

/** IoT TDengine 备份列表 */
defineOptions({ name: 'IotBackupList' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<BackupRecord[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  backupName: undefined,
  backupType: undefined,
  backupStatus: undefined,
  createTime: []
})
const queryFormRef = ref()

/** 格式化文件大小 */
const formatFileSize = (bytes: number | undefined): string => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
  return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
}

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
    const data = await BackupApi.getBackupPage(queryParams)
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

/** 创建备份 */
const createFormRef = ref()
const handleCreate = () => {
  createFormRef.value.open()
}

/** 查看详情 */
const detailDialogRef = ref()
const handleDetail = (row: BackupRecord) => {
  detailDialogRef.value.open(row.id)
}

/** 下载备份 */
const handleDownload = async (row: BackupRecord) => {
  try {
    const data = await BackupApi.downloadBackup(row.id)
    download.zip(data, `${row.backupName}.zip`)
    message.success('下载成功')
  } catch (error) {
    message.error('下载失败')
  }
}

/** 执行恢复 */
const restoreFormRef = ref()
const handleRestore = (row: BackupRecord) => {
  restoreFormRef.value.open(row)
}

/** 恢复成功 */
const handleRestoreSuccess = () => {
  message.success('恢复任务已提交，请在恢复记录中查看进度')
}

/** 删除备份 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await BackupApi.deleteBackup(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
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
