<template>
  <div class="dashboard-editor">
    <!-- 工具栏 -->
    <Toolbar @preview="handlePreview" @save="handleSave" />

    <!-- 主内容区 -->
    <div class="editor-main">
      <!-- 左侧:组件库 -->
      <ComponentLibrary />

      <!-- 中间:画布 -->
      <Canvas />

      <!-- 右侧:属性面板 -->
      <PropertyPanel />
    </div>

    <!-- 预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="大屏预览"
      width="90%"
      :fullscreen="true"
      :close-on-click-modal="false"
    >
      <div class="preview-container">
        <Preview :canvas="canvas" />
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { storeToRefs } from 'pinia'
import { useDashboardStore } from '@/store/modules/dashboard'
import { ElMessage } from 'element-plus'
import Toolbar from './components/Toolbar.vue'
import ComponentLibrary from './components/ComponentLibrary.vue'
import Canvas from './components/Canvas.vue'
import PropertyPanel from './components/PropertyPanel.vue'
import Preview from '../preview/index.vue'

defineOptions({ name: 'DashboardEditor' })

const route = useRoute()
const router = useRouter()
const dashboardStore = useDashboardStore()
const { canvas } = storeToRefs(dashboardStore)

// 预览对话框
const previewVisible = ref(false)

// 加载大屏配置
const loadDashboard = async () => {
  const id = route.query.id
  if (id) {
    try {
      // TODO: 从后端加载大屏配置
      // const data = await getDashboardApi(id)
      // dashboardStore.setCanvas(data)
      ElMessage.success('加载成功')
    } catch (error) {
      ElMessage.error('加载失败')
    }
  } else {
    // 新建大屏,使用默认配置
    dashboardStore.reset()
  }
}

// 预览
const handlePreview = () => {
  previewVisible.value = true
}

// 保存
const handleSave = async () => {
  try {
    // TODO: 保存到后端
    // await saveDashboardApi(canvas.value)
    console.log('保存配置:', canvas.value)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 初始化
onMounted(() => {
  loadDashboard()
})

// 离开前提示
onBeforeRouteLeave((to, from, next) => {
  // TODO: 检查是否有未保存的更改
  next()
})
</script>

<style lang="scss" scoped>
.dashboard-editor {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100vh;
  overflow: hidden;

  .editor-main {
    display: flex;
    flex: 1;
    overflow: hidden;
  }

  .preview-container {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #000;
  }
}
</style>
