import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import NewMatchFlow from '@/features/match/components/NewMatchFlow.vue'
import { useMatchDraftStore } from '@/features/match/stores/matchDraftStore'
import { useAuthStore } from '@/stores/auth'

const mockRoute = {
  query: {
    tournamentId: 't-100',
    tournamentMatchId: 'tm-200',
    ruleConfigId: 'rule-official-uuid',
  },
}

vi.mock('vue-router', () => ({
  useRoute: () => mockRoute,
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn(),
  }),
}))

describe('NewMatchFlow ATDD Component Tests (Tournament Context Enforcement)', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    const authStore = useAuthStore()
    authStore.profile = {
      id: 'user-1',
      nickname: 'Player 1',
    } as any
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
        },
        mocks: {
          t: (key: string, fallback: string) => fallback || key,
        },
      },
    })

    expect(draftStore.isTournamentMatch).toBe(true)
    expect(draftStore.tournamentId).toBe('t-100')
    expect(draftStore.tournamentMatchId).toBe('tm-200')
    expect(draftStore.ruleConfigurationId).toBe('rule-official-uuid')

    const rulePickerStub = wrapper.find('[data-testid="rule-picker-stub"]')
    expect(rulePickerStub.attributes('data-locked')).toBe('true')
  })
})
