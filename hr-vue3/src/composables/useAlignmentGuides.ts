/**
 * 组件对齐辅助线Hook
 */

import type { DashboardComponent, ComponentPosition } from '@/types/dashboard'

export interface AlignmentLine {
  type: 'vertical' | 'horizontal'
  position: number
  color: string
}

export interface AlignmentResult {
  position: {
    x: number
    y: number
  }
  lines: AlignmentLine[]
}

const SNAP_THRESHOLD = 5 // 吸附阈值（像素）

/**
 * 使用对齐辅助线
 */
export function useAlignmentGuides() {
  const alignmentLines = ref<AlignmentLine[]>([])

  /**
   * 计算对齐
   * @param movingComponent 正在移动的组件
   * @param position 目标位置
   * @param otherComponents 其他组件列表
   * @param canvas 画布配置(可选,用于画布边缘对齐)
   * @returns 调整后的位置和辅助线
   */
  const calculateAlignment = (
    movingComponent: DashboardComponent,
    position: { x: number; y: number },
    otherComponents: DashboardComponent[],
    canvas?: any
  ): AlignmentResult => {
    const lines: AlignmentLine[] = []
    let adjustedX = position.x
    let adjustedY = position.y

    // 获取移动组件的关键点
    const moving = {
      left: position.x,
      right: position.x + movingComponent.position.w,
      centerX: position.x + movingComponent.position.w / 2,
      top: position.y,
      bottom: position.y + movingComponent.position.h,
      centerY: position.y + movingComponent.position.h / 2
    }

    // 检查与画布边缘的对齐
    if (canvas) {
      const canvasRef = {
        left: 0,
        right: canvas.width,
        centerX: canvas.width / 2,
        top: 0,
        bottom: canvas.height,
        centerY: canvas.height / 2
      }

      checkVerticalAlignment(moving, canvasRef, adjustedX, lines, (x) => {
        adjustedX = x
      })

      checkHorizontalAlignment(moving, canvasRef, adjustedY, lines, (y) => {
        adjustedY = y
      })
    }

    // 遍历其他组件
    otherComponents.forEach((comp) => {
      if (comp.id === movingComponent.id || comp.hidden) return

      // 获取参考组件的关键点
      const ref = {
        left: comp.position.x,
        right: comp.position.x + comp.position.w,
        centerX: comp.position.x + comp.position.w / 2,
        top: comp.position.y,
        bottom: comp.position.y + comp.position.h,
        centerY: comp.position.y + comp.position.h / 2
      }

      // 检查垂直对齐
      checkVerticalAlignment(moving, ref, adjustedX, lines, (x) => {
        adjustedX = x
      })

      // 检查水平对齐
      checkHorizontalAlignment(moving, ref, adjustedY, lines, (y) => {
        adjustedY = y
      })
    })

    return {
      position: {
        x: adjustedX,
        y: adjustedY
      },
      lines
    }
  }

  /**
   * 检查垂直对齐
   */
  const checkVerticalAlignment = (
    moving: any,
    ref: any,
    currentX: number,
    lines: AlignmentLine[],
    setX: (x: number) => void
  ) => {
    // 左边对齐
    if (Math.abs(moving.left - ref.left) < SNAP_THRESHOLD) {
      setX(ref.left)
      lines.push({
        type: 'vertical',
        position: ref.left,
        color: '#ff4757'
      })
    }
    // 右边对齐
    else if (Math.abs(moving.right - ref.right) < SNAP_THRESHOLD) {
      setX(ref.right - (moving.right - moving.left))
      lines.push({
        type: 'vertical',
        position: ref.right,
        color: '#ff4757'
      })
    }
    // 中心对齐
    else if (Math.abs(moving.centerX - ref.centerX) < SNAP_THRESHOLD) {
      setX(ref.centerX - (moving.centerX - moving.left))
      lines.push({
        type: 'vertical',
        position: ref.centerX,
        color: '#5352ed'
      })
    }
    // 左边对齐参考右边
    else if (Math.abs(moving.left - ref.right) < SNAP_THRESHOLD) {
      setX(ref.right)
      lines.push({
        type: 'vertical',
        position: ref.right,
        color: '#ffa502'
      })
    }
    // 右边对齐参考左边
    else if (Math.abs(moving.right - ref.left) < SNAP_THRESHOLD) {
      setX(ref.left - (moving.right - moving.left))
      lines.push({
        type: 'vertical',
        position: ref.left,
        color: '#ffa502'
      })
    }
  }

  /**
   * 检查水平对齐
   */
  const checkHorizontalAlignment = (
    moving: any,
    ref: any,
    currentY: number,
    lines: AlignmentLine[],
    setY: (y: number) => void
  ) => {
    // 顶部对齐
    if (Math.abs(moving.top - ref.top) < SNAP_THRESHOLD) {
      setY(ref.top)
      lines.push({
        type: 'horizontal',
        position: ref.top,
        color: '#ff4757'
      })
    }
    // 底部对齐
    else if (Math.abs(moving.bottom - ref.bottom) < SNAP_THRESHOLD) {
      setY(ref.bottom - (moving.bottom - moving.top))
      lines.push({
        type: 'horizontal',
        position: ref.bottom,
        color: '#ff4757'
      })
    }
    // 中心对齐
    else if (Math.abs(moving.centerY - ref.centerY) < SNAP_THRESHOLD) {
      setY(ref.centerY - (moving.centerY - moving.top))
      lines.push({
        type: 'horizontal',
        position: ref.centerY,
        color: '#5352ed'
      })
    }
    // 顶部对齐参考底部
    else if (Math.abs(moving.top - ref.bottom) < SNAP_THRESHOLD) {
      setY(ref.bottom)
      lines.push({
        type: 'horizontal',
        position: ref.bottom,
        color: '#ffa502'
      })
    }
    // 底部对齐参考顶部
    else if (Math.abs(moving.bottom - ref.top) < SNAP_THRESHOLD) {
      setY(ref.top - (moving.bottom - moving.top))
      lines.push({
        type: 'horizontal',
        position: ref.top,
        color: '#ffa502'
      })
    }
  }

  /**
   * 显示对齐线
   */
  const showAlignmentLines = (lines: AlignmentLine[]) => {
    alignmentLines.value = lines
  }

  /**
   * 清除对齐线
   */
  const clearAlignmentLines = () => {
    alignmentLines.value = []
  }

  return {
    alignmentLines,
    calculateAlignment,
    showAlignmentLines,
    clearAlignmentLines
  }
}

/**
 * 对齐辅助线组件 Props
 */
export interface AlignmentGuidesProps {
  lines: AlignmentLine[]
  canvasWidth: number
  canvasHeight: number
}
