<script setup lang="ts">
import type { TournamentBracketDto } from '@/features/tournament/types/tournament';
import TournamentMatchCard from './TournamentMatchCard.vue';

interface Props {
  bracket: TournamentBracketDto;
}

defineProps<Props>();
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
          <div class="text-center font-bold text-xs uppercase tracking-wider text-on-surface-variant px-3 py-1.5 rounded-xl bg-surface-container-high/60">
            {{ round.roundName }}
          </div>

          <div class="flex flex-col justify-around gap-8 flex-1">
            <TournamentMatchCard
              v-for="match in round.matches"
              :key="match.id"
              :match="match"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
