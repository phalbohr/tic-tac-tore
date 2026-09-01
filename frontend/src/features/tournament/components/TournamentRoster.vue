<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import type { TournamentRegistrationDto } from '@/features/tournament/types/tournament';

interface Props {
  registrations: TournamentRegistrationDto[];
  maxParticipants: number;
}

const props = defineProps<Props>();
const { t } = useI18n();

const confirmedCount = computed(() => {
  return props.registrations.filter((r) => r.status === 'CONFIRMED').length;
});
</script>

<template>
  <div data-testid="tournament-roster" class="space-y-4">
    <div class="flex items-center justify-between">
      <h3 class="text-base font-semibold text-on-surface">
        {{ t('tournament.registration.rosterTitle') }}
      </h3>
      <span class="text-xs font-medium px-2.5 py-1 rounded-full bg-surface-container-high text-on-surface-variant">
        {{ t('tournament.registration.teamsCount', { count: confirmedCount, max: maxParticipants }) }}
      </span>
    </div>

    <div v-if="registrations.length === 0" class="text-center py-6 text-sm text-on-surface-variant">
      {{ t('tournament.registration.rosterEmpty') }}
    </div>

    <div v-else class="space-y-2">
      <div
        v-for="reg in registrations"
        :key="reg.id"
        class="p-3 rounded-xl bg-surface-container-low flex items-center justify-between"
      >
        <div class="flex items-center gap-3">
          <div class="flex items-center -space-x-2">
            <div
              class="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center text-xs font-bold text-primary border-2 border-surface"
            >
              {{ reg.playerNickname.charAt(0).toUpperCase() }}
            </div>
            <div
              v-if="reg.partnerNickname"
              class="w-8 h-8 rounded-full bg-secondary/20 flex items-center justify-center text-xs font-bold text-secondary border-2 border-surface"
            >
              {{ reg.partnerNickname.charAt(0).toUpperCase() }}
            </div>
          </div>
          <div>
            <div class="text-sm font-medium text-on-surface">
              {{ reg.playerNickname }}
              <span v-if="reg.partnerNickname" class="text-on-surface-variant">
                &amp; {{ reg.partnerNickname }}
              </span>
            </div>
          </div>
        </div>

        <div>
          <span
            v-if="reg.status === 'CONFIRMED'"
            class="text-xs font-medium px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-600 dark:text-emerald-400"
          >
            {{ t('tournament.confirmed') }}
          </span>
          <span
            v-else-if="reg.status === 'PENDING_CONFIRMATION'"
            class="text-xs font-medium px-2.5 py-0.5 rounded-full bg-amber-500/10 text-amber-600 dark:text-amber-400"
          >
            {{ t('tournament.pendingConfirmation') }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
