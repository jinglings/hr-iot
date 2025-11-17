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
      <el-form-item label="统计维度" prop="dimension">
        <el-select v-model="queryParams.dimension" placeholder="请选择统计维度" class="!w-240px" @change="handleDimensionChange">
          <el-option label="按计量点统计" value="meter" />
          <el-option label="按建筑统计" value="building" />
          <el-option label="按能源类型统计" value="energyType" />
        </el-select>
      </el-form-item>
      <el-form-item label="计量点" prop="meterId" v-if="queryParams.dimension === 'meter'">
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
      <el-form-item label="建筑" prop="buildingId" v-if="queryParams.dimension === 'building'">
        <el-select v-model="queryParams.buildingId" placeholder="请选择建筑" clearable class="!w-240px">
          <el-option
            v-for="building in buildingList"
            :key="building.id"
            :label="building.name"
            :value="building.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="能源类型" prop="energyTypeId" v-if="queryParams.dimension === 'energyType'">
        <el-select v-model="queryParams.energyTypeId" placeholder="请选择能源类型" clearable class="!w-240px">
          <el-option
            v-for="type in energyTypeList"
            :key="type.id"
            :label="type.name"
            :value="type.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="统计周期" prop="period">
        <el-select v-model="queryParams.period" placeholder="请选择统计周期" class="!w-240px">
          <el-option label="小时" value="hour" />
          <el-option label="天" value="day" />
          <el-option label="月" value="month" />
          <el-option label="年" value="year" />
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
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="handleShowChart">
          <Icon icon="ep:data-analysis" class="mr-5px" /> 统计分析
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 统计图表 -->
  <ContentWrap v-if="showChart">
    <el-row :gutter="20">
      <!-- 能耗趋势图 -->
      <el-col :span="24">
        <div class="chart-header">
          <h3>能耗趋势统计</h3>
        </div>
        <Echart :options="trendChartOptions" :height="350" />
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 能耗占比饼图 -->
      <el-col :span="12">
        <div class="chart-header">
          <h3>能耗占比分析</h3>
        </div>
        <Echart :options="pieChartOptions" :height="300" />
      </el-col>
      <!-- 折标煤与碳排放柱状图 -->
      <el-col :span="12">
        <div class="chart-header">
          <h3>折标煤与碳排放统计</h3>
        </div>
        <Echart :options="barChartOptions" :height="300" />
      </el-col>
    </el-row>
  </ContentWrap>

  <!-- 统计数据列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="周期" align="center" prop="periodValue" width="180" />
      <el-table-column label="计量点" align="center" prop="meterName" min-width="150" v-if="queryParams.dimension === 'meter'" />
      <el-table-column label="建筑" align="center" prop="buildingName" min-width="150" v-if="queryParams.dimension === 'building'" />
      <el-table-column label="能源类型" align="center" prop="energyTypeName" width="120" />
      <el-table-column label="总能耗" align="center" prop="totalEnergy" width="120">
        <template #default="scope">
          {{ scope.row.totalEnergy ? scope.row.totalEnergy.toFixed(2) : '0.00' }}
        </template>
      </el-table-column>
      <el-table-column label="单位" align="center" prop="unit" width="80" />
      <el-table-column label="平均功率" align="center" prop="avgPower" width="120">
        <template #default="scope">
          {{ scope.row.avgPower ? scope.row.avgPower.toFixed(2) : '0.00' }}
        </template>
      </el-table-column>
      <el-table-column label="最大功率" align="center" prop="maxPower" width="120">
        <template #default="scope">
          {{ scope.row.maxPower ? scope.row.maxPower.toFixed(2) : '0.00' }}
        </template>
      </el-table-column>
      <el-table-column label="最小功率" align="center" prop="minPower" width="120">
        <template #default="scope">
          {{ scope.row.minPower ? scope.row.minPower.toFixed(2) : '0.00' }}
        </template>
      </el-table-column>
      <el-table-column label="折标煤(吨)" align="center" prop="coalConsumption" width="120">
        <template #default="scope">
          {{ scope.row.coalConsumption ? scope.row.coalConsumption.toFixed(4) : '0.0000' }}
        </template>
      </el-table-column>
      <el-table-column label="碳排放(吨CO₂)" align="center" prop="carbonEmission" width="140">
        <template #default="scope">
          {{ scope.row.carbonEmission ? scope.row.carbonEmission.toFixed(4) : '0.0000' }}
        </template>
      </el-table-column>
      <el-table-column
        label="统计时间"
        align="center"
        prop="startTime"
        width="180px"
      >
        <template #default="scope">
          {{ formatDate(scope.row.startTime, 'YYYY-MM-DD HH:mm') }}
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
import {
  IotEnergyStatisticsVO,
  getIotEnergyStatisticsPage,
  getIotEnergyStatsByMeter,
  getIotEnergyStatsByBuilding,
  getIotEnergyStatsByType
} from '@/api/iot/energy/statistics'
import { getIotEnergyMeterSimpleList } from '@/api/iot/energy/meter'
import { getIotEnergyTypeSimpleList } from '@/api/iot/energy/energyType'
import { getIotEnergyBuildingSimpleList } from '@/api/iot/energy/building'
import { Echart } from '@/components/Echart'

/** IoT 能源统计分析 */
defineOptions({ name: 'IotEnergyStatistics' })

const message = useMessage() // 消息弹窗

const loading = ref(true) // 列表的加载中
const list = ref<IotEnergyStatisticsVO[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const meterList = ref([]) // 计量点列表
const energyTypeList = ref([]) // 能源类型列表
const buildingList = ref([]) // 建筑列表
const showChart = ref(false) // 是否显示图表
const trendChartOptions = ref({}) // 趋势图表配置
const pieChartOptions = ref({}) // 饼图配置
const barChartOptions = ref({}) // 柱状图配置
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  dimension: 'meter', // meter, building, energyType
  meterId: undefined,
  buildingId: undefined,
  energyTypeId: undefined,
  period: 'day',
  timeRange: []
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

/** 统计维度变化 */
const handleDimensionChange = () => {
  queryParams.meterId = undefined
  queryParams.buildingId = undefined
  queryParams.energyTypeId = undefined
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
    delete params.dimension
    const data = await getIotEnergyStatisticsPage(params)
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

/** 显示统计图表 */
const handleShowChart = async () => {
  // 验证参数
  if (queryParams.dimension === 'meter' && !queryParams.meterId) {
    message.warning('请选择计量点')
    return
  }
  if (queryParams.dimension === 'building' && !queryParams.buildingId) {
    message.warning('请选择建筑')
    return
  }
  if (queryParams.dimension === 'energyType' && !queryParams.energyTypeId) {
    message.warning('请选择能源类型')
    return
  }
  if (!queryParams.timeRange || queryParams.timeRange.length !== 2) {
    message.warning('请选择时间范围')
    return
  }

  showChart.value = true
  await loadChartData()
}

/** 加载图表数据 */
const loadChartData = async () => {
  try {
    let data: IotEnergyStatisticsVO[] = []

    const params = {
      period: queryParams.period,
      startTime: queryParams.timeRange[0],
      endTime: queryParams.timeRange[1]
    }

    // 根据不同维度获取统计数据
    if (queryParams.dimension === 'meter') {
      data = await getIotEnergyStatsByMeter({ ...params, meterId: queryParams.meterId })
    } else if (queryParams.dimension === 'building') {
      data = await getIotEnergyStatsByBuilding({ ...params, buildingId: queryParams.buildingId })
    } else if (queryParams.dimension === 'energyType') {
      data = await getIotEnergyStatsByType({ ...params, energyTypeId: queryParams.energyTypeId })
    }

    // 处理趋势图数据
    const timeData = data.map((item: any) => item.periodValue)
    const energyData = data.map((item: any) => item.totalEnergy?.toFixed(2) || 0)
    const avgPowerData = data.map((item: any) => item.avgPower?.toFixed(2) || 0)

    trendChartOptions.value = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross'
        }
      },
      legend: {
        data: ['总能耗', '平均功率']
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
      yAxis: [
        {
          type: 'value',
          name: '总能耗',
          position: 'left'
        },
        {
          type: 'value',
          name: '平均功率',
          position: 'right'
        }
      ],
      series: [
        {
          name: '总能耗',
          type: 'line',
          smooth: true,
          data: energyData,
          itemStyle: {
            color: '#5470c6'
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(84, 112, 198, 0.5)' },
                { offset: 1, color: 'rgba(84, 112, 198, 0.1)' }
              ]
            }
          }
        },
        {
          name: '平均功率',
          type: 'line',
          smooth: true,
          yAxisIndex: 1,
          data: avgPowerData,
          itemStyle: {
            color: '#91cc75'
          }
        }
      ]
    }

    // 处理饼图数据（能耗占比）
    const pieData = data.map((item: any) => ({
      name: item.periodValue,
      value: item.totalEnergy || 0
    }))

    pieChartOptions.value = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '能耗占比',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            formatter: '{b}: {d}%'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 16,
              fontWeight: 'bold'
            }
          },
          data: pieData
        }
      ]
    }

    // 处理柱状图数据（折标煤与碳排放）
    const coalData = data.map((item: any) => item.coalConsumption?.toFixed(4) || 0)
    const carbonData = data.map((item: any) => item.carbonEmission?.toFixed(4) || 0)

    barChartOptions.value = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      legend: {
        data: ['折标煤(吨)', '碳排放(吨CO₂)']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: timeData
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '折标煤(吨)',
          type: 'bar',
          data: coalData,
          itemStyle: {
            color: '#fac858'
          }
        },
        {
          name: '碳排放(吨CO₂)',
          type: 'bar',
          data: carbonData,
          itemStyle: {
            color: '#ee6666'
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
