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
      <el-form-item label="设备名称" prop="deviceName">
        <el-input
          v-model="queryParams.deviceName"
          placeholder="请输入设备名称"
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
      <el-form-item label="设备状态" prop="state">
        <el-select v-model="queryParams.state" placeholder="请选择状态" clearable class="!w-150px">
          <el-option label="未激活" :value="0" />
          <el-option label="在线" :value="1" />
          <el-option label="离线" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="success" @click="handleRefresh" :loading="loading">
          <Icon icon="ep:refresh-right" class="mr-5px" /> 刷新数据
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 电表展示区域 -->
  <ContentWrap>
    <div class="meter-panel" v-loading="loading">
      <div v-if="list.length === 0 && !loading" class="no-data">
        <el-empty description="暂无电表设备数据" />
      </div>
      <div class="meter-grid">
        <div
          v-for="device in list"
          :key="device.id"
          class="meter-unit"
          :class="{ 'is-offline': device.state !== 1 }"
        >
          <!-- 电表外壳 -->
          <div class="meter-housing">
            <!-- 顶部螺丝装饰 -->
            <div class="screw-row top">
              <div class="screw"></div>
              <div class="screw"></div>
            </div>

            <!-- 型号标签区 -->
            <div class="meter-label-area">
              <div class="brand-label">HR-IoT</div>
              <div class="model-label">DDS102</div>
            </div>

            <!-- 设备名称 -->
            <div class="device-info">
              <span class="device-name">{{ device.nickname || device.deviceName }}</span>
            </div>

            <!-- 主显示窗口 -->
            <div class="display-window">
              <div class="display-frame">
                <!-- 滚轮数字显示 -->
                <div class="roller-display">
                  <div class="roller-label">kWh</div>
                  <div class="roller-digits">
                    <template v-for="(digit, index) in formatEnergyDigits(device.energy)" :key="index">
                      <div
                        v-if="digit !== '.'"
                        class="roller-digit"
                        :class="{ 'decimal-place': isDecimalPlace(formatEnergyDigits(device.energy), index) }"
                      >
                        <span class="digit-value">{{ digit }}</span>
                      </div>
                      <div v-else class="decimal-dot"></div>
                    </template>
                  </div>
                </div>
              </div>
            </div>

            <!-- LCD辅助显示 -->
            <div class="lcd-display">
              <div class="lcd-screen">
                <div class="lcd-row">
                  <span class="lcd-label">状态</span>
                  <span class="lcd-value" :class="getStateClass(device.state)">
                    {{ getStateText(device.state) }}
                  </span>
                </div>
                <div class="lcd-row">
                  <span class="lcd-label">更新</span>
                  <span class="lcd-value">{{ formatUpdateTime(device.energyUpdateTime) }}</span>
                </div>
              </div>
            </div>

            <!-- 指示灯区域 -->
            <div class="indicator-area">
              <div class="indicator" :class="{ active: device.state === 1 }">
                <div class="indicator-led"></div>
                <span class="indicator-text">RUN</span>
              </div>
              <div class="indicator warning" :class="{ active: device.state === 2 }">
                <div class="indicator-led"></div>
                <span class="indicator-text">ALM</span>
              </div>
            </div>

            <!-- 底部产品信息 -->
            <div class="product-info">
              <span>{{ device.productName || '单相电能表' }}</span>
            </div>

            <!-- 底部螺丝装饰 -->
            <div class="screw-row bottom">
              <div class="screw"></div>
              <div class="screw"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

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
import { DeviceApi } from '@/api/iot/device/device'
import { ProductApi } from '@/api/iot/product/product'

/** IoT 电表能耗展示页面 */
defineOptions({ name: 'EnergyMeterDisplay' })

const loading = ref(true)
const list = ref<any[]>([])
const total = ref(0)
const productList = ref<any[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 12,
  deviceName: undefined,
  productId: undefined,
  state: undefined
})
const queryFormRef = ref()

/** 格式化能耗数字为电表显示格式（支持最高9位整数+2位小数） */
const formatEnergyDigits = (energy: number | null | undefined): string[] => {
  if (energy === null || energy === undefined) {
    return ['0', '0', '0', '0', '0', '0', '.', '0', '0']
  }
  const formatted = energy.toFixed(2)
  const [intPart, decPart] = formatted.split('.')
  // 动态计算整数位数，最少6位，最多9位
  const minDigits = 6
  const actualLength = Math.max(minDigits, intPart.length)
  const paddedInt = intPart.padStart(actualLength, '0')
  return [...paddedInt, '.', ...decPart]
}

/** 判断是否为小数位（小数点后的数字） */
const isDecimalPlace = (digits: string[], index: number): boolean => {
  const dotIndex = digits.indexOf('.')
  return dotIndex !== -1 && index > dotIndex
}

/** 格式化更新时间 */
const formatUpdateTime = (time: any): string => {
  if (!time) return '--:--:--'
  return formatDate(time, 'HH:mm:ss')
}

/** 获取状态文本 */
const getStateText = (state: number): string => {
  if (state === 1) return '在线'
  if (state === 2) return '离线'
  return '未激活'
}

/** 获取状态类名 */
const getStateClass = (state: number): string => {
  if (state === 1) return 'online'
  if (state === 2) return 'offline'
  return 'inactive'
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
  loading.value = true
  try {
    const data = await DeviceApi.getDevicePageWithEnergy(queryParams)
    list.value = data.list
    total.value = data.total
  } catch (error) {
    console.error('获取设备能耗数据失败:', error)
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
  queryFormRef.value?.resetFields()
  handleQuery()
}

/** 刷新数据 */
const handleRefresh = () => {
  getList()
}

/** 初始化 */
onMounted(() => {
  getProductList()
  getList()
})
</script>

<style scoped lang="scss">
/* 工业电表风格变量 */
$housing-color: #e8e4df;
$housing-dark: #d4d0cb;
$housing-light: #f5f3f0;
$frame-color: #2c2c2c;
$screw-color: #8a8a8a;
$lcd-bg: #c5cba3;
$lcd-text: #2a3a1a;
$digit-bg: #1a1a1a;
$digit-color: #ffffff;
$decimal-bg: #c41e3a;
$led-green: #22c55e;
$led-red: #ef4444;
$led-off: #4a4a4a;

.meter-panel {
  min-height: 400px;
  background: linear-gradient(180deg, #f0f0f0 0%, #e0e0e0 100%);
  border-radius: 4px;
  padding: 20px;
}

.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.meter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 24px;
}

.meter-unit {
  transition: transform 0.2s ease;

  &:hover {
    transform: translateY(-2px);
  }

  &.is-offline {
    opacity: 0.75;

    .meter-housing {
      background: linear-gradient(180deg, #d8d8d8 0%, #c8c8c8 50%, #b8b8b8 100%);
    }
  }
}

.meter-housing {
  background: linear-gradient(180deg, $housing-light 0%, $housing-color 50%, $housing-dark 100%);
  border-radius: 8px;
  padding: 12px 14px;
  box-shadow:
    0 4px 12px rgba(0, 0, 0, 0.15),
    0 1px 3px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  border: 1px solid #b8b4af;
  position: relative;
}

/* 螺丝装饰 */
.screw-row {
  display: flex;
  justify-content: space-between;
  padding: 0 4px;

  &.top {
    margin-bottom: 8px;
  }

  &.bottom {
    margin-top: 8px;
  }
}

.screw {
  width: 10px;
  height: 10px;
  background: linear-gradient(145deg, #999 0%, #666 100%);
  border-radius: 50%;
  box-shadow:
    inset 0 1px 2px rgba(255, 255, 255, 0.3),
    0 1px 2px rgba(0, 0, 0, 0.2);
  position: relative;

  &::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 6px;
    height: 1px;
    background: #444;
    box-shadow: 0 0 0 0.5px rgba(255, 255, 255, 0.2);
  }
}

/* 型号标签 */
.meter-label-area {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  padding: 0 2px;
}

.brand-label {
  font-family: 'Arial Black', Arial, sans-serif;
  font-size: 11px;
  font-weight: 900;
  color: #1a5fb4;
  letter-spacing: -0.5px;
}

.model-label {
  font-family: 'Courier New', monospace;
  font-size: 9px;
  color: #666;
  background: rgba(0, 0, 0, 0.05);
  padding: 1px 4px;
  border-radius: 2px;
}

/* 设备名称 */
.device-info {
  text-align: center;
  margin-bottom: 8px;
  padding: 4px 8px;
  background: rgba(0, 0, 0, 0.03);
  border-radius: 3px;
}

.device-name {
  font-family: 'Microsoft YaHei', sans-serif;
  font-size: 13px;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}

/* 主显示窗口 */
.display-window {
  background: $frame-color;
  border-radius: 6px;
  padding: 6px;
  box-shadow:
    inset 0 2px 4px rgba(0, 0, 0, 0.3),
    0 1px 0 rgba(255, 255, 255, 0.1);
}

.display-frame {
  background: linear-gradient(180deg, #0d0d0d 0%, #1a1a1a 100%);
  border-radius: 4px;
  padding: 8px 6px;
  border: 2px solid #444;
  overflow: hidden;
}

/* 滚轮数字显示 */
.roller-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.roller-label {
  font-family: 'Arial', sans-serif;
  font-size: 10px;
  color: #888;
  letter-spacing: 1px;
}

.roller-digits {
  display: flex;
  align-items: center;
  gap: 1px;
  flex-wrap: nowrap;
}

.roller-digit {
  width: 18px;
  height: 26px;
  background: linear-gradient(
    180deg,
    #0a0a0a 0%,
    $digit-bg 15%,
    $digit-bg 85%,
    #0a0a0a 100%
  );
  border-radius: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    inset 0 1px 2px rgba(0, 0, 0, 0.5),
    0 1px 0 rgba(255, 255, 255, 0.05);
  border: 1px solid #333;
  flex-shrink: 0;

  &.decimal-place {
    background: linear-gradient(
      180deg,
      #8b0000 0%,
      $decimal-bg 15%,
      $decimal-bg 85%,
      #8b0000 100%
    );
    border-color: #7a1a2e;
  }
}

.digit-value {
  font-family: 'Consolas', 'Courier New', monospace;
  font-size: 17px;
  font-weight: bold;
  color: $digit-color;
  text-shadow: 0 0 1px rgba(255, 255, 255, 0.5);
  line-height: 1;
}

.decimal-dot {
  width: 5px;
  height: 5px;
  background: $digit-color;
  border-radius: 50%;
  margin: 0 1px 5px;
  align-self: flex-end;
  box-shadow: 0 0 2px rgba(255, 255, 255, 0.5);
  flex-shrink: 0;
}

/* LCD辅助显示 */
.lcd-display {
  margin-top: 10px;
}

.lcd-screen {
  background: $lcd-bg;
  border-radius: 4px;
  padding: 8px 10px;
  border: 2px solid #8a9a6a;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.2);
}

.lcd-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-family: 'Consolas', monospace;
  font-size: 11px;
  color: $lcd-text;

  &:not(:last-child) {
    margin-bottom: 4px;
    padding-bottom: 4px;
    border-bottom: 1px dashed rgba(42, 58, 26, 0.3);
  }
}

.lcd-label {
  font-weight: 500;
}

.lcd-value {
  font-weight: 700;

  &.online {
    color: #1a5a1a;
  }

  &.offline {
    color: #8b0000;
  }

  &.inactive {
    color: #666;
  }
}

/* 指示灯区域 */
.indicator-area {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 10px;
  padding: 8px 0;
  background: rgba(0, 0, 0, 0.03);
  border-radius: 4px;
}

.indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
}

.indicator-led {
  width: 8px;
  height: 8px;
  background: $led-off;
  border-radius: 50%;
  box-shadow:
    inset 0 1px 2px rgba(0, 0, 0, 0.3),
    0 1px 0 rgba(255, 255, 255, 0.1);

  .indicator.active & {
    background: $led-green;
    box-shadow:
      0 0 6px $led-green,
      0 0 12px rgba($led-green, 0.5);
  }

  .indicator.warning.active & {
    background: $led-red;
    box-shadow:
      0 0 6px $led-red,
      0 0 12px rgba($led-red, 0.5);
    animation: blink 1s ease-in-out infinite;
  }
}

.indicator-text {
  font-family: 'Arial', sans-serif;
  font-size: 8px;
  font-weight: bold;
  color: #666;
  letter-spacing: 0.5px;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* 产品信息 */
.product-info {
  text-align: center;
  margin-top: 8px;
  font-family: 'Microsoft YaHei', sans-serif;
  font-size: 10px;
  color: #888;
}
</style>
