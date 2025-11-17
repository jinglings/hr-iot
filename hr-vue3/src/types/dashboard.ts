/**
 * 大屏可视化相关类型定义
 */

/**
 * 数据源类型
 */
export enum DataSourceType {
  STATIC = 'static', // 静态数据
  API = 'api', // API接口
  DATABASE = 'database', // 数据库
  WEBSOCKET = 'websocket', // WebSocket
  MQTT = 'mqtt' // MQTT
}

/**
 * 组件分类
 */
export enum ComponentCategory {
  CHART = 'chart', // 图表组件
  DECORATION = 'decoration', // 装饰组件
  MEDIA = 'media', // 媒体组件
  TEXT = 'text', // 文本组件
  TABLE = 'table' // 表格组件
}

/**
 * 屏幕适配模式
 */
export enum ScaleMode {
  SCALE = 'scale', // 等比缩放
  WIDTH = 'width', // 宽度适配
  HEIGHT = 'height', // 高度适配
  STRETCH = 'stretch', // 拉伸铺满
  FULL_WIDTH = 'fullWidth', // 宽度铺满
  FULL_HEIGHT = 'fullHeight' // 高度铺满
}

/**
 * 组件位置信息
 */
export interface ComponentPosition {
  x: number // X坐标
  y: number // Y坐标
  w: number // 宽度
  h: number // 高度
  rotate: number // 旋转角度
  zIndex: number // 层级
}

/**
 * 组件样式配置
 */
export interface ComponentStyle {
  backgroundColor?: string // 背景色
  backgroundImage?: string // 背景图片
  borderColor?: string // 边框颜色
  borderWidth?: number // 边框宽度
  borderStyle?: 'solid' | 'dashed' | 'dotted' // 边框样式
  borderRadius?: number // 圆角
  opacity?: number // 透明度 0-1
  boxShadow?: string // 阴影
  fontSize?: number // 字体大小
  fontWeight?: number | string // 字体粗细
  color?: string // 字体颜色
  textAlign?: 'left' | 'center' | 'right' // 文本对齐
}

/**
 * 数据配置
 */
export interface DataConfig {
  type: DataSourceType // 数据源类型
  static?: any // 静态数据
  api?: {
    url: string // 接口地址
    method: 'GET' | 'POST' | 'PUT' | 'DELETE' // 请求方法
    headers?: Record<string, string> // 请求头
    params?: Record<string, any> // 请求参数
    dataPath?: string // 数据路径,如 data.list
  }
  database?: {
    type: 'mysql' | 'postgresql' | 'mongodb' // 数据库类型
    query: string // SQL查询语句
    params?: Record<string, any> // 查询参数
  }
  websocket?: {
    url: string // WebSocket地址
    event?: string // 监听事件名
  }
  mqtt?: {
    host: string // MQTT服务器
    port: number // 端口
    topic: string // 主题
  }
  refresh?: number // 刷新间隔(秒), 0表示不刷新
  filter?: string // 数据过滤脚本(JavaScript)
  transform?: string // 数据转换脚本(JavaScript)
}

/**
 * 组件事件配置
 */
export interface ComponentEvent {
  type: 'click' | 'dblclick' | 'hover' // 事件类型
  action: 'link' | 'emit' | 'dialog' | 'custom' // 动作类型
  params?: {
    url?: string // 跳转链接
    target?: '_blank' | '_self' // 打开方式
    event?: string // 发射的事件名
    dialogId?: string // 弹窗ID
    script?: string // 自定义脚本
  }
}

/**
 * 大屏组件配置
 */
export interface DashboardComponent {
  id: string // 组件唯一ID
  type: string // 组件类型,如 BarChart
  name: string // 组件名称
  icon: string // 组件图标
  category: ComponentCategory // 组件分类
  position: ComponentPosition // 位置信息
  style: ComponentStyle // 样式配置
  data: DataConfig // 数据配置
  options?: any // 组件特定配置(如ECharts配置)
  events?: ComponentEvent[] // 事件配置
  locked?: boolean // 是否锁定
  hidden?: boolean // 是否隐藏
  [key: string]: any // 允许扩展属性
}

/**
 * 画布缩放配置
 */
export interface CanvasScale {
  mode: ScaleMode // 缩放模式
  ratio: number // 缩放比例
}

/**
 * 画布配置
 */
export interface CanvasConfig {
  id?: string // 画布ID
  name: string // 画布名称
  width: number // 画布宽度
  height: number // 画布高度
  backgroundColor: string // 背景色
  backgroundImage?: string // 背景图片
  scale: CanvasScale // 缩放配置
  grid?: {
    enabled: boolean // 是否启用网格
    size: number // 网格大小
    color: string // 网格颜色
  }
  components: DashboardComponent[] // 组件列表
  thumbnail?: string // 缩略图
  createTime?: string // 创建时间
  updateTime?: string // 更新时间
}

/**
 * 组件库项配置
 */
export interface ComponentLibraryItem {
  type: string // 组件类型
  name: string // 组件名称
  icon: string // 组件图标
  category: ComponentCategory // 组件分类
  defaultConfig: Partial<DashboardComponent> // 默认配置
  preview?: string // 预览图
}

/**
 * 历史记录项
 */
export interface HistoryRecord {
  timestamp: number // 时间戳
  action: string // 操作类型
  data: CanvasConfig // 画布数据
}

/**
 * 编辑器状态
 */
export interface EditorState {
  canvas: CanvasConfig // 画布配置
  selectedComponentId: string | null // 选中的组件ID
  hoveredComponentId: string | null // 悬停的组件ID
  isPreview: boolean // 是否预览模式
  zoom: number // 画布缩放比例 0.5-2
  showGrid: boolean // 是否显示网格
  showRuler: boolean // 是否显示标尺
  history: HistoryRecord[] // 历史记录
  historyIndex: number // 历史记录索引
  globalVariables: Map<string, any> // 全局变量
  eventListeners: Map<string, Array<(data: any) => void>> // 事件监听器
}

/**
 * 组件拖拽数据
 */
export interface DragData {
  type: 'new' | 'move' // 拖拽类型: new-新增组件, move-移动组件
  component?: ComponentLibraryItem // 新增的组件库项
  componentId?: string // 移动的组件ID
}
