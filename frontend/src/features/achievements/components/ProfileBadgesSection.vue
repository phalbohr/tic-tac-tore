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
  { immediate: true }
)

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
  return selectedBadge.value.code.replace(/_/g, ' ')
})

const selectedBadgeDescription = computed(() => {
  if (!selectedBadge.value) return ''
  if (selectedBadge.value.descriptionKey && te(selectedBadge.value.descriptionKey)) {
    return t(selectedBadge.value.descriptionKey)
  }
  return ''
})

const selectedBadgeIcon = computed(() => {
  if (!selectedBadge.value) return 'military_tech'
  switch (selectedBadge.value.icon) {
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
    class="w-full bg-surface-container-low/60 backdrop-blur-sm rounded-2xl p-5 border border-white/5 space-y-4"
  >
    <!-- Section Header -->
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-2">
        <span class="material-symbols-outlined text-primary text-xl">military_tech</span>
        <h3 class="font-headline text-sm font-bold text-on-surface uppercase tracking-wider">
          {{ t('achievements.title') }}
        </h3>
      </div>
      <div
        v-if="achievementStore.totalAvailable > 0"
        class="font-headline text-xs font-semibold px-2.5 py-1 rounded-full bg-surface-container-highest text-on-surface-variant flex items-center gap-1.5"
      >
        <span class="text-amber-400 font-bold">{{ achievementStore.totalUnlocked }}</span>
        <span>/</span>
        <span>{{ achievementStore.totalAvailable }}</span>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="achievementStore.loading && achievementStore.achievements.length === 0" class="py-8 flex justify-center">
      <span class="material-symbols-outlined animate-spin text-2xl text-primary">sync</span>
    </div>

    <!-- Empty State -->
    <div
      v-else-if="achievementStore.achievements.length === 0"
      class="py-6 text-center text-xs text-on-surface-variant/60 font-headline"
    >
      {{ t('achievements.empty') }}
    </div>

    <!-- Badges Grid -->
    <div
      v-else
      class="grid grid-cols-3 sm:grid-cols-5 gap-3"
    >
      <BadgeCard
        v-for="badge in achievementStore.achievements"
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
        <div class="w-full max-w-sm bg-surface-container-low rounded-2xl p-6 space-y-5 shadow-2xl border border-white/10 text-center">
          <!-- Icon -->
          <div class="flex justify-center">
            <div
              class="w-16 h-16 rounded-2xl flex items-center justify-center shadow-lg"
              :class="[
                selectedBadge.isUnlocked
                  ? 'bg-gradient-to-br from-amber-400/20 to-yellow-600/30 text-amber-300 border border-amber-400/30'
                  : 'bg-surface-container-highest text-on-surface-variant/40 border border-white/5'
              ]"
            >
              <span class="material-symbols-outlined text-3xl">{{ selectedBadgeIcon }}</span>
            </div>
          </div>

          <!-- Title & Category -->
          <div class="space-y-1">
            <h4 class="font-headline text-lg font-bold text-on-surface">
              {{ selectedBadgeTitle }}
            </h4>
            <p class="text-[10px] font-headline uppercase tracking-widest text-primary/80 font-semibold">
              {{ selectedBadge.category }}
            </p>
          </div>

          <!-- Description -->
          <p class="text-xs text-on-surface-variant leading-relaxed">
            {{ selectedBadgeDescription }}
          </p>

          <!-- Unlock Status & Date -->
          <div class="p-3 rounded-xl bg-surface-container-highest/60 border border-white/5 space-y-1">
            <div class="flex items-center justify-center gap-1.5 text-xs font-semibold"
              :class="selectedBadge.isUnlocked ? 'text-amber-400' : 'text-on-surface-variant/50'"
            >
              <span class="material-symbols-outlined text-sm">
                {{ selectedBadge.isUnlocked ? 'check_circle' : 'lock' }}
              </span>
              <span>{{ selectedBadge.isUnlocked ? t('achievements.unlocked') : t('achievements.locked') }}</span>
            </div>
            <div v-if="selectedBadge.isUnlocked && selectedBadge.unlockedAt" class="text-[10px] text-on-surface-variant/70 font-headline">
              {{ formatUnlockDate(selectedBadge.unlockedAt) }}
            </div>
          </div>

          <!-- Close Button -->
          <button
            type="button"
            @click="closeBadgeModal"
            class="w-full py-2.5 rounded-xl bg-surface-container-highest hover:bg-surface-container-high text-on-surface font-headline font-bold text-xs transition-colors"
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
