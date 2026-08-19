import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import MatchCard from '../MatchCard.vue'
import type { MatchResponse } from '@/services/matchService'
import { useAuthStore } from '@/stores/auth'

vi.mock('vue-i18n', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-i18n')>()
  return {
    ...actual,
    useI18n: () => ({
      t: (key: string, defaultVal?: string) => {
        const translations: Record<string, string> = {
          'history.outcome.win': 'Win',
          'history.outcome.loss': 'Loss',
          'history.outcome.draw': 'Draw',
          'history.retiredPlayer': 'Retired Player'
        }
        return translations[key] || defaultVal || key
      }
    })
  }
})

describe('MatchCard.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  const sample1v1Match: MatchResponse = {
    id: 'match-1',
    creatorId: 'user-1',
    teamAAttackerId: 'user-1',
    teamBAttackerId: 'user-2',
    status: 'CONFIRMED',
    games: [
      { teamAScore: 10, teamBScore: 6 },
      { teamAScore: 10, teamBScore: 8 }
    ],
    createdAt: '2026-08-19T10:00:00Z',
    teamAAttackerNickname: 'Alice',
    teamBAttackerNickname: 'Bob',
    matchFormat: 'STANDARD'
  }

  it('renders match format, scores, player names, and WIN badge for winner', () => {
    const authStore = useAuthStore()
    authStore.profile = { id: 'user-1', nickname: 'Alice', avatar: 'avatar1' }

    const wrapper = mount(MatchCard, {
      props: {
        match: sample1v1Match
      }
    })

    expect(wrapper.text()).toContain('Alice')
    expect(wrapper.text()).toContain('Bob')
    expect(wrapper.text()).toContain('10 - 6, 10 - 8')
    expect(wrapper.text()).toContain('1v1')
    expect(wrapper.text()).toContain('STANDARD')
    expect(wrapper.find('[data-testid="outcome-badge-match-1"]').text()).toBe('Win')
  })

  it('renders LOSS badge when current user is on the losing team', () => {
    const authStore = useAuthStore()
    authStore.profile = { id: 'user-2', nickname: 'Bob', avatar: 'avatar2' }

    const wrapper = mount(MatchCard, {
      props: {
        match: sample1v1Match
      }
    })

    expect(wrapper.find('[data-testid="outcome-badge-match-1"]').text()).toBe('Loss')
  })

  it('safely renders Retired Player when player has retired ex-player prefix', () => {
    const retiredMatch: MatchResponse = {
      ...sample1v1Match,
      teamBAttackerNickname: 'ex-player-9999'
    }

    const wrapper = mount(MatchCard, {
      props: {
        match: retiredMatch
      }
    })

    expect(wrapper.text()).toContain('Retired Player')
    expect(wrapper.text()).not.toContain('ex-player-9999')
  })

  it('renders neutral COMPLETED badge when current user is not a participant (NONE)', () => {
    const authStore = useAuthStore()
    authStore.profile = { id: 'spectator-99', nickname: 'Spectator', avatar: 'avatar99' }

    const wrapper = mount(MatchCard, {
      props: {
        match: sample1v1Match
      }
    })

    expect(wrapper.find('[data-testid="outcome-badge-match-1"]').text()).toBe('Completed')
  })

  it('handles invalid date strings gracefully without throwing', () => {
    const invalidDateMatch: MatchResponse = {
      ...sample1v1Match,
      createdAt: 'not-a-valid-date'
    }

    const wrapper = mount(MatchCard, {
      props: {
        match: invalidDateMatch
      }
    })

    expect(wrapper.exists()).toBe(true)
  })
})
