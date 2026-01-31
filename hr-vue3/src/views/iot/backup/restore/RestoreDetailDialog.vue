<template>
  <Dialog title="恢复详情" v-model="dialogVisible" width="700px">
    <el-descriptions :column="2" border>
      <el-descriptions-item label="恢复编号" :span="1">
        {{ detailData.id }}
      </el-descriptions-item>
      <el-descriptions-item label="备份编号" :span="1">
        {{ detailData.backupId }}
      </el-descriptions-item>
      <el-descriptions-item label="备份名称" :span="2">
        {{ detailData.backupName || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="恢复类型" :span="1">
        <el-tag v-if="detailData.restoreType === 1" type="primary">完整恢复</el-tag>
        <el-tag v-else-if="detailData.restoreType === 2" type="success">选择性恢复</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="恢复状态" :span="1">
        <el-tag v-if="detailData.restoreStatus === 0" type="warning">
          <Icon icon="ep:loading" class="mr-3px animate-spin" /> 恢复中
        </el-tag>
        <el-tag v-else-if="detailData.restoreStatus === 1" type="success">成功</el-tag>
        <el-tag v-else-if="detailData.restoreStatus === 2" type="danger">失败</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="数据条数" :span="1">
        {{ detailData.recordCount ? detailData.recordCount.toLocaleString() : '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="耗时" :span="1">
        {{ detailData.duration ? formatDuration(detailData.duration) : '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="开始时间" :span="1">
        {{ formatDateTime(detailData.startTime) }}
      </el-descriptions-item>
      <el-descriptions-item label="结束时间" :span="1">
        {{ formatDateTime(detailData.endTime) }}
      </el-descriptions-item>
      <el-descriptions-item label="恢复目标" :span="2">
        <template v-if="detailData.restoreTarget && detailData.restoreTarget.length > 0">
          <el-tag
            v-for="target in detailData.restoreTarget"
            :key="target"
            class="mr-1 mb-1"
            size="small"
          >
            {{ target }}
          </el-tag>
        </template>
        <span v-else class="text-gray-400">全部</span>
      </el-descriptions-item>
      <el-descriptions-item label="创建者" :span="1">
        {{ detailData.creator || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="创建时间" :span="1">
        {{ formatDateTime(detailData.createTime) }}
      </el-descriptions-item>
      <el-descriptions-item label="恢复备注" :span="2">
        {{ detailData.remark || '-' }}
      </el-descriptions-item>
      <el-descriptions-item v-if="detailData.restoreStatus === 2" label="错误信息" :span="2">
        <el-alert
          :title="detailData.errorMessage || '未知错误'"
          type="error"
          :closable="false"
          show-icon
        />
      </el-descriptions-item>
    </el-descriptions>

    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { RestoreRecord } from '@/api/iot/backup'
import dayjs from 'dayjs'

/** 恢复详情对话框 */
defineOptions({ name: 'RestoreDetailDialog' })

const dialogVisible = ref(false)
const detailData = ref<Partial<RestoreRecord>>({})

/** 格式化日期时间 */
const formatDateTime = (date: string | undefined): string => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
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
const open = (data: RestoreRecord) => {
  dialogVisible.value = true
  detailData.value = data
}
defineExpose({ open })
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
