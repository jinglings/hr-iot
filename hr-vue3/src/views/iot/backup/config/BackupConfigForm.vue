<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="650px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="140px"
      v-loading="formLoading"
    >
      <el-form-item label="配置名称" prop="configName">
        <el-input v-model="formData.configName" placeholder="请输入配置名称" />
      </el-form-item>
      <el-form-item label="备份类型" prop="backupType">
        <el-radio-group v-model="formData.backupType" @change="handleBackupTypeChange">
          <el-radio-button :value="1">全量备份</el-radio-button>
          <el-radio-button :value="2">数据库备份</el-radio-button>
          <el-radio-button :value="3">表备份</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <!-- 数据库备份时选择数据库 -->
      <el-form-item v-if="formData.backupType === 2" label="选择数据库" prop="backupScope">
        <el-select
          v-model="formData.backupScope"
          multiple
          placeholder="请选择要备份的数据库"
          class="w-full"
          v-loading="databaseLoading"
        >
          <el-option
            v-for="db in databaseList"
            :key="db"
            :label="db"
            :value="db"
          />
        </el-select>
      </el-form-item>

      <!-- 表备份时选择数据库和表 -->
      <template v-if="formData.backupType === 3">
        <el-form-item label="选择数据库">
          <el-select
            v-model="selectedDatabase"
            placeholder="请选择数据库"
            class="w-full"
            @change="handleDatabaseChange"
            v-loading="databaseLoading"
          >
            <el-option
              v-for="db in databaseList"
              :key="db"
              :label="db"
              :value="db"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择超级表" prop="backupScope">
          <el-select
            v-model="formData.backupScope"
            multiple
            placeholder="请选择要备份的超级表"
            class="w-full"
            v-loading="tableLoading"
          >
            <el-option
              v-for="table in tableList"
              :key="table"
              :label="table"
              :value="table"
            />
          </el-select>
        </el-form-item>
      </template>

      <el-form-item label="Cron表达式" prop="cronExpression">
        <div class="w-full">
          <el-input v-model="formData.cronExpression" placeholder="请输入Cron表达式">
            <template #append>
              <el-dropdown @command="handleCronSelect">
                <el-button>
                  常用表达式 <Icon icon="ep:arrow-down" class="ml-5px" />
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="0 0 2 * * ?">每天凌晨2点</el-dropdown-item>
                    <el-dropdown-item command="0 0 3 * * ?">每天凌晨3点</el-dropdown-item>
                    <el-dropdown-item command="0 0 0 * * ?">每天0点</el-dropdown-item>
                    <el-dropdown-item command="0 0 0 ? * 1">每周一0点</el-dropdown-item>
                    <el-dropdown-item command="0 0 0 ? * 7">每周日0点</el-dropdown-item>
                    <el-dropdown-item command="0 0 0 1 * ?">每月1日0点</el-dropdown-item>
                    <el-dropdown-item command="0 0 0 L * ?">每月最后一天0点</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-input>
          <div class="text-gray-400 text-sm mt-5px">
            格式：秒 分 时 日 月 周 [年]
          </div>
        </div>
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="保留天数" prop="retentionDays">
            <el-input-number
              v-model="formData.retentionDays"
              :min="1"
              :max="365"
              class="w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="最大备份数" prop="maxBackupCount">
            <el-input-number
              v-model="formData.maxBackupCount"
              :min="1"
              :max="100"
              class="w-full"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="启用压缩" prop="compressionEnabled">
        <el-switch v-model="formData.compressionEnabled" />
        <span class="text-gray-400 ml-10px text-sm">压缩可减小备份文件大小</span>
      </el-form-item>

      <el-form-item label="是否启用" prop="enabled">
        <el-switch v-model="formData.enabled" />
      </el-form-item>

      <el-divider content-position="left">通知设置</el-divider>

      <el-form-item label="成功时通知" prop="notifyOnSuccess">
        <el-switch v-model="formData.notifyOnSuccess" />
      </el-form-item>

      <el-form-item label="失败时通知" prop="notifyOnFailure">
        <el-switch v-model="formData.notifyOnFailure" />
      </el-form-item>

      <el-form-item 
        v-if="formData.notifyOnSuccess || formData.notifyOnFailure" 
        label="通知邮箱" 
        prop="notifyEmails"
      >
        <el-input
          v-model="formData.notifyEmails"
          placeholder="请输入通知邮箱，多个用逗号分隔"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { BackupConfigApi, BackupConfig, BackupApi } from '@/api/iot/backup'

/** 备份配置表单 */
defineOptions({ name: 'BackupConfigForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const databaseLoading = ref(false)
const tableLoading = ref(false)
const formType = ref('')

const databaseList = ref<string[]>([])
const tableList = ref<string[]>([])
const selectedDatabase = ref('')

const formData = ref({
  id: undefined as number | undefined,
  configName: '',
  backupType: 1,
  backupScope: [] as string[],
  cronExpression: '0 0 2 * * ?',
  retentionDays: 7,
  maxBackupCount: 10,
  compressionEnabled: true,
  enabled: true,
  notifyOnSuccess: false,
  notifyOnFailure: true,
  notifyEmails: ''
})

const formRules = reactive({
  configName: [{ required: true, message: '配置名称不能为空', trigger: 'blur' }],
  backupType: [{ required: true, message: '请选择备份类型', trigger: 'change' }],
  cronExpression: [{ required: true, message: 'Cron表达式不能为空', trigger: 'blur' }],
  retentionDays: [{ required: true, message: '保留天数不能为空', trigger: 'blur' }],
  maxBackupCount: [{ required: true, message: '最大备份数不能为空', trigger: 'blur' }],
  backupScope: [
    {
      validator: (_rule: any, value: any, callback: any) => {
        if (formData.value.backupType !== 1 && (!value || value.length === 0)) {
          callback(new Error('请选择备份范围'))
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
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()

  // 加载数据库列表
  await loadDatabases()

  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      const data = await BackupConfigApi.getBackupConfig(id)
      formData.value = {
        ...data,
        backupScope: data.backupScope || []
      }
      // 如果是表备份，需要加载表列表
      if (data.backupType === 3 && data.backupScope && data.backupScope.length > 0) {
        // 尝试推断数据库（这里假设范围格式为 database.table）
        const firstScope = data.backupScope[0]
        if (firstScope.includes('.')) {
          selectedDatabase.value = firstScope.split('.')[0]
          await loadTables(selectedDatabase.value)
        }
      }
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

/** 加载数据库列表 */
const loadDatabases = async () => {
  databaseLoading.value = true
  try {
    databaseList.value = await BackupApi.getTDengineDatabases()
  } catch (error) {
    console.error('加载数据库列表失败:', error)
    databaseList.value = []
  } finally {
    databaseLoading.value = false
  }
}

/** 加载表列表 */
const loadTables = async (database: string) => {
  tableLoading.value = true
  try {
    tableList.value = await BackupApi.getTDengineStables(database)
  } catch (error) {
    console.error('加载表列表失败:', error)
    tableList.value = []
  } finally {
    tableLoading.value = false
  }
}

/** 备份类型变更 */
const handleBackupTypeChange = () => {
  formData.value.backupScope = []
  selectedDatabase.value = ''
  tableList.value = []
}

/** 数据库变更 */
const handleDatabaseChange = async (database: string) => {
  formData.value.backupScope = []
  if (database) {
    await loadTables(database)
  } else {
    tableList.value = []
  }
}

/** Cron表达式选择 */
const handleCronSelect = (cron: string) => {
  formData.value.cronExpression = cron
}

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    const data = formData.value as unknown as BackupConfig
    if (formType.value === 'create') {
      await BackupConfigApi.createBackupConfig(data)
      message.success(t('common.createSuccess'))
    } else {
      await BackupConfigApi.updateBackupConfig(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    configName: '',
    backupType: 1,
    backupScope: [],
    cronExpression: '0 0 2 * * ?',
    retentionDays: 7,
    maxBackupCount: 10,
    compressionEnabled: true,
    enabled: true,
    notifyOnSuccess: false,
    notifyOnFailure: true,
    notifyEmails: ''
  }
  selectedDatabase.value = ''
  tableList.value = []
  formRef.value?.resetFields()
}
</script>
