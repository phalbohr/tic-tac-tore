import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useMatchDraftStore = defineStore('matchDraft', () => {
  const matchType = ref<'1v1' | '2v2'>('1v1')
  const selectedPlayers = ref<string[]>([])

  function setMatchType(type: '1v1' | '2v2') {
    matchType.value = type
    if (type === '1v1') {
      selectedPlayers.value = selectedPlayers.value.slice(0, 2)
    }
  }

  function addPlayer(playerId: string) {
    const maxPlayers = matchType.value === '1v1' ? 2 : 4
    if (selectedPlayers.value.length < maxPlayers && !selectedPlayers.value.includes(playerId)) {
      selectedPlayers.value.push(playerId)
    }
  }

  function removePlayer(playerId: string) {
    selectedPlayers.value = selectedPlayers.value.filter(id => id !== playerId)
  }

  function reset() {
    matchType.value = '1v1'
    selectedPlayers.value = []
  }

  return {
    matchType,
    selectedPlayers,
    setMatchType,
    addPlayer,
    removePlayer,
    reset
  }
})
