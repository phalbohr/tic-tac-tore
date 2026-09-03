import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import TournamentRegistrationModal from '@/features/tournament/components/TournamentRegistrationModal.vue'
import type { TournamentDto } from '@/features/tournament/types/tournament'

vi.mock('vue-i18n', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-i18n')>()
  return {
    ...actual,
    useI18n: () => ({
      t: (key: string, params?: Record<string, unknown>) => {
        if (params?.name) return `You are registering for ${params.name}. Ready to compete?`
        return key
      },
    }),
  }
})

describe('TournamentRegistrationModal.vue Component Tests — Story 8.2', () => {
  const mockTournament1v1: TournamentDto = {
    id: 'tourn-1v1',
    name: 'Solo Masters',
    format: 'CUP',
    mode: 'ONE_VS_ONE_PERSONAL',
    ruleConfiguration: {
      id: 'rule-1',
      name: 'Standard',
      goalLimit: 5,
      gameLimit: 1,
      winByTwo: false,
    },
    minParticipants: 4,
    maxParticipants: 16,
    registrationDeadline: '2026-09-10T12:00:00Z',
    roundCount: null,
    hasPlayoff: false,
    status: 'REGISTRATION_OPEN',
    creatorId: 'user-1',
    creatorNickname: 'Master',
    createdAt: '2026-09-01T10:00:00Z',
  }

  const mockTournament2v2: TournamentDto = {
    id: 'tourn-2v2',
    name: 'Duo Masters',
    format: 'CUP',
    mode: 'TWO_VS_TWO_FIXED_TEAMS',
    ruleConfiguration: {
      id: 'rule-1',
      name: 'Standard',
      goalLimit: 5,
      gameLimit: 1,
      winByTwo: false,
    },
    minParticipants: 4,
    maxParticipants: 16,
    registrationDeadline: '2026-09-10T12:00:00Z',
    roundCount: null,
    hasPlayoff: false,
    status: 'REGISTRATION_OPEN',
    creatorId: 'user-1',
    creatorNickname: 'Master',
    createdAt: '2026-09-01T10:00:00Z',
  }

  it('should render solo registration prompt when tournament mode is 1v1 (AC 1, AC 8)', () => {
    const wrapper = mount(TournamentRegistrationModal, {
      props: {
        isOpen: true,
        tournament: mockTournament1v1,
      },
    })

    expect(wrapper.find('[data-testid="tournament-registration-modal"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="partner-search-input"]').exists()).toBe(false)
    expect(wrapper.find('[data-testid="confirm-registration-btn"]').exists()).toBe(true)
  })

  it('should render partner selector when tournament mode is 2v2 fixed teams (AC 2, AC 8)', () => {
    const wrapper = mount(TournamentRegistrationModal, {
      props: {
        isOpen: true,
        tournament: mockTournament2v2,
      },
    })

    expect(wrapper.find('[data-testid="partner-search-input"]').exists()).toBe(true)
  })

  it('should emit register event without partner for 1v1 mode (AC 1)', async () => {
    const wrapper = mount(TournamentRegistrationModal, {
      props: {
        isOpen: true,
        tournament: mockTournament1v1,
      },
    })

    await wrapper.find('[data-testid="confirm-registration-btn"]').trigger('click')

    expect(wrapper.emitted('register')).toBeTruthy()
    expect(wrapper.emitted('register')?.[0]?.[0]).toEqual({ partnerId: null })
  })

  it('should validate partner selection before submitting 2v2 fixed team registration (AC 6)', async () => {
    const wrapper = mount(TournamentRegistrationModal, {
      props: {
        isOpen: true,
        tournament: mockTournament2v2,
      },
    })

    await wrapper.find('[data-testid="confirm-registration-btn"]').trigger('click')

    expect(wrapper.emitted('register')).toBeFalsy()
    expect(wrapper.find('[data-testid="partner-required-error"]').exists()).toBe(true)
  })

  it('should emit close event when cancel or close button is clicked', async () => {
    const wrapper = mount(TournamentRegistrationModal, {
      props: {
        isOpen: true,
        tournament: mockTournament1v1,
      },
    })

    await wrapper.find('[data-testid="close-registration-modal-btn"]').trigger('click')

    expect(wrapper.emitted('close')).toBeTruthy()
  })
})
