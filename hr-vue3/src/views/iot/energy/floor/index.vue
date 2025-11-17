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
        <el-select v-model="queryParams.buildingId" placeholder="请选择建筑" clearable class="!w-240px">
          <el-option
            v-for="building in buildingList"
            :key="building.id"
            :label="building.name"
            :value="building.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="楼层名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入楼层名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
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
          v-hasPermi="['iot:energy-floor:create']"
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
      <el-table-column label="楼层名称" align="center" prop="name" min-width="120" />
      <el-table-column label="楼层编码" align="center" prop="code" width="120" />
      <el-table-column label="楼层号" align="center" prop="floorNumber" width="80" />
      <el-table-column label="楼层面积" align="center" prop="area" width="100">
        <template #default="scope">
          {{ scope.row.area }}㎡
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
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
            v-hasPermi="['iot:energy-floor:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['iot:energy-floor:delete']"
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
  <FloorForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import { IotEnergyFloorVO, getIotEnergyFloorPage, deleteIotEnergyFloor } from '@/api/iot/energy/floor'
import { getIotEnergyBuildingSimpleList } from '@/api/iot/energy/building'
import FloorForm from './FloorForm.vue'

/** IoT 能源楼层管理 */
defineOptions({ name: 'IotEnergyFloor' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<IotEnergyFloorVO[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const buildingList = ref([]) // 建筑列表
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  buildingId: undefined,
  name: undefined,
  status: undefined
})
const queryFormRef = ref() // 搜索的表单

/** 查询建筑列表 */
const getBuildingList = async () => {
  buildingList.value = await getIotEnergyBuildingSimpleList()
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await getIotEnergyFloorPage(queryParams)
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
    await deleteIotEnergyFloor(id)
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
