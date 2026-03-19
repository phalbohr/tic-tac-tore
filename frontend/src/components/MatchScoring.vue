<script setup lang="ts">
import { ref, computed } from 'vue'

interface Player {
  id: number
  name: string
}

interface Players {
  creator: Player
  teammate: Player
  opponent1: Player
  opponent2: Player
}

const props = defineProps<{
  players: Players
}>()

const currentGameIndex = ref(0) // 0, 1, 2 (Game 1, 2, 3)
const games = ref([
  { team1Score: 0, team2Score: 0, team1Pos: '', team2Pos: '' },
  { team1Score: 0, team2Score: 0, team1Pos: '', team2Pos: '' },
  { team1Score: 0, team2Score: 0, team1Pos: '', team2Pos: '' }
])

// Derived positions for Game 2 (mandatory swap)
const game2Team1Pos = computed(() => {
  const g1Pos = games.value[0].team1Pos
  if (g1Pos === 'creator-teammate') return 'teammate-creator'
  if (g1Pos === 'teammate-creator') return 'creator-teammate'
  return ''
})

const game2Team2Pos = computed(() => {
  const g1Pos = games.value[0].team2Pos
  if (g1Pos === 'opponent1-opponent2') return 'opponent2-opponent1'
  if (g1Pos === 'opponent2-opponent1') return 'opponent1-opponent2'
  return ''
})

const team1Options = [
  { value: 'creator-teammate', label: `${props.players.creator.name} (A) - ${props.players.teammate.name} (D)` },
  { value: 'teammate-creator', label: `${props.players.teammate.name} (A) - ${props.players.creator.name} (D)` }
]

const team2Options = [
  { value: 'opponent1-opponent2', label: `${props.players.opponent1.name} (A) - ${props.players.opponent2.name} (D)` },
  { value: 'opponent2-opponent1', label: `${props.players.opponent2.name} (A) - ${props.players.opponent1.name} (D)` }
]

const canFinish = computed(() => {
  if (currentGameIndex.value === 1) {
    const g1Winner = games.value[0].team1Score > games.value[0].team2Score ? 1 : 2
    const g2Winner = games.value[1].team1Score > games.value[1].team2Score ? 1 : 2
    if (g1Winner === g2Winner) return true
  }
  return currentGameIndex.value === 2
})

const needsGame3 = computed(() => {
  if (currentGameIndex.value !== 1) return false
  const g1Winner = games.value[0].team1Score > games.value[0].team2Score ? 1 : 2
  const g2Winner = games.value[1].team1Score > games.value[1].team2Score ? 1 : 2
  return g1Winner !== g2Winner
})

const getPlayerByRole = (team: number, role: 'A' | 'D', gameIdx: number) => {
  let pos = ''
  if (gameIdx === 1) {
    pos = team === 1 ? game2Team1Pos.value : game2Team2Pos.value
  } else {
    pos = team === 1 ? games.value[gameIdx].team1Pos : games.value[gameIdx].team2Pos
  }
  
  if (team === 1) {
    if (pos === 'creator-teammate') return role === 'A' ? props.players.creator.name : props.players.teammate.name
    return role === 'A' ? props.players.teammate.name : props.players.creator.name
  } else {
    if (pos === 'opponent1-opponent2') return role === 'A' ? props.players.opponent1.name : props.players.opponent2.name
    return role === 'A' ? props.players.opponent2.name : props.players.opponent1.name
  }
}

const nextGame = () => {
  if (currentGameIndex.value < 2) {
    // Sync Game 2 positions if moving from Game 1
    if (currentGameIndex.value === 0) {
      games.value[1].team1Pos = game2Team1Pos.value
      games.value[1].team2Pos = game2Team2Pos.value
    }
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
    <div v-if="currentGameIndex !== 1" class="space-y-4 mb-8">
      <div class="p-4 bg-indigo-50 rounded-xl border border-indigo-100">
        <label :for="'t1pos-'+currentGameIndex" class="block text-xs font-bold text-indigo-400 uppercase tracking-widest mb-2">Team 1 Positions</label>
        <select :id="'t1pos-'+currentGameIndex" v-model="games[currentGameIndex].team1Pos" class="w-full p-2 bg-white border border-indigo-200 rounded-lg text-sm">
          <option value="">Select positions...</option>
          <option v-for="opt in team1Options" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
        </select>
      </div>
      <div class="p-4 bg-gray-50 rounded-xl border border-gray-100">
        <label :for="'t2pos-'+currentGameIndex" class="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">Team 2 Positions</label>
        <select :id="'t2pos-'+currentGameIndex" v-model="games[currentGameIndex].team2Pos" class="w-full p-2 bg-white border border-gray-200 rounded-lg text-sm">
          <option value="">Select positions...</option>
          <option v-for="opt in team2Options" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
        </select>
      </div>
    </div>

    <!-- Position Display (For Game 2 - Swapped) -->
    <div v-else class="space-y-4 mb-8">
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
    <div class="grid grid-cols-2 gap-8 items-center mb-8">
      <div class="text-center">
        <label class="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-4">Team 1</label>
        <input 
          type="number" 
          name="team1Score"
          min="0"
          v-model.number="games[currentGameIndex].team1Score"
          class="w-20 h-20 text-4xl font-black text-center bg-gray-50 border-2 border-gray-100 rounded-2xl focus:border-indigo-500 focus:bg-white outline-none transition-all"
        />
      </div>
      <div class="text-center">
        <label class="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-4">Team 2</label>
        <input 
          type="number" 
          name="team2Score"
          min="0"
          v-model.number="games[currentGameIndex].team2Score"
          class="w-20 h-20 text-4xl font-black text-center bg-gray-50 border-2 border-gray-100 rounded-2xl focus:border-indigo-500 focus:bg-white outline-none transition-all"
        />
      </div>
    </div>

    <div class="flex gap-4">
      <button 
        v-if="needsGame3 || (currentGameIndex === 0)"
        @click="nextGame"
        class="next-game flex-1 py-4 bg-gray-900 hover:bg-black text-white font-black rounded-xl transition-all active:scale-[0.98]"
      >
        Next Game {{ currentGameIndex + 2 }}
      </button>
      
      <button 
        v-if="canFinish"
        @click="finishMatch"
        class="finish-match flex-1 py-4 bg-indigo-600 hover:bg-indigo-700 text-white font-black rounded-xl transition-all active:scale-[0.98] shadow-lg shadow-indigo-200"
      >
        Finish Match
      </button>
    </div>
  </div>
</template>
