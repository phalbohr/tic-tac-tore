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
import NewMatchFlow from '@/features/match/components/NewMatchFlow.vue'
import PendingMatches, { type PendingMatchItem } from '@/features/match/components/PendingMatches.vue'
import UndoToast from '@/features/match/components/UndoToast.vue'
import ErrorToast from '@/features/match/components/ErrorToast.vue'
import BaseButton from '@/core/components/BaseButton.vue'
import { useMatchDraftStore } from '@/features/match/stores/matchDraftStore'
import { usePushNotifications } from '@/features/match/composables/usePushNotifications'
import { usePendingMatches } from '@/features/match/composables/usePendingMatches'
import { useMatchConfirmationStore } from '@/features/match/stores/matchConfirmationStore'
import { ref } from 'vue'

const { t } = useI18n()
const showNewMatch = ref(false)
const authStore = useAuthStore()
const statsStore = useStatsStore()
const matchStore = useMatchDraftStore()
const { permissionState, requestPermissionAndSubscribe } = usePushNotifications()
const { pendingCount } = usePendingMatches()
const confirmationStore = useMatchConfirmationStore()

const pendingMatches = ref<PendingMatchItem[]>([])

function handleMatchComplete() {
  showNewMatch.value = false
  matchStore.startSubmissionTimer()
}

function handleUndo() {
  matchStore.cancelSubmissionTimer()
  showNewMatch.value = true
}

function handleConfirmMatch(matchId: string) {
  confirmationStore.commitConfirmation(matchId)
}

function handleConfirmationUndo() {
  confirmationStore.cancelConfirmationTimer()
}

function handleDismissError() {
  matchStore.clearSubmitError()
}

watch(() => matchStore.submitError, (newVal) => {
  if (newVal) {
    showNewMatch.value = true
  }
})

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
      <!-- Permission Re-prompt Banner -->
      <div
        v-if="permissionState === 'denied'"
        class="w-full bg-error-container text-on-error-container p-4 rounded-xl mb-2 text-left text-sm flex flex-col gap-2 shadow-sm"
        data-testid="permission-warning-banner"
      >
        <div class="font-bold flex items-center gap-2">
          <span>⚠️ Push notifications disabled</span>
        </div>
        <p>
          Push notifications are disabled. You may miss match confirmation requests. Enable notifications in your browser settings.
        </p>
      </div>

      <!-- Permission Request CTA -->
      <button
        v-if="permissionState === 'default'"
        @click="requestPermissionAndSubscribe"
        class="w-full bg-surface-container-high text-primary p-4 rounded-xl mb-2 text-left text-sm flex items-center justify-between shadow-sm hover:opacity-90 transition-all font-medium"
        data-testid="enable-notifications-btn"
      >
        <span>🔔 Enable push notifications for match verification</span>
        <span class="font-bold text-xs bg-primary text-on-primary px-2 py-1 rounded-lg">Enable</span>
      </button>

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
          <div class="w-24 h-24 rounded-xl shadow-2xl bg-surface-container-low overflow-hidden relative">
            <AvatarBase :avatar="authStore.profile.avatar" />
            <div
              v-if="pendingCount > 0"
              class="absolute top-1.5 right-1.5 min-w-6 h-6 px-1 flex items-center justify-center bg-error text-on-error rounded-full text-xs font-bold shadow-md leading-none"
              data-testid="pending-badge-counter"
            >
              {{ pendingCount }}
            </div>
          </div>
          <p class="text-on-surface text-2xl font-bold font-headline mt-2">
            {{ t('home.welcomeBack') }}, {{ authStore.profile.nickname }}
          </p>
        </div>
        <div v-else class="animate-pulse flex flex-col items-center gap-3">
          <div class="w-24 h-24 bg-surface-container-highest rounded-xl"></div>
          <div class="h-8 w-48 bg-surface-container-highest rounded"></div>
        </div>

        <PendingMatches
          v-if="!showNewMatch && pendingMatches.length > 0"
          :pending-matches="pendingMatches"
          :pending-confirmation-id="confirmationStore.pendingConfirmation?.matchId"
          @confirm="handleConfirmMatch"
        />

        <template v-if="statsStore.isLoading">
          <div class="animate-pulse flex flex-col items-center w-full gap-4">
            <div class="h-32 w-full bg-surface-container-highest rounded-xl"></div>
          </div>
        </template>
        <template v-else-if="statsStore.confirmedMatchesCount !== null && statsStore.confirmedMatchesCount < 1 && !statsStore.shouldShowDemoData">
          <EmptyStateCTA />
        </template>
        <template v-else>
          <StatsDashboard v-if="!showNewMatch" />
          
          <div v-if="!showNewMatch" class="w-full flex flex-col gap-4">
            <BaseButton 
              @click="showNewMatch = true"
              class="w-full mt-4 rounded-full"
            >
              New Match
            </BaseButton>
            <p class="text-on-surface-variant italic font-body">{{ t('home.comingSoon') }}</p>
          </div>

          <NewMatchFlow v-else @cancel="showNewMatch = false" @complete="handleMatchComplete" />
        </template>
        <button 
          v-if="!showNewMatch"
          @click="authStore.logout()" 
          class="px-6 py-2.5 bg-orange-50 text-orange-600 rounded-lg hover:bg-orange-100 transition-colors font-medium"
        >
          {{ t('auth.signOut') }}
        </button>
      </div>

      <UndoToast
        v-if="matchStore.isPendingSubmission || matchStore.isOfflinePending"
        :countdown="matchStore.submissionCountdown"
        :is-offline="matchStore.isOfflinePending"
        @undo="handleUndo"
      />

      <UndoToast
        v-if="confirmationStore.isPending || confirmationStore.isOfflinePending"
        :countdown="confirmationStore.countdown"
        :is-offline="confirmationStore.isOfflinePending"
        :message="t('match.confirmedTapUndo')"
        @undo="handleConfirmationUndo"
      />

      <ErrorToast
        v-if="matchStore.submitError"
        :message="matchStore.submitError"
        @dismiss="handleDismissError"
      />
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
