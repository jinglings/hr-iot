<template>
  <ContentWrap>
    <div class="scada-alarms-page">
      <div class="page-header">
        <h2>告警历史</h2>
        <el-button type="primary" @click="exportAlarms" :loading="exporting">
          <Icon icon="ep:download" class="mr-1" />
          导出
        </el-button>
      </div>
      
      <ScadaAlarmCenter />
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ScadaAlarmCenter from './components/ScadaAlarmCenter.vue'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'IotScadaAlarms' })

const exporting = ref(false)

const exportAlarms = async () => {
  exporting.value = true
  try {
    // TODO: 实现导出功能
    ElMessage.success('导出功能开发中')
  } finally {
    exporting.value = false
  }
}
</script>

<style lang="scss" scoped>
.scada-alarms-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h2 {
      margin: 0;
      font-size: 18px;
      color: #303133;
    }
  }
}
</style>
