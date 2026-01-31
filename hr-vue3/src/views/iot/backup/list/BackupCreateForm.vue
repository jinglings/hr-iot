<template>
  <Dialog title="创建备份" v-model="dialogVisible" width="600px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
    >
      <el-form-item label="备份名称" prop="backupName">
        <el-input v-model="formData.backupName" placeholder="请输入备份名称，留空自动生成" />
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
        <el-form-item label="选择数据库" prop="selectedDatabase">
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

      <el-form-item label="备份备注" prop="remark">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备份备注"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :loading="formLoading">
        <Icon icon="ep:check" class="mr-5px" /> 开始备份
      </el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { BackupApi } from '@/api/iot/backup'

/** 创建备份表单 */
defineOptions({ name: 'BackupCreateForm' })

const message = useMessage()

const dialogVisible = ref(false)
const formLoading = ref(false)
const databaseLoading = ref(false)
const tableLoading = ref(false)

const databaseList = ref<string[]>([])
const tableList = ref<string[]>([])
const selectedDatabase = ref('')

const formData = ref({
  backupName: '',
  backupType: 1,
  backupScope: [] as string[],
  remark: ''
})

const formRules = reactive({
  backupType: [{ required: true, message: '请选择备份类型', trigger: 'change' }],
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
const open = async () => {
  dialogVisible.value = true
  resetForm()
  await loadDatabases()
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

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    await BackupApi.createBackup(formData.value)
    message.success('备份任务已创建，正在后台执行')
    dialogVisible.value = false
    emit('success')
  } catch (error) {
    console.error('创建备份失败:', error)
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    backupName: '',
    backupType: 1,
    backupScope: [],
    remark: ''
  }
  selectedDatabase.value = ''
  tableList.value = []
  formRef.value?.resetFields()
}
</script>
