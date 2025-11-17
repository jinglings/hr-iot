<template>
  <el-tree-select
    v-model="selectedValue"
    :data="treeData"
    :props="treeProps"
    :render-after-expand="false"
    :check-strictly="checkStrictly"
    :placeholder="placeholder"
    :clearable="clearable"
    :filterable="filterable"
    :disabled="disabled"
    :multiple="multiple"
    @change="handleChange"
    style="width: 100%"
  >
    <template #default="{ data }">
      <span class="custom-tree-node">
        <Icon v-if="data.icon" :icon="data.icon" class="node-icon" :style="{ color: data.color }" />
        <span>{{ data.name }}</span>
        <el-tag v-if="data.unit" type="info" size="small" style="margin-left: 8px">{{ data.unit }}</el-tag>
      </span>
    </template>
  </el-tree-select>
</template>

<script setup lang="ts">
import { getIotEnergyTypeSimpleList } from '@/api/iot/energy/energyType'
import { handleTree } from '@/utils/tree'

/** 能源类型选择器组件 */
defineOptions({ name: 'EnergyTypeSelect' })

interface Props {
  modelValue?: number | number[] // v-model绑定值
  checkStrictly?: boolean // 是否严格模式（父子不关联）
  multiple?: boolean // 是否多选
  placeholder?: string
  clearable?: boolean
  filterable?: boolean
  disabled?: boolean
  onlyLeaf?: boolean // 是否只能选择叶子节点
}

const props = withDefaults(defineProps<Props>(), {
  checkStrictly: true,
  multiple: false,
  placeholder: '请选择能源类型',
  clearable: true,
  filterable: true,
  disabled: false,
  onlyLeaf: false
})

const emit = defineEmits(['update:modelValue', 'change'])

const selectedValue = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const treeData = ref<any[]>([])
const treeProps = {
  value: 'id',
  label: 'name',
  children: 'children',
  disabled: (data: any) => {
    // 如果只能选择叶子节点，且当前节点有子节点，则禁用
    if (props.onlyLeaf && data.children && data.children.length > 0) {
      return true
    }
    return false
  }
}

/** 加载能源类型树数据 */
const loadTreeData = async () => {
  try {
    const data = await getIotEnergyTypeSimpleList()

    // 构建树形结构
    const treeNodes = handleTree(data, 'id', 'parentId')

    // 如果没有父级，添加顶级节点
    if (treeNodes.length === 0 && data.length > 0) {
      treeData.value = data
    } else {
      treeData.value = treeNodes
    }
  } catch (error) {
    console.error('加载能源类型失败:', error)
  }
}

/** 值变化处理 */
const handleChange = (value: any) => {
  const findNodeById = (nodes: any[], id: number): any => {
    for (const node of nodes) {
      if (node.id === id) {
        return node
      }
      if (node.children && node.children.length > 0) {
        const found = findNodeById(node.children, id)
        if (found) return found
      }
    }
    return null
  }

  if (props.multiple && Array.isArray(value)) {
    const selectedNodes = value.map(id => findNodeById(treeData.value, id)).filter(Boolean)
    emit('change', value, selectedNodes)
  } else {
    const selectedNode = findNodeById(treeData.value, value)
    emit('change', value, selectedNode)
  }
}

/** 初始化 */
onMounted(() => {
  loadTreeData()
})
</script>

<style scoped lang="scss">
.custom-tree-node {
  display: flex;
  align-items: center;
  gap: 5px;

  .node-icon {
    font-size: 14px;
  }
}
</style>
