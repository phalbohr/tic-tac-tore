<script setup lang="ts">
import { computed, watch } from 'vue'
import { useMatchDraftStore, MatchType } from '../stores/matchDraftStore'
import ScoreStepper from './ScoreStepper.vue'
import BaseButton from '@/core/components/BaseButton.vue'

const store = useMatchDraftStore()
const emit = defineEmits<{
  (e: 'complete'): void
  (e: 'cancel'): void
}>()

const getPlayerName = (id?: string) => {
  if (!id) return 'Unknown'
  const opp = store.frequentOpponents.find(o => o.id === id)
  if (opp) return opp.nickname
  const fetched = store.fetchedPlayers[id]
  if (fetched) return fetched.nickname
  return `Player ${id.substring(0, 4)}`
}

const team1Name = computed(() => {
  if (store.matchType === MatchType.ONE_VS_ONE) {
    return getPlayerName(store.selectedPlayers[0])
  } else {
    return `${getPlayerName(store.selectedPlayers[0])} & ${getPlayerName(store.selectedPlayers[1])}`
  }
})

const team2Name = computed(() => {
  if (store.matchType === MatchType.ONE_VS_ONE) {
    return getPlayerName(store.selectedPlayers[1])
  } else {
    return `${getPlayerName(store.selectedPlayers[2])} & ${getPlayerName(store.selectedPlayers[3])}`
  }
})

const team1Wins = computed(() => {
  return store.games.filter(g => g.team1Score > g.team2Score).length
})

const team2Wins = computed(() => {
  return store.games.filter(g => g.team2Score > g.team1Score).length
})

function onTeam1Increment(amount: number) {
  store.incrementScore(1, amount)
}
function onTeam1Decrement() {
  store.decrementScore(1)
}
function onTeam2Increment(amount: number) {
  store.incrementScore(2, amount)
}
function onTeam2Decrement() {
  store.decrementScore(2)
}

watch(() => store.matchState, (newVal) => {
  if (newVal === 'ready_for_submission') {
    emit('complete')
  }
})
</script>

<template>
  <div class="w-full flex flex-col bg-surface-container-low rounded-2xl p-4">
    <!-- Header with past match context -->
    <div class="relative flex items-center justify-center mb-6">
      <BaseButton variant="secondary" @click="emit('cancel')" class="!h-10 px-4 absolute left-0">Cancel</BaseButton>
      <div class="text-on-surface-variant text-base font-bold flex flex-col items-center">
        <span>Match Score</span>
        <span class="text-xl text-on-surface">{{ team1Wins }} - {{ team2Wins }}</span>
      </div>
    </div>
    
    <div class="flex flex-col items-center mb-4 gap-1">
      <div v-for="idx in (store.ruleConfig?.gameLimit || 1)" :key="idx" class="text-sm text-on-surface-variant h-5 flex items-center justify-center">
        <span v-if="idx <= store.games.length">
          Game {{ idx }}: {{ store.games[idx - 1]?.team1Score }} - {{ store.games[idx - 1]?.team2Score }}
        </span>
        <span v-else class="opacity-50">
          Game {{ idx }}: -
        </span>
      </div>
    </div>
    
    <div class="flex justify-between gap-4">
      <!-- Team 1 -->
      <div class="flex-1 flex flex-col items-center">
        <h3 class="text-on-surface font-bold text-center mb-4 h-12 w-full block overflow-hidden text-ellipsis line-clamp-2 break-words">{{ team1Name }}</h3>
        <ScoreStepper 
          :score="store.currentGame.team1Score"
          :score-limit="store.ruleConfig?.scoreLimit || 10"
          :win-by-two="store.ruleConfig?.winByTwo || false"
          @increment="onTeam1Increment"
          @decrement="onTeam1Decrement"
        />
      </div>
      
      <!-- Team 2 -->
      <div class="flex-1 flex flex-col items-center">
        <h3 class="text-on-surface font-bold text-center mb-4 h-12 w-full block overflow-hidden text-ellipsis line-clamp-2 break-words">{{ team2Name }}</h3>
        <ScoreStepper 
          :score="store.currentGame.team2Score"
          :score-limit="store.ruleConfig?.scoreLimit || 10"
          :win-by-two="store.ruleConfig?.winByTwo || false"
          @increment="onTeam2Increment"
          @decrement="onTeam2Decrement"
        />
      </div>
    </div>
    
    <BaseButton 
      :disabled="!store.isGameComplete"
      @click="store.completeCurrentGame()"
      class="w-full mt-6"
    >
      {{ store.isMatchComplete ? 'Complete Match' : 'Next Game' }}
    </BaseButton>
  </div>
</template>
