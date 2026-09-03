import type {
  CreateTournamentPayload,
  TournamentDto,
  TournamentStatus,
} from '@/features/tournament/types/tournament'

export async function createTournament(payload: CreateTournamentPayload): Promise<TournamentDto> {
  const res = await fetch('/api/v1/tournaments', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    },
    body: JSON.stringify(payload),
  })

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}))
    throw new Error(errorData.message || `Failed to create tournament (${res.status})`)
  }

  return res.json()
}

export async function getTournaments(status?: TournamentStatus): Promise<TournamentDto[]> {
  const url = status ? `/api/v1/tournaments?status=${status}` : '/api/v1/tournaments'

  const res = await fetch(url, {
    headers: { Accept: 'application/json' },
  })

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}))
    throw new Error(errorData.message || `Failed to fetch tournaments (${res.status})`)
  }

  return res.json()
}

export async function getTournamentById(id: string): Promise<TournamentDto> {
  const res = await fetch(`/api/v1/tournaments/${id}`, {
    headers: { Accept: 'application/json' },
  })

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}))
    throw new Error(errorData.message || `Failed to fetch tournament (${res.status})`)
  }

  return res.json()
}
