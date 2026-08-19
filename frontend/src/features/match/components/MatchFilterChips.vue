<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMatchHistoryStore } from '../stores/useMatchHistoryStore'
import { useRuleConfigStore } from '@/stores/useRuleConfigStore'
import type { PlayerDto } from '../stores/matchDraftStore'
import PlayerSearchOverlay from './PlayerSearchOverlay.vue'

const { t } = useI18n()
const store = useMatchHistoryStore()
const ruleConfigStore = useRuleConfigStore()

const isPlayerSearchOpen = ref(false)
const selectedPlayerNickname = ref<string | null>(null)

onMounted(async () => {
  if (ruleConfigStore.presets.length === 0) {
    try {
      await ruleConfigStore.fetchPresets()
    } catch {
      // ignore preset fetch error if offline or unauthenticated
    }
  }
})

function selectMatchType(type: '1v1' | '2v2' | null) {
  store.setFilter('matchType', type)
}

function selectRulePreset(ruleId: string | null) {
  store.setFilter('ruleConfigId', store.filters.ruleConfigId === ruleId ? null : ruleId)
}

function handleSelectPlayer(player: PlayerDto) {
  selectedPlayerNickname.value = player.nickname
  store.setFilter('playerId', player.id)
  isPlayerSearchOpen.value = false
}

function handleClearPlayer() {
  selectedPlayerNickname.value = null
  store.setFilter('playerId', null)
}

function handleReset() {
  selectedPlayerNickname.value = null
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
          !store.hasFilters
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

      <!-- Rule Template Presets -->
      <template v-if="ruleConfigStore.presets && ruleConfigStore.presets.length > 0">
        <button
          v-for="preset in ruleConfigStore.presets"
          :key="preset.id || preset.name"
          type="button"
          class="px-4 py-2 rounded-xl text-xs font-bold shrink-0 transition-all cursor-pointer flex items-center gap-1"
          :class="
            store.filters.ruleConfigId === preset.id
              ? 'bg-primary text-background shadow-md'
              : 'bg-surface-container-highest text-on-surface hover:bg-surface-container-highest/80'
          "
          @click="selectRulePreset(preset.id ?? null)"
          :data-testid="`filter-rule-${preset.id || preset.name}`"
        >
          <span class="material-symbols-outlined text-xs">gavel</span>
          <span>{{ preset.name }}</span>
        </button>
      </template>

      <!-- Player Filter Chip -->
      <button
        v-if="!store.filters.playerId"
        type="button"
        class="px-4 py-2 rounded-xl text-xs font-bold shrink-0 flex items-center gap-1.5 transition-all cursor-pointer bg-surface-container-highest text-on-surface hover:bg-surface-container-highest/80"
        @click="isPlayerSearchOpen = true"
        data-testid="filter-player-chip"
      >
        <span class="material-symbols-outlined text-sm">person_search</span>
        <span>{{ t('history.filters.player', 'Filter by Player') }}</span>
      </button>

      <div
        v-else
        class="px-3 py-1.5 rounded-xl text-xs font-bold shrink-0 flex items-center gap-1.5 bg-primary text-background shadow-md"
        data-testid="filter-player-active-chip"
      >
        <span class="material-symbols-outlined text-sm">person</span>
        <span>{{ selectedPlayerNickname || t('history.filters.playerSelected', 'Player Filtered') }}</span>
        <button
          type="button"
          class="hover:opacity-70 cursor-pointer ml-1 flex items-center"
          @click.stop="handleClearPlayer"
          aria-label="Remove player filter"
        >
          <span class="material-symbols-outlined text-xs">close</span>
        </button>
      </div>

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
      :custom-select="true"
      @select="handleSelectPlayer"
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
