import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  PlayerGroupResponse,
  CreatePlayerGroupRequest,
  UpdatePlayerGroupRequest,
} from '@/services/playerGroupService'

export const usePlayerGroupStore = defineStore('playerGroup', () => {
  const groups = ref<PlayerGroupResponse[]>([])
  const selectedGroupId = ref<string | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const favoriteGroup = computed(() => groups.value.find((g) => g.isFavorite))
  const customGroups = computed(() => groups.value.filter((g) => !g.isFavorite))
  const getGroupById = computed(() => (id: string) => groups.value.find((g) => g.id === id))

  async function fetchGroups(): Promise<void> {
    loading.value = true
    error.value = null
    try {
      const res = await fetch('/api/v1/player-groups', {
        headers: { Accept: 'application/json' },
      })
      if (!res.ok) {
        throw new Error(`Failed to fetch player groups (${res.status})`)
      }
      const data = await res.json()
      groups.value = data || []
    } catch (err: any) {
      error.value = err.message || 'Failed to load player groups'
      groups.value = []
    } finally {
      loading.value = false
    }
  }

  async function createGroup(payload: CreatePlayerGroupRequest): Promise<PlayerGroupResponse> {
    loading.value = true
    error.value = null
    try {
      const res = await fetch('/api/v1/player-groups', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })
      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}))
        throw new Error(errorData.message || `Failed to create player group (${res.status})`)
      }
      const created: PlayerGroupResponse = await res.json()
      groups.value.push(created)
      return created
    } catch (err: any) {
      error.value = err.message || 'Failed to create player group'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function updateGroup(
    id: string,
    payload: UpdatePlayerGroupRequest
  ): Promise<PlayerGroupResponse> {
    loading.value = true
    error.value = null
    try {
      const res = await fetch(`/api/v1/player-groups/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })
      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}))
        throw new Error(errorData.message || `Failed to update player group (${res.status})`)
      }
      const updated: PlayerGroupResponse = await res.json()
      const index = groups.value.findIndex((g) => g.id === id)
      if (index !== -1) {
        groups.value[index] = updated
      }
      return updated
    } catch (err: any) {
      error.value = err.message || 'Failed to update player group'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function deleteGroup(id: string): Promise<void> {
    loading.value = true
    error.value = null
    try {
      const res = await fetch(`/api/v1/player-groups/${id}`, {
        method: 'DELETE',
      })
      if (!res.ok) {
        throw new Error(`Failed to delete player group (${res.status})`)
      }
      groups.value = groups.value.filter((g) => g.id !== id)
      if (selectedGroupId.value === id) {
        selectedGroupId.value = null
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to delete player group'
      throw err
    } finally {
      loading.value = false
    }
  }

  function selectGroup(groupId: string | null): void {
    selectedGroupId.value = groupId
  }

  return {
    groups,
    selectedGroupId,
    loading,
    error,
    favoriteGroup,
    customGroups,
    getGroupById,
    fetchGroups,
    createGroup,
    updateGroup,
    deleteGroup,
    selectGroup,
  }
})
