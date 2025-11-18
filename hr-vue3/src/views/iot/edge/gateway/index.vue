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
      <el-form-item label="网关名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入网关名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="序列号" prop="serialNumber">
        <el-input
          v-model="queryParams.serialNumber"
          placeholder="请输入序列号"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="item in Object.keys(EdgeGatewayStatusMap)"
            :key="item"
            :label="EdgeGatewayStatusMap[item].label"
            :value="Number(item)"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" />
          重置
        </el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['iot:edge-gateway:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" />
          新增网关
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="网关名称" prop="name" min-width="150">
        <template #default="{ row }">
          <el-link type="primary" @click="openDetail(row.id)">
            {{ row.name }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column label="序列号" prop="serialNumber" min-width="180" />
      <el-table-column label="设备型号" prop="deviceType" min-width="120" />
      <el-table-column label="IP地址" prop="ipAddress" min-width="140" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="{ row }">
          <el-tag :type="EdgeGatewayStatusMap[row.status]?.type">
            {{ EdgeGatewayStatusMap[row.status]?.label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="安装位置" prop="location" min-width="150" show-overflow-tooltip />
      <el-table-column
        label="最后在线时间"
        prop="lastOnlineTime"
        :formatter="dateFormatter"
        width="180"
      />
      <el-table-column
        label="创建时间"
        prop="createTime"
        :formatter="dateFormatter"
        width="180"
      />
      <el-table-column label="操作" align="center" fixed="right" width="240">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            @click="openDetail(row.id)"
            v-hasPermi="['iot:edge-gateway:query']"
          >
            详情
          </el-button>
          <el-button
            link
            type="primary"
            @click="openForm('update', row.id)"
            v-hasPermi="['iot:edge-gateway:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="primary"
            v-if="row.status === EdgeGatewayStatus.INACTIVE || row.status === EdgeGatewayStatus.OFFLINE"
            @click="handleToggleStatus(row, true)"
            v-hasPermi="['iot:edge-gateway:update']"
          >
            启用
          </el-button>
          <el-button
            link
            type="warning"
            v-else
            @click="handleToggleStatus(row, false)"
            v-hasPermi="['iot:edge-gateway:update']"
          >
            禁用
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(row.id)"
            v-hasPermi="['iot:edge-gateway:delete']"
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
  <GatewayForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts" name="EdgeGateway">
import { dateFormatter } from '@/utils/formatTime'
import { EdgeGatewayApi, EdgeGatewayVO, EdgeGatewayStatus, EdgeGatewayStatusMap } from '@/api/iot/edge/gateway'
import GatewayForm from './GatewayForm.vue'

const message = useMessage() // 消息弹窗
const router = useRouter()

const loading = ref(true) // 列表的加载中
const list = ref<EdgeGatewayVO[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  serialNumber: undefined,
  status: undefined
})
const queryFormRef = ref() // 搜索的表单
const selectedIds = ref<number[]>([]) // 表格的选中 ID 数组

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await EdgeGatewayApi.getPage(queryParams)
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

/** 打开详情 */
const openDetail = (id: number) => {
  router.push({ path: `/iot/edge/gateway/detail/${id}` })
}

/** 启用/禁用操作 */
const handleToggleStatus = async (row: EdgeGatewayVO, enable: boolean) => {
  try {
    const action = enable ? '启用' : '禁用'
    await message.confirm(`确定要${action}网关"${row.name}"吗？`)
    if (enable) {
      await EdgeGatewayApi.enable(row.id)
    } else {
      await EdgeGatewayApi.disable(row.id)
    }
    message.success(`${action}成功`)
    await getList()
  } catch {}
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await EdgeGatewayApi.delete(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

/** 表格选中事件 */
const handleSelectionChange = (selection: EdgeGatewayVO[]) => {
  selectedIds.value = selection.map((item) => item.id)
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>
