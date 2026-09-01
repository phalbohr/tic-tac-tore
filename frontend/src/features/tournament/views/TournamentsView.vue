<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useTournamentStore } from '@/features/tournament/stores/tournamentStore';
import type {
  TournamentDto,
  TournamentRegistrationDto,
  RegisterTournamentPayload,
} from '@/features/tournament/types/tournament';
import TournamentRegistrationModal from '@/features/tournament/components/TournamentRegistrationModal.vue';
import TournamentInviteModal from '@/features/tournament/components/TournamentInviteModal.vue';

const { t } = useI18n();
const tournamentStore = useTournamentStore();

const selectedTournament = ref<TournamentDto | null>(null);
const isRegistrationModalOpen = ref(false);

const selectedInvite = ref<TournamentRegistrationDto | null>(null);
const isInviteModalOpen = ref(false);

const toastMessage = ref('');
const isSubmitting = ref(false);

function showToast(msg: string) {
  toastMessage.value = msg;
  setTimeout(() => {
    toastMessage.value = '';
  }, 4000);
}

onMounted(async () => {
  await Promise.allSettled([
    tournamentStore.fetchTournaments(),
    tournamentStore.fetchPendingInvitations(),
  ]);

  for (const tourn of tournamentStore.tournaments) {
    tournamentStore.fetchMyRegistration(tourn.id).catch(() => {});
  }
});

function openRegistration(tourn: TournamentDto) {
  selectedTournament.value = tourn;
  isRegistrationModalOpen.value = true;
}

function openInvite(invite: TournamentRegistrationDto) {
  selectedInvite.value = invite;
  isInviteModalOpen.value = true;
}

async function handleRegister(payload: RegisterTournamentPayload) {
  if (!selectedTournament.value) return;
  isSubmitting.value = true;
  try {
    const result = await tournamentStore.register(selectedTournament.value.id, payload);
    isRegistrationModalOpen.value = false;
    if (result.status === 'PENDING_CONFIRMATION') {
      showToast(t('tournament.registration.successPartner'));
    } else {
      showToast(t('tournament.registration.successSolo'));
    }
  } catch (err: unknown) {
    showToast(err instanceof Error ? err.message : 'Registration failed');
  } finally {
    isSubmitting.value = false;
  }
}

async function handleAcceptInvite(invite: TournamentRegistrationDto) {
  isSubmitting.value = true;
  try {
    await tournamentStore.acceptInvite(invite.tournamentId, invite.id);
    isInviteModalOpen.value = false;
    showToast(t('tournament.registration.acceptSuccess'));
  } catch (err: unknown) {
    showToast(err instanceof Error ? err.message : 'Failed to accept');
  } finally {
    isSubmitting.value = false;
  }
}

async function handleDeclineInvite(invite: TournamentRegistrationDto) {
  isSubmitting.value = true;
  try {
    await tournamentStore.declineInvite(invite.tournamentId, invite.id);
    isInviteModalOpen.value = false;
    showToast(t('tournament.registration.declineSuccess'));
  } catch (err: unknown) {
    showToast(err instanceof Error ? err.message : 'Failed to decline');
  } finally {
    isSubmitting.value = false;
  }
}

async function handleWithdraw(tournamentId: string, registrationId: string) {
  try {
    await tournamentStore.cancelRegistration(tournamentId, registrationId);
    showToast(t('tournament.registration.cancelSuccess'));
  } catch (err: unknown) {
    showToast(err instanceof Error ? err.message : 'Failed to withdraw');
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
              {{ t('tournament.registration.invitedBy', { inviter: inv.playerNickname, tournament: inv.tournamentName }) }}
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

    <div v-else-if="tournamentStore.tournaments.length === 0" class="text-center py-12 text-on-surface-variant">
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
            <div class="text-xs text-on-surface-variant">
              {{ tourn.format }} • {{ tourn.mode }}
            </div>
          </div>

          <!-- Status badge -->
          <div>
            <span
              v-if="tournamentStore.myRegistrations[tourn.id]?.isRegistered"
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
        </div>

        <div class="pt-2 border-t border-outline-variant/10 flex items-center justify-end gap-2">
          <button
            v-if="!tournamentStore.myRegistrations[tourn.id]?.isRegistered && tourn.status === 'REGISTRATION_OPEN'"
            data-testid="register-tournament-btn"
            type="button"
            class="px-4 py-2 text-xs font-semibold text-on-primary bg-primary hover:bg-primary/90 rounded-xl transition-colors shadow-sm"
            @click="openRegistration(tourn)"
          >
            {{ t('tournament.register') }}
          </button>

          <button
            v-else-if="tournamentStore.myRegistrations[tourn.id]?.isRegistered && tourn.status === 'REGISTRATION_OPEN'"
            data-testid="withdraw-registration-btn"
            type="button"
            class="px-3 py-1.5 text-xs font-medium text-error hover:bg-error/10 rounded-xl transition-colors"
            @click="
              tournamentStore.myRegistrations[tourn.id]?.registration &&
              handleWithdraw(
                tourn.id,
                tournamentStore.myRegistrations[tourn.id]!.registration!.id
              )
            "
          >
            {{ t('tournament.withdraw') }}
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
  </div>
</template>
