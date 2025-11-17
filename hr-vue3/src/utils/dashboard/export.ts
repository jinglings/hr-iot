/**
 * 大屏导出工具
 */

/**
 * 导出画布为图片
 * @param element 要导出的DOM元素
 * @param filename 文件名
 * @param options 导出选项
 */
export async function exportToImage(
  element: HTMLElement,
  filename: string = 'dashboard',
  options: {
    format?: 'png' | 'jpeg'
    quality?: number
    scale?: number
    backgroundColor?: string
  } = {}
): Promise<void> {
  const { format = 'png', quality = 1, scale = 2, backgroundColor = '#0e1117' } = options

  try {
    // 使用html2canvas库（需要在项目中安装）
    // 如果没有安装，可以使用动态导入
    const html2canvas = await importHtml2Canvas()

    // 配置选项
    const canvas = await html2canvas(element, {
      scale,
      backgroundColor,
      useCORS: true,
      allowTaint: true,
      logging: false,
      windowWidth: element.scrollWidth,
      windowHeight: element.scrollHeight
    })

    // 转换为Blob并下载
    canvas.toBlob(
      (blob) => {
        if (blob) {
          downloadBlob(blob, `${filename}.${format}`)
        }
      },
      `image/${format}`,
      quality
    )
  } catch (error) {
    console.error('导出图片失败:', error)
    throw new Error('导出图片失败，请稍后重试')
  }
}

/**
 * 动态导入html2canvas
 */
async function importHtml2Canvas(): Promise<any> {
  try {
    const module = await import('html2canvas')
    return module.default || module
  } catch (error) {
    // 如果没有安装html2canvas，提示用户
    throw new Error('请先安装html2canvas: npm install html2canvas')
  }
}

/**
 * 导出为JSON配置
 * @param data 画布数据
 * @param filename 文件名
 */
export function exportToJSON(data: any, filename: string = 'dashboard'): void {
  try {
    const jsonStr = JSON.stringify(data, null, 2)
    const blob = new Blob([jsonStr], { type: 'application/json' })
    downloadBlob(blob, `${filename}.json`)
  } catch (error) {
    console.error('导出JSON失败:', error)
    throw new Error('导出JSON失败')
  }
}

/**
 * 从JSON导入配置
 * @param file 文件对象
 * @returns Promise<any>
 */
export function importFromJSON(file: File): Promise<any> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()

    reader.onload = (e) => {
      try {
        const content = e.target?.result as string
        const data = JSON.parse(content)
        resolve(data)
      } catch (error) {
        reject(new Error('JSON格式不正确'))
      }
    }

    reader.onerror = () => {
      reject(new Error('文件读取失败'))
    }

    reader.readAsText(file)
  })
}

/**
 * 下载Blob对象
 * @param blob Blob对象
 * @param filename 文件名
 */
function downloadBlob(blob: Blob, filename: string): void {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.style.display = 'none'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

/**
 * 复制画布到剪贴板
 * @param element DOM元素
 */
export async function copyToClipboard(element: HTMLElement): Promise<void> {
  try {
    const html2canvas = await importHtml2Canvas()

    const canvas = await html2canvas(element, {
      scale: 2,
      backgroundColor: '#0e1117',
      useCORS: true,
      allowTaint: true,
      logging: false
    })

    // 转换为Blob
    const blob = await new Promise<Blob>((resolve, reject) => {
      canvas.toBlob((blob) => {
        if (blob) {
          resolve(blob)
        } else {
          reject(new Error('转换失败'))
        }
      })
    })

    // 复制到剪贴板
    if (navigator.clipboard && ClipboardItem) {
      await navigator.clipboard.write([
        new ClipboardItem({
          'image/png': blob
        })
      ])
    } else {
      throw new Error('浏览器不支持剪贴板API')
    }
  } catch (error) {
    console.error('复制到剪贴板失败:', error)
    throw new Error('复制到剪贴板失败')
  }
}

/**
 * 生成缩略图
 * @param element DOM元素
 * @param width 宽度
 * @param height 高度
 * @returns Promise<string> Base64图片
 */
export async function generateThumbnail(
  element: HTMLElement,
  width: number = 300,
  height: number = 200
): Promise<string> {
  try {
    const html2canvas = await importHtml2Canvas()

    const canvas = await html2canvas(element, {
      scale: 1,
      backgroundColor: '#0e1117',
      useCORS: true,
      allowTaint: true,
      logging: false,
      width: element.scrollWidth,
      height: element.scrollHeight
    })

    // 创建缩略图canvas
    const thumbnailCanvas = document.createElement('canvas')
    thumbnailCanvas.width = width
    thumbnailCanvas.height = height

    const ctx = thumbnailCanvas.getContext('2d')
    if (!ctx) {
      throw new Error('无法获取Canvas上下文')
    }

    // 计算缩放比例
    const scale = Math.min(width / canvas.width, height / canvas.height)
    const scaledWidth = canvas.width * scale
    const scaledHeight = canvas.height * scale
    const x = (width - scaledWidth) / 2
    const y = (height - scaledHeight) / 2

    // 绘制背景
    ctx.fillStyle = '#0e1117'
    ctx.fillRect(0, 0, width, height)

    // 绘制缩放后的图片
    ctx.drawImage(canvas, x, y, scaledWidth, scaledHeight)

    // 返回base64
    return thumbnailCanvas.toDataURL('image/png')
  } catch (error) {
    console.error('生成缩略图失败:', error)
    throw new Error('生成缩略图失败')
  }
}

/**
 * 打印画布
 * @param element DOM元素
 */
export async function printCanvas(element: HTMLElement): Promise<void> {
  try {
    const html2canvas = await importHtml2Canvas()

    const canvas = await html2canvas(element, {
      scale: 2,
      backgroundColor: '#0e1117',
      useCORS: true,
      allowTaint: true,
      logging: false
    })

    // 创建打印窗口
    const printWindow = window.open('', '_blank')
    if (!printWindow) {
      throw new Error('无法打开打印窗口')
    }

    printWindow.document.write(`
      <!DOCTYPE html>
      <html>
        <head>
          <title>打印大屏</title>
          <style>
            body { margin: 0; padding: 0; }
            img { max-width: 100%; height: auto; }
            @media print {
              body { margin: 0; }
              img { page-break-inside: avoid; }
            }
          </style>
        </head>
        <body>
          <img src="${canvas.toDataURL()}" />
        </body>
      </html>
    `)

    printWindow.document.close()

    // 等待图片加载后打印
    printWindow.onload = () => {
      printWindow.print()
    }
  } catch (error) {
    console.error('打印失败:', error)
    throw new Error('打印失败')
  }
}

/**
 * 导出配置验证
 * @param data 画布数据
 * @returns 验证结果
 */
export function validateExportData(data: any): {
  valid: boolean
  errors: string[]
} {
  const errors: string[] = []

  if (!data) {
    errors.push('数据为空')
    return { valid: false, errors }
  }

  if (!data.name) {
    errors.push('缺少画布名称')
  }

  if (!data.components || !Array.isArray(data.components)) {
    errors.push('组件数据格式不正确')
  }

  if (typeof data.width !== 'number' || typeof data.height !== 'number') {
    errors.push('画布尺寸格式不正确')
  }

  return {
    valid: errors.length === 0,
    errors
  }
}
