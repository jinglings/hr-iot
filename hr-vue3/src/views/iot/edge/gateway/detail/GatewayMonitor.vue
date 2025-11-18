<template>
  <div>
    <el-alert
      title="实时监控功能"
      type="info"
      description="此功能需要边缘网关上报监控数据后才能展示，包括CPU使用率、内存使用率、磁盘使用率、网络流量等实时数据。"
      :closable="false"
      show-icon
    />

    <el-row :gutter="16" class="mt-4">
      <el-col :span="12">
        <el-card header="CPU使用率">
          <div ref="cpuChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="内存使用率">
          <div ref="memoryChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt-4">
      <el-col :span="12">
        <el-card header="磁盘使用率">
          <div ref="diskChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="网络流量">
          <div ref="networkChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
// 此组件暂时只是占位，实际监控数据需要对接后端网关状态接口
// import * as echarts from 'echarts'

defineProps<{
  gatewayId: number
}>()

const cpuChartRef = ref()
const memoryChartRef = ref()
const diskChartRef = ref()
const networkChartRef = ref()

// TODO: 实现ECharts图表和实时数据获取
// onMounted(() => {
//   initCharts()
// })
</script>
