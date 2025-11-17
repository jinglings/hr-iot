<template>
  <el-dialog
    v-model="visible"
    title="键盘快捷键"
    width="700px"
    :close-on-click-modal="false"
  >
    <el-scrollbar max-height="600px">
      <div class="shortcuts-help">
        <!-- 快捷键分组 -->
        <div
          v-for="group in shortcutGroups"
          :key="group.name"
          class="shortcut-group"
        >
          <h3 class="group-title">{{ group.name }}</h3>
          <el-table :data="group.shortcuts" :show-header="false" size="small">
            <el-table-column prop="description" label="功能" width="200">
              <template #default="{ row }">
                <span class="shortcut-desc">{{ row.description }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="keys" label="快捷键">
              <template #default="{ row }">
                <div class="shortcut-keys">
                  <kbd
                    v-for="(key, index) in formatShortcutKeys(row)"
                    :key="index"
                    class="key-badge"
                  >
                    {{ key }}
                  </kbd>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 提示信息 -->
        <el-alert
          class="mt-20px"
          title="提示"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <ul class="tips-list">
              <li>在编辑器中按 <kbd>?</kbd> 或 <kbd>Ctrl+/</kbd> 可快速打开此帮助面板</li>
              <li>按 <kbd>Shift</kbd> + 方向键可以快速移动组件（10像素）</li>
              <li>某些快捷键在组件被选中时才生效</li>
            </ul>
          </template>
        </el-alert>
      </div>
    </el-scrollbar>

    <template #footer>
      <el-button type="primary" @click="visible = false">知道了</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import type { ShortcutGroup } from '@/composables/useKeyboardShortcuts'
import { formatShortcut } from '@/composables/useKeyboardShortcuts'

interface Props {
  modelValue: boolean
  shortcutGroups: ShortcutGroup[]
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

/**
 * 格式化快捷键为按键数组
 */
const formatShortcutKeys = (shortcut: any): string[] => {
  const keys: string[] = []

  // Mac平台使用Cmd，其他平台使用Ctrl
  const isMac = navigator.platform.toUpperCase().indexOf('MAC') >= 0

  if (shortcut.ctrl) {
    keys.push(isMac ? '⌘' : 'Ctrl')
  }
  if (shortcut.shift) {
    keys.push('Shift')
  }
  if (shortcut.alt) {
    keys.push(isMac ? '⌥' : 'Alt')
  }
  if (shortcut.meta) {
    keys.push('⌘')
  }

  // 特殊键名映射
  const keyMap: Record<string, string> = {
    delete: 'Del',
    escape: 'Esc',
    arrowup: '↑',
    arrowdown: '↓',
    arrowleft: '←',
    arrowright: '→',
    ' ': 'Space',
    '/': '/',
    '?': '?'
  }

  const key = keyMap[shortcut.key.toLowerCase()] || shortcut.key.toUpperCase()
  keys.push(key)

  return keys
}
</script>

<style lang="scss" scoped>
.shortcuts-help {
  .shortcut-group {
    margin-bottom: 24px;

    &:last-child {
      margin-bottom: 0;
    }

    .group-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--el-text-color-primary);
      margin-bottom: 12px;
      padding-bottom: 8px;
      border-bottom: 2px solid var(--el-color-primary);
    }

    :deep(.el-table) {
      .el-table__body {
        tr {
          &:hover {
            background-color: var(--el-fill-color-light);
          }
        }
      }

      td {
        padding: 12px 0;
      }
    }

    .shortcut-desc {
      font-size: 14px;
      color: var(--el-text-color-regular);
    }

    .shortcut-keys {
      display: flex;
      align-items: center;
      gap: 4px;
      flex-wrap: wrap;

      .key-badge {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        min-width: 28px;
        height: 28px;
        padding: 0 8px;
        font-family: 'Courier New', monospace;
        font-size: 13px;
        font-weight: 600;
        color: var(--el-text-color-primary);
        background: linear-gradient(
          to bottom,
          var(--el-fill-color-lighter),
          var(--el-fill-color-light)
        );
        border: 1px solid var(--el-border-color);
        border-radius: 4px;
        box-shadow:
          0 1px 2px rgba(0, 0, 0, 0.05),
          inset 0 -1px 0 rgba(0, 0, 0, 0.1);
        white-space: nowrap;

        &:first-child {
          margin-left: 0;
        }
      }
    }
  }

  .tips-list {
    margin: 0;
    padding-left: 20px;

    li {
      margin-bottom: 8px;
      font-size: 13px;
      color: var(--el-text-color-regular);

      &:last-child {
        margin-bottom: 0;
      }

      kbd {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        min-width: 20px;
        padding: 2px 6px;
        font-family: 'Courier New', monospace;
        font-size: 12px;
        font-weight: 600;
        color: var(--el-color-primary);
        background-color: var(--el-fill-color-light);
        border: 1px solid var(--el-border-color);
        border-radius: 3px;
        margin: 0 2px;
      }
    }
  }
}
</style>
