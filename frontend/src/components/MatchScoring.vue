<script setup lang="ts">
import { ref, computed } from 'vue'

interface Player {
  id: string
  name: string
}

interface Players {
  creator: Player
  teammate: Player
  opponent1: Player
  opponent2: Player
}

/**
 * MatchScoring component for recording game-by-game scores.
 * Enforces Best-of-3 format and mandatory position swaps.
 */

const props = defineProps<{
  players: Players
}>()

// Constants for position mapping
const POSITIONS = {
  T1_NORMAL: 'creator-teammate',
  T1_SWAPPED: 'teammate-creator',
  T2_NORMAL: 'opponent1-opponent2',
  T2_SWAPPED: 'opponent2-opponent1'
} as const

const currentGameIndex = ref(0) // 0, 1, 2 (Game 1, 2, 3)
const games = ref([
  { team1Score: 0, team2Score: 0, team1Pos: '' as string, team2Pos: '' as string },
  { team1Score: 0, team2Score: 0, team1Pos: '' as string, team2Pos: '' as string },
  { team1Score: 0, team2Score: 0, team1Pos: '' as string, team2Pos: '' as string }
])

const currentGame = computed(() => games.value[currentGameIndex.value])

// Derived mandatory positions for Game 2 based on Game 1 choices
const game2Team1Pos = computed(() => {
  const g1 = games.value[0]
  if (!g1) return ''
  const g1Pos = g1.team1Pos
  return g1Pos === POSITIONS.T1_NORMAL ? POSITIONS.T1_SWAPPED : (g1Pos === POSITIONS.T1_SWAPPED ? POSITIONS.T1_NORMAL : '')
})

const game2Team2Pos = computed(() => {
  const g1 = games.value[0]
  if (!g1) return ''
  const g1Pos = g1.team2Pos
  return g1Pos === POSITIONS.T2_NORMAL ? POSITIONS.T2_SWAPPED : (g1Pos === POSITIONS.T2_SWAPPED ? POSITIONS.T2_NORMAL : '')
})

const team1Options = [
  { value: POSITIONS.T1_NORMAL, label: `${props.players.creator.name} (A) - ${props.players.teammate.name} (D)` },
  { value: POSITIONS.T1_SWAPPED, label: `${props.players.teammate.name} (A) - ${props.players.creator.name} (D)` }
]

const team2Options = [
  { value: POSITIONS.T2_NORMAL, label: `${props.players.opponent1.name} (A) - ${props.players.opponent2.name} (D)` },
  { value: POSITIONS.T2_SWAPPED, label: `${props.players.opponent2.name} (A) - ${props.players.opponent1.name} (D)` }
]

const getWinner = (gameIdx: number) => {
  const g = games.value[gameIdx]
  if (!g || g.team1Score === g.team2Score) return null
  return g.team1Score > g.team2Score ? 1 : 2
}

const canFinish = computed(() => {
  const g1Winner = getWinner(0)
  const g2Winner = getWinner(1)
  
  if (currentGameIndex.value === 1) {
    return g1Winner !== null && g2Winner !== null && g1Winner === g2Winner
  }
  return currentGameIndex.value === 2 && getWinner(2) !== null
})

const needsGame3 = computed(() => {
  if (currentGameIndex.value !== 1) return false
  const g1Winner = getWinner(0)
  const g2Winner = getWinner(1)
  return g1Winner !== null && g2Winner !== null && g1Winner !== g2Winner
})

/**
 * Resolves player name based on team, role (Attacker/Defender), and game index.
 */
const getPlayerByRole = (team: number, role: 'A' | 'D', gameIdx: number) => {
  // Use derived positions for Game 2 to ensure consistency
  let pos = ''
  if (gameIdx === 1) {
    pos = team === 1 ? game2Team1Pos.value : game2Team2Pos.value
  } else {
    const g = games.value[gameIdx]
    if (g) {
      pos = team === 1 ? g.team1Pos : g.team2Pos
    }
  }
  
  const map: Record<string, Record<'A' | 'D', string>> = {
    [POSITIONS.T1_NORMAL]: { A: props.players.creator.name, D: props.players.teammate.name },
    [POSITIONS.T1_SWAPPED]: { A: props.players.teammate.name, D: props.players.creator.name },
    [POSITIONS.T2_NORMAL]: { A: props.players.opponent1.name, D: props.players.opponent2.name },
    [POSITIONS.T2_SWAPPED]: { A: props.players.opponent2.name, D: props.players.opponent1.name }
  }

  return map[pos]?.[role] || 'Unknown'
}

const nextGame = () => {
  if (currentGameIndex.value < 2) {
    currentGameIndex.value++
  }
}

const emit = defineEmits(['finish'])

const finishMatch = () => {
  emit('finish', games.value.slice(0, currentGameIndex.value + 1))
}
</script>

<template>
  <div class="max-w-md mx-auto bg-white rounded-2xl shadow-xl p-6 border border-gray-100">
    <div class="flex items-center justify-between mb-6">
      <h3 class="text-xl font-black text-gray-900 uppercase tracking-tight">
        Game {{ currentGameIndex + 1 }}
      </h3>
      <div class="flex gap-1">
        <div v-for="i in 3" :key="i" 
             class="w-8 h-2 rounded-full transition-colors"
             :class="i-1 <= currentGameIndex ? 'bg-indigo-600' : 'bg-gray-100'">
        </div>
      </div>
    </div>

    <!-- Position Selection (Only for Game 1 and Game 3) -->
    <div v-if="currentGameIndex !== 1 && currentGame" class="space-y-4 mb-8">
      <div class="p-4 bg-indigo-50 rounded-xl border border-indigo-100">
        <label :for="'t1pos-'+currentGameIndex" class="block text-xs font-bold text-indigo-700 uppercase tracking-widest mb-2">Team 1 Positions</label>
        <select :id="'t1pos-'+currentGameIndex" v-model="currentGame.team1Pos" class="w-full p-2 bg-white border border-indigo-200 rounded-lg text-sm text-gray-900 font-bold">
          <option value="">Select positions...</option>
          <option v-for="opt in team1Options" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
        </select>
      </div>
      <div class="p-4 bg-gray-50 rounded-xl border border-gray-100">
        <label :for="'t2pos-'+currentGameIndex" class="block text-xs font-bold text-gray-600 uppercase tracking-widest mb-2">Team 2 Positions</label>
        <select :id="'t2pos-'+currentGameIndex" v-model="currentGame.team2Pos" class="w-full p-2 bg-white border border-gray-200 rounded-lg text-sm text-gray-900 font-bold">
          <option value="">Select positions...</option>
          <option v-for="opt in team2Options" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
        </select>
      </div>
    </div>


    <!-- Position Display (For Game 2 - Swapped) -->
    <div v-else-if="currentGameIndex === 1" class="space-y-4 mb-8">
      <div class="p-4 bg-amber-50 rounded-xl border border-amber-100">
        <span class="inline-block px-2 py-0.5 bg-amber-200 text-amber-800 text-[10px] font-bold rounded uppercase mb-2">Mandatory Swap</span>
        <div class="flex justify-between items-center">
           <div class="text-sm font-bold text-gray-700 position-display">
             (A) {{ getPlayerByRole(1, 'A', 1) }}
           </div>
           <div class="text-sm font-bold text-gray-700 position-display">
             (D) {{ getPlayerByRole(1, 'D', 1) }}
           </div>
        </div>
      </div>
      <div class="p-4 bg-gray-50 rounded-xl border border-gray-100">
        <div class="flex justify-between items-center">
           <div class="text-sm font-bold text-gray-700">
             (A) {{ getPlayerByRole(2, 'A', 1) }}
           </div>
           <div class="text-sm font-bold text-gray-700">
             (D) {{ getPlayerByRole(2, 'D', 1) }}
           </div>
        </div>
      </div>
    </div>

    <!-- Scores -->
    <div v-if="currentGame" class="grid grid-cols-2 gap-8 items-center mb-8">
      <div class="text-center">
        <label class="block text-xs font-bold text-gray-600 uppercase tracking-widest mb-4">Team 1</label>
        <input
          type="number"
          name="team1Score"
          data-testid="score-input-1"
          min="0"
          max="99"
          v-model.number="currentGame.team1Score"
          class="w-20 h-20 text-4xl font-black text-center bg-gray-50 border-2 border-gray-100 rounded-2xl focus:border-indigo-500 focus:bg-white outline-none transition-all text-gray-900"
        />
        </div>
        <div class="text-center">
        <label class="block text-xs font-bold text-gray-600 uppercase tracking-widest mb-4">Team 2</label>
        <input
          type="number"
          name="team2Score"
          data-testid="score-input-2"
          min="0"
          max="99"
          v-model.number="currentGame.team2Score"
          class="w-20 h-20 text-4xl font-black text-center bg-gray-50 border-2 border-gray-100 rounded-2xl focus:border-indigo-500 focus:bg-white outline-none transition-all text-gray-900"
        />
        </div>    </div>

    <div class="flex gap-4">
      <button 
        v-if="needsGame3 || (currentGameIndex === 0)"
        @click="nextGame"
        data-testid="next-game-button"
        class="next-game flex-1 py-4 bg-gray-900 hover:bg-black text-white font-black rounded-xl transition-all active:scale-[0.98]"
      >
        Next Game {{ currentGameIndex + 2 }}
      </button>
      
      <button 
        v-if="canFinish"
        @click="finishMatch"
        data-testid="submit-match-button"
        class="finish-match flex-1 py-4 bg-indigo-600 hover:bg-indigo-700 text-white font-black rounded-xl transition-all active:scale-[0.98] shadow-lg shadow-indigo-200"
      >
        Finish Match
      </button>
    </div>
  </div>
</template>
