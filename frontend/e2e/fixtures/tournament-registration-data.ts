export interface RegisterTournamentPayload {
    partnerId?: string | null;
}

export interface TournamentRegistrationDto {
    id: string;
    tournamentId: string;
    tournamentName: string;
    playerId: string;
    playerNickname: string;
    playerAvatarUrl?: string | null;
    partnerId?: string | null;
    partnerNickname?: string | null;
    partnerAvatarUrl?: string | null;
    status: 'PENDING_CONFIRMATION' | 'CONFIRMED' | 'DECLINED' | 'CANCELLED';
    createdAt: string;
    updatedAt?: string | null;
}

export interface MyRegistrationStatusDto {
    isRegistered: boolean;
    registration?: TournamentRegistrationDto | null;
    isPendingInvite: boolean;
}

export function generateRegistrationPayload(overrides: Partial<RegisterTournamentPayload> = {}): RegisterTournamentPayload {
    return {
        partnerId: null,
        ...overrides,
    };
}
