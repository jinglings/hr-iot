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
      <el-form-item label="规则名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入规则名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="网关" prop="gatewayId">
        <el-select
          v-model="queryParams.gatewayId"
          placeholder="请选择网关"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="gateway in gatewayList"
            :key="gateway.id"
            :label="gateway.name"
            :value="gateway.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="规则类型" prop="ruleType">
        <el-select
          v-model="queryParams.ruleType"
          placeholder="请选择规则类型"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="(value, key) in EdgeRuleTypeMap"
            :key="key"
            :label="value.label"
            :value="key"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          class="!w-240px"
        >
          <el-option label="禁用" :value="0" />
          <el-option label="启用" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="部署状态" prop="deployStatus">
        <el-select
          v-model="queryParams.deployStatus"
          placeholder="请选择部署状态"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="(value, key) in EdgeRuleDeployStatusMap"
            :key="key"
            :label="value.label"
            :value="Number(key)"
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
          v-hasPermi="['iot:edge-rule:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" />
          新增规则
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="规则名称" prop="name" min-width="150">
        <template #default="{ row }">
          <el-link type="primary" @click="openDetail(row.id)">
            {{ row.name }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column label="所属网关" prop="gatewayId" min-width="150">
        <template #default="{ row }">
          {{ getGatewayName(row.gatewayId) }}
        </template>
      </el-table-column>
      <el-table-column label="规则类型" align="center" prop="ruleType" width="120">
        <template #default="{ row }">
          <el-tag :color="EdgeRuleTypeMap[row.ruleType]?.color" style="color: white">
            {{ EdgeRuleTypeMap[row.ruleType]?.label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="部署状态" align="center" prop="deployStatus" width="120">
        <template #default="{ row }">
          <el-tag :type="EdgeRuleDeployStatusMap[row.deployStatus]?.type">
            {{ EdgeRuleDeployStatusMap[row.deployStatus]?.label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="优先级" align="center" prop="priority" width="100" />
      <el-table-column label="执行次数" align="center" prop="executeCount" width="100" />
      <el-table-column
        label="最后执行时间"
        prop="lastExecuteTime"
        :formatter="dateFormatter"
        width="180"
      />
      <el-table-column
        label="创建时间"
        prop="createTime"
        :formatter="dateFormatter"
        width="180"
      />
      <el-table-column label="操作" align="center" fixed="right" width="280">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openDetail(scope.row.id)"
            v-hasPermi="['iot:edge-rule:query']"
          >
            详情
          </el-button>
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['iot:edge-rule:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="primary"
            v-if="scope.row.deployStatus === EdgeRuleDeployStatus.NOT_DEPLOYED"
            @click="handleDeploy(scope.row.id)"
            v-hasPermi="['iot:edge-rule:deploy']"
          >
            部署
          </el-button>
          <el-button
            link
            type="warning"
            v-if="scope.row.deployStatus === EdgeRuleDeployStatus.DEPLOYED"
            @click="handleUndeploy(scope.row.id)"
            v-hasPermi="['iot:edge-rule:deploy']"
          >
            取消部署
          </el-button>
          <el-button
            link
            :type="scope.row.status === 1 ? 'warning' : 'success'"
            @click="handleToggleStatus(scope.row)"
            v-hasPermi="['iot:edge-rule:update']"
          >
            {{ scope.row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['iot:edge-rule:delete']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <RuleForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import {
  EdgeRuleApi,
  EdgeRuleVO,
  EdgeRulePageReqVO,
  EdgeRuleTypeMap,
  EdgeRuleDeployStatusMap,
  EdgeRuleDeployStatus
} from '@/api/iot/edge/rule'
import { EdgeGatewayApi, EdgeGatewayVO } from '@/api/iot/edge/gateway'
import RuleForm from './RuleForm.vue'

defineOptions({ name: 'IoTEdgeRule' })

const message = useMessage()
const { t } = useI18n()
const router = useRouter()

const loading = ref(true)
const list = ref<EdgeRuleVO[]>([])
const total = ref(0)
const queryParams = reactive<EdgeRulePageReqVO>({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  gatewayId: undefined,
  ruleType: undefined,
  status: undefined,
  deployStatus: undefined
})
const queryFormRef = ref()
const selectedIds = ref<number[]>([])

// 网关列表
const gatewayList = ref<EdgeGatewayVO[]>([])

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await EdgeRuleApi.getPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 获取网关列表 */
const getGatewayList = async () => {
  const data = await EdgeGatewayApi.getPage({ pageNo: 1, pageSize: 1000 })
  gatewayList.value = data.list
}

/** 获取网关名称 */
const getGatewayName = (gatewayId: number) => {
  const gateway = gatewayList.value.find((g) => g.id === gatewayId)
  return gateway?.name || '-'
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
  router.push({ path: '/iot/edge/rule/detail/' + id })
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await EdgeRuleApi.delete(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

/** 部署规则 */
const handleDeploy = async (id: number) => {
  try {
    await message.confirm('确认部署该规则到网关吗？')
    await EdgeRuleApi.deploy(id)
    message.success('部署成功')
    await getList()
  } catch {}
}

/** 取消部署规则 */
const handleUndeploy = async (id: number) => {
  try {
    await message.confirm('确认取消部署该规则吗？')
    await EdgeRuleApi.undeploy(id)
    message.success('取消部署成功')
    await getList()
  } catch {}
}

/** 启用/禁用规则 */
const handleToggleStatus = async (row: EdgeRuleVO) => {
  try {
    const action = row.status === 1 ? '禁用' : '启用'
    await message.confirm(`确认${action}规则"${row.name}"吗？`)
    if (row.status === 1) {
      await EdgeRuleApi.disable(row.id)
    } else {
      await EdgeRuleApi.enable(row.id)
    }
    message.success(`${action}成功`)
    await getList()
  } catch {}
}

/** 表格选择事件 */
const handleSelectionChange = (selection: EdgeRuleVO[]) => {
  selectedIds.value = selection.map((item) => item.id)
}

/** 初始化 */
onMounted(async () => {
  await getGatewayList()
  await getList()
})
</script>
