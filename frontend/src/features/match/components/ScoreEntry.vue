<script setup lang="ts">
import { computed, watch, ref } from 'vue'
import { useMatchDraftStore, type PlayerDto, type GameScore } from '../stores/matchDraftStore'
import ScoreStepper from './ScoreStepper.vue'
import BaseButton from '@/core/components/BaseButton.vue'

const store = useMatchDraftStore()
const emit = defineEmits<{
  (e: 'complete'): void
  (e: 'cancel'): void
  (e: 'back'): void
}>()

const showCancelModal = ref(false)

const getPlayerName = (id?: string) => {
  if (!id) return 'Unknown'
  const opp = store.frequentOpponents.find((o: PlayerDto) => o.id === id)
  if (opp) return opp.nickname
  const fetched = store.fetchedPlayers[id]
  if (fetched) return fetched.nickname
  return `Player ${id.substring(0, 4)}`
}

const team1Name = computed(() => {
  const half = Math.ceil(store.selectedPlayers.length / 2)
  return store.selectedPlayers.slice(0, half).map((id: string) => getPlayerName(id)).join(' & ') || 'Team 1'
})

const team2Name = computed(() => {
  const half = Math.ceil(store.selectedPlayers.length / 2)
  return store.selectedPlayers.slice(half).map((id: string) => getPlayerName(id)).join(' & ') || 'Team 2'
})

const team1Wins = computed(() => {
  return store.games.filter((g: GameScore) => g.team1Score > g.team2Score).length
})

const team2Wins = computed(() => {
  return store.games.filter((g: GameScore) => g.team2Score > g.team1Score).length
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

    <div class="relative flex items-center justify-center mb-6">
      <BaseButton variant="secondary" @click="emit('back')" class="!h-10 px-4 absolute left-0">Back</BaseButton>
      <div class="text-on-surface-variant text-base font-bold flex flex-col items-center">
        <span>Match Score</span>
        <span class="text-xl text-on-surface">{{ team1Wins }} - {{ team2Wins }}</span>
      </div>
      <BaseButton variant="secondary" @click="showCancelModal = true" class="!h-10 px-4 absolute right-0">Cancel</BaseButton>
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
    
    <div class="flex flex-col gap-2 mt-6 w-full">
      <BaseButton
        :disabled="!store.isGameComplete"
        @click="store.completeCurrentGame()"
        class="w-full"
      >
        {{ store.isMatchComplete ? 'Complete Match' : 'Next Game' }}
      </BaseButton>

      <BaseButton
        v-if="store.canUndoLastGame"
        variant="secondary"
        @click="store.undoLastGame()"
        class="w-full"
      >
        Undo Last Game
      </BaseButton>
    </div>

    <Transition name="ch-fade">
      <div
        v-if="showCancelModal"
        class="fixed inset-0 z-50 flex items-center justify-center p-6 bg-black/75 backdrop-blur-md"
        role="dialog"
        aria-modal="true"
      >
        <div class="w-full max-w-sm bg-surface-container-low rounded-2xl p-6 space-y-6 shadow-2xl">
          <div class="text-center space-y-2">
            <div class="inline-flex items-center justify-center w-12 h-12 rounded-full bg-red-950/30 text-red-400 mb-2">
              <span class="material-symbols-outlined text-2xl">warning</span>
            </div>
            <h2 class="font-headline text-lg font-bold text-on-surface">
              Cancel Match
            </h2>
            <p class="text-xs text-on-surface-variant leading-relaxed">
              Are you sure you want to cancel this match? All recorded scores will be lost.
            </p>
          </div>

          <div class="flex flex-col gap-2">
            <BaseButton
              @click="showCancelModal = false; emit('cancel')"
              class="w-full !bg-red-600 hover:!bg-red-700 !text-white font-headline font-extrabold uppercase tracking-wider text-xs !h-12"
            >
              Confirm Cancel
            </BaseButton>

            <BaseButton
              variant="secondary"
              @click="showCancelModal = false"
              class="w-full font-headline font-bold text-xs !h-12"
            >
              Keep Playing
            </BaseButton>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.ch-fade-enter-active,
.ch-fade-leave-active {
  transition: opacity 0.2s ease;
}

.ch-fade-enter-from,
.ch-fade-leave-to {
  opacity: 0;
}
</style>
