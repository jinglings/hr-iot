<template>
  <div class="device-control-panel">
    <el-form :model="form" label-width="100px">
      <el-form-item label="设备">
        <el-tag>{{ device.deviceName }}</el-tag>
      </el-form-item>
      
      <el-form-item label="命令类型">
        <el-radio-group v-model="commandType">
          <el-radio value="property">设置属性</el-radio>
          <el-radio value="service">调用服务</el-radio>
        </el-radio-group>
      </el-form-item>
      
      <template v-if="commandType === 'property'">
        <el-form-item label="属性名称">
          <el-select v-model="form.propertyName" placeholder="选择属性">
            <el-option
              v-for="key in propertyKeys"
              :key="key"
              :label="key"
              :value="key"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="新值">
          <el-input v-model="form.newValue" placeholder="输入新值" />
        </el-form-item>
      </template>
      
      <template v-else>
        <el-form-item label="服务名称">
          <el-input v-model="form.serviceName" placeholder="输入服务名称" />
        </el-form-item>
        <el-form-item label="参数 (JSON)">
          <el-input 
            v-model="form.params" 
            type="textarea" 
            :rows="3"
            placeholder='例如: {"speed": 100}'
          />
        </el-form-item>
      </template>
      
      <el-form-item>
        <el-button type="primary" @click="sendCommand" :loading="sending">
          <Icon icon="ep:promotion" class="mr-1" />
          发送命令
        </el-button>
      </el-form-item>
    </el-form>
    
    <el-alert
      v-if="lastResult"
      :type="lastResult.success ? 'success' : 'error'"
      :title="lastResult.success ? '命令执行成功' : '命令执行失败'"
      :description="lastResult.message"
      show-icon
      class="mt-4"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { ScadaDevice } from '../types/scada'

defineOptions({ name: 'DeviceControlPanel' })

const props = defineProps<{
  device: ScadaDevice
}>()

const emit = defineEmits<{
  (e: 'command-sent'): void
}>()

const sending = ref(false)
const commandType = ref<'property' | 'service'>('property')
const lastResult = ref<{ success: boolean; message: string } | null>(null)

const form = reactive({
  propertyName: '',
  newValue: '',
  serviceName: '',
  params: ''
})

// 属性键列表
const propertyKeys = computed(() => {
  if (!props.device.properties) return []
  return Object.keys(props.device.properties)
})

// 发送命令
const sendCommand = async () => {
  sending.value = true
  lastResult.value = null
  
  try {
    // TODO: 调用 API
    // const result = await ScadaApi.sendControlCommand({...})
    
    await new Promise(resolve => setTimeout(resolve, 1000)) // 模拟
    
    lastResult.value = {
      success: true,
      message: `命令已发送到设备 ${props.device.deviceName}`
    }
    
    ElMessage.success('命令发送成功')
    emit('command-sent')
  } catch (error: any) {
    lastResult.value = {
      success: false,
      message: error.message || '命令发送失败'
    }
    ElMessage.error('命令发送失败')
  } finally {
    sending.value = false
  }
}
</script>

<style lang="scss" scoped>
.device-control-panel {
  padding: 16px;
}
</style>
