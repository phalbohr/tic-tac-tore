<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import BaseButton from '@/core/components/BaseButton.vue'
import AvatarBase from '@/components/AvatarBase.vue'
import { useMatchDraftStore } from '../stores/matchDraftStore'
import { computed } from 'vue'

defineOptions({
  name: 'PositionSwapDialog'
})

const { t } = useI18n()
const store = useMatchDraftStore()

const getPlayerInfo = (id?: string) => {
  if (!id) return { name: 'Unknown', avatar: null }
  const opp = store.frequentOpponents.find((o) => o.id === id)
  if (opp) return { name: opp.nickname, avatar: opp.avatar }
  const fetched = store.fetchedPlayers[id]
  if (fetched) return { name: fetched.nickname, avatar: fetched.avatar }
  return { name: `Player ${id.substring(0, 4)}`, avatar: null }
}

const team1Defender = computed(() => getPlayerInfo(store.currentGame.teamADefenderId))
const team1Attacker = computed(() => getPlayerInfo(store.currentGame.teamAAttackerId))

const team2Defender = computed(() => getPlayerInfo(store.currentGame.teamBDefenderId))
const team2Attacker = computed(() => getPlayerInfo(store.currentGame.teamBAttackerId))

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
        <div class="flex flex-col bg-surface-container p-4 rounded-xl gap-3">
          <div class="font-bold text-on-surface text-center">Team 1</div>
          
          <div class="grid grid-cols-2 gap-3 text-center">
            <!-- Defender Column -->
            <div class="flex flex-col items-center gap-1">
              <span class="text-xs font-bold text-on-surface-variant uppercase">{{ t('match.defender', 'Defender') }}</span>
              <div class="w-12 h-12 rounded-full overflow-hidden my-1" data-testid="team1-defender-avatar">
                <AvatarBase :name="team1Defender.name" :avatar="team1Defender.avatar" />
              </div>
              <span class="text-xs font-semibold text-on-surface truncate w-full text-center">{{ team1Defender.name }}</span>
            </div>

            <!-- Attacker Column -->
            <div class="flex flex-col items-center gap-1">
              <span class="text-xs font-bold text-on-surface-variant uppercase">{{ t('match.attacker', 'Attacker') }}</span>
              <div class="w-12 h-12 rounded-full overflow-hidden my-1" data-testid="team1-attacker-avatar">
                <AvatarBase :name="team1Attacker.name" :avatar="team1Attacker.avatar" />
              </div>
              <span class="text-xs font-semibold text-on-surface truncate w-full text-center">{{ team1Attacker.name }}</span>
            </div>
          </div>

          <BaseButton variant="secondary" @click="swapTeam1" class="!h-8 text-xs w-full mt-1">{{ t('match.swapTeam1', 'Swap Team 1') }}</BaseButton>
        </div>

        <!-- Team 2 -->
        <div class="flex flex-col bg-surface-container p-4 rounded-xl gap-3">
          <div class="font-bold text-on-surface text-center">Team 2</div>

          <div class="grid grid-cols-2 gap-3 text-center">
            <!-- Defender Column -->
            <div class="flex flex-col items-center gap-1">
              <span class="text-xs font-bold text-on-surface-variant uppercase">{{ t('match.defender', 'Defender') }}</span>
              <div class="w-12 h-12 rounded-full overflow-hidden my-1" data-testid="team2-defender-avatar">
                <AvatarBase :name="team2Defender.name" :avatar="team2Defender.avatar" />
              </div>
              <span class="text-xs font-semibold text-on-surface truncate w-full text-center">{{ team2Defender.name }}</span>
            </div>

            <!-- Attacker Column -->
            <div class="flex flex-col items-center gap-1">
              <span class="text-xs font-bold text-on-surface-variant uppercase">{{ t('match.attacker', 'Attacker') }}</span>
              <div class="w-12 h-12 rounded-full overflow-hidden my-1" data-testid="team2-attacker-avatar">
                <AvatarBase :name="team2Attacker.name" :avatar="team2Attacker.avatar" />
              </div>
              <span class="text-xs font-semibold text-on-surface truncate w-full text-center">{{ team2Attacker.name }}</span>
            </div>
          </div>

          <BaseButton variant="secondary" @click="swapTeam2" class="!h-8 text-xs w-full mt-1">{{ t('match.swapTeam2', 'Swap Team 2') }}</BaseButton>
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

