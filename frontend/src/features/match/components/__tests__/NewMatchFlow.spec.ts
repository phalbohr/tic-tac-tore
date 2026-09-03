import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import NewMatchFlow from '@/features/match/components/NewMatchFlow.vue'
import { useMatchDraftStore } from '@/features/match/stores/matchDraftStore'
import { useAuthStore, type UserProfile } from '@/stores/auth'

const mockRoute = {
  query: {
    tournamentId: 't-100',
    tournamentMatchId: 'tm-200',
    ruleConfigId: 'rule-official-uuid',
    ruleSystemName: 'Official Standard',
  },
}

vi.mock('vue-router', () => ({
  useRoute: () => mockRoute,
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn(),
  }),
}))

vi.mock('vue-i18n', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-i18n')>()
  return {
    ...actual,
    useI18n: () => ({
      t: (key: string, defaultVal?: string) => defaultVal || key,
    }),
  }
})

describe('NewMatchFlow ATDD Component Tests (Tournament Context Enforcement)', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    const authStore = useAuthStore()
    authStore.profile = {
      id: 'user-1',
      nickname: 'Player 1',
    } as unknown as UserProfile
  })

  it('should lock rule picker and match type picker when tournament query parameters are present', async () => {
    const draftStore = useMatchDraftStore()

    const wrapper = mount(NewMatchFlow, {
      global: {
        stubs: {
          RulePicker: {
            props: ['isLocked'],
            template: '<div data-testid="rule-picker-stub" :data-locked="isLocked">RulePicker</div>',
          },
          MatchTypePicker: {
            props: ['isLocked'],
            template: '<div data-testid="match-type-picker-stub" :data-locked="isLocked">MatchTypePicker</div>',
          },
          PlayerSelection: {
            template: '<div data-testid="player-selection-stub">PlayerSelection</div>',
          },
        },
      },
    })

    expect(draftStore.isTournamentMatch).toBe(true)
    expect(draftStore.tournamentId).toBe('t-100')
    expect(draftStore.tournamentMatchId).toBe('tm-200')
    expect(draftStore.ruleConfigurationId).toBe('rule-official-uuid')

    await wrapper.vm.$nextTick()

    const rulePickerStub = wrapper.find('[data-testid="rule-picker-stub"]')
    expect(rulePickerStub.attributes('data-locked')).toBe('true')

    const matchTypeStub = wrapper.find('[data-testid="match-type-picker-stub"]')
    expect(matchTypeStub.attributes('data-locked')).toBe('true')
  })
})
