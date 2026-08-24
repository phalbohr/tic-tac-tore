<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { useMatchDraftStore } from '../stores/matchDraftStore'
import { useAuthStore } from '@/stores/auth'
import MatchTypePicker from './MatchTypePicker.vue'
import RulePicker from './RulePicker.vue'
import PlayerSelection from './PlayerSelection.vue'
import ScoreEntry from './ScoreEntry.vue'
import BaseButton from '@/core/components/BaseButton.vue'

const store = useMatchDraftStore()
const authStore = useAuthStore()
const emit = defineEmits<{
  (e: 'cancel'): void
  (e: 'complete'): void
}>()

onMounted(() => {
  store.fetchDefaults()
  authStore.fetchProfile()
})

onUnmounted(() => {
  if (!store.isPendingSubmission && !store.isOfflinePending) {
    store.reset()
  }
})

const isSubmitting = ref(false)
const errorMsg = ref('')
let abortController: AbortController | null = null

async function submitMatchDraft() {
  if (isSubmitting.value) return
  if (store.selectedPlayers.length !== (store.matchType === '1v1' ? 2 : 4)) return
  isSubmitting.value = true
  errorMsg.value = ''
  abortController = new AbortController()
  try {
    await store.loadRuleConfig(abortController.signal)
    if (store.matchState === 'draft' && (!abortController || !abortController.signal.aborted)) store.beginScoreEntry()
  } catch (error) {
    const e = error as Error;
    if (e.name !== 'AbortError') {
      errorMsg.value = 'Failed to start match. Check rules config.'
    }
  } finally {
    isSubmitting.value = false
    abortController = null
  }
}

function handleCancel() {
  if (abortController) abortController.abort()
  store.reset()
  emit('cancel')
}

function handleBack() {
  store.returnToDraft()
}

function handleMatchReady() {
  emit('complete')
}
</script>

<template>
  <div v-if="store.matchState === 'draft'" class="w-full flex flex-col items-center bg-surface-container-low rounded-2xl p-4 gap-6">
    <div class="flex justify-between items-center w-full mb-2">
      <h2 class="text-on-surface font-bold text-xl">New Match</h2>
      <BaseButton variant="secondary" @click="handleCancel" class="!h-12">Cancel</BaseButton>
    </div>
    
    <div class="w-full flex flex-col gap-2 text-start">
      <h3 class="text-on-surface font-headline font-bold mb-1">Match Type</h3>
      <MatchTypePicker />
    </div>

    <RulePicker />
    
    <PlayerSelection />
    <div v-if="errorMsg" class="text-red-500 text-sm mt-2">{{ errorMsg }}</div>
    
    <BaseButton @click="submitMatchDraft" :disabled="isSubmitting" class="w-full mt-4 rounded-full">
      {{ isSubmitting ? 'Loading...' : 'Start Match' }}
    </BaseButton>
  </div>
  
  <ScoreEntry 
    v-else-if="store.matchState === 'score_entry' || store.matchState === 'ready_for_submission'" 
    @complete="handleMatchReady" 
    @cancel="handleCancel" 
    @back="handleBack"
  />
  
  <div v-else class="w-full flex flex-col items-center bg-surface-container-low rounded-2xl p-4 gap-6">
    <p>Invalid match state. Please try again.</p>
    <BaseButton @click="handleCancel">Go Back</BaseButton>
  </div>
</template>

