<template>
  <div class="ai-floating-wrapper">
    <!-- Floating Button -->
    <transition name="bounce">
      <div v-if="!panelVisible" class="ai-floating-btn" @click="panelVisible = true">
        <span class="btn-icon">🤖</span>
        <span class="btn-label">AI 助手</span>
      </div>
    </transition>

    <!-- Chat Panel (slide in from right) -->
    <transition name="slide-right">
      <div v-if="panelVisible" class="ai-panel-container">
        <AiChatPanel @close="panelVisible = false" />
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AiChatPanel from './AiChatPanel.vue'

const panelVisible = ref(false)
</script>

<style scoped lang="scss">
.ai-floating-wrapper {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 2000;
}

.ai-floating-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  background: var(--el-color-primary);
  color: white;
  border-radius: 24px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s;
  user-select: none;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
  }

  .btn-icon {
    font-size: 20px;
  }

  .btn-label {
    font-size: 14px;
    font-weight: 500;
  }
}

.ai-panel-container {
  position: fixed;
  bottom: 24px;
  right: 24px;
  width: 400px;
  height: 600px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  overflow: hidden;
  border: 1px solid var(--el-border-color-light);
}

/* Animations */
.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.3s ease;
}

.slide-right-enter-from,
.slide-right-leave-to {
  opacity: 0;
  transform: translateX(20px) translateY(20px) scale(0.9);
}

.bounce-enter-active {
  animation: bounce-in 0.3s;
}

.bounce-leave-active {
  animation: bounce-in 0.2s reverse;
}

@keyframes bounce-in {
  0% {
    transform: scale(0.8);
    opacity: 0;
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
