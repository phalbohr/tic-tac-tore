import type {
  CreateTournamentPayload,
  PageDto,
  TournamentDto,
  TournamentStandingDto,
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

  const data = await res.json()
  return Array.isArray(data) ? data : (data.content || [])
}

export async function getTournamentsPaginated(
  status?: TournamentStatus,
  page: number = 0,
  size: number = 10,
): Promise<PageDto<TournamentDto>> {
  const params = new URLSearchParams()
  if (status) params.append('status', status)
  params.append('page', page.toString())
  params.append('size', size.toString())

  const res = await fetch(`/api/v1/tournaments?${params.toString()}`, {
    headers: { Accept: 'application/json' },
  })

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}))
    throw new Error(errorData.message || `Failed to fetch tournaments (${res.status})`)
  }

  const data = await res.json()
  if (Array.isArray(data)) {
    return {
      content: data,
      totalPages: 1,
      totalElements: data.length,
      number: 0,
      size: data.length,
    }
  }

  return {
    content: data.content || [],
    totalPages: data.totalPages ?? 0,
    totalElements: data.totalElements ?? 0,
    number: data.number ?? page,
    size: data.size ?? size,
  }
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

export async function getTournamentStandings(tournamentId: string): Promise<TournamentStandingDto[]> {
  const res = await fetch(`/api/v1/tournaments/${tournamentId}/standings`, {
    headers: { Accept: 'application/json' },
  })

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}))
    throw new Error(errorData.message || `Failed to fetch tournament standings (${res.status})`)
  }

  return res.json()
}

export const tournamentService = {
  createTournament,
  getTournaments,
  getTournamentsPaginated,
  getTournamentById,
  getTournamentStandings,
}
