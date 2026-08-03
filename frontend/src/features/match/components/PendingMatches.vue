<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import BaseButton from '@/core/components/BaseButton.vue'

export interface GameScoreItem {
  teamAScore: number
  teamBScore: number
  teamAAttackerId?: string
  teamADefenderId?: string
  teamBAttackerId?: string
  teamBDefenderId?: string
}

export interface PendingMatchItem {
  id: string
  status?: string
  creatorNickname?: string
  teamAAttackerId?: string
  teamADefenderId?: string
  teamBAttackerId?: string
  teamBDefenderId?: string
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
  (e: 'dismiss-rejection', matchId: string): void
  (e: 'edit-rejection', match: PendingMatchItem): void
  (e: 'delete-rejection', matchId: string): void
  (e: 'close', matchId: string): void
}>()

const { t } = useI18n()

const matchIdToDelete = ref<string | null>(null)

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

    <TransitionGroup name="list" tag="div" class="flex flex-col gap-3 w-full">
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
              class="w-full flex flex-col gap-2 text-xs text-red-400 font-medium bg-surface-container/60 p-3 rounded-xl border border-red-500/20"
              :data-testid="`rejection-reason-${match.id}`"
            >
              <div>{{ t('match.rejectionReasonLabel', 'Rejection reason') }}: {{ match.rejectionReason || t('match.noReasonGiven', 'No reason provided') }}</div>
              <div class="grid grid-cols-3 gap-2 w-full mt-2">
                <button
                  type="button"
                  class="min-h-[48px] h-12 text-sm font-bold bg-primary hover:opacity-90 text-black rounded-xl transition-colors flex items-center justify-center"
                  :data-testid="`edit-rejection-btn-${match.id}`"
                  @click="emit('edit-rejection', match)"
                >
                  {{ t('match.editMatch', 'Edit Match') }}
                </button>
                <button
                  type="button"
                  class="min-h-[48px] h-12 text-sm font-bold bg-red-950/80 hover:bg-red-900 text-red-400 border border-red-500/40 rounded-xl transition-colors flex items-center justify-center"
                  :data-testid="`delete-rejection-btn-${match.id}`"
                  @click="matchIdToDelete = match.id"
                >
                  {{ t('match.deleteMatch', 'Delete Match') }}
                </button>
                <button
                  type="button"
                  class="min-h-[48px] h-12 text-sm font-bold bg-surface-container-low hover:bg-neutral-700 text-on-surface border border-neutral-600/50 rounded-xl transition-colors flex items-center justify-center"
                  :data-testid="`close-match-btn-${match.id}`"
                  @click="emit('close', match.id)"
                >
                  {{ t('match.close', 'Close') }}
                </button>
              </div>
            </div>
          </template>

          <template v-else-if="!isPendingConfirmation(match.id)">
            <div class="grid grid-cols-3 gap-2 w-full">
              <button
                type="button"
                class="min-h-[48px] h-12 rounded-xl font-bold bg-surface-container-low hover:bg-red-950/40 text-red-400 border border-red-500/30 transition-colors flex items-center justify-center text-sm"
                :data-testid="`reject-match-btn-${match.id}`"
                @click="emit('reject', match.id, mIdx + 1)"
              >
                {{ t('match.reject') }}
              </button>
              <BaseButton
                variant="primary"
                @click="emit('confirm', match.id, mIdx + 1)"
                class="!h-12 min-h-[48px] font-bold text-sm"
                :data-testid="`confirm-match-btn-${match.id}`"
              >
                {{ t('match.confirm') }}
              </BaseButton>
              <button
                type="button"
                class="min-h-[48px] h-12 rounded-xl font-bold bg-surface-container-low hover:bg-neutral-700 text-on-surface border border-neutral-600/50 transition-colors flex items-center justify-center text-sm"
                :data-testid="`close-match-btn-${match.id}`"
                @click="emit('close', match.id)"
              >
                {{ t('match.close', 'Close') }}
              </button>
            </div>
          </template>
          <div v-else class="min-h-[48px] h-12 flex items-center justify-center w-full">
            <span class="text-xs italic text-primary font-medium text-center">
              {{ t('match.confirmedTapUndo') }}
            </span>
          </div>
        </div>
      </div>
    </TransitionGroup>

    <!-- Confirmation Modal for Match Deletion -->
    <Transition name="ch-fade">
      <div
        v-if="matchIdToDelete"
        class="fixed inset-0 z-50 flex items-center justify-center p-6 bg-black/75 backdrop-blur-md"
        role="dialog"
        aria-modal="true"
        data-testid="delete-confirmation-modal"
      >
        <div class="w-full max-w-sm bg-surface-container-low rounded-2xl p-6 space-y-6 shadow-2xl">
          <div class="text-center space-y-2">
            <div class="inline-flex items-center justify-center w-12 h-12 rounded-full bg-red-950/30 text-red-400 mb-2">
              <span class="material-symbols-outlined text-2xl">warning</span>
            </div>
            <h2 class="font-headline text-lg font-bold text-on-surface">
              Cancel Match
            </h2>
            <p class="text-xs text-on-surface-variant leading-relaxed">
              Are you sure you want to delete this match? All recorded scores will be lost.
            </p>
          </div>

          <div class="flex flex-col gap-2">
            <BaseButton
              @click="emit('delete-rejection', matchIdToDelete); matchIdToDelete = null"
              class="w-full !bg-red-600 hover:!bg-red-700 !text-white font-headline font-extrabold uppercase tracking-wider text-xs !h-12"
              data-testid="confirm-delete-btn"
            >
              Confirm
            </BaseButton>

            <BaseButton
              variant="secondary"
              @click="matchIdToDelete = null"
              class="w-full font-headline font-bold text-xs !h-12"
              data-testid="keep-editing-btn"
            >
              Keep Editing
            </BaseButton>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.list-move,
.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.ch-fade-enter-active,
.ch-fade-leave-active {
  transition: opacity 0.2s ease;
}

.ch-fade-enter-from,
.ch-fade-leave-to {
  opacity: 0;
}
</style>
