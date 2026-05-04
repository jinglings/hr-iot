<template>
  <div class="mall-screen" ref="screenRef">
    <!-- ===== 顶部标题栏 ===== -->
    <header class="screen-header">
      <div class="header-left">
        <span class="weather-icon">☁️</span>
        <span class="header-info">{{ currentWeather }}°C</span>
        <span class="header-divider">|</span>
        <span class="header-info">{{ currentDateTime }}</span>
        <span class="header-divider">|</span>
        <span class="header-info">{{ currentWeekday }}</span>
      </div>
      <div class="header-center">
        <h1 class="screen-title">商场智慧电表监控大屏</h1>
      </div>
      <div class="header-right">
        <div class="header-btn" @click="showAlerts">
          <span class="btn-icon">🔔</span>
          <span>告警</span>
          <span class="badge badge-red">{{ overview.alertMeters }}</span>
        </div>
        <div class="header-btn" @click="showEvents">
          <span class="btn-icon">📋</span>
          <span>事件</span>
          <span class="badge badge-blue">5</span>
        </div>
        <div class="header-btn">
          <span class="btn-icon">📊</span>
          <span>报表</span>
        </div>
        <div class="header-btn">
          <span class="btn-icon">⚙️</span>
          <span>设置</span>
        </div>
        <div class="header-btn" @click="toggleFullscreen">
          <span class="btn-icon">⛶</span>
          <span>全屏</span>
        </div>
      </div>
    </header>

    <!-- ===== 主体内容区 ===== -->
    <main class="screen-body">
      <!-- ===== 左侧面板 ===== -->
      <aside class="panel-left">
        <!-- 今日用电概览 -->
        <section class="panel-section">
          <div class="section-title">今日用电概览</div>
          <div class="overview-cards">
            <div class="overview-card">
              <div class="ov-icon ov-icon-energy">⚡</div>
              <div class="ov-content">
                <div class="ov-label">总用电量（kWh）</div>
                <div class="ov-value ov-value-blue">{{ formatNumber(overview.totalEnergy) }}</div>
              </div>
            </div>
            <div class="overview-card">
              <div class="ov-icon ov-icon-power">〜</div>
              <div class="ov-content">
                <div class="ov-label">总功率（kW）</div>
                <div class="ov-value ov-value-cyan">{{ formatNumber(overview.totalPower) }}</div>
              </div>
            </div>
            <div class="overview-card">
              <div class="ov-icon ov-icon-online">⊟</div>
              <div class="ov-content">
                <div class="ov-label">在线电表（台）</div>
                <div class="ov-value ov-value-white">
                  {{ overview.onlineMeters }} / {{ overview.totalMeters }}
                </div>
              </div>
            </div>
            <div class="overview-card overview-card-alert">
              <div class="ov-icon ov-icon-alert">⚠</div>
              <div class="ov-content">
                <div class="ov-label">告警电表（台）</div>
                <div class="ov-value ov-value-red">{{ overview.alertMeters }}</div>
              </div>
            </div>
          </div>
        </section>

        <!-- 告警信息 -->
        <section class="panel-section flex-1">
          <div class="section-title">
            告警信息
            <span class="more-link" @click="showAlerts">更多 ›</span>
          </div>
          <div class="alert-list">
            <div
              v-for="(alert, idx) in alerts"
              :key="idx"
              class="alert-item"
              :class="'alert-level-' + alert.level"
            >
              <span class="alert-dot"></span>
              <span class="alert-text">{{ alert.meterName }} {{ alert.type }}</span>
              <span class="alert-time">{{ alert.time }}</span>
            </div>
          </div>
        </section>

        <!-- 用电趋势 -->
        <section class="panel-section">
          <div class="section-title">
            用电趋势（全场）
            <div class="trend-tabs">
              <span
                v-for="tab in trendTabs"
                :key="tab.key"
                :class="['trend-tab', activeTrendTab === tab.key && 'active']"
                @click="switchTrendTab(tab.key)"
              >{{ tab.label }}</span>
            </div>
          </div>
          <div ref="trendChartRef" class="trend-chart"></div>
        </section>
      </aside>

      <!-- ===== 中间区域 ===== -->
      <div class="panel-center">
        <!-- 电表位置区域图 -->
        <section class="panel-section floor-plan-section">
          <div class="section-title">
            电表位置区域图（配电室平面图）
          </div>
          <!-- 过滤工具栏 -->
          <div class="floor-toolbar">
            <div class="filter-group">
              <span class="filter-label">设备分组：</span>
              <select class="dark-select" v-model="selectedGroupId" @change="onGroupChange">
                <option :value="undefined">全部分组</option>
                <option v-for="g in deviceGroups" :key="g.id" :value="g.id">{{ g.name }}</option>
              </select>
            </div>
            <div class="search-box">
              <input
                class="dark-input"
                v-model="meterSearch"
                placeholder="搜索电表 / 商铺"
                @input="filterMeters"
              />
              <span class="search-icon">🔍</span>
            </div>
            <div class="legend">
              <span class="legend-dot dot-normal"></span><span>正常</span>
              <span class="legend-dot dot-warning"></span><span>告警</span>
              <span class="legend-dot dot-offline"></span><span>离线</span>
            </div>
          </div>

          <!-- 平面图区域 -->
          <div
            class="floor-plan"
            ref="floorPlanRef"
            :class="{ 'is-dragging': isDragging }"
            @mousedown="startDrag"
            @mousemove="onDrag"
            @mouseup="stopDrag"
            @mouseleave="stopDrag"
            @wheel.prevent="onWheel"
          >
            <!-- 地图控制按钮 -->
            <div class="map-controls">
              <button class="map-btn" @click="zoomIn">+</button>
              <button class="map-btn" @click="zoomOut">-</button>
              <button class="map-btn" @click="resetZoom">↺</button>
              <button class="map-btn" @click="resetZoom">1:1</button>
            </div>
            <!-- 指北针 -->
            <div class="compass">
              <div class="compass-n">N</div>
              <div class="compass-arrow">↑</div>
            </div>

            <!-- 配电室平面图 SVG -->
            <div class="plan-canvas" :style="[canvasStyle, { width: dynamicCanvasW + 'px', height: dynamicCanvasH + 'px' }]">
              <!-- 背景房间布局 -->
<!--
              <svg class="plan-svg" :viewBox="`0 0 ${dynamicCanvasW} ${dynamicCanvasH}`" xmlns="http://www.w3.org/2000/svg">
                &lt;!&ndash; 主配电室外框 &ndash;&gt;
                <rect x="60" y="20" width="680" height="340" rx="4" fill="none" stroke="#1e4d7b" stroke-width="2"/>
                &lt;!&ndash; 进线柜 左侧竖排 &ndash;&gt;
                <rect x="60" y="20" width="60" height="340" rx="0" fill="#0d2a4a" stroke="#1e4d7b" stroke-width="1"/>
                <text x="90" y="200" fill="#4a9dd4" font-size="13" text-anchor="middle" dominant-baseline="middle" transform="rotate(-90, 90, 200)">进线柜</text>
                &lt;!&ndash; 备用柜 右侧竖排 &ndash;&gt;
                <rect x="680" y="20" width="60" height="340" rx="0" fill="#0d2a4a" stroke="#1e4d7b" stroke-width="1"/>
                <text x="710" y="200" fill="#4a9dd4" font-size="13" text-anchor="middle" dominant-baseline="middle" transform="rotate(90, 710, 200)">备用柜</text>
                &lt;!&ndash; 中间分割线 &ndash;&gt;
                <line x1="120" y1="200" x2="680" y2="200" stroke="#1e4d7b" stroke-width="1" stroke-dasharray="4,4"/>
                &lt;!&ndash; 配电室A区标签 &ndash;&gt;
                <text x="400" y="120" fill="#2a6fa8" font-size="16" text-anchor="middle" font-weight="bold">配电室</text>
                <text x="400" y="140" fill="#2a6fa8" font-size="16" text-anchor="middle" font-weight="bold">A区</text>
                &lt;!&ndash; 配电室B区标签 &ndash;&gt;
                <text x="400" y="290" fill="#2a6fa8" font-size="16" text-anchor="middle" font-weight="bold">配电室</text>
                <text x="400" y="310" fill="#2a6fa8" font-size="16" text-anchor="middle" font-weight="bold">B区</text>
                &lt;!&ndash; 弧形装饰线 &ndash;&gt;
                <path d="M 200 210 Q 300 260 400 210 Q 500 160 600 210" fill="none" stroke="#1e4d7b" stroke-width="1" stroke-dasharray="3,3"/>
              </svg>
-->

              <!-- 电表图标 -->
              <div
                v-for="meter in visibleMeters"
                :key="meter.id"
                class="meter-icon-wrapper"
                :style="{ left: meter.x + 'px', top: meter.y + 'px' }"
                @click="selectMeter(meter)"
                @mouseenter="showTooltip(meter, $event)"
                @mouseleave="hideTooltip"
              >
                <!-- 选中光环动画 -->
                <div v-if="selectedMeter?.id === meter.id" class="meter-select-ring"></div>
                <!-- 电表图标主体 -->
                <div class="meter-icon" :class="'meter-status-' + meter.status">
                  <div class="meter-body">
                    <div class="meter-screen">
                      <div class="meter-screen-line"></div>
                      <div class="meter-screen-line short"></div>
                    </div>
                    <div class="meter-ports">
                      <div class="meter-port"></div>
                      <div class="meter-port"></div>
                    </div>
                  </div>
                  <div v-if="meter.status === 'warning'" class="meter-alert-dot">!</div>
                </div>
                <div class="meter-label">
                  <div class="meter-name-text">{{ meter.name }}</div>
<!--                  <div class="meter-shop-text">{{ meter.shopName }}</div>-->
                  <div class="meter-pos-text">表号：{{ meter.location }}</div>
                </div>
              </div>

              <!-- 悬浮 Tooltip -->
              <div
                v-if="tooltipVisible && tooltipMeter"
                class="meter-tooltip"
                :style="{ left: tooltipX + 'px', top: tooltipY + 'px' }"
              >
                <div class="tooltip-title">{{ tooltipMeter.name }} - {{ tooltipMeter.shopName }}</div>
                <div class="tooltip-row"><span>表号：</span><span>{{ tooltipMeter.location }}</span></div>
                <div class="tooltip-row"><span>回路：</span><span>1#出线回路</span></div>
                <div class="tooltip-row"><span>电表型号：</span><span>DTSD1352</span></div>
                <div class="tooltip-row"><span>安装位置：</span><span>配电室A柜02位</span></div>
                <div class="tooltip-row"><span>负责人：</span><span>张工 138****1234</span></div>
              </div>
            </div>
          </div>
        </section>

        <!-- 全部电表列表 -->
        <section class="panel-section meter-list-section">
          <div class="section-title">
            全部电表列表（{{ allMeters.length }}）
            <div class="list-filters">
              <select class="dark-select sm" v-model="listStatusFilter">
                <option value="">全部状态</option>
                <option value="normal">正常</option>
                <option value="warning">告警</option>
                <option value="offline">离线</option>
              </select>
              <select class="dark-select sm" v-model="listFloorFilter">
                <option value="">全部楼层</option>
                <option value="1">一楼</option>
              </select>
              <select class="dark-select sm" v-model="listCircuitFilter">
                <option value="">全部回路</option>
                <option value="1">1#出线回路</option>
              </select>
              <div class="search-box sm">
                <input class="dark-input sm" v-model="listSearch" placeholder="搜索电表 / 商铺 / 柜位" />
                <span class="search-icon">🔍</span>
              </div>
              <div class="view-btns">
                <button :class="['view-btn', listViewMode === 'grid' && 'active']" @click="listViewMode = 'grid'">⊞</button>
                <button :class="['view-btn', listViewMode === 'list' && 'active']" @click="listViewMode = 'list'">☰</button>
              </div>
            </div>
          </div>
          <!-- 电表卡片滚动列表 -->
          <div class="meter-cards-scroll">
            <button class="scroll-btn scroll-prev" @click="scrollCards(-1)">‹</button>
            <div class="meter-cards" ref="meterCardsRef">
              <div
                v-for="meter in filteredListMeters"
                :key="meter.id"
                class="meter-card"
                :class="[
                  'meter-card-' + meter.status,
                  selectedMeter?.id === meter.id && 'meter-card-selected'
                ]"
                @click="selectMeter(meter)"
              >
                <div class="mc-header">
                  <div class="mc-icon">
                    <div class="mc-meter-img"></div>
                  </div>
                  <div class="mc-info">
                    <div class="mc-name">{{ meter.name }}</div>
                    <div class="mc-status" :class="'status-text-' + meter.status">
                      <span class="status-dot-sm"></span>
                      {{ statusLabel(meter.status) }}
                    </div>
                  </div>
                </div>
                <div class="mc-shop">{{ meter.shopName }}</div>
                <div class="mc-location">
                  <span class="location-icon">📍</span>{{ meter.location }}
                </div>
                <div class="mc-stats">
                  <div class="mc-stat">
                    <span class="mc-stat-label">功率</span>
                    <span class="mc-stat-value">{{ meter.power }} kW</span>
                  </div>
                  <div class="mc-stat">
                    <span class="mc-stat-label">今日用电</span>
                    <span class="mc-stat-value highlight">{{ meter.todayEnergy }} kWh</span>
                  </div>
                </div>
              </div>
            </div>
            <button class="scroll-btn scroll-next" @click="scrollCards(1)">›</button>
          </div>
        </section>
      </div>

      <!-- ===== 右侧面板 ===== -->
      <aside class="panel-right" v-if="selectedMeter">
        <section class="panel-section right-detail">
          <div class="section-title">
            选中电表详情
            <button class="switch-btn" @click="switchMeter">切换电表</button>
          </div>

          <div class="detail-meter-title">
            <span>{{ selectedMeter.name }}</span>
            <span class="detail-status" :class="'status-text-' + selectedMeter.status">
              ● {{ statusLabel(selectedMeter.status) }}
            </span>
          </div>

          <div class="detail-info-grid">
            <div class="detail-img-wrap">
              <div class="detail-meter-img"></div>
            </div>
            <div class="detail-info-list">
              <div class="di-row"><span class="di-label">电表型号：</span><span class="di-val">DTSD1352</span></div>
              <div class="di-row"><span class="di-label">电表编号：</span><span class="di-val">2305120002</span></div>
              <div class="di-row"><span class="di-label">柜位：</span><span class="di-val">{{ selectedMeter.location }}</span></div>
              <div class="di-row"><span class="di-label">回路：</span><span class="di-val">1#出线回路</span></div>
              <div class="di-row"><span class="di-label">商铺名称：</span><span class="di-val">{{ selectedMeter.shopName }}</span></div>
              <div class="di-row"><span class="di-label">负责人：</span><span class="di-val">张工 138****1234</span></div>
            </div>
          </div>

          <!-- 主要统计值 -->
          <div class="detail-stats-grid">
            <div class="detail-stat-card">
              <div class="ds-label">当前功率（kW）</div>
              <div class="ds-value ds-blue">{{ selectedMeter.power }}</div>
            </div>
            <div class="detail-stat-card">
              <div class="ds-label">今日用电（kWh）</div>
              <div class="ds-value ds-blue">{{ selectedMeter.todayEnergy }}</div>
            </div>
            <div class="detail-stat-card">
              <div class="ds-label">本月用电（kWh）</div>
              <div class="ds-value ds-white">{{ selectedMeter.monthEnergy }}</div>
            </div>
            <div class="detail-stat-card">
              <div class="ds-label">昨日用电（kWh）</div>
              <div class="ds-value ds-white">{{ selectedMeter.yesterdayEnergy }}</div>
            </div>
          </div>

          <!-- 电气参数 -->
          <div class="detail-params-grid">
            <div class="param-card">
              <div class="param-label">电压（V）</div>
              <div class="param-value">{{ selectedMeter.voltage }}</div>
            </div>
            <div class="param-card">
              <div class="param-label">电流（A）</div>
              <div class="param-value">{{ selectedMeter.current }}</div>
            </div>
            <div class="param-card">
              <div class="param-label">功率因数</div>
              <div class="param-value">{{ selectedMeter.powerFactor }}</div>
            </div>
          </div>

          <!-- 状态行 -->
          <div class="detail-status-row">
            <span class="status-item">运行状态 <span class="status-text-normal">● 运行正常</span></span>
            <span class="status-item">通讯状态 <span class="status-text-normal">● 在线</span></span>
          </div>

          <!-- 今日用电趋势 -->
          <div class="detail-trend-header">
            今日用电趋势（kWh）
            <div class="detail-trend-tabs">
              <span
                v-for="tab in detailTrendTabs"
                :key="tab.key"
                :class="['detail-trend-tab', activeDetailTab === tab.key && 'active']"
                @click="switchDetailTab(tab.key)"
              >{{ tab.label }}</span>
            </div>
          </div>
          <div ref="detailChartRef" class="detail-chart"></div>
        </section>
      </aside>

      <!-- 右侧面板空状态 -->
      <aside class="panel-right panel-right-empty" v-else>
        <div class="empty-hint">点击电表查看详情</div>
      </aside>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import {
  getIotEnergyDashboardOverview,
  getIotEnergyDashboardTrend,
  type IotEnergyDashboardTrendReqVO
} from '@/api/iot/energy/dashboard'
import { getIotEnergyMeterPage, getIotEnergyMeterListByDeviceId, type IotEnergyMeterVO } from '@/api/iot/energy/meter'
import { getIotEnergyMeterLatestData, getIotEnergyDataAggregate } from '@/api/iot/energy/data'
import { getIotEnergyStatsByMeter, getIotEnergyStatisticsPage } from '@/api/iot/energy/statistics'
import { AlertRecordApi } from '@/api/iot/alert/record'
import { DeviceGroupApi } from '@/api/iot/device/group'
import { DeviceApi } from '@/api/iot/device/device'

echarts.use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

defineOptions({ name: 'MallElectricityScreen' })

// ===== 时间 =====
const currentDateTime = ref('')
const currentWeekday = ref('')
const currentWeather = ref(22)
let clockTimer: ReturnType<typeof setInterval>
let refreshTimer: ReturnType<typeof setInterval>

const updateClock = () => {
  const now = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  currentDateTime.value = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
  const days = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  currentWeekday.value = days[now.getDay()]
}

// ===== 日期工具 =====
const pad = (n: number) => String(n).padStart(2, '0')
const fmtDT = (d: Date) =>
  `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`

const todayRange = () => {
  const n = new Date()
  return {
    start: fmtDT(new Date(n.getFullYear(), n.getMonth(), n.getDate(), 0, 0, 0)),
    end: fmtDT(new Date(n.getFullYear(), n.getMonth(), n.getDate(), 23, 59, 59))
  }
}
const yesterdayRange = () => {
  const n = new Date()
  return {
    start: fmtDT(new Date(n.getFullYear(), n.getMonth(), n.getDate() - 1, 0, 0, 0)),
    end: fmtDT(new Date(n.getFullYear(), n.getMonth(), n.getDate() - 1, 23, 59, 59))
  }
}
const monthRange = () => {
  const n = new Date()
  return {
    start: fmtDT(new Date(n.getFullYear(), n.getMonth(), 1, 0, 0, 0)),
    end: fmtDT(new Date(n.getFullYear(), n.getMonth() + 1, 0, 23, 59, 59))
  }
}

// ===== 平面图位置网格（最多支持 9 个电表）=====
const METER_CELL_W = 180  // 每格宽度 px
const METER_CELL_H = 130  // 每格高度 px
const METER_COLS   = 6    // 最多列数

const computeMeterPos = (idx: number) => {
  const col = idx % METER_COLS
  const row = Math.floor(idx / METER_COLS)
  return { x: 80 + col * METER_CELL_W, y: 50 + row * METER_CELL_H }
}

// 画布尺寸随电表数量自适应
const dynamicCanvasW = computed(() => {
  const n = allMeters.value.length
  const cols = Math.min(n, METER_COLS)
  return Math.max(760, 80 + cols * METER_CELL_W + 40)
})
const dynamicCanvasH = computed(() => {
  const n = allMeters.value.length
  const rows = Math.ceil(n / METER_COLS)
  return Math.max(380, 50 + rows * METER_CELL_H + 40)
})

// ===== 数据类型 =====
interface Meter {
  id: number
  name: string
  shopName: string
  location: string
  status: 'normal' | 'warning' | 'offline'
  power: number
  todayEnergy: number
  monthEnergy: number
  yesterdayEnergy: number
  voltage: number
  current: number
  powerFactor: number
  deviceId: number
  x: number
  y: number
}

interface AlertItem {
  meterName: string
  type: string
  time: string
  level: 'high' | 'medium' | 'low'
}

// ===== 响应式状态 =====
const overview = reactive({ totalEnergy: 0, totalPower: 0, onlineMeters: 0, totalMeters: 0, alertMeters: 0 })
const allMeters = ref<Meter[]>([])
const alerts = ref<AlertItem[]>([])
const deviceGroups = ref<{ id: number; name: string }[]>([])
const selectedGroupId = ref<number | undefined>(undefined)
const selectedMeter = ref<Meter | null>(null)

const ALERT_LEVEL: Record<number, 'high' | 'medium' | 'low'> = { 1: 'high', 2: 'medium', 3: 'low' }

// ===== 加载：总览数据 =====
const loadOverview = async () => {
  try {
    const res = await getIotEnergyDashboardOverview()
    if (res) {
      overview.totalEnergy = res.todayEnergy ?? 0
      overview.onlineMeters = res.onlineMeterCount ?? 0
      overview.totalMeters = (res.onlineMeterCount ?? 0) + (res.offlineMeterCount ?? 0)
      overview.alertMeters = res.alertCount ?? 0
    }
  } catch (e) {
    console.warn('[大屏] 总览加载失败', e)
  }
}

// ===== 加载：电表列表 + 实时数据 + 今日统计 =====
const loadMeters = async () => {
  try {
    // 若选中了设备分组，先获取该分组下的设备 ID，再过滤计量点
    let list: IotEnergyMeterVO[] = []
    if (selectedGroupId.value) {
      // ===== 设备分组模式：直接用设备列表作为电表 =====
      const groupDevices: any[] = []
      let pageNo = 1
      while (true) {
        const deviceRes = await DeviceApi.getDevicePage({ groupId: selectedGroupId.value, pageNo, pageSize: 100 })
        const devices: any[] = deviceRes?.list ?? []
        groupDevices.push(...devices)
        if (devices.length < 100) break
        pageNo++
      }
      if (!groupDevices.length) { allMeters.value = []; return }

      // 获取告警设备集合
      const alertDeviceIds = new Set<number>()
      try {
        const alertRes = await AlertRecordApi.getAlertRecordPage({ pageNo: 1, pageSize: 200, processStatus: false } as any)
        ;(alertRes?.list ?? []).forEach((a: any) => alertDeviceIds.add(a.deviceId))
      } catch {}

      const meters: Meter[] = groupDevices.map((d: any, idx: number) => {
        let status: Meter['status'] = d.state === 1 ? 'normal' : 'offline'
        if (alertDeviceIds.has(d.id)) status = 'warning'
        return {
          id: d.id,
          name: d.nickname || d.deviceName,
          shopName: d.nickname || d.deviceName,
          location: d.serialNumber || d.deviceName,
          deviceId: d.id,
          status,
          power: 0,
          todayEnergy: 0,
          monthEnergy: 0,
          yesterdayEnergy: 0,
          voltage: 0,
          current: 0,
          powerFactor: 0,
          x: computeMeterPos(idx).x,
          y: computeMeterPos(idx).y
        }
      })

      allMeters.value = meters
      overview.totalPower = 0
      if (!selectedMeter.value && meters.length) {
        selectedMeter.value = { ...meters[0] }
        nextTick(() => { initDetailChart(); loadMeterDetail(meters[0].id) })
      }
      return
    }

    // ===== 能源计量点模式（无分组）=====
    const meterRes = await getIotEnergyMeterPage({ pageNo: 1, pageSize: 100, status: 1 } as any)
    list = meterRes?.list ?? []
    if (!list.length) { allMeters.value = []; return }

    const ids = list.map((m: IotEnergyMeterVO) => m.id)
    const today = todayRange()

    // 并行：告警设备集合 + 今日汇总统计 + 各表最新实时数据
    const [alertResult, statsResult, rtResults] = await Promise.allSettled([
      AlertRecordApi.getAlertRecordPage({ pageNo: 1, pageSize: 200, processStatus: false } as any),
      getIotEnergyStatisticsPage({ pageNo: 1, pageSize: 200, period: 'day', startTime: today.start, endTime: today.end } as any),
      Promise.all(ids.map((id: number) => getIotEnergyMeterLatestData(id).catch(() => null)))
    ])

    const alertDeviceIds2 = new Set<number>()
    if (alertResult.status === 'fulfilled') {
      ;(alertResult.value?.list ?? []).forEach((a: any) => alertDeviceIds2.add(a.deviceId))
    }

    const todayMap = new Map<number, number>()
    if (statsResult.status === 'fulfilled') {
      ;(statsResult.value?.list ?? []).forEach((s: any) => {
        todayMap.set(s.meterId, (todayMap.get(s.meterId) ?? 0) + (s.totalEnergy ?? 0))
      })
    }

    const rtMap = new Map<number, any>()
    if (rtResults.status === 'fulfilled') {
      rtResults.value.forEach((rt: any, i: number) => { if (rt) rtMap.set(ids[i], rt) })
    }

    let totalPower = 0
    const meters: Meter[] = list.map((m: IotEnergyMeterVO, idx: number) => {
      const rt = rtMap.get(m.id)
      const power = rt?.instantPower ?? 0
      totalPower += power

      let status: Meter['status'] = 'normal'
      if (!rt) status = 'offline'
      else if (alertDeviceIds2.has(m.deviceId)) status = 'warning'

      return {
        id: m.id,
        name: m.meterName,
        shopName: (m as any).roomName || (m as any).deviceName || m.meterName,
        location: m.meterCode,
        deviceId: m.deviceId,
        status,
        power: +power.toFixed(1),
        todayEnergy: +(todayMap.get(m.id) ?? 0).toFixed(1),
        monthEnergy: 0,
        yesterdayEnergy: 0,
        voltage: +(rt?.voltage ?? 0).toFixed(1),
        current: +(rt?.current ?? 0).toFixed(1),
        powerFactor: +(rt?.powerFactor ?? 0).toFixed(2),
        x: computeMeterPos(idx).x,
        y: computeMeterPos(idx).y
      }
    })

    allMeters.value = meters
    overview.totalPower = +totalPower.toFixed(1)

    if (!selectedMeter.value && meters.length) {
      selectedMeter.value = { ...meters[0] }
      nextTick(() => { initDetailChart(); loadMeterDetail(meters[0].id) })
    }
  } catch (e) {
    console.warn('[大屏] 电表列表加载失败', e)
  }
}

// ===== 加载：告警列表 =====
const loadAlerts = async () => {
  try {
    const res = await AlertRecordApi.getAlertRecordPage({ pageNo: 1, pageSize: 10, processStatus: false } as any)
    alerts.value = (res?.list ?? []).map((a: any) => ({
      meterName: a.configName ?? '未知设备',
      type: a.configName ?? '告警',
      time: a.createTime
        ? new Date(a.createTime).toLocaleTimeString('zh-CN', { hour12: false })
        : '--:--:--',
      level: ALERT_LEVEL[a.configLevel] ?? 'medium'
    }))
  } catch (e) {
    console.warn('[大屏] 告警加载失败', e)
  }
}

// ===== 加载：楼层选项 =====
const loadDeviceGroups = async () => {
  try {
    const res = await DeviceGroupApi.getSimpleDeviceGroupList()
    deviceGroups.value = (res ?? []).map((g: any) => ({ id: g.id, name: g.name }))
  } catch (e) {
    console.warn('[大屏] 设备分组加载失败', e)
  }
}

const onGroupChange = () => {
  selectedMeter.value = null
  allMeters.value = []
  loadMeters()
}

// ===== 选中电表 =====
const selectMeter = (meter: Meter) => {
  selectedMeter.value = { ...meter }
  loadMeterDetail(meter.id)
}

const switchMeter = () => {
  const idx = allMeters.value.findIndex(m => m.id === selectedMeter.value?.id)
  const next = allMeters.value[(idx + 1) % allMeters.value.length]
  if (next) selectMeter(next)
}

// ===== 加载：选中电表详情（实时数据 + 今日/昨日/本月统计）=====
const loadMeterDetail = async (meterId: number) => {
  try {
    // 设备分组模式：直接调设备属性接口
    if (selectedGroupId.value) {
      const meter = allMeters.value.find(m => m.id === meterId)
      if (!meter) return
      const propsRes = await DeviceApi.getLatestDeviceProperties({ deviceId: meter.deviceId }).catch(() => null)
      if (!selectedMeter.value || selectedMeter.value.id !== meterId) return
      if (propsRes) {
        const props: any[] = Array.isArray(propsRes) ? propsRes : []
        const getProp = (...keys: string[]) => {
          for (const k of keys) {
            const p = props.find((x: any) => x.identifier === k)
            if (p?.value != null) return parseFloat(p.value) || 0
          }
          return 0
        }
        selectedMeter.value.voltage = +(getProp('voltage', 'Ua', 'phaseVoltage')).toFixed(1)
        selectedMeter.value.current = +(getProp('current', 'Ia', 'phaseCurrent')).toFixed(1)
        selectedMeter.value.powerFactor = +(getProp('powerFactor', 'cosPhi', 'pf')).toFixed(2)
        selectedMeter.value.power = +(getProp('activePower', 'totalActivePower', 'power', 'instantPower')).toFixed(1)
        selectedMeter.value.todayEnergy = +(getProp('totalEnergy', 'totalActiveEnergy', 'energy')).toFixed(1)
      }
      await loadDetailTrend(meterId)
      return
    }

    const today = todayRange()
    const yesterday = yesterdayRange()
    const month = monthRange()

    const [rtRes, todayRes, yestRes, monthRes] = await Promise.allSettled([
      getIotEnergyMeterLatestData(meterId),
      getIotEnergyStatsByMeter({ meterId, period: 'day', startTime: today.start, endTime: today.end } as any),
      getIotEnergyStatsByMeter({ meterId, period: 'day', startTime: yesterday.start, endTime: yesterday.end } as any),
      getIotEnergyStatsByMeter({ meterId, period: 'month', startTime: month.start, endTime: month.end } as any)
    ])

    if (!selectedMeter.value || selectedMeter.value.id !== meterId) return

    if (rtRes.status === 'fulfilled' && rtRes.value) {
      const rt = rtRes.value as any
      selectedMeter.value.voltage = +(rt.voltage ?? 0).toFixed(1)
      selectedMeter.value.current = +(rt.current ?? 0).toFixed(1)
      selectedMeter.value.powerFactor = +(rt.powerFactor ?? 0).toFixed(2)
      selectedMeter.value.power = +(rt.instantPower ?? 0).toFixed(1)
    }

    const sumE = (list: any[]) => (list ?? []).reduce((s: number, r: any) => s + (r.totalEnergy ?? 0), 0)
    if (todayRes.status === 'fulfilled') selectedMeter.value.todayEnergy = +sumE(todayRes.value).toFixed(1)
    if (yestRes.status === 'fulfilled') selectedMeter.value.yesterdayEnergy = +sumE(yestRes.value).toFixed(1)
    if (monthRes.status === 'fulfilled') selectedMeter.value.monthEnergy = +sumE(monthRes.value).toFixed(1)

    await loadDetailTrend(meterId)
  } catch (e) {
    console.warn('[大屏] 电表详情加载失败', e)
  }
}

// ===== 平面图过滤 =====
const meterSearch = ref('')
const planScale = ref(1)
const panX = ref(0)
const panY = ref(0)
const isDragging = ref(false)
let dragStart = { x: 0, y: 0, panX: 0, panY: 0 }

const canvasStyle = computed(() => ({
  transform: `translate(${panX.value}px, ${panY.value}px) scale(${planScale.value})`,
  transformOrigin: 'center center',
}))

const startDrag = (e: MouseEvent) => {
  isDragging.value = true
  dragStart = { x: e.clientX, y: e.clientY, panX: panX.value, panY: panY.value }
}
const onDrag = (e: MouseEvent) => {
  if (!isDragging.value) return
  panX.value = dragStart.panX + (e.clientX - dragStart.x)
  panY.value = dragStart.panY + (e.clientY - dragStart.y)
}
const stopDrag = () => { isDragging.value = false }

const onWheel = (e: WheelEvent) => {
  const delta = e.deltaY > 0 ? -0.1 : 0.1
  planScale.value = Math.min(Math.max(planScale.value + delta, 0.3), 3)
}

const visibleMeters = computed(() => {
  if (!meterSearch.value) return allMeters.value
  const q = meterSearch.value.toLowerCase()
  return allMeters.value.filter(m =>
    m.name.toLowerCase().includes(q) ||
    m.shopName.toLowerCase().includes(q) ||
    m.location.toLowerCase().includes(q)
  )
})

const filterMeters = () => {}
const zoomIn = () => { planScale.value = Math.min(planScale.value + 0.1, 3) }
const zoomOut = () => { planScale.value = Math.max(planScale.value - 0.1, 0.3) }
const resetZoom = () => { planScale.value = 1; panX.value = 0; panY.value = 0 }

// ===== Tooltip =====
const tooltipVisible = ref(false)
const tooltipMeter = ref<Meter | null>(null)
const tooltipX = ref(0)
const tooltipY = ref(0)

const showTooltip = (meter: Meter, _event?: MouseEvent) => {
  tooltipMeter.value = meter
  tooltipX.value = meter.x + 90
  tooltipY.value = meter.y + 10
  tooltipVisible.value = true
}
const hideTooltip = () => { tooltipVisible.value = false }

// ===== 电表列表过滤 =====
const listStatusFilter = ref('')
const listFloorFilter = ref('')
const listCircuitFilter = ref('')
const listSearch = ref('')
const listViewMode = ref<'grid' | 'list'>('grid')
const meterCardsRef = ref<HTMLElement | null>(null)

const filteredListMeters = computed(() =>
  allMeters.value.filter(m => {
    if (listStatusFilter.value && m.status !== listStatusFilter.value) return false
    if (listSearch.value) {
      const q = listSearch.value.toLowerCase()
      if (!m.name.toLowerCase().includes(q) && !m.shopName.toLowerCase().includes(q) && !m.location.toLowerCase().includes(q)) return false
    }
    return true
  })
)

const scrollCards = (dir: number) => {
  if (meterCardsRef.value) meterCardsRef.value.scrollLeft += dir * 220
}

const statusLabel = (status: string) =>
  ({ normal: '运行正常', warning: '告警', offline: '离线' }[status] ?? status)

// ===== 全场用电趋势图 =====
const trendTabs = [
  { key: 'today', label: '今日' },
  { key: 'month', label: '本月' },
  { key: 'year', label: '本年' }
]
const activeTrendTab = ref('today')
const trendChartRef = ref<HTMLElement | null>(null)
let trendChartInstance: echarts.ECharts | null = null
const trendChartLabels = ref<string[]>([])
const trendChartValues = ref<number[]>([])

const periodMap: Record<string, string> = { today: '24h', month: '30d', year: '12m' }

const loadTrend = async (tabKey = 'today') => {
  try {
    const res = await getIotEnergyDashboardTrend({ period: periodMap[tabKey] ?? '24h' } as IotEnergyDashboardTrendReqVO)
    const list: any[] = Array.isArray(res) ? res : (res?.list ?? [])
    if (list.length) {
      trendChartLabels.value = list.map((d: any) => {
        const t = new Date(d.time)
        return `${pad(t.getHours())}:00`
      })
      trendChartValues.value = list.map((d: any) => d.energyValue ?? 0)
    }
  } catch (e) {
    console.warn('[大屏] 趋势数据加载失败', e)
  }
  updateTrendChart()
}

const switchTrendTab = (key: string) => { activeTrendTab.value = key; loadTrend(key) }

const initTrendChart = () => {
  if (!trendChartRef.value) return
  trendChartInstance = echarts.init(trendChartRef.value, 'dark')
  updateTrendChart()
}

const MOCK_TREND_HOURS = Array.from({ length: 25 }, (_, i) => `${pad(i)}:00`)
const MOCK_TREND_VALS = [320, 280, 240, 220, 200, 210, 380, 620, 850, 1100, 1320, 1480, 1526, 1490, 1380, 1250, 1180, 1320, 1410, 1380, 1200, 980, 720, 520, 380]

const updateTrendChart = () => {
  if (!trendChartInstance) return
  const labels = trendChartLabels.value.length ? trendChartLabels.value : MOCK_TREND_HOURS
  const values = trendChartValues.value.length ? trendChartValues.value : MOCK_TREND_VALS
  trendChartInstance.setOption({
    backgroundColor: 'transparent',
    grid: { top: 10, right: 10, bottom: 20, left: 40 },
    tooltip: { trigger: 'axis', backgroundColor: '#0d1b2a', borderColor: '#1e4d7b' },
    xAxis: {
      type: 'category', data: labels,
      axisLine: { lineStyle: { color: '#1e4d7b' } },
      axisLabel: { color: '#4a9dd4', fontSize: 9, interval: 4 },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value', axisLine: { show: false },
      splitLine: { lineStyle: { color: '#1e4d7b', type: 'dashed' } },
      axisLabel: { color: '#4a9dd4', fontSize: 9 }
    },
    series: [{
      type: 'line', data: values, smooth: true, symbol: 'none',
      lineStyle: { color: '#1a6fad', width: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(26,111,173,0.5)' },
          { offset: 1, color: 'rgba(26,111,173,0.05)' }
        ])
      }
    }]
  })
}

// ===== 选中电表详情趋势图 =====
const detailTrendTabs = [
  { key: '15m', label: '15分钟' },
  { key: '1h', label: '1小时' },
  { key: '1d', label: '1天' }
]
const activeDetailTab = ref('1h')
const detailChartRef = ref<HTMLElement | null>(null)
let detailChartInstance: echarts.ECharts | null = null
const detailChartLabels = ref<string[]>([])
const detailChartValues = ref<number[]>([])

const intervalMap: Record<string, string> = { '15m': '15m', '1h': '1h', '1d': '1h' }

const loadDetailTrend = async (meterId: number) => {
  try {
    const today = todayRange()
    const res = await getIotEnergyDataAggregate({
      meterIds: [meterId],
      startTime: today.start,
      endTime: today.end,
      interval: intervalMap[activeDetailTab.value] ?? '1h'
    } as any)
    const list: any[] = Array.isArray(res) ? res : (res?.list ?? [])
    if (list.length) {
      detailChartLabels.value = list.map((d: any) => {
        const t = new Date(d.ts)
        return `${pad(t.getHours())}:00`
      })
      detailChartValues.value = list.map((d: any) => +((d.sumValue ?? d.avgValue ?? 0).toFixed(2)))
    }
  } catch (e) {
    console.warn('[大屏] 详情趋势加载失败', e)
  }
  updateDetailChart()
}

const switchDetailTab = (key: string) => {
  activeDetailTab.value = key
  if (selectedMeter.value) loadDetailTrend(selectedMeter.value.id)
}

const initDetailChart = () => {
  if (!detailChartRef.value) return
  if (detailChartInstance) detailChartInstance.dispose()
  detailChartInstance = echarts.init(detailChartRef.value, 'dark')
  updateDetailChart()
}

const MOCK_DETAIL_VALS = [8, 7, 6, 5, 5, 6, 12, 20, 28, 34, 40, 45, 48, 47, 43, 39, 37, 41, 44, 43, 38, 30, 22, 16, 12]

const updateDetailChart = () => {
  if (!detailChartInstance) return
  const labels = detailChartLabels.value.length ? detailChartLabels.value : MOCK_TREND_HOURS
  const values = detailChartValues.value.length ? detailChartValues.value : MOCK_DETAIL_VALS
  detailChartInstance.setOption({
    backgroundColor: 'transparent',
    grid: { top: 8, right: 8, bottom: 18, left: 36 },
    tooltip: { trigger: 'axis', backgroundColor: '#0d1b2a', borderColor: '#1e4d7b' },
    xAxis: {
      type: 'category', data: labels,
      axisLine: { lineStyle: { color: '#1e4d7b' } },
      axisLabel: { color: '#4a9dd4', fontSize: 8, interval: 5 },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value', axisLine: { show: false },
      splitLine: { lineStyle: { color: '#1e4d7b', type: 'dashed' } },
      axisLabel: { color: '#4a9dd4', fontSize: 8 }
    },
    series: [{
      type: 'line', data: values, smooth: true, symbol: 'none',
      lineStyle: { color: '#1a6fad', width: 1.5 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(26,111,173,0.4)' },
          { offset: 1, color: 'rgba(26,111,173,0.02)' }
        ])
      }
    }]
  })
}

// ===== 全屏 =====
const screenRef = ref<HTMLElement | null>(null)
const toggleFullscreen = () => {
  if (!document.fullscreenElement) screenRef.value?.requestFullscreen()
  else document.exitFullscreen()
}

// ===== 占位事件 =====
const showAlerts = () => {}
const showEvents = () => {}

// ===== 格式化 =====
const formatNumber = (val: number) =>
  val.toLocaleString('zh-CN', { minimumFractionDigits: 1, maximumFractionDigits: 1 })

// ===== resize =====
const handleResize = () => {
  trendChartInstance?.resize()
  detailChartInstance?.resize()
}

// ===== 定时刷新（30s）=====
const refresh = () => {
  Promise.allSettled([loadOverview(), loadMeters(), loadAlerts()])
  if (selectedMeter.value) loadMeterDetail(selectedMeter.value.id)
}

onMounted(async () => {
  updateClock()
  clockTimer = setInterval(updateClock, 1000)
  refreshTimer = setInterval(refresh, 30_000)

  await Promise.allSettled([loadDeviceGroups(), loadOverview(), loadMeters(), loadAlerts(), loadTrend('today')])

  await nextTick(() => { initTrendChart(); initDetailChart() })
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  clearInterval(clockTimer)
  clearInterval(refreshTimer)
  trendChartInstance?.dispose()
  detailChartInstance?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
// ===== 全局色值 =====
$bg-main: #060e1a;
$bg-panel: #091525;
$bg-section: #0d1b2a;
$bg-card: #0f2035;
$border-color: #1a3a5c;
$text-primary: #e0f0ff;
$text-secondary: #6a9fcf;
$text-dim: #3a6a9a;
$accent-blue: #1a9fdf;
$accent-cyan: #00e5ff;
$accent-green: #00e676;
$accent-orange: #ff9800;
$accent-red: #f44336;

// ===== 主容器 =====
.mall-screen {
  width: 100vw;
  height: 100vh;
  background: $bg-main;
  display: flex;
  flex-direction: column;
  color: $text-primary;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  overflow: hidden;
  position: fixed;
  inset: 0;
  z-index: 9999;
}

// ===== Header =====
.screen-header {
  height: 52px;
  background: linear-gradient(180deg, #0a2040 0%, #06131f 100%);
  border-bottom: 1px solid $border-color;
  display: flex;
  align-items: center;
  padding: 0 16px;
  flex-shrink: 0;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    bottom: 0; left: 0; right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, $accent-blue, transparent);
  }
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 280px;
  font-size: 13px;
  color: $text-secondary;
}

.weather-icon { font-size: 18px; }
.header-info { color: $text-secondary; font-size: 13px; }
.header-divider { color: $border-color; }

.header-center {
  flex: 1;
  text-align: center;
}

.screen-title {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 4px;
  text-shadow: 0 0 20px rgba(26, 159, 223, 0.6);
  margin: 0;
  background: linear-gradient(180deg, #ffffff 0%, #82c8ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 280px;
  justify-content: flex-end;
}

.header-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  color: $text-secondary;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;

  &:hover { background: rgba(26, 159, 223, 0.1); color: $accent-blue; }
  .btn-icon { font-size: 14px; }
}

.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 16px;
  height: 16px;
  border-radius: 8px;
  font-size: 10px;
  padding: 0 4px;
  font-weight: 700;

  &.badge-red { background: $accent-red; color: #fff; }
  &.badge-blue { background: $accent-blue; color: #fff; }
}

// ===== Body =====
.screen-body {
  flex: 1;
  display: flex;
  gap: 8px;
  padding: 8px;
  overflow: hidden;
  min-height: 0;
}

// ===== Panel =====
.panel-left,
.panel-right {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow: hidden;
}

.panel-right {
  width: 300px;
}

.panel-center {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow: hidden;
  min-width: 0;
}

.panel-right-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  background: $bg-panel;
  border: 1px solid $border-color;
  border-radius: 6px;

  .empty-hint { color: $text-dim; font-size: 14px; }
}

.panel-section {
  background: $bg-panel;
  border: 1px solid $border-color;
  border-radius: 6px;
  padding: 10px 12px;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0; left: 0; right: 0;
    height: 2px;
    background: linear-gradient(90deg, transparent, $accent-blue 40%, transparent);
    opacity: 0.6;
  }
}

.flex-1 { flex: 1; overflow: hidden; display: flex; flex-direction: column; }

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.more-link {
  font-size: 11px;
  color: $accent-blue;
  cursor: pointer;
  &:hover { text-decoration: underline; }
}

// ===== 概览卡片 =====
.overview-cards {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.overview-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  background: $bg-section;
  border-radius: 4px;
  border: 1px solid $border-color;

  &.overview-card-alert { border-color: rgba($accent-red, 0.4); }
}

.ov-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;

  &.ov-icon-energy { background: rgba(26, 100, 200, 0.3); }
  &.ov-icon-power { background: rgba(0, 200, 200, 0.2); }
  &.ov-icon-online { background: rgba(50, 100, 150, 0.3); }
  &.ov-icon-alert { background: rgba(200, 50, 50, 0.3); color: $accent-red; }
}

.ov-content { flex: 1; min-width: 0; }
.ov-label { font-size: 11px; color: $text-secondary; margin-bottom: 2px; }
.ov-value {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 1px;

  &.ov-value-blue { color: $accent-blue; text-shadow: 0 0 12px rgba(26, 159, 223, 0.6); }
  &.ov-value-cyan { color: $accent-cyan; text-shadow: 0 0 12px rgba(0, 229, 255, 0.5); }
  &.ov-value-white { color: $text-primary; }
  &.ov-value-red { color: $accent-red; text-shadow: 0 0 12px rgba(244, 67, 54, 0.5); }
}

// ===== 告警列表 =====
.alert-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;

  &::-webkit-scrollbar { width: 3px; }
  &::-webkit-scrollbar-track { background: transparent; }
  &::-webkit-scrollbar-thumb { background: $border-color; border-radius: 2px; }
}

.alert-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 8px;
  border-radius: 3px;
  font-size: 12px;
  background: $bg-section;

  &.alert-level-high .alert-dot { background: $accent-red; box-shadow: 0 0 6px $accent-red; }
  &.alert-level-medium .alert-dot { background: $accent-orange; box-shadow: 0 0 6px $accent-orange; }
  &.alert-level-low .alert-dot { background: #ffeb3b; box-shadow: 0 0 6px #ffeb3b; }
}

.alert-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}

.alert-text { flex: 1; color: $text-primary; }
.alert-time { color: $text-dim; font-size: 11px; flex-shrink: 0; }

// ===== 趋势图 =====
.trend-tabs {
  display: flex;
  gap: 2px;
}

.trend-tab {
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 11px;
  cursor: pointer;
  color: $text-secondary;
  background: $bg-section;
  border: 1px solid $border-color;

  &.active { background: $accent-blue; color: #fff; border-color: $accent-blue; }
  &:hover:not(.active) { color: $accent-blue; }
}

.trend-chart {
  height: 120px;
  flex-shrink: 0;
}

// ===== 中间：平面图区域 =====
.floor-plan-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.floor-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  flex-shrink: 0;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: $text-secondary;
}

.dark-select {
  background: $bg-section;
  border: 1px solid $border-color;
  color: $text-primary;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  outline: none;
  cursor: pointer;

  &.sm { padding: 2px 6px; font-size: 11px; }
  &:focus { border-color: $accent-blue; }
  option { background: $bg-section; }
}

.search-box {
  display: flex;
  align-items: center;
  position: relative;

  &.sm .dark-input { padding: 2px 24px 2px 8px; font-size: 11px; }
}

.dark-input {
  background: $bg-section;
  border: 1px solid $border-color;
  color: $text-primary;
  padding: 4px 28px 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  outline: none;
  width: 160px;

  &:focus { border-color: $accent-blue; }
  &::placeholder { color: $text-dim; }
}

.search-icon {
  position: absolute;
  right: 8px;
  font-size: 12px;
  color: $text-dim;
  pointer-events: none;
}

.legend {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: $text-secondary;
  margin-left: auto;
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;

  &.dot-normal { background: $accent-green; }
  &.dot-warning { background: $accent-orange; }
  &.dot-offline { background: #607d8b; }
}

.floor-plan {
  flex: 1;
  background: #060f1c;
  border: 1px solid $border-color;
  border-radius: 4px;
  position: relative;
  overflow: hidden;
  min-height: 0;
  cursor: grab;
  user-select: none;

  &.is-dragging {
    cursor: grabbing;
  }
}

.map-controls {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 10;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.map-btn {
  width: 28px;
  height: 28px;
  background: rgba(10, 30, 60, 0.9);
  border: 1px solid $border-color;
  color: $text-secondary;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover { background: rgba(26, 159, 223, 0.2); color: $accent-blue; }
}

.compass {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 10;
  text-align: center;
  font-size: 10px;
  color: $text-secondary;

  .compass-n { font-weight: 700; color: $accent-blue; }
  .compass-arrow { font-size: 16px; }
}

.plan-canvas {
  position: relative;
  min-width: 760px;
  min-height: 380px;
  margin: 30px auto 0;
  transform-origin: center center;
}

.plan-svg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

// ===== 电表图标 =====
.meter-icon-wrapper {
  position: absolute;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 5;
  transform: translate(-50%, -50%);

  &:hover .meter-icon { filter: brightness(1.3); }
}

.meter-select-ring {
  position: absolute;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 2px solid $accent-blue;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -70%);
  animation: ring-pulse 1.5s ease-in-out infinite;
  box-shadow: 0 0 16px rgba(26, 159, 223, 0.5);
  pointer-events: none;
  z-index: 1;
}

@keyframes ring-pulse {
  0%, 100% { transform: translate(-50%, -70%) scale(1); opacity: 0.8; }
  50% { transform: translate(-50%, -70%) scale(1.12); opacity: 1; }
}

.meter-icon {
  width: 52px;
  height: 64px;
  position: relative;
  z-index: 2;

  &.meter-status-normal { filter: drop-shadow(0 0 6px rgba(0, 230, 118, 0.5)); }
  &.meter-status-warning { filter: drop-shadow(0 0 8px rgba(255, 152, 0, 0.7)); }
  &.meter-status-offline { filter: grayscale(0.8) brightness(0.6); }
}

.meter-body {
  width: 52px;
  height: 64px;
  background: linear-gradient(180deg, #1a2a3a 0%, #0d1a2a 100%);
  border-radius: 4px;
  padding: 6px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  position: relative;

  .meter-status-normal & { border: 1px solid #00e676; }
  .meter-status-warning & { border: 2px solid $accent-orange; }
  .meter-status-offline & { border: 1px solid #455a64; }
}

.meter-screen {
  background: #0a1a10;
  border: 1px solid rgba(0, 200, 80, 0.4);
  border-radius: 2px;
  padding: 4px 3px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3px;
  justify-content: center;
}

.meter-screen-line {
  height: 2px;
  background: rgba(0, 200, 80, 0.7);
  border-radius: 1px;

  &.short { width: 60%; }
}

.meter-ports {
  display: flex;
  gap: 4px;
  justify-content: center;
  padding-top: 2px;
}

.meter-port {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #1a3a2a;
  border: 1px solid rgba(0, 200, 80, 0.5);
}

.meter-alert-dot {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: $accent-red;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  box-shadow: 0 0 8px $accent-red;
}

.meter-label {
  text-align: center;
  margin-top: 4px;

  .meter-name-text {
    font-size: 11px;
    font-weight: 600;
    color: $text-primary;
    white-space: nowrap;
  }

  .meter-shop-text {
    font-size: 10px;
    color: $text-secondary;
    white-space: nowrap;
  }

  .meter-pos-text {
    font-size: 10px;
    color: $text-dim;
    white-space: nowrap;
  }
}

// ===== Tooltip =====
.meter-tooltip {
  position: absolute;
  background: rgba(6, 18, 34, 0.95);
  border: 1px solid $accent-blue;
  border-radius: 6px;
  padding: 10px 14px;
  z-index: 20;
  min-width: 180px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.6);
  pointer-events: none;
}

.tooltip-title {
  font-size: 13px;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: 6px;
  border-bottom: 1px solid $border-color;
  padding-bottom: 4px;
}

.tooltip-row {
  display: flex;
  gap: 4px;
  font-size: 11px;
  margin-bottom: 3px;
  color: $text-secondary;

  span:first-child { color: $text-dim; flex-shrink: 0; }
  span:last-child { color: $text-primary; }
}

// ===== 电表列表底部 =====
.meter-list-section {
  height: 180px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

.list-filters {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: nowrap;
}

.view-btns {
  display: flex;
  gap: 2px;
}

.view-btn {
  width: 24px;
  height: 24px;
  background: $bg-section;
  border: 1px solid $border-color;
  color: $text-secondary;
  border-radius: 3px;
  cursor: pointer;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;

  &.active { background: $accent-blue; color: #fff; border-color: $accent-blue; }
}

.meter-cards-scroll {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 4px;
  overflow: hidden;
  min-height: 0;
}

.scroll-btn {
  width: 24px;
  height: 80px;
  background: $bg-section;
  border: 1px solid $border-color;
  color: $text-secondary;
  border-radius: 4px;
  cursor: pointer;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &:hover { background: rgba(26, 159, 223, 0.2); color: $accent-blue; }
}

.meter-cards {
  flex: 1;
  display: flex;
  gap: 8px;
  overflow-x: auto;
  scroll-behavior: smooth;
  padding: 4px 0;

  &::-webkit-scrollbar { height: 3px; }
  &::-webkit-scrollbar-track { background: transparent; }
  &::-webkit-scrollbar-thumb { background: $border-color; border-radius: 2px; }
}

.meter-card {
  flex-shrink: 0;
  width: 200px;
  background: $bg-section;
  border-radius: 6px;
  padding: 8px 10px;
  cursor: pointer;
  border: 1px solid $border-color;
  transition: all 0.2s;

  &:hover { border-color: $accent-blue; }
  &.meter-card-selected { border-color: $accent-blue; background: rgba(26, 159, 223, 0.1); }
  &.meter-card-warning { border-color: rgba($accent-orange, 0.6); background: rgba($accent-orange, 0.05); }
}

.mc-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.mc-icon { width: 36px; height: 44px; flex-shrink: 0; }

.mc-meter-img {
  width: 36px;
  height: 44px;
  background: linear-gradient(180deg, #1a2a3a 0%, #0d1a2a 100%);
  border: 1px solid $border-color;
  border-radius: 3px;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    top: 8px; left: 4px; right: 4px; bottom: 16px;
    background: rgba(0, 200, 80, 0.15);
    border: 1px solid rgba(0, 200, 80, 0.3);
    border-radius: 1px;
  }
}

.mc-info { flex: 1; min-width: 0; }

.mc-name {
  font-size: 13px;
  font-weight: 600;
  color: $text-primary;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.mc-status {
  font-size: 11px;
  display: flex;
  align-items: center;
  gap: 3px;

  .status-dot-sm {
    width: 6px;
    height: 6px;
    border-radius: 50%;
  }
}

.mc-shop {
  font-size: 12px;
  color: $text-secondary;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.mc-location {
  font-size: 11px;
  color: $text-dim;
  margin-bottom: 6px;

  .location-icon { margin-right: 2px; }
}

.mc-stats {
  display: flex;
  justify-content: space-between;
}

.mc-stat {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.mc-stat-label { font-size: 10px; color: $text-dim; }
.mc-stat-value { font-size: 12px; font-weight: 600; color: $text-primary; }
.mc-stat-value.highlight { color: $accent-blue; }

// ===== 右侧详情 =====
.right-detail {
  flex: 1;
  overflow-y: auto;

  &::-webkit-scrollbar { width: 3px; }
  &::-webkit-scrollbar-track { background: transparent; }
  &::-webkit-scrollbar-thumb { background: $border-color; border-radius: 2px; }
}

.switch-btn {
  font-size: 11px;
  padding: 3px 8px;
  background: $bg-section;
  border: 1px solid $accent-blue;
  color: $accent-blue;
  border-radius: 3px;
  cursor: pointer;

  &:hover { background: rgba(26, 159, 223, 0.2); }
}

.detail-meter-title {
  font-size: 14px;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.detail-status { font-size: 12px; }

.detail-info-grid {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.detail-img-wrap {
  width: 70px;
  flex-shrink: 0;
}

.detail-meter-img {
  width: 70px;
  height: 90px;
  background: linear-gradient(180deg, #1a2a3a 0%, #0d1a2a 100%);
  border: 1px solid $accent-green;
  border-radius: 4px;
  position: relative;
  box-shadow: 0 0 10px rgba(0, 230, 118, 0.2);

  &::before {
    content: '';
    position: absolute;
    top: 12px; left: 6px; right: 6px; bottom: 28px;
    background: rgba(0, 200, 80, 0.1);
    border: 1px solid rgba(0, 200, 80, 0.3);
    border-radius: 2px;
  }
  &::after {
    content: '';
    position: absolute;
    bottom: 10px; left: 12px; right: 12px;
    height: 8px;
    display: flex;
    gap: 4px;
    background:
      radial-gradient(circle at 30% 50%, rgba(0,200,80,0.4) 40%, transparent 40%),
      radial-gradient(circle at 70% 50%, rgba(0,200,80,0.4) 40%, transparent 40%);
  }
}

.detail-info-list { flex: 1; }

.di-row {
  display: flex;
  font-size: 11px;
  margin-bottom: 3px;
  gap: 2px;
}

.di-label { color: $text-dim; flex-shrink: 0; }
.di-val { color: $text-primary; }

.detail-stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
  margin-bottom: 8px;
}

.detail-stat-card {
  background: $bg-section;
  border: 1px solid $border-color;
  border-radius: 4px;
  padding: 7px 10px;
}

.ds-label { font-size: 10px; color: $text-dim; margin-bottom: 2px; }
.ds-value {
  font-size: 18px;
  font-weight: 700;

  &.ds-blue { color: $accent-blue; text-shadow: 0 0 10px rgba(26, 159, 223, 0.5); }
  &.ds-white { color: $text-primary; }
}

.detail-params-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 6px;
  margin-bottom: 8px;
}

.param-card {
  background: $bg-section;
  border: 1px solid $border-color;
  border-radius: 4px;
  padding: 6px 8px;
  text-align: center;
}

.param-label { font-size: 10px; color: $text-dim; margin-bottom: 2px; }
.param-value { font-size: 15px; font-weight: 600; color: $text-primary; }

.detail-status-row {
  display: flex;
  gap: 16px;
  font-size: 11px;
  color: $text-secondary;
  margin-bottom: 10px;
}

.status-item { display: flex; align-items: center; gap: 4px; }

.detail-trend-header {
  font-size: 12px;
  color: $text-secondary;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.detail-trend-tabs { display: flex; gap: 2px; }

.detail-trend-tab {
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 10px;
  cursor: pointer;
  color: $text-dim;
  background: $bg-section;
  border: 1px solid $border-color;

  &.active { background: $accent-blue; color: #fff; border-color: $accent-blue; }
}

.detail-chart {
  height: 120px;
  width: 100%;
}

// ===== 通用状态颜色 =====
.status-text-normal { color: $accent-green; }
.status-text-warning { color: $accent-orange; }
.status-text-offline { color: #607d8b; }
</style>
