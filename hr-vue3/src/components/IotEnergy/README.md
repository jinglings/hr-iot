# IoT 能源管理公共组件

这是一套专为能源管理模块设计的公共组件库，提供了便捷的数据选择和展示功能。

## 组件列表

### 1. SpaceTreeSelect - 空间树选择器

用于选择空间位置（建筑/区域/楼层/房间）的树形选择器组件。

#### 使用示例

```vue
<template>
  <SpaceTreeSelect
    v-model="selectedSpace"
    level="all"
    @change="handleSpaceChange"
  />
</template>

<script setup lang="ts">
import { SpaceTreeSelect } from '@/components/IotEnergy'

const selectedSpace = ref()

const handleSpaceChange = (value) => {
  console.log('选中的空间ID:', value)
}
</script>
```

#### Props

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|-----|------|------|--------|--------|
| modelValue | v-model绑定值 | number | - | - |
| level | 显示到哪一级 | string | building / area / floor / room / all | all |
| checkStrictly | 是否严格模式（父子不关联） | boolean | - | true |
| placeholder | 占位文本 | string | - | 请选择空间位置 |
| clearable | 是否可清空 | boolean | - | true |
| filterable | 是否可搜索 | boolean | - | true |
| disabled | 是否禁用 | boolean | - | false |

#### Events

| 事件名 | 说明 | 回调参数 |
|--------|------|----------|
| change | 选中值变化时触发 | (value: number) |

---

### 2. MeterSelect - 计量点选择器

用于选择能源计量点的下拉选择器组件，支持多选、远程搜索、条件过滤等功能。

#### 使用示例

```vue
<template>
  <!-- 单选 -->
  <MeterSelect
    v-model="selectedMeter"
    :energy-type-id="energyTypeId"
    @change="handleMeterChange"
  />

  <!-- 多选 -->
  <MeterSelect
    v-model="selectedMeters"
    multiple
    remote
    @change="handleMetersChange"
  />

  <!-- 只显示虚拟表 -->
  <MeterSelect
    v-model="selectedVirtualMeter"
    :is-virtual="true"
  />
</template>

<script setup lang="ts">
import { MeterSelect } from '@/components/IotEnergy'

const selectedMeter = ref()
const selectedMeters = ref([])
const energyTypeId = ref(1)

const handleMeterChange = (value, meter) => {
  console.log('选中的计量点:', meter)
}

const handleMetersChange = (values, meters) => {
  console.log('选中的计量点列表:', meters)
}
</script>
```

#### Props

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|-----|------|------|--------|--------|
| modelValue | v-model绑定值 | number / number[] | - | - |
| energyTypeId | 按能源类型过滤 | number | - | - |
| buildingId | 按建筑过滤 | number | - | - |
| isVirtual | 是否只显示虚拟表 | boolean | - | - |
| multiple | 是否多选 | boolean | - | false |
| placeholder | 占位文本 | string | - | 请选择计量点 |
| clearable | 是否可清空 | boolean | - | true |
| filterable | 是否可搜索 | boolean | - | true |
| disabled | 是否禁用 | boolean | - | false |
| remote | 是否远程搜索 | boolean | - | false |

#### Events

| 事件名 | 说明 | 回调参数 |
|--------|------|----------|
| change | 选中值变化时触发 | (value: number \| number[], meter: Object \| Object[]) |

---

### 3. EnergyTypeSelect - 能源类型选择器

用于选择能源类型的树形选择器组件，支持多选、父子关联等功能。

#### 使用示例

```vue
<template>
  <!-- 单选 -->
  <EnergyTypeSelect
    v-model="selectedType"
    @change="handleTypeChange"
  />

  <!-- 多选 -->
  <EnergyTypeSelect
    v-model="selectedTypes"
    multiple
    @change="handleTypesChange"
  />

  <!-- 只能选择叶子节点 -->
  <EnergyTypeSelect
    v-model="selectedLeafType"
    only-leaf
  />
</template>

<script setup lang="ts">
import { EnergyTypeSelect } from '@/components/IotEnergy'

const selectedType = ref()
const selectedTypes = ref([])

const handleTypeChange = (value, typeNode) => {
  console.log('选中的能源类型:', typeNode)
}

const handleTypesChange = (values, typeNodes) => {
  console.log('选中的能源类型列表:', typeNodes)
}
</script>
```

#### Props

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
|-----|------|------|--------|--------|
| modelValue | v-model绑定值 | number / number[] | - | - |
| checkStrictly | 是否严格模式（父子不关联） | boolean | - | true |
| multiple | 是否多选 | boolean | - | false |
| placeholder | 占位文本 | string | - | 请选择能源类型 |
| clearable | 是否可清空 | boolean | - | true |
| filterable | 是否可搜索 | boolean | - | true |
| disabled | 是否禁用 | boolean | - | false |
| onlyLeaf | 是否只能选择叶子节点 | boolean | - | false |

#### Events

| 事件名 | 说明 | 回调参数 |
|--------|------|----------|
| change | 选中值变化时触发 | (value: number \| number[], node: Object \| Object[]) |

---

## 完整示例

```vue
<template>
  <el-form :model="formData" label-width="120px">
    <el-form-item label="空间位置">
      <SpaceTreeSelect
        v-model="formData.spaceId"
        level="room"
        @change="handleSpaceChange"
      />
    </el-form-item>

    <el-form-item label="能源类型">
      <EnergyTypeSelect
        v-model="formData.energyTypeId"
        @change="handleTypeChange"
      />
    </el-form-item>

    <el-form-item label="计量点">
      <MeterSelect
        v-model="formData.meterId"
        :energy-type-id="formData.energyTypeId"
        remote
        @change="handleMeterChange"
      />
    </el-form-item>

    <el-form-item label="多个计量点">
      <MeterSelect
        v-model="formData.meterIds"
        multiple
        :building-id="formData.buildingId"
      />
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { SpaceTreeSelect, MeterSelect, EnergyTypeSelect } from '@/components/IotEnergy'

const formData = reactive({
  spaceId: undefined,
  energyTypeId: undefined,
  meterId: undefined,
  meterIds: [],
  buildingId: undefined
})

const handleSpaceChange = (value) => {
  console.log('空间变化:', value)
}

const handleTypeChange = (value, node) => {
  console.log('能源类型变化:', value, node)
}

const handleMeterChange = (value, meter) => {
  console.log('计量点变化:', value, meter)
}
</script>
```

## 注意事项

1. **SpaceTreeSelect**: 树形结构会根据实际数据动态加载，支持异步加载子节点
2. **MeterSelect**: 远程搜索模式下需要设置 `remote` 属性为 `true`
3. **EnergyTypeSelect**: 树形结构基于后端返回的 `parentId` 字段构建
4. 所有组件都支持 v-model 双向绑定
5. 所有组件都支持响应式更新，过滤条件变化时会自动刷新数据
