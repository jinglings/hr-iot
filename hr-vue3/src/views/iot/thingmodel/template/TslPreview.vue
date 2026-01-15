<template>
  <div class="tsl-preview">
    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="属性" name="properties">
        <el-table :data="parsedTsl.properties" empty-text="无属性定义" border>
          <el-table-column label="标识符" prop="identifier" width="150" />
          <el-table-column label="名称" prop="name" width="150" />
          <el-table-column label="数据类型" width="120">
            <template #default="{ row }">
              <el-tag size="small">{{ row.dataType?.type || '-' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="读写" width="80">
            <template #default="{ row }">
              {{ row.accessMode === 'rw' ? '读写' : row.accessMode === 'r' ? '只读' : '只写' }}
            </template>
          </el-table-column>
          <el-table-column label="单位" prop="dataType.specs.unit" width="80" />
          <el-table-column label="必需" width="60">
            <template #default="{ row }">
              <el-tag v-if="row.required" type="danger" size="small">是</el-tag>
              <el-tag v-else type="info" size="small">否</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="事件" name="events">
        <el-table :data="parsedTsl.events" empty-text="无事件定义" border>
          <el-table-column label="标识符" prop="identifier" width="150" />
          <el-table-column label="名称" prop="name" width="150" />
          <el-table-column label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="row.type === 'alert' ? 'danger' : 'info'" size="small">
                {{ row.type === 'alert' ? '告警' : row.type === 'fault' ? '故障' : '信息' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="输出参数">
            <template #default="{ row }">
              <span v-for="(param, i) in row.outputData || []" :key="i">
                {{ param.name }}({{ param.identifier }})
                <span v-if="i < (row.outputData?.length || 0) - 1">, </span>
              </span>
              <span v-if="!row.outputData?.length">-</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="服务" name="services">
        <el-table :data="parsedTsl.services" empty-text="无服务定义" border>
          <el-table-column label="标识符" prop="identifier" width="150" />
          <el-table-column label="名称" prop="name" width="150" />
          <el-table-column label="调用方式" width="100">
            <template #default="{ row }">
              <el-tag size="small">{{ row.callType === 'sync' ? '同步' : '异步' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="输入参数">
            <template #default="{ row }">
              <span v-for="(param, i) in row.inputData || []" :key="i">
                {{ param.name }}({{ param.identifier }})
                <span v-if="i < (row.inputData?.length || 0) - 1">, </span>
              </span>
              <span v-if="!row.inputData?.length">-</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="原始JSON" name="json">
        <pre class="json-preview">{{ formattedJson }}</pre>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

defineOptions({ name: 'TslPreview' })

const props = defineProps<{
  tsl: string
}>()

const activeTab = ref('properties')

// 解析TSL
const parsedTsl = computed(() => {
  try {
    const data = JSON.parse(props.tsl)
    return {
      properties: data.properties || [],
      events: data.events || [],
      services: data.services || []
    }
  } catch {
    return { properties: [], events: [], services: [] }
  }
})

// 格式化JSON
const formattedJson = computed(() => {
  try {
    return JSON.stringify(JSON.parse(props.tsl), null, 2)
  } catch {
    return props.tsl
  }
})
</script>

<style scoped>
.tsl-preview {
  max-height: 400px;
  overflow: auto;
}

.json-preview {
  background: var(--el-fill-color-light);
  padding: 16px;
  border-radius: 4px;
  font-size: 12px;
  overflow: auto;
  max-height: 300px;
}
</style>
