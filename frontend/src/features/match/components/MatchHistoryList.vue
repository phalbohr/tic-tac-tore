<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { RouterLink } from 'vue-router'
import { useMatchHistoryStore } from '../stores/useMatchHistoryStore'
import MatchCard from './MatchCard.vue'

const { t } = useI18n()
const store = useMatchHistoryStore()

function handlePrevPage() {
  if (store.pagination.page > 0) {
    store.setPage(store.pagination.page - 1)
  }
}

function handleNextPage() {
  if (store.pagination.page < store.pagination.totalPages - 1) {
    store.setPage(store.pagination.page + 1)
  }
}

function enableDemoData() {
  store.enableDemoMode()
}
</script>

<template>
  <div class="match-history-list w-full flex flex-col gap-4" data-testid="match-history-list">
    <!-- Error State -->
    <div
      v-if="store.error"
      class="w-full p-4 rounded-2xl bg-error/10 border border-error/20 flex items-center justify-between text-xs text-error shadow-md"
      data-testid="history-error-state"
    >
      <div class="flex items-center gap-2">
        <span class="material-symbols-outlined text-base">error</span>
        <span>{{ store.error }}</span>
      </div>
      <button
        type="button"
        @click="store.fetchConfirmedHistory"
        class="px-3 py-1.5 rounded-xl bg-error text-background font-bold uppercase tracking-wider hover:opacity-90 transition-all cursor-pointer"
        data-testid="history-retry-btn"
      >
        {{ t('common.retry', 'Retry') }}
      </button>
    </div>

    <!-- Demo Mode Active Banner -->
    <div
      v-if="store.isDemoMode"
      class="w-full flex items-center justify-between p-3 rounded-xl bg-primary/10 border border-primary/20 text-xs text-primary shadow-sm"
      data-testid="demo-mode-banner"
    >
      <div class="flex items-center gap-2">
        <span class="material-symbols-outlined text-sm">info</span>
        <span>{{ t('stats.demoDataNotice', 'Viewing sample demo match history') }}</span>
      </div>
      <button
        type="button"
        @click="store.toggleDemoMode(false)"
        class="underline font-bold hover:opacity-80 cursor-pointer"
        data-testid="exit-demo-mode-btn"
      >
        {{ t('stats.exitDemo', 'Exit Demo') }}
      </button>
    </div>

    <!-- Loading State -->
    <div
      v-if="store.loading"
      class="w-full flex flex-col gap-3 py-6 items-center justify-center text-on-surface-variant"
    >
      <div
        class="w-8 h-8 rounded-full border-2 border-primary border-t-transparent animate-spin"
      ></div>
      <span class="text-xs font-medium">{{ t('common.loading', 'Loading...') }}</span>
    </div>

    <!-- Filtered Empty State -->
    <div
      v-else-if="store.confirmedMatches.length === 0 && store.hasFilters"
      class="w-full flex flex-col items-center justify-center p-8 bg-surface-container-low rounded-2xl text-center space-y-4 shadow-xl"
      data-testid="filtered-empty-state"
    >
      <div
        class="inline-flex items-center justify-center w-12 h-12 rounded-full bg-surface-container-highest text-on-surface-variant"
      >
        <span class="material-symbols-outlined text-2xl">search_off</span>
      </div>
      <div>
        <h3 class="font-headline text-base font-bold text-on-surface mb-1">
          {{ t('history.empty.filteredTitle', 'No matches found') }}
        </h3>
        <p class="text-xs text-on-surface-variant">
          {{ t('teamStats.noDataDescription', 'Try adjusting your filters to see more results.') }}
        </p>
      </div>
      <button
        type="button"
        class="px-5 py-2.5 rounded-xl bg-primary text-background font-headline font-bold text-xs uppercase tracking-wider shadow-lg hover:opacity-90 active:scale-95 transition-all cursor-pointer"
        @click="store.resetFilters"
        data-testid="clear-filters-cta"
      >
        {{ t('history.empty.filteredCta', 'Try removing filters') }}
      </button>
    </div>

    <!-- Unfiltered Confirmed 0 Matches Empty State -->
    <div
      v-else-if="store.confirmedMatches.length === 0"
      class="w-full flex flex-col items-center justify-center p-8 bg-surface-container-low rounded-2xl text-center space-y-6 shadow-2xl"
      data-testid="confirmed-empty-state"
    >
      <div
        class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-primary-container text-primary mb-1"
      >
        <span class="material-symbols-outlined text-3xl">sports_soccer</span>
      </div>

      <div class="space-y-1">
        <h2 class="font-headline text-lg font-bold text-on-surface">
          {{ t('history.empty.confirmedTitle', 'No confirmed matches yet') }}
        </h2>
        <p class="text-xs text-on-surface-variant leading-relaxed max-w-xs">
          {{
            t(
              'stats.emptyStateDescription',
              'Record your first match to start tracking your statistics and climb the leaderboard!',
            )
          }}
        </p>
      </div>

      <div class="flex flex-col gap-3 w-full max-w-xs pt-1">
        <RouterLink
          to="/matches/new"
          class="w-full py-3.5 rounded-xl bg-gradient-to-br from-primary to-primary-container text-background font-headline font-extrabold uppercase tracking-wider shadow-xl hover:opacity-90 active:scale-95 transition-all flex items-center justify-center gap-2 cursor-pointer text-xs"
          data-testid="record-match-cta"
        >
          <span class="material-symbols-outlined font-bold text-sm">add_circle</span>
          {{ t('history.empty.confirmedCta', 'Record your first match') }}
        </RouterLink>

        <button
          type="button"
          @click="enableDemoData"
          class="w-full py-3 rounded-xl bg-surface-container-highest hover:bg-surface-container-highest/80 text-on-surface font-headline font-bold uppercase tracking-wider text-xs transition-colors flex items-center justify-center gap-2 cursor-pointer"
          data-testid="toggle-demo-data-cta"
        >
          <span class="material-symbols-outlined text-sm">visibility</span>
          {{ t('stats.toggleDemoData', 'Toggle Demo Data') }}
        </button>
      </div>
    </div>

    <!-- Match Cards List -->
    <template v-else>
      <div class="flex flex-col gap-3 w-full">
        <MatchCard v-for="match in store.confirmedMatches" :key="match.id" :match="match" />
      </div>

      <!-- Pagination Controls -->
      <div
        v-if="store.pagination.totalPages > 1"
        class="flex items-center justify-between py-3 px-2 text-xs text-on-surface-variant"
        data-testid="pagination-controls"
      >
        <button
          type="button"
          :disabled="store.pagination.page === 0"
          @click="handlePrevPage"
          class="px-4 py-2 rounded-xl bg-surface-container-highest text-on-surface font-semibold disabled:opacity-40 disabled:cursor-not-allowed hover:bg-surface-container-highest/80 transition-all flex items-center gap-1 cursor-pointer"
          data-testid="pagination-prev"
        >
          <span class="material-symbols-outlined text-sm">chevron_left</span>
          <span>{{ t('history.pagination.previous', 'Previous') }}</span>
        </button>

        <span class="font-medium" data-testid="pagination-page-info">
          {{
            t('history.pagination.page', {
              current: store.pagination.page + 1,
              total: store.pagination.totalPages,
            })
          }}
        </span>

        <button
          type="button"
          :disabled="store.pagination.page >= store.pagination.totalPages - 1"
          @click="handleNextPage"
          class="px-4 py-2 rounded-xl bg-surface-container-highest text-on-surface font-semibold disabled:opacity-40 disabled:cursor-not-allowed hover:bg-surface-container-highest/80 transition-all flex items-center gap-1 cursor-pointer"
          data-testid="pagination-next"
        >
          <span>{{ t('history.pagination.next', 'Next') }}</span>
          <span class="material-symbols-outlined text-sm">chevron_right</span>
        </button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.match-history-list {
  border: none;
}
</style>
