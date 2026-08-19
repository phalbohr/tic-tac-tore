<script setup lang="ts">
import { useStatsStore } from '../stores/useStatsStore'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
  opponentId?: string
  opponentNickname?: string
  title?: string
  message?: string
}>()

const statsStore = useStatsStore()
const router = useRouter()
const { t } = useI18n()

function enableDemoData() {
  statsStore.toggleDemoMode(true)
}

function recordMatch() {
  if (props.opponentId) {
    router.push({ path: '/matches/new', query: { opponentId: props.opponentId } })
  } else {
    router.push('/matches/new')
  }
}
</script>

<template>
  <div class="ch-empty-state w-full flex items-center justify-center p-4">
    <div class="w-full max-w-sm bg-surface-container-low rounded-2xl p-6 space-y-6 shadow-2xl text-center">
      <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-primary-container text-primary mb-2">
        <span class="material-symbols-outlined text-3xl">sports_soccer</span>
      </div>
      
      <div>
        <h2 class="font-headline text-xl font-bold text-on-surface mb-2">
          {{ props.title || (props.opponentNickname ? t('h2h.title', 'Head-to-Head') : t('stats.noMatchesYet', 'No Matches Yet')) }}
        </h2>
        <p class="text-sm text-on-surface-variant leading-relaxed">
          <template v-if="props.opponentNickname">
            {{ t('h2h.emptyState', { opponent: props.opponentNickname }, `You haven't played ${props.opponentNickname} yet — start a match?`) }}
          </template>
          <template v-else-if="props.message">
            {{ props.message }}
          </template>
          <template v-else>
            {{ t('stats.emptyStateDescription', 'Record your first match to start tracking your statistics and climb the leaderboard!') }}
          </template>
        </p>
      </div>

      <div class="flex flex-col gap-3 pt-2">
        <button 
          @click="recordMatch"
          class="w-full py-3.5 rounded-xl bg-gradient-to-br from-primary to-primary-container text-background font-headline font-extrabold uppercase tracking-wider shadow-xl hover:opacity-90 active:scale-95 transition-all flex items-center justify-center gap-2 cursor-pointer"
        >
          <span class="material-symbols-outlined font-bold">add_circle</span>
          {{ props.opponentId ? t('h2h.startMatch', 'Start a match') : t('stats.recordFirstMatch', 'Record First Match') }}
        </button>
        
        <button 
          @click="enableDemoData"
          class="w-full py-3.5 rounded-xl bg-surface-container-highest hover:bg-surface-container-highest/80 text-on-surface font-headline font-bold uppercase tracking-wider text-sm transition-colors flex items-center justify-center gap-2 cursor-pointer"
        >
          <span class="material-symbols-outlined text-sm">visibility</span>
          {{ t('stats.toggleDemoData', 'Toggle Demo Data') }}
        </button>
      </div>
    </div>
  </div>
</template>

