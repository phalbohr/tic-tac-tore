<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMatchHistoryStore } from '../stores/useMatchHistoryStore'
import PlayerSearchOverlay from './PlayerSearchOverlay.vue'

const { t } = useI18n()
const store = useMatchHistoryStore()

const isPlayerSearchOpen = ref(false)

function selectMatchType(type: '1v1' | '2v2' | null) {
  store.setFilter('matchType', type)
}

function handleReset() {
  store.resetFilters()
}
</script>

<template>
  <div class="w-full flex flex-col gap-2">
    <div class="flex items-center gap-2 overflow-x-auto pb-1 no-scrollbar select-none" data-testid="match-filter-chips">
      <!-- All Chip -->
      <button
        type="button"
        class="px-4 py-2 rounded-xl text-xs font-bold shrink-0 transition-all cursor-pointer"
        :class="
          !store.filters.matchType && !store.filters.playerId
            ? 'bg-primary text-background shadow-md'
            : 'bg-surface-container-highest text-on-surface hover:bg-surface-container-highest/80'
        "
        @click="handleReset"
        data-testid="filter-all-chip"
      >
        {{ t('history.filters.all', 'All') }}
      </button>

      <!-- 1v1 Chip -->
      <button
        type="button"
        class="px-4 py-2 rounded-xl text-xs font-bold shrink-0 transition-all cursor-pointer"
        :class="
          store.filters.matchType === '1v1'
            ? 'bg-primary text-background shadow-md'
            : 'bg-surface-container-highest text-on-surface hover:bg-surface-container-highest/80'
        "
        @click="selectMatchType(store.filters.matchType === '1v1' ? null : '1v1')"
        data-testid="filter-1v1-chip"
      >
        1v1
      </button>

      <!-- 2v2 Chip -->
      <button
        type="button"
        class="px-4 py-2 rounded-xl text-xs font-bold shrink-0 transition-all cursor-pointer"
        :class="
          store.filters.matchType === '2v2'
            ? 'bg-primary text-background shadow-md'
            : 'bg-surface-container-highest text-on-surface hover:bg-surface-container-highest/80'
        "
        @click="selectMatchType(store.filters.matchType === '2v2' ? null : '2v2')"
        data-testid="filter-2v2-chip"
      >
        2v2
      </button>

      <!-- Player Filter Chip -->
      <button
        type="button"
        class="px-4 py-2 rounded-xl text-xs font-bold shrink-0 flex items-center gap-1.5 transition-all cursor-pointer"
        :class="
          store.filters.playerId
            ? 'bg-primary text-background shadow-md'
            : 'bg-surface-container-highest text-on-surface hover:bg-surface-container-highest/80'
        "
        @click="isPlayerSearchOpen = true"
        data-testid="filter-player-chip"
      >
        <span class="material-symbols-outlined text-sm">person_search</span>
        <span>{{ t('history.filters.player', 'Filter by Player') }}</span>
      </button>

      <!-- Reset CTA (only visible when filters active) -->
      <button
        v-if="store.hasFilters"
        type="button"
        class="px-3 py-2 rounded-xl text-xs font-semibold text-error hover:bg-error/10 shrink-0 transition-all flex items-center gap-1 cursor-pointer"
        @click="handleReset"
        data-testid="filter-clear-chip"
      >
        <span class="material-symbols-outlined text-xs">close</span>
        <span>{{ t('history.filters.clear', 'Clear Filters') }}</span>
      </button>
    </div>

    <!-- Player Search Overlay -->
    <PlayerSearchOverlay
      :is-open="isPlayerSearchOpen"
      @close="isPlayerSearchOpen = false"
    />
  </div>
</template>

<style scoped>
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
