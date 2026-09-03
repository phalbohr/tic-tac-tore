<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import BaseButton from '@/core/components/BaseButton.vue'
import { useMatchConfirmationStore } from '@/features/match/stores/matchConfirmationStore'

const { t } = useI18n()
const confirmationStore = useMatchConfirmationStore()

function handleConfirmationUndo(matchId?: string) {
  confirmationStore.cancelConfirmationTimer(matchId)
}

function getConfirmationToastMessage(matchNumber: number): string {
  const msg = t('match.matchConfirmedTapUndo', { number: matchNumber })
  return msg !== 'match.matchConfirmedTapUndo'
    ? msg
    : `Match ${matchNumber} confirmed. Tap to undo.`
}
</script>

<template>
  <div
    v-if="confirmationStore.activeConfirmations.length > 0"
    class="fixed bottom-6 left-4 right-4 z-50 max-w-md mx-auto pointer-events-none flex flex-col gap-2.5 items-stretch"
    data-testid="confirmation-toast-stack"
  >
    <TransitionGroup name="toast-list">
      <div
        v-for="item in confirmationStore.activeConfirmations"
        :key="item.matchId"
        class="pointer-events-auto w-full bg-surface-container-highest text-on-surface rounded-2xl p-4 shadow-2xl flex items-center justify-between gap-4"
        role="status"
        aria-live="polite"
        :data-testid="`confirmation-toast-${item.matchId}`"
      >
        <div class="flex items-center gap-3">
          <div
            class="w-8 h-8 rounded-full bg-primary/20 text-primary flex items-center justify-center font-bold text-sm"
          >
            {{ item.countdown }}s
          </div>
          <span class="text-sm font-medium">
            {{
              item.isOfflinePending
                ? t('match.willRetryOnline')
                : getConfirmationToastMessage(item.matchNumber)
            }}
          </span>
        </div>

        <BaseButton
          v-if="!item.isOfflinePending"
          variant="primary"
          @click="handleConfirmationUndo(item.matchId)"
          class="!h-10 px-4 text-xs font-bold min-h-12 min-w-[48px]"
          :data-testid="`undo-confirmation-btn-${item.matchId}`"
        >
          {{ t('match.undo') }}
        </BaseButton>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.toast-list-move,
.toast-list-enter-active,
.toast-list-leave-active {
  transition: all 0.3s ease;
}

.toast-list-enter-from,
.toast-list-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>
