<script setup lang="ts">
import { ref, computed, watchEffect, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useLiveMatchStore } from '@/stores/liveMatch'
import { useAuthStore } from '@/stores/auth'
import { useWakeLock } from '@/composables/useWakeLock'
import LiveQuadrant from './LiveQuadrant.vue'
import LiveActivityTimeline from './LiveActivityTimeline.vue'

defineOptions({
  name: 'LiveMatch',
})

const props = withDefaults(
  defineProps<{
    refereeMode?: boolean
  }>(),
  {
    refereeMode: false,
  },
)

const route = useRoute()
const matchStore = useLiveMatchStore()
const authStore = useAuthStore()
const wakeLock = useWakeLock()
const isMatchStarted = ref(false)
const liveMatchContainer = ref<HTMLElement | null>(null)

const isQueryReferee = computed(() => {
  const referee = route?.query?.referee
  const mode = route?.query?.mode
  const refereeMatches = Array.isArray(referee) ? referee.includes('true') : referee === 'true'
  const modeMatches = Array.isArray(mode) ? mode.includes('referee') : mode === 'referee'
  return refereeMatches || modeMatches
})

const isAutoReferee = computed(() => {
  if (!authStore.isAuthenticated || !authStore.profile) return false
  const user = authStore.profile
  const playerIds = [
    matchStore.teamA.attacker.id,
    matchStore.teamA.defender.id,
    matchStore.teamB.attacker.id,
    matchStore.teamB.defender.id,
  ]
  const playerNames = [
    matchStore.teamA.attacker.name,
    matchStore.teamA.defender.name,
    matchStore.teamB.attacker.name,
    matchStore.teamB.defender.name,
  ]
  const isParticipant =
    (user.id && playerIds.includes(user.id)) ||
    (user.nickname && playerNames.includes(user.nickname))
  return !isParticipant
})

const isRefereeActive = computed(() => {
  return props.refereeMode || isQueryReferee.value || isAutoReferee.value
})

watchEffect(() => {
  matchStore.setRefereeMode(isRefereeActive.value)
})

onMounted(() => {
  if (authStore.isAuthenticated && !authStore.profile) {
    authStore.fetchProfile()
  }
})

onUnmounted(() => {
  matchStore.setRefereeMode(false)
})

const startMatch = async () => {
  if (liveMatchContainer.value) {
    try {
      if (liveMatchContainer.value.requestFullscreen) {
        await liveMatchContainer.value.requestFullscreen()
      } else if ((liveMatchContainer.value as any).webkitRequestFullscreen) { // eslint-disable-line @typescript-eslint/no-explicit-any
        await (liveMatchContainer.value as any).webkitRequestFullscreen() // eslint-disable-line @typescript-eslint/no-explicit-any
      }
      if (typeof screen !== 'undefined' && screen.orientation && (screen.orientation as any).lock) { // eslint-disable-line @typescript-eslint/no-explicit-any
        const orientationMode = matchStore.isRefereeMode ? 'portrait' : 'landscape'
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        await (screen.orientation as any).lock(orientationMode)
      }
    } catch (err) {
      console.warn('Orientation lock failed', err)
    }
  }
  await wakeLock.request()
  isMatchStarted.value = true
}

const onScore = (playerId: string, role: string) => {
  matchStore.recordGoal(playerId, role)
}

const onSwap = (team: 'teamA' | 'teamB') => {
  if (typeof navigator !== 'undefined' && typeof navigator.vibrate === 'function') {
    navigator.vibrate([30])
  }
  matchStore.swapPositions(team)
}
</script>

<template>
  <div ref="liveMatchContainer" class="ch-bg-gray-900 ch-text-white w-screen h-screen overflow-hidden">
    <div
      v-if="isMatchStarted && !matchStore.isRefereeMode"
      data-testid="rotation-warning-overlay"
      class="absolute inset-0 flex items-center justify-center ch-bg-gray-900 z-50 landscape:hidden"
    >
      <p class="text-xl">Please rotate your device to landscape mode</p>
    </div>
    <div v-if="!isMatchStarted" class="flex items-center justify-center w-full h-full">
      <button @click="startMatch" data-testid="start-match-btn" class="ch-bg-primary ch-text-white px-6 py-3 rounded text-xl">Start Match</button>
    </div>
    
    <div v-else class="flex flex-col w-full h-full">
      <!-- Top strip: Timeline + Undo button -->
      <header class="flex items-center justify-between gap-3 px-3 py-1.5 ch-bg-gray-800 border-b ch-border-gray-700 z-30 flex-none shadow-md">
        <div class="flex-1 min-w-0 overflow-x-auto">
          <LiveActivityTimeline
            :goals="matchStore.goalTimeline"
            :startTime="matchStore.matchStartTime"
          />
        </div>

        <div class="flex-none shrink-0">
          <button
            @click="matchStore.undoLastGoal()"
            :disabled="!matchStore.canUndo"
            data-testid="undo-goal-btn"
            class="px-3 py-1.5 rounded-lg font-medium text-xs transition-all ch-bg-gray-700 border ch-border-gray-600 ch-text-white shadow flex items-center gap-1.5 disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer shrink-0"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6" />
            </svg>
            <span>Undo</span>
          </button>
        </div>
      </header>

      <div class="relative grid grid-cols-2 grid-rows-2 flex-1 w-full min-h-0" data-testid="match-grid">
        <template v-if="matchStore.isRefereeMode">
          <!-- Referee Mode: 2x2 grid representing table viewed from the end -->
          <!-- Row 1: Left = Team B Defender, Right = Team A Attacker -->
          <LiveQuadrant
            class="grid-item tl"
            :playerId="matchStore.teamB.defender.id"
            :playerName="matchStore.teamB.defender.name"
            role="teamB.defender"
            @score="onScore"
          />
          <LiveQuadrant
            class="grid-item tr"
            :playerId="matchStore.teamA.attacker.id"
            :playerName="matchStore.teamA.attacker.name"
            role="teamA.attacker"
            @score="onScore"
          />
          <!-- Row 2: Left = Team B Attacker, Right = Team A Defender -->
          <LiveQuadrant
            class="grid-item bl"
            :playerId="matchStore.teamB.attacker.id"
            :playerName="matchStore.teamB.attacker.name"
            role="teamB.attacker"
            @score="onScore"
          />
          <LiveQuadrant
            class="grid-item br"
            :playerId="matchStore.teamA.defender.id"
            :playerName="matchStore.teamA.defender.name"
            role="teamA.defender"
            @score="onScore"
          />
        </template>

        <template v-else>
          <!-- Standard Landscape Mode -->
          <!-- Top Row: Team B -->
          <LiveQuadrant
            class="grid-item tl"
            :playerId="matchStore.teamB.defender.id"
            :playerName="matchStore.teamB.defender.name"
            role="teamB.defender"
            @score="onScore"
          />
          <LiveQuadrant
            class="grid-item tr"
            :playerId="matchStore.teamB.attacker.id"
            :playerName="matchStore.teamB.attacker.name"
            role="teamB.attacker"
            @score="onScore"
          />
          <!-- Bottom Row: Team A -->
          <LiveQuadrant
            class="grid-item bl"
            :playerId="matchStore.teamA.attacker.id"
            :playerName="matchStore.teamA.attacker.name"
            role="teamA.attacker"
            @score="onScore"
          />
          <LiveQuadrant
            class="grid-item br"
            :playerId="matchStore.teamA.defender.id"
            :playerName="matchStore.teamA.defender.name"
            role="teamA.defender"
            @score="onScore"
          />
        </template>

        <!-- Centered Swap Buttons -->
        <!-- Team B Swap Button: Centered in left column in referee mode; top row center in landscape -->
        <div
          class="absolute -translate-x-1/2 -translate-y-1/2 z-20 pointer-events-auto"
          :class="matchStore.isRefereeMode ? 'top-1/2 left-1/4' : 'top-1/4 left-1/2'"
        >
          <button
            type="button"
            @pointerdown.stop
            @click.stop="onSwap('teamB')"
            data-testid="swap-team-b-btn"
            aria-label="Swap Team B Positions"
            class="w-14 h-14 min-w-[56px] min-h-[56px] rounded-full ch-bg-gray-700 border-2 ch-border-gray-600 ch-text-white shadow-lg flex items-center justify-center cursor-pointer hover:ch-bg-gray-600 active:scale-95 transition-all"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="w-7 h-7" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
            </svg>
          </button>
        </div>

        <!-- Team A Swap Button: Centered in right column in referee mode; bottom row center in landscape -->
        <div
          class="absolute -translate-x-1/2 -translate-y-1/2 z-20 pointer-events-auto"
          :class="matchStore.isRefereeMode ? 'top-1/2 left-3/4' : 'top-3/4 left-1/2'"
        >
          <button
            type="button"
            @pointerdown.stop
            @click.stop="onSwap('teamA')"
            data-testid="swap-team-a-btn"
            aria-label="Swap Team A Positions"
            class="w-14 h-14 min-w-[56px] min-h-[56px] rounded-full ch-bg-gray-700 border-2 ch-border-gray-600 ch-text-white shadow-lg flex items-center justify-center cursor-pointer hover:ch-bg-gray-600 active:scale-95 transition-all"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="w-7 h-7" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
/* Tailwind handles layout */
</style>
