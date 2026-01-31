<template>
  <Dialog title="数据恢复" v-model="dialogVisible" width="600px">
    <el-alert
      title="警告：数据恢复操作可能会覆盖现有数据，请谨慎操作！"
      type="warning"
      show-icon
      :closable="false"
      class="mb-20px"
    />
    
    <el-descriptions :column="2" border class="mb-20px">
      <el-descriptions-item label="备份名称" :span="2">
        {{ backupInfo.backupName }}
      </el-descriptions-item>
      <el-descriptions-item label="备份时间" :span="1">
        {{ formatDateTime(backupInfo.startTime) }}
      </el-descriptions-item>
      <el-descriptions-item label="数据条数" :span="1">
        {{ backupInfo.recordCount ? backupInfo.recordCount.toLocaleString() : '-' }}
      </el-descriptions-item>
    </el-descriptions>

    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
    >
      <el-form-item label="恢复类型" prop="restoreType">
        <el-radio-group v-model="formData.restoreType">
          <el-radio-button :value="1">完整恢复</el-radio-button>
          <el-radio-button :value="2">选择性恢复</el-radio-button>
        </el-radio-group>
      </el-form-item>
      
      <el-form-item v-if="formData.restoreType === 2" label="恢复目标" prop="restoreTarget">
        <el-select
          v-model="formData.restoreTarget"
          multiple
          placeholder="请选择要恢复的数据库/表"
          class="w-full"
        >
          <el-option
            v-for="scope in backupInfo.backupScope || []"
            :key="scope"
            :label="scope"
            :value="scope"
          />
        </el-select>
        <div class="text-gray-400 text-sm mt-5px">
          选择性恢复仅支持备份范围内的数据库/表
        </div>
      </el-form-item>

      <el-form-item label="恢复备注" prop="remark">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入恢复备注"
        />
      </el-form-item>
    </el-form>

    <el-alert
      type="info"
      show-icon
      :closable="false"
      class="mt-15px"
    >
      <template #title>
        <span>恢复操作为异步执行，提交后请前往【恢复记录】页面查看进度</span>
      </template>
    </el-alert>

    <template #footer>
      <el-button @click="submitForm" type="primary" :loading="formLoading">
        <Icon icon="ep:check" class="mr-5px" /> 确认恢复
      </el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { RestoreApi, BackupRecord } from '@/api/iot/backup'
import dayjs from 'dayjs'

/** 恢复表单 */
defineOptions({ name: 'RestoreForm' })

const message = useMessage()

const dialogVisible = ref(false)
const formLoading = ref(false)
const backupInfo = ref<Partial<BackupRecord>>({})

/** 格式化日期时间 */
const formatDateTime = (date: string | undefined): string => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const formData = ref({
  backupId: undefined as number | undefined,
  restoreType: 1,
  restoreTarget: [] as string[],
  remark: ''
})

const formRules = reactive({
  restoreType: [{ required: true, message: '请选择恢复类型', trigger: 'change' }],
  restoreTarget: [
    {
      validator: (_rule: any, value: any, callback: any) => {
        if (formData.value.restoreType === 2 && (!value || value.length === 0)) {
          callback(new Error('请选择恢复目标'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
})

const formRef = ref()

/** 打开弹窗 */
const open = (backup: BackupRecord) => {
  dialogVisible.value = true
  backupInfo.value = backup
  resetForm()
  formData.value.backupId = backup.id
}
defineExpose({ open })

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  
  // 二次确认
  try {
    await ElMessageBox.confirm(
      '确定要执行数据恢复吗？此操作可能会覆盖现有数据！',
      '确认恢复',
      {
        confirmButtonText: '确定恢复',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  formLoading.value = true
  try {
    await RestoreApi.executeRestore(formData.value)
    message.success('恢复任务已提交')
    dialogVisible.value = false
    emit('success')
  } catch (error) {
    console.error('提交恢复任务失败:', error)
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    backupId: undefined,
    restoreType: 1,
    restoreTarget: [],
    remark: ''
  }
  formRef.value?.resetFields()
}
</script>
