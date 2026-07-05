<script setup lang="ts">
import { useMatchDraftStore } from '../stores/matchDraftStore'

defineOptions({
  name: 'MatchTypePicker'
})

const store = useMatchDraftStore()
</script>

<template>
  <div class="flex gap-4 w-full">
    <button 
      class="flex-1 h-14 rounded-xl font-bold transition-colors"
      :class="store.matchType === '1v1' ? 'bg-primary text-background' : 'bg-surface-container-highest text-on-surface'"
      @click="store.setMatchType('1v1')"
    >
      1v1
    </button>
    <button 
      class="flex-1 h-14 rounded-xl font-bold transition-colors"
      :class="store.matchType === '2v2' ? 'bg-primary text-background' : 'bg-surface-container-highest text-on-surface'"
      @click="store.setMatchType('2v2')"
    >
      2v2
    </button>
  </div>
</template>
