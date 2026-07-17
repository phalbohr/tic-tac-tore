<script setup lang="ts">
defineProps<{
  score: number
  scoreLimit: number
}>()
const emit = defineEmits<{
  (e: 'increment', amount: number): void
  (e: 'decrement'): void
}>()
</script>

<template>
  <div class="flex flex-col items-center gap-2">
    <!-- +5 stepper, visually distinct and larger, hidden if scoreLimit < 5 -->
    <button v-if="scoreLimit >= 5" 
            @click="emit('increment', 5)"
            class="bg-surface-container-highest text-on-surface font-bold text-2xl w-24 h-16 rounded-xl flex items-center justify-center cursor-pointer hover:bg-surface-container-highest/80 transition-colors">
      +5
    </button>
    
    <!-- +1 stepper -->
    <button @click="emit('increment', 1)"
            class="bg-surface-container-highest text-on-surface font-bold text-xl w-20 h-12 rounded-xl flex items-center justify-center cursor-pointer hover:bg-surface-container-highest/80 transition-colors mt-2">
      +1
    </button>
    
    <!-- Current score -->
    <div class="text-4xl font-bold text-on-surface my-4">{{ score }}</div>
    
    <!-- -1 stepper -->
    <button @click="emit('decrement')"
            class="bg-surface-container-highest text-on-surface font-bold text-xl w-20 h-12 rounded-xl flex items-center justify-center cursor-pointer hover:bg-surface-container-highest/80 transition-colors">
      -1
    </button>
  </div>
</template>
