export type TournamentFormat = 'CUP' | 'CHAMPIONSHIP';

export type TournamentMode =
  | 'ONE_VS_ONE_PERSONAL'
  | 'TWO_VS_TWO_FIXED_TEAMS'
  | 'TWO_VS_TWO_RANDOM_PAIRINGS';

export type TournamentStatus =
  | 'REGISTRATION_OPEN'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED';

export type RegistrationStatus =
  | 'PENDING_CONFIRMATION'
  | 'CONFIRMED'
  | 'DECLINED'
  | 'CANCELLED';

export interface RuleConfigurationSummaryDto {
  id: string;
  name: string;
  goalLimit: number;
  gameLimit: number;
  winByTwo: boolean;
}

export interface CreateTournamentPayload {
  name: string;
  format: TournamentFormat;
  mode: TournamentMode;
  ruleConfigurationId: string;
  minParticipants: number;
  maxParticipants: number;
  registrationDeadline: string;
  roundCount?: number | null;
  hasPlayoff?: boolean;
}

export interface TournamentDto {
  id: string;
  name: string;
  format: TournamentFormat;
  mode: TournamentMode;
  ruleConfiguration: RuleConfigurationSummaryDto;
  minParticipants: number;
  maxParticipants: number;
  registrationDeadline: string;
  roundCount?: number | null;
  hasPlayoff: boolean;
  status: TournamentStatus;
  cancellationReason?: string | null;
  creatorId: string;
  creatorNickname: string;
  createdAt: string;
}

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
  status: RegistrationStatus;
  seed?: number | null;
  strengthScore?: number | null;
  createdAt: string;
  updatedAt?: string | null;
}

export interface MyRegistrationStatusDto {
  isRegistered: boolean;
  registration?: TournamentRegistrationDto | null;
  isPendingInvite: boolean;
}

export type TournamentMatchStatus =
  | 'PENDING'
  | 'READY'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'BYE'
  | 'CANCELLED';

export interface TournamentMatchDto {
  id: string;
  tournamentId: string;
  round: number;
  matchOrder: number;
  matchId?: string | null;
  participant1?: TournamentRegistrationDto | null;
  participant1Partner?: TournamentRegistrationDto | null;
  participant2?: TournamentRegistrationDto | null;
  participant2Partner?: TournamentRegistrationDto | null;
  isParticipant1Stub?: boolean;
  isParticipant2Stub?: boolean;
  seed1?: number | null;
  seed2?: number | null;
  status: TournamentMatchStatus;
  winnerRegistrationId?: string | null;
  nextMatchId?: string | null;
  createdAt?: string;
}

export interface RoundMatchesDto {
  round: number;
  roundName: string;
  matches: TournamentMatchDto[];
}

export interface TournamentBracketDto {
  tournamentId: string;
  tournamentName: string;
  format: TournamentFormat;
  mode: TournamentMode;
  status: TournamentStatus;
  totalRounds: number;
  rounds: RoundMatchesDto[];
  seededParticipants: TournamentRegistrationDto[];
}
