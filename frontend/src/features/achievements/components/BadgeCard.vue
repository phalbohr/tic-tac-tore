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

const localizedDescription = computed(() => {
  if (props.badge.descriptionKey && te(props.badge.descriptionKey)) {
    return t(props.badge.descriptionKey)
  }
  const fallbackKey = `achievements.${props.badge.code.toLowerCase()}.description`
  if (te(fallbackKey)) {
    return t(fallbackKey)
  }
  return ''
})

const iconName = computed(() => {
  switch (props.badge.icon) {
    case 'trophy':
      return 'emoji_events'
    case 'flame':
      return 'local_fire_department'
    case 'shield':
      return 'shield'
    case 'target':
      return 'track_changes'
    case 'wall':
      return 'fence'
    default:
      return 'military_tech'
  }
})
</script>

<template>
  <button
    type="button"
    data-testid="badge-card"
    :data-unlocked="badge.isUnlocked"
    @click="emit('select', badge)"
    class="ch-badge-card relative group w-full flex flex-col items-center p-3.5 rounded-2xl border transition-all duration-200 cursor-pointer text-center focus:outline-none focus-visible:ring-2 focus-visible:ring-ch-primary"
    :class="[
      badge.isUnlocked
        ? 'bg-ch-surface-card/90 hover:bg-ch-surface-highest/90 border-ch-primary/30 shadow-md hover:shadow-lg'
        : 'bg-ch-surface-card/40 hover:bg-ch-surface-card/70 border-ch-border opacity-60 hover:opacity-80'
    ]"
  >
    <!-- Badge Icon Container -->
    <div
      class="w-12 h-12 rounded-xl flex items-center justify-center mb-2.5 transition-transform duration-200 group-hover:scale-105"
      :class="[
        badge.isUnlocked
          ? 'bg-gradient-to-br from-amber-400/20 to-yellow-600/30 text-amber-300 border border-amber-400/30 shadow-inner'
          : 'bg-ch-surface-highest text-ch-text-secondary/40 border border-ch-border'
      ]"
    >
      <span class="material-symbols-outlined text-2xl">
        {{ iconName }}
      </span>
    </div>

    <!-- Badge Title -->
    <span
      class="font-headline text-xs font-bold tracking-tight line-clamp-1"
      :class="badge.isUnlocked ? 'text-ch-text-primary' : 'text-ch-text-secondary/70'"
    >
      {{ localizedTitle }}
    </span>

    <!-- Status Subtitle -->
    <span class="text-[9px] font-headline uppercase tracking-wider mt-1 font-semibold"
      :class="badge.isUnlocked ? 'text-amber-400/90' : 'text-ch-text-secondary/40'"
    >
      {{ badge.isUnlocked ? t('achievements.unlocked') : t('achievements.locked') }}
    </span>

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
