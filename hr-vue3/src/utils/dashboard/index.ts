/**
 * 大屏工具函数
 */

/**
 * 生成唯一ID
 */
export function generateId(prefix = 'component'): string {
  return `${prefix}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

/**
 * 深度合并对象
 */
export function deepMerge<T extends object>(target: T, source: Partial<T>): T {
  const result = { ...target }
  Object.keys(source).forEach((key) => {
    const sourceValue = source[key]
    const targetValue = result[key]

    if (
      sourceValue &&
      typeof sourceValue === 'object' &&
      !Array.isArray(sourceValue) &&
      targetValue &&
      typeof targetValue === 'object' &&
      !Array.isArray(targetValue)
    ) {
      result[key] = deepMerge(targetValue, sourceValue)
    } else if (sourceValue !== undefined) {
      result[key] = sourceValue
    }
  })
  return result
}

/**
 * 下载JSON文件
 */
export function downloadJSON(data: any, filename: string) {
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}

/**
 * 读取JSON文件
 */
export function readJSONFile(file: File): Promise<any> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      try {
        const data = JSON.parse(e.target?.result as string)
        resolve(data)
      } catch (error) {
        reject(new Error('JSON格式错误'))
      }
    }
    reader.onerror = () => reject(new Error('文件读取失败'))
    reader.readAsText(file)
  })
}

/**
 * 捕获画布截图
 */
export async function captureCanvasScreenshot(element: HTMLElement): Promise<string> {
  // 这里可以使用 html2canvas 库
  // 为了避免引入额外依赖,先返回空字符串
  // 实际项目中建议安装 html2canvas: npm install html2canvas
  console.warn('截图功能需要安装 html2canvas 库')
  return ''
}

/**
 * 检查两个矩形是否重叠
 */
export function isRectOverlap(
  rect1: { x: number; y: number; w: number; h: number },
  rect2: { x: number; y: number; w: number; h: number }
): boolean {
  return !(
    rect1.x + rect1.w < rect2.x ||
    rect2.x + rect2.w < rect1.x ||
    rect1.y + rect1.h < rect2.y ||
    rect2.y + rect2.h < rect1.y
  )
}

/**
 * 计算网格吸附位置
 */
export function snapToGrid(value: number, gridSize: number): number {
  return Math.round(value / gridSize) * gridSize
}

/**
 * 限制值在指定范围内
 */
export function clamp(value: number, min: number, max: number): number {
  return Math.min(Math.max(value, min), max)
}

/**
 * 角度转弧度
 */
export function degToRad(deg: number): number {
  return (deg * Math.PI) / 180
}

/**
 * 弧度转角度
 */
export function radToDeg(rad: number): number {
  return (rad * 180) / Math.PI
}

/**
 * 获取旋转后的边界框
 */
export function getRotatedBounds(
  x: number,
  y: number,
  w: number,
  h: number,
  rotate: number
): { x: number; y: number; w: number; h: number } {
  const rad = degToRad(rotate)
  const cos = Math.cos(rad)
  const sin = Math.sin(rad)

  // 计算四个角的坐标
  const corners = [
    { x: 0, y: 0 },
    { x: w, y: 0 },
    { x: w, y: h },
    { x: 0, y: h }
  ]

  const rotatedCorners = corners.map((corner) => ({
    x: corner.x * cos - corner.y * sin + x,
    y: corner.x * sin + corner.y * cos + y
  }))

  // 找到边界
  const xs = rotatedCorners.map((c) => c.x)
  const ys = rotatedCorners.map((c) => c.y)

  const minX = Math.min(...xs)
  const maxX = Math.max(...xs)
  const minY = Math.min(...ys)
  const maxY = Math.max(...ys)

  return {
    x: minX,
    y: minY,
    w: maxX - minX,
    h: maxY - minY
  }
}

/**
 * 颜色转换为RGBA
 */
export function colorToRgba(color: string, alpha = 1): string {
  // 简单实现,实际项目可以使用 tinycolor 库
  if (color.startsWith('rgba')) {
    return color
  }
  if (color.startsWith('rgb')) {
    return color.replace('rgb', 'rgba').replace(')', `, ${alpha})`)
  }
  if (color.startsWith('#')) {
    const hex = color.replace('#', '')
    const r = parseInt(hex.substring(0, 2), 16)
    const g = parseInt(hex.substring(2, 4), 16)
    const b = parseInt(hex.substring(4, 6), 16)
    return `rgba(${r}, ${g}, ${b}, ${alpha})`
  }
  return color
}

/**
 * 节流函数
 */
export function throttle<T extends (...args: any[]) => any>(
  func: T,
  delay: number
): (...args: Parameters<T>) => void {
  let timer: ReturnType<typeof setTimeout> | null = null
  let lastTime = 0

  return function (this: any, ...args: Parameters<T>) {
    const now = Date.now()

    if (now - lastTime >= delay) {
      func.apply(this, args)
      lastTime = now
    } else {
      if (timer) clearTimeout(timer)
      timer = setTimeout(() => {
        func.apply(this, args)
        lastTime = Date.now()
      }, delay - (now - lastTime))
    }
  }
}

/**
 * 防抖函数
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  delay: number
): (...args: Parameters<T>) => void {
  let timer: ReturnType<typeof setTimeout> | null = null

  return function (this: any, ...args: Parameters<T>) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      func.apply(this, args)
    }, delay)
  }
}
