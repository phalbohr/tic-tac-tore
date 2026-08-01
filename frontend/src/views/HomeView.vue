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
import RejectReasonSelector from '@/features/match/components/RejectReasonSelector.vue'
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
const { pendingCount, fetchPendingCount, rejectMatch } = usePendingMatches()
const confirmationStore = useMatchConfirmationStore()

const pendingMatches = ref<PendingMatchItem[]>([])
const selectedRejectMatchId = ref<string | null>(null)
const isRejectModalOpen = ref(false)
const rejectToastError = ref<string | null>(null)

watch(() => confirmationStore.lastConfirmedMatchId, async (confirmedId) => {
  if (confirmedId) {
    pendingMatches.value = pendingMatches.value.filter((m) => m.id !== confirmedId)
    await fetchPendingCount(true)
    await statsStore.fetchStats()
    await fetchPendingMatches()
  }
})

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
  isRejectModalOpen.value = false
  selectedRejectMatchId.value = null

  const res = await rejectMatch(matchId, payload.reason, payload.customReason)
  if (res.success) {
    pendingMatches.value = pendingMatches.value.filter((m) => m.id !== matchId)
    await fetchPendingCount(true)
  } else {
    const errorMsg = res.error || t('match.alreadyProcessed', 'Match was already processed by another opponent')
    rejectToastError.value = errorMsg
    pendingMatches.value = pendingMatches.value.filter((m) => m.id !== matchId)
    await fetchPendingCount(true)
    await fetchPendingMatches()

    setTimeout(() => {
      if (rejectToastError.value === errorMsg) {
        rejectToastError.value = ''
      }
    }, 5000)
  }
}

function handleConfirmationUndo(matchId?: string) {
  confirmationStore.cancelConfirmationTimer(matchId)
}

function getConfirmationToastMessage(matchNumber: number): string {
  const msg = t('match.matchConfirmedTapUndo', { number: matchNumber })
  return msg !== 'match.matchConfirmedTapUndo' ? msg : `Match ${matchNumber} confirmed. Tap to undo.`
}

function handleDismissError() {
  matchStore.clearSubmitError()
}

watch(() => matchStore.submitError, (newVal) => {
  if (newVal) {
    showNewMatch.value = true
  }
})

interface ApiMatchItem {
  id: string
  creatorNickname?: string
  teamAAttackerNickname?: string
  teamADefenderNickname?: string
  teamBAttackerNickname?: string
  teamBDefenderNickname?: string
  teamANames?: string[]
  teamBNames?: string[]
  teamAScore?: number
  teamBScore?: number
  games?: Array<{ teamAScore: number; teamBScore: number }>
  createdAt?: string
}

async function fetchPendingMatches() {
  if (!authStore.isAuthenticated) return
  try {
    const res = await fetch('/api/v1/matches/pending')
    if (res.ok) {
      const data = await res.json()
      const list: ApiMatchItem[] = Array.isArray(data) ? data : (data.matches || [])
      pendingMatches.value = list.map((m) => {
        const teamANames: string[] = []
        if (m.teamAAttackerNickname) teamANames.push(m.teamAAttackerNickname)
        if (m.teamADefenderNickname) teamANames.push(m.teamADefenderNickname)

        const teamBNames: string[] = []
        if (m.teamBAttackerNickname) teamBNames.push(m.teamBAttackerNickname)
        if (m.teamBDefenderNickname) teamBNames.push(m.teamBDefenderNickname)

        const games = (m.games || []).map((g) => ({
          teamAScore: g.teamAScore,
          teamBScore: g.teamBScore,
        }))

        return {
          id: m.id,
          creatorNickname: m.creatorNickname || 'Opponent',
          teamANames: teamANames.length > 0 ? teamANames : (m.teamANames || undefined),
          teamBNames: teamBNames.length > 0 ? teamBNames : (m.teamBNames || undefined),
          teamAScore: games[0]?.teamAScore ?? m.teamAScore,
          teamBScore: games[0]?.teamBScore ?? m.teamBScore,
          games: games.length > 0 ? games : undefined,
          createdAt: m.createdAt
        }
      })
    }
  } catch (e) {
    console.warn('Failed to fetch pending matches', e)
  }
}

onMounted(async () => {
  if (authStore.isAuthenticated) {
    await authStore.fetchProfile()
    await statsStore.fetchStats()
    await fetchPendingMatches()
  }
})

watch(() => authStore.isAuthenticated, async (newVal) => {
  if (newVal && !authStore.profile) {
    await authStore.fetchProfile()
    await fetchPendingMatches()
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
          :pending-confirmation-ids="confirmationStore.pendingConfirmationIds"
          @confirm="handleConfirmMatch"
          @reject="handleRejectMatch"
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

      <!-- Multi-Toast Stack for Pending Confirmations -->
      <div
        v-if="confirmationStore.activeConfirmations.length > 0"
        class="fixed bottom-6 left-4 right-4 z-50 max-w-md mx-auto pointer-events-none flex flex-col gap-2.5 items-stretch"
        data-testid="confirmation-toast-stack"
      >
        <TransitionGroup name="toast-list">
          <div
            v-for="item in confirmationStore.activeConfirmations"
            :key="item.matchId"
            class="pointer-events-auto w-full bg-surface-container-highest text-on-surface rounded-2xl p-4 shadow-2xl flex items-center justify-between gap-4"
            role="status"
            aria-live="polite"
            :data-testid="`confirmation-toast-${item.matchId}`"
          >
            <div class="flex items-center gap-3">
              <div class="w-8 h-8 rounded-full bg-primary/20 text-primary flex items-center justify-center font-bold text-sm">
                {{ item.countdown }}s
              </div>
              <span class="text-sm font-medium">
                {{ item.isOfflinePending ? t('match.willRetryOnline') : getConfirmationToastMessage(item.matchNumber) }}
              </span>
            </div>

            <BaseButton
              v-if="!item.isOfflinePending"
              variant="primary"
              @click="handleConfirmationUndo(item.matchId)"
              class="!h-10 px-4 text-xs font-bold min-h-12 min-w-[48px]"
              :data-testid="`undo-confirmation-btn-${item.matchId}`"
            >
              {{ t('match.undo') }}
            </BaseButton>
          </div>
        </TransitionGroup>
      </div>

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

.toast-list-move,
.toast-list-enter-active,
.toast-list-leave-active {
  transition: all 0.3s ease;
}

.toast-list-enter-from,
.toast-list-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>
