<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { TournamentStandingDto, TournamentFormat } from '@/features/tournament/types/tournament'
import AvatarBase from '@/components/AvatarBase.vue'

const props = withDefaults(
  defineProps<{
    standings: TournamentStandingDto[]
    isCompleted?: boolean
    format?: TournamentFormat
  }>(),
  {
    isCompleted: false,
  },
)

const { t } = useI18n()

const sortedStandings = computed(() => {
  return [...props.standings].sort((a, b) => (a.rank ?? 999) - (b.rank ?? 999))
})

function formatGameDiff(diff: number): string {
  if (diff > 0) return `+${diff}`
  return `${diff}`
}
</script>

<template>
  <div class="space-y-4">
    <div
      v-if="sortedStandings.length === 0"
      class="text-center py-12 text-on-surface-variant bg-surface-container-low rounded-2xl"
    >
      {{ t('tournament.standings.empty') }}
    </div>

    <div
      v-else
      class="overflow-hidden rounded-2xl bg-surface-container-low shadow-sm border border-outline-variant/10"
    >
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse text-sm">
          <thead>
            <tr class="border-b border-outline-variant/10 text-xs font-semibold text-on-surface-variant uppercase tracking-wider bg-surface-container/50">
              <th scope="col" class="py-3.5 px-4 w-12 text-center">
                {{ t('tournament.standings.rank') }}
              </th>
              <th scope="col" class="py-3.5 px-4">
                {{ t('tournament.standings.player') }}
              </th>
              <th scope="col" class="py-3.5 px-3 text-center">
                {{ t('tournament.standings.played') }}
              </th>
              <th scope="col" class="py-3.5 px-3 text-center">
                {{ t('tournament.standings.wins') }}
              </th>
              <th scope="col" class="py-3.5 px-3 text-center">
                {{ t('tournament.standings.losses') }}
              </th>
              <th scope="col" class="py-3.5 px-3 text-center">
                {{ t('tournament.standings.gamesWon') }}
              </th>
              <th scope="col" class="py-3.5 px-3 text-center">
                {{ t('tournament.standings.gamesLost') }}
              </th>
              <th scope="col" class="py-3.5 px-3 text-center">
                {{ t('tournament.standings.gameDiff') }}
              </th>
              <th scope="col" class="py-3.5 px-3 text-center font-bold text-on-surface">
                {{ t('tournament.standings.points') }}
              </th>
              <th scope="col" class="py-3.5 px-4 text-right">
                {{ t('tournament.standings.status') }}
              </th>
            </tr>
          </thead>
          <tbody class="divide-y divide-outline-variant/5">
            <tr
              v-for="standing in sortedStandings"
              :key="standing.registrationId"
              class="transition-colors hover:bg-surface-container-high/40"
              :class="{
                'bg-amber-500/5': isCompleted && standing.rank === 1,
              }"
            >
              <!-- Rank -->
              <td class="py-3.5 px-4 text-center font-bold text-on-surface">
                <span
                  v-if="standing.rank === 1"
                  class="inline-flex items-center justify-center w-6 h-6 rounded-full bg-amber-500 text-black font-extrabold text-xs shadow-sm"
                >
                  1
                </span>
                <span
                  v-else-if="standing.rank === 2"
                  class="inline-flex items-center justify-center w-6 h-6 rounded-full bg-slate-300 text-black font-bold text-xs"
                >
                  2
                </span>
                <span
                  v-else-if="standing.rank === 3"
                  class="inline-flex items-center justify-center w-6 h-6 rounded-full bg-amber-700/60 text-white font-bold text-xs"
                >
                  3
                </span>
                <span v-else class="text-on-surface-variant font-medium">
                  {{ standing.rank }}
                </span>
              </td>

              <!-- Player / Team -->
              <td class="py-3.5 px-4">
                <div class="flex items-center gap-3">
                  <div class="flex -space-x-2 shrink-0">
                    <div class="w-8 h-8 rounded-full ring-2 ring-surface">
                      <AvatarBase
                        :avatar="standing.avatarUrl"
                        :name="standing.nickname"
                        shape="circle"
                      />
                    </div>
                    <div
                      v-if="standing.partnerNickname"
                      class="w-8 h-8 rounded-full ring-2 ring-surface"
                    >
                      <AvatarBase
                        :avatar="standing.partnerAvatarUrl"
                        :name="standing.partnerNickname"
                        shape="circle"
                      />
                    </div>
                  </div>

                  <div class="flex flex-col">
                    <span class="font-semibold text-on-surface">
                      {{ standing.nickname }}
                    </span>
                    <span
                      v-if="standing.partnerNickname"
                      class="text-xs text-on-surface-variant"
                    >
                      + {{ standing.partnerNickname }}
                    </span>
                  </div>
                </div>
              </td>

              <!-- Matches Played -->
              <td class="py-3.5 px-3 text-center text-on-surface font-medium">
                {{ standing.matchesPlayed }}
              </td>

              <!-- Wins -->
              <td class="py-3.5 px-3 text-center text-emerald-600 dark:text-emerald-400 font-semibold">
                {{ standing.wins }}
              </td>

              <!-- Losses -->
              <td class="py-3.5 px-3 text-center text-rose-500 font-medium">
                {{ standing.losses }}
              </td>

              <!-- Games Won -->
              <td class="py-3.5 px-3 text-center text-on-surface font-medium">
                {{ standing.gamesWon }}
              </td>

              <!-- Games Lost -->
              <td class="py-3.5 px-3 text-center text-on-surface font-medium">
                {{ standing.gamesLost }}
              </td>

              <!-- Game Difference -->
              <td
                class="py-3.5 px-3 text-center font-medium"
                :class="{
                  'text-emerald-600 dark:text-emerald-400': standing.gameDifference > 0,
                  'text-rose-500': standing.gameDifference < 0,
                  'text-on-surface-variant': standing.gameDifference === 0,
                }"
              >
                {{ formatGameDiff(standing.gameDifference) }}
              </td>

              <!-- Points -->
              <td class="py-3.5 px-3 text-center font-bold text-on-surface text-base">
                {{ standing.points }}
              </td>

              <!-- Status -->
              <td class="py-3.5 px-4 text-right">
                <span
                  v-if="isCompleted && standing.rank === 1"
                  data-testid="standing-badge-winner"
                  class="inline-flex items-center gap-1 text-xs font-bold px-2.5 py-1 rounded-full bg-amber-500/20 text-amber-600 dark:text-amber-400 border border-amber-500/30"
                >
                  🏆 {{ t('tournament.standings.winnerBadge') }}
                </span>
                <span
                  v-else-if="standing.isEliminated"
                  data-testid="standing-badge-eliminated"
                  class="inline-flex items-center text-xs font-medium px-2.5 py-1 rounded-full bg-rose-500/10 text-rose-600 dark:text-rose-400 border border-rose-500/20"
                >
                  {{ t('tournament.standings.eliminatedBadge') }}
                </span>
                <span
                  v-else-if="isCompleted"
                  data-testid="standing-badge-completed"
                  class="inline-flex items-center text-xs font-medium px-2.5 py-1 rounded-full bg-surface-container-high text-on-surface border border-outline-variant/10"
                >
                  {{ t('tournament.standings.completedBadge') }}
                </span>
                <span
                  v-else
                  data-testid="standing-badge-active"
                  class="inline-flex items-center text-xs font-medium px-2.5 py-1 rounded-full bg-primary/10 text-primary border border-primary/20"
                >
                  {{ t('tournament.standings.activeBadge') }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
