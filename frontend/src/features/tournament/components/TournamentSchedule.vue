<script setup lang="ts">
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import type { TournamentBracketDto, RoundMatchesDto } from '@/features/tournament/types/tournament';
import TournamentMatchCard from './TournamentMatchCard.vue';

interface Props {
  bracket: TournamentBracketDto;
}

defineProps<Props>();
const { t, te } = useI18n();
const activeRound = ref(1);

function getRoundDisplayName(round: RoundMatchesDto): string {
  try {
    if (te('tournament.bracket.round')) {
      return t('tournament.bracket.round', { round: round.round });
    }
  } catch {
    // Fallback for tests mounted without i18n
  }
  return round.roundName || `Round ${round.round}`;
}
</script>

<template>
  <div data-testid="tournament-schedule-view" class="w-full space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-xl font-bold text-on-surface">
          {{ bracket.tournamentName }}
        </h2>
        <p class="text-xs text-on-surface-variant">
          Championship Schedule • {{ bracket.totalRounds }} Rounds
        </p>
      </div>
    </div>

    <!-- Round Selector Tabs -->
    <div class="flex gap-2 border-b border-outline-variant/10 pb-2 overflow-x-auto">
      <button
        v-for="round in bracket.rounds"
        :key="round.round"
        type="button"
        class="px-4 py-2 text-xs font-semibold rounded-xl transition-colors whitespace-nowrap"
        :class="activeRound === round.round ? 'bg-primary text-on-primary' : 'bg-surface-container-low text-on-surface hover:bg-surface-container'"
        @click="activeRound = round.round"
      >
        {{ getRoundDisplayName(round) }}
      </button>
    </div>

    <!-- Active Round Matches -->
    <template v-for="round in bracket.rounds" :key="round.round">
      <div
        v-if="activeRound === round.round"
        class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4"
      >
        <TournamentMatchCard
          v-for="match in round.matches"
          :key="match.id"
          :match="match"
        />
      </div>
    </template>
  </div>
</template>
