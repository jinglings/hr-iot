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
    @change="handleChange"
    style="width: 100%"
  >
    <template #default="{ data }">
      <span class="custom-tree-node">
        <Icon :icon="getNodeIcon(data.type)" class="node-icon" />
        <span>{{ data.name }}</span>
      </span>
    </template>
  </el-tree-select>
</template>

<script setup lang="ts">
import { getIotEnergyBuildingSimpleList } from '@/api/iot/energy/building'
import { getIotEnergyAreaListByBuildingId } from '@/api/iot/energy/area'
import { getIotEnergyFloorListByBuildingId, getIotEnergyFloorListByAreaId } from '@/api/iot/energy/floor'
import { getIotEnergyRoomListByFloorId } from '@/api/iot/energy/room'

/** 空间树选择器组件 */
defineOptions({ name: 'SpaceTreeSelect' })

interface Props {
  modelValue?: number // v-model绑定值
  level?: 'building' | 'area' | 'floor' | 'room' | 'all' // 显示到哪一级
  checkStrictly?: boolean // 是否严格模式（父子不关联）
  placeholder?: string
  clearable?: boolean
  filterable?: boolean
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  level: 'all',
  checkStrictly: true,
  placeholder: '请选择空间位置',
  clearable: true,
  filterable: true,
  disabled: false
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
  children: 'children'
}

/** 获取节点图标 */
const getNodeIcon = (type: string) => {
  const iconMap = {
    building: 'ep:office-building',
    area: 'ep:grid',
    floor: 'ep:menu',
    room: 'ep:house'
  }
  return iconMap[type] || 'ep:location'
}

/** 加载树数据 */
const loadTreeData = async () => {
  try {
    // 加载建筑列表
    const buildings = await getIotEnergyBuildingSimpleList()

    const treeNodes = await Promise.all(buildings.map(async (building: any) => {
      const buildingNode: any = {
        id: building.id,
        name: building.name,
        type: 'building',
        children: []
      }

      if (props.level === 'building') {
        return buildingNode
      }

      // 加载区域列表
      try {
        const areas = await getIotEnergyAreaListByBuildingId(building.id)

        if (areas && areas.length > 0) {
          buildingNode.children = await Promise.all(areas.map(async (area: any) => {
            const areaNode: any = {
              id: `area_${area.id}`,
              name: area.name,
              type: 'area',
              children: []
            }

            if (props.level === 'area') {
              return areaNode
            }

            // 加载楼层列表
            try {
              const floors = await getIotEnergyFloorListByAreaId(area.id)

              if (floors && floors.length > 0) {
                areaNode.children = await Promise.all(floors.map(async (floor: any) => {
                  const floorNode: any = {
                    id: `floor_${floor.id}`,
                    name: floor.name,
                    type: 'floor',
                    children: []
                  }

                  if (props.level === 'floor') {
                    return floorNode
                  }

                  // 加载房间列表
                  if (props.level === 'room' || props.level === 'all') {
                    try {
                      const rooms = await getIotEnergyRoomListByFloorId(floor.id)

                      if (rooms && rooms.length > 0) {
                        floorNode.children = rooms.map((room: any) => ({
                          id: `room_${room.id}`,
                          name: room.name,
                          type: 'room'
                        }))
                      }
                    } catch (error) {
                      console.error('加载房间失败:', error)
                    }
                  }

                  return floorNode
                }))
              }
            } catch (error) {
              console.error('加载楼层失败:', error)
            }

            return areaNode
          }))
        } else {
          // 如果没有区域，直接加载楼层
          if (props.level !== 'area') {
            try {
              const floors = await getIotEnergyFloorListByBuildingId(building.id)

              if (floors && floors.length > 0) {
                buildingNode.children = floors.map((floor: any) => ({
                  id: `floor_${floor.id}`,
                  name: floor.name,
                  type: 'floor'
                }))
              }
            } catch (error) {
              console.error('加载楼层失败:', error)
            }
          }
        }
      } catch (error) {
        console.error('加载区域失败:', error)
      }

      return buildingNode
    }))

    treeData.value = treeNodes
  } catch (error) {
    console.error('加载空间树失败:', error)
  }
}

/** 值变化处理 */
const handleChange = (value: any) => {
  emit('change', value)
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
