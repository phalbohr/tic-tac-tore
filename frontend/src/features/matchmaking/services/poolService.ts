import type { CreatePoolPayload, PoolResponse } from '../types/pool'

export async function createPool(payload: CreatePoolPayload): Promise<PoolResponse> {
  const res = await fetch('/api/v1/pools', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    },
    body: JSON.stringify(payload),
  })

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}))
    throw new Error(errorData.message || `Failed to create pool (${res.status})`)
  }

  return res.json()
}

export async function fetchActivePools(): Promise<PoolResponse[]> {
  const res = await fetch('/api/v1/pools', {
    headers: {
      Accept: 'application/json',
    },
  })

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}))
    throw new Error(errorData.message || `Failed to fetch active pools (${res.status})`)
  }

  return res.json()
}

export async function joinPool(id: string): Promise<PoolResponse> {
  const res = await fetch(`/api/v1/pools/${id}/join`, {
    method: 'POST',
    headers: {
      Accept: 'application/json',
    },
  })

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}))
    throw new Error(errorData.message || `Failed to join pool (${res.status})`)
  }

  return res.json()
}

export async function fetchPoolById(id: string): Promise<PoolResponse> {
  const res = await fetch(`/api/v1/pools/${id}`, {
    headers: {
      Accept: 'application/json',
    },
  })

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}))
    throw new Error(errorData.message || `Failed to fetch pool (${res.status})`)
  }

  return res.json()
}
