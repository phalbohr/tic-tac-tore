<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { useAchievementStore } from '../stores/useAchievementStore'
import type { AchievementDto } from '@/services/achievementService'
import BadgeCard from './BadgeCard.vue'

const { t, te } = useI18n()
const authStore = useAuthStore()
const achievementStore = useAchievementStore()

const selectedBadge = ref<AchievementDto | null>(null)
const activeFilter = ref<'all' | 'badges' | 'anti'>('all')

watch(
  () => authStore.profile?.id,
  async (playerId) => {
    if (playerId) {
      try {
        await achievementStore.fetchPlayerAchievements(playerId)
      } catch {
        // Handled in store
      }
    }
  },
  { immediate: true },
)

const filteredAchievements = computed(() => {
  if (activeFilter.value === 'badges') {
    return achievementStore.badgesList
  }
  if (activeFilter.value === 'anti') {
    return achievementStore.antiAchievementsList
  }
  return achievementStore.achievements
})

function openBadgeModal(badge: AchievementDto) {
  selectedBadge.value = badge
}

function closeBadgeModal() {
  selectedBadge.value = null
}

const selectedBadgeTitle = computed(() => {
  if (!selectedBadge.value) return ''
  if (selectedBadge.value.nameKey && te(selectedBadge.value.nameKey)) {
    return t(selectedBadge.value.nameKey)
  }
  const fallbackKey = `achievements.${selectedBadge.value.code.toLowerCase()}.title`
  if (te(fallbackKey)) {
    return t(fallbackKey)
  }
  return t('achievements.unknownTitle', 'Achievement')
})

const selectedBadgeDescription = computed(() => {
  if (!selectedBadge.value) return ''
  if (selectedBadge.value.descriptionKey && te(selectedBadge.value.descriptionKey)) {
    return t(selectedBadge.value.descriptionKey)
  }
  const fallbackKey = `achievements.${selectedBadge.value.code.toLowerCase()}.description`
  if (te(fallbackKey)) {
    return t(fallbackKey)
  }
  return ''
})

const ICON_MAP: Record<string, string> = {
  trophy: 'emoji_events',
  flame: 'local_fire_department',
  target: 'track_changes',
  wall: 'fence',
}

const selectedBadgeIcon = computed(() => {
  if (!selectedBadge.value) return 'military_tech'
  const icon = selectedBadge.value.icon
  return (icon && ICON_MAP[icon]) || icon || 'military_tech'
})

const selectedBadgeModalIconClasses = computed(() => {
  if (!selectedBadge.value) return ''
  if (!selectedBadge.value.isUnlocked) {
    return 'bg-ch-surface-highest text-ch-text-secondary/40 border border-ch-border'
  }
  if (selectedBadge.value.category === 'ANTI_ACHIEVEMENT') {
    return 'bg-ch-secondary/20 text-ch-secondary border border-ch-secondary/30'
  }
  return 'bg-ch-primary/20 text-ch-primary border border-ch-primary/30'
})

const selectedBadgeModalStatusClasses = computed(() => {
  if (!selectedBadge.value?.isUnlocked) {
    return 'text-ch-text-secondary/50'
  }
  return selectedBadge.value.category === 'ANTI_ACHIEVEMENT'
    ? 'text-ch-secondary'
    : 'text-ch-primary'
})

const showModalProgress = computed(() => {
  return Boolean(
    selectedBadge.value?.hasProgress &&
    !selectedBadge.value?.isUnlocked &&
    selectedBadge.value?.targetValue,
  )
})

const modalProgressPercentage = computed(() => {
  if (!selectedBadge.value?.targetValue || selectedBadge.value.targetValue <= 0) return 0
  const cur = selectedBadge.value.currentProgress || 0
  return Math.min(100, Math.max(0, (cur / selectedBadge.value.targetValue) * 100))
})

const modalProgressRemaining = computed(() => {
  if (!selectedBadge.value?.targetValue) return 0
  const cur = selectedBadge.value.currentProgress || 0
  return Math.max(0, selectedBadge.value.targetValue - cur)
})

function formatUnlockDate(unlockedAt: string | null): string {
  if (!unlockedAt) return ''
  try {
    const date = new Date(unlockedAt)
    return date.toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    })
  } catch {
    return unlockedAt
  }
}
</script>

<template>
  <section
    data-testid="profile-badges-section"
    class="ch-profile-badges w-full bg-ch-surface-card/60 backdrop-blur-sm rounded-2xl p-5 border border-ch-border space-y-4"
  >
    <!-- Section Header -->
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-2">
        <span class="material-symbols-outlined text-ch-primary text-xl">military_tech</span>
        <h3 class="font-headline text-sm font-bold text-ch-text-primary uppercase tracking-wider">
          {{ t('achievements.title') }}
        </h3>
      </div>
      <div
        v-if="achievementStore.totalAvailable > 0"
        class="font-headline text-xs font-semibold px-2.5 py-1 rounded-full bg-ch-surface-highest text-ch-text-secondary flex items-center gap-1.5"
      >
        <span class="text-ch-primary font-bold">{{ achievementStore.totalUnlocked }}</span>
        <span>/</span>
        <span>{{ achievementStore.totalAvailable }}</span>
      </div>
    </div>

    <!-- Category Filter Tabs -->
    <div
      role="tablist"
      aria-label="Achievement Categories"
      class="flex items-center gap-2 overflow-x-auto pb-1"
      data-testid="category-filter-tabs"
    >
      <button
        type="button"
        role="tab"
        :aria-selected="activeFilter === 'all'"
        data-testid="category-filter-tab-all"
        :data-active="activeFilter === 'all'"
        @click="activeFilter = 'all'"
        class="px-3 py-1.5 rounded-xl font-headline text-xs font-semibold transition-all duration-200 flex items-center gap-1.5 shrink-0"
        :class="
          activeFilter === 'all'
            ? 'bg-ch-primary text-ch-surface shadow-sm'
            : 'bg-ch-surface-highest text-ch-text-secondary hover:text-ch-text-primary hover:bg-ch-surface-highest/80'
        "
      >
        <span>{{ t('achievements.filterAll') }}</span>
        <span class="text-[10px] opacity-80" data-testid="filter-count-all"
          >({{ achievementStore.totalUnlocked }}/{{ achievementStore.totalAvailable }})</span
        >
      </button>
      <button
        type="button"
        role="tab"
        :aria-selected="activeFilter === 'badges'"
        data-testid="category-filter-tab-badges"
        :data-active="activeFilter === 'badges'"
        @click="activeFilter = 'badges'"
        class="px-3 py-1.5 rounded-xl font-headline text-xs font-semibold transition-all duration-200 flex items-center gap-1.5 shrink-0"
        :class="
          activeFilter === 'badges'
            ? 'bg-ch-primary text-ch-surface shadow-sm'
            : 'bg-ch-surface-highest text-ch-text-secondary hover:text-ch-text-primary hover:bg-ch-surface-highest/80'
        "
      >
        <span>{{ t('achievements.filterBadges') }}</span>
        <span class="text-[10px] opacity-80" data-testid="filter-count-badges"
          >({{ achievementStore.badgesUnlockedCount }}/{{
            achievementStore.badgesTotalCount
          }})</span
        >
      </button>
      <button
        type="button"
        role="tab"
        :aria-selected="activeFilter === 'anti'"
        data-testid="category-filter-tab-anti"
        :data-active="activeFilter === 'anti'"
        @click="activeFilter = 'anti'"
        class="px-3 py-1.5 rounded-xl font-headline text-xs font-semibold transition-all duration-200 flex items-center gap-1.5 shrink-0"
        :class="
          activeFilter === 'anti'
            ? 'bg-ch-secondary text-ch-surface shadow-sm'
            : 'bg-ch-surface-highest text-ch-text-secondary hover:text-ch-text-primary hover:bg-ch-surface-highest/80'
        "
      >
        <span>{{ t('achievements.filterAnti') }}</span>
        <span class="text-[10px] opacity-80" data-testid="filter-count-anti"
          >({{ achievementStore.antiAchievementsUnlockedCount }}/{{
            achievementStore.antiAchievementsTotalCount
          }})</span
        >
      </button>
    </div>

    <!-- Loading State -->
    <div
      v-if="achievementStore.loading && achievementStore.achievements.length === 0"
      class="py-8 flex justify-center"
    >
      <span class="material-symbols-outlined animate-spin text-2xl text-ch-primary">sync</span>
    </div>

    <!-- Empty State -->
    <div
      v-else-if="filteredAchievements.length === 0"
      class="py-6 text-center text-xs text-ch-text-secondary/60 font-headline"
    >
      {{ t('achievements.empty') }}
    </div>

    <!-- Badges Grid -->
    <div v-else class="grid grid-cols-3 sm:grid-cols-5 gap-3">
      <BadgeCard
        v-for="badge in filteredAchievements"
        :key="badge.id"
        :badge="badge"
        @select="openBadgeModal"
      />
    </div>

    <!-- Badge Details Modal -->
    <Transition name="fade">
      <div
        v-if="selectedBadge"
        data-testid="badge-modal"
        class="fixed inset-0 z-50 flex items-center justify-center p-6 bg-black/75 backdrop-blur-md"
        role="dialog"
        aria-modal="true"
        @click.self="closeBadgeModal"
      >
        <div
          class="w-full max-w-sm bg-ch-surface-card rounded-2xl p-6 space-y-5 shadow-2xl border border-ch-border text-center"
        >
          <!-- Icon -->
          <div class="flex justify-center">
            <div
              class="w-16 h-16 rounded-2xl flex items-center justify-center shadow-lg"
              :class="selectedBadgeModalIconClasses"
            >
              <span class="material-symbols-outlined text-3xl">{{ selectedBadgeIcon }}</span>
            </div>
          </div>

          <!-- Title & Category -->
          <div class="space-y-1">
            <h4 class="font-headline text-lg font-bold text-ch-text-primary">
              {{ selectedBadgeTitle }}
            </h4>
            <p
              class="text-[10px] font-headline uppercase tracking-widest text-ch-primary/80 font-semibold"
            >
              {{ selectedBadge.category }}
            </p>
          </div>

          <!-- Description -->
          <p class="text-xs text-ch-text-secondary leading-relaxed">
            {{ selectedBadgeDescription }}
          </p>

          <!-- Modal Progress for Progressive Badges -->
          <div
            v-if="showModalProgress && selectedBadge"
            data-testid="modal-progress-container"
            class="p-3.5 rounded-xl bg-ch-surface-highest/60 border border-ch-border space-y-2 text-left"
          >
            <div class="flex items-center justify-between text-xs font-headline font-semibold">
              <span class="text-ch-text-primary">
                {{
                  t('achievements.progress', {
                    current: selectedBadge.currentProgress || 0,
                    target: selectedBadge.targetValue || 0,
                  })
                }}
              </span>
              <span class="text-ch-text-secondary/80 text-[11px] flex items-center gap-1.5">
                <span data-testid="modal-progress-percentage" class="text-ch-primary font-bold"
                  >{{ Math.round(modalProgressPercentage) }}%</span
                >
                <span>•</span>
                <span>{{
                  t('achievements.remaining', modalProgressRemaining, {
                    named: { count: modalProgressRemaining },
                  })
                }}</span>
              </span>
            </div>
            <div
              data-testid="modal-progress-bar"
              class="w-full h-2 rounded-full bg-ch-surface-highest overflow-hidden"
            >
              <div
                class="h-full bg-ch-primary rounded-full transition-all duration-300"
                :style="{ width: `${modalProgressPercentage}%` }"
              />
            </div>
          </div>

          <!-- Unlock Status & Date -->
          <div class="p-3 rounded-xl bg-ch-surface-highest/60 border border-ch-border space-y-1">
            <div
              class="flex items-center justify-center gap-1.5 text-xs font-semibold"
              :class="selectedBadgeModalStatusClasses"
            >
              <span class="material-symbols-outlined text-sm">
                {{ selectedBadge.isUnlocked ? 'check_circle' : 'lock' }}
              </span>
              <span>{{
                selectedBadge.isUnlocked ? t('achievements.unlocked') : t('achievements.locked')
              }}</span>
            </div>
            <div
              v-if="selectedBadge.isUnlocked && selectedBadge.unlockedAt"
              class="text-[10px] text-ch-text-secondary/70 font-headline"
            >
              {{ formatUnlockDate(selectedBadge.unlockedAt) }}
            </div>
          </div>

          <!-- Close Button -->
          <button
            type="button"
            @click="closeBadgeModal"
            class="w-full py-2.5 rounded-xl bg-ch-surface-highest hover:bg-ch-surface-highest/80 text-ch-text-primary font-headline font-bold text-xs transition-colors"
          >
            {{ t('common.close') }}
          </button>
        </div>
      </div>
    </Transition>
  </section>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
