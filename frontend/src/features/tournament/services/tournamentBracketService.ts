import type {
  TournamentBracketDto,
  TournamentMatchDto,
  TournamentDto,
} from '@/features/tournament/types/tournament';

export async function getTournamentBracket(
  tournamentId: string
): Promise<TournamentBracketDto> {
  const res = await fetch(`/api/v1/tournaments/${tournamentId}/bracket`, {
    headers: { Accept: 'application/json' },
  });

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.message || `Failed to fetch tournament bracket (${res.status})`);
  }

  return res.json();
}

export async function getTournamentMatches(
  tournamentId: string,
  round?: number
): Promise<TournamentMatchDto[]> {
  const url = round !== undefined
    ? `/api/v1/tournaments/${tournamentId}/matches?round=${round}`
    : `/api/v1/tournaments/${tournamentId}/matches`;

  const res = await fetch(url, {
    headers: { Accept: 'application/json' },
  });

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.message || `Failed to fetch tournament matches (${res.status})`);
  }

  return res.json();
}

export async function startTournament(
  tournamentId: string
): Promise<TournamentDto> {
  const res = await fetch(`/api/v1/tournaments/${tournamentId}/start`, {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  });

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.message || `Failed to start tournament (${res.status})`);
  }

  return res.json();
}
