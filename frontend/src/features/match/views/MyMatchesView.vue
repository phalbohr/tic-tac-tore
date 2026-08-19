<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useMatchHistoryStore } from '../stores/useMatchHistoryStore'
import { usePendingMatches } from '../composables/usePendingMatches'
import { useMatchConfirmationStore } from '../stores/matchConfirmationStore'
import MatchFilterChips from '../components/MatchFilterChips.vue'
import MatchHistoryList from '../components/MatchHistoryList.vue'
import PendingMatches from '../components/PendingMatches.vue'
import RejectReasonSelector from '../components/RejectReasonSelector.vue'
import UndoToast from '../components/UndoToast.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const store = useMatchHistoryStore()
const pendingHelper = usePendingMatches()
const confirmationStore = useMatchConfirmationStore()

const isRejectModalOpen = ref(false)
const rejectingMatchId = ref<string | null>(null)
const isSubmittingRejection = ref(false)

function syncFromQuery() {
  const tab = route.query.tab
  if (tab === 'pending' || tab === 'confirmed') {
    store.activeTab = tab
  }
  const pId = route.query.playerId
  if (typeof pId === 'string' && pId.trim()) {
    store.filters.playerId = pId.trim()
  }
  const mType = route.query.matchType
  if (mType === '1v1' || mType === '2v2') {
    store.filters.matchType = mType
  }
}

watch(
  () => store.activeTab,
  (newTab) => {
    router.replace({
      query: {
        ...route.query,
        tab: newTab,
      },
    })
    if (newTab === 'confirmed') {
      store.fetchConfirmedHistory()
    } else {
      store.fetchPendingMatches()
      pendingHelper.fetchPendingCount(true)
    }
  }
)

onMounted(async () => {
  syncFromQuery()
  if (store.activeTab === 'confirmed') {
    await store.fetchConfirmedHistory()
  } else {
    await store.fetchPendingMatches()
  }
  await pendingHelper.fetchPendingCount(true)
})

function handleTabChange(tab: 'confirmed' | 'pending') {
  store.setTab(tab)
}

function handleConfirm(matchId: string, matchNumber: number) {
  confirmationStore.commitConfirmation(matchId, matchNumber)
}

function handleReject(matchId: string) {
  rejectingMatchId.value = matchId
  isRejectModalOpen.value = true
}

async function handleRejectionSubmit(payload: { reason: string; customReason: string }) {
  if (!rejectingMatchId.value) return
  isSubmittingRejection.value = true
  try {
    const res = await pendingHelper.rejectMatch(
      rejectingMatchId.value,
      payload.reason,
      payload.customReason
    )
    if (res.success) {
      isRejectModalOpen.value = false
      rejectingMatchId.value = null
      await store.fetchPendingMatches()
    }
  } finally {
    isSubmittingRejection.value = false
  }
}

function handleDismissRejection(matchId: string) {
  pendingHelper.collapseMatch(matchId)
}

function handleEditRejection(match: any) {
  router.push({
    path: '/matches/new',
    query: { editMatchId: match.id },
  })
}

async function handleDeleteRejection(matchId: string) {
  await pendingHelper.deleteMatch(matchId)
  await store.fetchPendingMatches()
}

function handleCloseMatch(matchId: string) {
  pendingHelper.collapseMatch(matchId)
}

function handleUndo() {
  if (confirmationStore.pendingConfirmation) {
    confirmationStore.cancelConfirmationTimer(confirmationStore.pendingConfirmation.matchId)
  }
}
</script>

<template>
  <div class="w-full max-w-2xl mx-auto px-4 py-6 flex flex-col gap-6" data-testid="my-matches-view">
    <!-- View Header -->
    <div class="flex items-center justify-between">
      <h1 class="font-headline text-2xl font-black text-on-surface tracking-tight" data-testid="history-title">
        {{ t('history.title', 'My Matches') }}
      </h1>

      <RouterLink
        to="/matches/new"
        class="inline-flex items-center gap-1 px-3.5 py-2 rounded-xl bg-primary text-background font-headline font-bold text-xs uppercase tracking-wider shadow-md hover:opacity-90 active:scale-95 transition-all"
        data-testid="header-new-match-btn"
      >
        <span class="material-symbols-outlined text-sm font-bold">add</span>
        <span>{{ t('match.submit', 'Submit Match') }}</span>
      </RouterLink>
    </div>

    <!-- Tab Navigation -->
    <div
      class="w-full flex p-1 rounded-2xl bg-surface-container-low select-none"
      role="tablist"
      aria-label="Match History Tabs"
      data-testid="history-tablist"
    >
      <!-- Confirmed Tab -->
      <button
        type="button"
        role="tab"
        :aria-selected="store.activeTab === 'confirmed'"
        class="flex-1 py-2.5 rounded-xl font-headline font-bold text-xs uppercase tracking-wider transition-all cursor-pointer flex items-center justify-center gap-2"
        :class="
          store.activeTab === 'confirmed'
            ? 'bg-surface-container-highest text-on-surface shadow-md'
            : 'text-on-surface-variant hover:text-on-surface'
        "
        @click="handleTabChange('confirmed')"
        data-testid="tab-confirmed"
      >
        <span>{{ t('history.tabs.confirmed', 'Confirmed') }}</span>
      </button>

      <!-- Pending Tab -->
      <button
        type="button"
        role="tab"
        :aria-selected="store.activeTab === 'pending'"
        class="flex-1 py-2.5 rounded-xl font-headline font-bold text-xs uppercase tracking-wider transition-all cursor-pointer flex items-center justify-center gap-2"
        :class="
          store.activeTab === 'pending'
            ? 'bg-surface-container-highest text-on-surface shadow-md'
            : 'text-on-surface-variant hover:text-on-surface'
        "
        @click="handleTabChange('pending')"
        data-testid="tab-pending"
      >
        <span>{{ t('history.tabs.pending', 'Pending') }}</span>
        <span
          v-if="pendingHelper.pendingCount.value > 0"
          class="px-1.5 py-0.5 rounded-full text-[10px] font-black bg-warning/20 text-warning"
          data-testid="pending-badge-count"
        >
          {{ pendingHelper.pendingCount.value }}
        </span>
      </button>
    </div>

    <!-- Confirmed Matches View -->
    <div v-if="store.activeTab === 'confirmed'" class="w-full flex flex-col gap-4">
      <MatchFilterChips />
      <MatchHistoryList />
    </div>

    <!-- Pending Matches View -->
    <div v-else class="w-full flex flex-col gap-4">
      <template v-if="store.pendingMatches.length > 0">
        <PendingMatches
          :pending-matches="store.pendingMatches"
          :pending-confirmation-id="confirmationStore.pendingConfirmation?.matchId"
          :pending-confirmation-ids="confirmationStore.pendingConfirmationIds"
          @confirm="handleConfirm"
          @reject="handleReject"
          @dismiss-rejection="handleDismissRejection"
          @edit-rejection="handleEditRejection"
          @delete-rejection="handleDeleteRejection"
          @close="handleCloseMatch"
        />
      </template>

      <!-- Pending Empty State -->
      <div
        v-else
        class="w-full flex flex-col items-center justify-center p-12 bg-surface-container-low rounded-2xl text-center space-y-4 shadow-xl"
        data-testid="pending-empty-state"
      >
        <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-surface-container-highest text-emerald-400">
          <span class="material-symbols-outlined text-3xl">task_alt</span>
        </div>
        <div class="space-y-1">
          <h2 class="font-headline text-lg font-bold text-on-surface">
            {{ t('history.empty.pendingTitle', 'All caught up') }}
          </h2>
          <p class="text-xs text-on-surface-variant max-w-xs">
            {{ t('history.empty.pendingSubtitle', 'No pending match confirmations') }}
          </p>
        </div>
      </div>
    </div>

    <!-- Rejection Modal -->
    <RejectReasonSelector
      :is-open="isRejectModalOpen"
      :is-submitting="isSubmittingRejection"
      @submit="handleRejectionSubmit"
      @cancel="isRejectModalOpen = false"
    />

    <!-- Undo Toast Notification -->
    <UndoToast
      :is-visible="confirmationStore.isPending"
      :countdown="confirmationStore.countdown"
      :message="t('match.confirmedTapUndo')"
      @undo="handleUndo"
    />
  </div>
</template>

<style scoped>
</style>
