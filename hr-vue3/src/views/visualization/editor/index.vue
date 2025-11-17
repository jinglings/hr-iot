<template>
  <div class="dashboard-editor">
    <!-- 工具栏 -->
    <Toolbar @preview="handlePreview" @save="handleSave" @export="handleExport" />

    <!-- 主内容区 -->
    <div class="editor-main">
      <!-- 左侧:组件库 -->
      <ComponentLibrary />

      <!-- 图层管理器 -->
      <LayerManager />

      <!-- 中间:画布 -->
      <Canvas ref="canvasRef" />

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
import { ElMessage, ElMessageBox } from 'element-plus'
import { exportToImage, copyToClipboard } from '@/utils/dashboard/export'
import { useDashboardShortcuts } from '@/composables/useKeyboardShortcuts'
import Toolbar from './components/Toolbar.vue'
import ComponentLibrary from './components/ComponentLibrary.vue'
import LayerManager from './components/LayerManager.vue'
import Canvas from './components/Canvas.vue'
import PropertyPanel from './components/PropertyPanel.vue'
import Preview from '../preview/index.vue'

defineOptions({ name: 'DashboardEditor' })

const route = useRoute()
const router = useRouter()
const dashboardStore = useDashboardStore()
const { canvas } = storeToRefs(dashboardStore)

// 画布引用
const canvasRef = ref<InstanceType<typeof Canvas>>()

// 预览对话框
const previewVisible = ref(false)

// 启用键盘快捷键
useDashboardShortcuts()

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

// 导出处理
const handleExport = async (type: string) => {
  if (!canvasRef.value) {
    ElMessage.error('画布未加载')
    return
  }

  const canvasElement = canvasRef.value.$el?.querySelector('.canvas-content')
  if (!canvasElement) {
    ElMessage.error('无法获取画布元素')
    return
  }

  const filename = `${canvas.value.name || '大屏'}_${Date.now()}`

  try {
    if (type === 'image') {
      const loading = ElMessage.loading('正在导出图片...')
      await exportToImage(canvasElement as HTMLElement, filename, {
        format: 'png',
        scale: 2,
        backgroundColor: canvas.value.backgroundColor
      })
      loading.close()
      ElMessage.success('导出图片成功')
    } else if (type === 'copy') {
      const loading = ElMessage.loading('正在复制到剪贴板...')
      await copyToClipboard(canvasElement as HTMLElement)
      loading.close()
      ElMessage.success('已复制到剪贴板')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '导出失败')
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
