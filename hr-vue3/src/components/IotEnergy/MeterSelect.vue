<template>
  <el-select
    v-model="selectedValue"
    :placeholder="placeholder"
    :clearable="clearable"
    :filterable="filterable"
    :multiple="multiple"
    :disabled="disabled"
    :loading="loading"
    :remote="remote"
    :remote-method="handleRemoteSearch"
    @change="handleChange"
    style="width: 100%"
  >
    <el-option
      v-for="meter in meterList"
      :key="meter.id"
      :label="formatMeterLabel(meter)"
      :value="meter.id"
    >
      <div class="meter-option">
        <div class="meter-info">
          <span class="meter-name">{{ meter.name }}</span>
          <el-tag v-if="meter.isVirtual" type="warning" size="small">虚拟表</el-tag>
        </div>
        <div class="meter-details">
          <span class="meter-code">{{ meter.code }}</span>
          <span class="meter-type">{{ meter.energyTypeName }}</span>
        </div>
      </div>
    </el-option>
  </el-select>
</template>

<script setup lang="ts">
import { getIotEnergyMeterSimpleList } from '@/api/iot/energy/meter'

/** 计量点选择器组件 */
defineOptions({ name: 'MeterSelect' })

interface Props {
  modelValue?: number | number[] // v-model绑定值
  energyTypeId?: number // 按能源类型过滤
  buildingId?: number // 按建筑过滤
  isVirtual?: boolean // 是否只显示虚拟表
  multiple?: boolean // 是否多选
  placeholder?: string
  clearable?: boolean
  filterable?: boolean
  disabled?: boolean
  remote?: boolean // 是否远程搜索
}

const props = withDefaults(defineProps<Props>(), {
  multiple: false,
  placeholder: '请选择计量点',
  clearable: true,
  filterable: true,
  disabled: false,
  remote: false
})

const emit = defineEmits(['update:modelValue', 'change'])

const selectedValue = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const meterList = ref<any[]>([])
const loading = ref(false)

/** 格式化计量点标签 */
const formatMeterLabel = (meter: any) => {
  return `${meter.name} (${meter.code})`
}

/** 加载计量点列表 */
const loadMeterList = async () => {
  loading.value = true
  try {
    const data = await getIotEnergyMeterSimpleList()

    // 根据条件过滤
    let filteredData = data

    if (props.energyTypeId) {
      filteredData = filteredData.filter((item: any) => item.energyTypeId === props.energyTypeId)
    }

    if (props.isVirtual !== undefined) {
      filteredData = filteredData.filter((item: any) => item.isVirtual === props.isVirtual)
    }

    meterList.value = filteredData
  } catch (error) {
    console.error('加载计量点列表失败:', error)
  } finally {
    loading.value = false
  }
}

/** 远程搜索 */
const handleRemoteSearch = async (query: string) => {
  if (query) {
    loading.value = true
    try {
      const data = await getIotEnergyMeterSimpleList()
      meterList.value = data.filter((item: any) =>
        item.name.toLowerCase().includes(query.toLowerCase()) ||
        item.code.toLowerCase().includes(query.toLowerCase())
      )
    } catch (error) {
      console.error('搜索计量点失败:', error)
    } finally {
      loading.value = false
    }
  } else {
    await loadMeterList()
  }
}

/** 值变化处理 */
const handleChange = (value: any) => {
  const selectedMeter = props.multiple
    ? meterList.value.filter((m: any) => value.includes(m.id))
    : meterList.value.find((m: any) => m.id === value)

  emit('change', value, selectedMeter)
}

/** 监听过滤条件变化 */
watch(
  () => [props.energyTypeId, props.buildingId, props.isVirtual],
  () => {
    loadMeterList()
  }
)

/** 初始化 */
onMounted(() => {
  if (!props.remote) {
    loadMeterList()
  }
})
</script>

<style scoped lang="scss">
.meter-option {
  .meter-info {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 4px;

    .meter-name {
      font-size: 14px;
      font-weight: 500;
    }
  }

  .meter-details {
    display: flex;
    gap: 12px;
    font-size: 12px;
    color: #999;

    .meter-code::before {
      content: '编码: ';
    }

    .meter-type::before {
      content: '类型: ';
    }
  }
}
</style>
