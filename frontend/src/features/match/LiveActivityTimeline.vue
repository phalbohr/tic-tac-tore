<script setup lang="ts">
import { computed } from 'vue'
import type { TimelineGoal } from '@/stores/liveMatch'

defineOptions({
  name: 'LiveActivityTimeline',
})

const props = withDefaults(
  defineProps<{
    goals?: TimelineGoal[]
  }>(),
  {
    goals: () => [],
  },
)

const timelineGoals = computed(() => [...props.goals].reverse())

const formatTime = (timestamp: number) => {
  return new Date(timestamp).toLocaleTimeString([], {
    minute: '2-digit',
    second: '2-digit',
  })
}
</script>

<template>
  <div
    data-testid="live-activity-timeline"
    class="max-h-36 overflow-y-auto ch-bg-gray-800/95 ch-border-gray-700 border rounded-lg p-2 text-xs ch-text-white shadow-xl space-y-1.5 backdrop-blur-sm"
  >
    <div
      v-if="timelineGoals.length === 0"
      data-testid="timeline-empty"
      class="text-center ch-text-gray-400 py-1.5 italic"
    >
      No goals recorded
    </div>

    <div
      v-for="goal in timelineGoals"
      :key="goal.id"
      data-testid="timeline-goal-item"
      class="flex items-center justify-between gap-2 px-2.5 py-1.5 rounded ch-bg-gray-700/70 border ch-border-gray-600/40"
    >
      <div class="flex items-center gap-1.5 min-w-0">
        <span class="font-semibold truncate">{{ goal.playerName }}</span>
        <span class="ch-text-gray-400 text-[10px] truncate">({{ goal.quadrantRole }})</span>
      </div>
      <span class="ch-text-gray-400 text-[10px] whitespace-nowrap">{{ formatTime(goal.timestamp) }}</span>
    </div>
  </div>
</template>

<style scoped lang="scss">
/* Custom scrollbar styling for compact HUD display */
div::-webkit-scrollbar {
  width: 4px;
}
div::-webkit-scrollbar-thumb {
  background: rgba(156, 163, 175, 0.4);
  border-radius: 4px;
}
</style>
