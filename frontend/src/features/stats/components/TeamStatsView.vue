<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useStatsStore } from '../stores/useStatsStore'
import type { TimePeriod } from '@/services/statisticsService'
import AvatarBase from '@/components/AvatarBase.vue'

const { t } = useI18n()
const statsStore = useStatsStore()

const selectedPeriod = ref<TimePeriod>('ALL_TIME')
const minMatches = ref<number>(1)
const currentPage = ref<number>(0)
const pageSize = ref<number>(10)
const playerIdFilter = ref<string>('')

async function loadData() {
  await statsStore.fetchTeamPairStats({
    period: selectedPeriod.value,
    minMatches: minMatches.value,
    page: currentPage.value,
    size: pageSize.value,
    playerId: playerIdFilter.value.trim() || undefined
  })
}

onMounted(() => {
  loadData()
})

watch([selectedPeriod, minMatches], () => {
  currentPage.value = 0
  loadData()
})

function prevPage() {
  if (currentPage.value > 0) {
    currentPage.value--
    loadData()
  }
}

function nextPage() {
  const totalPages = statsStore.teamPairPage?.totalPages || 0
  if (currentPage.value < totalPages - 1) {
    currentPage.value++
    loadData()
  }
}
</script>

<template>
  <div class="ch-team-stats w-full max-w-4xl mx-auto px-4 py-6 flex flex-col gap-6">
    <!-- Header -->
    <div class="ch-team-stats__header flex flex-col gap-1">
      <h1 class="text-2xl sm:text-3xl font-headline font-bold text-on-surface">
        {{ t('teamStats.title', 'Team Statistics') }}
      </h1>
      <p class="text-sm text-on-surface-variant">
        {{ t('teamStats.subtitle', 'Analyze your synergy with different teammates across positions') }}
      </p>
    </div>

    <!-- Filters Bar (No-Line Rule: tonal surfaces) -->
    <div class="ch-filter-bar bg-surface-container-low p-4 rounded-2xl flex flex-wrap gap-4 items-center justify-between shadow-xs">
      <div class="flex flex-wrap gap-4 items-center">
        <!-- Period Filter -->
        <div class="flex items-center gap-2">
          <label for="period-select" class="text-xs font-semibold text-on-surface-variant uppercase tracking-wider">
            {{ t('teamStats.filterPeriod', 'Period') }}:
          </label>
          <select
            id="period-select"
            v-model="selectedPeriod"
            data-testid="stats-period-select"
            class="bg-surface-container-highest text-on-surface text-sm rounded-xl px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-primary/40 cursor-pointer"
          >
            <option value="ALL_TIME">{{ t('teamStats.allTime', 'All Time') }}</option>
            <option value="LAST_WEEK">{{ t('teamStats.lastWeek', 'Last Week') }}</option>
            <option value="LAST_MONTH">{{ t('teamStats.lastMonth', 'Last Month') }}</option>
            <option value="LAST_YEAR">{{ t('teamStats.lastYear', 'Last Year') }}</option>
          </select>
        </div>

        <!-- Min Matches Filter -->
        <div class="flex items-center gap-2">
          <label for="min-matches-select" class="text-xs font-semibold text-on-surface-variant uppercase tracking-wider">
            {{ t('teamStats.filterMinMatches', 'Min Matches') }}:
          </label>
          <select
            id="min-matches-select"
            v-model.number="minMatches"
            class="bg-surface-container-highest text-on-surface text-sm rounded-xl px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-primary/40 cursor-pointer"
          >
            <option :value="1">1+</option>
            <option :value="3">3+</option>
            <option :value="5">5+</option>
            <option :value="10">10+</option>
          </select>
        </div>
      </div>

      <!-- Demo mode indicator -->
      <div v-if="statsStore.isDemoModeEnabled" class="flex items-center gap-2">
        <span class="text-xs text-orange-400 font-headline uppercase tracking-widest font-bold bg-orange-400/10 px-2.5 py-1 rounded-full">
          Demo Data
        </span>
      </div>
    </div>

    <!-- Loading Skeleton -->
    <div v-if="statsStore.isTeamPairsLoading" class="flex flex-col gap-3">
      <div v-for="n in 3" :key="n" class="h-24 bg-surface-container-low animate-pulse rounded-2xl"></div>
    </div>

    <!-- Empty State -->
    <div
      v-else-if="!statsStore.teamPairStats || statsStore.teamPairStats.length === 0"
      class="ch-empty-state bg-surface-container-low p-8 rounded-2xl text-center flex flex-col items-center gap-3"
    >
      <div class="w-12 h-12 rounded-full bg-surface-container-highest flex items-center justify-center text-on-surface-variant text-xl font-bold">
        👥
      </div>
      <h3 class="text-lg font-bold text-on-surface">
        {{ t('teamStats.noData', 'No team pair statistics found') }}
      </h3>
      <p class="text-sm text-on-surface-variant max-w-md">
        Try adjusting your filters or record more 2v2 matches to see synergy insights.
      </p>
    </div>

    <!-- Team Pairs List -->
    <div v-else class="flex flex-col gap-3">
      <div
        v-for="pair in statsStore.teamPairStats"
        :key="`${pair.attackerId}-${pair.defenderId}`"
        class="ch-pair-card bg-surface-container-low hover:bg-surface-container transition-colors duration-200 p-4 rounded-2xl flex flex-col sm:flex-row sm:items-center justify-between gap-4"
      >
        <!-- Team Members & Positions -->
        <div class="flex items-center gap-4 flex-1">
          <!-- Attacker & Defender Avatars / Names -->
          <div class="flex items-center -space-x-3">
            <AvatarBase :avatar="pair.attackerAvatar" :nickname="pair.attackerName" size="md" />
            <AvatarBase :avatar="pair.defenderAvatar" :nickname="pair.defenderName" size="md" />
          </div>

          <div class="flex flex-col gap-0.5">
            <div class="flex flex-wrap items-center gap-1.5 text-sm font-bold text-on-surface">
              <span class="text-primary">{{ pair.attackerName }} (Attacker)</span>
              <span class="text-on-surface-variant font-normal">&amp;</span>
              <span class="text-secondary">{{ pair.defenderName }} (Defender)</span>
            </div>
            <div class="text-xs text-on-surface-variant">
              {{ pair.matches }} {{ t('teamStats.matches', 'Matches') }} ({{ pair.wins }}W - {{ pair.losses }}L)
            </div>
          </div>
        </div>

        <!-- Win Rate & Stats Progress -->
        <div class="flex items-center gap-4 justify-between sm:justify-end">
          <div class="flex flex-col items-end">
            <span class="text-xl font-headline font-bold text-primary">
              {{ pair.winRate }}%
            </span>
            <span class="text-[11px] text-on-surface-variant uppercase tracking-wider">
              {{ t('teamStats.winRate', 'Win Rate') }}
            </span>
          </div>

          <!-- Mini Progress Bar (No-Line rule) -->
          <div class="w-16 sm:w-24 h-2.5 bg-surface-container-highest rounded-full overflow-hidden">
            <div
              class="h-full bg-primary rounded-full transition-all duration-300"
              :style="{ width: `${Math.min(100, Math.max(0, pair.winRate))}%` }"
            ></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination Controls -->
    <div
      v-if="statsStore.teamPairPage && statsStore.teamPairPage.totalPages > 1"
      class="ch-pagination flex items-center justify-between pt-2"
    >
      <button
        type="button"
        :disabled="currentPage === 0"
        @click="prevPage"
        class="px-4 py-2 bg-surface-container-low hover:bg-surface-container-highest disabled:opacity-40 disabled:cursor-not-allowed text-on-surface text-sm font-semibold rounded-xl transition-colors"
      >
        {{ t('teamStats.previous', 'Previous') }}
      </button>

      <span class="text-xs text-on-surface-variant font-medium">
        {{ t('teamStats.page', { current: currentPage + 1, total: statsStore.teamPairPage.totalPages }) }}
      </span>

      <button
        type="button"
        :disabled="currentPage >= statsStore.teamPairPage.totalPages - 1"
        @click="nextPage"
        class="px-4 py-2 bg-surface-container-low hover:bg-surface-container-highest disabled:opacity-40 disabled:cursor-not-allowed text-on-surface text-sm font-semibold rounded-xl transition-colors"
      >
        {{ t('teamStats.next', 'Next') }}
      </button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.ch-team-stats {
  user-select: none;
}
</style>
