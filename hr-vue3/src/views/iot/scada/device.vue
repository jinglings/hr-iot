<template>
  <ContentWrap>
    <div class="scada-device-page">
      <div class="page-header">
        <el-page-header @back="goBack">
          <template #content>
            <span class="text-large font-600">{{ device?.deviceName || '设备监控' }}</span>
          </template>
        </el-page-header>
      </div>
      
      <div class="device-content" v-loading="loading">
        <DeviceDetailPanel v-if="device" :device="device" />
      </div>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DeviceDetailPanel from './components/DeviceDetailPanel.vue'
import { ScadaApi, type ScadaDeviceVO } from '@/api/iot/scada'

defineOptions({ name: 'IotScadaDeviceDetail' })

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const device = ref<ScadaDeviceVO | null>(null)

const loadDevice = async () => {
  const deviceId = Number(route.params.deviceId)
  if (!deviceId) return
  
  loading.value = true
  try {
    device.value = await ScadaApi.getDevice(deviceId)
  } catch (error) {
    console.error('获取设备信息失败:', error)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push({ name: 'IotScadaIndex' })
}

onMounted(() => {
  loadDevice()
})
</script>

<style lang="scss" scoped>
.scada-device-page {
  .page-header {
    margin-bottom: 20px;
  }
}
</style>
