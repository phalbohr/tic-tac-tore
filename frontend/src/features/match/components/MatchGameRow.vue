<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import AvatarBase from '@/components/AvatarBase.vue'

export interface PlayerDisplayInfo {
  name: string
  avatar?: string | null
}

withDefaults(
  defineProps<{
    teamADefender?: PlayerDisplayInfo
    teamAAttacker?: PlayerDisplayInfo
    teamBDefender?: PlayerDisplayInfo
    teamBAttacker?: PlayerDisplayInfo
    teamAScore?: number
    teamBScore?: number
    showScore?: boolean
    teamALabel?: string
    teamBLabel?: string
  }>(),
  {
    showScore: true
  }
)

const { t } = useI18n()
</script>

<template>
  <div class="match-game-row w-full flex flex-col gap-1.5 bg-surface-container/60 p-3 rounded-xl">
    <div class="flex items-center justify-between gap-2">
      <!-- Team A Avatars (Defender first on left, then Attacker) -->
      <div class="flex items-center gap-1.5 shrink-0">
        <div class="w-8 h-8 rounded-full overflow-hidden" :title="teamADefender?.name || 'Team A Defender'" data-testid="team-a-defender-avatar">
          <AvatarBase :name="teamADefender?.name" :avatar="teamADefender?.avatar" />
        </div>
        <div class="w-8 h-8 rounded-full overflow-hidden" :title="teamAAttacker?.name || 'Team A Attacker'" data-testid="team-a-attacker-avatar">
          <AvatarBase :name="teamAAttacker?.name" :avatar="teamAAttacker?.avatar" />
        </div>
      </div>

      <!-- Center: Score or VS -->
      <div class="flex-1 text-center font-headline font-bold text-base text-primary leading-tight px-2" data-testid="score-vs-display">
        <template v-if="showScore && teamAScore !== undefined && teamBScore !== undefined">
          {{ teamAScore }} : {{ teamBScore }}
        </template>
        <template v-else>
          {{ t('match.vs', 'VS') }}
        </template>
      </div>

      <!-- Team B Avatars (Defender first on left, then Attacker) -->
      <div class="flex items-center gap-1.5 shrink-0">
        <div class="w-8 h-8 rounded-full overflow-hidden" :title="teamBDefender?.name || 'Team B Defender'" data-testid="team-b-defender-avatar">
          <AvatarBase :name="teamBDefender?.name" :avatar="teamBDefender?.avatar" />
        </div>
        <div class="w-8 h-8 rounded-full overflow-hidden" :title="teamBAttacker?.name || 'Team B Attacker'" data-testid="team-b-attacker-avatar">
          <AvatarBase :name="teamBAttacker?.name" :avatar="teamBAttacker?.avatar" />
        </div>
      </div>
    </div>

    <!-- Labels moved to header outside of row component -->
  </div>
</template>
