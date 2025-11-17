<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="80px"
    >
      <el-form-item label="计量点" prop="meterId">
        <el-select
          v-model="queryParams.meterId"
          placeholder="请选择计量点"
          filterable
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="meter in meterList"
            :key="meter.id"
            :label="meter.name + ' (' + meter.code + ')'"
            :value="meter.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="能源类型" prop="energyTypeId">
        <el-select v-model="queryParams.energyTypeId" placeholder="请选择能源类型" clearable class="!w-240px">
          <el-option
            v-for="type in energyTypeList"
            :key="type.id"
            :label="type.name"
            :value="type.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="所属建筑" prop="buildingId">
        <el-select v-model="queryParams.buildingId" placeholder="请选择建筑" clearable class="!w-240px">
          <el-option
            v-for="building in buildingList"
            :key="building.id"
            :label="building.name"
            :value="building.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="时间范围" prop="timeRange">
        <el-date-picker
          v-model="queryParams.timeRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-340px"
        />
      </el-form-item>
      <el-form-item label="数据质量" prop="dataQuality">
        <el-select v-model="queryParams.dataQuality" placeholder="请选择数据质量" clearable class="!w-240px">
          <el-option label="正常" :value="0" />
          <el-option label="异常" :value="1" />
          <el-option label="估算" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="handleShowChart">
          <Icon icon="ep:data-line" class="mr-5px" /> 查看图表
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 图表展示 -->
  <ContentWrap v-if="showChart">
    <el-row :gutter="20">
      <el-col :span="24">
        <div class="chart-header">
          <h3>能源消耗趋势图</h3>
          <el-radio-group v-model="chartInterval" size="small" @change="handleChartIntervalChange">
            <el-radio-button label="1m">1分钟</el-radio-button>
            <el-radio-button label="1h">1小时</el-radio-button>
            <el-radio-button label="1d">1天</el-radio-button>
          </el-radio-group>
        </div>
        <Echart :options="chartOptions" :height="400" />
      </el-col>
    </el-row>
  </ContentWrap>

  <!-- 数据列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="时间" align="center" prop="ts" width="180px">
        <template #default="scope">
          {{ formatDate(scope.row.ts, 'YYYY-MM-DD HH:mm:ss') }}
        </template>
      </el-table-column>
      <el-table-column label="计量点" align="center" prop="meterName" min-width="150" />
      <el-table-column label="能源类型" align="center" prop="energyTypeName" width="120" />
      <el-table-column label="建筑" align="center" prop="buildingName" width="120" />
      <el-table-column label="区域" align="center" prop="areaName" width="100" />
      <el-table-column label="楼层" align="center" prop="floorName" width="100" />
      <el-table-column label="房间" align="center" prop="roomName" width="120" />
      <el-table-column label="瞬时功率" align="center" prop="instantPower" width="120">
        <template #default="scope">
          {{ scope.row.instantPower ? scope.row.instantPower.toFixed(2) : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="累计值" align="center" prop="cumulativeValue" width="120">
        <template #default="scope">
          {{ scope.row.cumulativeValue ? scope.row.cumulativeValue.toFixed(2) : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="电压(V)" align="center" prop="voltage" width="100">
        <template #default="scope">
          {{ scope.row.voltage ? scope.row.voltage.toFixed(2) : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="电流(A)" align="center" prop="current" width="100">
        <template #default="scope">
          {{ scope.row.current ? scope.row.current.toFixed(2) : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="功率因数" align="center" prop="powerFactor" width="100">
        <template #default="scope">
          {{ scope.row.powerFactor ? scope.row.powerFactor.toFixed(2) : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="数据质量" align="center" prop="dataQuality" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.dataQuality === 0" type="success">正常</el-tag>
          <el-tag v-else-if="scope.row.dataQuality === 1" type="danger">异常</el-tag>
          <el-tag v-else-if="scope.row.dataQuality === 2" type="warning">估算</el-tag>
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
</template>

<script setup lang="ts">
import { formatDate } from '@/utils/formatTime'
import { getIotEnergyDataPage, getIotEnergyDataAggregate, IotEnergyRealtimeDataVO } from '@/api/iot/energy/data'
import { getIotEnergyMeterSimpleList } from '@/api/iot/energy/meter'
import { getIotEnergyTypeSimpleList } from '@/api/iot/energy/energyType'
import { getIotEnergyBuildingSimpleList } from '@/api/iot/energy/building'
import { Echart } from '@/components/Echart'

/** IoT 能源数据查询 */
defineOptions({ name: 'IotEnergyData' })

const message = useMessage() // 消息弹窗

const loading = ref(true) // 列表的加载中
const list = ref<IotEnergyRealtimeDataVO[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const meterList = ref([]) // 计量点列表
const energyTypeList = ref([]) // 能源类型列表
const buildingList = ref([]) // 建筑列表
const showChart = ref(false) // 是否显示图表
const chartInterval = ref('1h') // 图表聚合间隔
const chartOptions = ref({}) // 图表配置
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  meterId: undefined,
  energyTypeId: undefined,
  buildingId: undefined,
  timeRange: [],
  dataQuality: undefined
})
const queryFormRef = ref() // 搜索的表单

/** 查询计量点列表 */
const getMeterList = async () => {
  meterList.value = await getIotEnergyMeterSimpleList()
}

/** 查询能源类型列表 */
const getEnergyTypeList = async () => {
  energyTypeList.value = await getIotEnergyTypeSimpleList()
}

/** 查询建筑列表 */
const getBuildingList = async () => {
  buildingList.value = await getIotEnergyBuildingSimpleList()
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const params: any = {
      ...queryParams,
      startTime: queryParams.timeRange?.[0],
      endTime: queryParams.timeRange?.[1]
    }
    delete params.timeRange
    const data = await getIotEnergyDataPage(params)
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

/** 显示图表 */
const handleShowChart = async () => {
  if (!queryParams.meterId) {
    message.warning('请先选择计量点')
    return
  }
  if (!queryParams.timeRange || queryParams.timeRange.length !== 2) {
    message.warning('请选择时间范围')
    return
  }

  showChart.value = true
  await loadChartData()
}

/** 图表间隔变化 */
const handleChartIntervalChange = () => {
  loadChartData()
}

/** 加载图表数据 */
const loadChartData = async () => {
  try {
    const params = {
      meterIds: [queryParams.meterId],
      startTime: queryParams.timeRange[0],
      endTime: queryParams.timeRange[1],
      interval: chartInterval.value
    }
    const data = await getIotEnergyDataAggregate(params)

    // 处理图表数据
    const timeData = data.map((item: any) => formatDate(item.ts, 'MM-DD HH:mm'))
    const avgData = data.map((item: any) => item.avgValue?.toFixed(2) || 0)
    const maxData = data.map((item: any) => item.maxValue?.toFixed(2) || 0)
    const minData = data.map((item: any) => item.minValue?.toFixed(2) || 0)

    chartOptions.value = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross',
          label: {
            backgroundColor: '#6a7985'
          }
        }
      },
      legend: {
        data: ['平均值', '最大值', '最小值']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: timeData
      },
      yAxis: {
        type: 'value',
        name: '能耗值'
      },
      series: [
        {
          name: '平均值',
          type: 'line',
          smooth: true,
          data: avgData,
          itemStyle: {
            color: '#5470c6'
          }
        },
        {
          name: '最大值',
          type: 'line',
          smooth: true,
          data: maxData,
          itemStyle: {
            color: '#ee6666'
          }
        },
        {
          name: '最小值',
          type: 'line',
          smooth: true,
          data: minData,
          itemStyle: {
            color: '#91cc75'
          }
        }
      ]
    }
  } catch (error) {
    console.error('加载图表数据失败:', error)
    message.error('加载图表数据失败')
  }
}

/** 初始化 **/
onMounted(() => {
  getMeterList()
  getEnergyTypeList()
  getBuildingList()
  getList()
})
</script>

<style scoped>
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 10px;
}

.chart-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}
</style>
