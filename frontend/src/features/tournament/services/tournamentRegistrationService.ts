import type {
  TournamentRegistrationDto,
  RegisterTournamentPayload,
  MyRegistrationStatusDto,
  RegistrationStatus,
} from '@/features/tournament/types/tournament';

export async function registerForTournament(
  tournamentId: string,
  payload: RegisterTournamentPayload
): Promise<TournamentRegistrationDto> {
  const res = await fetch(`/api/v1/tournaments/${tournamentId}/registrations`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    },
    body: JSON.stringify(payload),
  });

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.message || `Failed to register for tournament (${res.status})`);
  }

  return res.json();
}

export async function getTournamentRegistrations(
  tournamentId: string,
  status?: RegistrationStatus
): Promise<TournamentRegistrationDto[]> {
  const url = status
    ? `/api/v1/tournaments/${tournamentId}/registrations?status=${status}`
    : `/api/v1/tournaments/${tournamentId}/registrations`;

  const res = await fetch(url, {
    headers: { Accept: 'application/json' },
  });

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.message || `Failed to fetch tournament registrations (${res.status})`);
  }

  return res.json();
}

export async function getMyRegistration(tournamentId: string): Promise<MyRegistrationStatusDto> {
  const res = await fetch(`/api/v1/tournaments/${tournamentId}/registrations/my`, {
    headers: { Accept: 'application/json' },
  });

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.message || `Failed to fetch my registration status (${res.status})`);
  }

  const data = await res.json();
  return {
    isRegistered: data.registered ?? data.isRegistered ?? false,
    registration: data.registration ?? null,
    isPendingInvite: data.isPendingInvite ?? false,
  };
}

export async function acceptInvitation(
  tournamentId: string,
  registrationId: string
): Promise<TournamentRegistrationDto> {
  const res = await fetch(
    `/api/v1/tournaments/${tournamentId}/registrations/${registrationId}/accept`,
    {
      method: 'POST',
      headers: {
        Accept: 'application/json',
      },
    }
  );

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.message || `Failed to accept tournament invitation (${res.status})`);
  }

  return res.json();
}

export async function declineInvitation(
  tournamentId: string,
  registrationId: string
): Promise<TournamentRegistrationDto> {
  const res = await fetch(
    `/api/v1/tournaments/${tournamentId}/registrations/${registrationId}/decline`,
    {
      method: 'POST',
      headers: {
        Accept: 'application/json',
      },
    }
  );

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.message || `Failed to decline tournament invitation (${res.status})`);
  }

  return res.json();
}

export async function cancelRegistration(
  tournamentId: string,
  registrationId: string
): Promise<void> {
  const res = await fetch(
    `/api/v1/tournaments/${tournamentId}/registrations/${registrationId}`,
    {
      method: 'DELETE',
    }
  );

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.message || `Failed to cancel tournament registration (${res.status})`);
  }
}

export async function getPendingInvitations(): Promise<TournamentRegistrationDto[]> {
  const res = await fetch('/api/v1/tournaments/invitations/pending', {
    headers: { Accept: 'application/json' },
  });

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.message || `Failed to fetch pending tournament invitations (${res.status})`);
  }

  return res.json();
}
