import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import RulePicker from '@/features/match/components/RulePicker.vue'
import { useRuleConfigStore } from '@/stores/useRuleConfigStore'
import { useAuthStore } from '@/stores/auth'
import { useMatchDraftStore } from '@/features/match/stores/matchDraftStore'

describe('RulePicker ATDD Component Tests (Tournament Rule Enforcement)', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    const authStore = useAuthStore()
    authStore.profile = {
      id: 'user-1',
      defaultRuleConfigurationId: null,
    } as any

    const ruleStore = useRuleConfigStore()
    ruleStore.allRules = [
      {
        id: 'rule-official',
        name: 'Official 3-Game Standard',
        type: 'PRESET',
        scoreLimit: 10,
        gameLimit: 3,
        winsNeeded: 2,
        winByTwo: false,
      },
      {
        id: 'rule-custom',
        name: 'Custom 5-Game',
        type: 'CUSTOM',
        scoreLimit: 10,
        gameLimit: 5,
        winsNeeded: 3,
        winByTwo: true,
      },
    ] as any
  })

  it('should render locked badge and disabled interaction when isLocked is true', async () => {
    const draftStore = useMatchDraftStore()
    draftStore.ruleSystem = 'Official 3-Game Standard'

    const wrapper = mount(RulePicker, {
      props: {
        isLocked: true,
      },
      global: {
        mocks: {
          t: (key: string, fallback: string) => fallback || key,
        },
      },
    })

    // Notice banner
    expect(wrapper.text()).toContain('Rule system is locked to tournament settings')

    // "+ Custom Rule" action button hidden
    expect(wrapper.find('[data-testid="create-custom-rule-inline-btn"]').exists()).toBe(false)

    // "Set as default" pin button hidden
    expect(wrapper.find('[data-test="set-as-default-rule-btn"]').exists()).toBe(false)

    // Lock icon / badge on active chip
    const activeChip = wrapper.find('[data-testid="rule-chip-rule-official"]')
    expect(activeChip.exists()).toBe(true)
    expect(activeChip.text()).toContain('lock')

    // Other chips disabled
    const otherChip = wrapper.find('[data-testid="rule-chip-rule-custom"]')
    expect(otherChip.classes()).toContain('pointer-events-none')

    // Attempting to click other chip does not change selection
    await otherChip.trigger('click')
    expect(draftStore.ruleSystem).toBe('Official 3-Game Standard')
  })
})
