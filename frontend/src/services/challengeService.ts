export interface ChallengeItem {
  id: string
  challengerId: string
  challengerNickname: string
  challengerAvatar?: string
  targetPlayerId?: string
  targetPlayerNickname?: string
  targetPlayerAvatar?: string
  targetGroupId?: string
  targetGroupName?: string
  matchType: 'ONE_VS_ONE' | 'TWO_VS_TWO'
  ruleConfigId?: string
  ruleConfigName?: string
  message?: string
  status: 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'CANCELLED' | 'EXPIRED'
  createdAt: string
  expiresAt?: string
}

export interface CreateChallengePayload {
  targetPlayerId?: string
  targetGroupId?: string
  matchType: 'ONE_VS_ONE' | 'TWO_VS_TWO'
  ruleConfigId?: string
  message?: string
}

export interface ChallengeActionResponse {
  challengeId: string
  status: 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'CANCELLED' | 'EXPIRED'
  message: string
}

export async function createChallenge(payload: CreateChallengePayload): Promise<ChallengeItem> {
  const res = await fetch('/api/v1/challenges', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    },
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}))
    throw new Error(errorData.message || `Failed to create challenge (${res.status})`)
  }
  return res.json()
}

export async function getIncomingChallenges(): Promise<ChallengeItem[]> {
  const res = await fetch('/api/v1/challenges/incoming', {
    headers: { Accept: 'application/json' },
  })
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}))
    throw new Error(errorData.message || `Failed to fetch incoming challenges (${res.status})`)
  }
  return res.json()
}

export async function getOutgoingChallenges(): Promise<ChallengeItem[]> {
  const res = await fetch('/api/v1/challenges/outgoing', {
    headers: { Accept: 'application/json' },
  })
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}))
    throw new Error(errorData.message || `Failed to fetch outgoing challenges (${res.status})`)
  }
  return res.json()
}

export async function getChallengeById(id: string): Promise<ChallengeItem> {
  const res = await fetch(`/api/v1/challenges/${id}`, {
    headers: { Accept: 'application/json' },
  })
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}))
    throw new Error(errorData.message || `Failed to fetch challenge (${res.status})`)
  }
  return res.json()
}

export async function acceptChallenge(id: string): Promise<ChallengeActionResponse> {
  const res = await fetch(`/api/v1/challenges/${id}/accept`, {
    method: 'POST',
    headers: { Accept: 'application/json' },
  })
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}))
    throw new Error(errorData.message || `Failed to accept challenge (${res.status})`)
  }
  return res.json()
}

export async function declineChallenge(id: string): Promise<ChallengeActionResponse> {
  const res = await fetch(`/api/v1/challenges/${id}/decline`, {
    method: 'POST',
    headers: { Accept: 'application/json' },
  })
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}))
    throw new Error(errorData.message || `Failed to decline challenge (${res.status})`)
  }
  return res.json()
}

export async function cancelChallenge(id: string): Promise<ChallengeActionResponse> {
  const res = await fetch(`/api/v1/challenges/${id}/cancel`, {
    method: 'POST',
    headers: { Accept: 'application/json' },
  })
  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}))
    throw new Error(errorData.message || `Failed to cancel challenge (${res.status})`)
  }
  return res.json()
}
