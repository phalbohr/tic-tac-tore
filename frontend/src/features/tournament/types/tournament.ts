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
  createdAt: string;
  updatedAt?: string | null;
}

export interface MyRegistrationStatusDto {
  isRegistered: boolean;
  registration?: TournamentRegistrationDto | null;
  isPendingInvite: boolean;
}
