<template>
  <ContentWrap>
    <div class="control-logs-page">
      <div class="page-header">
        <h2>控制操作日志</h2>
      </div>
      
      <!-- 搜索栏 -->
      <el-form :model="queryParams" :inline="true" class="search-form">
        <el-form-item label="设备名称">
          <el-input v-model="queryParams.deviceName" placeholder="请输入设备名称" clearable />
        </el-form-item>
        <el-form-item label="命令名称">
          <el-input v-model="queryParams.commandName" placeholder="请输入命令名称" clearable />
        </el-form-item>
        <el-form-item label="执行状态">
          <el-select v-model="queryParams.executionStatus" placeholder="全部" clearable>
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <Icon icon="ep:search" class="mr-1" />
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-1" />
            重置
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 日志表格 -->
      <el-table :data="logs" v-loading="loading" stripe>
        <el-table-column prop="createTime" label="时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="deviceName" label="设备" min-width="120" />
        <el-table-column prop="commandName" label="命令" min-width="100" />
        <el-table-column prop="oldValue" label="原值" width="100" />
        <el-table-column prop="newValue" label="新值" width="100" />
        <el-table-column prop="executionStatus" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.executionStatus === 1 ? 'success' : 'danger'" size="small">
              {{ row.executionStatus === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="操作人" width="100" />
        <el-table-column prop="executionTime" label="耗时" width="80" align="center">
          <template #default="{ row }">
            {{ row.executionTime }}ms
          </template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="错误信息" min-width="150" show-overflow-tooltip />
      </el-table>
      
      <!-- 分页 -->
      <Pagination
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ScadaApi, type ScadaControlLogVO } from '@/api/iot/scada'

defineOptions({ name: 'IotScadaControlLogs' })

const loading = ref(false)
const logs = ref<ScadaControlLogVO[]>([])
const total = ref(0)
const dateRange = ref<[string, string] | null>(null)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  deviceName: '',
  commandName: '',
  executionStatus: undefined as number | undefined,
  startTime: '',
  endTime: ''
})

// 获取日志列表
const getList = async () => {
  loading.value = true
  try {
    if (dateRange.value) {
      queryParams.startTime = dateRange.value[0]
      queryParams.endTime = dateRange.value[1]
    } else {
      queryParams.startTime = ''
      queryParams.endTime = ''
    }
    
    const data = await ScadaApi.getControlLogPage(queryParams)
    logs.value = data.list
    total.value = data.total
  } catch (error) {
    console.error('获取控制日志失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.deviceName = ''
  queryParams.commandName = ''
  queryParams.executionStatus = undefined
  dateRange.value = null
  handleQuery()
}

// 格式化日期时间
const formatDateTime = (date: Date): string => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.control-logs-page {
  .page-header {
    margin-bottom: 20px;
    
    h2 {
      margin: 0;
      font-size: 18px;
      color: #303133;
    }
  }
  
  .search-form {
    margin-bottom: 20px;
  }
}
</style>
