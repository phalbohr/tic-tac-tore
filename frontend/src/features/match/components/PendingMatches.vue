<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import BaseButton from '@/core/components/BaseButton.vue'

export interface PendingMatchItem {
  id: string
  creatorNickname?: string
  teamAScore?: number
  teamBScore?: number
  createdAt?: string
}

defineProps<{
  pendingMatches: PendingMatchItem[]
  pendingConfirmationId?: string | null
}>()

const emit = defineEmits<{
  (e: 'confirm', matchId: string): void
}>()

const { t } = useI18n()
</script>

<template>
  <div v-if="pendingMatches.length > 0" class="w-full flex flex-col gap-3 my-4">
    <h2 class="text-sm font-bold text-on-surface-variant uppercase tracking-wider text-left">
      {{ t('match.pending') }}
    </h2>

    <div
      v-for="match in pendingMatches"
      :key="match.id"
      class="w-full bg-surface-container-highest rounded-2xl p-4 flex flex-col gap-3 shadow-md border-0"
      :data-testid="`pending-match-card-${match.id}`"
    >
      <div class="flex items-center justify-between">
        <span class="text-xs font-semibold px-2.5 py-1 rounded-full bg-warning/20 text-warning">
          {{ t('match.pending') }}
        </span>
        <span v-if="match.createdAt" class="text-xs text-on-surface-variant">
          {{ new Date(match.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }}
        </span>
      </div>

      <div class="flex items-center justify-between text-left">
        <div>
          <p class="font-bold text-on-surface text-base">
            {{ match.creatorNickname ? match.creatorNickname : 'Match Request' }}
          </p>
          <p v-if="match.teamAScore !== undefined && match.teamBScore !== undefined" class="text-sm text-on-surface-variant">
            Score: {{ match.teamAScore }} - {{ match.teamBScore }}
          </p>
        </div>

        <BaseButton
          v-if="pendingConfirmationId !== match.id"
          variant="primary"
          @click="emit('confirm', match.id)"
          class="!h-12 px-5 font-bold min-h-12 min-w-[48px]"
          :data-testid="`confirm-match-btn-${match.id}`"
        >
          {{ t('match.confirm') }}
        </BaseButton>
        <span v-else class="text-xs italic text-primary font-medium">
          Match confirmed. Tap to undo.
        </span>
      </div>
    </div>
  </div>
</template>
