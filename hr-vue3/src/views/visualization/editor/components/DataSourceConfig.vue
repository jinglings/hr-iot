<template>
  <div class="data-source-config">
    <el-form :model="localConfig" label-position="top" size="default">
      <!-- 数据源类型选择 -->
      <el-form-item label="数据源类型">
        <el-select v-model="localConfig.type" placeholder="请选择数据源类型" @change="handleTypeChange">
          <el-option label="静态数据" value="static" />
          <el-option label="API接口" value="api" />
          <el-option label="WebSocket" value="websocket" />
          <el-option label="数据库查询" value="database" disabled />
          <el-option label="MQTT" value="mqtt" disabled />
        </el-select>
      </el-form-item>

      <!-- 静态数据配置 -->
      <template v-if="localConfig.type === 'static'">
        <el-form-item label="静态数据 (JSON格式)">
          <el-input
            v-model="staticDataText"
            :rows="8"
            placeholder="请输入JSON格式的数据"
            type="textarea"
            @blur="handleStaticDataChange"
          />
          <div class="form-tip">
            <el-text size="small" type="info">
              提示: 请输入有效的JSON格式数据
            </el-text>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button size="small" type="primary" @click="handleGenerateMockData">
            生成示例数据
          </el-button>
          <el-button size="small" @click="handleFormatJson">
            格式化JSON
          </el-button>
        </el-form-item>
      </template>

      <!-- API配置 -->
      <template v-if="localConfig.type === 'api'">
        <el-form-item label="请求地址" required>
          <el-input
            v-model="localConfig.api!.url"
            placeholder="https://api.example.com/data"
            @change="handleConfigChange"
          />
        </el-form-item>

        <el-form-item label="请求方法">
          <el-radio-group v-model="localConfig.api!.method" @change="handleConfigChange">
            <el-radio-button label="GET" />
            <el-radio-button label="POST" />
            <el-radio-button label="PUT" />
            <el-radio-button label="DELETE" />
          </el-radio-group>
        </el-form-item>

        <el-form-item label="请求头 (可选)">
          <div class="key-value-list">
            <div
              v-for="(value, key, index) in localConfig.api!.headers"
              :key="index"
              class="key-value-item"
            >
              <el-input
                :model-value="key"
                placeholder="Header名称"
                size="small"
                @change="(val) => handleHeaderKeyChange(key, val)"
              />
              <el-input
                v-model="localConfig.api!.headers![key]"
                placeholder="Header值"
                size="small"
                @change="handleConfigChange"
              />
              <el-button
                :icon="Delete"
                size="small"
                text
                type="danger"
                @click="handleDeleteHeader(key)"
              />
            </div>
            <el-button size="small" text type="primary" @click="handleAddHeader">
              + 添加请求头
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="请求参数 (可选)">
          <div class="key-value-list">
            <div
              v-for="(value, key, index) in localConfig.api!.params"
              :key="index"
              class="key-value-item"
            >
              <el-input
                :model-value="key"
                placeholder="参数名"
                size="small"
                @change="(val) => handleParamKeyChange(key, val)"
              />
              <el-input
                v-model="localConfig.api!.params![key]"
                placeholder="参数值"
                size="small"
                @change="handleConfigChange"
              />
              <el-button
                :icon="Delete"
                size="small"
                text
                type="danger"
                @click="handleDeleteParam(key)"
              />
            </div>
            <el-button size="small" text type="primary" @click="handleAddParam">
              + 添加参数
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="数据路径 (可选)">
          <el-input
            v-model="localConfig.api!.dataPath"
            placeholder="如: data.list"
            @change="handleConfigChange"
          />
          <div class="form-tip">
            <el-text size="small" type="info">
              用于提取响应中的数据,如响应为 {"{"}data: {"{"}list: [...]{"}"}{"}"},填写 data.list
            </el-text>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button size="small" type="primary" @click="handleTestApi">
            <Icon class="mr-5px" icon="ep:connection" />
            测试连接
          </el-button>
        </el-form-item>
      </template>

      <!-- WebSocket配置 -->
      <template v-if="localConfig.type === 'websocket'">
        <el-form-item label="WebSocket地址" required>
          <el-input
            v-model="localConfig.websocket!.url"
            placeholder="ws://localhost:8080/ws 或 wss://..."
            @change="handleConfigChange"
          />
        </el-form-item>

        <el-form-item label="监听事件 (可选)">
          <el-input
            v-model="localConfig.websocket!.event"
            placeholder="如: dataUpdate"
            @change="handleConfigChange"
          />
          <div class="form-tip">
            <el-text size="small" type="info">
              如果服务端发送的消息格式为 {"{"}event: 'xxx', data: {...}{"}"}, 可指定要监听的事件名
            </el-text>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button size="small" type="primary" @click="handleTestWebSocket">
            <Icon class="mr-5px" icon="ep:connection" />
            测试连接
          </el-button>
        </el-form-item>
      </template>

      <!-- 数据刷新配置 -->
      <el-divider />
      <el-form-item label="数据刷新">
        <el-input-number
          v-model="localConfig.refresh"
          :min="0"
          :step="5"
          placeholder="刷新间隔(秒)"
          @change="handleConfigChange"
        />
        <div class="form-tip">
          <el-text size="small" type="info">
            设置为0则不自动刷新,建议间隔不小于5秒
          </el-text>
        </div>
      </el-form-item>

      <!-- 数据转换 -->
      <el-divider />
      <el-collapse>
        <el-collapse-item name="transform" title="高级配置 - 数据转换脚本">
          <el-form-item label="数据转换脚本 (JavaScript)">
            <el-input
              v-model="localConfig.transform"
              :rows="6"
              placeholder="// 在这里编写JavaScript代码转换数据&#10;// data变量为原始数据&#10;// 示例: data = data.map(item => ({...item, value: item.value * 2}))"
              type="textarea"
              @change="handleConfigChange"
            />
            <div class="form-tip">
              <el-text size="small" type="info">
                可用的工具函数: toBarLineChart, toPieChart, sum, avg, max, min, groupBy, sort等
              </el-text>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button size="small" @click="showTransformHelp = true">
              查看可用函数
            </el-button>
          </el-form-item>
        </el-collapse-item>

        <el-collapse-item name="filter" title="高级配置 - 数据过滤脚本">
          <el-form-item label="数据过滤脚本 (JavaScript表达式)">
            <el-input
              v-model="localConfig.filter"
              :rows="4"
              placeholder="// 返回true保留数据,false过滤掉&#10;// 示例: data.value > 100"
              type="textarea"
              @change="handleConfigChange"
            />
            <div class="form-tip">
              <el-text size="small" type="info">
                对于数组数据,每个元素会执行此表达式判断是否保留
              </el-text>
            </div>
          </el-form-item>
        </el-collapse-item>
      </el-collapse>
    </el-form>

    <!-- 转换函数帮助对话框 -->
    <el-dialog v-model="showTransformHelp" title="数据转换工具函数" width="700px">
      <el-scrollbar max-height="500px">
        <div class="transform-help">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="toBarLineChart">
              转换为柱状图/折线图格式
              <pre>toBarLineChart(data, { xField, yField, seriesNames })</pre>
            </el-descriptions-item>
            <el-descriptions-item label="toPieChart">
              转换为饼图格式
              <pre>toPieChart(data, { nameField, valueField })</pre>
            </el-descriptions-item>
            <el-descriptions-item label="sum">
              求和
              <pre>sum(data, 'fieldName')</pre>
            </el-descriptions-item>
            <el-descriptions-item label="avg">
              平均值
              <pre>avg(data, 'fieldName')</pre>
            </el-descriptions-item>
            <el-descriptions-item label="max/min">
              最大值/最小值
              <pre>max(data, 'fieldName')</pre>
            </el-descriptions-item>
            <el-descriptions-item label="groupBy">
              分组聚合
              <pre>groupBy(data, 'groupField', 'aggField', 'sum')</pre>
            </el-descriptions-item>
            <el-descriptions-item label="sort">
              排序
              <pre>sort(data, 'fieldName', 'asc')</pre>
            </el-descriptions-item>
            <el-descriptions-item label="filterByValue">
              按值过滤
              <pre>filterByValue(data, 'field', value, '>')</pre>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </el-scrollbar>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import type { DataConfig, DataSourceType } from '@/types/dashboard'
import { Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { dataSourceManager, validateDataConfig } from '@/utils/dashboard/dataSource'

interface Props {
  modelValue: DataConfig
  componentType?: string // 组件类型,用于生成示例数据
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: DataConfig]
}>()

// 本地配置
const localConfig = ref<DataConfig>({
  type: 'static',
  static: null,
  api: {
    url: '',
    method: 'GET',
    headers: {},
    params: {},
    dataPath: ''
  },
  websocket: {
    url: '',
    event: ''
  },
  refresh: 0,
  transform: '',
  filter: ''
})

// 静态数据文本
const staticDataText = ref('')

// 转换函数帮助
const showTransformHelp = ref(false)

// 初始化
watch(
  () => props.modelValue,
  (newVal) => {
    if (newVal) {
      localConfig.value = JSON.parse(JSON.stringify(newVal))

      // 确保必要的属性存在
      if (!localConfig.value.api) {
        localConfig.value.api = {
          url: '',
          method: 'GET',
          headers: {},
          params: {},
          dataPath: ''
        }
      }
      if (!localConfig.value.websocket) {
        localConfig.value.websocket = {
          url: '',
          event: ''
        }
      }

      // 格式化静态数据
      if (localConfig.value.static) {
        staticDataText.value = JSON.stringify(localConfig.value.static, null, 2)
      }
    }
  },
  { immediate: true, deep: true }
)

// 处理配置变更
const handleConfigChange = () => {
  emit('update:modelValue', localConfig.value)
}

// 处理数据源类型变更
const handleTypeChange = () => {
  // 重置其他类型的配置
  localConfig.value.static = null
  staticDataText.value = ''
  handleConfigChange()
}

// 处理静态数据变更
const handleStaticDataChange = () => {
  try {
    if (staticDataText.value.trim()) {
      localConfig.value.static = JSON.parse(staticDataText.value)
      handleConfigChange()
    }
  } catch (error) {
    ElMessage.error('JSON格式不正确')
  }
}

// 格式化JSON
const handleFormatJson = () => {
  try {
    if (staticDataText.value.trim()) {
      const data = JSON.parse(staticDataText.value)
      staticDataText.value = JSON.stringify(data, null, 2)
      localConfig.value.static = data
      handleConfigChange()
      ElMessage.success('格式化成功')
    }
  } catch (error) {
    ElMessage.error('JSON格式不正确')
  }
}

// 生成示例数据
const handleGenerateMockData = () => {
  let mockData: any = {}

  // 根据组件类型生成不同的示例数据
  const type = props.componentType || 'BarChart'

  if (type.includes('Bar') || type.includes('Line')) {
    mockData = {
      xAxis: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      series: [
        {
          name: '销量',
          data: [120, 200, 150, 80, 70, 110, 130]
        }
      ]
    }
  } else if (type.includes('Pie')) {
    mockData = {
      series: [
        { name: '直接访问', value: 335 },
        { name: '邮件营销', value: 310 },
        { name: '联盟广告', value: 234 },
        { name: '视频广告', value: 135 },
        { name: '搜索引擎', value: 1548 }
      ]
    }
  } else if (type.includes('Number')) {
    mockData = {
      value: 12345
    }
  } else {
    mockData = {
      data: [1, 2, 3, 4, 5]
    }
  }

  staticDataText.value = JSON.stringify(mockData, null, 2)
  localConfig.value.static = mockData
  handleConfigChange()
  ElMessage.success('已生成示例数据')
}

// API Header操作
const handleAddHeader = () => {
  if (!localConfig.value.api!.headers) {
    localConfig.value.api!.headers = {}
  }
  localConfig.value.api!.headers[`header_${Date.now()}`] = ''
  handleConfigChange()
}

const handleDeleteHeader = (key: string) => {
  delete localConfig.value.api!.headers![key]
  handleConfigChange()
}

const handleHeaderKeyChange = (oldKey: string, newKey: string) => {
  if (oldKey !== newKey) {
    const value = localConfig.value.api!.headers![oldKey]
    delete localConfig.value.api!.headers![oldKey]
    localConfig.value.api!.headers![newKey] = value
    handleConfigChange()
  }
}

// API Param操作
const handleAddParam = () => {
  if (!localConfig.value.api!.params) {
    localConfig.value.api!.params = {}
  }
  localConfig.value.api!.params[`param_${Date.now()}`] = ''
  handleConfigChange()
}

const handleDeleteParam = (key: string) => {
  delete localConfig.value.api!.params![key]
  handleConfigChange()
}

const handleParamKeyChange = (oldKey: string, newKey: string) => {
  if (oldKey !== newKey) {
    const value = localConfig.value.api!.params![oldKey]
    delete localConfig.value.api!.params![oldKey]
    localConfig.value.api!.params![newKey] = value
    handleConfigChange()
  }
}

// 测试API连接
const handleTestApi = async () => {
  const validation = validateDataConfig(localConfig.value)
  if (!validation.valid) {
    ElMessage.error(validation.message || '配置不完整')
    return
  }

  try {
    ElMessage.loading('正在测试连接...')
    const data = await dataSourceManager.fetchData(localConfig.value, 'test')
    ElMessage.closeAll()
    ElMessage.success('连接成功!')
    console.log('API测试结果:', data)
  } catch (error) {
    ElMessage.closeAll()
    ElMessage.error('连接失败: ' + (error as Error).message)
  }
}

// 测试WebSocket连接
const handleTestWebSocket = async () => {
  const validation = validateDataConfig(localConfig.value)
  if (!validation.valid) {
    ElMessage.error(validation.message || '配置不完整')
    return
  }

  try {
    ElMessage.loading('正在测试连接...')
    const data = await dataSourceManager.fetchData(localConfig.value, 'test_ws')
    ElMessage.closeAll()
    ElMessage.success('连接成功!')
    console.log('WebSocket测试结果:', data)
  } catch (error) {
    ElMessage.closeAll()
    ElMessage.error('连接失败: ' + (error as Error).message)
    dataSourceManager.closeWebSocket('ws_test_ws')
  }
}
</script>

<style lang="scss" scoped>
.data-source-config {
  padding: 16px;

  .form-tip {
    margin-top: 4px;
  }

  .key-value-list {
    width: 100%;

    .key-value-item {
      display: flex;
      gap: 8px;
      align-items: center;
      margin-bottom: 8px;

      .el-input {
        flex: 1;
      }
    }
  }

  :deep(.el-collapse) {
    border: none;

    .el-collapse-item__header {
      border: none;
      background-color: var(--el-fill-color-lighter);
      padding: 0 12px;
      border-radius: 4px;
    }

    .el-collapse-item__wrap {
      border: none;
    }

    .el-collapse-item__content {
      padding: 16px 0;
    }
  }

  .transform-help {
    pre {
      margin: 8px 0;
      padding: 8px;
      background-color: var(--el-fill-color-light);
      border-radius: 4px;
      font-size: 12px;
      color: var(--el-color-primary);
    }
  }
}
</style>
