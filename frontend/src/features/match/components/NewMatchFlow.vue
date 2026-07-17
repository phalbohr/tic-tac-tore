<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { useMatchDraftStore } from '../stores/matchDraftStore'
import MatchTypePicker from './MatchTypePicker.vue'
import PlayerSelection from './PlayerSelection.vue'
import ScoreEntry from './ScoreEntry.vue'
import BaseButton from '@/core/components/BaseButton.vue'

const store = useMatchDraftStore()
const emit = defineEmits<{
  (e: 'cancel'): void
  (e: 'complete'): void
}>()

onMounted(() => {
  store.fetchDefaults()
})

onUnmounted(() => {
  store.reset()
})

const isSubmitting = ref(false)
async function submitMatchDraft() {
  if (isSubmitting.value) return
  isSubmitting.value = true
  try {
    await store.loadRuleConfig()
    store.beginScoreEntry()
  } finally {
    isSubmitting.value = false
  }
}

function handleCancel() {
  store.reset()
  emit('cancel')
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
    
    <PlayerSelection />
    
    <BaseButton @click="submitMatchDraft" :disabled="isSubmitting" class="w-full mt-4 rounded-full">
      {{ isSubmitting ? 'Loading...' : 'Start Match' }}
    </BaseButton>
  </div>
  
  <ScoreEntry 
    v-else-if="store.matchState === 'score_entry' || store.matchState === 'ready_for_submission'" 
    @complete="handleMatchReady" 
    @cancel="handleCancel" 
  />
</template>

