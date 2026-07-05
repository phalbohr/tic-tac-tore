<script setup lang="ts">
import { computed } from 'vue'
import { useMatchDraftStore } from '../stores/matchDraftStore'

defineOptions({
  name: 'PlayerSelection'
})

const store = useMatchDraftStore()
const maxPlayers = computed(() => store.matchType === '1v1' ? 2 : 4)

</script>

<template>
  <div class="flex flex-col gap-2 w-full mt-6">
    <h2 class="text-on-surface font-headline font-bold text-lg mb-2">Players</h2>
    <div 
      v-for="index in maxPlayers" 
      :key="index"
      class="player-slot h-16 flex items-center px-4 bg-surface-container-highest rounded-xl gap-4 mb-2"
    >
      <div class="w-10 h-10 rounded-full bg-surface-container-low flex items-center justify-center">
        <span class="text-on-surface-variant font-bold">{{ index }}</span>
      </div>
      <span class="text-on-surface flex-1">
        {{ store.selectedPlayers[index - 1] ? `Player ${store.selectedPlayers[index - 1]}` : 'Select Player' }}
      </span>
    </div>
  </div>
</template>
