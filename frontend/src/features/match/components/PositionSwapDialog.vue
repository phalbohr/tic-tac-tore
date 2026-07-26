<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import BaseButton from '@/core/components/BaseButton.vue'
import { useMatchDraftStore } from '../stores/matchDraftStore'
import { computed } from 'vue'

defineOptions({
  name: 'PositionSwapDialog'
})

const { t } = useI18n()
const store = useMatchDraftStore()

const getPlayerName = (id?: string) => {
  if (!id) return 'Unknown'
  const opp = store.frequentOpponents.find((o) => o.id === id)
  if (opp) return opp.nickname
  const fetched = store.fetchedPlayers[id]
  if (fetched) return fetched.nickname
  return `Player ${id.substring(0, 4)}`
}

const team1AttackerName = computed(() => getPlayerName(store.currentGame.teamAAttackerId))
const team1DefenderName = computed(() => getPlayerName(store.currentGame.teamADefenderId))

const team2AttackerName = computed(() => getPlayerName(store.currentGame.teamBAttackerId))
const team2DefenderName = computed(() => getPlayerName(store.currentGame.teamBDefenderId))

function confirm() {
  store.confirmPositions()
}

function swapTeam1() {
  store.swapPositions(1)
}

function swapTeam2() {
  store.swapPositions(2)
}
</script>

<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center p-6 bg-black/75 backdrop-blur-md ch-position-swap" role="dialog" aria-modal="true">
    <div class="w-full max-w-sm bg-surface-container-low rounded-2xl p-6 space-y-6 shadow-2xl">
      <div class="text-center space-y-2">
        <h2 class="font-headline text-xl font-bold text-on-surface">
          Confirm Positions
        </h2>
        <p class="text-xs text-on-surface-variant leading-relaxed">
          Who is attacking and who is defending?
        </p>
      </div>

      <div class="flex flex-col gap-4">
        <!-- Team 1 -->
        <div class="flex flex-col bg-surface-container p-4 rounded-xl">
          <div class="font-bold text-on-surface mb-2 text-center">Team 1</div>
          <div class="flex justify-between items-center text-sm mb-1">
            <span class="text-on-surface-variant">Attacker:</span>
            <span class="font-bold">{{ team1AttackerName }}</span>
          </div>
          <div class="flex justify-between items-center text-sm mb-3">
            <span class="text-on-surface-variant">Defender:</span>
            <span class="font-bold">{{ team1DefenderName }}</span>
          </div>
          <BaseButton variant="secondary" @click="swapTeam1" class="!h-8 text-xs">{{ t('match.swapTeam1', 'Swap Team 1') }}</BaseButton>
        </div>

        <!-- Team 2 -->
        <div class="flex flex-col bg-surface-container p-4 rounded-xl">
          <div class="font-bold text-on-surface mb-2 text-center">Team 2</div>
          <div class="flex justify-between items-center text-sm mb-1">
            <span class="text-on-surface-variant">Attacker:</span>
            <span class="font-bold">{{ team2AttackerName }}</span>
          </div>
          <div class="flex justify-between items-center text-sm mb-3">
            <span class="text-on-surface-variant">Defender:</span>
            <span class="font-bold">{{ team2DefenderName }}</span>
          </div>
          <BaseButton variant="secondary" @click="swapTeam2" class="!h-8 text-xs">{{ t('match.swapTeam2', 'Swap Team 2') }}</BaseButton>
        </div>
      </div>

      <div class="flex flex-col gap-2 mt-4">
        <BaseButton
          @click="confirm"
          class="w-full font-headline font-bold text-xs !h-12"
        >
          Confirm & Play
        </BaseButton>
      </div>
    </div>
  </div>
</template>
