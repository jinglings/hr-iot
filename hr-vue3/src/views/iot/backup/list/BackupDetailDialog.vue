<template>
  <Dialog title="备份详情" v-model="dialogVisible" width="700px">
    <el-descriptions :column="2" border v-loading="loading">
      <el-descriptions-item label="备份编号" :span="1">
        {{ detailData.id }}
      </el-descriptions-item>
      <el-descriptions-item label="备份名称" :span="1">
        {{ detailData.backupName }}
      </el-descriptions-item>
      <el-descriptions-item label="备份类型" :span="1">
        <el-tag v-if="detailData.backupType === 1" type="primary">全量备份</el-tag>
        <el-tag v-else-if="detailData.backupType === 2" type="success">数据库备份</el-tag>
        <el-tag v-else-if="detailData.backupType === 3" type="warning">表备份</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="备份模式" :span="1">
        <el-tag v-if="detailData.backupMode === 1" type="info">手动</el-tag>
        <el-tag v-else-if="detailData.backupMode === 2" type="primary">定时</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="备份状态" :span="1">
        <el-tag v-if="detailData.backupStatus === 0" type="warning">
          <Icon icon="ep:loading" class="mr-3px animate-spin" /> 备份中
        </el-tag>
        <el-tag v-else-if="detailData.backupStatus === 1" type="success">成功</el-tag>
        <el-tag v-else-if="detailData.backupStatus === 2" type="danger">失败</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="文件格式" :span="1">
        {{ detailData.fileFormat || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="文件大小" :span="1">
        {{ formatFileSize(detailData.fileSize) }}
      </el-descriptions-item>
      <el-descriptions-item label="数据条数" :span="1">
        {{ detailData.recordCount ? detailData.recordCount.toLocaleString() : '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="开始时间" :span="1">
        {{ formatDateTime(detailData.startTime) }}
      </el-descriptions-item>
      <el-descriptions-item label="结束时间" :span="1">
        {{ formatDateTime(detailData.endTime) }}
      </el-descriptions-item>
      <el-descriptions-item label="耗时" :span="1">
        {{ detailData.duration ? formatDuration(detailData.duration) : '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="创建者" :span="1">
        {{ detailData.creator || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="备份范围" :span="2">
        <template v-if="detailData.backupScope && detailData.backupScope.length > 0">
          <el-tag
            v-for="scope in detailData.backupScope"
            :key="scope"
            class="mr-1 mb-1"
            size="small"
          >
            {{ scope }}
          </el-tag>
        </template>
        <span v-else class="text-gray-400">-</span>
      </el-descriptions-item>
      <el-descriptions-item label="文件路径" :span="2">
        <el-tooltip :content="detailData.filePath" placement="top" v-if="detailData.filePath">
          <span class="truncate block max-w-[500px]">{{ detailData.filePath }}</span>
        </el-tooltip>
        <span v-else class="text-gray-400">-</span>
      </el-descriptions-item>
      <el-descriptions-item label="备份备注" :span="2">
        {{ detailData.remark || '-' }}
      </el-descriptions-item>
      <el-descriptions-item v-if="detailData.backupStatus === 2" label="错误信息" :span="2">
        <el-alert
          :title="detailData.errorMessage || '未知错误'"
          type="error"
          :closable="false"
          show-icon
        />
      </el-descriptions-item>
    </el-descriptions>

    <!-- 验证结果 -->
    <template v-if="detailData.backupStatus === 1">
      <el-divider content-position="left">
        <Icon icon="ep:document-checked" class="mr-5px" /> 备份验证
      </el-divider>
      <el-button type="primary" plain @click="handleValidate" :loading="validating">
        <Icon icon="ep:check" class="mr-5px" /> 验证备份完整性
      </el-button>
      <el-descriptions
        v-if="validationResult"
        :column="2"
        border
        class="mt-15px"
      >
        <el-descriptions-item label="验证状态" :span="1">
          <el-tag :type="validationResult.valid ? 'success' : 'danger'">
            {{ validationResult.valid ? '验证通过' : '验证失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="TDengine版本" :span="1">
          {{ validationResult.tdengineVersion || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="数据库列表" :span="2">
          <template v-if="validationResult.databases && validationResult.databases.length > 0">
            <el-tag
              v-for="db in validationResult.databases"
              :key="db"
              class="mr-1"
              size="small"
              type="info"
            >
              {{ db }}
            </el-tag>
          </template>
          <span v-else class="text-gray-400">-</span>
        </el-descriptions-item>
        <el-descriptions-item label="表列表" :span="2">
          <template v-if="validationResult.tables && validationResult.tables.length > 0">
            <el-tag
              v-for="table in validationResult.tables"
              :key="table"
              class="mr-1 mb-1"
              size="small"
            >
              {{ table }}
            </el-tag>
          </template>
          <span v-else class="text-gray-400">-</span>
        </el-descriptions-item>
      </el-descriptions>
    </template>

    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { BackupApi, BackupRecord, BackupValidation } from '@/api/iot/backup'
import dayjs from 'dayjs'

/** 备份详情对话框 */
defineOptions({ name: 'BackupDetailDialog' })

const message = useMessage()

const dialogVisible = ref(false)
const loading = ref(false)
const validating = ref(false)
const detailData = ref<Partial<BackupRecord>>({})
const validationResult = ref<BackupValidation | null>(null)

/** 格式化日期时间 */
const formatDateTime = (date: string | undefined): string => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

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

/** 打开弹窗 */
const open = async (id: number) => {
  dialogVisible.value = true
  validationResult.value = null
  await loadDetail(id)
}
defineExpose({ open })

/** 加载详情 */
const loadDetail = async (id: number) => {
  loading.value = true
  try {
    detailData.value = await BackupApi.getBackup(id)
  } finally {
    loading.value = false
  }
}

/** 验证备份 */
const handleValidate = async () => {
  validating.value = true
  try {
    validationResult.value = await BackupApi.validateBackup(detailData.value.id!)
    if (validationResult.value && validationResult.value.valid) {
      message.success('备份文件验证通过')
    } else {
      message.warning('备份文件验证失败：' + (validationResult.value?.errorMessage || '未知错误'))
    }
  } catch (error) {
    message.error('验证失败')
  } finally {
    validating.value = false
  }
}
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
