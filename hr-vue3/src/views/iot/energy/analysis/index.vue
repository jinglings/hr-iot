<template>
  <div class="energy-analysis">
    <!-- 查询条件 -->
    <ContentWrap>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="80px"
      >
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
          <el-button type="primary" @click="handleAnalysis">
            <Icon icon="ep:data-analysis" class="mr-5px" /> 开始分析
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" /> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <!-- 同比环比分析 -->
    <ContentWrap style="margin-top: 20px">
      <div class="section-header">
        <h3>同比环比分析</h3>
        <el-radio-group v-model="comparisonType" size="small" @change="loadComparisonData">
          <el-radio-button label="yoy">同比分析</el-radio-button>
          <el-radio-button label="mom">环比分析</el-radio-button>
        </el-radio-group>
      </div>

      <el-row :gutter="20" style="margin-top: 20px">
        <!-- 对比卡片 -->
        <el-col :span="8" v-for="item in comparisonList" :key="item.currentPeriod">
          <el-card shadow="hover" class="comparison-card">
            <div class="comparison-header">
              <span class="period-label">{{ item.currentPeriod }}</span>
              <el-tag :type="getComparisonTagType(item)">
                {{ comparisonType === 'yoy' ? '同比' : '环比' }}
                {{ formatRate(comparisonType === 'yoy' ? item.yearOnYearRate : item.monthOnMonthRate) }}
              </el-tag>
            </div>
            <div class="comparison-values">
              <div class="value-item current">
                <div class="label">当前值</div>
                <div class="value">{{ formatNumber(item.currentValue) }} {{ item.unit }}</div>
              </div>
              <div class="divider">
                <Icon :icon="getComparisonIcon(item)" :size="30" :color="getComparisonColor(item)" />
              </div>
              <div class="value-item last">
                <div class="label">{{ comparisonType === 'yoy' ? '去年同期' : '上期' }}</div>
                <div class="value">{{ formatNumber(item.lastValue) }} {{ item.unit }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 对比趋势图 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="24">
          <Echart :options="comparisonChartOptions" :height="350" />
        </el-col>
      </el-row>
    </ContentWrap>

    <!-- 能效指标评估 -->
    <ContentWrap style="margin-top: 20px">
      <div class="section-header">
        <h3>能效指标评估</h3>
        <el-radio-group v-model="indicatorType" size="small" @change="loadIndicatorData">
          <el-radio-button label="area">单位面积能耗</el-radio-button>
          <el-radio-button label="capita">人均能耗</el-radio-button>
          <el-radio-button label="output">单位产值能耗</el-radio-button>
        </el-radio-group>
      </div>

      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12">
          <el-table :data="indicatorList" :stripe="true" border>
            <el-table-column label="指标名称" align="center" prop="indicatorName" />
            <el-table-column label="指标值" align="center" prop="indicatorValue" width="120">
              <template #default="scope">
                {{ formatNumber(scope.row.indicatorValue) }}
              </template>
            </el-table-column>
            <el-table-column label="单位" align="center" prop="unit" width="120" />
            <el-table-column label="基准值" align="center" prop="benchmarkValue" width="120">
              <template #default="scope">
                {{ formatNumber(scope.row.benchmarkValue) }}
              </template>
            </el-table-column>
            <el-table-column label="偏离率" align="center" prop="deviationRate" width="120">
              <template #default="scope">
                <el-tag :type="scope.row.deviationRate > 0 ? 'danger' : 'success'">
                  {{ formatRate(scope.row.deviationRate) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
        <el-col :span="12">
          <Echart :options="indicatorChartOptions" :height="300" />
        </el-col>
      </el-row>
    </ContentWrap>

    <!-- 碳排放计算 -->
    <ContentWrap style="margin-top: 20px">
      <div class="section-header">
        <h3>碳排放计算分析</h3>
      </div>

      <el-row :gutter="20" style="margin-top: 20px">
        <!-- 碳排放总览 -->
        <el-col :span="24">
          <div class="carbon-overview">
            <div class="carbon-card total">
              <div class="icon-wrapper">
                <Icon icon="ep:partly-cloudy" :size="50" />
              </div>
              <div class="content">
                <div class="label">总碳排放量</div>
                <div class="value">{{ formatNumber(totalCarbon) }} <span class="unit">吨CO₂</span></div>
              </div>
            </div>
            <div class="carbon-card coal">
              <div class="icon-wrapper">
                <Icon icon="ep:box" :size="50" />
              </div>
              <div class="content">
                <div class="label">折标煤当量</div>
                <div class="value">{{ formatNumber(totalCoal) }} <span class="unit">吨标煤</span></div>
              </div>
            </div>
            <div class="carbon-card trees">
              <div class="icon-wrapper">
                <Icon icon="ep:orange" :size="50" />
              </div>
              <div class="content">
                <div class="label">需种植树木</div>
                <div class="value">{{ formatNumber(totalCarbon * 50) }} <span class="unit">棵</span></div>
                <div class="tip">以抵消碳排放</div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px">
        <!-- 碳排放列表 -->
        <el-col :span="14">
          <el-table :data="carbonList" :stripe="true" border>
            <el-table-column label="能源类型" align="center" prop="energyTypeName" />
            <el-table-column label="总能耗" align="center" prop="totalEnergy" width="120">
              <template #default="scope">
                {{ formatNumber(scope.row.totalEnergy) }}
              </template>
            </el-table-column>
            <el-table-column label="单位" align="center" prop="unit" width="80" />
            <el-table-column label="碳排放系数" align="center" prop="carbonFactor" width="130">
              <template #default="scope">
                {{ scope.row.carbonFactor }}
              </template>
            </el-table-column>
            <el-table-column label="碳排放量(吨CO₂)" align="center" prop="carbonEmission" width="150">
              <template #default="scope">
                {{ formatNumber(scope.row.carbonEmission) }}
              </template>
            </el-table-column>
            <el-table-column label="占比" align="center" prop="percentage" width="100">
              <template #default="scope">
                {{ formatRate(scope.row.percentage) }}
              </template>
            </el-table-column>
          </el-table>
        </el-col>
        <!-- 碳排放饼图 -->
        <el-col :span="10">
          <Echart :options="carbonChartOptions" :height="350" />
        </el-col>
      </el-row>
    </ContentWrap>
  </div>
</template>

<script setup lang="ts">
import {
  IotEnergyAnalysisComparisonVO,
  IotEnergyAnalysisIndicatorVO,
  IotEnergyAnalysisCarbonVO,
  getIotEnergyAnalysisComparison,
  getIotEnergyAnalysisIndicator,
  getIotEnergyAnalysisCarbon
} from '@/api/iot/energy/analysis'
import { getIotEnergyTypeSimpleList } from '@/api/iot/energy/energyType'
import { getIotEnergyBuildingSimpleList } from '@/api/iot/energy/building'
import { Echart } from '@/components/Echart'

/** IoT 能源效率分析 */
defineOptions({ name: 'IotEnergyAnalysis' })

const message = useMessage()

const buildingList = ref([])
const energyTypeList = ref([])
const comparisonType = ref('yoy') // yoy-同比, mom-环比
const indicatorType = ref('area') // area-单位面积, capita-人均, output-单位产值
const comparisonList = ref<IotEnergyAnalysisComparisonVO[]>([])
const indicatorList = ref<IotEnergyAnalysisIndicatorVO[]>([])
const carbonList = ref<IotEnergyAnalysisCarbonVO[]>([])
const totalCarbon = ref(0)
const totalCoal = ref(0)
const comparisonChartOptions = ref({})
const indicatorChartOptions = ref({})
const carbonChartOptions = ref({})
const queryParams = reactive({
  buildingId: undefined,
  energyTypeId: undefined,
  timeRange: []
})
const queryFormRef = ref()

/** 格式化数字 */
const formatNumber = (num: number) => {
  if (!num && num !== 0) return '0.00'
  return num.toFixed(2)
}

/** 格式化百分比 */
const formatRate = (rate: number) => {
  if (!rate && rate !== 0) return '0.00%'
  const sign = rate > 0 ? '+' : ''
  return sign + rate.toFixed(2) + '%'
}

/** 获取对比标签类型 */
const getComparisonTagType = (item: IotEnergyAnalysisComparisonVO) => {
  const rate = comparisonType.value === 'yoy' ? item.yearOnYearRate : item.monthOnMonthRate
  if (rate > 10) return 'danger'
  if (rate > 0) return 'warning'
  return 'success'
}

/** 获取对比图标 */
const getComparisonIcon = (item: IotEnergyAnalysisComparisonVO) => {
  const rate = comparisonType.value === 'yoy' ? item.yearOnYearRate : item.monthOnMonthRate
  if (rate > 0) return 'ep:top'
  if (rate < 0) return 'ep:bottom'
  return 'ep:minus'
}

/** 获取对比颜色 */
const getComparisonColor = (item: IotEnergyAnalysisComparisonVO) => {
  const rate = comparisonType.value === 'yoy' ? item.yearOnYearRate : item.monthOnMonthRate
  if (rate > 0) return '#f56c6c'
  if (rate < 0) return '#67c23a'
  return '#909399'
}

/** 加载建筑列表 */
const getBuildingList = async () => {
  buildingList.value = await getIotEnergyBuildingSimpleList()
}

/** 加载能源类型列表 */
const getEnergyTypeList = async () => {
  energyTypeList.value = await getIotEnergyTypeSimpleList()
}

/** 开始分析 */
const handleAnalysis = async () => {
  if (!queryParams.timeRange || queryParams.timeRange.length !== 2) {
    message.warning('请选择时间范围')
    return
  }

  await Promise.all([
    loadComparisonData(),
    loadIndicatorData(),
    loadCarbonData()
  ])
}

/** 重置查询 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
}

/** 加载同比环比数据 */
const loadComparisonData = async () => {
  try {
    // 模拟获取最近3个月的数据
    const data: IotEnergyAnalysisComparisonVO[] = await getIotEnergyAnalysisComparison({
      buildingId: queryParams.buildingId,
      energyTypeId: queryParams.energyTypeId,
      comparisonType: comparisonType.value,
      period: 'month',
      currentPeriod: '2024-03'
    })

    comparisonList.value = data

    // 生成对比趋势图
    const categories = data.map(item => item.currentPeriod)
    const currentData = data.map(item => item.currentValue)
    const lastData = data.map(item => item.lastValue)

    comparisonChartOptions.value = {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      legend: {
        data: ['当前值', comparisonType.value === 'yoy' ? '去年同期' : '上期']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: categories
      },
      yAxis: {
        type: 'value',
        name: '能耗值'
      },
      series: [
        {
          name: '当前值',
          type: 'bar',
          data: currentData,
          itemStyle: { color: '#5470c6' }
        },
        {
          name: comparisonType.value === 'yoy' ? '去年同期' : '上期',
          type: 'bar',
          data: lastData,
          itemStyle: { color: '#91cc75' }
        }
      ]
    }
  } catch (error) {
    console.error('加载对比数据失败:', error)
  }
}

/** 加载能效指标数据 */
const loadIndicatorData = async () => {
  try {
    if (!queryParams.timeRange || queryParams.timeRange.length !== 2) return

    const data: IotEnergyAnalysisIndicatorVO[] = await getIotEnergyAnalysisIndicator({
      buildingId: queryParams.buildingId,
      indicatorType: indicatorType.value,
      startTime: queryParams.timeRange[0],
      endTime: queryParams.timeRange[1]
    })

    indicatorList.value = data

    // 生成指标对比图
    const indicators = data.map(item => item.indicatorName)
    const actualValues = data.map(item => item.indicatorValue)
    const benchmarkValues = data.map(item => item.benchmarkValue)

    indicatorChartOptions.value = {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      legend: {
        data: ['实际值', '基准值']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: indicators
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '实际值',
          type: 'bar',
          data: actualValues,
          itemStyle: { color: '#ee6666' },
          label: {
            show: true,
            position: 'top'
          }
        },
        {
          name: '基准值',
          type: 'line',
          data: benchmarkValues,
          itemStyle: { color: '#fac858' },
          lineStyle: {
            type: 'dashed'
          }
        }
      ]
    }
  } catch (error) {
    console.error('加载能效指标失败:', error)
  }
}

/** 加载碳排放数据 */
const loadCarbonData = async () => {
  try {
    if (!queryParams.timeRange || queryParams.timeRange.length !== 2) return

    const data: IotEnergyAnalysisCarbonVO[] = await getIotEnergyAnalysisCarbon({
      buildingId: queryParams.buildingId,
      startTime: queryParams.timeRange[0],
      endTime: queryParams.timeRange[1]
    })

    carbonList.value = data
    totalCarbon.value = data.reduce((sum, item) => sum + item.carbonEmission, 0)
    totalCoal.value = totalCarbon.value * 0.4 // 简化计算，实际应根据标煤系数

    // 生成碳排放饼图
    const pieData = data.map(item => ({
      name: item.energyTypeName,
      value: item.carbonEmission
    }))

    carbonChartOptions.value = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} 吨CO₂ ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '碳排放',
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
            formatter: '{b}\n{d}%'
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
  } catch (error) {
    console.error('加载碳排放数据失败:', error)
  }
}

/** 初始化 */
onMounted(() => {
  getBuildingList()
  getEnergyTypeList()

  // 设置默认时间范围（最近30天）
  const endTime = new Date()
  const startTime = new Date(endTime.getTime() - 30 * 24 * 60 * 60 * 1000)
  queryParams.timeRange = [
    startTime.toISOString().slice(0, 19).replace('T', ' '),
    endTime.toISOString().slice(0, 19).replace('T', ' ')
  ]
})
</script>

<style scoped lang="scss">
.energy-analysis {
  padding: 20px;
  background-color: #f0f2f5;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
    }
  }

  .comparison-card {
    margin-bottom: 20px;

    .comparison-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;

      .period-label {
        font-size: 16px;
        font-weight: 600;
      }
    }

    .comparison-values {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .value-item {
        flex: 1;
        text-align: center;

        .label {
          font-size: 12px;
          color: #999;
          margin-bottom: 8px;
        }

        .value {
          font-size: 24px;
          font-weight: bold;
          color: #333;
        }

        &.current .value {
          color: #409eff;
        }

        &.last .value {
          color: #67c23a;
        }
      }

      .divider {
        width: 60px;
        display: flex;
        justify-content: center;
        align-items: center;
      }
    }
  }

  .carbon-overview {
    display: flex;
    gap: 20px;
    margin-bottom: 20px;

    .carbon-card {
      flex: 1;
      display: flex;
      align-items: center;
      padding: 30px;
      border-radius: 8px;
      background: white;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

      .icon-wrapper {
        width: 80px;
        height: 80px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        margin-right: 20px;
      }

      .content {
        flex: 1;

        .label {
          font-size: 14px;
          color: #999;
          margin-bottom: 8px;
        }

        .value {
          font-size: 32px;
          font-weight: bold;

          .unit {
            font-size: 16px;
            color: #999;
            margin-left: 5px;
          }
        }

        .tip {
          font-size: 12px;
          color: #999;
          margin-top: 5px;
        }
      }

      &.total {
        .icon-wrapper {
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          color: white;
        }
        .value {
          color: #4facfe;
        }
      }

      &.coal {
        .icon-wrapper {
          background: linear-gradient(135deg, #ffd89b 0%, #19547b 100%);
          color: white;
        }
        .value {
          color: #19547b;
        }
      }

      &.trees {
        .icon-wrapper {
          background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
          color: #333;
        }
        .value {
          color: #67c23a;
        }
      }
    }
  }
}
</style>
