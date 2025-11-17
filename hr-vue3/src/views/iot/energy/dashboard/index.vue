<template>
  <div class="energy-dashboard">
    <!-- 顶部总览卡片 -->
    <el-row :gutter="20" class="overview-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card total-energy">
          <div class="card-icon">
            <Icon icon="ep:odometer" :size="40" />
          </div>
          <div class="card-content">
            <div class="card-title">总能耗</div>
            <div class="card-value">{{ formatNumber(overview.totalEnergy) }}</div>
            <div class="card-subtitle">
              今日: {{ formatNumber(overview.todayEnergy) }} |
              环比: <span :class="getDayRateClass()">{{ formatRate(overview.dayOnDayRate) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card total-coal">
          <div class="card-icon">
            <Icon icon="ep:box" :size="40" />
          </div>
          <div class="card-content">
            <div class="card-title">总标煤（吨）</div>
            <div class="card-value">{{ formatNumber(overview.totalCoal) }}</div>
            <div class="card-subtitle">
              本月: {{ formatNumber(overview.monthEnergy) }} |
              环比: <span :class="getMonthRateClass()">{{ formatRate(overview.monthOnMonthRate) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card total-carbon">
          <div class="card-icon">
            <Icon icon="ep:partly-cloudy" :size="40" />
          </div>
          <div class="card-content">
            <div class="card-title">碳排放（吨CO₂）</div>
            <div class="card-value">{{ formatNumber(overview.totalCarbon) }}</div>
            <div class="card-subtitle">
              昨日: {{ formatNumber(overview.yesterdayEnergy) }} |
              上月: {{ formatNumber(overview.lastMonthEnergy) }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card total-cost">
          <div class="card-icon">
            <Icon icon="ep:money" :size="40" />
          </div>
          <div class="card-content">
            <div class="card-title">总费用（元）</div>
            <div class="card-value">{{ formatNumber(overview.totalCost) }}</div>
            <div class="card-subtitle">
              在线: <el-tag type="success" size="small">{{ overview.onlineMeterCount }}</el-tag> |
              离线: <el-tag type="danger" size="small">{{ overview.offlineMeterCount }}</el-tag> |
              告警: <el-tag type="warning" size="small">{{ overview.alertCount }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图和分项能耗 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header-content">
              <span class="card-header-title">能耗趋势分析</span>
              <el-radio-group v-model="trendPeriod" size="small" @change="loadTrendData">
                <el-radio-button label="24h">最近24小时</el-radio-button>
                <el-radio-button label="7d">最近7天</el-radio-button>
                <el-radio-button label="30d">最近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <Echart :options="trendChartOptions" :height="380" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span class="card-header-title">分项能耗占比</span>
          </template>
          <Echart :options="itemChartOptions" :height="380" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 排名和实时监控 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header-content">
              <span class="card-header-title">能耗排名 TOP10</span>
              <el-radio-group v-model="rankingType" size="small" @change="loadRankingData">
                <el-radio-button label="building">建筑</el-radio-button>
                <el-radio-button label="meter">计量点</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="ranking-list">
            <div v-for="item in rankingList" :key="item.id" class="ranking-item">
              <div class="ranking-rank" :class="getRankClass(item.rank)">{{ item.rank }}</div>
              <div class="ranking-info">
                <div class="ranking-name">{{ item.name }}</div>
                <div class="ranking-type">{{ item.energyTypeName }}</div>
              </div>
              <div class="ranking-value">
                <div class="ranking-energy">{{ formatNumber(item.totalEnergy) }} {{ item.unit }}</div>
                <el-progress :percentage="item.percentage" :stroke-width="6" :show-text="false" />
              </div>
            </div>
            <el-empty v-if="rankingList.length === 0" description="暂无数据" :image-size="100" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span class="card-header-title">实时能耗监控</span>
          </template>
          <Echart :options="realtimeChartOptions" :height="350" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import {
  IotEnergyDashboardOverviewVO,
  IotEnergyDashboardRankingVO,
  IotEnergyDashboardTrendVO,
  IotEnergyDashboardItemVO,
  getIotEnergyDashboardOverview,
  getIotEnergyDashboardRanking,
  getIotEnergyDashboardTrend,
  getIotEnergyDashboardItem
} from '@/api/iot/energy/dashboard'
import { Echart } from '@/components/Echart'

/** IoT 能源仪表板 */
defineOptions({ name: 'IotEnergyDashboard' })

const overview = ref<IotEnergyDashboardOverviewVO>({
  totalEnergy: 0,
  totalCoal: 0,
  totalCarbon: 0,
  totalCost: 0,
  todayEnergy: 0,
  yesterdayEnergy: 0,
  dayOnDayRate: 0,
  monthEnergy: 0,
  lastMonthEnergy: 0,
  monthOnMonthRate: 0,
  onlineMeterCount: 0,
  offlineMeterCount: 0,
  alertCount: 0
})
const trendPeriod = ref('24h')
const rankingType = ref('building')
const rankingList = ref<IotEnergyDashboardRankingVO[]>([])
const trendChartOptions = ref({})
const itemChartOptions = ref({})
const realtimeChartOptions = ref({})
let realtimeTimer: any = null

/** 格式化数字 */
const formatNumber = (num: number) => {
  if (!num && num !== 0) return '0'
  return num.toFixed(2)
}

/** 格式化百分比 */
const formatRate = (rate: number) => {
  if (!rate && rate !== 0) return '0%'
  const sign = rate > 0 ? '+' : ''
  return sign + rate.toFixed(2) + '%'
}

/** 获取日环比样式 */
const getDayRateClass = () => {
  return overview.value.dayOnDayRate > 0 ? 'rate-up' : 'rate-down'
}

/** 获取月环比样式 */
const getMonthRateClass = () => {
  return overview.value.monthOnMonthRate > 0 ? 'rate-up' : 'rate-down'
}

/** 获取排名样式 */
const getRankClass = (rank: number) => {
  if (rank === 1) return 'rank-first'
  if (rank === 2) return 'rank-second'
  if (rank === 3) return 'rank-third'
  return ''
}

/** 加载总览数据 */
const loadOverviewData = async () => {
  try {
    overview.value = await getIotEnergyDashboardOverview()
  } catch (error) {
    console.error('加载总览数据失败:', error)
  }
}

/** 加载趋势数据 */
const loadTrendData = async () => {
  try {
    const data: IotEnergyDashboardTrendVO[] = await getIotEnergyDashboardTrend({
      period: trendPeriod.value
    })

    const timeData = data.map(item => item.time)
    const energyData = data.map(item => item.energyValue)
    const coalData = data.map(item => item.coalValue)
    const carbonData = data.map(item => item.carbonValue)

    trendChartOptions.value = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross'
        }
      },
      legend: {
        data: ['能耗值', '标煤值', '碳排放值']
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
          name: '能耗/标煤',
          position: 'left'
        },
        {
          type: 'value',
          name: '碳排放',
          position: 'right'
        }
      ],
      series: [
        {
          name: '能耗值',
          type: 'line',
          smooth: true,
          data: energyData,
          itemStyle: { color: '#5470c6' },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(84, 112, 198, 0.5)' },
                { offset: 1, color: 'rgba(84, 112, 198, 0.1)' }
              ]
            }
          }
        },
        {
          name: '标煤值',
          type: 'line',
          smooth: true,
          data: coalData,
          itemStyle: { color: '#fac858' }
        },
        {
          name: '碳排放值',
          type: 'line',
          smooth: true,
          yAxisIndex: 1,
          data: carbonData,
          itemStyle: { color: '#ee6666' }
        }
      ]
    }
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  }
}

/** 加载分项能耗数据 */
const loadItemData = async () => {
  try {
    const endTime = new Date()
    const startTime = new Date(endTime.getTime() - 30 * 24 * 60 * 60 * 1000) // 最近30天

    const data: IotEnergyDashboardItemVO[] = await getIotEnergyDashboardItem({
      startTime,
      endTime
    })

    const itemData = data.map(item => ({
      name: item.energyTypeName,
      value: item.totalEnergy,
      itemStyle: { color: item.color }
    }))

    itemChartOptions.value = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        top: 'middle'
      },
      series: [
        {
          name: '能耗占比',
          type: 'pie',
          radius: ['45%', '75%'],
          avoidLabelOverlap: true,
          itemStyle: {
            borderRadius: 8,
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
          data: itemData
        }
      ]
    }
  } catch (error) {
    console.error('加载分项能耗数据失败:', error)
  }
}

/** 加载排名数据 */
const loadRankingData = async () => {
  try {
    const endTime = new Date()
    const startTime = new Date(endTime.getTime() - 24 * 60 * 60 * 1000) // 最近24小时

    const data: IotEnergyDashboardRankingVO[] = await getIotEnergyDashboardRanking({
      type: rankingType.value,
      startTime,
      endTime,
      topN: 10
    })

    rankingList.value = data
  } catch (error) {
    console.error('加载排名数据失败:', error)
  }
}

/** 加载实时监控数据 */
const loadRealtimeData = () => {
  // 模拟实时数据
  const now = new Date()
  const timeData: string[] = []
  const valueData: number[] = []

  for (let i = 29; i >= 0; i--) {
    const time = new Date(now.getTime() - i * 60 * 1000)
    timeData.push(time.getHours() + ':' + String(time.getMinutes()).padStart(2, '0'))
    valueData.push(Math.random() * 100 + 50)
  }

  realtimeChartOptions.value = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'line'
      }
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
      name: '功率 (kW)'
    },
    series: [
      {
        name: '实时功率',
        type: 'line',
        smooth: true,
        symbol: 'none',
        sampling: 'lttb',
        data: valueData,
        itemStyle: {
          color: '#91cc75'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(145, 204, 117, 0.5)' },
              { offset: 1, color: 'rgba(145, 204, 117, 0.1)' }
            ]
          }
        }
      }
    ]
  }
}

/** 启动实时数据刷新 */
const startRealtimeRefresh = () => {
  loadRealtimeData()
  realtimeTimer = setInterval(() => {
    loadRealtimeData()
  }, 60000) // 每分钟刷新一次
}

/** 停止实时数据刷新 */
const stopRealtimeRefresh = () => {
  if (realtimeTimer) {
    clearInterval(realtimeTimer)
    realtimeTimer = null
  }
}

/** 初始化 */
onMounted(() => {
  loadOverviewData()
  loadTrendData()
  loadItemData()
  loadRankingData()
  startRealtimeRefresh()

  // 每5分钟刷新一次总览数据
  setInterval(() => {
    loadOverviewData()
    loadRankingData()
  }, 300000)
})

onBeforeUnmount(() => {
  stopRealtimeRefresh()
})
</script>

<style scoped lang="scss">
.energy-dashboard {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 84px);

  .overview-cards {
    .stat-card {
      :deep(.el-card__body) {
        display: flex;
        align-items: center;
        padding: 20px;
      }

      .card-icon {
        width: 80px;
        height: 80px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        margin-right: 20px;
      }

      .card-content {
        flex: 1;

        .card-title {
          font-size: 14px;
          color: #999;
          margin-bottom: 8px;
        }

        .card-value {
          font-size: 28px;
          font-weight: bold;
          margin-bottom: 8px;
        }

        .card-subtitle {
          font-size: 12px;
          color: #666;

          .rate-up {
            color: #f56c6c;
          }

          .rate-down {
            color: #67c23a;
          }
        }
      }

      &.total-energy {
        .card-icon {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
        }
        .card-value {
          color: #667eea;
        }
      }

      &.total-coal {
        .card-icon {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          color: white;
        }
        .card-value {
          color: #f5576c;
        }
      }

      &.total-carbon {
        .card-icon {
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          color: white;
        }
        .card-value {
          color: #4facfe;
        }
      }

      &.total-cost {
        .card-icon {
          background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
          color: white;
        }
        .card-value {
          color: #43e97b;
        }
      }
    }
  }

  .card-header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .card-header-title {
      font-size: 16px;
      font-weight: 600;
    }
  }

  .ranking-list {
    max-height: 350px;
    overflow-y: auto;

    .ranking-item {
      display: flex;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .ranking-rank {
        width: 32px;
        height: 32px;
        line-height: 32px;
        text-align: center;
        border-radius: 4px;
        font-weight: bold;
        margin-right: 12px;
        background-color: #f0f0f0;
        color: #666;

        &.rank-first {
          background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
          color: white;
        }

        &.rank-second {
          background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
          color: white;
        }

        &.rank-third {
          background: linear-gradient(135deg, #cd7f32 0%, #d4a574 100%);
          color: white;
        }
      }

      .ranking-info {
        flex: 1;
        margin-right: 12px;

        .ranking-name {
          font-size: 14px;
          font-weight: 500;
          margin-bottom: 4px;
        }

        .ranking-type {
          font-size: 12px;
          color: #999;
        }
      }

      .ranking-value {
        min-width: 150px;
        text-align: right;

        .ranking-energy {
          font-size: 14px;
          font-weight: bold;
          color: #409eff;
          margin-bottom: 4px;
        }
      }
    }
  }
}
</style>
