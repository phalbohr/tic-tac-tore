export type MatchType = 'ONE_VS_ONE' | 'TWO_VS_TWO';
export type StartCondition = 'FILL_BASED' | 'SCHEDULED_TIME';
export type SkillLevel = 'OPEN_FOR_ALL' | 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
export type PoolStatus = 'OPEN' | 'FILLED' | 'CANCELLED' | 'EXPIRED';
export type PoolParticipantRole = 'HOST' | 'PLAYER';

export interface PoolParticipantDto {
  userId: string;
  nickname: string;
  avatar?: string | null;
  role: PoolParticipantRole;
  joinedAt: string;
}

export interface PoolResponse {
  id: string;
  creatorId: string;
  creatorNickname: string;
  matchType: MatchType;
  startCondition: StartCondition;
  scheduledTime?: string | null;
  skillLevel: SkillLevel;
  status: PoolStatus;
  requiredPlayers: number;
  currentPlayers: number;
  participants: PoolParticipantDto[];
  createdAt: string;
}

export interface CreatePoolPayload {
  matchType: MatchType;
  startCondition: StartCondition;
  scheduledTime?: string | null;
  skillLevel?: SkillLevel;
}
