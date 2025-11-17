/**
 * 键盘快捷键Hook
 */

import { onMounted, onUnmounted } from 'vue'

export interface ShortcutConfig {
  key: string
  ctrl?: boolean
  shift?: boolean
  alt?: boolean
  meta?: boolean // Command key on Mac
  handler: (event: KeyboardEvent) => void
  description?: string
  preventDefault?: boolean
}

export interface ShortcutGroup {
  name: string
  shortcuts: ShortcutConfig[]
}

/**
 * 使用键盘快捷键
 * @param shortcuts 快捷键配置数组
 * @param enabled 是否启用
 */
export function useKeyboardShortcuts(
  shortcuts: ShortcutConfig[] | ShortcutGroup[],
  enabled: Ref<boolean> = ref(true)
) {
  // 展平快捷键配置
  const flatShortcuts = computed(() => {
    const flat: ShortcutConfig[] = []

    shortcuts.forEach((item) => {
      if ('shortcuts' in item) {
        // 是分组
        flat.push(...item.shortcuts)
      } else {
        // 是单个快捷键
        flat.push(item as ShortcutConfig)
      }
    })

    return flat
  })

  // 键盘事件处理
  const handleKeydown = (event: KeyboardEvent) => {
    if (!enabled.value) return

    // 查找匹配的快捷键
    for (const shortcut of flatShortcuts.value) {
      if (isMatchShortcut(event, shortcut)) {
        // 执行处理函数
        if (shortcut.preventDefault !== false) {
          event.preventDefault()
        }
        shortcut.handler(event)
        break
      }
    }
  }

  // 挂载时添加监听
  onMounted(() => {
    window.addEventListener('keydown', handleKeydown)
  })

  // 卸载时移除监听
  onUnmounted(() => {
    window.removeEventListener('keydown', handleKeydown)
  })

  return {
    shortcuts: flatShortcuts
  }
}

/**
 * 判断事件是否匹配快捷键
 */
function isMatchShortcut(event: KeyboardEvent, shortcut: ShortcutConfig): boolean {
  // 检查key
  const keyMatches = event.key.toLowerCase() === shortcut.key.toLowerCase()

  // 检查修饰键
  const ctrlMatches = !!shortcut.ctrl === event.ctrlKey
  const shiftMatches = !!shortcut.shift === event.shiftKey
  const altMatches = !!shortcut.alt === event.altKey
  const metaMatches = !!shortcut.meta === event.metaKey

  return keyMatches && ctrlMatches && shiftMatches && altMatches && metaMatches
}

/**
 * 格式化快捷键显示
 */
export function formatShortcut(shortcut: ShortcutConfig): string {
  const parts: string[] = []

  if (shortcut.ctrl) parts.push('Ctrl')
  if (shortcut.shift) parts.push('Shift')
  if (shortcut.alt) parts.push('Alt')
  if (shortcut.meta) parts.push('Cmd')

  // 特殊键名映射
  const keyMap: Record<string, string> = {
    delete: 'Del',
    escape: 'Esc',
    arrowup: '↑',
    arrowdown: '↓',
    arrowleft: '←',
    arrowright: '→',
    ' ': 'Space'
  }

  const key = keyMap[shortcut.key.toLowerCase()] || shortcut.key.toUpperCase()
  parts.push(key)

  return parts.join('+')
}

/**
 * 大屏编辑器快捷键配置
 */
export function useDashboardShortcuts() {
  const dashboardStore = useDashboardStore()

  const shortcutGroups: ShortcutGroup[] = [
    {
      name: '编辑操作',
      shortcuts: [
        {
          key: 'z',
          ctrl: true,
          description: '撤销',
          handler: () => dashboardStore.undo()
        },
        {
          key: 'y',
          ctrl: true,
          description: '重做',
          handler: () => dashboardStore.redo()
        },
        {
          key: 'c',
          ctrl: true,
          description: '复制组件',
          handler: () => {
            const selectedId = dashboardStore.selectedComponentId
            if (selectedId) {
              dashboardStore.copyComponent(selectedId)
            }
          }
        },
        {
          key: 'Delete',
          description: '删除组件',
          handler: () => {
            const selectedId = dashboardStore.selectedComponentId
            if (selectedId) {
              dashboardStore.deleteComponent(selectedId)
            }
          }
        },
        {
          key: 'a',
          ctrl: true,
          description: '全选（暂未实现）',
          handler: (e) => {
            e.preventDefault()
            // TODO: 实现全选功能
          }
        }
      ]
    },
    {
      name: '组件操作',
      shortcuts: [
        {
          key: 'ArrowUp',
          description: '向上移动组件',
          handler: (e) => {
            const selectedId = dashboardStore.selectedComponentId
            if (selectedId) {
              const component = dashboardStore.canvas.components.find((c) => c.id === selectedId)
              if (component) {
                const step = e.shiftKey ? 10 : 1
                dashboardStore.updateComponentPosition(selectedId, {
                  y: component.position.y - step
                })
              }
            }
          },
          preventDefault: true
        },
        {
          key: 'ArrowDown',
          description: '向下移动组件',
          handler: (e) => {
            const selectedId = dashboardStore.selectedComponentId
            if (selectedId) {
              const component = dashboardStore.canvas.components.find((c) => c.id === selectedId)
              if (component) {
                const step = e.shiftKey ? 10 : 1
                dashboardStore.updateComponentPosition(selectedId, {
                  y: component.position.y + step
                })
              }
            }
          },
          preventDefault: true
        },
        {
          key: 'ArrowLeft',
          description: '向左移动组件',
          handler: (e) => {
            const selectedId = dashboardStore.selectedComponentId
            if (selectedId) {
              const component = dashboardStore.canvas.components.find((c) => c.id === selectedId)
              if (component) {
                const step = e.shiftKey ? 10 : 1
                dashboardStore.updateComponentPosition(selectedId, {
                  x: component.position.x - step
                })
              }
            }
          },
          preventDefault: true
        },
        {
          key: 'ArrowRight',
          description: '向右移动组件',
          handler: (e) => {
            const selectedId = dashboardStore.selectedComponentId
            if (selectedId) {
              const component = dashboardStore.canvas.components.find((c) => c.id === selectedId)
              if (component) {
                const step = e.shiftKey ? 10 : 1
                dashboardStore.updateComponentPosition(selectedId, {
                  x: component.position.x + step
                })
              }
            }
          },
          preventDefault: true
        },
        {
          key: 'l',
          ctrl: true,
          description: '锁定/解锁组件',
          handler: () => {
            const selectedId = dashboardStore.selectedComponentId
            if (selectedId) {
              dashboardStore.toggleComponentLock(selectedId)
            }
          }
        }
      ]
    },
    {
      name: '视图操作',
      shortcuts: [
        {
          key: '+',
          ctrl: true,
          description: '放大画布',
          handler: () => {
            dashboardStore.setZoom(dashboardStore.zoom + 0.1)
          }
        },
        {
          key: '-',
          ctrl: true,
          description: '缩小画布',
          handler: () => {
            dashboardStore.setZoom(dashboardStore.zoom - 0.1)
          }
        },
        {
          key: '0',
          ctrl: true,
          description: '重置缩放',
          handler: () => {
            dashboardStore.setZoom(1)
          }
        },
        {
          key: 'g',
          ctrl: true,
          description: '切换网格',
          handler: () => {
            dashboardStore.toggleGrid()
          }
        }
      ]
    }
  ]

  // 使用快捷键
  const { shortcuts } = useKeyboardShortcuts(shortcutGroups)

  return {
    shortcutGroups,
    shortcuts
  }
}

// 需要导入store
import { useDashboardStore } from '@/store/modules/dashboard'
