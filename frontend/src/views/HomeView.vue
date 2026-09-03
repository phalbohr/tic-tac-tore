<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
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
import RejectReasonSelector from '@/features/match/components/RejectReasonSelector.vue'
import PendingMatches, {
  type PendingMatchItem,
} from '@/features/match/components/PendingMatches.vue'
import UndoToast from '@/features/match/components/UndoToast.vue'
import ErrorToast from '@/features/match/components/ErrorToast.vue'
import SuccessToast from '@/core/components/SuccessToast.vue'
import BaseButton from '@/core/components/BaseButton.vue'
import CreatePoolModal from '@/features/matchmaking/components/CreatePoolModal.vue'
import ActivePoolsList from '@/features/matchmaking/components/ActivePoolsList.vue'
import PendingChallenges from '@/features/challenge/components/PendingChallenges.vue'
import HomeConfirmationToasts from '@/features/match/components/HomeConfirmationToasts.vue'
import MicroCelebrationBanner from '@/features/stats/components/MicroCelebrationBanner.vue'
import { mapApiMatchItem, type ApiMatchItem } from '@/features/match/utils/matchMapper'
import { useMatchDraftStore } from '@/features/match/stores/matchDraftStore'
import { usePushNotifications } from '@/features/match/composables/usePushNotifications'
import { usePendingMatches } from '@/features/match/composables/usePendingMatches'
import { useMatchConfirmationStore } from '@/features/match/stores/matchConfirmationStore'
import { useInsightStore } from '@/features/stats/stores/useInsightStore'

const { t } = useI18n()
const showNewMatch = ref(false)
const isCreatePoolOpen = ref(false)
const poolSuccessToast = ref<string | null>(null)
const authStore = useAuthStore()
const statsStore = useStatsStore()
const matchStore = useMatchDraftStore()
const insightStore = useInsightStore()
const { permissionState, requestPermissionAndSubscribe } = usePushNotifications()
const {
  pendingCount,
  fetchPendingCount,
  rejectMatch,
  deleteMatch,
  collapsedMatchIds,
  collapseMatch,
  expandAllMatches,
  cleanupCollapsedMatches,
} = usePendingMatches()
const confirmationStore = useMatchConfirmationStore()

const pendingMatches = ref<PendingMatchItem[]>([])
const selectedRejectMatchId = ref<string | null>(null)
const isRejectModalOpen = ref(false)
const rejectToastError = ref<string | null>(null)
const isRejecting = ref(false)
const isPulsing = ref(false)
let pulseTimeout: ReturnType<typeof setTimeout> | null = null

const visiblePendingMatches = computed(() => {
  return pendingMatches.value.filter((m) => !collapsedMatchIds.value.includes(m.id))
})

watch(pendingMatches, (newMatches) => {
  cleanupCollapsedMatches(newMatches.map((m) => m.id))
})

watch(pendingCount, (newVal, oldVal) => {
  if (newVal > oldVal) {
    isPulsing.value = true
    if (pulseTimeout) clearTimeout(pulseTimeout)
    pulseTimeout = setTimeout(() => {
      isPulsing.value = false
      pulseTimeout = null
    }, 3000)
  }
})

watch(
  () => confirmationStore.lastConfirmedMatchId,
  async (confirmedId) => {
    if (confirmedId) {
      pendingMatches.value = pendingMatches.value.filter((m) => m.id !== confirmedId)
      await fetchPendingCount(true)
      await statsStore.fetchStats()
      await insightStore.fetchInsights()
      await fetchPendingMatches()
    }
  },
)

function handleMatchComplete() {
  showNewMatch.value = false
  matchStore.startSubmissionTimer()
}

function handleUndo() {
  matchStore.cancelSubmissionTimer()
  showNewMatch.value = true
}

function handleConfirmMatch(matchId: string, matchNumber: number) {
  confirmationStore.commitConfirmation(matchId, matchNumber)
}

function handleRejectMatch(matchId: string) {
  selectedRejectMatchId.value = matchId
  isRejectModalOpen.value = true
}

async function handleSubmitRejection(payload: { reason: string; customReason: string }) {
  if (!selectedRejectMatchId.value) return
  const matchId = selectedRejectMatchId.value

  confirmationStore.cancelConfirmationTimer(matchId)

  isRejecting.value = true
  const res = await rejectMatch(matchId, payload.reason, payload.customReason)
  isRejecting.value = false

  if (res.success) {
    isRejectModalOpen.value = false
    selectedRejectMatchId.value = null
    pendingMatches.value = pendingMatches.value.filter((m) => m.id !== matchId)
    await fetchPendingCount(true)
  } else {
    const errorMsg =
      res.error || t('match.alreadyProcessed', 'Match was already processed by another opponent')
    rejectToastError.value = errorMsg

    if (res.error === undefined || res.error.includes('already processed')) {
      isRejectModalOpen.value = false
      selectedRejectMatchId.value = null
      pendingMatches.value = pendingMatches.value.filter((m) => m.id !== matchId)
      await fetchPendingCount(true)
      await fetchPendingMatches()
    }

    setTimeout(() => {
      if (rejectToastError.value === errorMsg) {
        rejectToastError.value = null
      }
    }, 5000)
  }
}

function handleDismissError() {
  matchStore.clearSubmitError()
}

function handlePoolCreated() {
  poolSuccessToast.value = t('pool.poolCreated', 'Matchmaking pool created successfully!')
  setTimeout(() => {
    poolSuccessToast.value = null
  }, 4000)
}

function handlePoolJoined() {
  poolSuccessToast.value = t('pool.poolJoinedSuccess', 'Joined matchmaking pool successfully!')
  setTimeout(() => {
    poolSuccessToast.value = null
  }, 4000)
}

watch(
  () => matchStore.submitError,
  (newVal) => {
    if (newVal) {
      showNewMatch.value = true
    }
  },
)

async function fetchPendingMatches() {
  if (!authStore.isAuthenticated) return
  try {
    const res = await fetch('/api/v1/matches/pending')
    if (res.ok) {
      const data = await res.json()
      const list: ApiMatchItem[] = Array.isArray(data) ? data : data.matches || []
      pendingMatches.value = list.map(mapApiMatchItem)
    }
  } catch (e) {
    console.warn('Failed to fetch pending matches', e)
  }
}

let pollInterval: ReturnType<typeof setInterval> | null = null

function handleDismissRejection(matchId: string) {
  pendingMatches.value = pendingMatches.value.filter((m) => m.id !== matchId)
}

async function handleDeleteRejection(matchId: string) {
  pendingMatches.value = pendingMatches.value.filter((m) => m.id !== matchId)
  await deleteMatch(matchId)
  await fetchPendingCount(true)
}

function handleEditRejection(matchItem: PendingMatchItem) {
  matchStore.loadFromRejectedMatch(matchItem)
  showNewMatch.value = true
}

function handleVisibilityChange() {
  if (document.visibilityState === 'visible' && authStore.isAuthenticated) {
    fetchPendingMatches()
    fetchPendingCount(true)
  }
}

onMounted(async () => {
  if (authStore.isAuthenticated) {
    await authStore.fetchProfile()
    await statsStore.fetchStats()
    await insightStore.fetchInsights()
    await fetchPendingMatches()
  }

  pollInterval = setInterval(() => {
    if (authStore.isAuthenticated) {
      fetchPendingMatches()
      fetchPendingCount(true)
    }
  }, 5000)

  if (typeof window !== 'undefined') {
    document.addEventListener('visibilitychange', handleVisibilityChange)
  }
})

onUnmounted(() => {
  if (pulseTimeout) {
    clearTimeout(pulseTimeout)
  }
  if (pollInterval) {
    clearInterval(pollInterval)
  }
  if (typeof window !== 'undefined') {
    document.removeEventListener('visibilitychange', handleVisibilityChange)
  }
})

watch(
  () => authStore.isAuthenticated,
  async (newVal) => {
    if (newVal && !authStore.profile) {
      await authStore.fetchProfile()
      await insightStore.fetchInsights()
      await fetchPendingMatches()
    }
  },
)
</script>

<template>
  <div class="min-h-screen bg-background text-on-surface flex flex-col items-center w-full">
    <!-- Tutorial Overlay -->
    <Transition name="fade">
      <TutorialCarousel
        v-if="
          authStore.isAuthenticated && authStore.profile && !authStore.profile.tutorialCompleted
        "
      />
    </Transition>

    <!-- Header -->
    <header
      v-if="authStore.isAuthenticated && authStore.profile"
      class="w-full max-w-md bg-surface-container-low/80 backdrop-blur-xl py-3 px-6 flex justify-between items-center top-0 sticky z-50"
    >
      <h1 class="text-lg font-bold text-on-surface font-headline tracking-tight">
        {{ t('home.title') }}
      </h1>
      <RouterLink to="/cabinet" class="flex items-center gap-2 hover:opacity-80 transition-opacity">
        <div class="w-8 h-8 rounded-lg overflow-hidden bg-white">
          <AvatarBase
            :avatar="authStore.profile.avatar"
            :name="authStore.profile.nickname"
            shape="square"
          />
        </div>
      </RouterLink>
    </header>

    <main
      class="w-full max-w-md flex flex-col items-center justify-center flex-grow gap-8 p-6 text-center"
    >
      <!-- Micro Celebration Banner -->
      <MicroCelebrationBanner
        v-if="!showNewMatch && authStore.isAuthenticated && insightStore.latestCelebrationInsight"
        :insight="insightStore.latestCelebrationInsight"
        @dismiss="insightStore.dismissCelebration(insightStore.latestCelebrationInsight.id)"
      />

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
          Push notifications are disabled. You may miss match confirmation requests. Enable
          notifications in your browser settings.
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
        <span class="font-bold text-xs bg-primary text-on-primary px-2 py-1 rounded-lg"
          >Enable</span
        >
      </button>

      <div
        v-if="!authStore.isAuthenticated"
        class="text-center flex flex-col items-center gap-6 mt-12"
      >
        <div>
          <h1 class="text-4xl font-bold text-on-surface mb-2 font-headline">
            {{ t('home.title') }}
          </h1>
          <p class="text-on-surface-variant text-lg font-body">{{ t('home.subtitle') }}</p>
        </div>
        <p class="text-on-surface-variant font-body">{{ t('home.signInMessage') }}</p>
        <GoogleOAuthButton />
      </div>

      <div v-else class="flex flex-col items-center gap-6 mt-12 w-full">
        <div v-if="authStore.profile" class="flex flex-col items-center gap-3">
          <div
            class="w-24 h-24 rounded-xl shadow-2xl bg-surface-container-low overflow-hidden relative"
          >
            <AvatarBase
              :avatar="authStore.profile.avatar"
              :name="authStore.profile.nickname"
              shape="square"
            />
            <div
              v-if="pendingCount > 0"
              class="absolute top-1.5 right-1.5 min-w-6 h-6 px-1 flex items-center justify-center bg-error text-on-error rounded-full text-xs font-bold shadow-md leading-none cursor-pointer"
              :class="{ 'animate-pulse': isPulsing }"
              data-testid="pending-badge-counter"
              role="button"
              tabindex="0"
              aria-label="Pending notifications"
              @click="expandAllMatches"
              @keydown.enter.prevent="expandAllMatches"
              @keydown.space.prevent="expandAllMatches"
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

        <Transition name="fade">
          <PendingMatches
            v-if="!showNewMatch && visiblePendingMatches.length > 0"
            :pending-matches="visiblePendingMatches"
            :pending-confirmation-ids="confirmationStore.pendingConfirmationIds"
            @confirm="handleConfirmMatch"
            @reject="handleRejectMatch"
            @dismiss-rejection="handleDismissRejection"
            @edit-rejection="handleEditRejection"
            @delete-rejection="handleDeleteRejection"
            @close="collapseMatch"
          />
        </Transition>

        <template v-if="statsStore.isLoading">
          <div class="animate-pulse flex flex-col items-center w-full gap-4">
            <div class="h-32 w-full bg-surface-container-highest rounded-xl"></div>
          </div>
        </template>
        <template
          v-else-if="
            statsStore.confirmedMatchesCount !== null &&
            statsStore.confirmedMatchesCount < 1 &&
            !statsStore.shouldShowDemoData
          "
        >
          <EmptyStateCTA />
        </template>
        <template v-else>
          <StatsDashboard v-if="!showNewMatch" />

          <div v-if="!showNewMatch" class="w-full flex flex-col gap-4">
            <PendingChallenges class="w-full text-left" />
            <BaseButton @click="showNewMatch = true" class="w-full mt-4 rounded-full">
              New Match
            </BaseButton>
            <BaseButton
              @click="isCreatePoolOpen = true"
              variant="primary"
              class="w-full rounded-full"
              data-test="want-to-play-button"
              data-testid="want-to-play-btn"
            >
              {{ t('pool.wantToPlay', 'Want to Play') }}
            </BaseButton>
            <ActivePoolsList @joined="handlePoolJoined" />
            <p class="text-on-surface-variant italic font-body">{{ t('home.comingSoon') }}</p>
          </div>

          <NewMatchFlow v-else @cancel="showNewMatch = false" @complete="handleMatchComplete" />
        </template>
      </div>

      <UndoToast
        v-if="matchStore.isPendingSubmission || matchStore.isOfflinePending"
        :countdown="matchStore.submissionCountdown"
        :is-offline="matchStore.isOfflinePending"
        @undo="handleUndo"
      />

      <SuccessToast
        v-if="poolSuccessToast"
        :message="poolSuccessToast"
        @dismiss="poolSuccessToast = null"
      />

      <CreatePoolModal
        :is-open="isCreatePoolOpen"
        @close="isCreatePoolOpen = false"
        @created="handlePoolCreated"
      />

      <!-- Multi-Toast Stack for Pending Confirmations -->
      <HomeConfirmationToasts />

      <ErrorToast
        v-if="matchStore.submitError"
        :message="matchStore.submitError"
        @dismiss="handleDismissError"
      />

      <ErrorToast
        v-if="rejectToastError"
        :message="rejectToastError"
        @dismiss="rejectToastError = null"
      />

      <RejectReasonSelector
        :is-open="isRejectModalOpen"
        :is-submitting="isRejecting"
        @submit="handleSubmitRejection"
        @cancel="isRejectModalOpen = false"
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
