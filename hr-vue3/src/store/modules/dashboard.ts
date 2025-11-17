import { defineStore } from 'pinia'
import { cloneDeep } from 'lodash-es'
import type {
  CanvasConfig,
  DashboardComponent,
  EditorState,
  HistoryRecord,
  ScaleMode
} from '@/types/dashboard'

/**
 * 大屏可视化 Store
 */
export const useDashboardStore = defineStore('dashboard', {
  state: (): EditorState => ({
    canvas: {
      name: '未命名大屏',
      width: 1920,
      height: 1080,
      backgroundColor: '#0e1117',
      backgroundImage: '',
      scale: {
        mode: 'scale' as ScaleMode,
        ratio: 1
      },
      grid: {
        enabled: true,
        size: 10,
        color: 'rgba(255, 255, 255, 0.05)'
      },
      components: []
    },
    selectedComponentId: null,
    hoveredComponentId: null,
    isPreview: false,
    zoom: 1,
    showGrid: true,
    showRuler: true,
    history: [],
    historyIndex: -1,
    // 新增：全局变量
    globalVariables: new Map<string, any>(),
    // 新增：组件事件监听器
    eventListeners: new Map<string, Array<(data: any) => void>>()
  }),

  getters: {
    /**
     * 获取选中的组件
     */
    selectedComponent(state): DashboardComponent | null {
      if (!state.selectedComponentId) return null
      return (
        state.canvas.components.find((c) => c.id === state.selectedComponentId) || null
      )
    },

    /**
     * 获取悬停的组件
     */
    hoveredComponent(state): DashboardComponent | null {
      if (!state.hoveredComponentId) return null
      return (
        state.canvas.components.find((c) => c.id === state.hoveredComponentId) || null
      )
    },

    /**
     * 是否可以撤销
     */
    canUndo(state): boolean {
      return state.historyIndex > 0
    },

    /**
     * 是否可以重做
     */
    canRedo(state): boolean {
      return state.historyIndex < state.history.length - 1
    },

    /**
     * 获取组件列表(按zIndex排序)
     */
    sortedComponents(state): DashboardComponent[] {
      return [...state.canvas.components].sort(
        (a, b) => a.position.zIndex - b.position.zIndex
      )
    }
  },

  actions: {
    /**
     * 设置画布配置
     */
    setCanvas(canvas: CanvasConfig) {
      this.canvas = canvas
      this.saveHistory('设置画布')
    },

    /**
     * 更新画布属性
     */
    updateCanvas(updates: Partial<CanvasConfig>) {
      this.canvas = { ...this.canvas, ...updates }
      this.saveHistory('更新画布属性')
    },

    /**
     * 添加组件
     */
    addComponent(component: DashboardComponent) {
      // 生成唯一ID
      component.id = `${component.type}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`

      // 设置默认zIndex
      const maxZIndex = Math.max(...this.canvas.components.map((c) => c.position.zIndex), 0)
      component.position.zIndex = maxZIndex + 1

      this.canvas.components.push(component)
      this.selectedComponentId = component.id
      this.saveHistory('添加组件')
    },

    /**
     * 删除组件
     */
    deleteComponent(componentId: string) {
      const index = this.canvas.components.findIndex((c) => c.id === componentId)
      if (index > -1) {
        this.canvas.components.splice(index, 1)
        if (this.selectedComponentId === componentId) {
          this.selectedComponentId = null
        }
        this.saveHistory('删除组件')
      }
    },

    /**
     * 更新组件
     */
    updateComponent(componentId: string, updates: Partial<DashboardComponent>) {
      const component = this.canvas.components.find((c) => c.id === componentId)
      if (component) {
        Object.assign(component, updates)
        this.saveHistory('更新组件')
      }
    },

    /**
     * 更新组件位置
     */
    updateComponentPosition(componentId: string, position: Partial<ComponentPosition>) {
      const component = this.canvas.components.find((c) => c.id === componentId)
      if (component) {
        component.position = { ...component.position, ...position }
      }
    },

    /**
     * 更新组件样式
     */
    updateComponentStyle(componentId: string, style: Partial<ComponentStyle>) {
      const component = this.canvas.components.find((c) => c.id === componentId)
      if (component) {
        component.style = { ...component.style, ...style }
        this.saveHistory('更新组件样式')
      }
    },

    /**
     * 选中组件
     */
    selectComponent(componentId: string | null) {
      this.selectedComponentId = componentId
    },

    /**
     * 悬停组件
     */
    hoverComponent(componentId: string | null) {
      this.hoveredComponentId = componentId
    },

    /**
     * 复制组件
     */
    copyComponent(componentId: string) {
      const component = this.canvas.components.find((c) => c.id === componentId)
      if (component) {
        const newComponent = cloneDeep(component)
        // 偏移位置
        newComponent.position.x += 20
        newComponent.position.y += 20
        this.addComponent(newComponent)
      }
    },

    /**
     * 移动组件层级
     */
    moveComponentLayer(componentId: string, direction: 'top' | 'bottom' | 'up' | 'down') {
      const component = this.canvas.components.find((c) => c.id === componentId)
      if (!component) return

      const zIndexes = this.canvas.components.map((c) => c.position.zIndex).sort((a, b) => a - b)

      switch (direction) {
        case 'top':
          component.position.zIndex = Math.max(...zIndexes) + 1
          break
        case 'bottom':
          component.position.zIndex = Math.min(...zIndexes) - 1
          break
        case 'up': {
          const currentIndex = zIndexes.indexOf(component.position.zIndex)
          if (currentIndex < zIndexes.length - 1) {
            component.position.zIndex = zIndexes[currentIndex + 1] + 0.5
          }
          break
        }
        case 'down': {
          const currentIndex = zIndexes.indexOf(component.position.zIndex)
          if (currentIndex > 0) {
            component.position.zIndex = zIndexes[currentIndex - 1] - 0.5
          }
          break
        }
      }

      this.saveHistory('调整图层')
    },

    /**
     * 锁定/解锁组件
     */
    toggleComponentLock(componentId: string) {
      const component = this.canvas.components.find((c) => c.id === componentId)
      if (component) {
        component.locked = !component.locked
      }
    },

    /**
     * 显示/隐藏组件
     */
    toggleComponentVisibility(componentId: string) {
      const component = this.canvas.components.find((c) => c.id === componentId)
      if (component) {
        component.hidden = !component.hidden
      }
    },

    /**
     * 设置画布缩放
     */
    setZoom(zoom: number) {
      this.zoom = Math.max(0.1, Math.min(3, zoom))
    },

    /**
     * 切换预览模式
     */
    togglePreview() {
      this.isPreview = !this.isPreview
    },

    /**
     * 切换网格显示
     */
    toggleGrid() {
      this.showGrid = !this.showGrid
    },

    /**
     * 切换标尺显示
     */
    toggleRuler() {
      this.showRuler = !this.showRuler
    },

    /**
     * 保存历史记录
     */
    saveHistory(action: string) {
      // 删除当前索引之后的历史记录
      this.history = this.history.slice(0, this.historyIndex + 1)

      // 添加新的历史记录
      const record: HistoryRecord = {
        timestamp: Date.now(),
        action,
        data: cloneDeep(this.canvas)
      }

      this.history.push(record)
      this.historyIndex = this.history.length - 1

      // 限制历史记录数量(最多保留50条)
      if (this.history.length > 50) {
        this.history.shift()
        this.historyIndex--
      }
    },

    /**
     * 撤销
     */
    undo() {
      if (this.canUndo) {
        this.historyIndex--
        this.canvas = cloneDeep(this.history[this.historyIndex].data)
      }
    },

    /**
     * 重做
     */
    redo() {
      if (this.canRedo) {
        this.historyIndex++
        this.canvas = cloneDeep(this.history[this.historyIndex].data)
      }
    },

    /**
     * 清空画布
     */
    clearCanvas() {
      this.canvas.components = []
      this.selectedComponentId = null
      this.saveHistory('清空画布')
    },

    /**
     * 重置编辑器
     */
    reset() {
      this.$reset()
    },

    /**
     * 设置全局变量
     */
    setGlobalVariable(key: string, value: any) {
      this.globalVariables.set(key, value)
      // 触发变量变更事件
      this.emitEvent(`variable:${key}`, value)
    },

    /**
     * 获取全局变量
     */
    getGlobalVariable(key: string): any {
      return this.globalVariables.get(key)
    },

    /**
     * 删除全局变量
     */
    deleteGlobalVariable(key: string) {
      this.globalVariables.delete(key)
    },

    /**
     * 监听组件事件
     */
    addEventListener(eventName: string, callback: (data: any) => void) {
      if (!this.eventListeners.has(eventName)) {
        this.eventListeners.set(eventName, [])
      }
      this.eventListeners.get(eventName)!.push(callback)

      // 返回取消监听的函数
      return () => {
        const listeners = this.eventListeners.get(eventName)
        if (listeners) {
          const index = listeners.indexOf(callback)
          if (index > -1) {
            listeners.splice(index, 1)
          }
        }
      }
    },

    /**
     * 触发组件事件
     */
    emitEvent(eventName: string, data: any) {
      const listeners = this.eventListeners.get(eventName)
      if (listeners) {
        listeners.forEach((callback) => {
          try {
            callback(data)
          } catch (error) {
            console.error(`事件监听器执行失败 (${eventName}):`, error)
          }
        })
      }
    },

    /**
     * 移除事件监听器
     */
    removeEventListener(eventName: string, callback?: (data: any) => void) {
      if (callback) {
        const listeners = this.eventListeners.get(eventName)
        if (listeners) {
          const index = listeners.indexOf(callback)
          if (index > -1) {
            listeners.splice(index, 1)
          }
        }
      } else {
        // 移除所有监听器
        this.eventListeners.delete(eventName)
      }
    },

    /**
     * 应用模板
     */
    applyTemplate(template: any) {
      // 从模板配置创建新的画布配置
      const newCanvas = cloneDeep(template.config)

      // 生成新的组件ID (避免ID冲突)
      newCanvas.components = newCanvas.components.map((component: DashboardComponent) => ({
        ...component,
        id: `${component.type.toLowerCase()}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
      }))

      // 应用画布配置
      this.canvas = {
        ...this.canvas,
        ...newCanvas,
        id: this.canvas.id, // 保留原画布ID
        createTime: this.canvas.createTime, // 保留创建时间
        updateTime: new Date().toISOString() // 更新修改时间
      }

      // 清除选中
      this.selectedComponentId = null

      // 保存历史记录
      this.saveHistory(`应用模板: ${template.name}`)
    }
  },

  // 持久化配置
  persist: {
    enabled: false // 大屏编辑器不需要持久化,每次都是加载服务器数据
  }
})

// 导出类型
import type { ComponentPosition, ComponentStyle } from '@/types/dashboard'
export type DashboardStore = ReturnType<typeof useDashboardStore>
