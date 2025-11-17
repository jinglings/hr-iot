<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="80px"
    >
      <el-form-item label="所属建筑" prop="buildingId">
        <el-select
          v-model="queryParams.buildingId"
          placeholder="请选择建筑"
          clearable
          class="!w-240px"
          @change="handleBuildingChange"
        >
          <el-option
            v-for="building in buildingList"
            :key="building.id"
            :label="building.name"
            :value="building.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="所属楼层" prop="floorId">
        <el-select v-model="queryParams.floorId" placeholder="请选择楼层" clearable class="!w-240px">
          <el-option
            v-for="floor in floorList"
            :key="floor.id"
            :label="floor.name"
            :value="floor.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="房间名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入房间名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="房间编码" prop="code">
        <el-input
          v-model="queryParams.code"
          placeholder="请输入房间编码"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="房间类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择房间类型" clearable class="!w-240px">
          <el-option label="办公室" value="office" />
          <el-option label="会议室" value="meeting" />
          <el-option label="仓库" value="warehouse" />
          <el-option label="实验室" value="lab" />
          <el-option label="其他" value="other" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-240px">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['iot:energy-room:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="所属建筑" align="center" prop="buildingName" min-width="120" />
      <el-table-column label="所属区域" align="center" prop="areaName" min-width="120" />
      <el-table-column label="所属楼层" align="center" prop="floorName" min-width="120" />
      <el-table-column label="房间名称" align="center" prop="name" min-width="120" />
      <el-table-column label="房间编码" align="center" prop="code" width="120" />
      <el-table-column label="房间类型" align="center" prop="type" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.type === 'office'" type="primary">办公室</el-tag>
          <el-tag v-else-if="scope.row.type === 'meeting'" type="success">会议室</el-tag>
          <el-tag v-else-if="scope.row.type === 'warehouse'" type="warning">仓库</el-tag>
          <el-tag v-else-if="scope.row.type === 'lab'" type="info">实验室</el-tag>
          <el-tag v-else>其他</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="房间面积" align="center" prop="area" width="100">
        <template #default="scope">
          {{ scope.row.area }}㎡
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sort" width="80" />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" min-width="140px" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['iot:energy-room:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['iot:energy-room:delete']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <RoomForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import { IotEnergyRoomVO, getIotEnergyRoomPage, deleteIotEnergyRoom } from '@/api/iot/energy/room'
import { getIotEnergyBuildingSimpleList } from '@/api/iot/energy/building'
import { getIotEnergyFloorListByBuildingId } from '@/api/iot/energy/floor'
import RoomForm from './RoomForm.vue'

/** IoT 能源房间管理 */
defineOptions({ name: 'IotEnergyRoom' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<IotEnergyRoomVO[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const buildingList = ref([]) // 建筑列表
const floorList = ref([]) // 楼层列表
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  buildingId: undefined,
  floorId: undefined,
  name: undefined,
  code: undefined,
  type: undefined,
  status: undefined
})
const queryFormRef = ref() // 搜索的表单

/** 查询建筑列表 */
const getBuildingList = async () => {
  buildingList.value = await getIotEnergyBuildingSimpleList()
}

/** 建筑变化时，加载楼层列表 */
const handleBuildingChange = async (buildingId: number) => {
  queryParams.floorId = undefined
  if (buildingId) {
    floorList.value = await getIotEnergyFloorListByBuildingId(buildingId)
  } else {
    floorList.value = []
  }
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await getIotEnergyRoomPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  floorList.value = []
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await deleteIotEnergyRoom(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 初始化 **/
onMounted(() => {
  getBuildingList()
  getList()
})
</script>
