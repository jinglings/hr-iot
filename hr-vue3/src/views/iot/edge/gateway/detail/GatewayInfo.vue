<template>
  <div>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="网关密钥">
        <el-input v-model="gateway?.gatewaySecret" readonly>
          <template #append>
            <el-button @click="copyToClipboard(gateway?.gatewaySecret)">
              <Icon icon="ep:document-copy" />
            </el-button>
          </template>
        </el-input>
      </el-descriptions-item>
      <el-descriptions-item label="MAC地址">
        {{ gateway?.macAddress || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="CPU核心数">
        {{ gateway?.cpuCores || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="总内存">
        {{ gateway?.memoryTotal ? `${gateway.memoryTotal} MB` : '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="总磁盘">
        {{ gateway?.diskTotal ? `${gateway.diskTotal} GB` : '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="创建时间">
        {{ formatDate(gateway?.createTime) }}
      </el-descriptions-item>
      <el-descriptions-item label="更新时间">
        {{ formatDate(gateway?.updateTime) }}
      </el-descriptions-item>
      <el-descriptions-item label="创建者">
        {{ gateway?.creator || '-' }}
      </el-descriptions-item>
    </el-descriptions>

    <el-divider content-position="left">网关配置</el-divider>
    <el-input
      v-model="gateway?.config"
      type="textarea"
      :rows="10"
      readonly
      placeholder="暂无配置"
    />
  </div>
</template>

<script setup lang="ts">
import { EdgeGatewayVO } from '@/api/iot/edge/gateway'
import { formatDate } from '@/utils/formatTime'
import { useClipboard } from '@vueuse/core'

defineProps<{
  gateway?: EdgeGatewayVO
}>()

const { copy } = useClipboard()
const message = useMessage()

const copyToClipboard = async (text: string) => {
  try {
    await copy(text)
    message.success('复制成功')
  } catch {
    message.error('复制失败')
  }
}
</script>
