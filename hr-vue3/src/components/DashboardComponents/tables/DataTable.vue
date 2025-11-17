<template>
  <div class="data-table" :style="tableWrapperStyle">
    <div v-if="title" class="table-title" :style="titleStyle">
      {{ title }}
    </div>
    <div class="table-content">
      <el-scrollbar :height="scrollHeight">
        <table class="custom-table" :style="tableStyle">
          <thead>
            <tr>
              <th
                v-for="column in columns"
                :key="column.prop"
                :style="getHeaderStyle(column)"
              >
                {{ column.label }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(row, index) in displayData"
              :key="index"
              :class="{ 'row-even': index % 2 === 1 }"
              :style="getRowStyle(index)"
            >
              <td
                v-for="column in columns"
                :key="column.prop"
                :style="getCellStyle(column)"
              >
                <template v-if="column.formatter">
                  {{ column.formatter(row[column.prop], row) }}
                </template>
                <template v-else>
                  {{ row[column.prop] }}
                </template>
              </td>
            </tr>
          </tbody>
        </table>
      </el-scrollbar>

      <!-- 分页 -->
      <div v-if="pagination && totalRows > pageSize" class="table-pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="totalRows"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          :background="true"
          small
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import type { DashboardComponent } from '@/types/dashboard'
import { dataSourceManager } from '@/utils/dashboard/dataSource'

interface Column {
  prop: string
  label: string
  width?: string
  align?: 'left' | 'center' | 'right'
  formatter?: (value: any, row: any) => string
}

interface Props {
  component: DashboardComponent
  options?: any
  data?: any
}

const props = defineProps<Props>()

// 动态数据
const dynamicData = ref<any[]>([])

// 当前页
const currentPage = ref(1)

// 每页条数
const pageSize = ref(10)

// 配置项
const title = computed(() => props.component.options?.title || '')
const columns = computed<Column[]>(
  () => props.component.options?.columns || []
)
const pagination = computed(() => props.component.options?.pagination !== false)
const scrollHeight = computed(() => {
  const height = props.component.position.h - (title.value ? 50 : 0) - (pagination.value ? 60 : 0)
  return `${height}px`
})

// 处理数据
const processedData = computed(() => {
  // 优先使用动态数据
  if (dynamicData.value && dynamicData.value.length > 0) {
    return dynamicData.value
  }

  // 使用静态数据
  const staticData = props.data?.static || props.component.data?.static
  if (staticData) {
    if (Array.isArray(staticData)) {
      return staticData
    }
    if (staticData.data && Array.isArray(staticData.data)) {
      return staticData.data
    }
  }

  // 默认数据
  return [
    { id: 1, name: '张三', age: 28, dept: '技术部', status: '在职' },
    { id: 2, name: '李四', age: 32, dept: '销售部', status: '在职' },
    { id: 3, name: '王五', age: 25, dept: '市场部', status: '在职' }
  ]
})

// 总行数
const totalRows = computed(() => processedData.value.length)

// 显示的数据
const displayData = computed(() => {
  if (!pagination.value) {
    return processedData.value
  }

  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return processedData.value.slice(start, end)
})

// 样式配置
const tableWrapperStyle = computed(() => ({
  width: '100%',
  height: '100%',
  backgroundColor: props.component.style?.backgroundColor || 'rgba(13, 25, 43, 0.8)',
  borderRadius: `${props.component.style?.borderRadius || 4}px`,
  padding: '16px',
  color: props.component.style?.color || '#e5e7eb'
}))

const titleStyle = computed(() => ({
  fontSize: '18px',
  fontWeight: 'bold',
  marginBottom: '16px',
  color: props.component.options?.titleColor || '#fff',
  textAlign: props.component.options?.titleAlign || 'left'
}))

const tableStyle = computed(() => ({
  width: '100%',
  borderCollapse: 'collapse',
  fontSize: props.component.options?.fontSize || '14px'
}))

const getHeaderStyle = (column: Column) => ({
  padding: '12px 8px',
  textAlign: column.align || 'left',
  backgroundColor: props.component.options?.headerBgColor || 'rgba(59, 130, 246, 0.2)',
  color: props.component.options?.headerColor || '#60a5fa',
  borderBottom: `2px solid ${props.component.options?.borderColor || 'rgba(59, 130, 246, 0.3)'}`,
  fontWeight: 'bold',
  width: column.width || 'auto'
})

const getRowStyle = (index: number) => ({
  transition: 'background-color 0.3s'
})

const getCellStyle = (column: Column) => ({
  padding: '10px 8px',
  textAlign: column.align || 'left',
  borderBottom: `1px solid ${props.component.options?.borderColor || 'rgba(59, 130, 246, 0.1)'}`
})

// 加载数据
const loadData = async () => {
  const dataConfig = props.component.data
  if (dataConfig.type === 'static') {
    dynamicData.value = []
    return
  }
  try {
    const data = await dataSourceManager.fetchData(dataConfig, props.component.id)

    // 处理数据格式
    if (Array.isArray(data)) {
      dynamicData.value = data
    } else if (data?.data && Array.isArray(data.data)) {
      dynamicData.value = data.data
    } else {
      dynamicData.value = []
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

// 启动数据轮询
const startDataPolling = () => {
  const dataConfig = props.component.data
  dataSourceManager.stopPolling(props.component.id)
  if (dataConfig.refresh && dataConfig.refresh > 0 && dataConfig.type !== 'static') {
    dataSourceManager.startPolling(dataConfig, props.component.id, (data) => {
      if (Array.isArray(data)) {
        dynamicData.value = data
      } else if (data?.data && Array.isArray(data.data)) {
        dynamicData.value = data.data
      }
    })
  }
}

// 监听数据配置变化
watch(
  () => props.component.data,
  () => {
    loadData()
    startDataPolling()
  },
  { deep: true }
)

// 生命周期
onMounted(() => {
  loadData()
  startDataPolling()
})

onBeforeUnmount(() => {
  dataSourceManager.stopPolling(props.component.id)
  dataSourceManager.closeWebSocket(`ws_${props.component.id}`)
})
</script>

<style lang="scss" scoped>
.data-table {
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .table-title {
    flex-shrink: 0;
  }

  .table-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .custom-table {
      th {
        position: sticky;
        top: 0;
        z-index: 10;
      }

      tbody tr {
        &:hover {
          background-color: rgba(59, 130, 246, 0.1);
        }

        &.row-even {
          background-color: rgba(0, 0, 0, 0.1);
        }
      }
    }
  }

  .table-pagination {
    margin-top: 16px;
    display: flex;
    justify-content: center;

    :deep(.el-pagination) {
      .btn-prev,
      .btn-next,
      .el-pager li {
        background-color: rgba(59, 130, 246, 0.1);
        color: #e5e7eb;

        &:hover {
          color: #60a5fa;
        }

        &.active {
          background-color: #3b82f6;
          color: #fff;
        }
      }
    }
  }
}
</style>
