<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { TournamentBracketDto, RoundMatchesDto } from '@/features/tournament/types/tournament'
import TournamentMatchCard from './TournamentMatchCard.vue'

interface Props {
  bracket: TournamentBracketDto
  currentUserId?: string | null
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'start-match', matchId: string): void
}>()

const { t, te } = useI18n()

function getRoundDisplayName(round: RoundMatchesDto): string {
  try {
    if (te('tournament.bracket.final')) {
      if (props.bracket.format === 'CUP') {
        const fromFinal = props.bracket.totalRounds - round.round
        if (fromFinal === 0) return t('tournament.bracket.final')
        if (fromFinal === 1) return t('tournament.bracket.semifinals')
        if (fromFinal === 2) return t('tournament.bracket.quarterfinals')
      }
      return t('tournament.bracket.round', { round: round.round })
    }
  } catch {
    // Fallback for tests mounted without i18n
  }
  return round.roundName || `Round ${round.round}`
}
</script>

<template>
  <div data-testid="tournament-bracket-view" class="w-full space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-xl font-bold text-on-surface">
          {{ bracket.tournamentName }}
        </h2>
        <p class="text-xs text-on-surface-variant">
          {{ bracket.format }} • {{ bracket.totalRounds }} Rounds
        </p>
      </div>
    </div>

    <!-- Bracket Multi-Column Tree -->
    <div class="overflow-x-auto pb-6">
      <div class="flex gap-8 min-w-max items-center">
        <div
          v-for="round in bracket.rounds"
          :key="round.round"
          data-testid="bracket-round-column"
          class="flex flex-col gap-6"
        >
          <div
            class="text-center font-bold text-xs uppercase tracking-wider text-on-surface-variant px-3 py-1.5 rounded-xl bg-surface-container-high/60"
          >
            {{ getRoundDisplayName(round) }}
          </div>

          <div class="flex flex-col justify-around gap-8 flex-1">
            <TournamentMatchCard
              v-for="match in round.matches"
              :key="match.id"
              :match="match"
              :current-user-id="currentUserId"
              @start-match="emit('start-match', $event)"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
