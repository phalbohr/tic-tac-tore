import { defineStore } from 'pinia'
import { ref } from 'vue'

export enum MatchType {
  ONE_VS_ONE = '1v1',
  TWO_VS_TWO = '2v2'
}

export const useMatchDraftStore = defineStore('matchDraft', () => {
  const matchType = ref<MatchType>(MatchType.ONE_VS_ONE)
  const selectedPlayers = ref<string[]>([])
  const ruleSystem = ref<string>('STANDARD')
  const frequentOpponents = ref<any[]>([])

  async function fetchDefaults() {
    try {
      const [opponentsRes, prefsRes] = await Promise.all([
        fetch('/api/users/me/frequent-opponents'),
        fetch('/api/users/me/preferences/last-rule-system')
      ])
      if (opponentsRes.ok) {
        frequentOpponents.value = await opponentsRes.json()
      }
      if (prefsRes.ok) {
        const data = await prefsRes.json()
        if (data.lastRuleSystem) {
          ruleSystem.value = data.lastRuleSystem
        }
      }
    } catch (e) {
      console.error('Failed to fetch match defaults', e)
    }
  }

  function setMatchType(type: MatchType) {
    matchType.value = type
    if (type === MatchType.ONE_VS_ONE) {
      selectedPlayers.value = selectedPlayers.value.slice(0, 2)
    }
  }

  function addPlayer(playerId: string) {
    if (!playerId.trim()) return;
    const maxPlayers = matchType.value === MatchType.ONE_VS_ONE ? 2 : 4
    if (selectedPlayers.value.length < maxPlayers && !selectedPlayers.value.includes(playerId)) {
      selectedPlayers.value.push(playerId)
    }
  }

  function removePlayer(playerId: string) {
    selectedPlayers.value = selectedPlayers.value.filter(id => id !== playerId)
  }

  function reset() {
    matchType.value = MatchType.ONE_VS_ONE
    selectedPlayers.value = []
    ruleSystem.value = 'STANDARD'
  }

  return {
    matchType,
    selectedPlayers,
    ruleSystem,
    frequentOpponents,
    fetchDefaults,
    setMatchType,
    addPlayer,
    removePlayer,
    reset
  }
})
