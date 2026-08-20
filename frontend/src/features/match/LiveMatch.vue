<script setup lang="ts">
import { ref } from 'vue'
import { useLiveMatchStore } from '@/stores/liveMatch'
import LiveQuadrant from './LiveQuadrant.vue'
import LiveActivityTimeline from './LiveActivityTimeline.vue'

defineOptions({
  name: 'LiveMatch',
})

const matchStore = useLiveMatchStore()
const isMatchStarted = ref(false)
const liveMatchContainer = ref<HTMLElement | null>(null)

const startMatch = async () => {
  if (liveMatchContainer.value) {
    try {
      if (liveMatchContainer.value.requestFullscreen) {
        await liveMatchContainer.value.requestFullscreen()
      } else if ((liveMatchContainer.value as any).webkitRequestFullscreen) { // eslint-disable-line @typescript-eslint/no-explicit-any
        await (liveMatchContainer.value as any).webkitRequestFullscreen() // eslint-disable-line @typescript-eslint/no-explicit-any
      }
      if (screen.orientation && (screen.orientation as any).lock) { // eslint-disable-line @typescript-eslint/no-explicit-any
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        await (screen.orientation as any).lock('landscape')
      }
    } catch (err) {
      console.warn('Orientation lock failed', err)
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
    <div v-if="isMatchStarted" class="absolute inset-0 flex items-center justify-center ch-bg-gray-900 z-50 landscape:hidden">
      <p class="text-xl">Please rotate your device to landscape mode</p>
    </div>
    <div v-if="!isMatchStarted" class="flex items-center justify-center w-full h-full">
      <button @click="startMatch" data-testid="start-match-btn" class="ch-bg-primary ch-text-white px-6 py-3 rounded text-xl">Start Match</button>
    </div>
    
    <div v-else class="flex flex-col w-full h-full">
      <!-- Top strip: Timeline + Undo button -->
      <header class="flex items-center justify-between gap-3 px-3 py-1.5 ch-bg-gray-800 border-b ch-border-gray-700 z-30 flex-none shadow-md">
        <div class="flex-1 min-w-0 overflow-hidden">
          <LiveActivityTimeline
            :goals="matchStore.goalTimeline"
            :startTime="matchStore.matchStartTime"
          />
        </div>

        <div class="flex-none">
          <button
            @click="matchStore.undoLastGoal()"
            :disabled="!matchStore.canUndo"
            data-testid="undo-goal-btn"
            class="px-3 py-1.5 rounded-lg font-medium text-xs transition-all ch-bg-gray-700 border ch-border-gray-600 ch-text-white shadow flex items-center gap-1.5 disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6" />
            </svg>
            <span>Undo</span>
          </button>
        </div>
      </header>

      <div class="grid grid-cols-2 grid-rows-2 flex-1 w-full min-h-0" data-testid="match-grid">
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
  </div>
</template>

<style scoped lang="scss">
/* Tailwind handles layout */
</style>
