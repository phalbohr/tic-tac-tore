<script setup lang="ts">
import { ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import type {
  TournamentBracketDto,
  RoundMatchesDto,
  TournamentMatchDto,
} from '@/features/tournament/types/tournament';
import TournamentMatchCard from './TournamentMatchCard.vue';

interface Props {
  bracket: TournamentBracketDto;
  currentUserId?: string | null;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  (e: 'start-match', matchId: string): void;
}>();

const { t, te } = useI18n();

const activeFilter = ref<'all' | 'my' | 'available'>('all');

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

const allRoundsLabel = computed(() => {
  if (te('tournament.match.allRounds')) return t('tournament.match.allRounds');
  return 'All Rounds';
});

const myMatchesLabel = computed(() => {
  if (te('tournament.match.myMatches')) return t('tournament.match.myMatches');
  return 'My Matches';
});

const availableMatchesLabel = computed(() => {
  if (te('tournament.match.available')) return t('tournament.match.available');
  return 'Available to Play';
});

function isUserInMatch(match: TournamentMatchDto): boolean {
  if (!props.currentUserId) return true;
  const uid = props.currentUserId;
  return (
    match.participant1?.playerId === uid ||
    match.participant1Partner?.playerId === uid ||
    match.participant2?.playerId === uid ||
    match.participant2Partner?.playerId === uid
  );
}

function getFilteredMatches(matches: TournamentMatchDto[]): TournamentMatchDto[] {
  if (activeFilter.value === 'my') {
    return matches.filter((m) => isUserInMatch(m));
  }
  if (activeFilter.value === 'available') {
    return matches.filter((m) => m.status === 'READY' && !m.isOpponentBusy);
  }
  return matches;
}

const displayedRounds = computed(() => {
  if (!props.bracket?.rounds) return [];
  return props.bracket.rounds
    .map((r) => ({
      ...r,
      filteredMatches: getFilteredMatches(r.matches),
    }))
    .filter((r) => r.filteredMatches.length > 0 || activeFilter.value === 'all');
});
</script>

<template>
  <div data-testid="tournament-schedule-view" class="w-full space-y-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h2 class="text-xl font-bold text-on-surface">
          {{ bracket.tournamentName }}
        </h2>
        <p class="text-xs text-on-surface-variant">
          Championship Schedule • {{ bracket.totalRounds }} Rounds
        </p>
      </div>

      <!-- Quick Filter Chips -->
      <div class="flex items-center gap-2">
        <button
          type="button"
          data-testid="filter-all"
          data-test="filter-all"
          class="px-3 py-1.5 rounded-xl text-xs font-semibold transition-colors"
          :class="
            activeFilter === 'all'
              ? 'bg-primary text-on-primary'
              : 'bg-surface-container-low text-on-surface hover:bg-surface-container'
          "
          @click="activeFilter = 'all'"
        >
          {{ allRoundsLabel }}
        </button>
        <button
          type="button"
          data-testid="filter-my"
          data-test="filter-my"
          class="px-3 py-1.5 rounded-xl text-xs font-semibold transition-colors"
          :class="
            activeFilter === 'my'
              ? 'bg-primary text-on-primary'
              : 'bg-surface-container-low text-on-surface hover:bg-surface-container'
          "
          @click="activeFilter = 'my'"
        >
          {{ myMatchesLabel }}
        </button>
        <button
          type="button"
          data-testid="filter-available"
          data-test="filter-available"
          class="px-3 py-1.5 rounded-xl text-xs font-semibold transition-colors"
          :class="
            activeFilter === 'available'
              ? 'bg-primary text-on-primary'
              : 'bg-surface-container-low text-on-surface hover:bg-surface-container'
          "
          @click="activeFilter = 'available'"
        >
          {{ availableMatchesLabel }}
        </button>
      </div>
    </div>

    <!-- Displayed Rounds -->
    <div v-for="round in displayedRounds" :key="round.round" class="space-y-3">
      <h3 class="text-xs font-bold uppercase tracking-wider text-on-surface-variant">
        {{ getRoundDisplayName(round) }}
      </h3>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <TournamentMatchCard
          v-for="match in round.filteredMatches"
          :key="match.id"
          :match="match"
          :current-user-id="currentUserId"
          @start-match="emit('start-match', $event)"
        />
        <div
          v-if="round.filteredMatches.length === 0"
          data-testid="no-matches-found"
          class="col-span-full text-center py-4 text-on-surface-variant text-xs"
        >
          No matches found for current filter.
        </div>
      </div>
    </div>
  </div>
</template>
