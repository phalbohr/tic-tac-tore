<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { useMatchDraftStore } from '../stores/matchDraftStore'
import MatchTypePicker from './MatchTypePicker.vue'
import PlayerSelection from './PlayerSelection.vue'
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

function submitMatchDraft() {
  console.log('Submitting match draft', store.matchType, store.selectedPlayers, store.ruleSystem)
  // Logic to actually create the match
  store.reset()
  emit('complete')
}

function handleCancel() {
  store.reset()
  emit('cancel')
}
</script>

<template>
  <div class="w-full flex flex-col items-center bg-surface-container-low rounded-2xl p-4 gap-6">
    <div class="flex justify-between items-center w-full mb-2">
      <h2 class="text-on-surface font-bold text-xl">New Match</h2>
      <BaseButton variant="secondary" @click="handleCancel" class="!h-12">Cancel</BaseButton>
    </div>
    
    <div class="w-full flex flex-col gap-2 text-start">
      <h3 class="text-on-surface font-headline font-bold mb-1">Match Type</h3>
      <MatchTypePicker />
    </div>
    
    <PlayerSelection />
    
    <BaseButton @click="submitMatchDraft" class="w-full mt-4 rounded-full">
      Start Match
    </BaseButton>
  </div>
</template>
