<template>
  <div class="energy-cost">
    <!-- 查询条件 -->
    <ContentWrap>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="80px"
      >
        <el-form-item label="设备名称" prop="deviceName">
          <el-input
            v-model="queryParams.deviceName"
            placeholder="请输入设备名称"
            clearable
            class="!w-200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="备注名称" prop="nickname">
          <el-input
            v-model="queryParams.nickname"
            placeholder="请输入备注名称"
            clearable
            class="!w-200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="产品" prop="productId">
          <el-select v-model="queryParams.productId" placeholder="请选择产品" clearable class="!w-200px">
            <el-option
              v-for="product in productList"
              :key="product.id"
              :label="product.name"
              :value="product.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="queryParams.startTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择开始时间"
            class="!w-220px"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="queryParams.endTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择结束时间"
            class="!w-220px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" /> 查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
          <el-button type="success" @click="handleExport" :loading="exportLoading">
            <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <!-- 汇总信息 -->
    <el-row :gutter="16" class="summary-row" v-if="list.length > 0">
      <el-col :span="6">
        <div class="summary-card">
          <div class="summary-label">设备总数</div>
          <div class="summary-value">{{ total }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="summary-card">
          <div class="summary-label">总消耗 (kWh)</div>
          <div class="summary-value">{{ totalConsumption }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="summary-card">
          <div class="summary-label">总电费 (元)</div>
          <div class="summary-value cost">{{ totalCost }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="summary-card">
          <div class="summary-label">电费单价 (元/度)</div>
          <div class="summary-value">1.0616</div>
        </div>
      </el-col>
    </el-row>

    <!-- 数据列表 -->
    <ContentWrap>
      <el-table
        v-loading="loading"
        :data="list"
        :stripe="true"
        border
        show-summary
        :summary-method="getSummaries"
      >
        <el-table-column label="设备名称" align="center" prop="deviceName" min-width="120" fixed="left">
          <template #default="scope">
            <div>{{ scope.row.deviceName }}</div>
            <div v-if="scope.row.nickname" class="text-gray-400 text-xs">{{ scope.row.nickname }}</div>
          </template>
        </el-table-column>
        <el-table-column label="产品名称" align="center" prop="productName" min-width="100" />
        <el-table-column label="开始读数 (kWh)" align="right" prop="startEnergy" min-width="130">
          <template #default="scope">
            <template v-if="scope.row.startEnergy != null">
              <div>{{ formatNum(scope.row.startEnergy) }}</div>
              <div class="text-gray-400 text-xs">{{ formatTime(scope.row.startEnergyTime) }}</div>
            </template>
            <span v-else class="text-gray-300">--</span>
          </template>
        </el-table-column>
        <el-table-column label="结束读数 (kWh)" align="right" prop="endEnergy" min-width="130">
          <template #default="scope">
            <template v-if="scope.row.endEnergy != null">
              <div>{{ formatNum(scope.row.endEnergy) }}</div>
              <div class="text-gray-400 text-xs">{{ formatTime(scope.row.endEnergyTime) }}</div>
            </template>
            <span v-else class="text-gray-300">--</span>
          </template>
        </el-table-column>
        <el-table-column label="原始消耗 (kWh)" align="right" prop="rawConsumption" min-width="120">
          <template #default="scope">
            <span v-if="scope.row.rawConsumption != null">{{ formatNum(scope.row.rawConsumption) }}</span>
            <span v-else class="text-gray-300">--</span>
          </template>
        </el-table-column>
        <el-table-column label="倍率" align="center" prop="ratio" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.ratio && scope.row.ratio != 1" type="warning" size="small">
              {{ scope.row.ratio }}
            </el-tag>
            <span v-else>1</span>
          </template>
        </el-table-column>
        <el-table-column label="实际消耗 (kWh)" align="right" prop="consumption" min-width="120">
          <template #default="scope">
            <span v-if="scope.row.consumption != null" class="font-bold">
              {{ formatNum(scope.row.consumption) }}
            </span>
            <span v-else class="text-gray-300">--</span>
          </template>
        </el-table-column>
        <el-table-column label="单价 (元/度)" align="right" prop="unitPrice" width="100">
          <template #default="scope">
            {{ scope.row.unitPrice || '1.0616' }}
          </template>
        </el-table-column>
        <el-table-column label="电费 (元)" align="right" prop="cost" min-width="110">
          <template #default="scope">
            <span v-if="scope.row.cost != null" class="font-bold text-red-500">
              {{ formatNum(scope.row.cost) }}
            </span>
            <span v-else class="text-gray-300">--</span>
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
  </div>
</template>

<script setup lang="ts">
import { DeviceApi } from '@/api/iot/device/device'
import { ProductApi } from '@/api/iot/product/product'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'IotEnergyCost' })

const message = useMessage()

const loading = ref(false)
const exportLoading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const productList = ref<any[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  deviceName: undefined,
  nickname: undefined,
  productId: undefined,
  groupId: undefined,
  startTime: undefined as string | undefined,
  endTime: undefined as string | undefined
})
const queryFormRef = ref()

/** 格式化数字（保留2位小数） */
const formatNum = (val: number | string | null | undefined): string => {
  if (val === null || val === undefined) return '--'
  const num = typeof val === 'string' ? parseFloat(val) : val
  if (isNaN(num)) return '--'
  return num.toFixed(2)
}

/** 格式化时间 */
const formatTime = (time: any): string => {
  if (!time) return ''
  return formatDate(time, 'YYYY-MM-DD HH:mm:ss')
}

/** 汇总统计 */
const totalConsumption = computed(() => {
  const sum = list.value.reduce((acc, item) => {
    return acc + (item.consumption ? parseFloat(item.consumption) : 0)
  }, 0)
  return sum.toFixed(2)
})

const totalCost = computed(() => {
  const sum = list.value.reduce((acc, item) => {
    return acc + (item.cost ? parseFloat(item.cost) : 0)
  }, 0)
  return sum.toFixed(2)
})

/** 表格合计行 */
const getSummaries = (param: any) => {
  const { columns, data } = param
  const sums: string[] = []
  columns.forEach((_col: any, index: number) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    const prop = columns[index].property
    if (['consumption', 'cost', 'rawConsumption'].includes(prop)) {
      const total = data.reduce((sum: number, row: any) => {
        const val = row[prop] ? parseFloat(row[prop]) : 0
        return sum + (isNaN(val) ? 0 : val)
      }, 0)
      sums[index] = total.toFixed(2)
    } else {
      sums[index] = ''
    }
  })
  return sums
}

/** 获取产品列表 */
const getProductList = async () => {
  try {
    productList.value = await ProductApi.getSimpleProductList()
  } catch (error) {
    console.error('获取产品列表失败:', error)
  }
}

/** 查询列表 */
const getList = async () => {
  if (!queryParams.startTime || !queryParams.endTime) {
    message.warning('请选择开始时间和结束时间')
    return
  }
  loading.value = true
  try {
    const data = await DeviceApi.getDeviceEnergyCost(queryParams)
    list.value = data.list
    total.value = data.total
  } catch (error) {
    console.error('获取电费数据失败:', error)
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
  queryFormRef.value?.resetFields()
  list.value = []
  total.value = 0
}

/** 导出（简易CSV导出） */
const handleExport = async () => {
  if (list.value.length === 0) {
    message.warning('暂无数据可导出')
    return
  }
  exportLoading.value = true
  try {
    const headers = ['设备名称', '备注名称', '产品名称', '开始读数(kWh)', '结束读数(kWh)', '原始消耗(kWh)', '倍率', '实际消耗(kWh)', '单价(元/度)', '电费(元)']
    const rows = list.value.map(item => [
      item.deviceName || '',
      item.nickname || '',
      item.productName || '',
      item.startEnergy ?? '',
      item.endEnergy ?? '',
      item.rawConsumption ?? '',
      item.ratio ?? '1',
      item.consumption ?? '',
      item.unitPrice ?? '1.0616',
      item.cost ?? ''
    ])

    const BOM = '\uFEFF'
    const csvContent = BOM + [headers.join(','), ...rows.map(r => r.join(','))].join('\n')
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `电费计算_${queryParams.startTime}_${queryParams.endTime}.csv`
    link.click()
    URL.revokeObjectURL(link.href)
    message.success('导出成功')
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 */
onMounted(() => {
  getProductList()
  // 设置默认时间范围：本月1号 00:00:00 到当前时间
  const now = new Date()
  const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1)
  queryParams.startTime = formatDate(startOfMonth, 'YYYY-MM-DD HH:mm:ss')
  queryParams.endTime = formatDate(now, 'YYYY-MM-DD HH:mm:ss')
})
</script>

<style scoped lang="scss">
.energy-cost {
  .summary-row {
    margin: 0 0 16px 0;
    padding: 0 10px;
  }

  .summary-card {
    background: #fff;
    border-radius: 8px;
    padding: 16px 20px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
    text-align: center;

    .summary-label {
      font-size: 13px;
      color: #909399;
      margin-bottom: 8px;
    }

    .summary-value {
      font-size: 24px;
      font-weight: 700;
      color: #303133;

      &.cost {
        color: #f56c6c;
      }
    }
  }

  .font-bold {
    font-weight: 700;
  }

  .text-red-500 {
    color: #f56c6c;
  }

  .text-gray-300 {
    color: #c0c4cc;
  }

  .text-gray-400 {
    color: #909399;
  }

  .text-xs {
    font-size: 12px;
  }
}
</style>
