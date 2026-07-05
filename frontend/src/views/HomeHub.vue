<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useStatsStore } from '@/features/stats/stores/useStatsStore'
import GoogleOAuthButton from '@/components/GoogleOAuthButton.vue'
import AvatarBase from '@/components/AvatarBase.vue'
import TutorialCarousel from '@/components/TutorialCarousel.vue'
import StatsDashboard from '@/features/stats/components/StatsDashboard.vue'
import EmptyStateCTA from '@/features/stats/components/EmptyStateCTA.vue'

const { t } = useI18n()
const authStore = useAuthStore()
const statsStore = useStatsStore()

onMounted(async () => {
  if (authStore.isAuthenticated) {
    await authStore.fetchProfile()
    await statsStore.fetchStats()
  }
})

watch(() => authStore.isAuthenticated, async (newVal) => {
  if (newVal && !authStore.profile) {
    await authStore.fetchProfile()
  }
})
</script>

<template>
  <div class="min-h-screen bg-background text-on-surface flex flex-col items-center w-full">
    <!-- Tutorial Overlay -->
    <Transition name="fade">
      <TutorialCarousel v-if="authStore.isAuthenticated && authStore.profile && !authStore.profile.tutorialCompleted" />
    </Transition>

    <!-- Header -->
    <header v-if="authStore.isAuthenticated && authStore.profile" class="w-full max-w-md bg-surface-container-low/80 backdrop-blur-xl py-3 px-6 flex justify-between items-center top-0 sticky z-50">
      <h1 class="text-lg font-bold text-on-surface font-headline tracking-tight">{{ t('home.title') }}</h1>
      <RouterLink to="/cabinet" class="flex items-center gap-2 hover:opacity-80 transition-opacity">
        <div class="w-8 h-8 rounded-lg overflow-hidden bg-white">
          <AvatarBase :avatar="authStore.profile.avatar" />
        </div>
      </RouterLink>
    </header>

    <main class="w-full max-w-md flex flex-col items-center justify-center flex-grow gap-8 p-6 text-center">
      <div v-if="!authStore.isAuthenticated" class="text-center flex flex-col items-center gap-6 mt-12">
        <div>
          <h1 class="text-4xl font-bold text-on-surface mb-2 font-headline">{{ t('home.title') }}</h1>
          <p class="text-on-surface-variant text-lg font-body">{{ t('home.subtitle') }}</p>
        </div>
        <p class="text-on-surface-variant font-body">{{ t('home.signInMessage') }}</p>
        <GoogleOAuthButton />
      </div>

      <div v-else class="flex flex-col items-center gap-6 mt-12 w-full">
        <div v-if="authStore.profile" class="flex flex-col items-center gap-3">
          <div class="w-24 h-24 rounded-xl shadow-2xl bg-surface-container-low overflow-hidden">
            <AvatarBase :avatar="authStore.profile.avatar" />
          </div>
          <p class="text-on-surface text-2xl font-bold font-headline mt-2">
            {{ t('home.welcomeBack') }}, {{ authStore.profile.nickname }}
          </p>
        </div>
        <div v-else class="animate-pulse flex flex-col items-center gap-3">
          <div class="w-24 h-24 bg-surface-container-highest rounded-xl"></div>
          <div class="h-8 w-48 bg-surface-container-highest rounded"></div>
        </div>

        <template v-if="(statsStore.confirmedMatchesCount ?? 0) < 5 && !statsStore.shouldShowDemoData">
          <EmptyStateCTA />
        </template>
        <template v-else>
          <StatsDashboard />
        </template>
        
        <button 
          @click="authStore.logout()" 
          class="px-6 py-2.5 bg-orange-50 text-orange-600 rounded-lg hover:bg-orange-100 transition-colors font-medium"
        >
          {{ t('auth.signOut') }}
        </button>
      </div>
    </main>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
