<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useStatsStore } from '../stores/useStatsStore'
import type { TimePeriod, MatchTypeFilter } from '@/services/statisticsService'
import AvatarBase from '@/components/AvatarBase.vue'
import EmptyStateCTA from './EmptyStateCTA.vue'

const props = defineProps<{
  opponentId?: string
}>()

const route = useRoute()
const { t } = useI18n()
const statsStore = useStatsStore()

const currentOpponentId = ref<string>(
  props.opponentId || (route.query.opponentId as string) || 'opp-user-456'
)
const selectedPeriod = ref<TimePeriod>('ALL_TIME')
const selectedMatchType = ref<string>('')
const selectedRuleConfigId = ref<string>('')

const opponentNickname = computed(() => {
  return statsStore.h2hStats?.opponent?.nickname || 'Opponent'
})

const opponentAvatar = computed(() => {
  return statsStore.h2hStats?.opponent?.avatarUrl || null
})

const totalMatchCount = computed(() => {
  if (!statsStore.h2hStats) return 0
  const withMatches = statsStore.h2hStats.matches?.with?.matches ?? 0
  const vsMatches = statsStore.h2hStats.matches?.vs?.matches ?? 0
  return withMatches + vsMatches
})

async function loadH2HData() {
  if (!currentOpponentId.value) return
  await statsStore.fetchH2HStats(currentOpponentId.value, {
    period: selectedPeriod.value || undefined,
    matchType: (selectedMatchType.value as MatchTypeFilter) || undefined,
    ruleConfigId: selectedRuleConfigId.value || undefined,
  })
}

onMounted(() => {
  if (route.query.opponentId) {
    currentOpponentId.value = route.query.opponentId as string
  }
  loadH2HData()
})

watch(
  () => route.query.opponentId,
  (newOppId) => {
    if (newOppId && typeof newOppId === 'string') {
      currentOpponentId.value = newOppId
      loadH2HData()
    }
  }
)

watch([selectedPeriod, selectedMatchType, selectedRuleConfigId], () => {
  loadH2HData()
})
</script>

<template>
  <div class="ch-h2h w-full max-w-4xl mx-auto px-4 py-6 flex flex-col gap-6">
    <!-- Header with Opponent Profile -->
    <div class="ch-h2h__header bg-surface-container-low p-6 rounded-2xl flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 shadow-xs">
      <div class="flex items-center gap-4">
        <AvatarBase :avatar="opponentAvatar" :nickname="opponentNickname" size="lg" />
        <div class="flex flex-col">
          <span class="text-xs text-primary font-bold uppercase tracking-wider">{{ t('h2h.title', 'Head-to-Head') }}</span>
          <h1 class="text-2xl sm:text-3xl font-headline font-bold text-on-surface">
            {{ opponentNickname }}
          </h1>
          <span class="text-xs text-on-surface-variant">
            {{ t('h2h.subtitle', 'Historical matchup breakdown across matches, games & positions') }}
          </span>
        </div>
      </div>

      <!-- Demo Badge -->
      <div v-if="statsStore.isDemoModeEnabled" class="flex items-center">
        <span class="text-xs text-orange-400 font-headline uppercase tracking-widest font-bold bg-orange-400/10 px-3 py-1 rounded-full">
          {{ t('h2h.demoData', 'Demo Data') }}
        </span>
      </div>
    </div>

    <!-- Filters Bar -->
    <div class="ch-filter-bar bg-surface-container-low p-4 rounded-2xl flex flex-wrap gap-4 items-center justify-between shadow-xs">
      <div class="flex flex-wrap gap-4 items-center">
        <!-- Period Filter -->
        <div class="flex items-center gap-2">
          <label for="h2h-period-select" class="text-xs font-semibold text-on-surface-variant uppercase tracking-wider">
            {{ t('teamStats.filterPeriod', 'Period') }}:
          </label>
          <select
            id="h2h-period-select"
            v-model="selectedPeriod"
            data-testid="stats-period-select"
            class="bg-surface-container-highest text-on-surface text-sm rounded-xl px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-primary/40 cursor-pointer"
          >
            <option value="ALL_TIME">{{ t('h2h.allTime', 'All Time') }}</option>
            <option value="WEEKLY">{{ t('h2h.weekly', 'Weekly') }}</option>
            <option value="MONTHLY">{{ t('h2h.monthly', 'Monthly') }}</option>
            <option value="YEARLY">{{ t('h2h.yearly', 'Yearly') }}</option>
          </select>
        </div>

        <!-- Match Type Filter -->
        <div class="flex items-center gap-2">
          <label for="h2h-match-type-select" class="text-xs font-semibold text-on-surface-variant uppercase tracking-wider">
            {{ t('h2h.category', 'Type') }}:
          </label>
          <select
            id="h2h-match-type-select"
            v-model="selectedMatchType"
            data-testid="stats-match-type-select"
            class="bg-surface-container-highest text-on-surface text-sm rounded-xl px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-primary/40 cursor-pointer"
          >
            <option value="">{{ t('h2h.allTypes', 'All Types') }}</option>
            <option value="1v1">{{ t('h2h.type1v1', '1v1') }}</option>
            <option value="2v2">{{ t('h2h.type2v2', '2v2') }}</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Loading Skeleton -->
    <div v-if="statsStore.isH2HLoading" class="flex flex-col gap-4">
      <div v-for="n in 3" :key="n" class="h-36 bg-surface-container-low animate-pulse rounded-2xl"></div>
    </div>

    <!-- Empty State CTA when 0 matches -->
    <EmptyStateCTA
      v-else-if="totalMatchCount === 0"
      :opponent-id="currentOpponentId"
      :opponent-nickname="opponentNickname"
    />

    <!-- Matrix Tables -->
    <div v-else-if="statsStore.h2hStats" class="flex flex-col gap-6">
      <!-- 1. Matches Table -->
      <section class="ch-table-card bg-surface-container-low p-5 rounded-2xl shadow-xs flex flex-col gap-3">
        <div class="flex items-center justify-between">
          <h2 class="text-lg font-headline font-bold text-on-surface">
            {{ t('h2h.matchesTitle', 'Matches') }}
          </h2>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="text-xs text-on-surface-variant uppercase tracking-wider">
                <th class="py-2.5 px-3 bg-surface-container-highest/40 rounded-l-xl">{{ t('h2h.category', 'Category') }}</th>
                <th class="py-2.5 px-3 bg-surface-container-highest/40 text-center">{{ t('h2h.matches', 'Matches') }}</th>
                <th class="py-2.5 px-3 bg-surface-container-highest/40 text-center text-green-400">{{ t('h2h.wins', 'Wins') }}</th>
                <th class="py-2.5 px-3 bg-surface-container-highest/40 text-center text-red-400">{{ t('h2h.losses', 'Losses') }}</th>
                <th class="py-2.5 px-3 bg-surface-container-highest/40 text-center">{{ t('h2h.draws', 'Draws') }}</th>
                <th class="py-2.5 px-3 bg-surface-container-highest/40 text-right rounded-r-xl">{{ t('h2h.winRate', 'Win Rate') }}</th>
              </tr>
            </thead>
            <tbody class="text-sm font-medium text-on-surface divide-y divide-transparent">
              <tr class="hover:bg-surface-container-highest/30 transition-colors">
                <td class="py-3 px-3 font-semibold text-primary">{{ t('h2h.with', 'With') }}</td>
                <td class="py-3 px-3 text-center">{{ statsStore.h2hStats.matches.with.matches }}</td>
                <td class="py-3 px-3 text-center text-green-400 font-bold">{{ statsStore.h2hStats.matches.with.wins }}</td>
                <td class="py-3 px-3 text-center text-red-400 font-bold">{{ statsStore.h2hStats.matches.with.losses }}</td>
                <td class="py-3 px-3 text-center">{{ statsStore.h2hStats.matches.with.draws }}</td>
                <td class="py-3 px-3 text-right font-headline font-bold text-primary">
                  {{ (statsStore.h2hStats.matches.with.winRate ?? 0).toFixed(1) }}%
                </td>
              </tr>
              <tr class="hover:bg-surface-container-highest/30 transition-colors">
                <td class="py-3 px-3 font-semibold text-secondary">{{ t('h2h.vs', 'Vs') }}</td>
                <td class="py-3 px-3 text-center">{{ statsStore.h2hStats.matches.vs.matches }}</td>
                <td class="py-3 px-3 text-center text-green-400 font-bold">{{ statsStore.h2hStats.matches.vs.wins }}</td>
                <td class="py-3 px-3 text-center text-red-400 font-bold">{{ statsStore.h2hStats.matches.vs.losses }}</td>
                <td class="py-3 px-3 text-center">{{ statsStore.h2hStats.matches.vs.draws }}</td>
                <td class="py-3 px-3 text-right font-headline font-bold text-secondary">
                  {{ (statsStore.h2hStats.matches.vs.winRate ?? 0).toFixed(1) }}%
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- 2. Games Table -->
      <section class="ch-table-card bg-surface-container-low p-5 rounded-2xl shadow-xs flex flex-col gap-3">
        <div class="flex items-center justify-between">
          <h2 class="text-lg font-headline font-bold text-on-surface">
            {{ t('h2h.gamesTitle', 'Games') }}
          </h2>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="text-xs text-on-surface-variant uppercase tracking-wider">
                <th class="py-2.5 px-3 bg-surface-container-highest/40 rounded-l-xl">{{ t('h2h.category', 'Category') }}</th>
                <th class="py-2.5 px-3 bg-surface-container-highest/40 text-center">{{ t('h2h.totalGames', 'Total Games') }}</th>
                <th class="py-2.5 px-3 bg-surface-container-highest/40 text-center text-green-400">{{ t('h2h.gamesWon', 'Won') }}</th>
                <th class="py-2.5 px-3 bg-surface-container-highest/40 text-center text-red-400">{{ t('h2h.gamesLost', 'Lost') }}</th>
                <th class="py-2.5 px-3 bg-surface-container-highest/40 text-right rounded-r-xl">{{ t('h2h.winRate', 'Win Rate') }}</th>
              </tr>
            </thead>
            <tbody class="text-sm font-medium text-on-surface divide-y divide-transparent">
              <tr class="hover:bg-surface-container-highest/30 transition-colors">
                <td class="py-3 px-3 font-semibold text-primary">{{ t('h2h.with', 'With') }}</td>
                <td class="py-3 px-3 text-center">{{ statsStore.h2hStats.games.with.totalGames }}</td>
                <td class="py-3 px-3 text-center text-green-400 font-bold">{{ statsStore.h2hStats.games.with.gamesWon }}</td>
                <td class="py-3 px-3 text-center text-red-400 font-bold">{{ statsStore.h2hStats.games.with.gamesLost }}</td>
                <td class="py-3 px-3 text-right font-headline font-bold text-primary">
                  {{ (statsStore.h2hStats.games.with.winRate ?? 0).toFixed(1) }}%
                </td>
              </tr>
              <tr class="hover:bg-surface-container-highest/30 transition-colors">
                <td class="py-3 px-3 font-semibold text-secondary">{{ t('h2h.vs', 'Vs') }}</td>
                <td class="py-3 px-3 text-center">{{ statsStore.h2hStats.games.vs.totalGames }}</td>
                <td class="py-3 px-3 text-center text-green-400 font-bold">{{ statsStore.h2hStats.games.vs.gamesWon }}</td>
                <td class="py-3 px-3 text-center text-red-400 font-bold">{{ statsStore.h2hStats.games.vs.gamesLost }}</td>
                <td class="py-3 px-3 text-right font-headline font-bold text-secondary">
                  {{ (statsStore.h2hStats.games.vs.winRate ?? 0).toFixed(1) }}%
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <!-- 3. Goals Table (Positional Cross-Tabulation Matrix) -->
      <section class="ch-table-card bg-surface-container-low p-5 rounded-2xl shadow-xs flex flex-col gap-3">
        <div class="flex items-center justify-between">
          <h2 class="text-lg font-headline font-bold text-on-surface">
            {{ t('h2h.goalsTitle', 'Goals') }}
          </h2>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="text-xs text-on-surface-variant uppercase tracking-wider">
                <th class="py-2.5 px-3 bg-surface-container-highest/40 rounded-l-xl">{{ t('h2h.matchup', 'Matchup (You vs Opponent)') }}</th>
                <th class="py-2.5 px-3 bg-surface-container-highest/40 text-center text-green-400">{{ t('h2h.scored', 'Scored') }}</th>
                <th class="py-2.5 px-3 bg-surface-container-highest/40 text-center text-red-400">{{ t('h2h.conceded', 'Conceded') }}</th>
                <th class="py-2.5 px-3 bg-surface-container-highest/40 text-right rounded-r-xl">{{ t('h2h.difference', 'Diff') }}</th>
              </tr>
            </thead>
            <tbody class="text-sm font-medium text-on-surface divide-y divide-transparent">
              <!-- Attacker vs Defender -->
              <tr class="hover:bg-surface-container-highest/30 transition-colors">
                <td class="py-3 px-3 font-semibold text-on-surface">{{ t('h2h.attackerVsDefender', 'Attacker vs Defender') }}</td>
                <td class="py-3 px-3 text-center text-green-400 font-bold">{{ statsStore.h2hStats.goals.attackerVsDefender.scored }}</td>
                <td class="py-3 px-3 text-center text-red-400 font-bold">{{ statsStore.h2hStats.goals.attackerVsDefender.conceded }}</td>
                <td class="py-3 px-3 text-right font-headline font-bold" :class="statsStore.h2hStats.goals.attackerVsDefender.scored - statsStore.h2hStats.goals.attackerVsDefender.conceded >= 0 ? 'text-green-400' : 'text-red-400'">
                  {{ statsStore.h2hStats.goals.attackerVsDefender.scored - statsStore.h2hStats.goals.attackerVsDefender.conceded > 0 ? '+' : '' }}{{ statsStore.h2hStats.goals.attackerVsDefender.scored - statsStore.h2hStats.goals.attackerVsDefender.conceded }}
                </td>
              </tr>
              <!-- Attacker vs Attacker -->
              <tr class="hover:bg-surface-container-highest/30 transition-colors">
                <td class="py-3 px-3 font-semibold text-on-surface">{{ t('h2h.attackerVsAttacker', 'Attacker vs Attacker') }}</td>
                <td class="py-3 px-3 text-center text-green-400 font-bold">{{ statsStore.h2hStats.goals.attackerVsAttacker.scored }}</td>
                <td class="py-3 px-3 text-center text-red-400 font-bold">{{ statsStore.h2hStats.goals.attackerVsAttacker.conceded }}</td>
                <td class="py-3 px-3 text-right font-headline font-bold" :class="statsStore.h2hStats.goals.attackerVsAttacker.scored - statsStore.h2hStats.goals.attackerVsAttacker.conceded >= 0 ? 'text-green-400' : 'text-red-400'">
                  {{ statsStore.h2hStats.goals.attackerVsAttacker.scored - statsStore.h2hStats.goals.attackerVsAttacker.conceded > 0 ? '+' : '' }}{{ statsStore.h2hStats.goals.attackerVsAttacker.scored - statsStore.h2hStats.goals.attackerVsAttacker.conceded }}
                </td>
              </tr>
              <!-- Defender vs Attacker -->
              <tr class="hover:bg-surface-container-highest/30 transition-colors">
                <td class="py-3 px-3 font-semibold text-on-surface">{{ t('h2h.defenderVsAttacker', 'Defender vs Attacker') }}</td>
                <td class="py-3 px-3 text-center text-green-400 font-bold">{{ statsStore.h2hStats.goals.defenderVsAttacker.scored }}</td>
                <td class="py-3 px-3 text-center text-red-400 font-bold">{{ statsStore.h2hStats.goals.defenderVsAttacker.conceded }}</td>
                <td class="py-3 px-3 text-right font-headline font-bold" :class="statsStore.h2hStats.goals.defenderVsAttacker.scored - statsStore.h2hStats.goals.defenderVsAttacker.conceded >= 0 ? 'text-green-400' : 'text-red-400'">
                  {{ statsStore.h2hStats.goals.defenderVsAttacker.scored - statsStore.h2hStats.goals.defenderVsAttacker.conceded > 0 ? '+' : '' }}{{ statsStore.h2hStats.goals.defenderVsAttacker.scored - statsStore.h2hStats.goals.defenderVsAttacker.conceded }}
                </td>
              </tr>
              <!-- Defender vs Defender -->
              <tr class="hover:bg-surface-container-highest/30 transition-colors">
                <td class="py-3 px-3 font-semibold text-on-surface">{{ t('h2h.defenderVsDefender', 'Defender vs Defender') }}</td>
                <td class="py-3 px-3 text-center text-green-400 font-bold">{{ statsStore.h2hStats.goals.defenderVsDefender.scored }}</td>
                <td class="py-3 px-3 text-center text-red-400 font-bold">{{ statsStore.h2hStats.goals.defenderVsDefender.conceded }}</td>
                <td class="py-3 px-3 text-right font-headline font-bold" :class="statsStore.h2hStats.goals.defenderVsDefender.scored - statsStore.h2hStats.goals.defenderVsDefender.conceded >= 0 ? 'text-green-400' : 'text-red-400'">
                  {{ statsStore.h2hStats.goals.defenderVsDefender.scored - statsStore.h2hStats.goals.defenderVsDefender.conceded > 0 ? '+' : '' }}{{ statsStore.h2hStats.goals.defenderVsDefender.scored - statsStore.h2hStats.goals.defenderVsDefender.conceded }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped lang="scss">
.ch-h2h {
  user-select: none;
}
</style>
