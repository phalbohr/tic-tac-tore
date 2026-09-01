<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import type { TournamentRegistrationDto } from '@/features/tournament/types/tournament';

interface Props {
  isOpen: boolean;
  invite: TournamentRegistrationDto | null;
  loading?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
});

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'accept', invite: TournamentRegistrationDto): void;
  (e: 'decline', invite: TournamentRegistrationDto): void;
}>();

const { t } = useI18n();

function handleAccept() {
  if (props.invite) {
    emit('accept', props.invite);
  }
}

function handleDecline() {
  if (props.invite) {
    emit('decline', props.invite);
  }
}

function handleClose() {
  emit('close');
}
</script>

<template>
  <div
    v-if="isOpen && invite"
    data-testid="tournament-invite-modal"
    class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs"
    @click.self="handleClose"
  >
    <div
      class="w-full max-w-md bg-surface-container-low rounded-2xl shadow-xl overflow-hidden animate-in fade-in zoom-in-95 duration-150"
    >
      <div class="px-6 py-5 border-b border-outline-variant/10">
        <h2 class="text-xl font-bold text-on-surface">
          {{ t('tournament.registration.inviteModalTitle') }}
        </h2>
      </div>

      <div class="px-6 py-6 space-y-4">
        <div class="p-4 rounded-xl bg-surface-container-high space-y-2">
          <div class="text-sm font-semibold text-primary">
            {{ invite.tournamentName }}
          </div>
          <p class="text-sm text-on-surface">
            {{ t('tournament.registration.invitedBy', { inviter: invite.playerNickname, tournament: invite.tournamentName }) }}
          </p>
        </div>
      </div>

      <div class="px-6 py-4 flex items-center justify-end gap-3 bg-surface-container-low border-t border-outline-variant/10">
        <button
          data-testid="decline-invite-btn"
          type="button"
          :disabled="loading"
          class="px-4 py-2 text-sm font-medium text-error hover:bg-error/10 disabled:opacity-50 rounded-xl transition-colors"
          @click="handleDecline"
        >
          {{ t('tournament.decline') }}
        </button>
        <button
          data-testid="accept-invite-btn"
          type="button"
          :disabled="loading"
          class="px-5 py-2 text-sm font-medium text-on-primary bg-primary hover:bg-primary/90 disabled:opacity-50 rounded-xl transition-colors shadow-sm flex items-center gap-2"
          @click="handleAccept"
        >
          <span v-if="loading" class="inline-block w-4 h-4 border-2 border-on-primary border-t-transparent rounded-full animate-spin"></span>
          {{ t('tournament.accept') }}
        </button>
      </div>
    </div>
  </div>
</template>
