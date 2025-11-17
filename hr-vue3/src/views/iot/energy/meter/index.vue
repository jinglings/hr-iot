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
      <el-form-item label="计量点名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入计量点名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="计量点编码" prop="code">
        <el-input
          v-model="queryParams.code"
          placeholder="请输入计量点编码"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="能源类型" prop="energyTypeId">
        <el-select v-model="queryParams.energyTypeId" placeholder="请选择能源类型" clearable class="!w-240px">
          <el-option
            v-for="type in energyTypeList"
            :key="type.id"
            :label="type.name"
            :value="type.id"
          />
        </el-select>
      </el-form-item>
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
      <el-form-item label="计量点级别" prop="meterLevel">
        <el-select v-model="queryParams.meterLevel" placeholder="请选择计量点级别" clearable class="!w-240px">
          <el-option label="一级表" :value="1" />
          <el-option label="二级表" :value="2" />
          <el-option label="三级表" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否虚拟表" prop="isVirtual">
        <el-select v-model="queryParams.isVirtual" placeholder="请选择" clearable class="!w-240px">
          <el-option label="是" :value="true" />
          <el-option label="否" :value="false" />
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
          v-hasPermi="['iot:energy-meter:create']"
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
      <el-table-column label="计量点名称" align="center" prop="name" min-width="150" />
      <el-table-column label="计量点编码" align="center" prop="code" width="120" />
      <el-table-column label="能源类型" align="center" prop="energyTypeName" width="120" />
      <el-table-column label="设备名称" align="center" prop="deviceName" min-width="150" />
      <el-table-column label="建筑" align="center" prop="buildingName" width="120" />
      <el-table-column label="区域" align="center" prop="areaName" width="120" />
      <el-table-column label="楼层" align="center" prop="floorName" width="100" />
      <el-table-column label="房间" align="center" prop="roomName" width="120" />
      <el-table-column label="计量点级别" align="center" prop="meterLevel" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.meterLevel === 1" type="danger">一级表</el-tag>
          <el-tag v-else-if="scope.row.meterLevel === 2" type="warning">二级表</el-tag>
          <el-tag v-else-if="scope.row.meterLevel === 3" type="info">三级表</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="是否虚拟表" align="center" prop="isVirtual" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.isVirtual" type="success">是</el-tag>
          <el-tag v-else type="info">否</el-tag>
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
            v-hasPermi="['iot:energy-meter:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['iot:energy-meter:delete']"
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
  <MeterForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import { IotEnergyMeterVO, getIotEnergyMeterPage, deleteIotEnergyMeter } from '@/api/iot/energy/meter'
import { getIotEnergyTypeSimpleList } from '@/api/iot/energy/energyType'
import { getIotEnergyBuildingSimpleList } from '@/api/iot/energy/building'
import MeterForm from './MeterForm.vue'

/** IoT 能源计量点管理 */
defineOptions({ name: 'IotEnergyMeter' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<IotEnergyMeterVO[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const energyTypeList = ref([]) // 能源类型列表
const buildingList = ref([]) // 建筑列表
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  code: undefined,
  energyTypeId: undefined,
  buildingId: undefined,
  meterLevel: undefined,
  isVirtual: undefined,
  status: undefined
})
const queryFormRef = ref() // 搜索的表单

/** 查询能源类型列表 */
const getEnergyTypeList = async () => {
  energyTypeList.value = await getIotEnergyTypeSimpleList()
}

/** 查询建筑列表 */
const getBuildingList = async () => {
  buildingList.value = await getIotEnergyBuildingSimpleList()
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await getIotEnergyMeterPage(queryParams)
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
    await deleteIotEnergyMeter(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 初始化 **/
onMounted(() => {
  getEnergyTypeList()
  getBuildingList()
  getList()
})
</script>
