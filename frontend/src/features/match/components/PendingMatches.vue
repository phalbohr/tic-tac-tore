<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import BaseButton from '@/core/components/BaseButton.vue'

export interface GameScoreItem {
  teamAScore: number
  teamBScore: number
}

export interface PendingMatchItem {
  id: string
  status?: string
  creatorNickname?: string
  teamANames?: string[]
  teamBNames?: string[]
  teamAScore?: number
  teamBScore?: number
  games?: GameScoreItem[]
  createdAt?: string
  rejectionReason?: string
}

const props = defineProps<{
  pendingMatches: PendingMatchItem[]
  pendingConfirmationId?: string | null
  pendingConfirmationIds?: string[]
}>()

const emit = defineEmits<{
  (e: 'confirm', matchId: string, matchNumber: number): void
  (e: 'reject', matchId: string, matchNumber: number): void
}>()

const { t } = useI18n()

function isPendingConfirmation(matchId: string): boolean {
  if (props.pendingConfirmationIds && props.pendingConfirmationIds.includes(matchId)) {
    return true
  }
  if (props.pendingConfirmationId && props.pendingConfirmationId === matchId) {
    return true
  }
  return false
}

function getMatchBadgeText(index: number): string {
  const res = t('match.pendingMatch', { number: index + 1 })
  return res !== 'match.pendingMatch' ? res : `Match ${index + 1}`
}
</script>

<template>
  <div v-if="pendingMatches.length > 0" class="w-full flex flex-col gap-3 my-4">
    <h2 class="text-sm font-bold text-on-surface-variant uppercase tracking-wider text-left">
      {{ t('match.pending') }}
    </h2>

    <div
      v-for="(match, mIdx) in pendingMatches"
      :key="match.id"
      class="w-full bg-surface-container-highest rounded-2xl p-4 flex flex-col gap-3 shadow-md border-0"
      :data-testid="`pending-match-card-${match.id}`"
    >
      <div class="flex items-center justify-between">
        <span class="text-xs font-semibold px-2.5 py-1 rounded-full bg-warning/20 text-warning">
          {{ getMatchBadgeText(mIdx) }}
        </span>
        <span v-if="match.createdAt" class="text-xs text-on-surface-variant">
          {{ new Date(match.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }}
        </span>
      </div>

      <!-- 3-Column Table Layout -->
      <div class="grid grid-cols-3 gap-2 items-center bg-surface-container/60 p-3 rounded-xl">
        <!-- Team A Column -->
        <div class="flex flex-col text-left font-medium text-sm text-on-surface">
          <span class="text-xs text-on-surface-variant font-bold uppercase mb-1">
            {{ t('match.teamA') }}
          </span>
          <template v-if="match.teamANames && match.teamANames.length > 0">
            <span v-for="(name, idx) in match.teamANames" :key="idx" class="truncate font-semibold">
              {{ name }}
            </span>
          </template>
          <span v-else class="truncate font-semibold">Team A</span>
        </div>

        <!-- Scores Column (Center) -->
        <div class="flex flex-col items-center justify-center text-center">
          <span class="text-xs text-on-surface-variant font-bold uppercase mb-1">
            {{ t('match.scores') }}
          </span>
          <template v-if="match.games && match.games.length > 0">
            <div
              v-for="(game, gIdx) in match.games"
              :key="gIdx"
              class="font-headline font-bold text-base text-primary leading-tight"
            >
              {{ game.teamAScore }} : {{ game.teamBScore }}
            </div>
          </template>
          <div v-else-if="match.teamAScore !== undefined && match.teamBScore !== undefined" class="font-headline font-bold text-base text-primary leading-tight">
            {{ match.teamAScore }} : {{ match.teamBScore }}
          </div>
        </div>

        <!-- Team B Column -->
        <div class="flex flex-col text-right font-medium text-sm text-on-surface">
          <span class="text-xs text-on-surface-variant font-bold uppercase mb-1">
            {{ t('match.teamB') }}
          </span>
          <template v-if="match.teamBNames && match.teamBNames.length > 0">
            <span v-for="(name, idx) in match.teamBNames" :key="idx" class="truncate font-semibold">
              {{ name }}
            </span>
          </template>
          <span v-else class="truncate font-semibold">Team B</span>
        </div>
      </div>

      <!-- Action Button / Confirmation State / Rejection Reason -->
      <div class="w-full flex items-center gap-2 justify-end mt-1">
        <template v-if="match.status === 'REJECTED'">
          <div
            class="w-full text-center text-xs text-red-400 font-medium bg-surface-container/60 p-2.5 rounded-xl border border-red-500/20"
            :data-testid="`rejection-reason-${match.id}`"
          >
            {{ t('match.rejectionReasonLabel', 'Rejection reason') }}: {{ match.rejectionReason || t('match.noReasonGiven', 'No reason provided') }}
          </div>
        </template>
        <template v-else-if="!isPendingConfirmation(match.id)">
          <button
            type="button"
            class="flex-1 min-h-12 h-12 rounded-xl font-bold bg-surface-container hover:bg-neutral-800 text-red-400 border-none transition-colors"
            :data-testid="`reject-match-btn-${match.id}`"
            @click="emit('reject', match.id, mIdx + 1)"
          >
            {{ t('match.reject') }}
          </button>
          <BaseButton
            variant="primary"
            @click="emit('confirm', match.id, mIdx + 1)"
            class="flex-1 !h-12 font-bold min-h-12"
            :data-testid="`confirm-match-btn-${match.id}`"
          >
            {{ t('match.confirm') }}
          </BaseButton>
        </template>
        <span v-else class="text-xs italic text-primary font-medium w-full text-center py-2">
          {{ t('match.confirmedTapUndo') }}
        </span>
      </div>
    </div>
  </div>
</template>
