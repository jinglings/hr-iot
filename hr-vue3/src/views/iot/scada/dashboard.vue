<template>
  <div class="scada-dashboard-page">
    <FuxaEmbed
      :dashboard-id="dashboardId"
      :dashboard-name="dashboardName"
      :show-toolbar="true"
      height="calc(100vh - 84px)"
      @load="onDashboardLoad"
      @error="onDashboardError"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import FuxaEmbed from './components/FuxaEmbed.vue'
import { ScadaApi } from '@/api/iot/scada'

defineOptions({ name: 'IotScadaDashboard' })

const route = useRoute()

const dashboardId = ref('')
const dashboardName = ref('SCADA 仪表板')

// 加载仪表板信息
const loadDashboard = async () => {
  const id = route.params.dashboardId as string
  if (id) {
    dashboardId.value = id
    // TODO: 加载仪表板详情获取名称
  } else {
    // 获取默认仪表板
    try {
      const defaultDashboard = await ScadaApi.getDefaultDashboard()
      if (defaultDashboard) {
        dashboardId.value = defaultDashboard.dashboardId
        dashboardName.value = defaultDashboard.name
      }
    } catch (error) {
      console.error('获取默认仪表板失败:', error)
    }
  }
}

const onDashboardLoad = () => {
  console.log('仪表板加载完成')
}

const onDashboardError = (error: Error) => {
  console.error('仪表板加载失败:', error)
}

onMounted(() => {
  loadDashboard()
})
</script>

<style lang="scss" scoped>
.scada-dashboard-page {
  height: 100%;
  background: #1a1a2e;
}
</style>
