<script setup lang="ts">
import { computed, watch, nextTick, ref } from 'vue'
import { useMatchDraftStore, type PlayerDto } from '../stores/matchDraftStore'
import AvatarBase from '@/components/AvatarBase.vue'

const props = withDefaults(
  defineProps<{
    isOpen: boolean
    customSelect?: boolean
  }>(),
  {
    customSelect: false
  }
)

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'select', player: PlayerDto): void
}>()

const store = useMatchDraftStore()
const searchInput = ref<HTMLInputElement | null>(null)

watch(() => props.isOpen, async (newVal) => {
  if (newVal) {
    store.openSearch()
    await nextTick()
    searchInput.value?.focus()
  } else {
    store.closeSearch()
  }
})

watch(() => store.searchQuery, (newQuery) => {
  store.searchPlayers(newQuery)
})

const displayResults = computed(() => {
  const frequent = store.frequentOpponents || []
  const others = store.searchResults || []
  const frequentIds = new Set(frequent.map(p => p.id))
  const otherResults = others.filter(p => !frequentIds.has(p.id))
  const combined = [...frequent, ...otherResults]
  combined.sort((a, b) => {
    if (frequentIds.has(a.id) && !frequentIds.has(b.id)) return -1
    if (!frequentIds.has(a.id) && frequentIds.has(b.id)) return 1
    return a.nickname.localeCompare(b.nickname)
  })
  return combined
})

function handleSelect(player: PlayerDto) {
  if (props.customSelect) {
    emit('select', player)
    store.closeSearch()
    emit('close')
    return
  }
  store.addPlayer(player.id)
  store.closeSearch()
  emit('close')
}

function handleBackdropClick() {
  store.closeSearch()
  emit('close')
}

function handleCancel() {
  store.closeSearch()
  emit('close')
}

function handleKeyDown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    handleCancel()
  }
}
</script>

<template>
  <div
    v-if="isOpen"
    class="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/60 backdrop-blur-sm p-4 animate-fade-in"
    @click.self="handleBackdropClick"
    @keydown.escape="handleKeyDown"
    data-testid="player-search-overlay"
  >
    <div class="w-full max-w-md bg-surface-container-low rounded-2xl p-6 shadow-2xl space-y-4 flex flex-col max-h-[90vh]">
      <h2 class="font-headline text-lg font-bold text-on-surface">Find Player</h2>

      <input
        ref="searchInput"
        v-model="store.searchQuery"
        type="text"
        placeholder="Search by nickname..."
        class="w-full bg-surface-container-highest text-on-surface rounded-xl p-3 border-none focus:outline-none focus:ring-2 focus:ring-primary"
        data-testid="player-search-input"
      />

      <div v-if="store.searchLoading" class="text-center text-on-surface-variant py-4" data-testid="search-loading">
        Searching...
      </div>

      <div v-else-if="store.searchError" class="text-center" data-testid="search-error">
        <p class="text-error">{{ store.searchError }}</p>
      </div>

      <div v-else-if="displayResults.length === 0 && store.searchQuery" class="text-center text-on-surface-variant py-4" data-testid="no-results">
        No players found
      </div>

      <div v-else class="overflow-y-auto flex-grow space-y-2">
        <button
          v-for="player in displayResults"
          :key="player.id"
          class="w-full flex items-center gap-3 p-3 rounded-xl bg-surface-container-highest hover:bg-surface-container-highest/80 active:scale-[0.98] transition-all"
          @click="handleSelect(player)"
          data-testid="search-result-row"
        >
          <div class="w-10 h-10 rounded-full bg-surface-container-low flex items-center justify-center overflow-hidden">
            <AvatarBase :avatar="player.avatar" :name="player.nickname" shape="circle" />
          </div>
          <span class="text-on-surface flex-1 text-left">{{ player.nickname }}</span>
        </button>
      </div>

      <button
        type="button"
        class="w-full py-3 rounded-xl bg-surface-container-highest text-on-surface font-headline font-bold text-sm hover:bg-surface-container-highest/80 active:scale-95 transition-colors"
        @click="handleCancel"
      >
        Cancel
      </button>
    </div>
  </div>
</template>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.15s ease-out;
}
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>
