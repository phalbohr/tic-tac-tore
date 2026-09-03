import { getCsrfHeaders } from '../utils/cookieUtils'

export type SideSwapRule = 'NONE' | 'BETWEEN_GAMES' | 'AFTER_HALF_POINTS'
export type RestartRule = 'CONCEDING_TEAM' | 'RANDOM_DROP'
export type PositionSwapRule = 'BETWEEN_GAMES' | 'NEVER' | 'FREE'
export type PointDistribution = 'WIN_LOSS_3_0' | 'WIN_LOSS_2_0' | 'WIN_DRAW_LOSS_3_1_0'
export type RuleConfigurationType = 'PRESET' | 'CUSTOM'

export interface RuleConfig {
  id: string
  name: string
  type: RuleConfigurationType
  goalLimit: number
  gameLimit: number
  winByTwo: boolean
  absoluteScoreCap?: number | null
  timeoutsPerGame: number
  timeoutDurationSeconds: number
  possessionLimit5BarSeconds: number
  possessionLimitOtherSeconds: number
  sideSwapRule: SideSwapRule
  restartRule: RestartRule
  spinningAllowed: boolean
  aerialsAllowed: boolean
  positionSwapRule: PositionSwapRule
  pointDistribution: PointDistribution
  createdBy?: string
  createdAt?: string
}

export interface CreateRuleConfigRequest {
  name: string
  goalLimit: number
  gameLimit: number
  winByTwo: boolean
  absoluteScoreCap?: number | null
  timeoutsPerGame: number
  timeoutDurationSeconds: number
  possessionLimit5BarSeconds: number
  possessionLimitOtherSeconds: number
  sideSwapRule: SideSwapRule
  restartRule: RestartRule
  spinningAllowed: boolean
  aerialsAllowed: boolean
  positionSwapRule: PositionSwapRule
  pointDistribution: PointDistribution
}

export async function getRuleConfigurations(type?: RuleConfigurationType): Promise<RuleConfig[]> {
  const url = type ? `/api/v1/rule-configurations?type=${type}` : '/api/v1/rule-configurations'
  const res = await fetch(url)
  if (!res.ok) {
    throw new Error(`Failed to fetch rule configurations (${res.status})`)
  }
  return res.json()
}

export async function getRuleConfigurationById(id: string): Promise<RuleConfig> {
  const res = await fetch(`/api/v1/rule-configurations/${id}`)
  if (!res.ok) {
    throw new Error(`Failed to fetch rule configuration (${res.status})`)
  }
  return res.json()
}

export async function createRuleConfiguration(data: CreateRuleConfigRequest): Promise<RuleConfig> {
  const res = await fetch('/api/v1/rule-configurations', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...getCsrfHeaders(),
    },
    body: JSON.stringify(data),
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error(err.message || `Failed to create rule configuration (${res.status})`)
  }
  return res.json()
}

export async function deleteRuleConfiguration(id: string): Promise<void> {
  const res = await fetch(`/api/v1/rule-configurations/${id}`, {
    method: 'DELETE',
    headers: {
      ...getCsrfHeaders(),
    },
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error(err.message || `Failed to delete rule configuration (${res.status})`)
  }
}
