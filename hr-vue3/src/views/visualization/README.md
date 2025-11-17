# 大屏可视化系统使用说明

## 📖 功能概述

大屏可视化系统是一个完整的低代码可视化大屏搭建平台，支持通过拖拽方式快速创建漂亮的数据大屏。

## 🎯 主要功能

### 1. 大屏管理
- 大屏列表查看
- 新建/编辑/删除大屏
- 大屏预览
- 大屏复制

### 2. 可视化编辑器
- **拖拽式编辑**: 从组件库拖拽组件到画布
- **组件操作**: 移动、缩放、旋转、删除
- **属性配置**: 位置、样式、数据配置
- **撤销/重做**: 支持50步历史记录
- **导入/导出**: JSON配置导入导出
- **屏幕适配**: 多种适配模式(等比缩放、宽度适配等)

### 3. 组件库
- **图表组件**: 柱状图、折线图、饼图
- **文本组件**: 文本框、数字翻牌器
- **装饰组件**: 科技边框

## 🚀 快速开始

### 访问路径

```
大屏管理列表: /visualization/dashboard
大屏编辑器:   /visualization/editor
大屏预览:     /visualization/preview/:id
```

### 创建第一个大屏

1. **进入大屏管理**
   - 访问 `/visualization/dashboard`
   - 点击"新建大屏"按钮

2. **编辑大屏**
   - 从左侧组件库拖拽组件到画布
   - 点击组件查看和修改属性
   - 使用工具栏进行保存、预览等操作

3. **配置组件**
   - 选中组件后在右侧属性面板配置
   - 位置属性: X、Y、宽、高、旋转、层级
   - 样式属性: 背景色、边框、圆角、透明度
   - 数据配置: 数据源、刷新间隔

4. **保存和预览**
   - 点击工具栏"保存"按钮保存大屏
   - 点击"预览"按钮全屏预览效果

### 快捷键

- `Ctrl + Z`: 撤销
- `Ctrl + Y`: 重做
- `Ctrl + S`: 保存
- `Delete`: 删除选中组件

## 📦 组件使用说明

### 柱状图 (BarChart)

```json
{
  "data": {
    "type": "static",
    "static": {
      "xAxis": ["周一", "周二", "周三", "周四", "周五"],
      "series": [
        {
          "name": "销量",
          "data": [120, 200, 150, 80, 70]
        }
      ]
    }
  }
}
```

### 折线图 (LineChart)

```json
{
  "data": {
    "type": "static",
    "static": {
      "xAxis": ["1月", "2月", "3月", "4月", "5月"],
      "series": [
        {
          "name": "温度",
          "data": [22, 24, 26, 25, 23]
        }
      ]
    }
  }
}
```

### 饼图 (PieChart)

```json
{
  "data": {
    "type": "static",
    "static": {
      "series": [
        { "name": "直接访问", "value": 335 },
        { "name": "邮件营销", "value": 310 },
        { "name": "联盟广告", "value": 234 }
      ]
    }
  }
}
```

### 数字翻牌器 (NumberFlip)

```json
{
  "data": {
    "type": "static",
    "static": {
      "value": 12345
    }
  },
  "options": {
    "duration": 2000,
    "decimals": 0,
    "separator": ",",
    "prefix": "¥",
    "suffix": "元"
  }
}
```

## 🔐 权限配置

需要在后端配置以下权限标识：

```
visualization:dashboard:query   - 查询大屏列表
visualization:dashboard:create  - 创建大屏
visualization:dashboard:update  - 更新大屏
visualization:dashboard:delete  - 删除大屏
```

### 菜单配置

在系统管理 > 菜单管理中添加以下菜单：

```
菜单名称: 大屏可视化
路由路径: /visualization/dashboard
组件路径: visualization/dashboard/index
菜单图标: ep:data-analysis
权限标识: visualization:dashboard:query
```

## 🛠 API 接口

### 后端需要提供的接口

```typescript
// 获取大屏列表
GET  /admin-api/visualization/dashboard/list
参数: { pageNo, pageSize, name? }
返回: { list: [], total: number }

// 获取大屏详情
GET  /admin-api/visualization/dashboard/get?id=:id
返回: CanvasConfig

// 创建大屏
POST /admin-api/visualization/dashboard/create
参数: CanvasConfig
返回: { id: number }

// 更新大屏
PUT  /admin-api/visualization/dashboard/update
参数: CanvasConfig
返回: boolean

// 删除大屏
DELETE /admin-api/visualization/dashboard/delete?id=:id
返回: boolean
```

### CanvasConfig 数据结构

```typescript
interface CanvasConfig {
  id?: string | number
  name: string              // 大屏名称
  width: number            // 画布宽度 (如: 1920)
  height: number           // 画布高度 (如: 1080)
  backgroundColor: string  // 背景色
  backgroundImage?: string // 背景图片URL
  scale: {
    mode: 'scale' | 'width' | 'height' | 'stretch'  // 适配模式
    ratio: number          // 缩放比例
  }
  components: DashboardComponent[]  // 组件列表
  thumbnail?: string       // 缩略图URL
  createTime?: string
  updateTime?: string
}
```

## 📝 开发指南

### 添加新组件

1. 在 `src/components/DashboardComponents/` 下创建组件文件
2. 在 `src/components/DashboardComponents/index.ts` 中注册组件
3. 在 `src/components/DashboardComponents/config.ts` 中添加组件配置

示例:
```typescript
// 1. 创建组件 MyComponent.vue
export default {
  name: 'MyComponent',
  props: ['component', 'options', 'data']
}

// 2. 注册组件
import MyComponent from './MyComponent.vue'
export const componentMap = {
  ...
  MyComponent
}

// 3. 添加配置
export const componentLibrary = [
  ...
  {
    type: 'MyComponent',
    name: '我的组件',
    icon: 'ep:xxx',
    category: ComponentCategory.CHART,
    defaultConfig: { ... }
  }
]
```

### 数据源类型

目前支持:
- `static`: 静态数据
- `api`: API接口 (计划开发)
- `database`: 数据库查询 (计划开发)
- `websocket`: WebSocket实时数据 (计划开发)
- `mqtt`: MQTT物联网数据 (计划开发)

## 🎨 最佳实践

1. **命名规范**: 大屏名称建议使用场景+业务的方式,如"生产监控大屏"
2. **分辨率**: 推荐使用 1920×1080 (Full HD)
3. **适配模式**: 通用场景使用"等比缩放",特定屏幕使用对应模式
4. **组件数量**: 单个大屏建议不超过20个组件,保证性能
5. **定期保存**: 编辑过程中定期保存,避免数据丢失

## ❓ 常见问题

**Q: 为什么菜单中看不到"大屏可视化"入口？**
A: 需要管理员在"菜单管理"中添加对应菜单,并分配权限。

**Q: 如何自定义组件默认样式？**
A: 修改 `src/components/DashboardComponents/config.ts` 中的 defaultConfig。

**Q: 支持多屏联动吗？**
A: 当前版本暂不支持,计划在后续版本实现。

**Q: 可以导出为图片吗？**
A: 当前版本导出功能需要安装 html2canvas 库,后续版本会完善。

## 📞 技术支持

如有问题请联系开发团队或提交 Issue。

---

**版本**: v1.0.0
**更新时间**: 2025-11-17
