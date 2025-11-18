<template>
  <div class="rule-config">
    <el-card header="触发器配置" class="mb-4">
      <el-input
        v-model="triggerJson"
        type="textarea"
        :rows="8"
        readonly
        placeholder="触发器配置（JSON格式）"
      />
      <div class="mt-2 text-sm text-gray-500">
        <Icon icon="ep:info-filled" class="mr-1" />
        定义规则的触发条件
      </div>
    </el-card>

    <el-card header="条件配置" class="mb-4">
      <el-input
        v-model="conditionJson"
        type="textarea"
        :rows="6"
        readonly
        placeholder="条件配置（JSON格式，可选）"
      />
      <div class="mt-2 text-sm text-gray-500">
        <Icon icon="ep:info-filled" class="mr-1" />
        定义额外的执行条件，如时间范围、星期等
      </div>
    </el-card>

    <el-card header="动作配置" class="mb-4">
      <el-input
        v-model="actionJson"
        type="textarea"
        :rows="8"
        readonly
        placeholder="动作配置（JSON格式）"
      />
      <div class="mt-2 text-sm text-gray-500">
        <Icon icon="ep:info-filled" class="mr-1" />
        定义规则触发后执行的动作
      </div>
    </el-card>

    <div class="text-center">
      <el-button type="primary" @click="handleEdit" v-hasPermi="['iot:edge-rule:update']">
        <Icon icon="ep:edit" class="mr-5px" />
        编辑配置
      </el-button>
      <el-button
        v-if="props.rule.deployStatus === EdgeRuleDeployStatus.NOT_DEPLOYED"
        type="success"
        @click="handleDeploy"
        v-hasPermi="['iot:edge-rule:deploy']"
      >
        <Icon icon="ep:upload" class="mr-5px" />
        部署规则
      </el-button>
      <el-button
        v-if="props.rule.deployStatus === EdgeRuleDeployStatus.DEPLOYED"
        type="warning"
        @click="handleUndeploy"
        v-hasPermi="['iot:edge-rule:deploy']"
      >
        <Icon icon="ep:download" class="mr-5px" />
        取消部署
      </el-button>
      <el-button
        :type="props.rule.status === 1 ? 'warning' : 'success'"
        @click="handleToggleStatus"
        v-hasPermi="['iot:edge-rule:update']"
      >
        <Icon icon="ep:switch-button" class="mr-5px" />
        {{ props.rule.status === 1 ? '禁用规则' : '启用规则' }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { EdgeRuleApi, EdgeRuleVO, EdgeRuleDeployStatus } from '@/api/iot/edge/rule'

const props = defineProps<{
  rule: EdgeRuleVO
}>()

const emit = defineEmits(['refresh'])
const message = useMessage()
const router = useRouter()

// JSON格式化显示
const triggerJson = computed(() => {
  try {
    return JSON.stringify(JSON.parse(props.rule.triggerConfig || '{}'), null, 2)
  } catch {
    return props.rule.triggerConfig
  }
})

const conditionJson = computed(() => {
  try {
    return props.rule.conditionConfig
      ? JSON.stringify(JSON.parse(props.rule.conditionConfig), null, 2)
      : ''
  } catch {
    return props.rule.conditionConfig
  }
})

const actionJson = computed(() => {
  try {
    return JSON.stringify(JSON.parse(props.rule.actionConfig || '{}'), null, 2)
  } catch {
    return props.rule.actionConfig
  }
})

/** 编辑配置 */
const handleEdit = () => {
  router.push({ path: '/iot/edge/rule', query: { edit: props.rule.id } })
}

/** 部署规则 */
const handleDeploy = async () => {
  try {
    await message.confirm('确认部署该规则到网关吗？')
    await EdgeRuleApi.deploy(props.rule.id)
    message.success('部署成功')
    emit('refresh')
  } catch {}
}

/** 取消部署 */
const handleUndeploy = async () => {
  try {
    await message.confirm('确认取消部署该规则吗？')
    await EdgeRuleApi.undeploy(props.rule.id)
    message.success('取消部署成功')
    emit('refresh')
  } catch {}
}

/** 启用/禁用规则 */
const handleToggleStatus = async () => {
  try {
    const action = props.rule.status === 1 ? '禁用' : '启用'
    await message.confirm(`确认${action}该规则吗？`)
    if (props.rule.status === 1) {
      await EdgeRuleApi.disable(props.rule.id)
    } else {
      await EdgeRuleApi.enable(props.rule.id)
    }
    message.success(`${action}成功`)
    emit('refresh')
  } catch {}
}
</script>
