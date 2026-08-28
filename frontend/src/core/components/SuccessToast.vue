<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import BaseButton from '@/core/components/BaseButton.vue';

defineProps<{
  message: string;
}>();

const emit = defineEmits<{
  (e: 'dismiss'): void;
}>();

const { t } = useI18n();
</script>

<template>
  <Transition name="toast-slide">
    <div
      v-if="!!message"
      class="fixed bottom-6 left-4 right-4 z-50 max-w-md mx-auto bg-primary text-background rounded-2xl p-4 shadow-2xl flex items-center justify-between gap-4"
      role="status"
      aria-live="polite"
      data-testid="success-toast"
    >
      <div class="flex items-center gap-3">
        <span class="material-symbols-outlined text-xl">check_circle</span>
        <span class="text-sm font-medium">
          {{ message }}
        </span>
      </div>

      <BaseButton
        variant="secondary"
        @click="emit('dismiss')"
        class="!h-10 px-4 text-xs font-bold bg-background/20 text-background hover:bg-background/30"
      >
        {{ t('common.close', 'Close') }}
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
