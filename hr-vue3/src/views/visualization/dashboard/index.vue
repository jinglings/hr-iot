<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="68px"
    >
      <el-form-item label="大屏名称" prop="name">
        <el-input
          v-model="queryParams.name"
          class="!w-240px"
          clearable
          placeholder="请输入大屏名称"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />
          重置
        </el-button>
        <el-button v-hasPermi="['visualization:dashboard:create']" type="primary" @click="handleCreate">
          <Icon class="mr-5px" icon="ep:plus" />
          新建大屏
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column align="center" label="ID" prop="id" width="80" />
      <el-table-column align="center" label="缩略图" width="200">
        <template #default="{ row }">
          <el-image
            v-if="row.thumbnail"
            :preview-src-list="[row.thumbnail]"
            :src="row.thumbnail"
            class="w-160px h-90px"
            fit="cover"
            preview-teleported
          />
          <span v-else class="text-gray-400">暂无缩略图</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="大屏名称" prop="name" />
      <el-table-column align="center" label="分辨率" width="150">
        <template #default="{ row }">
          {{ row.width }} × {{ row.height }}
        </template>
      </el-table-column>
      <el-table-column align="center" label="适配模式" prop="scaleMode" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.scaleMode === 'scale'" type="success">等比缩放</el-tag>
          <el-tag v-else-if="row.scaleMode === 'width'" type="primary">宽度适配</el-tag>
          <el-tag v-else-if="row.scaleMode === 'height'" type="warning">高度适配</el-tag>
          <el-tag v-else type="info">拉伸铺满</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="创建时间"
        prop="createTime"
        width="180"
      />
      <el-table-column align="center" label="操作" width="280">
        <template #default="{ row }">
          <el-button
            v-hasPermi="['visualization:dashboard:update']"
            link
            type="primary"
            @click="handleEdit(row.id)"
          >
            编辑
          </el-button>
          <el-button link type="success" @click="handlePreview(row.id)">
            预览
          </el-button>
          <el-button link type="warning" @click="handleCopy(row)">
            复制
          </el-button>
          <el-button
            v-hasPermi="['visualization:dashboard:delete']"
            link
            type="danger"
            @click="handleDelete(row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 预览对话框 -->
  <el-dialog
    v-model="previewVisible"
    :fullscreen="true"
    :title="`预览 - ${previewDashboard?.name || ''}`"
  >
    <div v-if="previewDashboard" class="preview-container">
      <Preview :canvas="previewDashboard" />
    </div>
  </el-dialog>
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as DashboardApi from '@/api/visualization'
import type { CanvasConfig } from '@/types/dashboard'
import Preview from '../preview/index.vue'
import { ElMessageBox } from 'element-plus'

defineOptions({ name: 'VisualizationDashboard' })

const router = useRouter()
const message = useMessage()

// 查询参数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined
})

// 列表数据
const loading = ref(true)
const list = ref<any[]>([])
const total = ref(0)

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const data = await DashboardApi.getDashboardListApi(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    console.error('获取大屏列表失败:', error)
    // 临时数据，后端未实现时使用
    list.value = [
      {
        id: 1,
        name: '示例大屏',
        width: 1920,
        height: 1080,
        scaleMode: 'scale',
        thumbnail: '',
        createTime: new Date().toISOString()
      }
    ]
    total.value = 1
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

// 重置
const queryFormRef = ref()
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

// 新建
const handleCreate = () => {
  router.push({ name: 'VisualizationDashboardEditor' })
}

// 编辑
const handleEdit = (id: number) => {
  router.push({
    name: 'VisualizationDashboardEditor',
    query: { id }
  })
}

// 预览
const previewVisible = ref(false)
const previewDashboard = ref<CanvasConfig | null>(null)

const handlePreview = async (id: number) => {
  try {
    const data = await DashboardApi.getDashboardApi(id)
    previewDashboard.value = data
    previewVisible.value = true
  } catch (error) {
    message.error('获取大屏配置失败')
  }
}

// 复制
const handleCopy = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要复制大屏"${row.name}"吗?`, '提示', {
      type: 'warning'
    })

    // TODO: 调用复制接口
    const config = await DashboardApi.getDashboardApi(row.id)
    config.name = `${row.name} - 副本`
    delete config.id
    await DashboardApi.createDashboardApi(config)

    message.success('复制成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      message.error('复制失败')
    }
  }
}

// 删除
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该大屏吗? 删除后将无法恢复!', '警告', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    await DashboardApi.deleteDashboardApi(id)
    message.success('删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      message.error('删除失败')
    }
  }
}

// 初始化
onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.preview-container {
  width: 100%;
  height: calc(100vh - 120px);
  background-color: #000;
}
</style>
