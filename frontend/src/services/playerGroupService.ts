export interface PlayerSummaryDto {
  id: string
  nickname: string
  avatar?: string
}

export interface PlayerGroupResponse {
  id: string
  name: string
  isFavorite: boolean
  creatorId: string
  members: PlayerSummaryDto[]
  createdAt: string
  updatedAt?: string
}

export interface CreatePlayerGroupRequest {
  name: string
  memberIds: string[]
  isFavorite?: boolean
}

export interface UpdatePlayerGroupRequest {
  name: string
  memberIds: string[]
  isFavorite?: boolean
}

export async function getPlayerGroups(): Promise<PlayerGroupResponse[]> {
  const res = await fetch('/api/v1/player-groups')
  if (!res.ok) {
    throw new Error(`Failed to fetch player groups (${res.status})`)
  }
  return res.json()
}

export async function createPlayerGroup(data: CreatePlayerGroupRequest): Promise<PlayerGroupResponse> {
  const res = await fetch('/api/v1/player-groups', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}))
    throw new Error(errorData.message || `Failed to create player group (${res.status})`)
  }
  return res.json()
}

export async function updatePlayerGroup(id: string, data: UpdatePlayerGroupRequest): Promise<PlayerGroupResponse> {
  const res = await fetch(`/api/v1/player-groups/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}))
    throw new Error(errorData.message || `Failed to update player group (${res.status})`)
  }
  return res.json()
}

export async function deletePlayerGroup(id: string): Promise<void> {
  const res = await fetch(`/api/v1/player-groups/${id}`, {
    method: 'DELETE',
  })
  if (!res.ok) {
    throw new Error(`Failed to delete player group (${res.status})`)
  }
}
