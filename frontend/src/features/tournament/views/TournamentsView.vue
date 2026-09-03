<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useTournamentStore } from '@/features/tournament/stores/tournamentStore'
import { useAuthStore } from '@/stores/auth'
import type {
  TournamentDto,
  TournamentRegistrationDto,
  RegisterTournamentPayload,
  TournamentMatchDto,
} from '@/features/tournament/types/tournament'
import TournamentRegistrationModal from '@/features/tournament/components/TournamentRegistrationModal.vue'
import TournamentInviteModal from '@/features/tournament/components/TournamentInviteModal.vue'
import TournamentBracket from '@/features/tournament/components/TournamentBracket.vue'
import TournamentSchedule from '@/features/tournament/components/TournamentSchedule.vue'
import TournamentStandings from '@/features/tournament/components/TournamentStandings.vue'

const { t } = useI18n()
const router = useRouter()
const authStore = useAuthStore()
const tournamentStore = useTournamentStore()
const currentUserId = computed(() => authStore.profile?.id)

const activeMainTab = ref<'active' | 'archive'>('active')
const activeModalTab = ref<'bracket' | 'standings'>('bracket')

const selectedTournament = ref<TournamentDto | null>(null)
const isRegistrationModalOpen = ref(false)

const selectedInvite = ref<TournamentRegistrationDto | null>(null)
const isInviteModalOpen = ref(false)

const activeBracketTournament = ref<TournamentDto | null>(null)
const isBracketModalOpen = ref(false)

const activeBracket = computed(() => {
  if (!activeBracketTournament.value) return null
  return tournamentStore.brackets[activeBracketTournament.value.id] ?? null
})

const activeStandings = computed(() => {
  if (!activeBracketTournament.value) return []
  return tournamentStore.standings[activeBracketTournament.value.id] ?? []
})

const toastMessage = ref('')
const isSubmitting = ref(false)

function showToast(msg: string) {
  toastMessage.value = msg
  setTimeout(() => {
    toastMessage.value = ''
  }, 4000)
}

onMounted(async () => {
  await Promise.allSettled([
    authStore.fetchProfile(),
    tournamentStore.fetchTournaments(),
    tournamentStore.fetchPendingInvitations(),
  ])

  for (const tourn of tournamentStore.tournaments) {
    tournamentStore.fetchMyRegistration(tourn.id).catch(() => {})
  }
})

async function selectArchiveTab() {
  activeMainTab.value = 'archive'
  if (tournamentStore.archiveTournaments.length === 0) {
    try {
      await tournamentStore.fetchArchive(0, 10)
    } catch (err: unknown) {
      showToast(err instanceof Error ? err.message : 'Failed to load archive')
    }
  }
}

async function changeArchivePage(newPage: number) {
  if (newPage < 0 || (tournamentStore.archiveTotalPages > 0 && newPage >= tournamentStore.archiveTotalPages)) {
    return
  }
  try {
    await tournamentStore.fetchArchive(newPage, 10)
  } catch (err: unknown) {
    showToast(err instanceof Error ? err.message : 'Failed to load page')
  }
}

function openRegistration(tourn: TournamentDto) {
  selectedTournament.value = tourn
  isRegistrationModalOpen.value = true
}

function openInvite(invite: TournamentRegistrationDto) {
  selectedInvite.value = invite
  isInviteModalOpen.value = true
}

async function openBracket(tourn: TournamentDto, defaultTab: 'bracket' | 'standings' = 'bracket') {
  activeBracketTournament.value = tourn
  activeModalTab.value = tourn.status === 'COMPLETED' ? 'standings' : defaultTab
  try {
    const results = await Promise.allSettled([
      tournamentStore.fetchBracket(tourn.id),
      tournamentStore.fetchStandings(tourn.id),
    ])
    const errors = results
      .filter((r): r is PromiseRejectedResult => r.status === 'rejected')
      .map((r) => (r.reason instanceof Error ? r.reason.message : 'Failed to load tournament data'))

    if (errors.length === results.length) {
      showToast(errors[0] ?? 'Failed to load tournament details')
      return
    }
    if (errors.length > 0 && errors[0]) {
      showToast(errors[0])
    }
    isBracketModalOpen.value = true
  } catch (err: unknown) {
    showToast(err instanceof Error ? err.message : 'Failed to load tournament details')
  }
}

async function switchModalTab(tab: 'bracket' | 'standings') {
  activeModalTab.value = tab
  if (tab === 'standings' && activeBracketTournament.value) {
    try {
      await tournamentStore.fetchStandings(activeBracketTournament.value.id)
    } catch (err: unknown) {
      showToast(err instanceof Error ? err.message : 'Failed to load standings')
    }
  }
}

async function handleStartTournament(tourn: TournamentDto) {
  isSubmitting.value = true
  try {
    const updated = await tournamentStore.startTournament(tourn.id)
    showToast(t('tournament.startedSuccess'))
    await openBracket(updated)
  } catch (err: unknown) {
    showToast(err instanceof Error ? err.message : 'Failed to start tournament')
  } finally {
    isSubmitting.value = false
  }
}

async function handleRegister(payload: RegisterTournamentPayload) {
  if (!selectedTournament.value) return
  isSubmitting.value = true
  try {
    const result = await tournamentStore.register(selectedTournament.value.id, payload)
    isRegistrationModalOpen.value = false
    if (result.status === 'PENDING_CONFIRMATION') {
      showToast(t('tournament.registration.successPartner'))
    } else {
      showToast(t('tournament.registration.successSolo'))
    }
  } catch (err: unknown) {
    showToast(err instanceof Error ? err.message : 'Registration failed')
  } finally {
    isSubmitting.value = false
  }
}

async function handleAcceptInvite(invite: TournamentRegistrationDto) {
  isSubmitting.value = true
  try {
    await tournamentStore.acceptInvite(invite.tournamentId, invite.id)
    isInviteModalOpen.value = false
    showToast(t('tournament.registration.acceptSuccess'))
  } catch (err: unknown) {
    showToast(err instanceof Error ? err.message : 'Failed to accept')
  } finally {
    isSubmitting.value = false
  }
}

async function handleDeclineInvite(invite: TournamentRegistrationDto) {
  isSubmitting.value = true
  try {
    await tournamentStore.declineInvite(invite.tournamentId, invite.id)
    isInviteModalOpen.value = false
    showToast(t('tournament.registration.declineSuccess'))
  } catch (err: unknown) {
    showToast(err instanceof Error ? err.message : 'Failed to decline')
  } finally {
    isSubmitting.value = false
  }
}

async function handleWithdraw(tournamentId: string, registrationId: string) {
  try {
    await tournamentStore.cancelRegistration(tournamentId, registrationId)
    showToast(t('tournament.registration.cancelSuccess'))
  } catch (err: unknown) {
    showToast(err instanceof Error ? err.message : 'Failed to withdraw')
  }
}

async function handleStartMatch(matchId: string) {
  if (!activeBracketTournament.value) return
  try {
    await tournamentStore.startMatch(activeBracketTournament.value.id, matchId)
    isBracketModalOpen.value = false

    let matchDto: TournamentMatchDto | undefined
    if (activeBracket.value) {
      for (const round of activeBracket.value.rounds) {
        const found = round.matches.find((m) => m.id === matchId)
        if (found) {
          matchDto = found
          break
        }
      }
    }

    const is2v2 = activeBracketTournament.value.mode !== 'ONE_VS_ONE_PERSONAL'
    const playerIds: string[] = []
    if (matchDto) {
      const p1Id = matchDto.participant1?.playerId
      const p1PartnerId = matchDto.participant1Partner?.playerId || matchDto.participant1?.partnerId
      const p2Id = matchDto.participant2?.playerId
      const p2PartnerId = matchDto.participant2Partner?.playerId || matchDto.participant2?.partnerId

      if (is2v2) {
        if (p1PartnerId) playerIds.push(p1PartnerId)
        if (p1Id) playerIds.push(p1Id)
        if (p2PartnerId) playerIds.push(p2PartnerId)
        if (p2Id) playerIds.push(p2Id)
      } else {
        if (p1Id) playerIds.push(p1Id)
        if (p2Id) playerIds.push(p2Id)
      }
    }

    router.push({
      path: '/matches/new',
      query: {
        tournamentId: activeBracketTournament.value.id,
        tournamentMatchId: matchId,
        ruleConfigId: activeBracketTournament.value.ruleConfiguration?.id,
        ruleSystemName: activeBracketTournament.value.ruleConfiguration?.name,
        matchType: is2v2 ? '2v2' : '1v1',
        ...(playerIds.length > 0 ? { playerIds: playerIds.join(',') } : {}),
      },
    })
  } catch (err: unknown) {
    showToast(err instanceof Error ? err.message : 'Failed to start match')
  }
}
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 py-6 space-y-6">
    <!-- Toast Notification -->
    <div
      v-if="toastMessage"
      data-testid="tournament-toast"
      class="fixed bottom-5 right-5 z-50 px-4 py-3 bg-inverse-surface text-inverse-on-surface rounded-xl shadow-lg text-sm font-medium animate-in fade-in slide-in-from-bottom-2"
    >
      {{ toastMessage }}
    </div>

    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-on-surface">
          {{ t('tournament.title') }}
        </h1>
        <p class="text-sm text-on-surface-variant">
          {{ t('tournament.subtitle') }}
        </p>
      </div>
    </div>

    <!-- Segmented Navigation Tabs -->
    <div class="flex items-center p-1 rounded-2xl bg-surface-container-low border border-outline-variant/10 w-fit">
      <button
        type="button"
        data-testid="tab-active"
        class="px-4 py-2 text-xs font-semibold rounded-xl transition-all"
        :class="
          activeMainTab === 'active'
            ? 'bg-primary text-on-primary shadow-sm'
            : 'text-on-surface-variant hover:text-on-surface'
        "
        @click="activeMainTab = 'active'"
      >
        {{ t('tournament.tabs.active') }}
      </button>
      <button
        type="button"
        data-testid="tab-archive"
        class="px-4 py-2 text-xs font-semibold rounded-xl transition-all"
        :class="
          activeMainTab === 'archive'
            ? 'bg-primary text-on-primary shadow-sm'
            : 'text-on-surface-variant hover:text-on-surface'
        "
        @click="selectArchiveTab"
      >
        {{ t('tournament.tabs.archive') }}
      </button>
    </div>

    <!-- Active Tournaments View -->
    <div v-if="activeMainTab === 'active'" class="space-y-6">
      <!-- Pending Invitations Banner -->
      <div
        v-if="tournamentStore.pendingInvitations.length > 0"
        data-testid="pending-invitations-banner"
        class="p-4 rounded-2xl bg-amber-500/10 border border-amber-500/20 space-y-3"
      >
        <div class="text-sm font-semibold text-amber-600 dark:text-amber-400">
          {{ t('tournament.pendingInvitationsTitle') }}
        </div>
        <div class="space-y-2">
          <div
            v-for="inv in tournamentStore.pendingInvitations"
            :key="inv.id"
            data-testid="pending-invite-card"
            class="p-3 rounded-xl bg-surface-container-low flex items-center justify-between"
          >
            <div>
              <div class="text-sm font-medium text-on-surface">
                {{ inv.tournamentName }}
              </div>
              <div class="text-xs text-on-surface-variant">
                {{
                  t('tournament.registration.invitedBy', {
                    inviter: inv.playerNickname,
                    tournament: inv.tournamentName,
                  })
                }}
              </div>
            </div>
            <button
              type="button"
              data-testid="respond-invite-btn"
              class="px-3 py-1.5 text-xs font-medium text-on-primary bg-primary rounded-lg hover:bg-primary/90 transition-colors"
              @click="openInvite(inv)"
            >
              {{ t('tournament.accept') }} / {{ t('tournament.decline') }}
            </button>
          </div>
        </div>
      </div>

      <!-- Tournaments List -->
      <div v-if="tournamentStore.isLoading" class="text-center py-12 text-on-surface-variant">
        {{ t('common.loading') }}
      </div>

      <div
        v-else-if="tournamentStore.tournaments.length === 0"
        class="text-center py-12 text-on-surface-variant bg-surface-container-low rounded-2xl"
      >
        {{ t('tournament.empty') }}
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div
          v-for="tourn in tournamentStore.tournaments"
          :key="tourn.id"
          data-testid="tournament-card"
          class="p-5 rounded-2xl bg-surface-container-low shadow-sm hover:shadow-md transition-shadow space-y-4"
        >
          <div class="flex items-start justify-between">
            <div>
              <h2 class="text-lg font-bold text-on-surface">
                {{ tourn.name }}
              </h2>
              <div class="text-xs text-on-surface-variant">{{ tourn.format }} • {{ tourn.mode }}</div>
            </div>

            <!-- Status badge -->
            <div class="flex items-center gap-2">
              <span
                v-if="tourn.status === 'CANCELLED'"
                class="text-xs font-semibold px-2.5 py-1 rounded-full bg-error/10 text-error"
                data-testid="tournament-status-badge"
              >
                {{ t('tournament.cancelled') }}
              </span>
              <span
                v-else-if="tourn.status === 'IN_PROGRESS'"
                class="text-xs font-semibold px-2.5 py-1 rounded-full bg-primary/10 text-primary"
                data-testid="tournament-status-badge"
              >
                {{ t('tournament.bracket.live') }}
              </span>
              <span
                v-else-if="tourn.status === 'COMPLETED'"
                class="text-xs font-semibold px-2.5 py-1 rounded-full bg-surface-container-high text-on-surface"
                data-testid="tournament-status-badge"
              >
                {{ t('tournament.bracket.completed') }}
              </span>
              <span
                v-else-if="tournamentStore.myRegistrations[tourn.id]?.isRegistered"
                class="text-xs font-semibold px-2.5 py-1 rounded-full bg-emerald-500/10 text-emerald-600 dark:text-emerald-400"
                data-testid="tournament-status-badge"
              >
                {{
                  tournamentStore.myRegistrations[tourn.id]?.isPendingInvite
                    ? t('tournament.pendingInvite')
                    : t('tournament.registered')
                }}
              </span>
            </div>
          </div>

          <div class="text-xs text-on-surface-variant space-y-1">
            <div>Participants: {{ tourn.minParticipants }} - {{ tourn.maxParticipants }}</div>
            <div>Deadline: {{ new Date(tourn.registrationDeadline).toLocaleDateString() }}</div>
            <div v-if="tourn.cancellationReason" class="text-error font-medium">
              {{ tourn.cancellationReason }}
            </div>
          </div>

          <div class="pt-2 border-t border-outline-variant/10 flex items-center justify-end gap-2">
            <!-- View Bracket/Schedule Button for IN_PROGRESS or COMPLETED -->
            <button
              v-if="tourn.status === 'IN_PROGRESS' || tourn.status === 'COMPLETED'"
              :data-testid="
                tourn.format === 'CHAMPIONSHIP' ? 'view-schedule-btn' : 'view-bracket-btn'
              "
              type="button"
              class="px-4 py-2 text-xs font-semibold text-on-primary bg-primary hover:bg-primary/90 rounded-xl transition-colors shadow-sm"
              @click="openBracket(tourn, 'bracket')"
            >
              {{
                tourn.format === 'CHAMPIONSHIP'
                  ? t('tournament.viewSchedule')
                  : t('tournament.viewBracket')
              }}
            </button>

            <!-- Register button -->
            <button
              v-if="
                !tournamentStore.myRegistrations[tourn.id]?.isRegistered &&
                tourn.status === 'REGISTRATION_OPEN'
              "
              data-testid="register-tournament-btn"
              type="button"
              class="px-4 py-2 text-xs font-semibold text-on-primary bg-primary hover:bg-primary/90 rounded-xl transition-colors shadow-sm"
              @click="openRegistration(tourn)"
            >
              {{ t('tournament.register') }}
            </button>

            <!-- Start Tournament button (manual trigger) -->
            <button
              v-if="tourn.status === 'REGISTRATION_OPEN'"
              data-testid="start-tournament-btn"
              type="button"
              class="px-3 py-1.5 text-xs font-medium text-primary hover:bg-primary/10 rounded-xl transition-colors"
              @click="handleStartTournament(tourn)"
            >
              {{ t('tournament.startTournament') }}
            </button>

            <!-- Withdraw button -->
            <button
              v-if="
                tournamentStore.myRegistrations[tourn.id]?.isRegistered &&
                tourn.status === 'REGISTRATION_OPEN'
              "
              data-testid="withdraw-registration-btn"
              type="button"
              class="px-3 py-1.5 text-xs font-medium text-error hover:bg-error/10 rounded-xl transition-colors"
              @click="
                tournamentStore.myRegistrations[tourn.id]?.registration &&
                handleWithdraw(tourn.id, tournamentStore.myRegistrations[tourn.id]!.registration!.id)
              "
            >
              {{ t('tournament.withdraw') }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Archive Tournaments View -->
    <div v-else-if="activeMainTab === 'archive'" class="space-y-6">
      <div v-if="tournamentStore.isArchiveLoading" class="text-center py-12 text-on-surface-variant">
        {{ t('common.loading') }}
      </div>

      <div
        v-else-if="tournamentStore.archiveTournaments.length === 0"
        class="text-center py-12 text-on-surface-variant bg-surface-container-low rounded-2xl"
      >
        {{ t('tournament.archive.empty') }}
      </div>

      <div v-else class="space-y-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div
            v-for="tourn in tournamentStore.archiveTournaments"
            :key="tourn.id"
            data-testid="tournament-card"
            class="p-5 rounded-2xl bg-surface-container-low shadow-sm hover:shadow-md transition-shadow space-y-4 border border-outline-variant/10"
          >
            <div class="flex items-start justify-between">
              <div>
                <h2 class="text-lg font-bold text-on-surface">
                  {{ tourn.name }}
                </h2>
                <div class="text-xs text-on-surface-variant">
                  {{ tourn.format }} • {{ tourn.mode }}
                </div>
              </div>

              <span
                class="text-xs font-semibold px-2.5 py-1 rounded-full bg-surface-container-high text-on-surface"
                data-testid="tournament-status-badge"
              >
                {{ t('tournament.bracket.completed') }}
              </span>
            </div>

            <div class="text-xs text-on-surface-variant space-y-1">
              <div>{{ t('tournament.archive.participants', { count: `${tourn.minParticipants} - ${tourn.maxParticipants}` }) }}</div>
              <div>{{ t('tournament.archive.completedDate', { date: new Date(tourn.updatedAt || tourn.createdAt).toLocaleDateString() }) }}</div>
            </div>

            <div class="pt-2 border-t border-outline-variant/10 flex items-center justify-end gap-2">
              <button
                type="button"
                data-testid="view-bracket-btn"
                class="px-4 py-2 text-xs font-semibold text-on-primary bg-primary hover:bg-primary/90 rounded-xl transition-colors shadow-sm"
                @click="openBracket(tourn, 'standings')"
              >
                {{ t('tournament.archive.viewDetails') }}
              </button>
            </div>
          </div>
        </div>

        <!-- Pagination Controls -->
        <div
          v-if="tournamentStore.archiveTotalPages > 1"
          class="flex items-center justify-between p-4 rounded-2xl bg-surface-container-low border border-outline-variant/10 text-xs font-medium"
        >
          <button
            type="button"
            data-testid="archive-prev-page"
            class="px-3 py-1.5 rounded-xl bg-surface-container hover:bg-surface-container-high disabled:opacity-40 disabled:cursor-not-allowed transition-colors text-on-surface"
            :disabled="tournamentStore.archivePage <= 0"
            @click="changeArchivePage(tournamentStore.archivePage - 1)"
          >
            {{ t('history.pagination.previous') }}
          </button>

          <span class="text-on-surface-variant">
            {{
              t('history.pagination.page', {
                current: tournamentStore.archivePage + 1,
                total: tournamentStore.archiveTotalPages,
              })
            }}
          </span>

          <button
            type="button"
            data-testid="archive-next-page"
            class="px-3 py-1.5 rounded-xl bg-surface-container hover:bg-surface-container-high disabled:opacity-40 disabled:cursor-not-allowed transition-colors text-on-surface"
            :disabled="tournamentStore.archivePage >= tournamentStore.archiveTotalPages - 1"
            @click="changeArchivePage(tournamentStore.archivePage + 1)"
          >
            {{ t('history.pagination.next') }}
          </button>
        </div>
      </div>
    </div>

    <!-- Modals -->
    <TournamentRegistrationModal
      :is-open="isRegistrationModalOpen"
      :tournament="selectedTournament"
      :loading="isSubmitting"
      @close="isRegistrationModalOpen = false"
      @register="handleRegister"
    />

    <TournamentInviteModal
      :is-open="isInviteModalOpen"
      :invite="selectedInvite"
      :loading="isSubmitting"
      @close="isInviteModalOpen = false"
      @accept="handleAcceptInvite"
      @decline="handleDeclineInvite"
    />

    <!-- Bracket & Standings Modal -->
    <div
      v-if="isBracketModalOpen && activeBracketTournament"
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-scrim/40 backdrop-blur-sm"
      data-testid="bracket-modal"
    >
      <div
        class="bg-surface rounded-3xl p-6 max-w-5xl w-full max-h-[90vh] overflow-y-auto space-y-6 shadow-2xl"
      >
        <div class="flex items-center justify-between">
          <!-- Modal Tab Toggle -->
          <div class="flex items-center p-1 rounded-2xl bg-surface-container-low border border-outline-variant/10">
            <button
              v-if="activeBracket"
              type="button"
              data-testid="modal-tab-bracket"
              class="px-3 py-1.5 text-xs font-semibold rounded-xl transition-all"
              :class="
                activeModalTab === 'bracket'
                  ? 'bg-primary text-on-primary shadow-sm'
                  : 'text-on-surface-variant hover:text-on-surface'
              "
              @click="switchModalTab('bracket')"
            >
              {{ t('tournament.standings.bracketTab') }}
            </button>
            <button
              type="button"
              data-testid="modal-tab-standings"
              class="px-3 py-1.5 text-xs font-semibold rounded-xl transition-all"
              :class="
                activeModalTab === 'standings'
                  ? 'bg-primary text-on-primary shadow-sm'
                  : 'text-on-surface-variant hover:text-on-surface'
              "
              @click="switchModalTab('standings')"
            >
              {{ t('tournament.standings.tabTitle') }}
            </button>
          </div>

          <button
            type="button"
            class="p-2 rounded-full hover:bg-surface-container text-on-surface-variant hover:text-on-surface"
            @click="isBracketModalOpen = false"
          >
            ✕
          </button>
        </div>

        <!-- Standings Tab -->
        <TournamentStandings
          v-if="activeModalTab === 'standings'"
          :standings="activeStandings"
          :is-completed="activeBracketTournament.status === 'COMPLETED'"
          :format="activeBracketTournament.format"
        />

        <!-- Bracket / Schedule Tab -->
        <template v-else-if="activeBracket">
          <TournamentSchedule
            v-if="activeBracketTournament.format === 'CHAMPIONSHIP'"
            :bracket="activeBracket"
            :current-user-id="currentUserId"
            @start-match="handleStartMatch"
          />
          <TournamentBracket
            v-else
            :bracket="activeBracket"
            :current-user-id="currentUserId"
            @start-match="handleStartMatch"
          />
        </template>
      </div>
    </div>
  </div>
</template>
