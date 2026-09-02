<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import type { TournamentMatchDto } from '@/features/tournament/types/tournament';

interface Props {
  match: TournamentMatchDto;
}

const props = defineProps<Props>();

const isBye = computed(() => props.match.status === 'BYE');

const participant1Name = computed(() => {
  if (!props.match.participant1) return isBye.value ? 'BYE' : 'TBD';
  const p = props.match.participant1;
  const partner = props.match.participant1Partner;
  if (partner) {
    return `${p.playerNickname} & ${partner.playerNickname}`;
  }
  return p.partnerNickname ? `${p.playerNickname} & ${p.partnerNickname}` : p.playerNickname;
});

const participant2Name = computed(() => {
  if (isBye.value) return 'BYE';
  if (!props.match.participant2) return 'TBD';
  const p = props.match.participant2;
  const partner = props.match.participant2Partner;
  if (partner) {
    return `${p.playerNickname} & ${partner.playerNickname}`;
  }
  return p.partnerNickname ? `${p.playerNickname} & ${p.partnerNickname}` : p.playerNickname;
});

const stubLabel = computed(() => {
  try {
    const { t, te } = useI18n();
    if (te('tournament.stub_partner')) {
      return t('tournament.stub_partner');
    }
  } catch {
    // Fallback for environments mounted without i18n
  }
  return 'Stub';
});
</script>

<template>
  <div
    data-testid="tournament-match-card"
    class="p-3.5 rounded-2xl bg-surface-container-low shadow-sm hover:shadow-md transition-shadow space-y-2.5 w-64 text-xs select-none"
  >
    <div class="flex items-center justify-between text-on-surface-variant font-medium pb-1 border-b border-outline-variant/10">
      <span>Match {{ match.matchOrder }}</span>
      <span
        v-if="isBye"
        data-testid="match-status-bye"
        class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/10 text-amber-600 dark:text-amber-400"
      >
        BYE
      </span>
      <span
        v-else-if="match.status === 'READY'"
        class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-600 dark:text-emerald-400"
      >
        READY
      </span>
      <span
        v-else-if="match.status === 'IN_PROGRESS'"
        class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-primary/10 text-primary"
      >
        LIVE
      </span>
      <span
        v-else-if="match.status === 'COMPLETED'"
        class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-surface-container-high text-on-surface"
      >
        DONE
      </span>
      <span
        v-else
        class="px-2 py-0.5 rounded-full text-[10px] font-medium bg-surface-container-high text-on-surface-variant"
      >
        PENDING
      </span>
    </div>

    <!-- Participant 1 -->
    <div
      class="flex items-center justify-between p-2 rounded-xl bg-surface-container transition-colors"
      :class="{ 'font-bold text-primary bg-primary/5': match.winnerRegistrationId && match.participant1 && match.winnerRegistrationId === match.participant1.id }"
    >
      <div class="flex items-center gap-2 truncate">
        <span
          v-if="match.seed1"
          data-testid="participant1-seed"
          class="px-1.5 py-0.5 rounded-md bg-surface-container-high font-mono text-[10px] text-on-surface-variant font-semibold"
        >
          #{{ match.seed1 }}
        </span>
        <div class="w-5 h-5 rounded-full bg-primary/20 flex items-center justify-center text-[10px] font-bold text-primary shrink-0">
          {{ match.participant1 ? match.participant1.playerNickname.charAt(0).toUpperCase() : '?' }}
        </div>
        <span class="truncate font-medium text-on-surface">{{ participant1Name }}</span>
      </div>
      <span
        v-if="match.isParticipant1Stub"
        data-testid="stub-partner-badge"
        class="px-1.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/10 text-amber-600 dark:text-amber-400 shrink-0"
      >
        {{ stubLabel }}
      </span>
    </div>

    <!-- Participant 2 -->
    <div
      class="flex items-center justify-between p-2 rounded-xl bg-surface-container transition-colors"
      :class="{
        'font-bold text-primary bg-primary/5': match.winnerRegistrationId && match.participant2 && match.winnerRegistrationId === match.participant2.id,
        'opacity-60': isBye || !match.participant2
      }"
    >
      <div class="flex items-center gap-2 truncate">
        <span
          v-if="match.seed2"
          data-testid="participant2-seed"
          class="px-1.5 py-0.5 rounded-md bg-surface-container-high font-mono text-[10px] text-on-surface-variant font-semibold"
        >
          #{{ match.seed2 }}
        </span>
        <div class="w-5 h-5 rounded-full bg-secondary/20 flex items-center justify-center text-[10px] font-bold text-secondary shrink-0">
          {{ match.participant2 ? match.participant2.playerNickname.charAt(0).toUpperCase() : '-' }}
        </div>
        <span class="truncate font-medium text-on-surface">{{ participant2Name }}</span>
      </div>
      <span
        v-if="match.isParticipant2Stub"
        data-testid="stub-partner-badge"
        class="px-1.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/10 text-amber-600 dark:text-amber-400 shrink-0"
      >
        {{ stubLabel }}
      </span>
    </div>
  </div>
</template>
