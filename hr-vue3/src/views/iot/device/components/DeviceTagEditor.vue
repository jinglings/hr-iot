<template>
  <div class="device-tag-editor">
    <div class="tag-list mb-2">
      <el-tag
        v-for="tag in deviceTags"
        :key="tag.id"
        :color="tag.color"
        effect="dark"
        closable
        class="mr-2 mb-2"
        style="border: none"
        @close="handleRemoveTag(tag.id!)"
      >
        {{ tag.tagKey }}{{ tag.tagValue ? '=' + tag.tagValue : '' }}
      </el-tag>
      <el-tag v-if="deviceTags.length === 0" type="info" class="mb-2">
        暂无标签
      </el-tag>
    </div>

    <el-popover
      v-model:visible="popoverVisible"
      placement="bottom-start"
      :width="360"
      trigger="click"
    >
      <template #reference>
        <el-button type="primary" plain size="small">
          <Icon icon="ep:plus" class="mr-1" /> 添加标签
        </el-button>
      </template>
      <div>
        <el-input
          v-model="searchKey"
          placeholder="搜索标签"
          clearable
          class="mb-3"
          size="small"
        >
          <template #prefix>
            <Icon icon="ep:search" />
          </template>
        </el-input>
        <div class="tag-select-list max-h-200px overflow-auto">
          <div
            v-for="tag in filteredTags"
            :key="tag.id"
            class="tag-select-item flex items-center justify-between py-2 px-2 cursor-pointer hover:bg-gray-100 rounded"
            @click="handleAddTag(tag.id!)"
          >
            <el-tag :color="tag.color" effect="dark" size="small" style="border: none">
              {{ tag.tagKey }}{{ tag.tagValue ? '=' + tag.tagValue : '' }}
            </el-tag>
            <span class="text-xs text-gray-400">{{ tag.usageCount }}次</span>
          </div>
          <div v-if="filteredTags.length === 0" class="text-center text-gray-400 py-4">
            没有找到标签
          </div>
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as TagApi from '@/api/iot/device/tag'

defineOptions({ name: 'DeviceTagEditor' })

const props = defineProps<{
  deviceId: number
}>()

const emit = defineEmits(['update'])

const popoverVisible = ref(false)
const searchKey = ref('')
const allTags = ref<TagApi.DeviceTagVO[]>([])
const deviceTags = ref<TagApi.DeviceTagVO[]>([])
const loading = ref(false)

// 过滤后的可选标签（排除已绑定的）
const filteredTags = computed(() => {
  const boundIds = new Set(deviceTags.value.map((t) => t.id))
  let list = allTags.value.filter((t) => !boundIds.has(t.id))
  if (searchKey.value) {
    const keyword = searchKey.value.toLowerCase()
    list = list.filter(
      (t) =>
        t.tagKey.toLowerCase().includes(keyword) ||
        (t.tagValue && t.tagValue.toLowerCase().includes(keyword))
    )
  }
  return list
})

// 加载所有标签
const loadAllTags = async () => {
  try {
    allTags.value = await TagApi.getTagList()
  } catch (e) {
    console.error('加载标签列表失败', e)
  }
}

// 加载设备已绑定的标签
const loadDeviceTags = async () => {
  if (!props.deviceId) return
  loading.value = true
  try {
    deviceTags.value = await TagApi.getTagsByDevice(props.deviceId)
  } catch (e) {
    console.error('加载设备标签失败', e)
  } finally {
    loading.value = false
  }
}

// 添加标签
const handleAddTag = async (tagId: number) => {
  try {
    await TagApi.bindTagsToDevice({
      deviceId: props.deviceId,
      tagIds: [tagId]
    })
    ElMessage.success('添加成功')
    popoverVisible.value = false
    await loadDeviceTags()
    emit('update')
  } catch (e) {
    console.error('添加标签失败', e)
  }
}

// 移除标签
const handleRemoveTag = async (tagId: number) => {
  try {
    await TagApi.unbindTagsFromDevice({
      deviceId: props.deviceId,
      tagIds: [tagId]
    })
    ElMessage.success('移除成功')
    await loadDeviceTags()
    emit('update')
  } catch (e) {
    console.error('移除标签失败', e)
  }
}

watch(
  () => props.deviceId,
  () => {
    loadDeviceTags()
  },
  { immediate: true }
)

onMounted(() => {
  loadAllTags()
})
</script>

<style scoped>
.device-tag-editor {
  padding: 8px 0;
}
</style>
