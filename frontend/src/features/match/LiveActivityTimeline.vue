<script setup lang="ts">
import { computed } from 'vue'
import type { TimelineGoal } from '@/stores/liveMatch'

defineOptions({
  name: 'LiveActivityTimeline',
})

const props = withDefaults(
  defineProps<{
    goals?: TimelineGoal[]
    startTime?: number | null
  }>(),
  {
    goals: () => [],
    startTime: null,
  },
)

const timelineGoals = computed(() => [...props.goals].reverse())

const formatTime = (timestamp: number) => {
  const firstGoal = props.goals[0]
  const base =
    props.startTime ?? (firstGoal && firstGoal.timestamp > 1000000000 ? firstGoal.timestamp : 0)
  const elapsedMs = timestamp >= base ? timestamp - base : timestamp
  const totalSeconds = Math.max(0, Math.floor(elapsedMs / 1000))
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
}
</script>

<template>
  <div
    data-testid="live-activity-timeline"
    class="live-activity-timeline flex items-center gap-2 overflow-x-auto py-1 px-1 text-xs ch-text-white"
  >
    <div
      v-if="timelineGoals.length === 0"
      data-testid="timeline-empty"
      class="text-xs ch-text-gray-400 italic whitespace-nowrap"
    >
      No goals recorded
    </div>

    <div
      v-for="goal in timelineGoals"
      :key="goal.id"
      data-testid="timeline-goal-item"
      class="flex items-center gap-1.5 px-2.5 py-1 rounded ch-bg-gray-700/80 border ch-border-gray-600/50 whitespace-nowrap shrink-0 shadow-sm"
    >
      <span class="font-semibold">{{ goal.playerName }}</span>
      <span class="ch-text-gray-400 text-[11px]">({{ goal.quadrantRole }})</span>
      <span class="ch-text-gray-400 text-[10px] pl-1 font-mono">{{
        formatTime(goal.timestamp)
      }}</span>
    </div>
  </div>
</template>

<style scoped lang="scss">
/* Custom scrollbar styling for compact top-strip display */
.live-activity-timeline {
  scrollbar-width: thin;

  &::-webkit-scrollbar {
    height: 4px;
    width: 4px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(156, 163, 175, 0.4);
    border-radius: 4px;
  }
}
</style>
