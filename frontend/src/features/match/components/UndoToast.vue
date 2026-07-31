<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import BaseButton from '@/core/components/BaseButton.vue'

defineProps<{
  countdown: number
  isOffline?: boolean
  message?: string
}>()

const emit = defineEmits<{
  (e: 'undo'): void
}>()

const { t } = useI18n()
</script>

<template>
  <Transition name="toast-slide">
    <div
      v-if="countdown > 0"
      class="fixed bottom-6 left-4 right-4 z-50 max-w-md mx-auto bg-surface-container-highest text-on-surface rounded-2xl p-4 shadow-2xl flex items-center justify-between gap-4"
      role="status"
      aria-live="polite"
    >
      <div class="flex items-center gap-3">
        <div class="w-8 h-8 rounded-full bg-primary/20 text-primary flex items-center justify-center font-bold text-sm">
          {{ countdown }}s
        </div>
        <span class="text-sm font-medium">
          {{ message ? message : (isOffline ? t('match.willRetryOnline') : t('match.submittedTapUndo')) }}
        </span>
      </div>

      <BaseButton
        v-if="!isOffline"
        variant="primary"
        @click="emit('undo')"
        class="!h-10 px-4 text-xs font-bold min-h-12 min-w-[48px]"
      >
        {{ t('match.undo') }}
      </BaseButton>
    </div>
  </Transition>
</template>

<style scoped>
.toast-slide-enter-active,
.toast-slide-leave-active {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.toast-slide-enter-from,
.toast-slide-leave-to {
  transform: translateY(100%);
  opacity: 0;
}
</style>
