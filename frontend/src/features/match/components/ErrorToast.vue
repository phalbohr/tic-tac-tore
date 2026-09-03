<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import BaseButton from '@/core/components/BaseButton.vue'

defineProps<{
  message: string
}>()

const emit = defineEmits<{
  (e: 'dismiss'): void
}>()

const { t } = useI18n()
</script>

<template>
  <Transition name="toast-slide">
    <div
      v-if="!!message"
      class="fixed bottom-6 left-4 right-4 z-50 max-w-md mx-auto bg-error-container text-on-error-container rounded-2xl p-4 shadow-2xl flex items-center justify-between gap-4"
      role="alert"
      aria-live="assertive"
      data-testid="error-toast"
    >
      <div class="flex items-center gap-3">
        <span class="text-sm font-medium">
          {{ message }}
        </span>
      </div>

      <BaseButton variant="secondary" @click="emit('dismiss')" class="!h-10 px-4 text-xs font-bold">
        {{ t('common.close', 'Close') }}
      </BaseButton>
    </div>
  </Transition>
</template>

<style scoped>
.toast-slide-enter-active,
.toast-slide-leave-active {
  transition:
    transform 0.3s ease,
    opacity 0.3s ease;
}

.toast-slide-enter-from,
.toast-slide-leave-to {
  transform: translateY(100%);
  opacity: 0;
}
</style>
