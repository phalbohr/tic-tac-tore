<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import type { MatchResponse } from '@/services/matchService'
import AvatarBase from '@/components/AvatarBase.vue'

const props = defineProps<{
  match: MatchResponse
}>()

const { t } = useI18n()
const authStore = useAuthStore()

const currentUserId = computed(() => authStore.profile?.id)
const currentUserNickname = computed(() => authStore.profile?.nickname)

const is2v2 = computed(() => {
  return !!(
    props.match.teamADefenderId ||
    props.match.teamBDefenderId ||
    props.match.teamADefenderNickname ||
    props.match.teamBDefenderNickname
  )
})

const matchTypeTag = computed(() => (is2v2.value ? '2v2' : '1v1'))

const userTeam = computed<'A' | 'B' | 'NONE'>(() => {
  const uid = currentUserId.value
  const nick = currentUserNickname.value

  const isTeamA =
    (uid && (props.match.teamAAttackerId === uid || props.match.teamADefenderId === uid)) ||
    (nick && (props.match.teamAAttackerNickname === nick || props.match.teamADefenderNickname === nick))

  if (isTeamA) return 'A'

  const isTeamB =
    (uid && (props.match.teamBAttackerId === uid || props.match.teamBDefenderId === uid)) ||
    (nick && (props.match.teamBAttackerNickname === nick || props.match.teamBDefenderNickname === nick))

  if (isTeamB) return 'B'

  return 'NONE'
})

const matchOutcome = computed<'WIN' | 'LOSS' | 'DRAW'>(() => {
  let teamAWins = 0
  let teamBWins = 0
  let teamATotal = 0
  let teamBTotal = 0

  if (props.match.games && props.match.games.length > 0) {
    for (const g of props.match.games) {
      teamATotal += g.teamAScore
      teamBTotal += g.teamBScore
      if (g.teamAScore > g.teamBScore) teamAWins++
      else if (g.teamBScore > g.teamAScore) teamBWins++
    }
  }

  const teamAWon = teamAWins > teamBWins || (teamAWins === teamBWins && teamATotal > teamBTotal)
  const teamBWon = teamBWins > teamAWins || (teamAWins === teamBWins && teamBTotal > teamATotal)

  if (userTeam.value === 'A') {
    if (teamAWon) return 'WIN'
    if (teamBWon) return 'LOSS'
    return 'DRAW'
  } else if (userTeam.value === 'B') {
    if (teamBWon) return 'WIN'
    if (teamAWon) return 'LOSS'
    return 'DRAW'
  }

  if (teamAWon) return 'WIN'
  if (teamBWon) return 'LOSS'
  return 'DRAW'
})

const outcomeBadgeClass = computed(() => {
  switch (matchOutcome.value) {
    case 'WIN':
      return 'bg-emerald-500/20 text-emerald-400'
    case 'LOSS':
      return 'bg-red-500/20 text-red-400'
    case 'DRAW':
    default:
      return 'bg-neutral-500/20 text-neutral-300'
  }
})

const outcomeLabel = computed(() => {
  switch (matchOutcome.value) {
    case 'WIN':
      return t('history.outcome.win', 'Win')
    case 'LOSS':
      return t('history.outcome.loss', 'Loss')
    case 'DRAW':
    default:
      return t('history.outcome.draw', 'Draw')
  }
})

function formatPlayerName(nickname?: string | null): string {
  if (!nickname) return t('history.retiredPlayer', 'Retired Player')
  if (nickname.startsWith('ex-player-')) return t('history.retiredPlayer', 'Retired Player')
  return nickname
}

const formattedDate = computed(() => {
  if (!props.match.createdAt) return ''
  const d = new Date(props.match.createdAt)
  return d.toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
})

const scoreSummary = computed(() => {
  if (!props.match.games || props.match.games.length === 0) return ''
  return props.match.games.map((g) => `${g.teamAScore} - ${g.teamBScore}`).join(', ')
})
</script>

<template>
  <div
    class="ch-match-card w-full bg-surface-container-highest rounded-2xl p-4 flex flex-col gap-3 shadow-md border-0 transition-all hover:bg-surface-container-highest/90"
    :data-testid="`match-card-${match.id}`"
  >
    <!-- Header: Status / Outcome & Tags & Date -->
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-2">
        <span
          class="text-xs font-bold px-2.5 py-1 rounded-full uppercase tracking-wider"
          :class="outcomeBadgeClass"
          :data-testid="`outcome-badge-${match.id}`"
        >
          {{ outcomeLabel }}
        </span>
        <span
          class="text-xs font-medium px-2 py-0.5 rounded-full bg-surface-container-low text-on-surface-variant"
        >
          {{ matchTypeTag }}
        </span>
        <span
          v-if="match.matchFormat"
          class="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-surface-container-low text-on-surface-variant uppercase"
        >
          {{ match.matchFormat }}
        </span>
      </div>
      <span class="text-xs text-on-surface-variant font-medium">
        {{ formattedDate }}
      </span>
    </div>

    <!-- Teams & Scores Row -->
    <div class="flex items-center justify-between gap-2 py-2">
      <!-- Team A -->
      <div class="flex items-center gap-2 flex-1 min-w-0">
        <div class="flex -space-x-2 shrink-0">
          <div class="w-8 h-8 rounded-full overflow-hidden bg-surface-container-low ring-2 ring-surface-container-highest">
            <AvatarBase
              :avatar="match.teamAAttackerAvatar"
              :name="formatPlayerName(match.teamAAttackerNickname)"
            />
          </div>
          <div
            v-if="is2v2 && (match.teamADefenderId || match.teamADefenderNickname)"
            class="w-8 h-8 rounded-full overflow-hidden bg-surface-container-low ring-2 ring-surface-container-highest"
          >
            <AvatarBase
              :avatar="match.teamADefenderAvatar"
              :name="formatPlayerName(match.teamADefenderNickname)"
            />
          </div>
        </div>
        <div class="flex flex-col min-w-0">
          <span class="text-xs font-bold text-on-surface truncate">
            {{ formatPlayerName(match.teamAAttackerNickname) }}
          </span>
          <span
            v-if="is2v2 && match.teamADefenderNickname"
            class="text-[10px] text-on-surface-variant truncate"
          >
            {{ formatPlayerName(match.teamADefenderNickname) }}
          </span>
        </div>
      </div>

      <!-- Score Display -->
      <div class="flex flex-col items-center justify-center shrink-0 px-3 py-1.5 rounded-xl bg-surface-container-low">
        <span class="text-sm font-black tracking-tight text-on-surface" :data-testid="`match-score-${match.id}`">
          {{ scoreSummary }}
        </span>
      </div>

      <!-- Team B -->
      <div class="flex items-center justify-end gap-2 flex-1 min-w-0 text-right">
        <div class="flex flex-col min-w-0 items-end">
          <span class="text-xs font-bold text-on-surface truncate">
            {{ formatPlayerName(match.teamBAttackerNickname) }}
          </span>
          <span
            v-if="is2v2 && match.teamBDefenderNickname"
            class="text-[10px] text-on-surface-variant truncate"
          >
            {{ formatPlayerName(match.teamBDefenderNickname) }}
          </span>
        </div>
        <div class="flex -space-x-2 shrink-0">
          <div class="w-8 h-8 rounded-full overflow-hidden bg-surface-container-low ring-2 ring-surface-container-highest">
            <AvatarBase
              :avatar="match.teamBAttackerAvatar"
              :name="formatPlayerName(match.teamBAttackerNickname)"
            />
          </div>
          <div
            v-if="is2v2 && (match.teamBDefenderId || match.teamBDefenderNickname)"
            class="w-8 h-8 rounded-full overflow-hidden bg-surface-container-low ring-2 ring-surface-container-highest"
          >
            <AvatarBase
              :avatar="match.teamBDefenderAvatar"
              :name="formatPlayerName(match.teamBDefenderNickname)"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ch-match-card {
  border: none;
}
</style>
