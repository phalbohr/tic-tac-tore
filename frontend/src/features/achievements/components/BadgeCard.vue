<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { AchievementDto } from '@/services/achievementService'

const props = defineProps<{
  badge: AchievementDto
}>()

const emit = defineEmits<{
  (e: 'select', badge: AchievementDto): void
}>()

const { t, te } = useI18n()

const ICON_MAP: Record<string, string> = {
  trophy: 'emoji_events',
  flame: 'local_fire_department',
  target: 'track_changes',
  wall: 'fence',
}

const isAntiAchievement = computed(() => props.badge.category === 'ANTI_ACHIEVEMENT')

const localizedTitle = computed(() => {
  if (props.badge.nameKey && te(props.badge.nameKey)) {
    return t(props.badge.nameKey)
  }
  const fallbackKey = `achievements.${props.badge.code.toLowerCase()}.title`
  if (te(fallbackKey)) {
    return t(fallbackKey)
  }
  return t('achievements.unknownTitle', 'Achievement')
})

const iconName = computed(() => {
  const icon = props.badge.icon
  return (icon && ICON_MAP[icon]) || icon || 'military_tech'
})

const cardClasses = computed(() => [
  'ch-badge-card relative group w-full flex flex-col items-center p-3.5 rounded-2xl border transition-all duration-200 cursor-pointer text-center focus:outline-none focus-visible:ring-2 focus-visible:ring-ch-primary',
  props.badge.isUnlocked
    ? 'bg-ch-surface-card/90 hover:bg-ch-surface-highest/90 border-ch-primary/30 shadow-md hover:shadow-lg'
    : 'bg-ch-surface-card/40 hover:bg-ch-surface-card/70 border-ch-border opacity-60 hover:opacity-80',
])

const iconContainerClasses = computed(() => {
  if (!props.badge.isUnlocked) {
    return 'bg-ch-surface-highest text-ch-text-secondary/40 border border-ch-border'
  }
  if (isAntiAchievement.value) {
    return 'bg-ch-secondary/20 text-ch-secondary border border-ch-secondary/30 shadow-inner'
  }
  return 'bg-ch-primary/20 text-ch-primary border border-ch-primary/30 shadow-inner'
})

const titleClasses = computed(() => [
  'font-headline text-xs font-bold tracking-tight line-clamp-1',
  props.badge.isUnlocked ? 'text-ch-text-primary' : 'text-ch-text-secondary/70',
])

const statusClasses = computed(() => {
  if (!props.badge.isUnlocked) {
    return 'text-ch-text-secondary/40'
  }
  return isAntiAchievement.value ? 'text-ch-secondary' : 'text-ch-primary'
})

const showProgress = computed(() =>
  Boolean(props.badge.hasProgress && !props.badge.isUnlocked && props.badge.targetValue),
)

const progressPercentage = computed(() => {
  if (!props.badge.targetValue || props.badge.targetValue <= 0) return 0
  const cur = props.badge.currentProgress || 0
  return Math.min(100, Math.max(0, (cur / props.badge.targetValue) * 100))
})

const progressRatioText = computed(() => {
  const cur = props.badge.currentProgress ?? 0
  const target = props.badge.targetValue ?? 0
  return `${cur} / ${target}`
})
</script>

<template>
  <button
    type="button"
    data-testid="badge-card"
    :data-unlocked="badge.isUnlocked"
    @click="emit('select', badge)"
    :class="cardClasses"
  >
    <!-- Badge Icon Container -->
    <div
      class="w-12 h-12 rounded-xl flex items-center justify-center mb-2.5 transition-transform duration-200 group-hover:scale-105"
      :class="iconContainerClasses"
    >
      <span class="material-symbols-outlined text-2xl">
        {{ iconName }}
      </span>
    </div>

    <!-- Badge Title -->
    <span :class="titleClasses">
      {{ localizedTitle }}
    </span>

    <!-- Status Subtitle -->
    <span
      class="text-[9px] font-headline uppercase tracking-wider mt-1 font-semibold"
      :class="statusClasses"
    >
      {{ badge.isUnlocked ? t('achievements.unlocked') : t('achievements.locked') }}
    </span>

    <!-- Mini Progress Bar for Locked Progressive Badges -->
    <div v-if="showProgress" class="w-full mt-2 space-y-1" data-testid="badge-progress-container">
      <div
        data-testid="badge-progress-bar"
        class="w-full h-1.5 rounded-full bg-ch-surface-highest overflow-hidden"
      >
        <div
          class="h-full bg-ch-primary rounded-full transition-all duration-300"
          :style="{ width: `${progressPercentage}%` }"
        />
      </div>
      <span
        data-testid="badge-progress-ratio"
        class="text-[9px] font-headline font-semibold text-ch-text-secondary/70 tracking-tight"
      >
        {{ progressRatioText }}
      </span>
    </div>

    <!-- Locked Overlay Lock Badge -->
    <div
      v-if="!badge.isUnlocked"
      class="absolute top-2 right-2 text-ch-text-secondary/40"
      aria-hidden="true"
    >
      <span class="material-symbols-outlined text-xs">lock</span>
    </div>
  </button>
</template>
