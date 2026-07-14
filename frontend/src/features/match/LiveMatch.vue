<script setup lang="ts">
import { ref } from 'vue'
import { useLiveMatchStore } from '@/stores/liveMatch'
import LiveQuadrant from './LiveQuadrant.vue'

defineOptions({
  name: 'LiveMatch',
})

const matchStore = useLiveMatchStore()
const isMatchStarted = ref(false)
const showRotateFallback = ref(false)
const liveMatchContainer = ref<HTMLElement | null>(null)

const startMatch = async () => {
  if (liveMatchContainer.value) {
    try {
      if (liveMatchContainer.value.requestFullscreen) {
        await liveMatchContainer.value.requestFullscreen()
      } else if ((liveMatchContainer.value as any).webkitRequestFullscreen) {
        await (liveMatchContainer.value as any).webkitRequestFullscreen()
      }
      if (screen.orientation && (screen.orientation as any).lock) {
        await (screen.orientation as any).lock('landscape')
      }
    } catch (err) {
      console.warn('Orientation lock failed, showing fallback', err)
      showRotateFallback.value = true
    }
  }
  isMatchStarted.value = true
}

const onScore = (playerId: string, role: string) => {
  matchStore.recordGoal(playerId, role)
}
</script>

<template>
  <div ref="liveMatchContainer" class="ch-bg-gray-900 ch-text-white w-screen h-screen overflow-hidden">
    <div v-if="showRotateFallback" class="absolute inset-0 flex items-center justify-center ch-bg-gray-900 z-50 landscape:hidden">
      <p class="text-xl">Please rotate your device to landscape mode</p>
    </div>
    <div v-if="!isMatchStarted" class="flex items-center justify-center w-full h-full">
      <button @click="startMatch" data-testid="start-match-btn" class="ch-bg-primary ch-text-white px-6 py-3 rounded text-xl">Start Match</button>
    </div>
    
    <div v-else class="grid grid-cols-2 grid-rows-2 w-full h-full" data-testid="match-grid">
      <LiveQuadrant
        class="grid-item tl"
        :playerId="matchStore.teamA.attacker.id"
        :playerName="matchStore.teamA.attacker.name"
        role="teamA.attacker"
        @score="onScore"
      />
      <LiveQuadrant
        class="grid-item tr"
        :playerId="matchStore.teamA.defender.id"
        :playerName="matchStore.teamA.defender.name"
        role="teamA.defender"
        @score="onScore"
      />
      <LiveQuadrant
        class="grid-item bl"
        :playerId="matchStore.teamB.defender.id"
        :playerName="matchStore.teamB.defender.name"
        role="teamB.defender"
        @score="onScore"
      />
      <LiveQuadrant
        class="grid-item br"
        :playerId="matchStore.teamB.attacker.id"
        :playerName="matchStore.teamB.attacker.name"
        role="teamB.attacker"
        @score="onScore"
      />
    </div>
  </div>
</template>

<style scoped lang="scss">
/* Tailwind handles layout */
</style>
