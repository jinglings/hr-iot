/**
 * 屏幕适配工具
 */
import { ScaleMode } from '@/types/dashboard'
import type { CSSProperties } from 'vue'

/**
 * 屏幕适配配置
 */
export interface ScreenAdapterConfig {
  width: number // 设计稿宽度
  height: number // 设计稿高度
  mode: ScaleMode // 适配模式
}

/**
 * 计算适配样式
 */
export function getAdapterStyle(config: ScreenAdapterConfig): CSSProperties {
  const { width, height, mode } = config
  const windowWidth = window.innerWidth
  const windowHeight = window.innerHeight

  const scaleX = windowWidth / width
  const scaleY = windowHeight / height

  let style: CSSProperties = {
    width: `${width}px`,
    height: `${height}px`,
    transformOrigin: 'top left'
  }

  switch (mode) {
    case ScaleMode.SCALE: {
      // 等比缩放,保持宽高比
      const scale = Math.min(scaleX, scaleY)
      style.transform = `scale(${scale})`
      // 居中显示
      const offsetX = (windowWidth - width * scale) / 2
      const offsetY = (windowHeight - height * scale) / 2
      style.left = `${offsetX}px`
      style.top = `${offsetY}px`
      style.position = 'absolute'
      break
    }

    case ScaleMode.WIDTH: {
      // 宽度适配,高度按比例缩放
      style.transform = `scale(${scaleX})`
      style.position = 'absolute'
      style.left = '0'
      style.top = '0'
      break
    }

    case ScaleMode.HEIGHT: {
      // 高度适配,宽度按比例缩放
      style.transform = `scale(${scaleY})`
      style.position = 'absolute'
      style.left = '0'
      style.top = '0'
      break
    }

    case ScaleMode.STRETCH: {
      // 拉伸铺满,不保持宽高比
      style.transform = `scale(${scaleX}, ${scaleY})`
      style.position = 'absolute'
      style.left = '0'
      style.top = '0'
      break
    }

    case ScaleMode.FULL_WIDTH: {
      // 宽度铺满
      style.width = '100%'
      style.height = `${height}px`
      delete style.transform
      break
    }

    case ScaleMode.FULL_HEIGHT: {
      // 高度铺满
      style.width = `${width}px`
      style.height = '100%'
      delete style.transform
      break
    }
  }

  return style
}

/**
 * 使用屏幕适配
 */
export function useScreenAdapter(config: Ref<ScreenAdapterConfig> | ScreenAdapterConfig) {
  const style = ref<CSSProperties>({})

  const updateStyle = () => {
    const currentConfig = unref(config)
    style.value = getAdapterStyle(currentConfig)
  }

  // 初始化
  updateStyle()

  // 监听配置变化
  if (isRef(config)) {
    watch(config, updateStyle, { deep: true })
  }

  // 监听窗口大小变化
  useEventListener('resize', updateStyle)

  return {
    style,
    updateStyle
  }
}

/**
 * 常用分辨率预设
 */
export const SCREEN_PRESETS = [
  { label: '1920×1080 (Full HD)', width: 1920, height: 1080 },
  { label: '2560×1440 (2K)', width: 2560, height: 1440 },
  { label: '3840×2160 (4K)', width: 3840, height: 2160 },
  { label: '1366×768', width: 1366, height: 768 },
  { label: '1440×900', width: 1440, height: 900 },
  { label: '1600×900', width: 1600, height: 900 },
  { label: '1680×1050', width: 1680, height: 1050 },
  { label: '2048×1080', width: 2048, height: 1080 },
  { label: '自定义', width: 0, height: 0 }
]

// 导入必要的类型
import { ref, unref, watch, isRef, type Ref } from 'vue'
import { useEventListener } from '@vueuse/core'
