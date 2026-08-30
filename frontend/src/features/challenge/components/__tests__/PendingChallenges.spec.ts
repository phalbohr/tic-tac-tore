import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import PendingChallenges from '@/features/challenge/components/PendingChallenges.vue'
import { useChallengeStore } from '@/features/challenge/stores/useChallengeStore'

vi.mock('vue-i18n', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-i18n')>()
  return {
    ...actual,
    useI18n: () => ({
      t: (key: string, defaultVal?: string) => defaultVal || key,
    }),
  }
})

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn(),
  }),
}))

describe('PendingChallenges.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders incoming challenges list with card details', async () => {
    const store = useChallengeStore()
    store.incomingChallenges = [
      {
        id: 'c-1',
        challengerId: 'u-1',
        challengerNickname: 'Pavel',
        targetPlayerId: 'u-me',
        matchType: 'ONE_VS_ONE',
        ruleConfigName: 'Standard',
        message: 'Let us play!',
        status: 'PENDING',
        createdAt: '2026-08-30T10:00:00Z',
      },
    ]

    const wrapper = mount(PendingChallenges, {
      global: {
        stubs: {
          AvatarBase: true,
        },
      },
    })

    expect(wrapper.find('[data-testid="incoming-challenge-badge"]').text()).toBe('1')
    expect(wrapper.find('[data-testid="challenger-name"]').text()).toBe('Pavel')
    expect(wrapper.find('[data-testid="match-type-chip"]').text()).toBe('1v1')
    expect(wrapper.text()).toContain('Let us play!')
    expect(wrapper.find('[data-testid="accept-challenge-btn"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="decline-challenge-btn"]').exists()).toBe(true)
  })

  it('emits challengeAccepted when Accept is clicked', async () => {
    const store = useChallengeStore()
    const challenge = {
      id: 'c-1',
      challengerId: 'u-1',
      challengerNickname: 'Pavel',
      targetPlayerId: 'u-me',
      matchType: 'ONE_VS_ONE' as const,
      status: 'PENDING' as const,
      createdAt: '2026-08-30T10:00:00Z',
    }
    store.incomingChallenges = [challenge]

    const acceptSpy = vi.spyOn(store, 'acceptChallenge').mockResolvedValueOnce({
      challengeId: 'c-1',
      status: 'ACCEPTED',
      message: 'Accepted',
    })

    const wrapper = mount(PendingChallenges, {
      global: {
        stubs: {
          AvatarBase: true,
        },
      },
    })

    await wrapper.find('[data-testid="accept-challenge-btn"]').trigger('click')

    expect(acceptSpy).toHaveBeenCalledWith('c-1')
    expect(wrapper.emitted('challengeAccepted')).toBeTruthy()
  })

  it('emits challengeDeclined when Decline is clicked', async () => {
    const store = useChallengeStore()
    const challenge = {
      id: 'c-1',
      challengerId: 'u-1',
      challengerNickname: 'Pavel',
      targetPlayerId: 'u-me',
      matchType: 'ONE_VS_ONE' as const,
      status: 'PENDING' as const,
      createdAt: '2026-08-30T10:00:00Z',
    }
    store.incomingChallenges = [challenge]

    const declineSpy = vi.spyOn(store, 'declineChallenge').mockResolvedValueOnce({
      challengeId: 'c-1',
      status: 'DECLINED',
      message: 'Declined',
    })

    const wrapper = mount(PendingChallenges, {
      global: {
        stubs: {
          AvatarBase: true,
        },
      },
    })

    await wrapper.find('[data-testid="decline-challenge-btn"]').trigger('click')

    expect(declineSpy).toHaveBeenCalledWith('c-1')
    expect(wrapper.emitted('challengeDeclined')).toBeTruthy()
  })

  it('switches to outgoing tab and allows cancelling a challenge', async () => {
    const store = useChallengeStore()
    const challenge = {
      id: 'c-out-1',
      challengerId: 'u-me',
      challengerNickname: 'Me',
      targetPlayerId: 'u-2',
      targetPlayerNickname: 'TargetUser',
      matchType: 'TWO_VS_TWO' as const,
      status: 'PENDING' as const,
      createdAt: '2026-08-30T10:00:00Z',
    }
    store.outgoingChallenges = [challenge]

    const cancelSpy = vi.spyOn(store, 'cancelChallenge').mockResolvedValueOnce({
      challengeId: 'c-out-1',
      status: 'CANCELLED',
      message: 'Cancelled',
    })

    const wrapper = mount(PendingChallenges, {
      global: {
        stubs: {
          AvatarBase: true,
        },
      },
    })

    await wrapper.find('[data-testid="tab-outgoing"]').trigger('click')

    expect(wrapper.find('[data-testid="outgoing-challenge-card"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('TargetUser')

    await wrapper.find('[data-testid="cancel-challenge-btn"]').trigger('click')

    expect(cancelSpy).toHaveBeenCalledWith('c-out-1')
    expect(wrapper.emitted('challengeCancelled')).toBeTruthy()
  })
})
