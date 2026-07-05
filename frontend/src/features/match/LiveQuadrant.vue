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

const handleTouch = (event: Event) => {
  event.preventDefault()

  if (navigator.vibrate) {
    navigator.vibrate([50])
  }

  isFlashing.value = true
  setTimeout(() => {
    isFlashing.value = false
  }, 300)

  emit('score', props.playerId, props.role)
}
</script>

<template>
  <div
    class="live-quadrant"
    :class="{ flashing: isFlashing }"
    @touchstart="handleTouch"
    :data-testid="`quadrant-${role}`"
  >
    <div class="quadrant-content">
      <span class="player-name">{{ playerName }}</span>
      <span class="player-role">{{ role }}</span>
    </div>
  </div>
</template>

<style scoped>
.live-quadrant {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  touch-action: manipulation;
  user-select: none;
  border: 1px solid #ccc;
  background-color: #f8f9fa;
  transition: background-color 0.1s;
}

.flashing {
  background-color: #4ade80;
}

.quadrant-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.player-name {
  font-weight: bold;
  font-size: 1.5rem;
}

.player-role {
  font-size: 1rem;
  color: #666;
}
</style>
