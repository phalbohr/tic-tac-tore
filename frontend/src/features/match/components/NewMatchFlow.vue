<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useMatchDraftStore, MatchType } from '../stores/matchDraftStore'
import { useAuthStore } from '@/stores/auth'
import MatchTypePicker from './MatchTypePicker.vue'
import RulePicker from './RulePicker.vue'
import PlayerSelection from './PlayerSelection.vue'
import ScoreEntry from './ScoreEntry.vue'
import BaseButton from '@/core/components/BaseButton.vue'

const store = useMatchDraftStore()
const authStore = useAuthStore()
const route = useRoute()
const emit = defineEmits<{
  (e: 'cancel'): void
  (e: 'complete'): void
}>()

onMounted(async () => {
  store.fetchDefaults()
  authStore.fetchProfile()
  if (route.query.tournamentId && route.query.tournamentMatchId) {
    const tournId = String(route.query.tournamentId)
    const matchId = String(route.query.tournamentMatchId)

    let rawPlayerIds = route.query.playerIds
      ? String(route.query.playerIds).split(',').filter(Boolean)
      : undefined
    let matchTypeParam =
      route.query.matchType === '2v2'
        ? MatchType.TWO_VS_TWO
        : route.query.matchType === '1v1'
          ? MatchType.ONE_VS_ONE
          : undefined
    let ruleConfigId = route.query.ruleConfigId ? String(route.query.ruleConfigId) : undefined
    let ruleSystemName = route.query.ruleSystemName ? String(route.query.ruleSystemName) : undefined

    // Immediate synchronous context initialization from route query
    store.setTournamentContext({
      tournamentId: tournId,
      tournamentMatchId: matchId,
      ruleConfigId,
      ruleSystemName,
      matchType: matchTypeParam || MatchType.ONE_VS_ONE,
      playerIds: rawPlayerIds,
    })

    if (!rawPlayerIds || rawPlayerIds.length === 0 || !matchTypeParam || !ruleConfigId) {
      try {
        const [matchesRes, tournRes] = await Promise.all([
          fetch(`/api/v1/tournaments/${tournId}/matches`),
          fetch(`/api/v1/tournaments/${tournId}`),
        ])
        if (matchesRes.ok) {
          const matches: any[] = await matchesRes.json()
          const targetMatch = matches.find((m) => m.id === matchId)
          if (targetMatch) {
            if (!ruleConfigId && targetMatch.ruleConfigurationId) {
              ruleConfigId = targetMatch.ruleConfigurationId
            }
            if (!ruleSystemName && targetMatch.ruleConfigurationName) {
              ruleSystemName = targetMatch.ruleConfigurationName
            }

            let is2v2 = matchTypeParam === MatchType.TWO_VS_TWO
            if (!matchTypeParam && tournRes.ok) {
              const tourn = await tournRes.json()
              is2v2 = tourn.mode !== 'ONE_VS_ONE_PERSONAL'
              matchTypeParam = is2v2 ? MatchType.TWO_VS_TWO : MatchType.ONE_VS_ONE
            } else if (!matchTypeParam) {
              is2v2 = Boolean(
                targetMatch.participant1Partner ||
                  targetMatch.participant2Partner ||
                  targetMatch.participant1?.partnerId,
              )
              matchTypeParam = is2v2 ? MatchType.TWO_VS_TWO : MatchType.ONE_VS_ONE
            }

            if (!rawPlayerIds || rawPlayerIds.length === 0) {
              const p1Id = targetMatch.participant1?.playerId || targetMatch.participant1?.player?.id
              const p1PartnerId =
                targetMatch.participant1Partner?.playerId ||
                targetMatch.participant1Partner?.player?.id ||
                targetMatch.participant1?.partnerId ||
                targetMatch.participant1?.partner?.id
              const p2Id = targetMatch.participant2?.playerId || targetMatch.participant2?.player?.id
              const p2PartnerId =
                targetMatch.participant2Partner?.playerId ||
                targetMatch.participant2Partner?.player?.id ||
                targetMatch.participant2?.partnerId ||
                targetMatch.participant2?.partner?.id

              if (is2v2) {
                rawPlayerIds = [p1PartnerId, p1Id, p2PartnerId, p2Id].filter(Boolean)
              } else {
                rawPlayerIds = [p1Id, p2Id].filter(Boolean)
              }
            }

            const populatePlayer = (part: any) => {
              if (!part) return
              const id = part.playerId || part.player?.id
              const nickname = part.playerNickname || part.player?.nickname || 'Player'
              const avatar = part.playerAvatarUrl || part.player?.avatarUrl || ''
              if (id) {
                store.fetchedPlayers[id] = { id, nickname, avatar }
              }
            }
            populatePlayer(targetMatch.participant1)
            populatePlayer(targetMatch.participant1Partner)
            populatePlayer(targetMatch.participant2)
            populatePlayer(targetMatch.participant2Partner)
          }
        }
      } catch {
        // Fallback gracefully
      }
    }

    store.setTournamentContext({
      tournamentId: tournId,
      tournamentMatchId: matchId,
      ruleConfigId,
      ruleSystemName,
      matchType: matchTypeParam || MatchType.ONE_VS_ONE,
      playerIds: rawPlayerIds,
    })
  } else {
    if (route.query.tournamentId) {
      store.tournamentId = String(route.query.tournamentId)
    }
    if (route.query.tournamentMatchId) {
      store.tournamentMatchId = String(route.query.tournamentMatchId)
    }
    if (route.query.ruleConfigId) {
      store.ruleConfigurationId = String(route.query.ruleConfigId)
    }
  }
})

onUnmounted(() => {
  if (!store.isPendingSubmission && !store.isOfflinePending) {
    store.reset()
  }
})

const isSubmitting = ref(false)
const errorMsg = ref('')
let abortController: AbortController | null = null

async function submitMatchDraft() {
  if (isSubmitting.value) return
  if (store.selectedPlayers.length !== (store.matchType === '1v1' ? 2 : 4)) return
  isSubmitting.value = true
  errorMsg.value = ''
  abortController = new AbortController()
  try {
    await store.loadRuleConfig(abortController.signal)
    if (store.matchState === 'draft' && (!abortController || !abortController.signal.aborted))
      store.beginScoreEntry()
  } catch (error) {
    const e = error as Error
    if (e.name !== 'AbortError') {
      errorMsg.value = 'Failed to start match. Check rules config.'
    }
  } finally {
    isSubmitting.value = false
    abortController = null
  }
}

function handleCancel() {
  if (abortController) abortController.abort()
  store.reset()
  emit('cancel')
}

function handleBack() {
  store.returnToDraft()
}

function handleMatchReady() {
  emit('complete')
}
</script>

<template>
  <div
    v-if="store.matchState === 'draft'"
    class="w-full flex flex-col items-center bg-surface-container-low rounded-2xl p-4 gap-6"
  >
    <div class="flex justify-between items-center w-full mb-2">
      <h2 class="text-on-surface font-bold text-xl">New Match</h2>
      <BaseButton variant="secondary" @click="handleCancel" class="!h-12">Cancel</BaseButton>
    </div>

    <div class="w-full flex flex-col gap-2 text-start">
      <h3 class="text-on-surface font-headline font-bold mb-1">Match Type</h3>
      <MatchTypePicker :is-locked="store.isTournamentMatch" />
    </div>

    <RulePicker :is-locked="store.isTournamentMatch" />

    <PlayerSelection />
    <div v-if="errorMsg" class="text-red-500 text-sm mt-2">{{ errorMsg }}</div>

    <BaseButton @click="submitMatchDraft" :disabled="isSubmitting" class="w-full mt-4 rounded-full">
      {{ isSubmitting ? 'Loading...' : 'Start Match' }}
    </BaseButton>
  </div>

  <ScoreEntry
    v-else-if="store.matchState === 'score_entry' || store.matchState === 'ready_for_submission'"
    @complete="handleMatchReady"
    @cancel="handleCancel"
    @back="handleBack"
  />

  <div
    v-else
    class="w-full flex flex-col items-center bg-surface-container-low rounded-2xl p-4 gap-6"
  >
    <p>Invalid match state. Please try again.</p>
    <BaseButton @click="handleCancel">Go Back</BaseButton>
  </div>
</template>
