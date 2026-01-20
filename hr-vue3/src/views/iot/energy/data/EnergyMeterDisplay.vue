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
    <div class="meter-container" v-loading="loading">
      <div v-if="list.length === 0 && !loading" class="no-data">
        <el-empty description="暂无电表设备数据" />
      </div>
      <div class="meter-grid">
        <div
          v-for="device in list"
          :key="device.id"
          class="meter-card"
          :class="{ 'offline': device.state !== 1 }"
        >
          <!-- 电表头部信息 -->
          <div class="meter-header">
            <div class="meter-title">
              <span class="device-name">{{ device.nickname || device.deviceName }}</span>
              <el-tag
                :type="device.state === 1 ? 'success' : device.state === 2 ? 'danger' : 'info'"
                size="small"
              >
                {{ device.state === 1 ? '在线' : device.state === 2 ? '离线' : '未激活' }}
              </el-tag>
            </div>
            <div class="product-name">{{ device.productName || '未知产品' }}</div>
          </div>

          <!-- 仿真电表显示 -->
          <div class="meter-display">
            <div class="meter-frame">
              <div class="meter-glass">
                <div class="meter-screen">
                  <div class="meter-label">累计能耗 (kWh)</div>
                  <div class="meter-value">
                    <span class="digit-container">
                      <span
                        v-for="(digit, index) in formatEnergyDigits(device.energy)"
                        :key="index"
                        class="digit"
                        :class="{ decimal: digit === '.' }"
                      >{{ digit }}</span>
                    </span>
                  </div>
                </div>
                <div class="meter-indicator" :class="{ active: device.state === 1 }">
                  <div class="indicator-light"></div>
                  <span>{{ device.state === 1 ? '运行中' : '停止' }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 电表底部信息 -->
          <div class="meter-footer">
            <div class="info-row">
              <span class="info-label">设备ID:</span>
              <span class="info-value">{{ device.id }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">更新时间:</span>
              <span class="info-value">{{ formatUpdateTime(device.energyUpdateTime) }}</span>
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

/** 格式化能耗数字为8位电表显示格式 */
const formatEnergyDigits = (energy: number | null | undefined): string[] => {
  if (energy === null || energy === undefined) {
    return ['0', '0', '0', '0', '0', '.', '0', '0']
  }
  // 格式化为最多5位整数部分和2位小数部分
  const formatted = energy.toFixed(2)
  const [intPart, decPart] = formatted.split('.')
  const paddedInt = intPart.padStart(5, '0')
  return [...paddedInt, '.', ...decPart]
}

/** 格式化更新时间 */
const formatUpdateTime = (time: any): string => {
  if (!time) return '暂无数据'
  return formatDate(time, 'MM-DD HH:mm:ss')
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
.meter-container {
  min-height: 400px;
}

.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.meter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  padding: 10px;
}

.meter-card {
  background: linear-gradient(145deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 
    0 10px 40px rgba(0, 0, 0, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.05);

  &:hover {
    transform: translateY(-5px);
    box-shadow: 
      0 20px 60px rgba(0, 0, 0, 0.5),
      inset 0 1px 0 rgba(255, 255, 255, 0.15);
  }

  &.offline {
    opacity: 0.7;
    filter: grayscale(0.3);
  }
}

.meter-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.meter-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.device-name {
  font-size: 16px;
  font-weight: 600;
  color: #e0e0e0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 180px;
}

.product-name {
  font-size: 12px;
  color: #888;
}

.meter-display {
  display: flex;
  justify-content: center;
  padding: 15px 0;
}

.meter-frame {
  background: linear-gradient(180deg, #2a2a3e 0%, #1e1e2e 100%);
  border-radius: 12px;
  padding: 4px;
  box-shadow: 
    inset 0 2px 10px rgba(0, 0, 0, 0.5),
    0 2px 4px rgba(0, 0, 0, 0.3);
  border: 2px solid #3a3a4e;
}

.meter-glass {
  background: linear-gradient(180deg, #0a1628 0%, #0d2137 50%, #0a1628 100%);
  border-radius: 8px;
  padding: 16px 20px;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 5px;
    left: 10px;
    right: 10px;
    height: 20px;
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.1) 0%, transparent 100%);
    border-radius: 4px;
    pointer-events: none;
  }
}

.meter-screen {
  text-align: center;
}

.meter-label {
  font-size: 11px;
  color: #4ecdc4;
  text-transform: uppercase;
  letter-spacing: 2px;
  margin-bottom: 8px;
  text-shadow: 0 0 10px rgba(78, 205, 196, 0.5);
}

.meter-value {
  display: flex;
  justify-content: center;
  background: #030a12;
  padding: 10px 12px;
  border-radius: 6px;
  box-shadow: inset 0 2px 10px rgba(0, 0, 0, 0.8);
  border: 1px solid rgba(78, 205, 196, 0.2);
}

.digit-container {
  display: flex;
  gap: 2px;
}

.digit {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  width: 24px;
  height: 36px;
  background: linear-gradient(180deg, #0a1a2a 0%, #051520 100%);
  color: #00ff88;
  font-family: 'Courier New', monospace;
  font-size: 24px;
  font-weight: bold;
  border-radius: 3px;
  text-shadow: 
    0 0 10px rgba(0, 255, 136, 0.8),
    0 0 20px rgba(0, 255, 136, 0.4);
  box-shadow: 
    inset 0 1px 3px rgba(0, 0, 0, 0.5),
    0 1px 0 rgba(255, 255, 255, 0.05);

  &.decimal {
    width: 12px;
    background: transparent;
    box-shadow: none;
    color: #00ff88;
    text-shadow: 0 0 10px rgba(0, 255, 136, 0.8);
  }
}

.meter-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  font-size: 11px;
  color: #666;

  .indicator-light {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #333;
    box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.5);
  }

  &.active {
    color: #4ecdc4;
    
    .indicator-light {
      background: #00ff88;
      box-shadow: 
        0 0 10px rgba(0, 255, 136, 0.8),
        0 0 20px rgba(0, 255, 136, 0.4);
      animation: pulse 2s ease-in-out infinite;
    }
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    box-shadow: 
      0 0 10px rgba(0, 255, 136, 0.8),
      0 0 20px rgba(0, 255, 136, 0.4);
  }
  50% {
    opacity: 0.6;
    box-shadow: 
      0 0 5px rgba(0, 255, 136, 0.4),
      0 0 10px rgba(0, 255, 136, 0.2);
  }
}

.meter-footer {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 12px;

  &:last-child {
    margin-bottom: 0;
  }
}

.info-label {
  color: #666;
}

.info-value {
  color: #aaa;
  font-family: monospace;
}
</style>
