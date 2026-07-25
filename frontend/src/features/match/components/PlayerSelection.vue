<script setup lang="ts">
import { computed } from 'vue'
import { useMatchDraftStore, MatchType, type PlayerDto } from '../stores/matchDraftStore'

defineOptions({
  name: 'PlayerSelection'
})

const store = useMatchDraftStore()
const maxPlayers = computed(() => store.matchType === MatchType.ONE_VS_ONE ? 2 : 4)

function getPlayer(id?: string) {
  if (!id) return undefined
  return store.frequentOpponents.find((p: PlayerDto) => p.id === id)
}

</script>

<template>
  <div class="flex flex-col gap-2 w-full mt-6">
    <h2 class="text-on-surface font-headline font-bold text-lg mb-2">Players</h2>
    <div 
      v-for="index in maxPlayers" 
      :key="index"
      class="player-slot h-16 flex items-center px-4 bg-surface-container-highest rounded-xl gap-4 mb-2"
    >
      <div class="w-10 h-10 rounded-full bg-surface-container-low flex items-center justify-center overflow-hidden">
        <span v-if="!store.selectedPlayers[index - 1]" class="text-on-surface-variant font-bold">{{ index }}</span>
        <img v-else-if="getPlayer(store.selectedPlayers[index - 1])?.avatar" :src="getPlayer(store.selectedPlayers[index - 1])?.avatar" class="w-full h-full object-cover" />
        <span v-else class="text-on-surface-variant font-bold">{{ getPlayer(store.selectedPlayers[index - 1])?.nickname?.charAt(0)?.toUpperCase() || '?' }}</span>
      </div>
      <span class="text-on-surface flex-1">
        {{ store.selectedPlayers[index - 1] ? (getPlayer(store.selectedPlayers[index - 1])?.nickname || `Player ${store.selectedPlayers[index - 1]}`) : 'Select Player' }}
      </span>
      <button v-if="store.selectedPlayers[index - 1]" @click="store.removePlayer(store.selectedPlayers[index - 1]!)" class="text-error font-bold px-2">X</button>
    </div>
    
    <div v-if="store.frequentOpponents.length > 0 && store.selectedPlayers.length < maxPlayers" class="mt-4">
      <h3 class="text-on-surface-variant font-bold text-sm mb-2">Frequent Opponents</h3>
      <div class="flex gap-2 overflow-x-auto pb-2">
        <button 
          v-for="opponent in store.frequentOpponents" 
          :key="opponent.id"
          @click="store.addPlayer(opponent.id)"
          :disabled="store.selectedPlayers.includes(opponent.id)"
          class="flex flex-col items-center gap-1 min-w-[72px] opacity-100 disabled:opacity-50"
        >
          <div class="w-12 h-12 rounded-full bg-surface-container-highest overflow-hidden">
             <img v-if="opponent.avatar" :src="opponent.avatar" class="w-full h-full object-cover" />
          </div>
          <span class="text-xs text-on-surface truncate w-full text-center">{{ opponent.nickname }}</span>
        </button>
      </div>
    </div>
  </div>
</template>
