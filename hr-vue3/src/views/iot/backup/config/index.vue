<template>
  <ContentWrap>
    <!-- 操作按钮 -->
    <div class="mb-15px">
      <el-button
        type="primary"
        plain
        @click="openForm('create')"
        v-hasPermi="['iot:backup:config:create']"
      >
        <Icon icon="ep:plus" class="mr-5px" /> 新建配置
      </el-button>
    </div>

    <!-- 配置列表 -->
    <el-table
      row-key="id"
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
    >
      <el-table-column label="配置编号" align="center" prop="id" width="100" />
      <el-table-column label="配置名称" align="center" prop="configName" min-width="150" />
      <el-table-column label="备份类型" align="center" prop="backupType" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.backupType === 1" type="primary">全量备份</el-tag>
          <el-tag v-else-if="scope.row.backupType === 2" type="success">数据库备份</el-tag>
          <el-tag v-else-if="scope.row.backupType === 3" type="warning">表备份</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Cron表达式" align="center" prop="cronExpression" width="150">
        <template #default="scope">
          <el-tooltip :content="getCronDescription(scope.row.cronExpression)" placement="top">
            <code class="bg-gray-100 px-2 py-1 rounded text-sm">
              {{ scope.row.cronExpression }}
            </code>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="保留天数" align="center" prop="retentionDays" width="100">
        <template #default="scope">
          {{ scope.row.retentionDays }} 天
        </template>
      </el-table-column>
      <el-table-column label="最大备份数" align="center" prop="maxBackupCount" width="100">
        <template #default="scope">
          {{ scope.row.maxBackupCount }} 个
        </template>
      </el-table-column>
      <el-table-column label="启用压缩" align="center" prop="compressionEnabled" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.compressionEnabled ? 'success' : 'info'" size="small">
            {{ scope.row.compressionEnabled ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="enabled" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.enabled"
            @change="handleEnableChange(scope.row)"
            v-hasPermi="['iot:backup:config:enable']"
          />
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" min-width="180px" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['iot:backup:config:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="success"
            @click="handleExecute(scope.row)"
            v-hasPermi="['iot:backup:create']"
          >
            立即执行
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['iot:backup:config:delete']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <BackupConfigForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import { BackupConfigApi, BackupConfig } from '@/api/iot/backup'
import BackupConfigForm from './BackupConfigForm.vue'

/** IoT TDengine 备份配置列表 */
defineOptions({ name: 'IotBackupConfig' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<BackupConfig[]>([])

/** Cron表达式描述 */
const getCronDescription = (cron: string): string => {
  // 简单的Cron表达式解析
  const commonCrons: Record<string, string> = {
    '0 0 2 * * ?': '每天凌晨2点',
    '0 0 0 * * ?': '每天0点',
    '0 0 3 * * ?': '每天凌晨3点',
    '0 0 0 ? * 1': '每周一0点',
    '0 0 0 1 * ?': '每月1日0点'
  }
  return commonCrons[cron] || cron
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    list.value = await BackupConfigApi.getBackupConfigList()
  } finally {
    loading.value = false
  }
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 启用/禁用状态变更 */
const handleEnableChange = async (row: BackupConfig) => {
  try {
    await BackupConfigApi.enableBackupConfig(row.id, row.enabled)
    message.success(row.enabled ? '已启用' : '已禁用')
  } catch (error) {
    // 恢复原状态
    row.enabled = !row.enabled
  }
}

/** 立即执行 */
const handleExecute = async (row: BackupConfig) => {
  try {
    await message.confirm(`确定要立即执行【${row.configName}】配置吗？`)
    await BackupConfigApi.executeBackupConfig(row.id)
    message.success('备份任务已提交，请在备份管理中查看')
  } catch {}
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await BackupConfigApi.deleteBackupConfig(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

/** 初始化 */
onMounted(() => {
  getList()
})
</script>
