<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
  playerId: string
  playerName: string
  role: string
}>()

const emit = defineEmits<{
  score: [playerId: string, role: string]
}>()

const isFlashing = ref(false)
let flashTimer: any = null

const handleTouch = (event: Event) => {
  event.preventDefault()

  if (navigator.vibrate) {
    navigator.vibrate([50])
  }

  isFlashing.value = true
  if (flashTimer) clearTimeout(flashTimer)
  flashTimer = setTimeout(() => {
    isFlashing.value = false
  }, 300)

  emit('score', props.playerId, props.role)
}
</script>

<template>
  <div
    class="flex items-center justify-center border border-gray-800 transition-colors duration-100 touch-manipulation select-none"
    :class="isFlashing ? 'ch-bg-green-500' : 'ch-bg-gray-800'"
    @touchstart="handleTouch"
    :data-testid="`quadrant-${role}`"
  >
    <div class="flex flex-col items-center">
      <span class="font-bold text-2xl ch-text-white">{{ playerName }}</span>
      <span class="text-base ch-text-gray-400">{{ role }}</span>
    </div>
  </div>
</template>

<style scoped lang="scss">
/* Tailwind handles layout */
</style>
