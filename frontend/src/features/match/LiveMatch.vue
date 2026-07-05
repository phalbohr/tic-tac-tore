<script setup lang="ts">
import { ref } from 'vue'
import { useMatchStore } from '@/stores/match'
import LiveQuadrant from './LiveQuadrant.vue'

defineOptions({
  name: 'LiveMatch',
})

const matchStore = useMatchStore()
const isMatchStarted = ref(false)
const liveMatchContainer = ref<HTMLElement | null>(null)

const startMatch = async () => {
  if (liveMatchContainer.value && liveMatchContainer.value.requestFullscreen) {
    try {
      await liveMatchContainer.value.requestFullscreen()
      if (screen.orientation && ((screen.orientation as unknown as { lock: (orientation: string) => Promise<void> }).lock)) {
        await ((screen.orientation as unknown as { lock: (orientation: string) => Promise<void> }).lock)('landscape')
      }
    } catch (err) {
      console.warn(err)
    }
  }
  isMatchStarted.value = true
}

const onScore = (playerId: string, role: string) => {
  matchStore.recordGoal(playerId, role)
}
</script>

<template>
  <div ref="liveMatchContainer" class="live-match-container">
    <div v-if="!isMatchStarted" class="start-screen">
      <button @click="startMatch" data-testid="start-match-btn">Start Match</button>
    </div>
    
    <div v-else class="match-grid" data-testid="match-grid">
      <LiveQuadrant
        class="grid-item tl"
        :playerId="matchStore.teamA.attacker.id"
        :playerName="matchStore.teamA.attacker.name"
        role="teamA.attacker"
        @score="onScore"
      />
      <LiveQuadrant
        class="grid-item tr"
        :playerId="matchStore.teamB.attacker.id"
        :playerName="matchStore.teamB.attacker.name"
        role="teamB.attacker"
        @score="onScore"
      />
      <LiveQuadrant
        class="grid-item bl"
        :playerId="matchStore.teamA.defender.id"
        :playerName="matchStore.teamA.defender.name"
        role="teamA.defender"
        @score="onScore"
      />
      <LiveQuadrant
        class="grid-item br"
        :playerId="matchStore.teamB.defender.id"
        :playerName="matchStore.teamB.defender.name"
        role="teamB.defender"
        @score="onScore"
      />
    </div>
  </div>
</template>

<style scoped>
.live-match-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  background-color: #ffffff;
}

.start-screen {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.start-screen button {
  padding: 1rem 2rem;
  font-size: 1.5rem;
  cursor: pointer;
}

.match-grid {
  width: 100%;
  height: 100%;
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
}
</style>
