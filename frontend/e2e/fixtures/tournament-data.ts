export interface CreateTournamentPayload {
    name: string;
    format: 'CUP' | 'CHAMPIONSHIP';
    mode: 'ONE_VS_ONE_PERSONAL' | 'TWO_VS_TWO_FIXED_TEAMS' | 'TWO_VS_TWO_RANDOM_PAIRINGS';
    ruleConfigurationId: string;
    minParticipants: number;
    maxParticipants: number;
    registrationDeadline: string;
    roundCount?: number;
    hasPlayoff?: boolean;
}

export function generateValidTournamentPayload(overrides: Partial<CreateTournamentPayload> = {}): CreateTournamentPayload {
    const futureDate = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString();
    return {
        name: `Autumn Cup ${crypto.randomUUID().substring(0, 6)}`,
        format: 'CUP',
        mode: 'ONE_VS_ONE_PERSONAL',
        ruleConfigurationId: '00000000-0000-0000-0000-000000000001',
        minParticipants: 4,
        maxParticipants: 16,
        registrationDeadline: futureDate,
        hasPlayoff: false,
        ...overrides,
    };
}
